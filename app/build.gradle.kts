import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.safeargs)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)

}

val keyProperties = Properties()
try {
    keyProperties.load(FileInputStream(rootProject.file("keystore.properties")))
} catch (_: java.io.IOException) {

}


android {
    signingConfigs {
            create("release") {
                storeFile = file(keyProperties.getProperty("KEYSTORE_FILE").toString())
                storePassword = keyProperties.getProperty("KEYSTORE_PASSWORD").toString()
                keyAlias = keyProperties.getProperty("KEY_ALIAS").toString()
                keyPassword = keyProperties.getProperty("KEY_PASSWORD").toString()
            }

    }
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.siju.acexplorer"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        resourceConfigurations.addAll(
           setOf("en", "af", "ar", "bg", "da", "de", "el", "es", "fa", "fr", "ga", "hi", "hr", "hu",
                "hy", "in", "is", "it", "iw", "ja", "ka", "ko", "mk", "nl", "no", "pl", "pt", "ro", "ru",
                "sl", "sq", "sr", "sv", "th", "tr", "uk", "vi", "zh", "zh-rCN", "zh-rTW"))
    }


    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }


        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
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
        }
    }

    sourceSets {
        getByName("main").res.srcDirs(
            arrayOf(
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

        )
    }

    flavorDimensions.add("dimen")
    productFlavors {
        create("dev") {
            applicationIdSuffix = ".dev"
            buildConfigField("boolean", "IS_DEV_VERSION", "true")
        }
        create("prod") {
            buildConfigField("boolean", "IS_DEV_VERSION", "false")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig= true
        compose = true
    }


    sonarqube {
        properties {
            property("sonar.projectKey", "AceExplorer")
            property("sonar.language", "kotlin")
            property("sonar.sources", "src/main/java/")
            property("sonar.tests", "src/test/java/")
            property("sonar.binaries", "build")
            property("sonar.sourceEncoding", "UTF-8")
            property("sonar.host.url", "http://localhost:9000")
        }
    }

    kotlin {
        jvmToolchain(22)
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    lint {
        abortOnError = false
        xmlReport = false
        lintConfig = File("${project.rootDir}/lint/lint-config.xml")
        htmlOutput = File("${project.rootDir}/lint/lint-report.html")
    }
    namespace = "com.siju.acexplorer"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.root.tools) {
        exclude(module = "RootShell")
    }
    implementation(project(":common"))
    implementation(project(":feature:appmanager"))

    implementation(libs.root.shell)

    implementation(libs.viewpager)
    implementation(libs.cardView)
    implementation(libs.pagination)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.analytics)
    implementation(libs.crashlytics)

    implementation(libs.gson)
    implementation(libs.play.app.update)
    implementation(libs.play.review)

   implementation(libs.apacheCompress)
    implementation(libs.floatingButton)
//    implementation(libs.rateApp)
    implementation(libs.photoView)

    ksp(libs.glide.compiler)


//    debugImplementation(Libraries.ExternalLibs.leakCanary)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
}