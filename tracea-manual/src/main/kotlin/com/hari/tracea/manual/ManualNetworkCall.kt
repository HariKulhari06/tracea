package com.hari.tracea.manual

import com.hari.tracea.core.config.TraceaConfig
import com.hari.tracea.core.model.BodyContentType
import com.hari.tracea.core.model.BodyData
import com.hari.tracea.core.model.ErrorType
import com.hari.tracea.core.model.HttpMethod
import com.hari.tracea.core.model.NetworkError
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.model.NetworkEventState
import com.hari.tracea.core.model.NetworkSource
import com.hari.tracea.core.model.DebuggerSession
import com.hari.tracea.core.model.NetworkTiming
import com.hari.tracea.core.pipeline.NetworkEventCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URI
import java.net.URLDecoder

/**
 * Builder-style API for manually tracking a network call lifecycle.
 *
 * Use [ManualCaptureApi.startRequest] to construct instances of this class.
 */
class ManualNetworkCall internal constructor(
    private val id: String,
    private val method: HttpMethod,
    private val url: String,
    private val collector: NetworkEventCollector,
    private val config: TraceaConfig,
    private val scope: CoroutineScope
) {
    private var state: NetworkEventState = NetworkEventState.STARTED
    private var requestHeaders: Map<String, List<String>> = emptyMap()
    private var requestBody: BodyData? = null
    private var requestContentType: String? = null
    private var requestSize: Long = 0L
    private var statusCode: Int? = null
    private var statusMessage: String? = null
    private var responseHeaders: Map<String, List<String>> = emptyMap()
    private var responseBody: BodyData? = null
    private var responseContentType: String? = null
    private var responseSize: Long = 0L
    private var error: NetworkError? = null
    private val startTime = System.currentTimeMillis()
    private var endTime: Long? = null
    private var dnsMs: Long? = null
    private var connectMs: Long? = null
    private var tlsMs: Long? = null
    private var waitingMs: Long? = null
    private var downloadMs: Long? = null

    /**
     * Sets detailed network timing breakdown for testing or manual logging.
     *
     * @param dnsMs DNS lookup duration in ms
     * @param connectMs TCP connect duration in ms
     * @param tlsMs TLS handshake duration in ms
     * @param waitingMs Time to first byte (TTFB) in ms
     * @param downloadMs Content download duration in ms
     * @return This builder instance for chaining
     */
    fun timing(
        dnsMs: Long? = null,
        connectMs: Long? = null,
        tlsMs: Long? = null,
        waitingMs: Long? = null,
        downloadMs: Long? = null
    ): ManualNetworkCall {
        this.dnsMs = dnsMs
        this.connectMs = connectMs
        this.tlsMs = tlsMs
        this.waitingMs = waitingMs
        this.downloadMs = downloadMs
        return this
    }

    /**
     * Sets request headers for the network call.
     *
     * @param headers Map of header names to header values
     * @return This builder instance for chaining
     */
    fun requestHeaders(headers: Map<String, String>): ManualNetworkCall {
        this.requestHeaders = headers.mapValues { listOf(it.value) }
        if (state == NetworkEventState.STARTED) {
            state = NetworkEventState.REQUEST_CAPTURED
        }
        return this
    }

    /**
     * Sets request body content and content type.
     *
     * @param body Raw body text
     * @param contentType Optional MIME content type (e.g. "application/json")
     * @return This builder instance for chaining
     */
    fun requestBody(body: String, contentType: String? = null): ManualNetworkCall {
        val bytes = body.toByteArray(Charsets.UTF_8)
        this.requestSize = bytes.size.toLong()
        this.requestContentType = contentType
        val type = BodyContentType.fromContentType(contentType)
        this.requestBody = if (config.bodyCaptureConfig.enabled) {
            if (requestSize > config.bodyCaptureConfig.maxRequestBodySize) {
                BodyData.Truncated(
                    actualSize = requestSize,
                    capturedSize = config.bodyCaptureConfig.maxRequestBodySize,
                    contentType = type
                )
            } else {
                BodyData.Text(
                    content = body,
                    contentType = type,
                    size = requestSize
                )
            }
        } else null

        if (state == NetworkEventState.STARTED) {
            state = NetworkEventState.REQUEST_CAPTURED
        }
        return this
    }

    /**
     * Completes the call with response details.
     *
     * @param statusCode HTTP response status code
     * @param headers Optional response headers map
     * @param body Optional response body string
     * @param contentType Optional response MIME content type
     * @return This builder instance for chaining
     */
    fun response(
        statusCode: Int,
        headers: Map<String, String>? = null,
        body: String? = null,
        contentType: String? = null
    ): ManualNetworkCall {
        this.statusCode = statusCode
        this.statusMessage = getStatusMessage(statusCode)
        if (headers != null) {
            this.responseHeaders = headers.mapValues { listOf(it.value) }
        }
        this.responseContentType = contentType
        if (body != null) {
            val bytes = body.toByteArray(Charsets.UTF_8)
            this.responseSize = bytes.size.toLong()
            val type = BodyContentType.fromContentType(contentType)
            this.responseBody = if (config.bodyCaptureConfig.enabled) {
                if (responseSize > config.bodyCaptureConfig.maxResponseBodySize) {
                    BodyData.Truncated(
                        actualSize = responseSize,
                        capturedSize = config.bodyCaptureConfig.maxResponseBodySize,
                        contentType = type
                    )
                } else {
                    BodyData.Text(
                        content = body,
                        contentType = type,
                        size = responseSize
                    )
                }
            } else null
        }
        state = NetworkEventState.RESPONSE_RECEIVED
        state = NetworkEventState.COMPLETED
        finishAndEmit()
        return this
    }

    /**
     * Marks the network call as failed due to an exception.
     *
     * @param throwable Exception that caused the failure
     * @return This builder instance for chaining
     */
    fun failure(throwable: Throwable): ManualNetworkCall {
        state = NetworkEventState.FAILED
        this.error = NetworkError.fromThrowable(throwable)
        finishAndEmit()
        return this
    }

    /**
     * Marks the network call as cancelled.
     *
     * @return This builder instance for chaining
     */
    fun cancel(): ManualNetworkCall {
        state = NetworkEventState.CANCELLED
        this.error = NetworkError(
            type = ErrorType.CANCELLED,
            message = "Manual network call was cancelled",
            throwableClassName = null
        )
        finishAndEmit()
        return this
    }

    private fun finishAndEmit() {
        val now = System.currentTimeMillis()
        this.endTime = now

        val parsedUrl = parseUrl(url)

        val timing = NetworkTiming(
            startTimestamp = startTime,
            endTimestamp = now,
            dnsMs = dnsMs,
            connectMs = connectMs,
            tlsMs = tlsMs,
            waitingMs = waitingMs,
            downloadMs = downloadMs
        )

        val event = NetworkEvent(
            id = id,
            timestamp = startTime,
            method = method,
            url = url,
            scheme = parsedUrl.scheme,
            host = parsedUrl.host,
            port = parsedUrl.port,
            path = parsedUrl.path,
            queryParameters = parsedUrl.queryParameters,
            protocol = null,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            requestContentType = requestContentType,
            requestSize = requestSize,
            statusCode = statusCode,
            statusMessage = statusMessage,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            responseContentType = responseContentType,
            responseSize = responseSize,
            timing = timing,
            error = error,
            source = NetworkSource.MANUAL,
            state = state,
            sessionId = DebuggerSession.sessionId,
            sessionName = DebuggerSession.sessionName
        )

        scope.launch {
            collector.emit(event)
        }
    }

    private fun parseUrl(url: String): ParsedUrl {
        return try {
            val uri = URI(url)
            val scheme = uri.scheme.orEmpty()
            val host = uri.host.orEmpty()
            val port = if (uri.port != -1) uri.port else null
            val path = uri.path.orEmpty()
            val queryParams = parseQueryParams(uri.rawQuery)
            ParsedUrl(scheme, host, port, path, queryParams)
        } catch (e: Exception) {
            ParsedUrl(
                scheme = "",
                host = "",
                port = null,
                path = url,
                queryParameters = emptyMap()
            )
        }
    }

    private fun parseQueryParams(rawQuery: String?): Map<String, List<String>> {
        if (rawQuery.isNullOrEmpty()) return emptyMap()
        val map = mutableMapOf<String, MutableList<String>>()
        rawQuery.split("&").forEach { param ->
            if (param.isNotEmpty()) {
                val parts = param.split("=", limit = 2)
                val key = try {
                    URLDecoder.decode(parts[0], "UTF-8")
                } catch (e: Exception) {
                    parts[0]
                }
                val value = if (parts.size > 1) {
                    try {
                        URLDecoder.decode(parts[1], "UTF-8")
                    } catch (e: Exception) {
                        parts[1]
                    }
                } else ""
                map.getOrPut(key) { mutableListOf() }.add(value)
            }
        }
        return map
    }

    private fun getStatusMessage(statusCode: Int): String {
        return when (statusCode) {
            200 -> "OK"
            201 -> "Created"
            202 -> "Accepted"
            204 -> "No Content"
            301 -> "Moved Permanently"
            302 -> "Found"
            304 -> "Not Modified"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            409 -> "Conflict"
            422 -> "Unprocessable Entity"
            429 -> "Too Many Requests"
            500 -> "Internal Server Error"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Timeout"
            else -> "HTTP $statusCode"
        }
    }

    private data class ParsedUrl(
        val scheme: String,
        val host: String,
        val port: Int?,
        val path: String,
        val queryParameters: Map<String, List<String>>
    )
}
