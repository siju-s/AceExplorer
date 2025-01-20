// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    rootProject.apply {
        from(rootProject.file("gradlescripts/version_generation.gradle.kts"))
    }

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.ksp)  apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.safeargs) apply false
    alias(libs.plugins.sonarqube) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.firebase.perf) apply  false
}



tasks.register("clean", Delete::class){
    delete(rootProject.layout.buildDirectory)
}

