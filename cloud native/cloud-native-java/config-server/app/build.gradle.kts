plugins {
    application
    alias(libs.plugins.springboot)
    alias(libs.plugins.springdependency)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.guava)
    implementation(libs.web)
    implementation(libs.cloud.config)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.example.CloudConfigApplication"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
