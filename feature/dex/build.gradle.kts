plugins {
    alias(libs.plugins.nightcatchers.android.feature)
}

android {
    namespace = "com.nightcatchers.feature.dex"
}

dependencies {
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}