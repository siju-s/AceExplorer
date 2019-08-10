package com.siju.acexplorer.main.model.data

import android.content.Context
import android.content.pm.PackageInfo
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SortHelper
import java.io.File
import java.util.*

class AppManagerDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        val data = getInstalledUserApps(context)
        SortHelper.sortAppManager(data, getSortMode(context))
        return data
    }

    override fun fetchCount(context: Context, path: String?) = 0

    private fun getInstalledUserApps(context: Context): ArrayList<FileInfo> {
        val packages = context.packageManager.getInstalledPackages(0)
        return getAppPackageInfo(context, packages)
    }

    private fun getAppPackageInfo(context: Context, packages: List<PackageInfo>): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()

        for (packageInfo in packages) {
            val applicationInfo = packageInfo.applicationInfo

            if (AppHelper.isSystemPackage(applicationInfo)) {
                continue
            }
            val appDir = File(applicationInfo.publicSourceDir)
            val size = appDir.length()
            val lastUpdateTime = packageInfo.lastUpdateTime
            val appName = applicationInfo.loadLabel(context.packageManager).toString()
            val packageName = applicationInfo.packageName
            fileInfoList.add(FileInfo.createAppInfo(Category.APP_MANAGER, appName, packageName,
                                                    lastUpdateTime, size))
        }
        return fileInfoList
    }
}