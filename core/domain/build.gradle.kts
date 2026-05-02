plugins {
    alias(libs.plugins.nightcatchers.android.library)
    alias(libs.plugins.nightcatchers.testing)
    alias(libs.plugins.nightcatchers.hilt)
}

android {
    namespace = "com.nightcatchers.core.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.android)
}
