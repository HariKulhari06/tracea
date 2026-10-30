package com.hari.tracea.plugin.instrumentation

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * ASM ClassVisitor that delegates to [OkHttpBuildMethodVisitor] for
 * every method in the class being visited.
 *
 * The MethodVisitor scans for calls to `OkHttpClient.Builder.build()`
 * and injects the Tracea interceptor before each one.
 */
class OkHttpClassVisitor(
    api: Int,
    classVisitor: ClassVisitor
) : ClassVisitor(api, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return OkHttpBuildMethodVisitor(api, mv)
    }
}
