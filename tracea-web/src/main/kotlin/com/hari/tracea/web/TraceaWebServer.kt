package com.hari.tracea.web

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.store.NetworkEventStore
import com.hari.tracea.core.util.HarExporter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Embedded HTTP and WebSocket server that hosts the Tracea Web Dashboard on the local network.
 */
object TraceaWebServer {

    private var server: io.ktor.server.engine.EmbeddedServer<*, *>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _port = MutableStateFlow(8080)
    val port: StateFlow<Int> = _port

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount

    var store: NetworkEventStore? = null

    /**
     * Starts the embedded Tracea Web Server on the specified port.
     */
    fun start(context: Context, port: Int = 8080): Boolean {
        if (_isRunning.value) return true

        return try {
            _port.value = port
            val appName = try {
                val appInfo = context.applicationInfo
                context.packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                "Tracea"
            }

            server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                install(CORS) {
                    anyHost()
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader(HttpHeaders.Authorization)
                }
                install(WebSockets)

                routing {
                    // 1. Web Dashboard UI
                    get("/") {
                        call.respondText(
                            text = TraceaWebDashboardAssets.getDashboardHtml(appName = appName),
                            contentType = ContentType.Text.Html
                        )
                    }

                    // 2. Status Endpoint
                    get("/api/status") {
                        val eventsCount = store?.getAll()?.first()?.size ?: 0
                        val statusJson = buildStatusJson(context, appName, eventsCount)
                        call.respondText(statusJson, ContentType.Application.Json)
                    }

                    // 3. Transactions List
                    get("/api/events") {
                        val events = store?.getAll()?.first() ?: emptyList()
                        val eventsJson = NetworkEventJsonSerializer.serializeList(events).toString()
                        call.respondText(eventsJson, ContentType.Application.Json)
                    }

                    // 4. Single Transaction Detail
                    get("/api/events/{id}") {
                        val id = call.parameters["id"]
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Missing transaction ID")
                            return@get
                        }
                        val event = store?.get(id)
                        if (event != null) {
                            val eventJson = NetworkEventJsonSerializer.serialize(event).toString()
                            call.respondText(eventJson, ContentType.Application.Json)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Transaction not found")
                        }
                    }

                    // 5. Clear Transactions
                    delete("/api/events") {
                        store?.clear()
                        call.respond(HttpStatusCode.OK, """{"status":"cleared"}""")
                    }

                    // 6. Export HAR
                    get("/api/export/har") {
                        val singleId = call.parameters["id"]
                        val sessionId = call.parameters["sessionId"]
                        val all = store?.getAll()?.first() ?: emptyList()

                        val events = when {
                            singleId != null -> {
                                val event = store?.get(singleId)
                                if (event != null) listOf(event) else emptyList()
                            }
                            sessionId != null -> {
                                all.filter { it.sessionId == sessionId || it.sessionName == sessionId }
                            }
                            else -> all
                        }

                        val harString = HarExporter.exportToHarString(events)
                        val filename = if (sessionId != null) "tracea_session_${System.currentTimeMillis()}.har" else "tracea_export_${System.currentTimeMillis()}.har"
                        call.response.header(
                            HttpHeaders.ContentDisposition,
                            "attachment; filename=\"$filename\""
                        )
                        call.respondText(harString, ContentType.Application.Json)
                    }

                    // 7. Live Device Screenshot
                    get("/api/device/screenshot") {
                        val screenshotBytes = TraceaActivityTracker.captureCurrentScreen()
                        if (screenshotBytes != null) {
                            call.respondBytes(
                                bytes = screenshotBytes,
                                contentType = ContentType.Image.PNG
                            )
                        } else {
                            call.respond(HttpStatusCode.NotFound, "No active foreground screen available")
                        }
                    }

                    // 8. Real-Time WebSocket Traffic Stream
                    webSocket("/ws/traffic") {
                        _connectedClientsCount.value++
                        var streamingJob: kotlinx.coroutines.Job? = null
                        try {
                            // Send initial snapshot
                            val initialEvents = store?.getAll()?.first() ?: emptyList()
                            val initialJson = NetworkEventJsonSerializer.serializeList(initialEvents).toString()
                            send(Frame.Text(initialJson))

                            // Launch coroutine to stream ongoing updates
                            streamingJob = launch {
                                store?.getAll()?.collect { updatedEvents ->
                                    val updatedJson = NetworkEventJsonSerializer.serializeList(updatedEvents).toString()
                                    send(Frame.Text(updatedJson))
                                }
                            }

                            // Keep connection open by listening for incoming frames / pings from client
                            for (frame in incoming) {
                                // Consuming incoming frames prevents Ktor from closing the socket
                            }
                        } catch (e: Exception) {
                            // Client disconnected normally or error
                        } finally {
                            streamingJob?.cancel()
                            _connectedClientsCount.value = (_connectedClientsCount.value - 1).coerceAtLeast(0)
                        }
                    }
                }
            }.start(wait = false)

            _isRunning.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isRunning.value = false
            false
        }
    }

    /**
     * Stops the embedded Web Server.
     */
    fun stop() {
        if (!_isRunning.value) return
        try {
            server?.stop(1000, 2000)
            server = null
            _isRunning.value = false
            _connectedClientsCount.value = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves the device's local Wi-Fi IPv4 address.
     */
    fun getLocalIpAddress(context: Context): String? {
        try {
            // First attempt: NetworkInterfaces scan
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (host != null && !host.startsWith("127.")) {
                            return host
                        }
                    }
                }
            }

            // Fallback: WifiManager
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    fun getDashboardUrl(context: Context): String {
        val ip = getLocalIpAddress(context) ?: "127.0.0.1"
        return "http://$ip:${_port.value}"
    }

    private fun buildStatusJson(context: Context, appName: String, totalRequests: Int): String {
        val ip = getLocalIpAddress(context) ?: "127.0.0.1"
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        return """
        {
            "appName": "$appName",
            "deviceModel": "$deviceModel",
            "osVersion": "$osVersion",
            "ipAddress": "$ip",
            "port": ${_port.value},
            "isRunning": ${_isRunning.value},
            "totalRequests": $totalRequests,
            "connectedClients": ${_connectedClientsCount.value}
        }
        """.trimIndent()
    }
}
