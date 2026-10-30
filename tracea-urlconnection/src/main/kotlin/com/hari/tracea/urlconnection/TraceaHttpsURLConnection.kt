package com.hari.tracea.urlconnection

import com.hari.tracea.core.config.TraceaConfig
import com.hari.tracea.core.pipeline.NetworkEventCollector
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.security.Permission
import java.security.Principal
import java.security.cert.Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * Decorator for [HttpsURLConnection] that captures request/response lifecycle
 * for the Tracea SDK without disrupting normal application behavior.
 */
class TraceaHttpsURLConnection(
    private val httpsDelegate: HttpsURLConnection,
    collector: NetworkEventCollector,
    config: TraceaConfig
) : HttpsURLConnection(httpsDelegate.url) {

    private val baseHttp = TraceaHttpURLConnection(httpsDelegate, collector, config)

    // --- HttpsURLConnection abstract & specific methods ---

    override fun getCipherSuite(): String = httpsDelegate.cipherSuite

    override fun getLocalCertificates(): Array<Certificate>? = httpsDelegate.localCertificates

    override fun getServerCertificates(): Array<Certificate> = httpsDelegate.serverCertificates

    override fun getPeerPrincipal(): Principal = httpsDelegate.peerPrincipal

    override fun getLocalPrincipal(): Principal? = httpsDelegate.localPrincipal

    override fun setHostnameVerifier(v: HostnameVerifier?) {
        httpsDelegate.hostnameVerifier = v
    }

    override fun getHostnameVerifier(): HostnameVerifier = httpsDelegate.hostnameVerifier

    override fun setSSLSocketFactory(sf: SSLSocketFactory?) {
        httpsDelegate.sslSocketFactory = sf
    }

    override fun getSSLSocketFactory(): SSLSocketFactory = httpsDelegate.sslSocketFactory

    // --- HttpURLConnection & URLConnection methods delegated to baseHttp ---

    override fun connect() = baseHttp.connect()
    override fun disconnect() = baseHttp.disconnect()
    override fun usingProxy(): Boolean = baseHttp.usingProxy()

    override fun getResponseCode(): Int = baseHttp.responseCode
    override fun getResponseMessage(): String? = baseHttp.responseMessage

    override fun getInputStream(): InputStream = baseHttp.inputStream
    override fun getErrorStream(): InputStream? = baseHttp.errorStream
    override fun getOutputStream(): OutputStream = baseHttp.outputStream

    override fun setRequestMethod(method: String) {
        baseHttp.requestMethod = method
    }

    override fun getRequestMethod(): String = baseHttp.requestMethod

    override fun setRequestProperty(key: String, value: String?) = baseHttp.setRequestProperty(key, value)
    override fun addRequestProperty(key: String, value: String) = baseHttp.addRequestProperty(key, value)
    override fun getRequestProperty(key: String): String? = baseHttp.getRequestProperty(key)
    override fun getRequestProperties(): Map<String, List<String>> = baseHttp.requestProperties

    override fun getHeaderFields(): Map<String, List<String>> = baseHttp.headerFields
    override fun getHeaderField(name: String?): String? = baseHttp.getHeaderField(name)
    override fun getHeaderField(n: Int): String? = baseHttp.getHeaderField(n)
    override fun getHeaderFieldKey(n: Int): String? = baseHttp.getHeaderFieldKey(n)
    override fun getHeaderFieldDate(name: String?, Default: Long): Long = baseHttp.getHeaderFieldDate(name, Default)
    override fun getHeaderFieldInt(name: String?, Default: Int): Int = baseHttp.getHeaderFieldInt(name, Default)
    override fun getHeaderFieldLong(name: String?, Default: Long): Long = baseHttp.getHeaderFieldLong(name, Default)

    override fun getContentLength(): Int = baseHttp.contentLength
    override fun getContentLengthLong(): Long = baseHttp.contentLengthLong
    override fun getContentType(): String? = baseHttp.contentType
    override fun getContentEncoding(): String? = baseHttp.contentEncoding
    override fun getExpiration(): Long = baseHttp.expiration
    override fun getDate(): Long = baseHttp.date
    override fun getLastModified(): Long = baseHttp.lastModified

    override fun setConnectTimeout(timeout: Int) { baseHttp.connectTimeout = timeout }
    override fun getConnectTimeout(): Int = baseHttp.connectTimeout

    override fun setReadTimeout(timeout: Int) { baseHttp.readTimeout = timeout }
    override fun getReadTimeout(): Int = baseHttp.readTimeout

    override fun setDoInput(doinput: Boolean) { baseHttp.doInput = doinput }
    override fun getDoInput(): Boolean = baseHttp.doInput

    override fun setDoOutput(dooutput: Boolean) { baseHttp.doOutput = dooutput }
    override fun getDoOutput(): Boolean = baseHttp.doOutput

    override fun setAllowUserInteraction(allow: Boolean) { baseHttp.allowUserInteraction = allow }
    override fun getAllowUserInteraction(): Boolean = baseHttp.allowUserInteraction

    override fun setUseCaches(use: Boolean) { baseHttp.useCaches = use }
    override fun getUseCaches(): Boolean = baseHttp.useCaches

    override fun setIfModifiedSince(time: Long) { baseHttp.ifModifiedSince = time }
    override fun getIfModifiedSince(): Long = baseHttp.ifModifiedSince

    override fun getDefaultUseCaches(): Boolean = baseHttp.defaultUseCaches
    override fun setDefaultUseCaches(default: Boolean) { baseHttp.defaultUseCaches = default }

    override fun setInstanceFollowRedirects(follow: Boolean) { baseHttp.instanceFollowRedirects = follow }
    override fun getInstanceFollowRedirects(): Boolean = baseHttp.instanceFollowRedirects

    override fun setChunkedStreamingMode(chunklen: Int) { baseHttp.setChunkedStreamingMode(chunklen) }
    override fun setFixedLengthStreamingMode(contentLength: Int) { baseHttp.setFixedLengthStreamingMode(contentLength) }
    override fun setFixedLengthStreamingMode(contentLength: Long) { baseHttp.setFixedLengthStreamingMode(contentLength) }

    override fun getPermission(): Permission = baseHttp.permission
    override fun getURL(): URL = baseHttp.url
}
