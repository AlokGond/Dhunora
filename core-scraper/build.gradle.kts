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
    implementation("io.ktor:ktor-client-core:3.5.2")
    implementation("io.ktor:ktor-client-okhttp:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:2.4.10")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("com.squareup.okio:okio:3.18.1")
    // HTML parsing
    implementation("com.mohamedrejeb:ksoup:0.6.0")
    // QuickJS for cipher
    implementation("io.github.dokar3:quickjs-kt:1.0.14")
    // PipePipe extractor (YouTube)
    implementation("com.github.maxrave-dev:PipePipeExtractor:f8982ca9e7")
    // NewPipe extractor
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.26.1")
    // XML
    implementation("nl.adaptivity.xmlutil:xmlutil-core:0.90.0")
}
