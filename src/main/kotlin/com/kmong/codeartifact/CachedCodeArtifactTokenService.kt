package com.kmong.codeartifact

import com.kmong.codeartifact.model.CodeArtifactEndpoint
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest
import java.util.concurrent.ConcurrentHashMap

abstract class CachedCodeArtifactTokenService : BuildService<BuildServiceParameters.None>, AutoCloseable {
    private var cachedTokenModels: ConcurrentHashMap<String, TokenModel> = ConcurrentHashMap()

    fun getToken(endpoint: CodeArtifactEndpoint, credentials: AwsCredentials): String {
        val cacheKey = "${endpoint.domainOwner}:${endpoint.domain}"
        return cachedTokenModels.compute(cacheKey) { _, existing ->
            if (existing == null || existing.isTokenExpired()) {
                TokenModel(
                    token = getAuthToken(endpoint, credentials),
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                existing
            }
        }!!.token
    }

    private fun getAuthToken(
        endpoint: CodeArtifactEndpoint,
        credentials: AwsCredentials
    ): String {
        return CodeartifactClient.builder()
            .region(Region.of(endpoint.region))
            .credentialsProvider { credentials }
            .build()
            .use { client ->
                val request = GetAuthorizationTokenRequest.builder()
                    .domain(endpoint.domain)
                    .domainOwner(endpoint.domainOwner)
                    .durationSeconds(43200) // 12시간
                    .build()
                client.getAuthorizationToken(request).authorizationToken()
            }
    }


    override fun close() {
        cachedTokenModels.clear()
    }

    data class TokenModel(
        val token: String,
        val lastUpdated: Long
    ) {
        fun isTokenExpired(): Boolean {
            // CodeArtifact 토큰은 12시간 유효하므로, 11시간 후에 만료된 것으로 간주
            return System.currentTimeMillis() - lastUpdated > 11L * 60 * 60 * 1000
        }
    }
}
