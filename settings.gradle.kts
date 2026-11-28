pluginManagement {
    repositories {
        google()
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

rootProject.name = "FireEscape"

include(":core-model")
include(":launcher")

// M0.2 spike: plugin transport across APKs. See docs/adr/0004-plugin-form-and-transport.md.
include(":plugin-api")
include(":spike-plugin")

// M0.8: contract checks and a mock host that runs plugins without the launcher.
include(":conformance-kit")
