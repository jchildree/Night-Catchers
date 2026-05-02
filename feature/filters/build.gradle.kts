plugins {
    alias(libs.plugins.nightcatchers.android.library)
    alias(libs.plugins.nightcatchers.hilt)
    alias(libs.plugins.nightcatchers.testing)
}

android {
    namespace = "com.nightcatchers.feature.filters"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.lottie.compose)
}
