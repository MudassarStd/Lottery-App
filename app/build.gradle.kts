plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.lottery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lottery"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true

    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.foundation.android)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.runner)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Firebase Core SDKs
    implementation (libs.firebase.auth.ktx)

    implementation (libs.firebase.messaging)

    implementation (libs.firebase.database.ktx)
    implementation (libs.firebase.storage.ktx)

    // Firebase UI (Optional for authentication UI)
    implementation (libs.firebase.ui.auth)

    // Realtime Database UI (Optional for ListView/RecyclerView binding)
    implementation (libs.firebase.ui.database)
    implementation (libs.firebase.auth)
    implementation (libs.firebase.database)

    // Lifecycle Components (for ViewModel, LiveData)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)

    // Kotlin Coroutines (for async operations)
    implementation (libs.kotlinx.coroutines.android)

    // Testing Libraries
    testImplementation (libs.junit)
    androidTestImplementation (libs.androidx.junit.v115)
    androidTestImplementation (libs.androidx.espresso.core.v351)

//    implementation (libs.firebase.admin)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

}