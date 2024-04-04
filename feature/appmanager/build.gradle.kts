plugins {
    id ("com.android.library")
    kotlin(BuildPlugins.kotlinAndroid)
    id("androidx.navigation.safeargs")
    id("dagger.hilt.android.plugin")
    kotlin(BuildPlugins.kotlinKapt)
}

android {

    defaultConfig {
        minSdk = AndroidSdk.min
        compileSdk = AndroidSdk.compile
        targetSdk = AndroidSdk.target
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
        kotlinCompilerExtensionVersion = "1.5.6"
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

//    kapt(libs.lifecycle.compiler)

    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    kapt(libs.annotation)

//    kapt(libs.glide.compiler)
//    implementation(libs.glide.compose)




}