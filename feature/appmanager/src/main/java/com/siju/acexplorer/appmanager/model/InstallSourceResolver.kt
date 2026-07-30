package com.siju.acexplorer.appmanager.model

import android.content.Context
import android.content.pm.PackageManager
import com.siju.acexplorer.appmanager.store.AppStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Turns an installer package name into something a user recognises.
 *
 * Anything that is not one of the stores in [AppStore] still gets a real name by loading the
 * installing app's own label, so a browser or file manager sideload reads as "Chrome" rather than
 * collapsing into "Unknown source".
 */
class InstallSourceResolver @Inject constructor(@ApplicationContext private val context: Context) {

    fun resolveName(installerPackage: String?): String {
        installerPackage ?: return context.getString(com.siju.acexplorer.common.R.string.unknown)

        val store = AppStore.matching(installerPackage)
        if (store != null) {
            return context.getString(store.labelRes)
        }
        return installerAppLabel(installerPackage) ?: installerPackage
    }

    private fun installerAppLabel(installerPackage: String): String? {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(installerPackage, 0)
            applicationInfo.loadLabel(context.packageManager).toString()
        }
        catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
