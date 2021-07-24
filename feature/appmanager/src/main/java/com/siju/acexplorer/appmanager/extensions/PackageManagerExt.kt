package com.siju.acexplorer.appmanager.extensions

import android.content.pm.PackageManager
import com.siju.acexplorer.common.utils.SdkHelper

@Suppress("deprecation")
fun PackageManager.getInstallerPackage(packageName : String) : String? {
    return if (SdkHelper.isAtleastAndroid11) {
        this.getInstallSourceInfo(packageName).initiatingPackageName
    }
    else {
        this.getInstallerPackageName(packageName)
    }
}