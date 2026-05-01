plugins {
    alias(libs.plugins.nightcatchers.android.feature)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.nightcatchers.testing)
}

android {
    namespace = "com.nightcatchers.feature.ar"
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":feature:filters"))

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // ARCore
    implementation(libs.arcore)

    // ML Kit
    implementation(libs.mlkit.object.detection)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
}
