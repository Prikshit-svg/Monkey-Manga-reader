plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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



    buildFeatures {
        compose = true
    }
    //noinspection WrongGradleMethod
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")   // faster incremental builds
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Lifecycle + ViewModel
    implementation(libs.bundles.lifecycle)

    // Auth
    implementation(libs.play.services.auth)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Networking
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.moshi)
    ksp(libs.moshi.codegen)

    // Coil 3
    implementation(libs.bundles.coil)

    // DataStore
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)

    // Security
    implementation(libs.security.crypto)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Paging
    implementation(libs.bundles.paging)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}