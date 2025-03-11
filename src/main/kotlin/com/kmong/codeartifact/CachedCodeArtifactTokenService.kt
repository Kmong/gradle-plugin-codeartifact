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
        val cachedTokenModel = cachedTokenModels[endpoint.domain]

        if (cachedTokenModel == null || cachedTokenModel.isTokenExpired()) {
            val tokenModel = TokenModel(
                token = getAuthToken(endpoint, credentials),
                lastUpdated = System.currentTimeMillis()
            )
            cachedTokenModels[endpoint.domain] = tokenModel
            return tokenModel.token
        }
        return cachedTokenModel.token
    }

    private fun getAuthToken(
        endpoint: CodeArtifactEndpoint,
        credentials: AwsCredentials
    ): String {
        val client = CodeartifactClient.builder()
            .region(Region.of(endpoint.region))
            .credentialsProvider { credentials }
            .build()

        val request = GetAuthorizationTokenRequest.builder()
            .domain(endpoint.domain)
            .domainOwner(endpoint.domainOwner)
            .durationSeconds(43200) // 12시간
            .build()

        val response = client.getAuthorizationToken(request)
        return response.authorizationToken()
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
            return System.currentTimeMillis() - lastUpdated > 11 * 60 * 60 * 1000
        }
    }
}
