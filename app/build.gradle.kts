plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    // Add serialization plugin for easier data handling
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.uj.echocrisismain"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.uj.echocrisismain"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Existing dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    
    // Firebase App Check (debug) - silences AppCheck warning
    implementation("com.google.firebase:firebase-appcheck-debug:17.1.1")

    // NEW DEPENDENCIES FOR ECOCRISIS APP

    // Firebase BOM (Bill of Materials) - ensures compatible versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Storage for photo uploads
    implementation("com.google.firebase:firebase-storage-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Compose Extended Icons
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Date Picker
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Permissions handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Data serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // DataStore for preferences (better than SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Charts for progress visualization
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Camera and image handling
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // System UI Controller for status bar
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation(libs.firebase.database)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}