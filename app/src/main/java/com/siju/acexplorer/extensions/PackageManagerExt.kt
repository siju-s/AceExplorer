package com.siju.acexplorer.extensions

import android.content.pm.PackageManager
import com.siju.acexplorer.main.model.helper.SdkHelper

fun PackageManager.getInstallerPackage(packageName : String) : String? {
    return if (SdkHelper.isAtleastAndroid11) {
        this.getInstallSourceInfo(packageName).initiatingPackageName
    }
    else {
        this.getInstallerPackageName(packageName)
    }
}