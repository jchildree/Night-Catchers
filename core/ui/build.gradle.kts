plugins {
    alias(libs.plugins.nightcatchers.android.library)
    alias(libs.plugins.nightcatchers.compose)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nightcatchers.core.ui"
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
