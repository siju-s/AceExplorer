plugins {
    id ("com.android.library")
    kotlin(BuildPlugins.kotlinAndroid)
    kotlin(BuildPlugins.kotlinKapt)
    id("androidx.navigation.safeargs")
    id("dagger.hilt.android.plugin")
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
//        dataBinding = true
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

    kapt(Libraries.lifecycleCompiler)

    implementation(Libraries.hilt)
    kapt(Libraries.hiltCompiler)
    kapt(Libraries.annotation)

    kapt(Libraries.ExternalLibs.glideCompiler)

}