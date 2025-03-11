package com.kmong.codeartifact.model

import net.pearx.kasechange.toPascalCase
import java.net.URI

open class CodeArtifactEndpoint {
    var domain: String = ""
    var domainOwner: String = ""
    var repository: String = ""
    var region: String = ""
    var type: String = ""
    var url: URI = URI.create("")
    val name: String
        get() = "${domain}-${repository}".toPascalCase()

    companion object {
        fun fromUrl(url: String): CodeArtifactEndpoint? {
            return fromUrl(URI(url))
        }

        fun fromUrl(url: URI): CodeArtifactEndpoint? {
            val urlString = url.toString()
            val match = regex.matchEntire(urlString) ?: return null
            return CodeArtifactEndpoint().apply {
                domain = match.groups["domain"]!!.value
                domainOwner = match.groups["domainOwner"]!!.value
                region = match.groups["region"]!!.value
                type = match.groups["type"]!!.value
                repository = match.groups["repository"]!!.value
                this.url = url
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
            """^https://(?<domain>.*?)-(?<domainOwner>[0-9].*?).d.codeartifact.(?<region>.+?).amazonaws.com/(?<type>.+?)/(?<repository>.+?)(?:/|\?.*|/\?.*)?$"""
                .toRegex()
    }
}