plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cycleapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cycleapp"
        minSdk = 26
        targetSdk = 34
        // versionCode растёт с каждым запуском сборки на GitHub (номер запуска), поэтому каждая
        // новая сборка считается обновлением и ставится поверх предыдущей.
        versionCode = 100 + (System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0)
        versionName = "1.4"
    }

    // Общий ключ подписи, лежит в репозитории. Раньше каждая сборка на GitHub получала новый случайный
    // debug-ключ, и Android отказывался ставить новую версию поверх старой («конфликт пакетов»).
    // Теперь все сборки подписаны одним и тем же ключом.
    signingConfigs {
        create("shared") {
            storeFile = file("cycleapp-shared.keystore")
            storePassword = "cycleapp"
            keyAlias = "cycleapp"
            keyPassword = "cycleapp"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("shared")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
