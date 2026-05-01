import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.nightcatchers.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "nightcatchers.android.application"
            implementationClass = "com.nightcatchers.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "nightcatchers.android.library"
            implementationClass = "com.nightcatchers.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "nightcatchers.android.feature"
            implementationClass = "com.nightcatchers.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id = "nightcatchers.hilt"
            implementationClass = "com.nightcatchers.buildlogic.HiltConventionPlugin"
        }
        register("compose") {
            id = "nightcatchers.compose"
            implementationClass = "com.nightcatchers.buildlogic.ComposeConventionPlugin"
        }
        register("testing") {
            id = "nightcatchers.testing"
            implementationClass = "com.nightcatchers.buildlogic.TestingConventionPlugin"
        }
    }
}
