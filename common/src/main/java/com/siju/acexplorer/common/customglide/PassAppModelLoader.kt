package com.siju.acexplorer.common.customglide

import android.content.Context
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
import com.siju.acexplorer.common.utils.AppUtils.getAppInfo

internal class PassAppModelLoader(private val context: Context) : ModelLoader<String, ApplicationInfo> {
    override fun buildLoadData(
        path: String, width: Int,
        height: Int, options: Options
    ): LoadData<ApplicationInfo> {
        return LoadData<ApplicationInfo>(CustomKey(path), CastingDataFetcher(context, path))
    }

    override fun handles(s: String): Boolean {
        return true
    }

    class PassAppModelLoaderFactory(val context: Context) : ModelLoaderFactory<String, ApplicationInfo> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, ApplicationInfo> {
            return PassAppModelLoader(context)
        }

        override fun teardown() {
        }
    }

    /**
     * Extremely unsafe, use with care.
     */
    private class CastingDataFetcher(val context: Context, private val path: String) : DataFetcher<ApplicationInfo?> {

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ApplicationInfo?>) {
            var applicationInfo: ApplicationInfo? = null
            if (path.contains("/")) {
                applicationInfo = getAppInfo(
                    context,
                    path
                )
            }
            else {
                try { // For App manager, package name to be passed
                    applicationInfo = context.packageManager.getApplicationInfo(path, 0)
                }
                catch (e: PackageManager.NameNotFoundException) {
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