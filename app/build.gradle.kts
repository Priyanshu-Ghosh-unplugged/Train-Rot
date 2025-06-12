plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.ghosh.trainrot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ghosh.trainrot"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    
    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.litert)
    ksp(libs.room.compiler)
    
    // GraphQL
    implementation(libs.apollo.runtime)
    
    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Material Design 3
    implementation(libs.material.v1110)
    
    // Lottie Animations
    implementation(libs.lottie)
    
    // Maps and Location
    implementation(libs.play.services.maps.v1920)
    implementation(libs.play.services.location.v2130)
    
    // AR Core
    implementation(libs.core.v1490)
    
    // ML Kit for CAPTCHA solving
    implementation(libs.mlkit.vision)
    implementation(libs.image.labeling.v1709)
    
    // Payment Integration
    implementation(libs.razorpay)
    
    // Blockchain Integration
    implementation(libs.web3j)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}