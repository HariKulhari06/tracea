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

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className

        // Built-in exclusions: SDK internals, OkHttp, framework
        // (tracea.demo is intentionally NOT in this list so it gets instrumented!)
        if (className == "com.hari.tracea.Tracea" || className.startsWith("com.hari.tracea.Tracea$")) {
            return false
        }

        val builtInExclusions = listOf(
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
            "com.google.firebase.",      // Firebase
        )

        for (prefix in builtInExclusions) {
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
