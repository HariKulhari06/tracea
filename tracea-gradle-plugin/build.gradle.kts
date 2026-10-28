plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.1.20"
    `maven-publish`
}

group = "com.github.HariKulhari06"
version = "1.7.0-alpha05"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle-api:8.10.1")
    compileOnly("org.ow2.asm:asm:9.7.1")
}

gradlePlugin {
    plugins {
        create("tracea") {
            id = "com.hari.tracea"
            implementationClass = "com.hari.tracea.plugin.TraceaPlugin"
            displayName = "Tracea Auto Network Capture"
            description = "Automatically instruments OkHttp and HttpURLConnection to capture all network requests"
        }
    }
}
