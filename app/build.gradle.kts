

import java.io.FileInputStream
import java.util.*

plugins {
    id(BuildPlugins.androidApp)
    kotlin(BuildPlugins.kotlinAndroid)
    kotlin(BuildPlugins.kotlinAndroidExtensions)
    kotlin(BuildPlugins.kotlinKapt)
    id(BuildPlugins.googleServicesPlugin)
    id(BuildPlugins.crashlyticsAppPlugin)
    id("androidx.navigation.safeargs")
    id("org.sonarqube")
}

android {
    compileSdkVersion(AndroidSdk.compile)

    signingConfigs {
        if (rootProject.file("keystore.properties").exists()) {
            val signingRelease = Properties()
            signingRelease.load(FileInputStream(rootProject.file("keystore.properties")))
            create("release") {
                storeFile =  rootProject.file(signingRelease.getProperty("storeFile"))
                storePassword = signingRelease.getProperty("storePassword")
                keyAlias = signingRelease.getProperty("keyAlias")
                keyPassword = signingRelease.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = AndroidSdk.applicationId
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        print("Apply root gradle:${rootProject.extra["versionCode"]}")
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        resConfigs("en", "af", "ar", "bg", "da", "de", "el", "es", "fa", "fr", "ga", "hi", "hr", "hu",
        "hy", "in", "is", "it", "iw", "ja", "ka", "ko", "mk", "nl", "no", "pl", "pt", "ro", "ru",
        "sl", "sq", "sr", "sv", "th", "tr", "uk", "vi", "zh", "zh-rCN", "zh-rTW")
    }


    dexOptions {
        maxProcessCount = 2
        javaMaxHeapSize = "8g"
    }
//    configurations {
//        all*.exclude group: "commons-logging", module: "commons-logging"
//    }
    packagingOptions {
        exclude ("proguard-project.txt")
        exclude ("project.properties")
        exclude ("META-INF/LICENSE.txt")
        exclude ("META-INF/LICENSE")
        exclude ("META-INF/NOTICE.txt")
        exclude ("META-INF/NOTICE")
        exclude ("META-INF/DEPENDENCIES.txt")
        exclude ("META-INF/DEPENDENCIES")
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            firebaseCrashlytics {
                // If you don't need crash reporting for your debug build,
                // you can speed up your build by disabling mapping file uploading.
                mappingFileUploadEnabled = false
            }
        }


        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            applicationVariants.all {
                if (this.buildType.name == "release") {
                    this.outputs.all {
                        this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                        this.outputFileName = "Ace_" + defaultConfig.versionName + ".apk"
                    }
                }
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }

    sourceSets {
       getByName("main").res.srcDirs(
               Libraries.ResDirs.directories
       )
    }


    flavorDimensions("dimen")
    productFlavors {
        create("dev") {
            applicationIdSuffix = ".dev"
            buildConfigField ("boolean", "IS_DEV_VERSION", "true")
        }
        create("prod") {
            buildConfigField ("boolean", "IS_DEV_VERSION", "false")
        }
    }
    lintOptions {
        isAbortOnError = false
    }

    sonarqube {
        properties {
            property("sonar.projectName", "ace-explorer")
            property ("sonar.projectKey", "ace-explorer")
            property ("sonar.language", "kotlin")
            property ("sonar.sources", "src/main/java/")
            property ("sonar.tests", "src/test/java/")
            property ("sonar.binaries", "build")
            property ("sonar.sourceEncoding", "UTF-8")
            property ("sonar.login", "admin")
            property ("sonar.password", "admin")
            property ("sonar.host.url", "http://localhost:9000")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(Libraries.ExternalLibs.rootTools) {
         exclude(module = "RootShell")
    }
    implementation (Libraries.ExternalLibs.rootShell)
    implementation (project(":billingsecure"))

    implementation (Libraries.Kotlin.stdlib)
    implementation (Libraries.Kotlin.coroutineCore)
    implementation (Libraries.Kotlin.coroutineAndroid)

    implementation (Libraries.viewModelKtx)
    implementation (Libraries.lifecycleLiveData)
    implementation(Libraries.lifecycleRuntime)
    kapt(Libraries.lifecycleCompiler)

    implementation (Libraries.roomRuntime)
    kapt  (Libraries.roomCompiler)

    // Support libraries
    implementation (Libraries.appCompat)
    implementation (Libraries.recyclerView)
    implementation (Libraries.design)
    implementation (Libraries.viewpager)
    implementation (Libraries.cardView)
    implementation (Libraries.palette)
    implementation (Libraries.preference)
    implementation (Libraries.navigation)
    implementation (Libraries.navigationUi)
    kapt (Libraries.annotation)

    implementation (Libraries.Firebase.ads)
    implementation (Libraries.Firebase.invites)
    implementation (Libraries.Firebase.analytics)

    implementation (Libraries.billing)
    implementation (Libraries.billingKtx)
    implementation (Libraries.ExternalLibs.gson)
    implementation (Libraries.playCore)

    implementation  (Libraries.ExternalLibs.glideRuntime)
    kapt (Libraries.ExternalLibs.glideCompiler)

    implementation (Libraries.ExternalLibs.apacheCompress)
    implementation (Libraries.ExternalLibs.floatingButton)
    implementation (Libraries.ExternalLibs.rateApp)
    implementation (Libraries.ExternalLibs.photoView)
    implementation (Libraries.ExternalLibs.fastScrollRecyclerview)

//    debugImplementation (Libraries.ExternalLibs.leakCanary)

    implementation (Libraries.ExternalLibs.crashlytics)

    testImplementation (Libraries.TestLibs.junit)
    testImplementation (Libraries.TestLibs.mockito)
}