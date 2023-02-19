rootProject.name = "mediaprocessor-service"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagement: String by settings
    val detektVersion: String by settings
    val ktlintVersion: String by settings
    val versionsVersion: String by settings
    val jibVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagement
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("com.github.ben-manes.versions") version versionsVersion
        id("com.google.cloud.tools.jib") version jibVersion
        id("jacoco")

        kotlin("plugin.spring") version kotlinVersion
        kotlin("jvm") version kotlinVersion
    }
    repositories {
        gradlePluginPortal()
    }
}
