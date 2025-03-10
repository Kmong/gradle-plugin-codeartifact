package codeartifact

import codeartifact.model.CodeArtifactExtension
import codeartifact.model.Repository
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider

class CodeArtifactRepositoryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 확장 속성 생성
        val extension = project.extensions.create("codeartifact", CodeArtifactExtension::class.java)

        val serviceProvider =
            project.gradle.sharedServices.registerIfAbsent(
                "cachedTokenService",
                CachedCodeArtifactTokenService::class.java
            )

        // 플러그인 태스크 설정
        project.afterEvaluate {
            if (extension.enabled) configureRepositories(project, serviceProvider, extension)
        }
    }

    private fun configureRepositories(
        project: Project,
        serviceProvider: Provider<CachedCodeArtifactTokenService>,
        extension: CodeArtifactExtension
    ) {
        extension.repositories.forEach { repository ->
            val credentials = getAwsCredentials(repository.profileName)
            repository.setAccountId(extension, credentials)
            repository.setDomain(extension)
            // CodeArtifact 토큰 가져오기
            val tokenService = serviceProvider.get()
            val token = tokenService.getToken(repository, credentials)

            // 레포지토리 설정
            project.repositories.maven {
                url = project.uri(repository.generateRepositoryUrl())
                credentials {
                    username = "aws"
                    password = token
                }
                metadataSources {
                    artifact()
                    mavenPom()
                    gradleMetadata()
                }
            }
        }
    }

    private fun getAwsCredentials(profileName: String?): AwsCredentials {
        return if (profileName != null) {
            ProfileCredentialsProvider.create(profileName).resolveCredentials()
        } else {
            DefaultCredentialsProvider.create().resolveCredentials()
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun Repository.generateRepositoryUrl(): String {
        return if (url.isNotEmpty()) {
            url
        } else {
            this.run { "https://${domain}-${accountId}.d.codeartifact.${region}.amazonaws.com/maven/${repositoryName}/" }
        }
    }

    /**
     * accountId가 비어있으면, globalAccountId를 사용하거나, credentials 에서 accountId를 가져온다.
     */
    private fun Repository.setAccountId(extension: CodeArtifactExtension, credentials: AwsCredentials) {
        if (accountId.isEmpty()) {
            accountId = if (extension.globalAccountId.isEmpty()) {
                credentials.accountId().get()
            } else {
                extension.globalAccountId
            }
        }
    }

    /**
     * domain 이 비어있으면, globalDomain 을 사용한다.
     */
    private fun Repository.setDomain(extension: CodeArtifactExtension) {
        if (domain.isEmpty()) {
            if (extension.globalDomain.isNotEmpty()) {
                domain = extension.globalDomain
            } else {
                throw IllegalArgumentException("domain must be set, repository domain name: [$domain], global domain name: [${extension.globalDomain}")
            }
        }
    }
}
