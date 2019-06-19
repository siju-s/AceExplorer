package com.siju.acexplorer.main.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.siju.acexplorer.billing.repository.BillingRepository
import com.siju.acexplorer.billing.repository.localdb.AugmentedSkuDetails
import com.siju.acexplorer.billing.repository.localdb.Premium
import com.siju.acexplorer.main.model.MainModel
import com.siju.acexplorer.main.model.MainModelImpl
import com.siju.acexplorer.permission.PermissionHelper

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    private val billingRepository: BillingRepository
    val premiumLiveData: LiveData<Premium>
    private val mainModel: MainModel
    private lateinit var permissionHelper: PermissionHelper
    lateinit var permissionStatus : LiveData<PermissionHelper.PermissionState>

    init {
        mainModel = MainModelImpl()
        billingRepository = BillingRepository.getInstance(app)
        billingRepository.startDataSourceConnections()
        premiumLiveData = billingRepository.premiumLiveData
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
    }


    fun onPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionHelper.onPermissionResult(requestCode, permissions, grantResults)
    }

    fun onResume() {
        permissionHelper.checkPermissions()
    }

    fun requestPermissions() {
        permissionHelper.requestPermission()
    }

    fun showPermissionRationale() {
        permissionHelper.showRationale()
    }

}