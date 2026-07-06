plugins {
    id("com.android.application")
}

android {
    namespace = "com.ombrebrain.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ombrebrain.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("ombrebrain.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "ombrebrain123"
                keyAlias = "ombrebrain"
                keyPassword = "ombrebrain123"
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.9.0")
    implementation("com.google.android.material:material:1.11.0")
}
