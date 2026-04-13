package com.kmong.codeartifact

import kotlin.test.*

class CachedCodeArtifactTokenServiceTest {

    @Test
    fun `TokenModel isTokenExpired returns false for fresh token`() {
        val token = CachedCodeArtifactTokenService.TokenModel(
            token = "test-token",
            lastUpdated = System.currentTimeMillis()
        )

        assertFalse(token.isTokenExpired())
    }

    @Test
    fun `TokenModel isTokenExpired returns false within 11 hours`() {
        val tenHoursAgo = System.currentTimeMillis() - (10 * 60 * 60 * 1000)
        val token = CachedCodeArtifactTokenService.TokenModel(
            token = "test-token",
            lastUpdated = tenHoursAgo
        )

        assertFalse(token.isTokenExpired())
    }

    @Test
    fun `TokenModel isTokenExpired returns true after 11 hours`() {
        val twelveHoursAgo = System.currentTimeMillis() - (12 * 60 * 60 * 1000)
        val token = CachedCodeArtifactTokenService.TokenModel(
            token = "test-token",
            lastUpdated = twelveHoursAgo
        )

        assertTrue(token.isTokenExpired())
    }

    @Test
    fun `TokenModel isTokenExpired returns true at exactly 11 hours boundary`() {
        val elevenHoursOneSecondAgo = System.currentTimeMillis() - (11 * 60 * 60 * 1000 + 1000)
        val token = CachedCodeArtifactTokenService.TokenModel(
            token = "test-token",
            lastUpdated = elevenHoursOneSecondAgo
        )

        assertTrue(token.isTokenExpired())
    }

    @Test
    fun `TokenModel isTokenExpired returns false just before 11 hours`() {
        val justUnderElevenHours = System.currentTimeMillis() - (11 * 60 * 60 * 1000 - 60000)
        val token = CachedCodeArtifactTokenService.TokenModel(
            token = "test-token",
            lastUpdated = justUnderElevenHours
        )

        assertFalse(token.isTokenExpired())
    }

    @Test
    fun `TokenModel equality works correctly as data class`() {
        val now = System.currentTimeMillis()
        val token1 = CachedCodeArtifactTokenService.TokenModel(token = "abc", lastUpdated = now)
        val token2 = CachedCodeArtifactTokenService.TokenModel(token = "abc", lastUpdated = now)
        val token3 = CachedCodeArtifactTokenService.TokenModel(token = "xyz", lastUpdated = now)

        assertEquals(token1, token2)
        assertNotEquals(token1, token3)
    }

    @Test
    fun `TokenModel copy preserves token value`() {
        val original = CachedCodeArtifactTokenService.TokenModel(
            token = "original-token",
            lastUpdated = System.currentTimeMillis()
        )
        val copy = original.copy(lastUpdated = 0L)

        assertEquals("original-token", copy.token)
        assertTrue(copy.isTokenExpired())
    }
}
