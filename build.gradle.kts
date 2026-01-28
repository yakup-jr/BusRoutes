plugins {
    java
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

group = "ru.teamscore"
version = "0.0.1-SNAPSHOT"
description = "BusRoutes"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.data.jpa)
    implementation(libs.spring.webmvc)
    implementation(libs.mapstruct)
    implementation(libs.spring.validation)

    compileOnly(libs.lombok)

    runtimeOnly(libs.postgresql)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation(libs.spring.data.jpa.test)
    testImplementation(libs.spring.webmvc.test)
    testImplementation(libs.tc.junit.jupiter)
    testImplementation(libs.spring.tc)
    testImplementation(libs.tc.postgresql)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)

    testRuntimeOnly(libs.junit.platform)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
