plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id ("kotlin-kapt")
    id ("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id ("androidx.navigation.safeargs")
}
android {
    namespace = "com.adista.finalproject"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.adista.finalproject"
        minSdk = 24
        targetSdk = 34
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

    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
}
dependencies {
    implementation(libs.core.ktx.v1120)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.google.material.v1100)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    // ROOM
    val roomVersion = "2.6.1"
    implementation (libs.room.runtime)
    ksp(libs.room.compiler)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation (libs.room.ktx)
    // Navigation
    val navVersion = "2.7.5"
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // Life Cycle Arch
    val lifecycleVersion = "2.6.2"
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v262)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx.v262)
    // Annotation processor
    ksp(libs.androidx.lifecycle.compiler)
    //SDP SSP
    implementation (libs.sdp.android)
    implementation (libs.ssp.android)
}