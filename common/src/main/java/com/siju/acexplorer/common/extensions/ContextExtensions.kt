package com.siju.acexplorer.common.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast

fun Context?.canHandleIntent(intent: Intent): Boolean {
    this ?: return false
    return this.packageManager.resolveActivity(intent, 0) != null
}

fun Context?.showToast(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.getAppInfo(packageName: String): ApplicationInfo? {
    return try {
        this.packageManager.getApplicationInfo(packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}