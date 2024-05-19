package com.siju.acexplorer.appmanager.viewmodel

import androidx.core.util.keyIterator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.appmanager.filter.AppType
import com.siju.acexplorer.appmanager.helper.SortHelper
import com.siju.acexplorer.appmanager.model.AppMgrModel
import com.siju.acexplorer.appmanager.selection.MultiSelection
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.common.ActionModeState
import com.siju.acexplorer.common.SortMode
import com.siju.acexplorer.common.SortModeData
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppMgrViewModel @Inject constructor(private val model: AppMgrModel,
                                          private val sortModeData: SortModeData,
                                          private val multiSelection: MultiSelection) : ViewModel(),
    AppMgr,
    MultiSelection.Listener {

    private val _appsList =  MutableLiveData<ArrayList<AppInfo>>()

    val appsList : LiveData<ArrayList<AppInfo>>
    get() = _appsList

    private val _appsSourceFilteredList =  MutableLiveData<ArrayList<AppInfo>>()

    val appsSourceFilteredList : LiveData<ArrayList<AppInfo>>
        get() = _appsSourceFilteredList

    val updateList = MutableLiveData<Event<Boolean>>()

    val selectedItemCount = multiSelection.selectedItemCount()

    private val _actionModeState = MutableLiveData<ActionModeState>()

    val actionModeState: LiveData<ActionModeState>
        get() = _actionModeState

    private val _selectedItemChanged = MutableLiveData<Int>()

    val selectedItemChanged : LiveData<Int>
    get() = _selectedItemChanged

    private val _multiOperationData = MutableLiveData<ArrayList<AppInfo>>()

    val multiOperationData : LiveData<ArrayList<AppInfo>>
    get() = _multiOperationData

    private val _navigateToAppDetail = MutableLiveData<Event<Pair<AppInfo, Boolean>>>()

    val navigateToAppDetail : LiveData<Event<Pair<AppInfo, Boolean>>>
    get() = _navigateToAppDetail

    private val _refreshList = MutableLiveData<Event<Boolean>>()

    val refreshList : LiveData<Event<Boolean>>
    get() = _refreshList

    private val _closeSearch = MutableLiveData<Event<Boolean>>()

    val closeSearch : LiveData<Event<Boolean>>
    get() = _closeSearch

    val backPressed = MutableLiveData<Event<Boolean>>()

    private var appType = AppType.USER_APP
    private var searchActive = false

    private val _viewMode = MutableLiveData(model.getViewMode())
    val viewMode : LiveData<ViewMode> = _viewMode

    init {
        multiSelection.setListener(this)
    }

    fun fetchPackagesCurrentType() {
        fetchPackages(appType)
    }

    fun fetchPackages(appType: AppType) {
        println("fetchPackages apptype:$appType")
        this.appType = appType
        viewModelScope.launch(Dispatchers.IO) {
            val result = when(appType) {
                AppType.ALL_APPS -> model.getAllApps()
                AppType.USER_APP -> model.getUserApps()
                AppType.SYSTEM_APP -> model.getSystemApps()
            }
            _appsList.postValue(SortHelper.sort(result, getSortMode()))
        }
    }

    fun filterAppBySource(appSource: AppSource) {
        println("filterAppBySource $appSource")
        val list = appsList.value?.filter {
            when (appSource) {
                AppSource.ALL             -> it.source == AppSource.PLAYSTORE ||
                        it.source == AppSource.AMAZON_APPSTORE ||
                        it.source == AppSource.UNKNOWN

                AppSource.PLAYSTORE       -> it.source == AppSource.PLAYSTORE
                AppSource.AMAZON_APPSTORE -> it.source == AppSource.AMAZON_APPSTORE
                AppSource.UNKNOWN         -> it.source == AppSource.UNKNOWN
                AppSource.SYSTEM          -> it.source == AppSource.SYSTEM
            }
        }
        list ?: return
        _appsSourceFilteredList.postValue(list as ArrayList<AppInfo>)
    }

    fun getViewMode() = model.getViewMode()

    fun saveViewMode(viewMode: ViewMode) = model.saveViewMode(viewMode)

    fun onSortClicked(sortMode: SortMode) {
        val list = _appsList.value
        viewModelScope.launch(Dispatchers.Default) {
            list?.let {
                _appsList.postValue(SortHelper.sort(it, sortMode))
                updateList.postValue(Event(true))
            }
        }
        sortModeData.saveSortMode(sortMode)
    }

    fun getSortMode() = sortModeData.getSortMode()

    fun onItemLongClicked(pos: Int) {
        if (!isActionModeActive()) {
            _actionModeState.postValue(ActionModeState.STARTED)
        }
        multiSelection.toggleSelection(pos)
    }

    fun onItemClicked(appInfo: AppInfo, pos: Int) {
        if (isActionModeActive()) {
            multiSelection.toggleSelection(pos)
        }
        else {
            _navigateToAppDetail.postValue(Event(Pair(appInfo, true)))
        }
    }

    override fun onSelectionChanged(position: Int) {
       _selectedItemChanged.postValue(position)
    }

    override fun onNoItemsChecked() {
        _actionModeState.postValue(ActionModeState.ENDED)
    }

    override fun onAllItemsSelected() {
        _refreshList.postValue(Event(true))
    }

    fun isSelected(pos: Int) = multiSelection.isSelected(pos)


    override fun isActionModeActive() = actionModeState.value == ActionModeState.STARTED

    fun onSelectAllClicked() {
        val list = _appsList.value
        list?.let {
            if (multiSelection.getSelectedItemCount() < list.size) {
                selectAllItems(list)
            }
            else {
                clearSelection()
            }
        }
    }

    private fun selectAllItems(list: ArrayList<AppInfo>) {
        multiSelection.selectAll(list.size)
    }

    private fun clearSelection() {
        multiSelection.clearSelection()
    }

    fun onDeleteClicked() {
        val items = multiSelection.getSelectedItems()
        val appsToDelete = arrayListOf<AppInfo>()
        for (value in items.keyIterator())  {
            _appsList.value?.get(value)?.let { appsToDelete.add(it) }
        }
        _multiOperationData.postValue(appsToDelete)
        _actionModeState.postValue(ActionModeState.ENDED)
        multiSelection.clearSelection()
    }

    fun handleBackPress() {
        if (isActionModeActive()) {
            multiSelection.clearSelection()
            _actionModeState.postValue(ActionModeState.ENDED)
        }
        else if (searchActive) {
            _closeSearch.postValue(Event(true))
        }
        else {
            backPressed.postValue(Event(true))
        }
    }

    fun onSearchActive() {
        this.searchActive = true
    }

    fun onSearchInactive() {
        this.searchActive = false
    }

    fun setSelectedViewMode(viewMode: ViewMode) {
        _viewMode.value = viewMode
        saveViewMode(viewMode)
    }
}