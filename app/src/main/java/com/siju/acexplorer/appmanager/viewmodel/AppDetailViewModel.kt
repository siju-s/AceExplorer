package com.siju.acexplorer.appmanager.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.appmanager.model.*

class AppDetailViewModel @ViewModelInject constructor(private val appDetailModel: AppDetailModel) : ViewModel() {

    val versionInfo: LiveData<AppVersionInfo>

    val appInfo: LiveData<AppInfo>

    val permissionInfo: LiveData<PermissionInfo>


    init {
        appDetailModel as AppDetailDetailModelImpl
        versionInfo = appDetailModel.versionInfo
        appInfo = appDetailModel.appInfo
        permissionInfo = appDetailModel.permissionInfo
    }

    fun fetchPackageInfo(packageName: String?) {
        packageName?.let { appDetailModel.fetchPackageInfo(it) }
    }
}