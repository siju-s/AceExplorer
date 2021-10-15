const val kotlinVersion = "1.4.21"

object BuildPlugins {

    object Versions {
        const val gradlePlugin = "7.0.3"
        const val crashlytics = "2.6.1"
        const val googleServices = "4.3.8"
        const val safeArgsPlugin = "2.3.5"
        const val sonarqube = "3.0"
        const val hilt = "2.37"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradlePlugin}"
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
    const val compile = 31
    const val target = compile
    const val applicationId = "com.siju.acexplorer"
}

object Libraries {
   private object Versions {
       const val appCompat    = "1.2.0"
       const val design       = "1.4.0"
       const val support      = "1.0.0"
       const val annotation   = "1.1.0"
       const val recyclerview = "1.1.0"
       const val preference   = "1.1.0"
       const val viewpager    = "1.0.0"
       const val lifecycle    = "2.2.0"
       const val room         = "2.2.6"
       const val playCore     = "1.10.0"
       const val billing      = "3.0.0"
       const val navigation   = "2.3.5"
       const val constraintLayout = "2.0.4"
       const val exif             = "1.3.2"
       const val activity         = "1.1.0"
       const val fragment         = "1.3.6"
       const val swiperefresh     = "1.1.0"
       const val pagination       = "3.0.1"

   }
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val design    = "com.google.android.material:material:${Versions.design}"
    const val viewpager   = "androidx.viewpager2:viewpager2:${Versions.viewpager}"
    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
    const val preference   = "androidx.preference:preference:${Versions.preference}"
    const val cardView     = "androidx.cardview:cardview:${Versions.support}"
    const val palette      = "androidx.palette:palette:${Versions.support}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val lifecycleRuntime       = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val lifecycleLiveData      = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val viewModelKtx           = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val lifecycleCompiler      = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"
    const val activityKtx           = "androidx.activity:activity-ktx:${Versions.activity}"
    const val fragmentKtx           = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    const val swipeRefresh          = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swiperefresh}"
    const val pagination            =  "androidx.paging:paging-runtime:${Versions.pagination}"

    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"

    const val playCore       = "com.google.android.play:core:${Versions.playCore}"

    const val billingKtx     = "com.android.billingclient:billing-ktx:${Versions.billing}"

    const val navigation     = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigationUi   = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    const val navFeature     = "androidx.navigation:navigation-dynamic-features-fragment:${Versions.navigation}"
    const val hilt           = "com.google.dagger:hilt-android:${BuildPlugins.Versions.hilt}"
    const val hiltCompiler   = "com.google.dagger:hilt-android-compiler:${BuildPlugins.Versions.hilt}"

    const val exif           = "androidx.exifinterface:exifinterface:${Versions.exif}"


   object Firebase {

       private object Versions {
           const val ads          = "20.1.0"
           const val analytics    = "19.0.0"
           const val crashlytics  = "18.0.0"
       }

       const val ads     = "com.google.android.gms:play-services-ads:${Versions.ads}"
       const val analytics = "com.google.firebase:firebase-analytics-ktx:${Versions.analytics}"
       const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx:${Versions.crashlytics}"
   }

    object Kotlin {
        private object Versions {
            const val stdlib          = "1.5.21"
            const val coroutineCore      = "1.4.1"
        }

        const val stdlib            = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.stdlib}"
        const val coroutineCore     = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutineCore}"
        const val coroutineAndroid  = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutineCore}"
    }

    object ExternalLibs {
        private object Versions {
            const val gson = "2.8.6"
            const val glide = "4.12.0"
            const val apacheCompress = "1.20"
            const val ratethisapp = "1.2.0"
            const val floatingButton     = "1.10.1"
            const val leakCanary          = "2.6"
            const val photoView           = "2.3.0"
            const val fastscrollRecycler = "2.0.1"
            const val rootTools          = "5.0"
            const val rootShell          = "1.6"
        }

        const val gson        = "com.google.code.gson:gson:${Versions.gson}"
        const val glideRuntime  = "com.github.bumptech.glide:glide:${Versions.glide}"
        const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
        const val apacheCompress   = "org.apache.commons:commons-compress:${Versions.apacheCompress}"
        const val floatingButton  = "com.getbase:floatingactionbutton:${Versions.floatingButton}"
        const val rateApp          = "io.github.kobakei:ratethisapp:${Versions.ratethisapp}"
        const val leakCanary       = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
        const val photoView        = "com.github.chrisbanes:PhotoView:${Versions.photoView}"
        const val fastScrollRecyclerview = "com.simplecityapps:recyclerview-fastscroll:${Versions.fastscrollRecycler}"
        const val rootTools        = "com.github.Stericson:RootTools:${Versions.rootTools}"
        const val rootShell        = "com.github.Stericson:RootShell:${Versions.rootShell}"
    }

    object TestLibs {
        private object Versions {
            const val junit =    "4.12"
            const val mockito  = "3.3.3"
        }

        const val junit    = "junit:junit:${Versions.junit}"
        const val mockito  = "org.mockito:mockito-core:${Versions.mockito}"
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


}