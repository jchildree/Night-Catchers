plugins {
    alias(libs.plugins.nightcatchers.android.library)
    alias(libs.plugins.nightcatchers.hilt)
}

android {
    namespace = "com.nightcatchers.core.security"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.biometric)
    implementation(libs.bcrypt)
    implementation(libs.androidx.datastore.preferences)
}
