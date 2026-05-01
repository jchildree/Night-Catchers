plugins {
    alias(libs.plugins.nightcatchers.android.feature)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nightcatchers.feature.vault"
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)
}
