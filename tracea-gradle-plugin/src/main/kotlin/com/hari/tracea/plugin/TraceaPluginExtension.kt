package com.hari.tracea.plugin

/**
 * Extension for configuring the Tracea Gradle plugin.
 *
 * Usage in build.gradle.kts:
 * ```
 * tracea {
 *     enabled = true                          // default: true
 *     instrumentAllVariants = false            // default: false (debug only)
 *     excludePackages = listOf("com.example.internal")  // default: empty
 * }
 * ```
 */
open class TraceaPluginExtension {

    /**
     * Enable or disable automatic network instrumentation entirely.
     * When false, the plugin does nothing.
     */
    var enabled: Boolean = true

    /**
     * By default, only debug build variants are instrumented.
     * Set to true to instrument all variants (including release).
     * This is useful for production monitoring SDKs.
     */
    var instrumentAllVariants: Boolean = false

    /**
     * Additional package prefixes to exclude from instrumentation.
     * OkHttp clients created in classes matching these packages will NOT
     * be auto-instrumented.
     *
     * Built-in exclusions (always applied):
     * - com.hari.tracea.okhttp (Tracea SDK internals)
     * - okhttp3, okio (OkHttp internals)
     * - android, androidx (Android framework)
     */
    var excludePackages: List<String> = emptyList()
}
