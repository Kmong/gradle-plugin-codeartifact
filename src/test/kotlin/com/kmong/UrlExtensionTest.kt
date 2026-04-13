package com.kmong

import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class UrlExtensionTest {

    @Test
    fun `queryParameters parses single parameter`() {
        val uri = URI("https://example.com/path?key=value")
        val params = uri.queryParameters()

        assertEquals(mapOf("key" to "value"), params)
    }

    @Test
    fun `queryParameters parses multiple parameters`() {
        val uri = URI("https://example.com/path?key1=value1&key2=value2")
        val params = uri.queryParameters()

        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), params)
    }

    @Test
    fun `queryParameters returns empty map when no query string`() {
        val uri = URI("https://example.com/path")
        val params = uri.queryParameters()

        assertEquals(emptyMap(), params)
    }

    @Test
    fun `queryParameters handles value with equals sign`() {
        val uri = URI("https://example.com/path?key=val=ue")
        val params = uri.queryParameters()

        assertEquals(mapOf("key" to "val=ue"), params)
    }

    @Test
    fun `queryParameters handles key-only parameter without equals`() {
        val uri = URI("https://example.com/path?flag")
        val params = uri.queryParameters()

        assertEquals(mapOf("flag" to ""), params)
    }

    @Test
    fun `queryParameters handles mixed key-only and key-value parameters`() {
        val uri = URI("https://example.com/path?flag&key=value")
        val params = uri.queryParameters()

        assertEquals(mapOf("flag" to "", "key" to "value"), params)
    }

    @Test
    fun `queryParameters handles empty value`() {
        val uri = URI("https://example.com/path?key=")
        val params = uri.queryParameters()

        assertEquals(mapOf("key" to ""), params)
    }

    @Test
    fun `queryParameters parses profile from CodeArtifact URL`() {
        val uri = URI("https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo?profile=myprofile")
        val params = uri.queryParameters()

        assertEquals("myprofile", params["profile"])
    }

    @Test
    fun `resolveSystemVar returns system property when set`() {
        val key = "test.codeartifact.prop"
        System.setProperty(key, "test-value")
        try {
            assertEquals("test-value", resolveSystemVar(key))
        } finally {
            System.clearProperty(key)
        }
    }

    @Test
    fun `resolveSystemVar returns null when property and env not set`() {
        val result = resolveSystemVar("nonexistent.test.property.xyz")
        assertEquals(null, result)
    }

    @Test
    fun `resolveSystemVar returns null for blank property`() {
        val key = "test.codeartifact.blank"
        System.setProperty(key, "   ")
        try {
            // Blank property should be ignored, falls through to env var lookup
            // Env var also likely not set, so returns null
            val result = resolveSystemVar(key)
            assertEquals(null, result)
        } finally {
            System.clearProperty(key)
        }
    }

    @Test
    fun `queryParameters handles URI with only path and no query`() {
        val uri = URI("https://example.com/a/b/c")
        assertEquals(emptyMap(), uri.queryParameters())
    }

    @Test
    fun `queryParameters handles duplicate keys by last-wins`() {
        val uri = URI("https://example.com?key=first&key=second")
        val params = uri.queryParameters()
        // associate() uses last entry for duplicate keys
        assertEquals("second", params["key"])
    }

    @Test
    fun `queryParameters handles URL-encoded values`() {
        val uri = URI("https://example.com?key=hello%20world")
        val params = uri.queryParameters()
        // URI.query returns decoded query, so %20 becomes space
        assertEquals("hello world", params["key"])
    }
}
