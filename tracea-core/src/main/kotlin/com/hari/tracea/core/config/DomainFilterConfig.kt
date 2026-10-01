package com.hari.tracea.core.config

/**
 * Configuration for domain filtering (allowed & ignored domains).
 */
data class DomainFilterConfig(
    /**
     * List of allowed domains. If non-empty, ONLY requests to these domains are captured.
     * Supports exact hostnames (e.g. "api.test.com") and wildcard subdomains (e.g. "*.test.com", ".test.com", "test.com").
     */
    val allowedDomains: List<String> = emptyList(),

    /**
     * List of ignored/blocked domains. Requests matching these domains are NEVER captured.
     * Supports exact hostnames (e.g. "crashlytics.google.com") and wildcard subdomains (e.g. "*.firebase.com").
     */
    val ignoredDomains: List<String> = emptyList()
) {
    companion object {
        @Volatile
        var activeConfig: DomainFilterConfig? = null
    }
    /**
     * Evaluates whether a given URL string should be captured based on this configuration.
     */
    fun shouldCaptureUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return true
        val host = try {
            java.net.URI(url).host
        } catch (_: Exception) {
            null
        }
        return shouldCaptureHost(host)
    }

    /**
     * Evaluates whether a given hostname should be captured based on this configuration.
     */
    fun shouldCaptureHost(host: String?): Boolean {
        val cleanHost = host?.lowercase()?.trim()
        if (cleanHost.isNullOrEmpty()) return true

        // 1. Check ignored/blocklist domains first
        for (ignored in ignoredDomains) {
            val pattern = ignored.lowercase().trim()
            if (matches(cleanHost, pattern)) {
                return false
            }
        }

        // 2. If allowedDomains is empty, capture everything that wasn't explicitly ignored
        val cleanedAllowed = allowedDomains
            .map { it.lowercase().trim() }
            .filter { it.isNotEmpty() }

        if (cleanedAllowed.isEmpty()) {
            return true
        }

        // 3. If allowedDomains has entries, only capture if host matches at least one pattern
        for (allowed in cleanedAllowed) {
            if (matches(cleanHost, pattern = allowed)) {
                return true
            }
        }

        return false
    }

    /**
     * Checks if a host matches a domain pattern.
     * Supported formats:
     * - "api.example.com" -> exact match or subdomain
     * - "*.example.com" -> matches "api.example.com", "sub.api.example.com", "example.com"
     * - ".example.com" -> matches "api.example.com", "example.com"
     * - "example.com" -> matches "example.com" and any "*.example.com"
     */
    fun matches(host: String, pattern: String): Boolean {
        if (pattern.isEmpty() || host.isEmpty()) return false

        // Exact match
        if (host == pattern) return true

        // Wildcard prefix: "*.example.com"
        if (pattern.startsWith("*.")) {
            val root = pattern.substring(2)
            if (host == root || host.endsWith(".$root")) {
                return true
            }
        }

        // Leading dot: ".example.com"
        if (pattern.startsWith(".")) {
            val root = pattern.substring(1)
            if (host == root || host.endsWith(".$root")) {
                return true
            }
        }

        // Root domain shorthand: "example.com" matches "example.com" and "*.example.com"
        if (host.endsWith(".$pattern")) {
            return true
        }

        return false
    }
}
