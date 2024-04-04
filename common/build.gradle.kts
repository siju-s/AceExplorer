plugins {
    id ("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin(BuildPlugins.kotlinKapt)
    id("org.sonarqube")
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
    namespace = "com.siju.acexplorer.common"

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
}

dependencies {

    api(libs.stdlib)
    api(libs.coroutine.core)
    api(libs.coroutine.android)

    api(libs.appCompat)
    api(libs.activityKtx)
    api(libs.fragmentKtx)
    api(libs.preference)

    api(libs.viewModelKtx)
    api(libs.lifecycle.liveData)
    api(libs.lifecycle.runtime)

    kapt(libs.lifecycle.compiler)

    api(libs.recyclerView)
    api(libs.design)
    api(libs.swipeRefresh)
    api(libs.constraintLayout)
    api(libs.palette)

    api(libs.navigation)
    api(libs.navigation.ui)
//    api(libs.navigation.navfeature)
    api(libs.exif)

    api(libs.glide.runtime)
    api(libs.glide.compose)
    kapt(libs.glide.compiler)
    api(libs.fastScrollRecyclerview)

    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.material)
    api(libs.compose.tooling)
    api(libs.compose.livedata)
    api(libs.compose.compiler)

}