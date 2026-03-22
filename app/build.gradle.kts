plugins {
    alias(libs.plugins.android.application)

    // ✅ Add this line
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.healthops"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.healthops"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)

    // ✅ Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))

    // ✅ Firebase Authentication (IMPORTANT)
    implementation("com.google.firebase:firebase-auth")

    // ✅ Optional (good for viva)
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-firestore")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}