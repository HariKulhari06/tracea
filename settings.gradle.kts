pluginManagement {
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
include(":tracea-ui")
include(":tracea")
include(":tracea-noop")
include(":tracea-demo")
