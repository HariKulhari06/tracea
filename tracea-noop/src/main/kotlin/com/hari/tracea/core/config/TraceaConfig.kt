package com.hari.tracea.core.config

/**
 * No-op configuration for Tracea in release builds.
 */
data class TraceaConfig(
    val enabled: Boolean = false,
    val bodyCaptureConfig: BodyCaptureConfig = BodyCaptureConfig(),
    val storageConfig: StorageConfig = StorageConfig(),
    val redactionConfig: RedactionConfig = RedactionConfig(),
    val showFloatingButton: Boolean = false
)

data class BodyCaptureConfig(
    val enabled: Boolean = false,
    val maxRequestBodySize: Long = 0L,
    val maxResponseBodySize: Long = 0L,
    val captureBinary: Boolean = false
)

data class StorageConfig(
    val maxRequests: Int = 0,
    val maxBodySize: Long = 0L
)

data class RedactionConfig(
    val sensitiveHeaders: Set<String> = emptySet(),
    val sensitiveJsonFields: Set<String> = emptySet(),
    val replacementText: String = ""
)
