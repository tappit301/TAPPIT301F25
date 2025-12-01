// Top-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false

    // Google Services plugin declared here but NOT applied
    id("com.google.gms.google-services") version "4.4.4" apply false
}
