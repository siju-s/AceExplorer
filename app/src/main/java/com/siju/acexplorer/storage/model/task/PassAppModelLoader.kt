package com.siju.acexplorer.storage.model.task

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.siju.acexplorer.AceApplication.Companion.appContext
import com.siju.acexplorer.main.model.helper.AppUtils.getAppInfo

internal class PassAppModelLoader : ModelLoader<String, ApplicationInfo> {
    override fun buildLoadData(path: String, width: Int,
                               height: Int, options: Options): LoadData<ApplicationInfo> {
        return LoadData<ApplicationInfo>(CustomKey(path), CastingDataFetcher(path))
    }

    override fun handles(s: String): Boolean {
        return true
    }

    class PassAppModelLoaderFactory : ModelLoaderFactory<String, ApplicationInfo> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, ApplicationInfo> {
            return PassAppModelLoader()
        }

        override fun teardown() {
        }
    }

    /**
     * Extremely unsafe, use with care.
     */
    private class CastingDataFetcher(private val path: String) : DataFetcher<ApplicationInfo?> {
        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ApplicationInfo?>) {
            var applicationInfo: ApplicationInfo? = null
            if (path.contains("/")) {
                applicationInfo = getAppInfo(appContext,
                        path)
            } else {
                try { // For App manager, package name to be passed
                    applicationInfo = appContext.packageManager.getApplicationInfo(path, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            callback.onDataReady(applicationInfo)
        }

        override fun cleanup() {}
        override fun cancel() {}
        @Suppress("UNCHECKED_CAST")
        override fun getDataClass(): Class<ApplicationInfo?> {
            return ApplicationInfo::class.java as Class<ApplicationInfo?>
        }

        override fun getDataSource(): DataSource {
            return DataSource.LOCAL
        }

    }
}