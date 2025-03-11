@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.nio.charset.StandardCharsets


val javaVersion: String by project
val javaToolChainVersion = JavaLanguageVersion.of(javaVersion)
val gradleJavaVersion = JavaVersion.toVersion(javaVersion)
val jvmTargetVersion = JvmTarget.fromTarget(javaVersion)
val awsSdkVersion: String by project
val releaseVersion: String by project

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
    signing // https://docs.gradle.org/current/userguide/signing_plugin.html
    id("org.ysb33r.gradletest")
}

group = "com.kmong"
version = releaseVersion

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
            tags = listOf("aws", "codeartifact", "codeartifact-authentication", "gradle", "plugin", "jdk17")
        }
    }
}

publishing {
    repositories {
        mavenLocal()
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
    implementation(platform("software.amazon.awssdk:bom:$awsSdkVersion"))
    implementation("software.amazon.awssdk:sts")
    implementation("software.amazon.awssdk:sso")
    implementation("software.amazon.awssdk:ssooidc")
    implementation("software.amazon.awssdk:codeartifact")
    implementation("net.pearx.kasechange:kasechange:1.4.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
}