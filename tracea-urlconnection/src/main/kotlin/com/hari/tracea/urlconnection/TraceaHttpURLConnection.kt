package com.hari.tracea.urlconnection

import com.hari.tracea.core.config.DomainFilterConfig
import com.hari.tracea.core.config.TraceaConfig
import com.hari.tracea.core.model.DebuggerSession
import com.hari.tracea.core.model.HttpMethod
import com.hari.tracea.core.model.NetworkError
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.model.NetworkEventState
import com.hari.tracea.core.model.NetworkSource
import com.hari.tracea.core.model.NetworkTiming
import com.hari.tracea.core.pipeline.NetworkEventCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilterInputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.Permission
import java.util.UUID

/**
 * Decorator for [HttpURLConnection] that captures request/response lifecycle
 * for the Tracea SDK without disrupting normal application behavior.
 */
open class TraceaHttpURLConnection(
    protected val delegate: HttpURLConnection,
    protected val collector: NetworkEventCollector,
    protected val config: TraceaConfig
) : HttpURLConnection(delegate.url) {

    protected val eventId: String = UUID.randomUUID().toString()
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    protected val requestHeadersMap = mutableMapOf<String, MutableList<String>>()

    protected var requestBodyBuffer = ByteArrayOutputStream()
    protected var responseBodyBuffer = ByteArrayOutputStream()

    protected var startNs: Long = 0L
    protected var startMs: Long = 0L
    protected var requestEventEmitted = false
    protected var responseCompleted = false

    protected var interceptedInputStream: InputStream? = null
    protected var interceptedErrorStream: InputStream? = null
    protected var interceptedOutputStream: OutputStream? = null

    init {
        // Initialize request properties from delegate if any were already set
        delegate.requestProperties.forEach { (key, values) ->
            if (key != null) {
                requestHeadersMap[key] = values.toMutableList()
            }
        }
    }

    private fun checkDomainFilter(): Boolean {
        val activeFilter = DomainFilterConfig.activeConfig ?: config.domainFilterConfig
        return activeFilter.shouldCaptureHost(delegate.url.host)
    }

    protected fun emitInitialRequestIfNeeded() {
        if (requestEventEmitted || !config.enabled || !checkDomainFilter()) return
        requestEventEmitted = true
        startNs = System.nanoTime()
        startMs = System.currentTimeMillis()

        val httpUrl = delegate.url
        val method = HttpMethod.from(delegate.requestMethod ?: "GET")
        val requestBytes = requestBodyBuffer.toByteArray()
        val contentTypeHeader = requestHeadersMap["Content-Type"]?.firstOrNull()
            ?: requestHeadersMap["content-type"]?.firstOrNull()

        val requestBodyData = BodyBufferHelper.createBodyData(
            bytes = requestBytes,
            contentTypeHeader = contentTypeHeader,
            config = config.bodyCaptureConfig,
            maxSize = config.bodyCaptureConfig.maxRequestBodySize
        )
        val requestSize = requestBodyData?.size ?: requestBytes.size.toLong()

        val queryParams = parseQueryParams(httpUrl.query)

        val requestEvent = NetworkEvent(
            id = eventId,
            timestamp = startMs,
            method = method,
            url = httpUrl.toString(),
            scheme = httpUrl.protocol ?: "http",
            host = httpUrl.host ?: "",
            port = if (httpUrl.port != -1) httpUrl.port else httpUrl.defaultPort,
            path = httpUrl.path ?: "/",
            queryParameters = queryParams,
            protocol = "HTTP/1.1",
            requestHeaders = requestHeadersMap.toMap(),
            requestBody = requestBodyData,
            requestContentType = contentTypeHeader,
            requestSize = requestSize,
            statusCode = null,
            statusMessage = null,
            responseHeaders = emptyMap(),
            responseBody = null,
            responseContentType = null,
            responseSize = 0L,
            timing = NetworkTiming(startTimestamp = startMs),
            error = null,
            source = NetworkSource.URL_CONNECTION,
            state = NetworkEventState.REQUEST_CAPTURED,
            sessionId = DebuggerSession.sessionId,
            sessionName = DebuggerSession.sessionName
        )

        scope.launch { collector.emit(requestEvent) }
    }

    protected fun emitFinalResponse(statusCode: Int, statusMessage: String?, error: NetworkError?) {
        if (responseCompleted || !config.enabled || !checkDomainFilter()) return
        responseCompleted = true

        val endNs = System.nanoTime()
        val endMs = System.currentTimeMillis()
        val durationMs = if (startNs > 0) (endNs - startNs) / 1_000_000L else 0L

        val respHeaders = delegate.headerFields
            ?.filterKeys { it != null }
            ?.mapValues { it.value ?: emptyList() }
            ?: emptyMap()

        val respBytes = responseBodyBuffer.toByteArray()
        val contentType = delegate.contentType

        val responseBodyData = BodyBufferHelper.createBodyData(
            bytes = respBytes,
            contentTypeHeader = contentType,
            config = config.bodyCaptureConfig,
            maxSize = config.bodyCaptureConfig.maxResponseBodySize
        )
        val responseSize = if (respBytes.isNotEmpty()) {
            responseBodyData?.size ?: respBytes.size.toLong()
        } else {
            delegate.contentLengthLong.coerceAtLeast(0L)
        }

        val httpUrl = delegate.url
        val method = HttpMethod.from(delegate.requestMethod ?: "GET")
        val finalError = error ?: NetworkError.fromStatusCode(statusCode)

        val event = NetworkEvent(
            id = eventId,
            timestamp = if (startMs > 0) startMs else endMs,
            method = method,
            url = httpUrl.toString(),
            scheme = httpUrl.protocol ?: "http",
            host = httpUrl.host ?: "",
            port = if (httpUrl.port != -1) httpUrl.port else httpUrl.defaultPort,
            path = httpUrl.path ?: "/",
            queryParameters = parseQueryParams(httpUrl.query),
            protocol = "HTTP/1.1",
            requestHeaders = requestHeadersMap.toMap(),
            requestBody = BodyBufferHelper.createBodyData(
                requestBodyBuffer.toByteArray(),
                requestHeadersMap["Content-Type"]?.firstOrNull(),
                config.bodyCaptureConfig,
                config.bodyCaptureConfig.maxRequestBodySize
            ),
            requestContentType = requestHeadersMap["Content-Type"]?.firstOrNull(),
            requestSize = requestBodyBuffer.size().toLong(),
            statusCode = statusCode,
            statusMessage = statusMessage,
            responseHeaders = respHeaders,
            responseBody = responseBodyData,
            responseContentType = contentType,
            responseSize = responseSize,
            timing = NetworkTiming(startTimestamp = startMs, endTimestamp = endMs),
            error = finalError,
            source = NetworkSource.URL_CONNECTION,
            state = if (finalError != null && statusCode >= 400) NetworkEventState.FAILED else NetworkEventState.COMPLETED,
            sessionId = DebuggerSession.sessionId,
            sessionName = DebuggerSession.sessionName
        )

        scope.launch { collector.emit(event) }
    }

    private fun emitError(e: Throwable) {
        if (responseCompleted || !config.enabled || !checkDomainFilter()) return
        responseCompleted = true

        val endNs = System.nanoTime()
        val endMs = System.currentTimeMillis()
        val durationMs = if (startNs > 0) (endNs - startNs) / 1_000_000L else 0L

        val httpUrl = delegate.url
        val method = HttpMethod.from(delegate.requestMethod ?: "GET")
        val networkError = NetworkError.fromThrowable(e)

        val event = NetworkEvent(
            id = eventId,
            timestamp = if (startMs > 0) startMs else endMs,
            method = method,
            url = httpUrl.toString(),
            scheme = httpUrl.protocol ?: "http",
            host = httpUrl.host ?: "",
            port = if (httpUrl.port != -1) httpUrl.port else httpUrl.defaultPort,
            path = httpUrl.path ?: "/",
            queryParameters = parseQueryParams(httpUrl.query),
            protocol = "HTTP/1.1",
            requestHeaders = requestHeadersMap.toMap(),
            requestBody = BodyBufferHelper.createBodyData(
                requestBodyBuffer.toByteArray(),
                requestHeadersMap["Content-Type"]?.firstOrNull(),
                config.bodyCaptureConfig,
                config.bodyCaptureConfig.maxRequestBodySize
            ),
            requestContentType = requestHeadersMap["Content-Type"]?.firstOrNull(),
            requestSize = requestBodyBuffer.size().toLong(),
            statusCode = null,
            statusMessage = null,
            responseHeaders = emptyMap(),
            responseBody = null,
            responseContentType = null,
            responseSize = 0L,
            timing = NetworkTiming(startTimestamp = startMs, endTimestamp = endMs),
            error = networkError,
            source = NetworkSource.URL_CONNECTION,
            state = NetworkEventState.FAILED,
            sessionId = DebuggerSession.sessionId,
            sessionName = DebuggerSession.sessionName
        )

        scope.launch { collector.emit(event) }
    }

    private fun parseQueryParams(query: String?): Map<String, List<String>> {
        if (query.isNullOrEmpty()) return emptyMap()
        val result = mutableMapOf<String, MutableList<String>>()
        query.split("&").forEach { pair ->
            val idx = pair.indexOf("=")
            if (idx != -1) {
                val key = pair.substring(0, idx)
                val value = pair.substring(idx + 1)
                result.getOrPut(key) { mutableListOf() }.add(value)
            } else {
                result.getOrPut(pair) { mutableListOf() }.add("")
            }
        }
        return result
    }

    // --- Delegate Methods ---

    override fun connect() {
        emitInitialRequestIfNeeded()
        try {
            delegate.connect()
        } catch (e: IOException) {
            emitError(e)
            throw e
        }
    }

    override fun disconnect() {
        delegate.disconnect()
    }

    override fun usingProxy(): Boolean = delegate.usingProxy()

    override fun getResponseCode(): Int {
        emitInitialRequestIfNeeded()
        return try {
            val code = delegate.responseCode
            val msg = runCatching { delegate.responseMessage }.getOrNull()
            emitFinalResponse(code, msg, null)
            code
        } catch (e: IOException) {
            emitError(e)
            throw e
        }
    }

    override fun getResponseMessage(): String? {
        emitInitialRequestIfNeeded()
        return try {
            delegate.responseMessage
        } catch (e: IOException) {
            emitError(e)
            throw e
        }
    }

    override fun getInputStream(): InputStream {
        emitInitialRequestIfNeeded()
        if (interceptedInputStream != null) return interceptedInputStream!!
        return try {
            val original = delegate.inputStream
            val maxCapture = config.bodyCaptureConfig.maxResponseBodySize
            val wrapped = object : FilterInputStream(original) {
                override fun read(): Int {
                    val b = super.read()
                    if (b != -1 && responseBodyBuffer.size() < maxCapture) {
                        responseBodyBuffer.write(b)
                    }
                    return b
                }

                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    val bytesRead = super.read(b, off, len)
                    if (bytesRead > 0 && responseBodyBuffer.size() < maxCapture) {
                        val toWrite = minOf(bytesRead, (maxCapture - responseBodyBuffer.size()).toInt())
                        responseBodyBuffer.write(b, off, toWrite)
                    }
                    return bytesRead
                }

                override fun close() {
                    try {
                        super.close()
                    } finally {
                        val code = runCatching { delegate.responseCode }.getOrDefault(200)
                        val msg = runCatching { delegate.responseMessage }.getOrNull()
                        emitFinalResponse(code, msg, null)
                    }
                }
            }
            interceptedInputStream = wrapped
            wrapped
        } catch (e: IOException) {
            emitError(e)
            throw e
        }
    }

    override fun getErrorStream(): InputStream? {
        emitInitialRequestIfNeeded()
        if (interceptedErrorStream != null) return interceptedErrorStream
        val original = delegate.errorStream ?: return null
        val maxCapture = config.bodyCaptureConfig.maxResponseBodySize
        val wrapped = object : FilterInputStream(original) {
            override fun read(): Int {
                val b = super.read()
                if (b != -1 && responseBodyBuffer.size() < maxCapture) {
                    responseBodyBuffer.write(b)
                }
                return b
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                val bytesRead = super.read(b, off, len)
                if (bytesRead > 0 && responseBodyBuffer.size() < maxCapture) {
                    val toWrite = minOf(bytesRead, (maxCapture - responseBodyBuffer.size()).toInt())
                    responseBodyBuffer.write(b, off, toWrite)
                }
                return bytesRead
            }

            override fun close() {
                try {
                    super.close()
                } finally {
                    val code = runCatching { delegate.responseCode }.getOrDefault(400)
                    val msg = runCatching { delegate.responseMessage }.getOrNull()
                    emitFinalResponse(code, msg, NetworkError.fromStatusCode(code))
                }
            }
        }
        interceptedErrorStream = wrapped
        return wrapped
    }

    override fun getOutputStream(): OutputStream {
        if (interceptedOutputStream != null) return interceptedOutputStream!!
        val original = delegate.outputStream
        val maxCapture = config.bodyCaptureConfig.maxRequestBodySize
        val wrapped = object : FilterOutputStream(original) {
            override fun write(b: Int) {
                super.write(b)
                if (requestBodyBuffer.size() < maxCapture) {
                    requestBodyBuffer.write(b)
                }
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                super.write(b, off, len)
                if (requestBodyBuffer.size() < maxCapture) {
                    val toWrite = minOf(len, (maxCapture - requestBodyBuffer.size()).toInt())
                    requestBodyBuffer.write(b, off, toWrite)
                }
            }

            override fun close() {
                super.close()
                emitInitialRequestIfNeeded()
            }
        }
        interceptedOutputStream = wrapped
        return wrapped
    }

    override fun setRequestMethod(method: String) {
        delegate.requestMethod = method
    }

    override fun getRequestMethod(): String = delegate.requestMethod ?: "GET"

    override fun setRequestProperty(key: String, value: String?) {
        delegate.setRequestProperty(key, value)
        if (value != null) {
            requestHeadersMap[key] = mutableListOf(value)
        } else {
            requestHeadersMap.remove(key)
        }
    }

    override fun addRequestProperty(key: String, value: String) {
        delegate.addRequestProperty(key, value)
        requestHeadersMap.getOrPut(key) { mutableListOf() }.add(value)
    }

    override fun getRequestProperty(key: String): String? = delegate.getRequestProperty(key)

    override fun getRequestProperties(): Map<String, List<String>> = delegate.requestProperties

    override fun getHeaderFields(): Map<String, List<String>> = delegate.headerFields ?: emptyMap()

    override fun getHeaderField(name: String?): String? = delegate.getHeaderField(name)

    override fun getHeaderField(n: Int): String? = delegate.getHeaderField(n)

    override fun getHeaderFieldKey(n: Int): String? = delegate.getHeaderFieldKey(n)

    override fun getHeaderFieldDate(name: String?, Default: Long): Long = delegate.getHeaderFieldDate(name, Default)

    override fun getHeaderFieldInt(name: String?, Default: Int): Int = delegate.getHeaderFieldInt(name, Default)

    override fun getHeaderFieldLong(name: String?, Default: Long): Long = delegate.getHeaderFieldLong(name, Default)

    override fun getContentLength(): Int = delegate.contentLength

    override fun getContentLengthLong(): Long = delegate.contentLengthLong

    override fun getContentType(): String? = delegate.contentType

    override fun getContentEncoding(): String? = delegate.contentEncoding

    override fun getExpiration(): Long = delegate.expiration

    override fun getDate(): Long = delegate.date

    override fun getLastModified(): Long = delegate.lastModified

    override fun setConnectTimeout(timeout: Int) { delegate.connectTimeout = timeout }
    override fun getConnectTimeout(): Int = delegate.connectTimeout

    override fun setReadTimeout(timeout: Int) { delegate.readTimeout = timeout }
    override fun getReadTimeout(): Int = delegate.readTimeout

    override fun setDoInput(doinput: Boolean) { delegate.doInput = doinput }
    override fun getDoInput(): Boolean = delegate.doInput

    override fun setDoOutput(dooutput: Boolean) { delegate.doOutput = dooutput }
    override fun getDoOutput(): Boolean = delegate.doOutput

    override fun setAllowUserInteraction(allow: Boolean) { delegate.allowUserInteraction = allow }
    override fun getAllowUserInteraction(): Boolean = delegate.allowUserInteraction

    override fun setUseCaches(use: Boolean) { delegate.useCaches = use }
    override fun getUseCaches(): Boolean = delegate.useCaches

    override fun setIfModifiedSince(time: Long) { delegate.ifModifiedSince = time }
    override fun getIfModifiedSince(): Long = delegate.ifModifiedSince

    override fun getDefaultUseCaches(): Boolean = delegate.defaultUseCaches
    override fun setDefaultUseCaches(default: Boolean) { delegate.defaultUseCaches = default }

    override fun setInstanceFollowRedirects(follow: Boolean) { delegate.instanceFollowRedirects = follow }
    override fun getInstanceFollowRedirects(): Boolean = delegate.instanceFollowRedirects

    override fun setChunkedStreamingMode(chunklen: Int) { delegate.setChunkedStreamingMode(chunklen) }
    override fun setFixedLengthStreamingMode(contentLength: Int) { delegate.setFixedLengthStreamingMode(contentLength) }
    override fun setFixedLengthStreamingMode(contentLength: Long) { delegate.setFixedLengthStreamingMode(contentLength) }

    override fun getPermission(): Permission = delegate.permission
    override fun getURL(): URL = delegate.url
}
