plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "org.fireescape.launcher"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }

    defaultConfig {
        applicationId = "org.fireescape.launcher"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

roborazzi {
    // Reference images are committed (ADR 0002).
    outputDir.set(file("src/screenshots"))
    // Comparison images are build output, uploaded by CI (ADR 0003).
    @OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
    compare {
        outputDir.set(file("build/outputs/screenshots_comparison"))
    }
}

dependencies {
    implementation(project(":core-model"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.tv.material)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    debugImplementation(libs.compose.ui.test.manifest)
}
