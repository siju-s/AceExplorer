package com.siju.acexplorer.appmanager.extensions

import android.content.Context

fun Context.getPackageInstaller() = this.packageManager.packageInstaller