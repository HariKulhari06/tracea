package com.hari.tracea.plugin.instrumentation

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 * AGP AsmClassVisitorFactory that creates OkHttp bytecode visitors.
 *
 * This factory is registered by [com.hari.tracea.plugin.TraceaPlugin] and
 * creates [OkHttpClassVisitor] instances for each class that passes
 * the [isInstrumentable] filter.
 */
abstract class OkHttpClassVisitorFactory :
    AsmClassVisitorFactory<OkHttpClassVisitorFactory.Params> {

    interface Params : InstrumentationParameters {
        @get:Input
        val excludePackages: ListProperty<String>
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return OkHttpClassVisitor(Opcodes.ASM9, nextClassVisitor)
    }

    companion object {
        private val BUILT_IN_EXCLUSION_PREFIXES = arrayOf(
            "com.hari.tracea.okhttp.",
            "com.hari.tracea.urlconnection.",
            "com.hari.tracea.core.",
            "com.hari.tracea.storage.",
            "com.hari.tracea.web.",
            "com.hari.tracea.ui.",
            "com.hari.tracea.manual.",
            "com.hari.tracea.noop.",
            "okhttp3.",                  // OkHttp internals
            "okio.",                     // Okio (OkHttp's IO library)
            "android.",                  // Android framework
            "androidx.",                 // AndroidX framework
            "kotlin.",                   // Kotlin stdlib
            "kotlinx.",                  // Kotlin extensions
            "com.google.android.",       // Google Play Services
            "com.google.firebase.",      // Firebase SDKs
            "com.google.common.",        // Guava utilities
            "com.google.protobuf.",      // Protobuf generated models
            "com.google.gson.",          // Gson JSON serializer
            "org.bouncycastle.",         // BouncyCastle crypto
            "org.jetbrains.",            // JetBrains annotations/tools
            "org.intellij.",             // IntelliJ annotations
            "org.apache.commons.",       // Apache Commons
            "io.reactivex.",             // RxJava
            "com.airbnb.lottie."         // Lottie UI animations
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className

        // Fast rejection 1: Tracea root singleton itself
        if (className == "com.hari.tracea.Tracea" || className.startsWith("com.hari.tracea.Tracea$")) {
            return false
        }

        // Fast rejection 2: Android generated resource & BuildConfig classes
        // (Saves inspecting thousands of R and BuildConfig classes across all dependencies)
        if (className.endsWith(".R") || className.contains(".R$") || className.endsWith(".BuildConfig")) {
            return false
        }

        // Fast rejection 3: Known non-network libraries & SDK internals
        for (prefix in BUILT_IN_EXCLUSION_PREFIXES) {
            if (className.startsWith(prefix)) return false
        }

        // User-configured exclusions
        val userExclusions = parameters.get().excludePackages.getOrElse(emptyList())
        for (prefix in userExclusions) {
            if (className.startsWith(prefix)) return false
        }

        return true
    }
}
