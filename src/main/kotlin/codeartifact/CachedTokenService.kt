package com.kmong.codeartifact

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest
import java.util.concurrent.ConcurrentHashMap

abstract class CachedTokenService : BuildService<BuildServiceParameters.None>, AutoCloseable {
    private var cachedTokenModels: ConcurrentHashMap<String, TokenModel> = ConcurrentHashMap()

    fun getToken(repository: Repository, credentials: AwsCredentials): String {
        val profileName = repository.profileName ?: "default"
        val cachedTokenModel = cachedTokenModels[profileName]

        if (cachedTokenModel == null || cachedTokenModel.isTokenExpired()) {
            val tokenModel = TokenModel(
                token = getAuthToken(repository, credentials),
                lastUpdated = System.currentTimeMillis()
            )
            cachedTokenModels[profileName] = tokenModel
            return tokenModel.token
        }
        return cachedTokenModel.token
    }

    private fun getAuthToken(
        repository: Repository,
        credentials: AwsCredentials
    ): String {
        val client = CodeartifactClient.builder()
            .region(Region.of(repository.region))
            .credentialsProvider { credentials }
            .build()

        val request = GetAuthorizationTokenRequest.builder()
            .domain(repository.domain)
            .domainOwner(repository.accountId)
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
