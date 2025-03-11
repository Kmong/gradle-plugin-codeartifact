@file:Suppress("SpellCheckingInspection")

rootProject.name = "plugin-kmong-codeartifact"

pluginManagement {
    val kotlinVersion: String by settings
    val gradleTestVersion: String by settings
    val pluginPublishVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.gradle.plugin-publish") version pluginPublishVersion
        id("org.ysb33r.gradletest") version gradleTestVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

