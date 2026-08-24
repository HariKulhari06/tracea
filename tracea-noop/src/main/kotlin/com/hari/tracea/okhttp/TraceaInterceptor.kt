package com.hari.tracea.okhttp

import com.hari.tracea.core.config.TraceaConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * No-op implementation of [TraceaInterceptor] for release builds.
 */
class TraceaInterceptor(
    config: TraceaConfig = TraceaConfig()
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
