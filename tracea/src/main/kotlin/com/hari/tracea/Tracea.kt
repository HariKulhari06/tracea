package com.hari.tracea

import android.content.Context
import android.content.Intent
import android.util.Log
import com.hari.tracea.core.config.TraceaConfig
import com.hari.tracea.core.pipeline.DefaultNetworkEventCollector
import com.hari.tracea.core.redaction.RedactionEngine
import com.hari.tracea.core.model.DebuggerSession
import com.hari.tracea.core.store.NetworkEventStore
import com.hari.tracea.manual.ManualCaptureApi
import com.hari.tracea.manual.ManualNetworkCall
import com.hari.tracea.okhttp.TraceaInterceptor
import com.hari.tracea.storage.RoomNetworkEventStore
import com.hari.tracea.ui.TraceaServiceLocator
import com.hari.tracea.ui.TraceaActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object Tracea {
    private const val TAG = "Tracea"
    private var initialized = false
    private lateinit var _config: TraceaConfig
    private lateinit var _collector: DefaultNetworkEventCollector  // from core pipeline
    private lateinit var _store: NetworkEventStore
    private lateinit var _redactionEngine: RedactionEngine  // from core redaction
    private lateinit var _manualApi: ManualCaptureApi  // from manual module
    private var _interceptor: TraceaInterceptor? = null  // from okhttp module
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** No-op interceptor returned when Tracea is not initialized or disabled. */
    private val noOpInterceptor = okhttp3.Interceptor { chain -> chain.proceed(chain.request()) }

    /**
     * Initialize the Tracea SDK. Call this in Application.onCreate().
     */
    fun initialize(context: Context, config: TraceaConfig = TraceaConfig()) {
        if (initialized) return
        if (!config.enabled) {
            initialized = true
            _config = config
            return
        }
        
        _config = config
        _collector = DefaultNetworkEventCollector()
        _redactionEngine = RedactionEngine(config.redactionConfig)
        _store = RoomNetworkEventStore(context, config.storageConfig)
        _manualApi = ManualCaptureApi(_collector, config)
        
        com.hari.tracea.core.mock.MockEngine.initialize(context)
        
        // Set up the UI service locator
        TraceaServiceLocator.store = _store
        TraceaServiceLocator.config = config
        DebuggerSession.startNewSession()
        TraceaServiceLocator.sessionId = DebuggerSession.sessionId
        TraceaServiceLocator.sessionName = DebuggerSession.sessionName
        
        // Initialize Floating Button Overlay Manager
        val app = context.applicationContext as? android.app.Application
        if (app != null) {
            com.hari.tracea.ui.overlay.FloatingButtonManager.init(app, config)
        }

        // Wire Web Server store & Activity Tracker
        com.hari.tracea.web.TraceaWebServer.store = _store
        (context.applicationContext as? android.app.Application)?.let {
            com.hari.tracea.web.TraceaActivityTracker.register(it)
        }
        
        // Start pipeline: collector events -> redaction -> store
        scope.launch {
            _collector.events.collect { event ->
                try {
                    val redactedEvent = _redactionEngine.redactEvent(event)
                    _store.insert(redactedEvent)
                } catch (e: Exception) {
                    // Silently handle storage errors
                }
            }
        }

        // Keep floating overlay request count up-to-date
        scope.launch {
            _store.getAll().collect { events ->
                com.hari.tracea.ui.overlay.FloatingButtonManager.updateRequestCount(events.size)
            }
        }
        
        initialized = true
    }

    /**
     * Dynamically enable or disable the floating debug overlay button.
     */
    fun setFloatingButtonEnabled(enabled: Boolean) {
        if (initialized) {
            com.hari.tracea.ui.overlay.FloatingButtonManager.setEnabled(enabled)
        }
    }

    /**
     * Get an OkHttp Interceptor that automatically captures network requests.
     * Returns a no-op interceptor if Tracea is not initialized or disabled.
     */
    fun okHttpInterceptor(): okhttp3.Interceptor {
        if (!initialized) {
            Log.w(TAG, "Tracea.initialize() was not called. Returning no-op interceptor. Network calls will not be captured.")
            return noOpInterceptor
        }
        if (!_config.enabled) {
            return noOpInterceptor
        }
        val interceptor = _interceptor ?: TraceaInterceptor(_collector, _config).also { _interceptor = it }
        return interceptor
    }

    /**
     * Start tracking a manual network request.
     * Returns null if Tracea is not initialized or disabled.
     */
    fun startRequest(method: String, url: String): ManualNetworkCall? {
        if (!initialized) {
            Log.w(TAG, "Tracea.initialize() was not called. Manual request for '$method $url' will not be captured.")
            return null
        }
        if (!_config.enabled) {
            Log.d(TAG, "Tracea is disabled. Manual request for '$method $url' will not be captured.")
            return null
        }
        return _manualApi.startRequest(method, url)
    }

    /**
     * Open the Tracea UI.
     * Does nothing if Tracea is not initialized.
     */
    fun show(context: Context) {
        if (!initialized) {
            Log.w(TAG, "Tracea.initialize() was not called. Cannot show Tracea UI.")
            return
        }
        val intent = Intent(context, TraceaActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun isEnabled(): Boolean = if (initialized) _config.enabled else false

    fun clear() {
        if (initialized && _config.enabled) {
            scope.launch { _store.clear() }
        }
    }

    /**
     * Start the embedded Web Dashboard server.
     * Returns false if Tracea is not initialized.
     */
    fun startWebServer(context: Context, port: Int = 8080): Boolean {
        if (!initialized) {
            Log.w(TAG, "Tracea.initialize() was not called. Cannot start web server.")
            return false
        }
        return com.hari.tracea.web.TraceaWebServer.start(context, port)
    }

    /**
     * Stop the embedded Web Dashboard server.
     */
    fun stopWebServer() {
        if (initialized) {
            com.hari.tracea.web.TraceaWebServer.stop()
        }
    }

    /**
     * Get the local network URL for the Web Dashboard.
     */
    fun getWebDashboardUrl(context: Context): String {
        return com.hari.tracea.web.TraceaWebServer.getDashboardUrl(context)
    }
}
