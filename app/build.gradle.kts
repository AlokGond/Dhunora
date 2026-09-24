plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.alok.dhunora"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.alok.dhunora"
        minSdk = 26
        targetSdk = 36
        versionCode = 19
        versionName = "0.19.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures { compose = true }

    signingConfigs {
        val ks = System.getenv("DHUNORA_KEYSTORE")
        if (!ks.isNullOrBlank()) {
            create("release") {
                storeFile = file(ks)
                storePassword = System.getenv("DHUNORA_STORE_PASSWORD")
                keyAlias = System.getenv("DHUNORA_KEY_ALIAS")
                keyPassword = System.getenv("DHUNORA_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (signingConfigs.names.contains("release")) signingConfigs.getByName("release") else null
        }
    }
    packaging {
        resources.excludes += setOf("META-INF/DEPENDENCIES","META-INF/NOTICE","META-INF/LICENSE","META-INF/LICENSE.txt","META-INF/NOTICE.txt")
    }
}

dependencies {
    implementation(project(":feature-ui"))
    implementation(project(":player-media3"))
    implementation(project(":core-data"))
    implementation(project(":core-common"))
    implementation(project(":core-domain"))
    implementation("network.multiplatform:cmptoast:1.0.8")
    implementation("androidx.media3:media3-common:1.7.1")

    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.compose.material3:material3:1.5.0-alpha26")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Koin
    implementation(platform("io.insert-koin:koin-bom:4.2.2"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-android")

    // Coil 3
    implementation("io.coil-kt.coil3:coil:3.5.0")
    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")

    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}

// Exclude duplicate protobuf to avoid DuplicateClass error
configurations.all {
    exclude(group = "com.google.protobuf", module = "protobuf-javalite")
}
