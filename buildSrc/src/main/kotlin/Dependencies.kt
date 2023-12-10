const val kotlinVersion = "1.8.20"
const val gradleVersion = "8.2.0"

object BuildPlugins {

    object Versions {
        const val crashlytics = "2.9.4"
        const val googleServices = "4.3.15"
        const val safeArgsPlugin = "2.5.3"
        const val sonarqube = "3.3"
        const val hilt = "2.45"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${gradleVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val googleServices = "com.google.gms:google-services:${Versions.googleServices}"
    const val androidApp     = "com.android.application"
    const val kotlinAndroid = "android"
    const val kotlinParcelize = "kotlin-parcelize"
    const val kotlinKapt = "kapt"
    const val googleServicesPlugin = "com.google.gms.google-services"
    const val crashlyticsPlugin = "com.google.firebase:firebase-crashlytics-gradle:${Versions.crashlytics}"
    const val safeArgsPlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.safeArgsPlugin}"
    const val sonarqube = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${Versions.sonarqube}"
    const val crashlyticsAppPlugin = "com.google.firebase.crashlytics"
    const val hiltPlugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}"
}

object AndroidSdk {
    const val min = 21
    const val compile = 34
    const val target = compile
    const val applicationId = "com.siju.acexplorer"
}

    object ResDirs {
        val directories = arrayOf(
                "src/main/res-appmanager",
                "src/main/res-common",
                "src/main/res-main",
                "src/main/res-permission",
                "src/main/res-screen",
                "src/main/res-screen/common",
                "src/main/res-screen/home",
                "src/main/res-screen/imageviewer",
                "src/main/res-screen/tools",
                "src/main/res-screen/storage",
                "src/main/res-screen/search",
                "src/main/res-screen/storage/common",
                "src/main/res-screen/storage/info",
                "src/main/res-screen/storage/operations",
                "src/main/res-screen/storage/operations/paste",
                "src/main/res-screen/storage/peekpop",
                "src/main/res-screen/storage/sort",
                "src/main/res-settings",
                "src/main/res-settings/about",
                "src/main/res-welcome")
    }
