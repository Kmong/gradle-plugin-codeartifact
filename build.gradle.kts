@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.nio.charset.StandardCharsets


val javaVersion: String = libs.versions.java.get()
val javaToolChainVersion = JavaLanguageVersion.of(javaVersion)
val gradleJavaVersion = JavaVersion.toVersion(javaVersion)
val jvmTargetVersion = JvmTarget.fromTarget(javaVersion)

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.publish)
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}
group = "com.kmong"
version = libs.versions.release.get()

gradlePlugin {
    plugins {
        website = "https://github.com/Kmong/gradle-plugin-codeartifact"
        vcsUrl = "https://github.com/Kmong/gradle-plugin-codeartifact.git"
        create("codeartifact") {
            id = "$group.codeartifact"
            implementationClass = "$group.codeartifact.CodeArtifactPlugin"
            displayName = "Gradle CodeArtifact Config Plugin"
            description = """
                gradle dependency management, simple aws codeartifact config plugin.
                
                simple aws codeartifact private repository authentication.

                > aws codeartifact private repository setting, very tired and complex.
                
                |This plugin configures the AWS CodeArtifact repository for the project.
                |It retrieves the CodeArtifact token and sets the repository URL and credentials.
                |The plugin uses the AWS SDK for Java v2 to interact with the AWS CodeArtifact service.
                |The plugin requires the AWS credentials to be set up in the AWS credentials file.
                |The plugin also requires the AWS CodeArtifact repository to be set up in the AWS account.
                |The plugin is designed to work with the Gradle build system.
                |The plugin is designed to work with the Kotlin programming language.
            """.trimIndent()
            tags = listOf(
                "aws",
                "codeartifact",
                "codeartifact-authentication",
                "gradle-publish",
                "jdk17"
            )
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(javaToolChainVersion)
    }

    compilerOptions {
        jvmTarget.set(jvmTargetVersion)
        apiVersion.set(KotlinVersion.KOTLIN_1_9)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

java {
    sourceCompatibility = gradleJavaVersion
    targetCompatibility = gradleJavaVersion
    toolchain {
        languageVersion = javaToolChainVersion
    }

    withJavadocJar()
    withSourcesJar()
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(platform(libs.awssdk.bom))
    implementation(libs.awssdk.sts)
    implementation(libs.awssdk.sso)
    implementation(libs.awssdk.ssooidc)
    implementation(libs.awssdk.codeartifact)
    implementation(libs.kasechange)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
}