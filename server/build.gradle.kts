plugins {
    kotlin("jvm")
    application
}

group = "com.cashi"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(projects.sharedLogic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.logback.classic)
}

application {
    mainClass.set("com.cashi.cashichallengev1.server.ServerKt")
}
