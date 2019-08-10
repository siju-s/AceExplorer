package com.siju.acexplorer.appmanager.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SdkHelper

private const val PACKAGE_NAME_PLAYSTORE = "com.android.vending"
private const val PACKAGE_NAME_AMAZON_APPSTORE = "com.amazon.venezia"

class AppDetailDetailModelImpl(val context: Context) : AppDetailModel {

    private val _versionInfo = MutableLiveData<AppVersionInfo>()

    val versionInfo: LiveData<AppVersionInfo>
        get() = _versionInfo

    private val _appInfo = MutableLiveData<AppInfo>()

    val appInfo: LiveData<AppInfo>
        get() = _appInfo

    private val _permissionInfo = MutableLiveData<PermissionInfo>()

    val permissionInfo: LiveData<PermissionInfo>
        get() = _permissionInfo


    override fun fetchPackageInfo(packageName: String) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName,
                                                                    PackageManager.GET_PERMISSIONS)
            _versionInfo.postValue(createVersionInfo(packageInfo))
            _appInfo.postValue(createAppInfo(packageInfo))
            _permissionInfo.postValue(createPermissionInfo(packageInfo))

        }
        catch (e: PackageManager.NameNotFoundException) {

        }
    }

    private fun createVersionInfo(packageInfo: PackageInfo): AppVersionInfo {
        val versionCode = if (SdkHelper.isAtleastPie) {
            packageInfo.longVersionCode.toInt()
        }
        else {
            packageInfo.versionCode
        }
        val versionName = packageInfo.versionName

        return AppVersionInfo(versionName, versionCode)
    }

    private fun createAppInfo(packageInfo: PackageInfo): AppInfo {
        val packageName = packageInfo.packageName
        val applicationInfo = packageInfo.applicationInfo
        val appName = applicationInfo.loadLabel(context.packageManager)
        return AppInfo(packageName, appName.toString(), getInstallerSource(packageName),
                       applicationInfo.enabled,
                       getMinSdkVersion(applicationInfo), applicationInfo.targetSdkVersion,
                       FileUtils.convertDate(packageInfo.firstInstallTime),
                       FileUtils.convertDate(packageInfo.lastUpdateTime))
    }

    private fun createPermissionInfo(packageInfo: PackageInfo): PermissionInfo {
        return PermissionInfo(packageInfo.requestedPermissions)
    }

    private fun getMinSdkVersion(applicationInfo: ApplicationInfo): Int {
        return if (SdkHelper.isAtleastNougat) {
            applicationInfo.minSdkVersion
        }
        else {
            0
        }
    }

    private fun getInstallerSource(packageName: String): String {
        return getInstallerSourceName(context.packageManager.getInstallerPackageName(packageName))
    }

    private fun getInstallerSourceName(packageName: String?): String {
        return when {
            packageName == null -> context.getString(R.string.unknown)
            PACKAGE_NAME_PLAYSTORE == packageName -> context.getString(R.string.play_store)
            PACKAGE_NAME_AMAZON_APPSTORE == packageName -> context.getString(
                    R.string.amazon_play_store)
            else -> context.getString(R.string.unknown)
        }
    }
}
