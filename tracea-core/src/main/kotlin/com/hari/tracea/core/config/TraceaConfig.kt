package com.hari.tracea.core.config

/**
 * Main configuration for Tracea.
 */
data class TraceaConfig(
    val enabled: Boolean = true,
    val bodyCaptureConfig: BodyCaptureConfig = BodyCaptureConfig(),
    val storageConfig: StorageConfig = StorageConfig(),
    val redactionConfig: RedactionConfig = RedactionConfig(),
    val showFloatingButton: Boolean = true,
    val enableWebDashboard: Boolean = true
)
