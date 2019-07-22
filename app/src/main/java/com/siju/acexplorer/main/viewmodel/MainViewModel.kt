package com.siju.acexplorer.main.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.siju.acexplorer.billing.repository.BillingRepository
import com.siju.acexplorer.billing.repository.localdb.AugmentedSkuDetails
import com.siju.acexplorer.billing.repository.localdb.Premium
import com.siju.acexplorer.main.model.MainModelImpl
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.theme.Theme

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    private val billingRepository = BillingRepository.getInstance(app)
    val premiumLiveData: LiveData<Premium>
    private val mainModel = MainModelImpl()
    private lateinit var permissionHelper: PermissionHelper
    lateinit var permissionStatus: LiveData<PermissionHelper.PermissionState>
    val theme: LiveData<Theme>
    private var storageList: ArrayList<StorageItem>? = null

    init {
        billingRepository.startDataSourceConnections()
        premiumLiveData = billingRepository.premiumLiveData
        theme = mainModel.theme
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }

    fun makePurchase(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) {
        billingRepository.launchBillingFlow(activity, augmentedSkuDetails)
    }

    fun setPermissionHelper(permissionHelper: PermissionHelper) {
        this.permissionHelper = permissionHelper
        permissionStatus = permissionHelper.permissionStatus
        permissionHelper.checkPermissions()
    }


    fun onPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionHelper.onPermissionResult(requestCode, permissions, grantResults)
    }

    fun onResume() {
        permissionHelper.onForeground()
    }

    fun requestPermissions() {
        permissionHelper.requestPermission()
    }

    fun showPermissionRationale() {
        permissionHelper.showRationale()
    }

    fun setStorageList(storageList: ArrayList<StorageItem>) {
        this.storageList = storageList
    }

    fun getExternalSdList() : ArrayList<String> {
        val extSdList = arrayListOf<String>()
        storageList?.let {
            for(storage in it) {
                if (storage.storageType == StorageUtils.StorageType.EXTERNAL) {
                     extSdList.add(storage.path)
                }
            }
        }
        return extSdList
    }

}