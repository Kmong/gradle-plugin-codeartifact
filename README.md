## Why codeartifact plugin?

gradle dependency management, simple aws codeartifact config plugin

aws codeartifact private repository setting, very tired and complex.

## Requirements

| plugin Version | Requirement | Version |
|----------------|-------------|---------|
| 0.0.4          | kotlin      | 1.9.24  |
|                | gradle      | 8.10    |
|                | awssdk      | 2.30.33 |
|                | jdk         | 17      |

## Process Change, Before and After
### get private repository:before

`build.gradle:`

```groovy
repositories {
    maven {
        url "https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo"
        credentials {
            username = "aws"
            password = getAwsCodeArtifactToken(... parameters)
            // <- aws codeartifact token, token generate process required
        }
    }
}

fun getAwsCodeArtifactToken(profileName: String, region: String, domain: String, repositoryName: String): String {
    val process = ProcessBuilder("aws", "codeartifact", "get-authorization-token", "--profile", profileName, "--region", region, "--domain", domain, "--domain-owner", "123456789012", "--repository", repositoryName).start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val token = reader.readLine()
    process.waitFor()
    return token
}
```

### get private repository:after

`build.gradle:`

```groovy
import com.kmong.codeartifact.codeartifact

plugins {
    id 'com.kmong.codeartifact' version <latest>
}

repositories {
    codeartifact(url = "https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<aws profile name, default=default>")
}
```

### publish private repository:before

`build.gradle.kts:`

```groovy
publishing {
    publications {
        create("mavenJava", MavenPublication::class) {
            artifactId = codeArtifactId
            version = moduleVersion.toString()
            groupId = group.toString()
            from(components["java"])
        }
    }
    repositories {
        maven {
            url "https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo"
            credentials {
                username = "aws"
                password = getAwsCodeArtifactToken(... parameters)
                // <- aws codeartifact token, token generate process required
            }
        }
    }
}

fun getAwsCodeArtifactToken(profileName: String, region: String, domain: String, repositoryName: String): String {
    val process = ProcessBuilder("aws", "codeartifact", "get-authorization-token", "--profile", profileName, "--region", region, "--domain", domain, "--domain-owner", "123456789012", "--repository", repositoryName).start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val token = reader.readLine()
    process.waitFor()
    return token
}
```

### publish private repository:after

`build.gradle.kts:`

```kotlin
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = codeArtifactId
            version = moduleVersion.toString()
            groupId = group.toString()
            from(components["java"])
        }
    }
    repositories {
        codeartifact(url = "https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<aws profile name, default=default>")
    }
}
```

## How to use

| Property     | Description      | Default                                                                                                                                                                                                                                                     |
|--------------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| profile name | aws profile name | aws profile name                                                                                                                                                                                                                                            |
| region       | aws region       | repository region                                                                                                                                                                                                                                           |
| domain       | domain name      | Domain                                                                                                                                                                                                                                                      |
| domainOwner  | account id       | AccountId                                                                                                                                                                                                                                                   |
| repository   | repository name  | repository name, If a repository url is present, this value is ignored. If you don't have a repository url and you have a repository name, assemble it as `https://${domain}-${accountId}.d.codeartifact.${region}.amazonaws.com/maven/${repositoryName}/.` |
| type         | repository type  | maven                                                                                                                                                                                                                                                       |

### build.gradle.kts

`build.gradle.kts:`

```kotlin
import com.kmong.codeartifact.codeartifact

plugins {
    id("com.kmong.aws:codeartifact") version <latest>
}

repositories {
    codeartifact(url = "https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<aws profile name, default=default>")
}
```

### Multiple repositories

`build.gradle.kts:`

```kotlin
import com.kmong.codeartifact.codeartifact

repositories {
    codeartifact(url = "https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<aws profile name, default=default>")
    codeartifact(url = "https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<aws profile name, default=default>")
}
```
