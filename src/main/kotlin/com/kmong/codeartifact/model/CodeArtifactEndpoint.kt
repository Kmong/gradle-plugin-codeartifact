package com.kmong.codeartifact.model

import com.kmong.queryParameters
import net.pearx.kasechange.toPascalCase
import java.net.URI

open class CodeArtifactEndpoint {
    var domain: String = ""
    var domainOwner: String = ""
    var repository: String = ""
    var region: String = ""
    var type: String = ""
    var profile: String = "default"
    var url: URI = URI.create("")
    val name: String
        get() = "${domain}-${repository}".toPascalCase()

    companion object {
        fun fromUrl(url: String): CodeArtifactEndpoint? {
            return try {
                fromUrl(URI(url))
            } catch (_: Exception) {
                null
            }
        }

        fun fromUrl(url: URI): CodeArtifactEndpoint? {
            val urlString = url.toString()
            val match = regex.matchEntire(urlString) ?: return null
            val queryParams = url.queryParameters()
            return CodeArtifactEndpoint().apply {
                domain = match.groups["domain"]!!.value
                domainOwner = match.groups["domainOwner"]!!.value
                region = match.groups["region"]!!.value
                type = match.groups["type"]!!.value
                repository = match.groups["repository"]!!.value
                profile = queryParams["profile"]?.takeIf { it.isNotBlank() } ?: "default"
                this.url =
                    URI("https://${domain}-${domainOwner}.d.codeartifact.${region}.amazonaws.com/${type}/${repository}/")
            }
        }

        fun String.toEndpoint(): CodeArtifactEndpoint {
            return fromUrl(this) ?: error("Invalid CodeArtifact endpoint: $this")
        }

        fun URI.toEndpoint(): CodeArtifactEndpoint {
            return fromUrl(this) ?: error("Invalid CodeArtifact endpoint: $this")
        }

        fun URI.toEndpointOrNull(): CodeArtifactEndpoint? {
            return fromUrl(this)
        }

        private val regex =
            """^https://(?<domain>.+)-(?<domainOwner>\d{12})\.d\.codeartifact\.(?<region>[a-z0-9-]+)\.amazonaws\.com/(?<type>[^/]+)/(?<repository>[^/?#]+)(?:[/?#].*)?$"""
                .toRegex()
    }
}