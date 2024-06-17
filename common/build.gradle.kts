plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.safeargs)
}

android {

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildTypes {
        getByName("release") {
            // TODO Revisit later after Kotlin 2.0 and Hilt KSP matures
            isMinifyEnabled = false
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
        abortOnError = false
        lintConfig = File("${project.rootDir}/lint/lint-config.xml")
        htmlOutput = File("${project.rootDir}/lint/lint-report.html")
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
    api(libs.annotation)


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
    api(libs.compose.material3)
    api(libs.compose.tooling)
    api(libs.compose.livedata)
    api(libs.compose.compiler)
    api(libs.compose.navigation)
    api(libs.androidx.activity.compose)
    api(libs.androidx.fragment.compose)
    api(platform(libs.androidx.compose.bom))

}