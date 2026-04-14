package com.kmong.codeartifact.model

import com.kmong.codeartifact.model.CodeArtifactEndpoint.Companion.toEndpoint
import com.kmong.codeartifact.model.CodeArtifactEndpoint.Companion.toEndpointOrNull
import java.net.URI
import kotlin.test.*

class CodeArtifactEndpointTest {

    @Test
    fun `fromUrl parses standard CodeArtifact URL`() {
        val url = "https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-domain", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
        assertEquals("us-east-1", endpoint.region)
        assertEquals("maven", endpoint.type)
        assertEquals("my-repo", endpoint.repository)
        assertEquals("default", endpoint.profile)
    }

    @Test
    fun `fromUrl parses URL with trailing slash`() {
        val url = "https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo/"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-domain", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
        assertEquals("my-repo", endpoint.repository)
    }

    @Test
    fun `fromUrl parses URL with profile query parameter`() {
        val url =
            "https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo?profile=myprofile"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-domain", endpoint.domain)
        assertEquals("my-repo", endpoint.repository)
        assertEquals("myprofile", endpoint.profile)
    }

    @Test
    fun `fromUrl parses URL with trailing slash and query parameter`() {
        val url =
            "https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo/?profile=myprofile"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-domain", endpoint.domain)
        assertEquals("my-repo", endpoint.repository)
        assertEquals("myprofile", endpoint.profile)
    }

    @Test
    fun `fromUrl parses URL with multiple query parameters`() {
        val url =
            "https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo?profile=prod&other=value"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("prod", endpoint.profile)
        assertEquals("ap-northeast-2", endpoint.region)
    }

    @Test
    fun `fromUrl handles domain with hyphens correctly`() {
        val url = "https://my-complex-domain-123456789012.d.codeartifact.us-west-2.amazonaws.com/maven/repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-complex-domain", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
    }

    @Test
    fun `fromUrl handles domain with digits after hyphens`() {
        val url = "https://my-3rd-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-3rd-domain", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
    }

    @Test
    fun `fromUrl handles single-word domain`() {
        val url = "https://domain-123456789012.d.codeartifact.eu-west-1.amazonaws.com/maven/repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("domain", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
        assertEquals("eu-west-1", endpoint.region)
    }

    @Test
    fun `fromUrl handles various AWS regions`() {
        val regions = listOf(
            "us-east-1", "us-west-2", "eu-west-1", "eu-central-1",
            "ap-northeast-1", "ap-northeast-2", "ap-southeast-1"
        )
        for (region in regions) {
            val url = "https://domain-123456789012.d.codeartifact.${region}.amazonaws.com/maven/repo"
            val endpoint = CodeArtifactEndpoint.fromUrl(url)
            assertNotNull(endpoint, "Failed for region: $region")
            assertEquals(region, endpoint.region)
        }
    }

