@file:Suppress("SpellCheckingInspection")

rootProject.name = "plugin-kmong-codeartifact"

pluginManagement {
    val kotlinVersion: String by settings
    val gradleTestVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.ysb33r.gradletest") version gradleTestVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

