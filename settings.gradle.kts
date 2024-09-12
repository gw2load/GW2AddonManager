pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "GW2AddonManager"

dependencyResolutionManagement {
    versionCatalogs {
        register("buildDeps") {
            from(files("./gradle/build.versions.toml"))
        }
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")