pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral(); maven(url = "https://jitpack.io") }
}
rootProject.name = "Dhunora"
include(":app")
include(":core-common")
include(":core-domain")
include(":core-ktorext")
include(":core-scraper")
include(":core-lyrics")
include(":core-data")
include(":player-media3")
include(":feature-ui")
