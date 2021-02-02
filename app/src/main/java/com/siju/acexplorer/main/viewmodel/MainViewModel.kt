package com.siju.acexplorer.main.viewmodel

import android.app.Activity
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.billing.repository.BillingRepository
import com.siju.acexplorer.billing.repository.localdb.Premium
import com.siju.acexplorer.home.view.CategoryMenuHelper
import com.siju.acexplorer.home.view.MenuItemWrapper
import com.siju.acexplorer.main.helper.SingleLiveEvent
import com.siju.acexplorer.main.model.MainModelImpl
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.preferences.PreferenceConstants
import com.siju.acexplorer.storage.helper.Event
import com.siju.acexplorer.storage.model.SortMode
import com.siju.acexplorer.theme.Theme

enum class Pane {
    SINGLE,
    DUAL
}
class MainViewModel @ViewModelInject constructor(private val permissionHelper: PermissionHelper) : ViewModel() {

    private var categoryMenuHelper: CategoryMenuHelper? = null
    val navigateToSearch = MutableLiveData<Event<Boolean>>()
    var isDualPaneInFocus = false
    private set

    private val billingRepository = BillingRepository.getInstance(AceApplication.appContext)
    val premiumLiveData: LiveData<Premium>
    private val mainModel = MainModelImpl()
    lateinit var permissionStatus: LiveData<PermissionHelper.PermissionState>
    val theme: LiveData<Theme>
    private var storageList: ArrayList<StorageItem>? = null
    val dualMode : LiveData<Boolean>
    val sortMode: LiveData<Int>
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

    private val menuItemClicked = MutableLiveData<MenuItemWrapper>()

    val onMenuItemClicked : LiveData<MenuItemWrapper>
    get() = menuItemClicked

    val refreshData = MutableLiveData<Boolean>()

    private var filePicker = false
    private var pickerMultipleSelection = false

    private val _navigateToRecent = SingleLiveEvent<Boolean>()

    val navigateToRecent : SingleLiveEvent<Boolean>
    get() = _navigateToRecent

    private val _sortEvent = MutableLiveData<Event<SortMode>>()
    val sortEvent: LiveData<Event<SortMode>>
        get() = _sortEvent

    init {
        Log.d("MainViewModel", "init")
        billingRepository.startDataSourceConnections()
        premiumLiveData = billingRepository.premiumLiveData
        theme = mainModel.theme
        dualMode = mainModel.dualMode
        sortMode = mainModel.sortMode
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }

    fun buyPremiumVersion(activity: Activity) {
        billingRepository.purchaseFullVersion(activity)
    }

    fun checkPermissions() {
        permissionStatus = permissionHelper.permissionStatus
        permissionHelper.checkPermissions()
    }

    fun isPremiumVersion() = premiumLiveData.value?.entitled == true

    fun isFreeVersion() = premiumLiveData.value?.entitled == false

    fun isDualPaneEnabled() = dualMode.value == true

    fun getSortMode() : Int {
        return sortMode.value ?: PreferenceConstants.DEFAULT_VALUE_SORT_MODE
    }

    fun onPermissionResult() {
        permissionHelper.onPermissionResult()
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

    fun setStorageReady() {
       _storageScreenReady.value = true
    }

    fun setStorageNotReady() {
        _storageScreenReady.value = false
    }

    fun refreshLayout(pane: Pane) {
        Log.d(this.javaClass.simpleName, "refreshLayout:$pane")
        _refreshGridColumns.value = Pair(pane, true)
    }

    fun setRefreshDone(pane: Pane) {
        _refreshGridColumns.value = Pair(pane, false)
    }

    fun setReloadPane(pane: Pane, reload : Boolean) {
        Log.d(this.javaClass.simpleName, "setReloadPane:$pane, reload:$reload")
        _reloadPane.value = Pair(pane, reload)
    }

    fun setPaneFocus(isDualPaneInFocus: Boolean) {
        this.isDualPaneInFocus = isDualPaneInFocus
    }

    fun userCancelledUpdate() {
        mainModel.saveUserCancelledUpdate()
    }

    fun onUpdateInstalled() {
        mainModel.onUpdateComplete()
    }

    fun hasUserCancelledUpdate() = mainModel.hasUserCancelledUpdate()

    fun setCategoryMenuHelper(categoryMenuHelper: CategoryMenuHelper?) {
        this.categoryMenuHelper = categoryMenuHelper
    }

    fun getCategoryMenuHelper(): CategoryMenuHelper? {
        return categoryMenuHelper
    }

    fun onMenuItemClicked(wrapper: MenuItemWrapper) {
        menuItemClicked.value = wrapper
    }

    fun refreshData() {
        refreshData.value = true
    }

    fun onFilePicker(multiSelection : Boolean) {
       filePicker = true
        pickerMultipleSelection = multiSelection
        _navigateToRecent.value = true
    }

    fun isFilePicker() = filePicker

    fun isPickerMultiSelection() = pickerMultipleSelection

    fun onSortClicked() {
        val value = sortMode.value
        value?.let {
            _sortEvent.value = Event(SortMode.getSortModeFromValue(value))
        }
    }

    fun navigateToSearch() {
        navigateToSearch.value = Event(true)
    }

}