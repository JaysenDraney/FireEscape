plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "org.fireescape.conformance"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }
    defaultConfig {
        minSdk = 28
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":plugin-api"))
    implementation(project(":core-model"))
    testImplementation(libs.junit)
}
