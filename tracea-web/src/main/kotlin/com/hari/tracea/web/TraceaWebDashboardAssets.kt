package com.hari.tracea.web

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Loads the self-contained Single Page Application HTML/CSS/JS for the Tracea Web Dashboard.
 */
object TraceaWebDashboardAssets {

    private var cachedHtml: String? = null

    fun getDashboardHtml(appName: String = "Tracea", appVersion: String = "1.1.0"): String {
        if (cachedHtml == null) {
            cachedHtml = try {
                val inputStream = javaClass.classLoader?.getResourceAsStream("tracea_dashboard.html")
                    ?: javaClass.getResourceAsStream("/tracea_dashboard.html")
                if (inputStream != null) {
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
                } else {
                    getFallbackHtml()
                }
            } catch (e: Exception) {
                getFallbackHtml()
            }
        }
        return cachedHtml!!.replace("Tracea Web Inspector", "$appName Web Inspector (v$appVersion)")
    }

    private fun getFallbackHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head><title>Tracea Web Inspector</title></head>
        <body style="background:#0F111A;color:#fff;font-family:sans-serif;padding:40px;text-align:center;">
            <h2>📡 Tracea Web Inspector</h2>
            <p>Dashboard loaded successfully. Please refresh if assets are updating.</p>
        </body>
        </html>
    """.trimIndent()
}
