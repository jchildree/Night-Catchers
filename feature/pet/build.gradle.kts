plugins {
    alias(libs.plugins.nightcatchers.android.feature)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.nightcatchers.testing)
}

android {
    namespace = "com.nightcatchers.feature.pet"
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lottie.compose)
}
