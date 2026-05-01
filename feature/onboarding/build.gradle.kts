plugins {
    alias(libs.plugins.nightcatchers.android.feature)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nightcatchers.feature.onboarding"
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:security"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lottie.compose)
}
