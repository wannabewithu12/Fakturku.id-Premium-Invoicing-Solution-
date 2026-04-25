plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    androidTarget {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                
                // Common dependencies
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("io.insert-koin:koin-core:3.6.0-Beta4")
                implementation("io.insert-koin:koin-compose:1.2.0-Beta4")

                // Room Multiplatform
                val roomVersion = "2.8.4"
                implementation("androidx.room:room-runtime:$roomVersion")
                implementation("androidx.sqlite:sqlite-bundled:2.6.2")
                
                // Navigation
                implementation("androidx.navigation:navigation-compose:2.7.7")
                
                // Icons and Image Loading
                implementation(compose.materialIconsExtended)
                implementation("io.coil-kt:coil-compose:2.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
                implementation("io.insert-koin:koin-android:3.6.0-Beta4")
                implementation("io.insert-koin:koin-androidx-compose:3.6.0-Beta4")
                // Apache POI for Excel export
                implementation("org.apache.poi:poi-ooxml:5.2.3")
                // Google Fonts
                implementation("androidx.compose.ui:ui-text-google-fonts:1.6.3")
                // Biometric
                implementation("androidx.biometric:biometric:1.2.0-alpha05")
                // Vico Chart Library (Compose M3)
                implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")
                // Glance API for Home Screen Widget
                implementation("androidx.glance:glance-appwidget:1.0.0")
                implementation("androidx.glance:glance-material3:1.0.0")
                // WorkManager for background tasks
                implementation("androidx.work:work-runtime-ktx:2.9.0")
                // Jetpack Security — EncryptedSharedPreferences for secure key storage
                implementation("androidx.security:security-crypto:1.1.0-alpha06")

                // Firebase
                implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
                implementation("com.google.firebase:firebase-firestore-ktx")
                implementation("com.google.firebase:firebase-storage-ktx")
                implementation("com.google.firebase:firebase-auth-ktx")
                implementation("com.google.android.gms:play-services-auth:21.2.0")
            }
        }
    }
}

android {
    namespace = "com.fakturkuid.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fakturkuid.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

dependencies {
    add("kspAndroid", "androidx.room:room-compiler:2.8.4")
}
