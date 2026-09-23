plugins {
    id("com.android.library")
}

android {
    namespace = "com.maxrave.scraper"
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
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-ktorext"))
    implementation(project(":core-common"))
    implementation("io.ktor:ktor-client-core:3.5.2")
    implementation("io.ktor:ktor-client-okhttp:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
    implementation("io.ktor:ktor-client-encoding:3.5.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.5.2")
    implementation("io.ktor:ktor-client-logging:3.5.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.4.10")
    implementation("com.eygraber:uri-kmp:0.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("com.squareup.okio:okio:3.18.1")
    // HTML parsing
    implementation("com.mohamedrejeb.ksoup:ksoup-html:0.6.0")
    implementation("com.mohamedrejeb.ksoup:ksoup-entities:0.6.0")
    // QuickJS for cipher
    implementation("io.github.dokar3:quickjs-kt:1.0.14")
    // PipePipe extractor (YouTube)
    implementation("com.github.maxrave-dev:PipePipeExtractor:f8982ca9e7")
    // NewPipe extractor
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.26.1")
    // XML (upstream resolves xmlutil transitively via ktor-xml)
    implementation("io.ktor:ktor-serialization-kotlinx-xml:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-protobuf:3.5.2")
    implementation("io.github.maxrave-dev:ffmpeg-kit-audio:6.0.1")
    // Protobuf content negotiation (Ytmusic client)
    implementation("io.ktor:ktor-serialization-kotlinx-protobuf:3.5.2")
    // FFmpegKit for download post-processing (upstream androidMain)
    implementation("io.github.maxrave-dev:ffmpeg-kit-audio:6.0.1")
}
