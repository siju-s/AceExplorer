import java.io.FileInputStream
import java.util.*

plugins {
    id(BuildPlugins.androidApp)
    kotlin(BuildPlugins.kotlinAndroid)
    kotlin(BuildPlugins.kotlinKapt)
    id(BuildPlugins.kotlinParcelize)
    id(BuildPlugins.googleServicesPlugin)
    id(BuildPlugins.crashlyticsAppPlugin)
    id("androidx.navigation.safeargs")
    id("org.sonarqube")
    id("dagger.hilt.android.plugin")
}

val keyProperties = Properties()
try {
    keyProperties.load(FileInputStream(rootProject.file("keystore.properties")))
} catch (exception: java.io.IOException) {

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


    defaultConfig {
        applicationId = AndroidSdk.applicationId
        minSdk = AndroidSdk.min
        compileSdk = AndroidSdk.compile
        targetSdk = AndroidSdk.target
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
                ResDirs.directories
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

    viewBinding {
        android.buildFeatures.viewBinding = true
    }

    buildFeatures {
        dataBinding = true
        buildConfig= true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
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
        jvmToolchain(17)
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        //isCheckDependencies = true
        //isAbortOnError = false
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

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.material)



    kapt(libs.lifecycle.compiler)

    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)

    kapt(libs.annotation)

    implementation(libs.analytics)
    implementation(libs.crashlytics)

    implementation(libs.gson)
    implementation(libs.play.core)

    kapt(libs.glide.compiler)

    implementation(libs.apacheCompress)
    implementation(libs.floatingButton)
    implementation(libs.rateApp)
    implementation(libs.photoView)

//    debugImplementation(Libraries.ExternalLibs.leakCanary)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
}