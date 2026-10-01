package com.hari.tracea.core.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainFilterConfigTest {

    @Test
    fun testDomainFilterConfigAllowedDomains() {
        val config = DomainFilterConfig(
            allowedDomains = listOf("api.test.com", "*.myserver.com")
        )

        // Exact match
        assertTrue(config.shouldCaptureHost("api.test.com"))
        // Wildcard match
        assertTrue(config.shouldCaptureHost("auth.myserver.com"))
        assertTrue(config.shouldCaptureHost("myserver.com"))
        // Disallowed
        assertFalse(config.shouldCaptureHost("other.test.com"))
        assertFalse(config.shouldCaptureHost("google.com"))
    }

    @Test
    fun testDomainFilterConfigIgnoredDomains() {
        val config = DomainFilterConfig(
            ignoredDomains = listOf("*.firebaseio.com", "crashlytics.google.com")
        )

        // Ignored
        assertFalse(config.shouldCaptureHost("test-db.firebaseio.com"))
        assertFalse(config.shouldCaptureHost("firebaseio.com"))
        assertFalse(config.shouldCaptureHost("crashlytics.google.com"))
        // Allowed
        assertTrue(config.shouldCaptureHost("api.test.com"))
        assertTrue(config.shouldCaptureHost("google.com"))
    }

    @Test
    fun testDomainFilterEmptyAllowedCapturesAllNonIgnored() {
        val config = DomainFilterConfig(
            allowedDomains = emptyList(),
            ignoredDomains = listOf("*.telemetry.com")
        )

        assertTrue(config.shouldCaptureHost("api.example.com"))
        assertTrue(config.shouldCaptureHost("anything.org"))
        assertFalse(config.shouldCaptureHost("stats.telemetry.com"))
    }

    @Test
    fun testDomainFilterMatchingRules() {
        val config = DomainFilterConfig()

        // Exact or subdomain
        assertTrue(config.matches("api.test.com", "api.test.com"))
        assertTrue(config.matches("sub.api.test.com", "api.test.com"))
        assertFalse(config.matches("other.test.com", "api.test.com"))

        // Wildcard prefix
        assertTrue(config.matches("api.test.com", "*.test.com"))
        assertTrue(config.matches("test.com", "*.test.com"))
        assertFalse(config.matches("other.com", "*.test.com"))

        // Leading dot
        assertTrue(config.matches("api.test.com", ".test.com"))
        assertTrue(config.matches("test.com", ".test.com"))

        // Root shorthand
        assertTrue(config.matches("api.test.com", "test.com"))
        assertTrue(config.matches("test.com", "test.com"))
    }
}
