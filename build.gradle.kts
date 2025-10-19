plugins {
    val kotlinVersion = "2.2.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version "2.2.20"

    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "user-auth-service"
val basePackage: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val hibernateUtilsVersion = "3.11.0"

val springBootVersion = "3.5.6"
val kotestVersion = "6.0.4"
val mockkVersion = "1.14.6"
val testContainersVersion = "1.21.3"
val fixtureVersion = "1.2.0"
val javaFakerVersion = "1.0.2"
val jwtVersion = "0.13.0"
val kotlinLoggingVersion = "7.0.13"
val springdocVersion = "2.8.6"
val yitterIdGeneratorVersion = "1.0.6"
val flywayVersion = "11.14.1"

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-amqp:$springBootVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Hibernate
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:$hibernateUtilsVersion")
    kapt("org.hibernate.orm:hibernate-jpamodelgen")

    // flyway
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // Snowflake ID Generator
    implementation("com.github.yitter:yitter-idgenerator:$yitterIdGeneratorVersion")

    runtimeOnly("org.postgresql:postgresql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")

    // Logging
    implementation("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")

    // SpringDoc OpenAPI (Swagger)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Spring
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-now:$kotestVersion")

    // Mockk
    testImplementation("io.mockk:mockk:$mockkVersion")

    // TestContainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers:$springBootVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.testcontainers:rabbitmq:$testContainersVersion")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("kotest.framework.config.fqn", "$basePackage.KotestConfiguration")
}

ktlint {
    version.set("1.7.1")

    filter {
        exclude("**/generated/**")
    }
}
