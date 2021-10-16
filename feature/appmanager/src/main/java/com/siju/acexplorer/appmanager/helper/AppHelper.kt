package com.siju.acexplorer.appmanager.helper


import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.extensions.getPackageInstaller
import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.appmanager.view.AppDetailActivity.Companion.REQUEST_CODE_UNINSTALL
import com.siju.acexplorer.common.utils.SdkHelper


private const val PACKAGE_NAME_PLAYSTORE = "com.android.vending"
private const val PACKAGE_NAME_AMAZON_APPSTORE = "com.amazon.venezia"
const val ACTION_UNINSTALL = "uninstall"

object AppHelper {

    const val SCHEME_PACKAGE = "package"
    private const val PREFIX_PACKAGE_URI = "package:"

    @Suppress("Deprecation")
    fun uninstallApp(activity: AppCompatActivity?, packageName: String?, launcher: ActivityResultLauncher<Intent>) {
        packageName ?: return
        activity ?: return
        if (SdkHelper.isAtleastAndroid10) {
            val intent = Intent(activity, activity.javaClass)
            intent.action = ACTION_UNINSTALL
            val intentSender = PendingIntent.getActivity(activity, REQUEST_CODE_UNINSTALL, intent, 0).intentSender
            activity.applicationContext.getPackageInstaller().uninstall(packageName, intentSender)
        }
        else {
            val packageUri = Uri.parse(PREFIX_PACKAGE_URI + packageName)
            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
            uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
            launcher.launch(uninstallIntent)
        }
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

    fun getInstallerSourceName(isSystemApp : Boolean, packageName: String?): AppSource {
        if (isSystemApp) {
            return AppSource.SYSTEM
        }
        return when (packageName) {
            PACKAGE_NAME_PLAYSTORE -> AppSource.PLAYSTORE
            PACKAGE_NAME_AMAZON_APPSTORE -> AppSource.AMAZON_APPSTORE
            else -> AppSource.UNKNOWN
        }
    }

    fun getUninstallAction() = ACTION_UNINSTALL

}
