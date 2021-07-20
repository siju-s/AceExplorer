package com.siju.acexplorer.appmanager.helper


import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.R
import com.siju.acexplorer.appmanager.filter.AppSource

import com.siju.acexplorer.appmanager.view.AppDetailActivity.Companion.REQUEST_CODE_UNINSTALL


private const val PACKAGE_NAME_PLAYSTORE = "com.android.vending"
private const val PACKAGE_NAME_AMAZON_APPSTORE = "com.amazon.venezia"
object AppHelper {

    const val SCHEME_PACKAGE = "package"
    private const val PREFIX_PACKAGE_URI = "package:"

    fun uninstallApp(activity: AppCompatActivity, packageName: String?) {
        packageName ?: return
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
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            false
        }
        catch (e: PackageManager.NameNotFoundException) {
            true
        }
    }

    fun isSystemPackage(applicationInfo: ApplicationInfo): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    fun getInstallerSourceName(context: Context, packageName: String?): String {
        return when (packageName) {
            null -> context.getString(R.string.unknown)
            PACKAGE_NAME_PLAYSTORE -> context.getString(R.string.play_store)
            PACKAGE_NAME_AMAZON_APPSTORE -> context.getString(
                R.string.amazon_play_store)
            else -> context.getString(R.string.unknown)
        }
    }

    fun getInstallerSourceName(packageName: String?): AppSource {
        return when (packageName) {
            null -> AppSource.UNKNOWN
            PACKAGE_NAME_PLAYSTORE -> AppSource.PLAYSTORE
            PACKAGE_NAME_AMAZON_APPSTORE -> AppSource.AMAZON_APPSTORE
            else -> AppSource.UNKNOWN
        }
    }

}
