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
     * This is useful for production monitoring SDKs or internal test builds.
     */
    var instrumentAllVariants: Boolean = false

    /**
     * Specific variant names or patterns (e.g. `listOf("qa", "internal", "debug")`) to instrument.
     * If specified, only variants matching any of these substrings (case-insensitive) will be instrumented.
     * When empty (default), falls back to [instrumentAllVariants] or variants containing "debug".
     */
    var includeVariants: List<String> = emptyList()

    /**
     * Specific variant names or patterns (e.g. `listOf("playstore", "prodRelease")`) to exclude from instrumentation.
     * Any variant matching any of these substrings (case-insensitive) will NEVER be instrumented.
     */
    var excludeVariants: List<String> = emptyList()

    /**
     * Whether to instrument third-party external dependencies (JARs and AARs) in addition to project code.
     *
     * - `true` (default): Scans both project code and third-party libraries (e.g., Mixpanel, Firebase,
     *   analytics SDKs) so their internal HTTP calls are automatically captured by Tracea.
     * - `false`: Scans ONLY project source code (`InstrumentationScope.PROJECT`). Recommended if you
     *   only need to capture your own app's calls and want maximum build speed during iterative development.
     */
    var instrumentDependencies: Boolean = true

    /**
     * Additional package prefixes to exclude from instrumentation.
     * OkHttp clients created in classes matching these packages will NOT
     * be auto-instrumented.
     *
     * Built-in exclusions (always applied):
     * - com.hari.tracea (Tracea SDK internals)
     * - okhttp3, okio (OkHttp internals)
     * - android, androidx (Android framework)
     * - kotlin, kotlinx (Kotlin stdlib and extensions)
     * - com.google.android, com.google.common, com.google.protobuf (Google SDKs & utilities)
     * - org.bouncycastle, io.reactivex, org.jetbrains (utility/crypto/reactive libraries)
     * - Android resource classes (*.R, *.R$*, *.BuildConfig)
     */
    var excludePackages: List<String> = emptyList()
}
