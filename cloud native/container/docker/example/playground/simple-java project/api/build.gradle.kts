plugins {
    application
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.springBoot)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)

    implementation(libs.springBoot.web)

    runtimeOnly(libs.h2)

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
