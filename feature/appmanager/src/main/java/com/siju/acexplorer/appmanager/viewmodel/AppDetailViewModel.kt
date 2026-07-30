package com.siju.acexplorer.appmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siju.acexplorer.appmanager.model.AppDetailDetailModelImpl
import com.siju.acexplorer.appmanager.model.AppDetailInfo
import com.siju.acexplorer.appmanager.model.AppDetailModel
import com.siju.acexplorer.appmanager.model.PermissionInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(private val appDetailModel: AppDetailModel) : ViewModel() {

    val appDetailInfo: LiveData<AppDetailInfo>

    val permissionInfo: LiveData<PermissionInfo>

    init {
        appDetailModel as AppDetailDetailModelImpl
        appDetailInfo = appDetailModel.appDetailInfo
        permissionInfo = appDetailModel.permissionInfo
    }

    /**
     * Reading package metadata and measuring APK files both hit the file system, so the fetch is
     * kept off the main thread.
     */
    fun fetchPackageInfo(packageName: String?) {
        packageName ?: return
        viewModelScope.launch(Dispatchers.IO) {
            appDetailModel.fetchPackageInfo(packageName)
        }
    }
}
