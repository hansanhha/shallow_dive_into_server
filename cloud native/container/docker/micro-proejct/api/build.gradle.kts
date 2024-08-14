plugins {
    application
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.springBoot)
}

group = "com.docker"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)

    implementation(libs.springBoot.web)
    implementation(libs.springBoot.dataJpa)

    runtimeOnly(libs.h2)
//    runtimeOnly(libs.mysql)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
