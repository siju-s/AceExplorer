package com.siju.acexplorer.appmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.appmanager.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(private val appDetailModel: AppDetailModel) : ViewModel() {

    val versionInfo: LiveData<AppVersionInfo>

    val appDetailInfo: LiveData<AppDetailInfo>

    val permissionInfo: LiveData<PermissionInfo>


    init {
        appDetailModel as AppDetailDetailModelImpl
        versionInfo = appDetailModel.versionInfo
        appDetailInfo = appDetailModel.appDetailInfo
        permissionInfo = appDetailModel.permissionInfo
    }

    fun fetchPackageInfo(packageName: String?) {
        packageName?.let { appDetailModel.fetchPackageInfo(it) }
    }
}