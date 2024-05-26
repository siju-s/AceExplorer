plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.safeargs)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
}

android {

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    kotlin {
        jvmToolchain(22)
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }


    lint {
//        isAbortOnError = false
        lintConfig = File("${project.rootDir}/lint/lint-config.xml")
        htmlOutput = File("${project.rootDir}/lint/lint-report.html")
    }
    namespace = "com.siju.acexplorer.appmanager"
}

dependencies {
    implementation(project(":common"))

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)
    ksp(libs.lifecycle.compiler)

    ksp(libs.glide.compiler)

}