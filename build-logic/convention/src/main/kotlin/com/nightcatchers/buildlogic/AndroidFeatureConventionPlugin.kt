package com.nightcatchers.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nightcatchers.android.library")
            pluginManager.apply("nightcatchers.hilt")
            pluginManager.apply("nightcatchers.compose")

            dependencies {
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:domain"))
                "implementation"(project(":core:common"))
            }
        }
    }
}
