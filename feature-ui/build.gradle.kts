plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.alok.dhunora.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-domain"))
    implementation(project(":core-data"))
    implementation(project(":player-media3"))

    // Kotlin / Coroutines / Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")

    // Koin
    implementation(platform("io.insert-koin:koin-bom:4.2.2"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-android")
    implementation("io.insert-koin:koin-androidx-compose")

    // Coil 3
    implementation("io.coil-kt.coil3:coil:3.5.0")
    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")

    // Ktor
    implementation("io.ktor:ktor-client-core:3.5.2")
    implementation("io.ktor:ktor-client-okhttp:3.5.2")
    implementation("io.ktor:ktor-client-cio:3.5.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.2")

    // URI
    implementation("com.eygraber:uri-kmp:0.0.21")

    // Color / Palette
    implementation("com.kmpalette:kmpalette-core:3.1.0")
    implementation("com.kmpalette:extensions-network:3.1.0")
    implementation("com.materialkolor:material-kolor:5.0.0")

    // Liquid Glass (kyant backdrop)
    implementation("io.github.kyant0:backdrop:2.0.0")
    implementation("io.github.kyant0:shapes:1.2.0")

    // Blur
    implementation("dev.chrisbanes.haze:haze:1.7.2")
    implementation("dev.chrisbanes.haze:haze-materials:1.7.2")

    // Date/time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")

    // AboutLibraries
    implementation("com.mikepenz:aboutlibraries-core:15.0.2")
    implementation("com.mikepenz:aboutlibraries-compose-m3:15.0.2")

    // Markdown
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.44.0")

    // File picker (CALF)
    implementation("com.mohamedrejeb.calf:calf-ui:0.13.0")
    implementation("com.mohamedrejeb.calf:calf-file-picker:0.13.0")

    // Toast
    implementation("network.chaintech:cmptoast:1.0.71")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation("androidx.paging:paging-compose:3.5.1")
    implementation("androidx.media3:media3-session:1.11.1")
    implementation("androidx.media3:media3-ui:1.11.1")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.5.0-alpha26")
    implementation("androidx.compose.material3.adaptive:adaptive:1.3.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
}
