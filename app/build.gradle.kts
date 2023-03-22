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
                Libraries.ResDirs.directories
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

    kotlinOptions.apply {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation(Libraries.ExternalLibs.rootTools) {
        exclude(module = "RootShell")
    }
    implementation(project(":common"))
    implementation(project(":feature:appmanager"))

    implementation(Libraries.ExternalLibs.rootShell)

    implementation(Libraries.viewpager)
    implementation(Libraries.cardView)
    implementation(Libraries.pagination)

    kapt(Libraries.lifecycleCompiler)

    implementation(Libraries.hilt)
    kapt(Libraries.hiltCompiler)

    implementation(Libraries.roomRuntime)
    kapt(Libraries.roomCompiler)

    kapt(Libraries.annotation)

    implementation(Libraries.Firebase.analytics)
    implementation(Libraries.Firebase.crashlytics)

    implementation(Libraries.ExternalLibs.gson)
    implementation(Libraries.playCore)

    kapt(Libraries.ExternalLibs.glideCompiler)

    implementation(Libraries.ExternalLibs.apacheCompress)
    implementation(Libraries.ExternalLibs.floatingButton)
    implementation(Libraries.ExternalLibs.rateApp)
    implementation(Libraries.ExternalLibs.photoView)

//    debugImplementation(Libraries.ExternalLibs.leakCanary)

    testImplementation(Libraries.TestLibs.junit)
    testImplementation(Libraries.TestLibs.mockito)
}