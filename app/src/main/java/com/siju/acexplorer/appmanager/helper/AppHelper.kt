package com.siju.acexplorer.appmanager.helper


import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

import androidx.appcompat.app.AppCompatActivity

import com.siju.acexplorer.appmanager.view.AppDetailActivity.Companion.REQUEST_CODE_UNINSTALL


object AppHelper {

    const val SCHEME_PACKAGE = "package"
    private const val PREFIX_PACKAGE_URI = "package:"

    fun uninstallApp(activity: AppCompatActivity, packageName: String) {
        val packageUri = Uri.parse(PREFIX_PACKAGE_URI + packageName)
        val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
        uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        activity.startActivityForResult(uninstallIntent, REQUEST_CODE_UNINSTALL)
    }

    fun openAppSettings(context: Context, packageName: String) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(SCHEME_PACKAGE, packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun isPackageNotExisting(context: Context, packageName: String?): Boolean {
        if (packageName == null) {
            return true
        }
        val pm = context.packageManager
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            return false
        }
        catch (e: PackageManager.NameNotFoundException) {
            return true
        }
    }

    fun isSystemPackage(applicationInfo: ApplicationInfo): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

}
