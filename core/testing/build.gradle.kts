plugins {
    alias(libs.plugins.nightcatchers.android.library)
}

android {
    namespace = "com.nightcatchers.core.testing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.turbine)
    implementation(libs.mockk)
    implementation(libs.kotest.assertions)
}
