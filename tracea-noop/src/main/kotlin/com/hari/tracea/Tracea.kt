package com.hari.tracea

import android.content.Context
import com.hari.tracea.core.config.TraceaConfig
import com.hari.tracea.manual.ManualNetworkCall
import okhttp3.Interceptor

/**
 * No-op implementation of [Tracea] for release builds.
 * Provides identical API signatures with zero runtime overhead.
 */
object Tracea {

    private val noOpInterceptor = Interceptor { chain -> chain.proceed(chain.request()) }
    private val noOpManualCall = ManualNetworkCall()

    /**
     * Initialize the Tracea SDK (no-op in release builds).
     */
    fun initialize(context: Context, config: TraceaConfig = TraceaConfig()) {
        // No-op
    }

    /**
     * Get an OkHttp Interceptor that passes through requests without capture.
     */
    fun okHttpInterceptor(): Interceptor = noOpInterceptor

    /**
     * Start tracking a manual network request (no-op in release builds).
     */
    fun startRequest(method: String, url: String): ManualNetworkCall = noOpManualCall

    /**
     * Open the Tracea UI (no-op in release builds).
     */
    fun show(context: Context) {
        // No-op
    }

    fun isEnabled(): Boolean = false

    fun clear() {
        // No-op
    }

    /**
     * Start the embedded Web Dashboard server (no-op in release builds).
     */
    fun startWebServer(context: Context, port: Int = 8080): Boolean = false

    /**
     * Stop the embedded Web Dashboard server (no-op in release builds).
     */
    fun stopWebServer() {
        // No-op
    }

    /**
     * Get the local network URL for the Web Dashboard (empty in release builds).
     */
    fun getWebDashboardUrl(context: Context): String = ""
}
