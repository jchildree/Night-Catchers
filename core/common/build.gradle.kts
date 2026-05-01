plugins {
    alias(libs.plugins.nightcatchers.android.library)
    alias(libs.plugins.nightcatchers.testing)
    alias(libs.plugins.nightcatchers.hilt)
}

android {
    namespace = "com.nightcatchers.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
}
