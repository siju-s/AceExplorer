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
}

dependencies {

    implementation(Libraries.Kotlin.stdlib)
    implementation(Libraries.Kotlin.coroutineCore)
    implementation(Libraries.Kotlin.coroutineAndroid)

    implementation(Libraries.viewModelKtx)
    implementation(Libraries.lifecycleLiveData)
    implementation(Libraries.lifecycleRuntime)
    implementation(Libraries.constraintLayout)
    kapt(Libraries.lifecycleCompiler)

    implementation(Libraries.appCompat)
    implementation(Libraries.recyclerView)
    implementation(Libraries.design)

    implementation(Libraries.ExternalLibs.glideRuntime)
    kapt(Libraries.ExternalLibs.glideCompiler)
    implementation(Libraries.palette)

}