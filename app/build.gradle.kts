plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/{AL2.0,LGPL2.1}",
                "META-INF/versions/9/**",
                "META-INF/versions/9/module-info.class",
                "META-INF/versions/9/previous-compilation-data.bin*",
                "META-INF/versions/9/previous-compilation-data.bin.*",
                "META-INF/versions/9/previous-compilation-data.bin.tmp*",
                "META-INF/versions/9/previous-compilation-data.bin.tmp.*"
            )
            pickFirsts += setOf(
                "META-INF/versions/9/module-info.class",
                "META-INF/versions/9/previous-compilation-data.bin"
            )
            merges += setOf(
                "META-INF/versions/9/module-info.class",
                "META-INF/versions/9/previous-compilation-data.bin"
            )
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${libs.versions.kotlin.get()}")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${libs.versions.kotlin.get()}")
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // Apache Commons Math3
    implementation(libs.commons.math3)
    
    // ThreeTenABP for time API compatibility
    implementation(libs.threetenabp)
    
    // Selenium WebDriver
    implementation(libs.selenium.java)
    implementation(libs.selenium.chrome.driver)
    implementation(libs.selenium.support)
    
    // TensorFlow Lite
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    
    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
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