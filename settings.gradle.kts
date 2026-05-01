pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NightCatchers"

include(":app")

// Core modules
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":core:network")
include(":core:security")
include(":core:testing")

// Feature modules
include(":feature:ar")
include(":feature:filters")
include(":feature:capture")
include(":feature:pet")
include(":feature:vault")
include(":feature:parental")
include(":feature:onboarding")
