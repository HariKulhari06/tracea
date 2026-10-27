package com.hari.tracea.urlconnection

import com.hari.tracea.core.config.BodyCaptureConfig
import com.hari.tracea.core.config.TraceaConfig
import com.hari.tracea.core.model.BodyContentType
import com.hari.tracea.core.model.BodyData
import com.hari.tracea.core.pipeline.DefaultNetworkEventCollector
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class TraceaUrlConnectionTrackerTest {

    @After
    fun tearDown() {
        TraceaUrlConnectionTracker.reset()
    }

    @Test
    fun testWrapWhenNotInstalledReturnsOriginal() {
        TraceaUrlConnectionTracker.reset()
        val mockConnection = createMockHttpURLConnection("https://example.com/test")

        val wrapped = TraceaUrlConnectionTracker.wrap(mockConnection)
        assertSame(mockConnection, wrapped)
    }

    @Test
    fun testWrapWhenInstalledReturnsTraceaHttpURLConnection() {
        val collector = DefaultNetworkEventCollector()
        val config = TraceaConfig(enabled = true)
        TraceaUrlConnectionTracker.install(collector, config)

        val mockConnection = createMockHttpURLConnection("http://example.com/test")
        val wrapped = TraceaUrlConnectionTracker.wrap(mockConnection)

        assertTrue(wrapped is TraceaHttpURLConnection)
    }

    @Test
    fun testWrapAvoidsDoubleWrapping() {
        val collector = DefaultNetworkEventCollector()
        val config = TraceaConfig(enabled = true)
        TraceaUrlConnectionTracker.install(collector, config)

        val mockConnection = createMockHttpURLConnection("http://example.com/test")
        val wrapped1 = TraceaUrlConnectionTracker.wrap(mockConnection)
        val wrapped2 = TraceaUrlConnectionTracker.wrap(wrapped1)

        assertSame(wrapped1, wrapped2)
    }

    @Test
    fun testWrapWhenDisabledReturnsOriginal() {
        val collector = DefaultNetworkEventCollector()
        val config = TraceaConfig(enabled = false)
        TraceaUrlConnectionTracker.install(collector, config)

        val mockConnection = createMockHttpURLConnection("http://example.com/test")
        val wrapped = TraceaUrlConnectionTracker.wrap(mockConnection)

        assertSame(mockConnection, wrapped)
    }

    @Test
    fun testBodyBufferHelperContentTypeParsing() {
        assertEquals(BodyContentType.JSON, BodyBufferHelper.parseContentType("application/json; charset=utf-8"))
        assertEquals(BodyContentType.XML, BodyBufferHelper.parseContentType("text/xml"))
        assertEquals(BodyContentType.HTML, BodyBufferHelper.parseContentType("text/html; charset=UTF-8"))
        assertEquals(BodyContentType.FORM, BodyBufferHelper.parseContentType("application/x-www-form-urlencoded"))
        assertEquals(BodyContentType.MULTIPART, BodyBufferHelper.parseContentType("multipart/form-data"))
        assertEquals(BodyContentType.IMAGE, BodyBufferHelper.parseContentType("image/png"))
        assertEquals(BodyContentType.UNKNOWN, BodyBufferHelper.parseContentType(null))
    }

    @Test
    fun testBodyBufferHelperBodyDataCreation() {
        val jsonBytes = """{"status":"ok"}""".toByteArray()
        val config = BodyCaptureConfig()
        val bodyData = BodyBufferHelper.createBodyData(jsonBytes, "application/json", config, 1024L)

        assertTrue(bodyData is BodyData.Text)
        assertEquals("""{"status":"ok"}""", (bodyData as BodyData.Text).content)
        assertEquals(BodyContentType.JSON, bodyData.contentType)
    }

    private fun createMockHttpURLConnection(urlString: String): HttpURLConnection {
        return object : HttpURLConnection(URL(urlString)) {
            override fun disconnect() {}
            override fun usingProxy(): Boolean = false
            override fun connect() {}
        }
    }
}