    @Test
    fun `fromUrl handles non-maven repository types`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/npm/my-npm-repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("npm", endpoint.type)
        assertEquals("my-npm-repo", endpoint.repository)
    }

    @Test
    fun `fromUrl generates clean URL without query parameters`() {
        val url =
            "https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo?profile=myprofile"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals(
            URI("https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo/"),
            endpoint.url
        )
        assertNull(endpoint.url.query)
    }

    @Test
    fun `fromUrl generates URL with trailing slash`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals(
            "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo/",
            endpoint.url.toString()
        )
    }

    @Test
    fun `fromUrl returns null for non-CodeArtifact URL`() {
        assertNull(CodeArtifactEndpoint.fromUrl("https://repo.maven.apache.org/maven2"))
        assertNull(CodeArtifactEndpoint.fromUrl("https://plugins.gradle.org/m2/"))
        assertNull(CodeArtifactEndpoint.fromUrl("https://example.com"))
    }

    @Test
    fun `fromUrl returns null for malformed URL`() {
        assertNull(CodeArtifactEndpoint.fromUrl("not a url"))
        assertNull(CodeArtifactEndpoint.fromUrl(""))
        assertNull(CodeArtifactEndpoint.fromUrl("https://"))
    }

    @Test
    fun `fromUrl returns null for HTTP (non-HTTPS) URL`() {
        val url = "http://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo"
        assertNull(CodeArtifactEndpoint.fromUrl(url))
    }

    @Test
    fun `fromUrl returns null for URL with incorrect domain owner format`() {
        // domainOwner must be exactly 12 digits
        assertNull(CodeArtifactEndpoint.fromUrl("https://domain-12345.d.codeartifact.us-east-1.amazonaws.com/maven/repo"))
        assertNull(CodeArtifactEndpoint.fromUrl("https://domain-1234567890123.d.codeartifact.us-east-1.amazonaws.com/maven/repo"))
        assertNull(CodeArtifactEndpoint.fromUrl("https://domain-12345678901a.d.codeartifact.us-east-1.amazonaws.com/maven/repo"))
    }

    @Test
    fun `fromUrl with URI delegates correctly`() {
        val uri = URI("https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo")
        val endpoint = CodeArtifactEndpoint.fromUrl(uri)

        assertNotNull(endpoint)
        assertEquals("domain", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
    }

    @Test
    fun `toEndpoint returns endpoint for valid URL`() {
        val endpoint = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo".toEndpoint()

        assertEquals("domain", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
    }

    @Test
    fun `toEndpoint throws for invalid URL`() {
        assertFailsWith<IllegalStateException> {
            "https://example.com".toEndpoint()
        }
    }

    @Test
    fun `URI toEndpoint returns endpoint for valid URI`() {
        val uri = URI("https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo")
        val endpoint = uri.toEndpoint()

        assertEquals("domain", endpoint.domain)
    }

    @Test
    fun `URI toEndpointOrNull returns null for non-CodeArtifact URI`() {
        val uri = URI("https://example.com")
        assertNull(uri.toEndpointOrNull())
    }

    @Test
    fun `URI toEndpointOrNull returns endpoint for valid URI`() {
        val uri = URI("https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo")
        assertNotNull(uri.toEndpointOrNull())
    }

    @Test
    fun `name property generates PascalCase from domain and repository`() {
        val endpoint = CodeArtifactEndpoint.fromUrl(
            "https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo"
        )

        assertNotNull(endpoint)
        assertEquals("MyDomainMyRepo", endpoint.name)
    }

    @Test
    fun `profile defaults to default when no query parameter`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("default", endpoint.profile)
    }

    @Test
    fun `regex does not match URLs with non-literal dots`() {
        // Ensure dots in the URL pattern must be literal dots, not arbitrary characters
        val url = "https://domain-123456789012XdXcodeartifactXus-east-1XamazonawsXcom/maven/repo"
        assertNull(CodeArtifactEndpoint.fromUrl(url))
    }

    @Test
    fun `fromUrl handles repository name with hyphens`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-complex-repo-name"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-complex-repo-name", endpoint.repository)
    }

    @Test
    fun `fromUrl returns null when missing repository path`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/"
        assertNull(CodeArtifactEndpoint.fromUrl(url))
    }

    @Test
    fun `fromUrl returns null when missing type and repository`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/"
        assertNull(CodeArtifactEndpoint.fromUrl(url))
    }

    @Test
    fun `fromUrl returns null for URL with spaces`() {
        // Verifies try-catch in fromUrl(String) handles URISyntaxException
        assertNull(CodeArtifactEndpoint.fromUrl("https://domain 123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo"))
    }

    @Test
    fun `fromUrl handles repository name with dots`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my.repo.name"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my.repo.name", endpoint.repository)
    }

    @Test
    fun `fromUrl handles repository name with underscores`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my_repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my_repo", endpoint.repository)
    }

    @Test
    fun `fromUrl handles pypi repository type`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/pypi/my-pypi-repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("pypi", endpoint.type)
        assertEquals("my-pypi-repo", endpoint.repository)
    }

    @Test
    fun `fromUrl handles GovCloud region`() {
        val url = "https://domain-123456789012.d.codeartifact.us-gov-west-1.amazonaws.com/maven/repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("us-gov-west-1", endpoint.region)
    }

    @Test
    fun `fromUrl handles domain containing only digits`() {
        val url = "https://test123-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("test123", endpoint.domain)
        assertEquals("123456789012", endpoint.domainOwner)
    }

    @Test
    fun `fromUrl differentiates endpoints with same domain but different domainOwner`() {
        val url1 = "https://shared-111111111111.d.codeartifact.us-east-1.amazonaws.com/maven/repo"
        val url2 = "https://shared-222222222222.d.codeartifact.us-east-1.amazonaws.com/maven/repo"

        val endpoint1 = CodeArtifactEndpoint.fromUrl(url1)
        val endpoint2 = CodeArtifactEndpoint.fromUrl(url2)

        assertNotNull(endpoint1)
        assertNotNull(endpoint2)
        assertEquals("shared", endpoint1.domain)
        assertEquals("shared", endpoint2.domain)
        assertEquals("111111111111", endpoint1.domainOwner)
        assertEquals("222222222222", endpoint2.domainOwner)
        assertNotEquals(endpoint1.url, endpoint2.url)
    }

    @Test
    fun `fromUrl strips fragment from URL`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo#fragment"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        // Fragment should be ignored; repository name should not include it
        assertNotNull(endpoint)
        assertEquals("repo", endpoint.repository)
        assertNull(endpoint.url.fragment)
    }

    @Test
    fun `name property for single-word domain and repository`() {
        val endpoint = CodeArtifactEndpoint.fromUrl(
            "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo"
        )

        assertNotNull(endpoint)
        assertEquals("DomainRepo", endpoint.name)
    }

    @Test
    fun `fromUrl preserves all parsed fields consistently`() {
        val url =
            "https://my-domain-999888777666.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo?profile=staging"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("my-domain", endpoint.domain)
        assertEquals("999888777666", endpoint.domainOwner)
        assertEquals("ap-northeast-2", endpoint.region)
        assertEquals("maven", endpoint.type)
        assertEquals("my-repo", endpoint.repository)
        assertEquals("staging", endpoint.profile)
        assertEquals(
            URI("https://my-domain-999888777666.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo/"),
            endpoint.url
        )
        assertEquals("MyDomainMyRepo", endpoint.name)
    }

    @Test
    fun `fromUrl treats empty profile value as default`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo?profile="
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("default", endpoint.profile)
    }

    @Test
    fun `fromUrl treats blank profile value as default`() {
        val url = "https://domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/repo?profile=%20%20"
        val endpoint = CodeArtifactEndpoint.fromUrl(url)

        assertNotNull(endpoint)
        assertEquals("default", endpoint.profile)
    }
}
