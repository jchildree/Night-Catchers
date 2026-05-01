plugins {
    alias(libs.plugins.nightcatchers.android.library)
    alias(libs.plugins.nightcatchers.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nightcatchers.core.network"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
}
