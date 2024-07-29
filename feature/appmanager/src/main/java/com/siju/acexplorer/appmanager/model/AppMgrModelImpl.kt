package com.siju.acexplorer.appmanager.model

import android.annotation.SuppressLint
import android.content.Context
import com.siju.acexplorer.appmanager.extensions.getInstallerPackage
import com.siju.acexplorer.appmanager.filter.AppType
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.ViewModeData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.*
import javax.inject.Inject

class AppMgrModelImpl @Inject constructor(@ApplicationContext val context: Context, private val viewModeData: ViewModeData) : AppMgrModel {

    override fun getViewMode(): ViewMode {
        return viewModeData.getViewMode()
    }

    override fun saveViewMode(viewMode: ViewMode) {
        viewModeData.saveViewMode(viewMode)
    }

    override fun getSystemApps(): ArrayList<AppInfo> {
        return getAppPackageInfo(context, AppType.SYSTEM_APP)
    }

    override fun getAllApps(): ArrayList<AppInfo> {
        return getAppPackageInfo(context, AppType.ALL_APPS)
    }

    override fun getUserApps(): ArrayList<AppInfo> {
        return getAppPackageInfo(context, AppType.USER_APP)
    }

    // Querying applications allowed since it's a file manager app @see https://support.google.com/googleplay/android-developer/answer/10158779#zippy=%2Cpermitted-uses-of-the-query-all-packages-permission
    @SuppressLint("QueryPermissionsNeeded")
    private fun getAppPackageInfo(context: Context, appType: AppType): ArrayList<AppInfo> {
        val packages = context.packageManager.getInstalledPackages(0)
        val appsList = ArrayList<AppInfo>()

        for (packageInfo in packages) {
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo ?: continue
            val packageName = applicationInfo.packageName
            val isSystemApp = AppHelper.isSystemPackage(applicationInfo)

            if ((appType == AppType.SYSTEM_APP && !isSystemApp) || (appType == AppType.USER_APP && isSystemApp)) {
                continue
            }

            val source =  AppHelper.getInstallerSourceName(isSystemApp, context.packageManager.getInstallerPackage(packageName))
            val appDir = File(applicationInfo.publicSourceDir)
            val sourceDir = applicationInfo.sourceDir

            val size = appDir.length()
            val appName = applicationInfo.loadLabel(context.packageManager).toString()

            appsList.add(
                AppInfo(appName, packageName, appType, source, sourceDir, size, packageInfo.firstInstallTime, packageInfo.lastUpdateTime)
            )
        }
        return appsList
    }
}