/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2026 Leon Linhart
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU Lesser General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    }

    includeBuild("./build-logic")
    includeBuild("./deploy-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "GW2AddonManager"

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        register("buildDeps") {
            from(files("./gradle/build.versions.toml"))
        }
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("./libs/addon-manifest-lib")
includeBuild("./libs/fileinfo-reader")
includeBuild("./libs/manager-manifest-lib")
