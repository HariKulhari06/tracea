package com.hari.tracea.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.hari.tracea.plugin.instrumentation.OkHttpClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Tracea Gradle Plugin that automatically instruments OkHttp clients
 * at build time using ASM bytecode transformation.
 *
 * When applied, this plugin scans all compiled classes (app code + dependencies)
 * and injects TraceaInterceptor into every OkHttpClient.Builder.build() call.
 *
 * Usage in build.gradle.kts:
 * ```
 * plugins {
 *     id("com.hari.tracea")
 * }
 * ```
 */
class TraceaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Create the extension for user configuration
        val extension = project.extensions.create(
            "tracea",
            TraceaPluginExtension::class.java
        )

        val androidComponents = project.extensions.findByType(
            AndroidComponentsExtension::class.java
        ) ?: run {
            project.logger.error(
                "Tracea plugin requires an Android project. " +
                "Apply 'com.android.application' or 'com.android.library' before 'com.hari.tracea'."
            )
            return
        }

        androidComponents.onVariants { variant ->
            if (!extension.enabled) {
                project.logger.info("Tracea auto-instrumentation is disabled for variant: ${variant.name}")
                return@onVariants
            }

            val variantName = variant.name.lowercase()

            // Check explicit exclusions first
            val isExcluded = extension.excludeVariants.any {
                variantName.contains(it.lowercase())
            }
            if (isExcluded) {
                project.logger.info("Tracea skipping excluded variant: ${variant.name}")
                return@onVariants
            }

            // Determine if variant should be instrumented
            val shouldInstrument = if (extension.includeVariants.isNotEmpty()) {
                extension.includeVariants.any { variantName.contains(it.lowercase()) }
            } else {
                extension.instrumentAllVariants || variantName.contains("debug")
            }

            if (!shouldInstrument) {
                project.logger.info("Tracea skipping instrumentation for variant: ${variant.name}")
                return@onVariants
            }

            project.logger.lifecycle("Tracea: Enabling auto network capture for variant '${variant.name}'")

            val scope = if (extension.instrumentDependencies) {
                InstrumentationScope.ALL
            } else {
                InstrumentationScope.PROJECT
            }

            // Register OkHttp / URLConnection bytecode transformation
            variant.instrumentation.transformClassesWith(
                OkHttpClassVisitorFactory::class.java,
                scope
            ) { params ->
                params.excludePackages.set(extension.excludePackages)
            }

            variant.instrumentation.setAsmFramesComputationMode(
                com.android.build.api.instrumentation.FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )
        }
    }
}
