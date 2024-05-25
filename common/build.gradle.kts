plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.sonarqube)
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

    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    namespace = "com.siju.acexplorer.common"

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

    ksp(libs.lifecycle.compiler)

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
    ksp(libs.glide.compiler)
    api(libs.fastScrollRecyclerview)

    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.material)
    api(libs.compose.tooling)
    api(libs.compose.livedata)
    api(libs.compose.compiler)
    api(libs.compose.navigation)

}