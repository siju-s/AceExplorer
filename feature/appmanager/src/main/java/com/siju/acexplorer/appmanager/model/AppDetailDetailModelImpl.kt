package com.siju.acexplorer.appmanager.model

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val BASE_FLAGS = PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNING_CERTIFICATES

private const val COMPONENT_FLAGS = PackageManager.GET_ACTIVITIES or
        PackageManager.GET_SERVICES or
        PackageManager.GET_RECEIVERS or
        PackageManager.GET_PROVIDERS

class AppDetailDetailModelImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val appDetailInfoMapper: AppDetailInfoMapper
) : AppDetailModel {

    private val _appInfo = MutableLiveData<AppDetailInfo>()

    val appDetailInfo: LiveData<AppDetailInfo>
        get() = _appInfo

    private val _permissionInfo = MutableLiveData<PermissionInfo>()

    val permissionInfo: LiveData<PermissionInfo>
        get() = _permissionInfo

    override fun fetchPackageInfo(packageName: String) {
        val packageInfo = loadPackageInfo(packageName) ?: return
        _appInfo.postValue(appDetailInfoMapper.map(packageInfo))
        _permissionInfo.postValue(createPermissionInfo(packageInfo))
    }

    /**
     * Component metadata is fetched on a best effort basis. Apps with thousands of components can
     * blow past the binder transaction limit, so the query is retried without those flags rather
     * than failing the whole screen.
     */
    @Suppress("DEPRECATION")
    private fun loadPackageInfo(packageName: String): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(packageName, BASE_FLAGS or COMPONENT_FLAGS)
        }
        catch (e: PackageManager.NameNotFoundException) {
            null
        }
        catch (e: RuntimeException) {
            loadPackageInfoWithoutComponents(packageName)
        }
    }

    @Suppress("DEPRECATION")
    private fun loadPackageInfoWithoutComponents(packageName: String): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(packageName, BASE_FLAGS)
        }
        catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun createPermissionInfo(packageInfo: PackageInfo): PermissionInfo {
        return PermissionInfo(packageInfo.requestedPermissions)
    }
}
