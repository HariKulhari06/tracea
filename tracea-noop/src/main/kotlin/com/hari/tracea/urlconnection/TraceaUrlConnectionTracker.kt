package com.hari.tracea.urlconnection

import java.net.URLConnection

/**
 * No-op version of [TraceaUrlConnectionTracker] for release builds.
 * Returns the raw URLConnection unchanged with zero runtime overhead.
 */
object TraceaUrlConnectionTracker {

    @JvmStatic
    fun wrap(connection: URLConnection): URLConnection = connection

    @JvmStatic
    fun reset() {
        // No-op
    }
}
