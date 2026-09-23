plugins {
    id("com.android.application") version "9.4.0" apply false
    id("com.android.library") version "9.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10" apply false
    id("com.google.devtools.ksp") version "2.3.11" apply false
}

// protobuf-java (full) is a strict superset of protobuf-javalite (identical class
// FQNs); drop javalite everywhere to fix :app:checkReleaseDuplicateClasses.
subprojects {
    configurations.all {
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        // PipePipe and Brave both depend on com.github.TeamNewPipe:nanojson with different commit
        // hashes. Gradle's default resolver picks PipePipe's older 1d9e1aea... commit which lacks
        // JsonArray.streamAsJsonObjects(), causing NoSuchMethodError when Brave's fallback runs at
        // runtime. Force the latest upstream commit (newer than both libs ship) across every module
        // so the merged APK carries a nanojson with the API both extractors expect.
        resolutionStrategy {
            force("com.github.TeamNewPipe:nanojson:c7a6c1c08d16b6d5ecded34758e6415e07be2166")
        }
    }
}
