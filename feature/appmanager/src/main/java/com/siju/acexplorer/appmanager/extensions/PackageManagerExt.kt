package com.siju.acexplorer.appmanager.extensions

import android.content.pm.PackageManager
import com.siju.acexplorer.common.utils.SdkHelper

/**
 * Package that started the install, for example the store the user tapped install in.
 */
@Suppress("deprecation")
fun PackageManager.getInstallerPackage(packageName : String) : String? {
    return if (SdkHelper.isAtleastAndroid11) {
        this.getInstallSourceInfo(packageName).initiatingPackageName
    }
    else {
        this.getInstallerPackageName(packageName)
    }
}

/**
 * Package that performed the install. This differs from the initiator when a store hands the APK
 * over to the system package installer.
 */
@Suppress("deprecation")
fun PackageManager.getInstallingPackage(packageName: String): String? {
    return if (SdkHelper.isAtleastAndroid11) {
        this.getInstallSourceInfo(packageName).installingPackageName
    }
    else {
        this.getInstallerPackageName(packageName)
    }
}