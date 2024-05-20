plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.safeargs)
    alias(libs.plugins.hilt)
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
        jvmToolchain(17)
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
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
    kapt(libs.hilt.compiler)
    kapt(libs.annotation)
}