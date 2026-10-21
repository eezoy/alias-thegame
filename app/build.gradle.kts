plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Номер сборки на GitHub растёт с каждым запуском, поэтому новые APK
// ставятся поверх старых без удаления приложения.
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

android {
    namespace = "com.eezoy.alias"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.eezoy.alias"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
    }

    signingConfigs {
        // Постоянный ключ подписи: благодаря ему обновления встают поверх
        // старой версии и не сбрасывают настройки и историю слов.
        create("release") {
            storeFile = file("alias-release.jks")
            storePassword = "aliasgame"
            keyAlias = "alias"
            keyPassword = "aliasgame"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
}
