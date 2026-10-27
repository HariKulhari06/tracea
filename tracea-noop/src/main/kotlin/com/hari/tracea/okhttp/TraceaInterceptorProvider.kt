package com.hari.tracea.okhttp

import okhttp3.Interceptor

/**
 * No-op implementation of [TraceaInterceptorProvider] for release builds.
 * The Gradle plugin may still inject calls to [getInterceptor], but in release
 * builds (where tracea-noop replaces tracea), this returns a no-op interceptor.
 */
object TraceaInterceptorProvider {

    private val NO_OP = Interceptor { chain -> chain.proceed(chain.request()) }

    @JvmStatic
    fun install(interceptor: Interceptor) {
        // No-op
    }

    @JvmStatic
    fun getInterceptor(): Interceptor = NO_OP

    @JvmStatic
    fun reset() {
        // No-op
    }
}
