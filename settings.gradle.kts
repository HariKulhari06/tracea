pluginManagement {
    // Include the Tracea Gradle plugin from the composite build
    includeBuild("tracea-gradle-plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "tracea"

include(":tracea-core")
include(":tracea-okhttp")
include(":tracea-manual")
include(":tracea-storage")
include(":tracea-web")
include(":tracea-ui")
include(":tracea")
include(":tracea-noop")
include(":tracea-urlconnection")
include(":tracea-demo")
