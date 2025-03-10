## Why codeartifact plugin?
gradle dependency management, simple aws codeartifact config plugin

aws codeartifact private repository setting, very tired and complex.

### before

```groovy
repositories {
    maven {
        url "https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo"
        credentials {
            username = "aws"
            password = getAwsCodeArtifactToken(... parameters) // <- aws codeartifact token, token generate process required
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

### after

```groovy
plugins {
    id 'com.kmong.aws:codeartifact' version pluginVersion
}

codeartifact {
    enabled = true
    globalAccountId = "123456789012"
    globalDomain = "my-domain"
    repository {
        profileName = "default"
        region = "ap-northeast-1"
        domain = "my-domain" // optional, default: globalDomain
        accountId = "123456789012" // optional, default: globalAccountId
        repositoryName = "my-repo"
    }
}
```

## How to use

| Property                  | Description              | Default                           |
|---------------------------|--------------------------|-----------------------------------|
| enabled                   | plugin enabled condition | false                             |
| globalAccountId           | global account id        | repository accountId              |
| globalDomain              | global domain name       | repository domain name(ex: kmong) |
| repository                | repository config        | -                                 |
| repository.profileName    | aws profile name         | aws profile name                  |
| repository.region         | aws region               | repository region                 |
| repository.domain         | domain name              | globalDomain                      |
| repository.accountId      | account id               | globalAccountId                   |
| repository.repositoryName | repository name          | repository name                   |

### build.gradle.kts

```kotlin
plugins {
    id("com.kmong.aws:codeartifact") version pluginVersion
}

codeartifact {
    enabled = true
    globalAccountId = "123456789012"
    globalDomain = "my-domain"
    repository {
        profileName = "default"
        region = "ap-northeast-1"
        domain = "my-domain" // optional, default: globalDomain
        accountId = "123456789012" // optional, default: globalAccountId
        repositoryName = "my-repo"
    }
}
```

### build.gradle

```groovy
plugins {
    id 'com.kmong.aws:codeartifact' version pluginVersion
}

codeartifact {
    enabled = true
    globalAccountId = "123456789012"
    globalDomain = "my-domain"
    repository {
        profileName = "default"
        region = "ap-northeast-1"
        domain = "my-domain" // optional, default: globalDomain
        accountId = "123456789012" // optional, default: globalAccountId
        repositoryName = "my-repo"
    }
}
```

### Multiple repositories

```kotlin
codeartifact {
    enabled = true
    globalAccountId = "123456789012"
    globalDomain = "my-domain"
    repository {
        profileName = "default"
        region = "ap-northeast-1"
        domain = "my-domain" // optional, default: globalDomain
        accountId = "123456789012" // optional, default: globalAccountId
        repositoryName = "my-repo-1"
    }
    repository {
        profileName = "default"
        region = "ap-northeast-1"
        domain = "my-domain" // optional, default: globalDomain
        accountId = "123456789012" // optional, default: globalAccountId
        repositoryName = "my-repo-2"
    }
}
```

## Requirements

| plugin Version | Requirement | Version |
|----------------|-------------|---------|
| 0.0.1          | kotlin      | 1.9.24  |
|                | gradle      | 8.10    |
|                | awssdk      | 2.30.33 |
|                | jdk         | 17      |