# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 참고하는 프로젝트 컨텍스트입니다.

## 프로젝트 개요

AWS CodeArtifact 프라이빗 저장소 인증을 자동화하는 Gradle 플러그인.
URL 하나로 토큰 발급, 캐싱, Maven 저장소 설정을 처리합니다.

- **플러그인 ID**: `com.kmong.codeartifact`
- **그룹**: `com.kmong`
- **Gradle Plugin Portal**: [gradle-plugin-codeartifact](https://github.com/Kmong/gradle-plugin-codeartifact)

## 빌드 명령어

```bash
./gradlew build          # 전체 빌드
./gradlew test           # 테스트 실행 (JUnit 5)
./gradlew publishPlugins # Gradle Plugin Portal 배포 (CI에서만 실행)
```

## 기술 스택

| 항목         | 버전      |
|------------|---------|
| Kotlin     | 2.3.20  |
| JDK        | 25      |
| Gradle     | 9.4.1   |
| AWS SDK v2 | 2.42.33 |

- Kotlin API 버전은 `KOTLIN_2_0`으로 설정되어 있음 (하위 호환성 유지 목적)
- 버전 관리는 `gradle/libs.versions.toml`에서 일원화

## 프로젝트 구조

```
src/main/kotlin/com/kmong/
├── Credentials.kt                          # AWS 자격증명 체인 해석
├── UrlExtension.kt                         # URI 쿼리 파라미터 파싱 유틸
└── codeartifact/
    ├── CodeArtifactPlugin.kt               # 플러그인 메인 (Settings/Project/Gradle)
    ├── CachedCodeArtifactTokenService.kt   # BuildService 기반 토큰 캐싱
    └── model/
        └── CodeArtifactEndpoint.kt         # CodeArtifact URL 파싱 모델

src/test/kotlin/com/kmong/
├── UrlExtensionTest.kt                     # 쿼리 파라미터 파싱 테스트
└── codeartifact/
    ├── CachedCodeArtifactTokenServiceTest.kt  # 토큰 만료 로직 테스트
    ├── CodeArtifactPluginFunctionalTest.kt    # Gradle TestKit 기능 테스트
    └── model/
        └── CodeArtifactEndpointTest.kt     # URL 파싱/정규식 테스트
```

## 핵심 아키텍처

### 플러그인 적용 흐름

1. `CodeArtifactPlugin.apply()` — Settings, Project, Gradle 타입에 따라 분기
2. `repositories.all {}` 콜백으로 모든 저장소 감시
3. `shouldConfigureCodeArtifactRepository()` — CodeArtifact URL인지 판별 (정규식)
4. CodeArtifact URL이면 → 토큰 발급 → 자격증명 설정 → 클린 URL로 교체

### 토큰 캐싱

- `CachedCodeArtifactTokenService`는 Gradle `BuildService`로 등록
- `ConcurrentHashMap.compute()`로 원자적 캐시 갱신
- 캐시 키: `"${domainOwner}:${domain}"` (동일 도메인이라도 계정이 다르면 분리)
- 11시간 후 자동 만료 (토큰 유효 기간 12시간)

### URL 파싱 정규식

```
^https://(?<domain>.+)-(?<domainOwner>\d{12})\.d\.codeartifact\.(?<region>[a-z0-9-]+)\.amazonaws\.com/(?<type>[^/]+)/(?<repository>[^/?#]+)(?:[/?#].*)?$
```

- `domain`: greedy 매칭 (하이픈 뒤 숫자가 있는 도메인명도 올바르게 파싱)
- `domainOwner`: 정확히 12자리 숫자 (AWS 계정 ID)
- 쿼리 파라미터와 fragment는 저장소 URL에서 제거

### 주의: dependencyResolutionManagement 미지원

현재 플러그인은 `settings.pluginManagement.repositories`와 `project.repositories`에만
`all {}` 콜백을 등록합니다. `dependencyResolutionManagement.repositories`에 추가된 저장소는
자동 자격증명 설정이 되지 않으므로, `codeartifact()` 확장 함수는 각 프로젝트의
`build.gradle.kts`에서 사용해야 합니다.

## 코딩 규칙

- `UrlExtension.kt`의 함수들은 `internal` — 모듈 외부 노출 금지
- `CodeArtifactEndpoint`는 `open class` — 확장 가능하나 현재 서브클래스 없음
- Gradle 내부 API(`DefaultMavenArtifactRepository`) 사용 — Kotlin contracts로 스마트 캐스트
- `CodeartifactClient`는 반드시 `.use {}` 블록으로 사용 (리소스 누수 방지)

## CI/CD

- `.github/workflows/release.yml`에서 `release/*` 태그 push 시 Gradle Plugin Portal 배포
- Release Drafter로 자동 릴리스 노트 생성
- 현재 CI에서 테스트는 `--exclude-task test`로 스킵 중
