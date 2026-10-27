package com.hari.tracea.plugin.instrumentation

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * ASM MethodVisitor that intercepts calls to `OkHttpClient.Builder.build()`
 * and injects Tracea's interceptor BEFORE the build() call.
 *
 * ## What this does at the bytecode level:
 *
 * **Before transformation:**
 * ```
 * INVOKEVIRTUAL okhttp3/OkHttpClient$Builder.build ()Lokhttp3/OkHttpClient;
 * ```
 *
 * **After transformation:**
 * ```
 * INVOKESTATIC  com/hari/tracea/okhttp/TraceaInterceptorProvider.getInterceptor ()Lokhttp3/Interceptor;
 * INVOKEVIRTUAL okhttp3/OkHttpClient$Builder.addInterceptor (Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient$Builder;
 * INVOKEVIRTUAL okhttp3/OkHttpClient$Builder.build ()Lokhttp3/OkHttpClient;
 * ```
 *
 * ## Stack analysis:
 * ```
 * Before:  [..., builderRef]
 * Step 1:  [..., builderRef, interceptor]     // getInterceptor() pushes interceptor
 * Step 2:  [..., builderRef']                 // addInterceptor() consumes both, returns builder
 * Step 3:  [..., client]                      // build() consumes builder, returns OkHttpClient
 * ```
 *
 * @see com.hari.tracea.okhttp.TraceaInterceptorProvider (runtime counterpart)
 */
class OkHttpBuildMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor
) : MethodVisitor(api, methodVisitor) {

    companion object {
        // Target 1: OkHttpClient.Builder.build()
        private const val BUILDER_OWNER = "okhttp3/OkHttpClient\$Builder"
        private const val BUILD_METHOD = "build"
        private const val BUILD_DESCRIPTOR = "()Lokhttp3/OkHttpClient;"

        // Injection 1: TraceaInterceptorProvider.getInterceptor()
        private const val PROVIDER_OWNER = "com/hari/tracea/okhttp/TraceaInterceptorProvider"
        private const val PROVIDER_METHOD = "getInterceptor"
        private const val PROVIDER_DESCRIPTOR = "()Lokhttp3/Interceptor;"

        // Injection 1: Builder.addInterceptor(Interceptor)
        private const val ADD_INTERCEPTOR_METHOD = "addInterceptor"
        private const val ADD_INTERCEPTOR_DESCRIPTOR = "(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient\$Builder;"

        // Target 2: java/net/URL.openConnection()
        private const val URL_OWNER = "java/net/URL"
        private const val OPEN_CONNECTION_METHOD = "openConnection"
        private const val OPEN_CONNECTION_NO_ARG = "()Ljava/net/URLConnection;"
        private const val OPEN_CONNECTION_PROXY = "(Ljava/net/Proxy;)Ljava/net/URLConnection;"

        // Injection 2: TraceaUrlConnectionTracker.wrap(URLConnection)
        private const val URL_TRACKER_OWNER = "com/hari/tracea/urlconnection/TraceaUrlConnectionTracker"
        private const val WRAP_METHOD = "wrap"
        private const val WRAP_DESCRIPTOR = "(Ljava/net/URLConnection;)Ljava/net/URLConnection;"
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        // Target 1: OkHttpClient.Builder.build()
        if (opcode == Opcodes.INVOKEVIRTUAL &&
            owner == BUILDER_OWNER &&
            name == BUILD_METHOD &&
            descriptor == BUILD_DESCRIPTOR
        ) {
            // ---- INJECT: builder.addInterceptor(TraceaInterceptorProvider.getInterceptor()) ----
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                PROVIDER_OWNER,
                PROVIDER_METHOD,
                PROVIDER_DESCRIPTOR,
                false
            )
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                BUILDER_OWNER,
                ADD_INTERCEPTOR_METHOD,
                ADD_INTERCEPTOR_DESCRIPTOR,
                false
            )
        }

        // Target 2: URL.openConnection() or URL.openConnection(Proxy)
        if (opcode == Opcodes.INVOKEVIRTUAL &&
            owner == URL_OWNER &&
            name == OPEN_CONNECTION_METHOD &&
            (descriptor == OPEN_CONNECTION_NO_ARG || descriptor == OPEN_CONNECTION_PROXY)
        ) {
            // 1. Let the original URL.openConnection execute
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

            // 2. Wrap returned URLConnection via TraceaUrlConnectionTracker.wrap(connection)
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                URL_TRACKER_OWNER,
                WRAP_METHOD,
                WRAP_DESCRIPTOR,
                false
            )
            return
        }

        // Forward other instructions normally
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}
