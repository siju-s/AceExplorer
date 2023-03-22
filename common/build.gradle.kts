plugins {
    id ("com.android.library")
    id ("kotlin-android")
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
    kotlinOptions.apply {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.siju.acexplorer.common"
}

dependencies {

    api(Libraries.Kotlin.stdlib)
    api(Libraries.Kotlin.coroutineCore)
    api(Libraries.Kotlin.coroutineAndroid)

    api(Libraries.appCompat)
    api(Libraries.activityKtx)
    api(Libraries.fragmentKtx)
    api(Libraries.preference)

    api(Libraries.viewModelKtx)
    api(Libraries.lifecycleLiveData)
    api(Libraries.lifecycleRuntime)

    kapt(Libraries.lifecycleCompiler)

    api(Libraries.recyclerView)
    api(Libraries.design)
    api(Libraries.swipeRefresh)
    api(Libraries.constraintLayout)
    api(Libraries.palette)

    api(Libraries.navigation)
    api(Libraries.navigationUi)
    api(Libraries.navFeature)
    api(Libraries.exif)

    api(Libraries.ExternalLibs.glideRuntime)
    kapt(Libraries.ExternalLibs.glideCompiler)
    api(Libraries.ExternalLibs.fastScrollRecyclerview)

}