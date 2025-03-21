package com.kmong.codeartifact

import com.kmong.codeartifact.model.CodeArtifactEndpoint
import com.kmong.codeartifact.model.CodeArtifactEndpoint.Companion.toEndpoint
import com.kmong.codeartifact.model.CodeArtifactEndpoint.Companion.toEndpointOrNull
import com.kmong.getAwsCredentials
import com.kmong.queryParameters
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.PluginAware
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

abstract class CodeArtifactPlugin @Inject constructor() : Plugin<PluginAware> {
    override fun apply(target: PluginAware) {
        val provider = when (target) {
            is Settings -> target.gradle.sharedServices.registerIfAbsent(
                "cachedTokenService",
                CachedCodeArtifactTokenService::class.java
            )

            is Project -> target.gradle.sharedServices.registerIfAbsent(
                "cachedTokenService",
                CachedCodeArtifactTokenService::class.java
            )

            is Gradle -> target.gradle.sharedServices.registerIfAbsent(
                "cachedTokenService",
                CachedCodeArtifactTokenService::class.java
            )

            else -> error("Unsupported Plugin implementation")
        }

        when (target) {
            is Project -> initProject(target, provider)

            is Settings -> initSettings(target, provider)

            is Gradle -> applyToGradle(target, provider)

            else -> error("Unsupported Plugin implementation")
        }
    }

    private fun applyToGradle(gradle: Gradle, provider: Provider<CachedCodeArtifactTokenService>) {
        gradle.beforeSettings {
            buildscript.repositories.all { configureCodeArtifactRepository(this, provider) }
            pluginManagement.repositories.all { configureCodeArtifactRepository(this, provider) }
            pluginManager.apply(CodeArtifactPlugin::class)
        }
    }

    private fun initSettings(settings: Settings, provider: Provider<CachedCodeArtifactTokenService>) {
        settings.pluginManagement.repositories.all {
            configureCodeArtifactRepository(this, provider)
        }

        settings.gradle.beforeProject {
            buildscript.repositories.all { configureCodeArtifactRepository(this, provider) }
            pluginManager.apply(CodeArtifactPlugin::class)
        }
    }

    private fun initProject(project: Project, provider: Provider<CachedCodeArtifactTokenService>) {
        project.repositories.all {
            configureCodeArtifactRepository(this, provider)
        }
        project.plugins.withType(MavenPublishPlugin::class.java) {
            project.configure<PublishingExtension> {
                repositories.all {
                    configureCodeArtifactRepository(this, provider)
                }
            }
        }
    }

    private fun configureCodeArtifactRepository(
        repository: ArtifactRepository,
        provider: Provider<CachedCodeArtifactTokenService>
    ) {
        if (!shouldConfigureCodeArtifactRepository(repository)) return
        val queryParameters = repository.url.queryParameters()
        val tokenService = provider.get()
        val endpoint = repository.url.toEndpoint()
        val profile = queryParameters["profile"] ?: "default"
        val credentials = getAwsCredentials(profile)

        val token = tokenService.getToken(endpoint, credentials)

        repository.credentials {
            username = "aws"
            password = token
        }
        repository.metadataSources {
            artifact()
            mavenPom()
            gradleMetadata()
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun shouldConfigureCodeArtifactRepository(repository: ArtifactRepository): Boolean {
        contract { returns(true) implies (repository is DefaultMavenArtifactRepository) }
        if (repository !is DefaultMavenArtifactRepository) return false

        val endpoint = repository.url.toEndpointOrNull()
        return when {
            endpoint == null -> false
            else -> true
        }
    }
}

fun RepositoryHandler.codeartifact(
    url: String,
    block: Action<MavenArtifactRepository> = Action {}
) {
    codeartifact(url.toEndpoint(), block)
}

fun RepositoryHandler.codeartifact(
    endpoint: CodeArtifactEndpoint,
    block: Action<MavenArtifactRepository> = Action {}
) {
    maven {
        this.name = endpoint.name
        this.url = endpoint.url
        block.execute(this)
    }
}
