val springBootVersion: String by project
val springDocVersion: String by project
val detektVersion: String by project
val ktlintPluginVersion: String by project
val testcontainersVersion: String by project
val kotestVersion: String by project
val exposedVersion: String by project
val awsSdkVersion: String by project
val kotlinLoggingVersion: String by project
val sentryVersion: String by project
val jacocoVersion: String by project
val mockkVersion: String by project
val jaffreeVersion: String by project
val slf4jVersion: String by project
val stubRunnerVersion: String by project

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
    id("com.google.cloud.tools.jib")
    id("jacoco")

    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "com.hometech"
version = "0.0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-parent:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springdoc:springdoc-openapi-ui:$springDocVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$springDocVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("com.github.kokorin.jaffree:jaffree:$jaffreeVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("com.amazonaws:aws-java-sdk:$awsSdkVersion")

    implementation("io.sentry:sentry-spring-boot-starter:$sentryVersion")
    implementation("io.sentry:sentry-logback:$sentryVersion")

    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")

    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotestVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "junit", module = "junit")
    }
    testImplementation("com.ninja-squad:springmockk:$mockkVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:$stubRunnerVersion")
}

jacoco {
    toolVersion = jacocoVersion
}

tasks {
    detekt {
        toolVersion = detektVersion
        source = files("src/main/kotlin")
        config = files("detekt.yml")
    }

    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            html.required.set(true)
            sarif.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
        }
    }

    ktlint {
        version.set(ktlintPluginVersion)
        verbose.set(true)
        coloredOutput.set(true)
    }
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed", "standard_out", "standard_error")
            showStandardStreams = true
            setExceptionFormat("full")
        }
        reports {
            junitXml.required.set(true)
        }
        configure<JacocoTaskExtension> { excludes = listOf("com/gargoylesoftware/**") }
        setFinalizedBy(setOf(jacocoTestReport))
    }
    jacocoTestReport {
        classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                val excludes = listOf(
                    "com/hometech/**/configuration/**",
                    "com/hometech/**/*Helper.kt",
                    "com/hometech/**/Hooks.kt",
                    "com/hometech/mediaprocessor/*Application.kt"
                )
                this.exclude(excludes)
            }
        )
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        dependsOn(test)
    }
}
