# Gradle CodeArtifact Plugin

## 왜 CodeArtifact 플러그인인가?

AWS CodeArtifact 프라이빗 저장소 인증을 자동화하는 Gradle 플러그인입니다.

기존에는 CodeArtifact 인증 토큰을 CLI로 직접 생성하고, 저장소 URL과 자격증명을 수동으로 설정해야 했습니다.
이 플러그인은 **URL 하나만으로** 인증 토큰 발급, 캐싱, 저장소 설정을 모두 처리합니다.

## 요구 사항

| 플러그인 버전 | 항목      | 버전      |
|---------|---------|---------|
| 0.0.5   | Kotlin  | 2.3.20  |
|         | Gradle  | 9.4.1   |
|         | AWS SDK | 2.42.33 |
|         | JDK     | 25      |

## 적용 전후 비교

### 의존성 저장소 설정: 적용 전

`build.gradle.kts:`

```kotlin
repositories {
    maven {
        url = uri("https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo")
        credentials {
            username = "aws"
            password = getAwsCodeArtifactToken(/* ... */)
            // 토큰 생성 로직을 직접 구현해야 함
        }
    }
}

fun getAwsCodeArtifactToken(profileName: String, region: String, domain: String, repositoryName: String): String {
    val process = ProcessBuilder(
        "aws", "codeartifact", "get-authorization-token",
        "--profile", profileName, "--region", region,
        "--domain", domain, "--domain-owner", "123456789012",
        "--repository", repositoryName
    ).start()
    val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
    val token = reader.readLine()
    process.waitFor()
    return token
}
```

### 의존성 저장소 설정: 적용 후

`build.gradle.kts:`

```kotlin
import com.kmong.codeartifact.codeartifact

plugins {
    id("com.kmong.codeartifact") version "0.0.5"
}

repositories {
    codeartifact(url = "https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<프로필명>")
}
```

### 배포(publish) 저장소: 적용 전

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
        maven {
            url = uri("https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo")
            credentials {
                username = "aws"
                password = getAwsCodeArtifactToken(/* ... */)
            }
        }
    }
}
```

### 배포(publish) 저장소: 적용 후

`build.gradle.kts:`

```kotlin
import com.kmong.codeartifact.codeartifact

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
        codeartifact(url = "https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<프로필명>")
    }
}
```

## 사용 방법

### URL 형식

```
https://<domain>-<domainOwner>.d.codeartifact.<region>.amazonaws.com/<type>/<repository>?profile=<프로필명>
```

| 속성          | 설명                              | 비고                |
|-------------|---------------------------------|-------------------|
| domain      | CodeArtifact 도메인 이름             | URL 경로에서 자동 파싱    |
| domainOwner | AWS 계정 ID (12자리 숫자)             | URL 경로에서 자동 파싱    |
| region      | AWS 리전                          | URL 경로에서 자동 파싱    |
| type        | 저장소 타입 (`maven`, `npm`, `pypi`) | URL 경로에서 자동 파싱    |
| repository  | 저장소 이름                          | URL 경로에서 자동 파싱    |
| profile     | AWS 프로필 이름 (쿼리 파라미터)            | 생략 시 `default` 사용 |

### build.gradle.kts

```kotlin
import com.kmong.codeartifact.codeartifact

plugins {
    id("com.kmong.codeartifact") version "0.0.5"
}

repositories {
    codeartifact(url = "https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo?profile=default")
}
```

### settings.gradle.kts

Settings 플러그인으로 적용하면 모든 하위 프로젝트에 자동 전파됩니다.
각 프로젝트의 `build.gradle.kts`에서 플러그인을 별도로 적용할 필요가 없습니다.

```kotlin
// settings.gradle.kts
plugins {
    id("com.kmong.codeartifact") version "0.0.5"
}
```

```kotlin
// 각 프로젝트의 build.gradle.kts (플러그인 재적용 불필요)
import com.kmong.codeartifact.codeartifact

repositories {
    codeartifact(url = "https://my-domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/my-repo")
}
```

### 다중 저장소

```kotlin
import com.kmong.codeartifact.codeartifact

repositories {
    codeartifact(url = "https://domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/repo-a?profile=default")
    codeartifact(url = "https://domain-123456789012.d.codeartifact.ap-northeast-2.amazonaws.com/maven/repo-b?profile=staging")
}
```

## AWS 자격증명 해석 순서

플러그인은 다음 순서로 AWS 자격증명을 해석합니다:

1. **Web Identity Token** — EKS Pod Identity 등
2. **환경 변수** — `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
3. **컨테이너 자격증명** — ECS Task Role
4. **인스턴스 프로필** — EC2 IAM Role
5. **AWS 프로필** — URL의 `profile` 쿼리 파라미터로 지정 (기본값: `default`)
6. **기본 자격증명 프로바이더** — AWS SDK 기본 체인

## 토큰 캐싱

인증 토큰은 Gradle `BuildService`를 통해 빌드 세션 내에서 자동 캐싱됩니다.

- **토큰 유효 기간**: 12시간
- **캐시 갱신 주기**: 11시간 경과 시 자동 갱신
- **캐시 키**: `domainOwner:domain` 조합으로 AWS 계정별 분리
- 동일 빌드 내에서 같은 도메인의 여러 저장소가 있어도 토큰 요청은 한 번만 발생합니다.
