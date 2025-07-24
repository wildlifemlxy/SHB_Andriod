plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ecss.shb_andriod"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ecss.shb_andriod"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson converter for JSON parsing
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp for HTTP client (optional, but recommended)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // MPAndroidChart for charting
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}