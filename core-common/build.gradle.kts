plugins {
    id("com.android.library")
}

android {
    namespace = "com.maxrave.common"
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
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
    implementation("co.touchlab:kermit:2.1.0")
}
