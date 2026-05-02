plugins {
    alias(libs.plugins.nightcatchers.android.feature)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.nightcatchers.testing)
}

android {
    namespace = "com.nightcatchers.feature.capture"
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":feature:ar"))
    implementation(project(":feature:filters"))

    implementation(libs.camerax.view)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
}
