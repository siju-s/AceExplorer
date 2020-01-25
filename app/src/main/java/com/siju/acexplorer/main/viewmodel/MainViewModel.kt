package com.siju.acexplorer.main.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.billing.repository.BillingRepository
import com.siju.acexplorer.billing.repository.localdb.AugmentedSkuDetails
import com.siju.acexplorer.billing.repository.localdb.Premium
import com.siju.acexplorer.main.model.MainModelImpl
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.theme.Theme

enum class Pane {
    SINGLE,
    DUAL
}
class MainViewModel(val app: Application) : AndroidViewModel(app) {

    var navigateToSearch = MutableLiveData<Boolean>()
    var isDualPaneInFocus = false
    private set

    private val billingRepository = BillingRepository.getInstance(app)
    val premiumLiveData: LiveData<Premium>
    private val mainModel = MainModelImpl()
    private lateinit var permissionHelper: PermissionHelper
    lateinit var permissionStatus: LiveData<PermissionHelper.PermissionState>
    val theme: LiveData<Theme>
    private var storageList: ArrayList<StorageItem>? = null
    val dualMode : LiveData<Boolean>
    private val _homeClicked = MutableLiveData<Boolean>()

    val homeClicked: LiveData<Boolean>
        get() = _homeClicked

    private val _storageScreenReady = MutableLiveData<Boolean>()
    val storageScreenReady : LiveData<Boolean>
    get() = _storageScreenReady

    private val _refreshGridColumns = MutableLiveData<Pair<Pane, Boolean>>()

    val refreshGridCols : LiveData<Pair<Pane, Boolean>>
    get() = _refreshGridColumns

    private val _reloadPane = MutableLiveData<Pair<Pane, Boolean>>()

    val reloadPane : LiveData<Pair<Pane, Boolean>>
    get() = _reloadPane

    init {
        billingRepository.startDataSourceConnections()
        premiumLiveData = billingRepository.premiumLiveData
        theme = mainModel.theme
        dualMode = mainModel.dualMode
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

    fun onHomeClicked() {
        _homeClicked.value = true
    }

    fun setHomeClickedFalse() {
        _homeClicked.value = false
    }

    fun setNavigatedToSearch() {
        navigateToSearch.value = false
    }

    fun setStorageReady() {
       _storageScreenReady.value = true
    }

    fun setStorageNotReady() {
        _storageScreenReady.value = false
    }

    fun refreshLayout(pane: Pane) {
        Log.e(this.javaClass.simpleName, "refreshLayout:$pane")
        _refreshGridColumns.value = Pair(pane, true)
    }

    fun setRefreshDone(pane: Pane) {
        _refreshGridColumns.value = Pair(pane, false)
    }

    fun setReloadPane(pane: Pane, reload : Boolean) {
        Log.e(this.javaClass.simpleName, "setReloadPane:$pane, reload:$reload")
        _reloadPane.value = Pair(pane, reload)
    }

    fun setPaneFocus(isDualPaneInFocus: Boolean) {
        this.isDualPaneInFocus = isDualPaneInFocus
    }

}