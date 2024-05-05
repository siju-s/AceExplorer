// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    rootProject.apply {
        from(rootProject.file("gradlescripts/version_generation.gradle.kts"))
    }

    repositories {
        google()
        mavenCentral()
        maven { url = uri( "https://jitpack.io") }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath (BuildPlugins.androidGradlePlugin)
        classpath (BuildPlugins.kotlinGradlePlugin)
        classpath (BuildPlugins.googleServices)
        classpath (BuildPlugins.crashlyticsPlugin)
        classpath (BuildPlugins.safeArgsPlugin)
        classpath (BuildPlugins.sonarqube)
        classpath (BuildPlugins.hiltPlugin)


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri( "https://jitpack.io") }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.layout.buildDirectory)
}

