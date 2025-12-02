plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.eventapp"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {

    // ---------------------------------------------------------
    // UI + AndroidX
    // ---------------------------------------------------------
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment:1.6.2")

    // Navigation Component (ALL must match)
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // ---------------------------------------------------------
    // Firebase
    // ---------------------------------------------------------
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // ---------------------------------------------------------
    // Images (Glide)
    // ---------------------------------------------------------
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ---------------------------------------------------------
    // QR Code
    // ---------------------------------------------------------
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")

    // ---------------------------------------------------------
    // Google Play Services
    // ---------------------------------------------------------
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // ---------------------------------------------------------
    // Unit Testing (Local JVM tests with Robolectric)
    // ---------------------------------------------------------
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // Fragment testing (works with Robolectric)
    testImplementation("androidx.fragment:fragment-testing:1.6.2")

    // ---------------------------------------------------------
    // Navigation Testing (IMPORTANT)
    // ---------------------------------------------------------
    testImplementation("androidx.navigation:navigation-testing:2.7.7")
    testImplementation("androidx.navigation:navigation-runtime:2.7.7")
    // ^ REQUIRED to fix TestNavHostController + Navigation.setViewNavController()

    // ---------------------------------------------------------
    // Instrumentation Tests (if needed)
    // ---------------------------------------------------------
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("androidx.navigation:navigation-testing:2.7.7")
}
