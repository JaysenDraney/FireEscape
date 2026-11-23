plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.fireescape.spike.plugin"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }
    defaultConfig {
        applicationId = "org.fireescape.spike.plugin"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.1-spike"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":plugin-api"))
}