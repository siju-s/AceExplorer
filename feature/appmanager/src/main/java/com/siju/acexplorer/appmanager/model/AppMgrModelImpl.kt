package com.siju.acexplorer.appmanager.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

private const val ACTION_UNINSTALL = "action_uninstall"
class AppMgrModelImpl @Inject constructor(@ApplicationContext val context: Context, val viewModeData: ViewModeData) : AppMgrModel {

   private val receiver = AppUninstallReceiver()

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

    private fun getAppPackageInfo(context: Context, appType: AppType): ArrayList<AppInfo> {
        val packages = context.packageManager.getInstalledPackages(0)
        val appsList = ArrayList<AppInfo>()

        for (packageInfo in packages) {
            val applicationInfo = packageInfo.applicationInfo
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

    override fun registerAppUninstallReceiver() {
        val filter = IntentFilter(ACTION_UNINSTALL)
        context.registerReceiver(receiver, filter)
    }

    override fun unregisterAppUninstallReceiver() {
        context.unregisterReceiver(receiver)
    }

    class AppUninstallReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == ACTION_UNINSTALL) {

            }
        }
    }
}