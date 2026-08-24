package com.hari.tracea.manual

/**
 * No-op implementation of [ManualNetworkCall] for release builds.
 */
class ManualNetworkCall internal constructor() {

    fun requestHeaders(headers: Map<String, String>): ManualNetworkCall = this

    fun requestBody(body: String, contentType: String? = null): ManualNetworkCall = this

    fun response(
        statusCode: Int,
        headers: Map<String, String>? = null,
        body: String? = null,
        contentType: String? = null
    ): ManualNetworkCall = this

    fun failure(throwable: Throwable): ManualNetworkCall = this

    fun cancel(): ManualNetworkCall = this
}
