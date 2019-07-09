package com.siju.acexplorer.storage.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.Category.*
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.model.SortMode
import com.siju.acexplorer.storage.model.StorageModel
import com.siju.acexplorer.storage.model.StorageModelImpl
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.model.backstack.BackStackInfo
import com.siju.acexplorer.storage.model.operations.OperationAction
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.view.*
import com.siju.acexplorer.utils.InstallHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "FileListViewModel"

class FileListViewModel(private val storageModel: StorageModel) : ViewModel() {
    var apkPath: String? = null
    private lateinit var navigationView: NavigationView
    private lateinit var category: Category
    private val navigation = Navigation(this)
    private var bucketName: String? = null
    private val backStackInfo = BackStackInfo()

    private val _viewFileEvent = MutableLiveData<Pair<String, String?>>()
    private val _sortEvent = MutableLiveData<SortMode>()

    val viewFileEvent: LiveData<Pair<String, String?>>
        get() = _viewFileEvent

    private val _fileData = MutableLiveData<ArrayList<FileInfo>>()

    val fileData: LiveData<ArrayList<FileInfo>>
        get() = _fileData

    private val _singleOpData = MutableLiveData<Pair<Operations, FileInfo>>()

    val singleOpData: LiveData<Pair<Operations, FileInfo>>
        get() = _singleOpData

    val showFab = MutableLiveData<Boolean>()

    val sortEvent: LiveData<SortMode>
        get() = _sortEvent

    private val _viewMode = MutableLiveData<ViewMode>()

    val viewMode: LiveData<ViewMode>
        get() = _viewMode

    private val _installAppEvent = MutableLiveData<Pair<Boolean, String?>>()

    val installAppEvent: LiveData<Pair<Boolean, String?>>
        get() = _installAppEvent

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _actionModeState = MutableLiveData<ActionModeState>()

    lateinit var multiSelectionHelper: MultiSelectionHelper

    val actionModeState: LiveData<ActionModeState>
        get() = _actionModeState

    private val _selectedCount = MutableLiveData<Int>()

    val selectedCount: LiveData<Int>
        get() = _selectedCount

    private val _refreshEvent = MutableLiveData<Boolean>()

    val refreshEvent: LiveData<Boolean>
        get() = _refreshEvent

    val operationData: LiveData<Pair<Operations, OperationAction>>

    init {
        val model = storageModel as StorageModelImpl
        operationData = model.operationData
    }

    fun loadData(path: String?, category: Category) {
        Log.e(this.javaClass.name, "loadData: path $path , category $category")
        addNavigation(path, category)
        addToBackStack(path, category)
        setCategory(category)
        uiScope.launch(Dispatchers.IO) {
            _fileData.postValue(storageModel.loadData(path, category))
        }
    }

    private fun reloadData(path: String?, category: Category) {
        Log.e(this.javaClass.name, "reloadData: path $path , category $category")
        addNavigation(path, category)
        setCategory(category)
        uiScope.launch(Dispatchers.IO) {
            _fileData.postValue(storageModel.loadData(path, category))
        }
    }

    fun getViewMode() = storageModel.getViewMode()

    private fun setCategory(category: Category) {
        this.category = category
        showFab.postValue(canShowFab(category))
    }

    private fun canShowFab(category: Category) =
            !CategoryHelper.checkIfLibraryCategory(category)

    fun addHomeButton() {
        navigationView.addHomeButton()
    }

    fun addGenericTitle(category: Category) {
        navigationView.addGenericTitle(category)
    }

    fun addLibraryTitle(category: Category) {
        navigationView.addLibraryTitle(category)
    }

    fun createNavButtonStorage(storageType: StorageUtils.StorageType, dir: String) {
        when (storageType) {
            StorageUtils.StorageType.ROOT     -> navigationView.createRootStorageButton(dir)
            StorageUtils.StorageType.INTERNAL -> navigationView.createInternalStorageButton(dir)
            StorageUtils.StorageType.EXTERNAL -> navigationView.createExternalStorageButton(dir)
        }
    }

    fun createNavButtonStorageParts(path: String, dirName: String) {
        navigationView.createNavButtonStorageParts(path, dirName)
    }

    fun setNavigationView(navigationView: NavigationView) {
        this.navigationView = navigationView
    }

    fun setInitialDir(path: String?, category: Category) {
        navigation.setInitialDir(path, category)
    }

    fun setNavDirectory(path: String?, category: Category) {
        navigation.setNavDirectory(path, category)
    }

    fun createNavigationForCategory(category: Category) {
        navigation.createNavigationForCategory(category)
    }

    fun createLibraryTitleNavigation(category: Category, bucketName: String?) {
        navigationView.createLibraryTitleNavigation(category, bucketName)
    }

    fun setupNavigation(path: String?, category: Category) {
        setInitialDir(path, category)
        setNavDirectory(path, category)
        createNavigationForCategory(category)
    }

    fun onHiddenFileSettingChanged(value: Boolean) {
        storageModel.saveHiddenFileSetting(value)
        val backStack = backStackInfo.getCurrentBackStack()
        backStack?.let {
            reloadData(backStack.first, backStack.second)
        }
    }

    fun shouldShowHiddenFiles() = storageModel.shouldShowHiddenFiles()


    fun handleItemClick(fileInfo: FileInfo, position: Int) {
        if (isActionModeActive()) {
            multiSelectionHelper.toggleSelection(position)
            handleActionModeClick()
            return
        }
        when (category) {
            AUDIO, VIDEO, IMAGE, DOCS, PODCASTS, ALBUM_DETAIL, ARTIST_DETAIL, GENRE_DETAIL, FOLDER_IMAGES,
            FOLDER_VIDEOS, ALL_TRACKS, RECENT_AUDIO, RECENT_DOCS, RECENT_IMAGES, RECENT_VIDEOS -> {
                onFileClicked(fileInfo)
            }
            FILES, DOWNLOADS, COMPRESSED, FAVORITES, PDF, APPS, LARGE_FILES, RECENT_APPS       -> {
                onFileItemClicked(fileInfo)
            }

            GENERIC_MUSIC                                                                      -> {
                loadData(null, fileInfo.subcategory)
            }

            ALBUMS                                                                             -> {
                bucketName = fileInfo.title
                loadData(fileInfo.id.toString(), ALBUM_DETAIL)
            }

            ARTISTS                                                                            -> {
                bucketName = fileInfo.title
                loadData(fileInfo.id.toString(), ARTIST_DETAIL)
            }

            GENRES                                                                             -> {
                bucketName = fileInfo.title
                loadData(fileInfo.id.toString(), GENRE_DETAIL)
            }

            GENERIC_IMAGES                                                                     -> {
                bucketName = fileInfo.fileName
                loadData(fileInfo.bucketId.toString(), FOLDER_IMAGES)
            }

            GENERIC_VIDEOS                                                                     -> {
                bucketName = fileInfo.fileName
                loadData(fileInfo.bucketId.toString(), FOLDER_VIDEOS)
            }

            RECENT                                                                             -> {
                loadData(null, fileInfo.category)
            }

            APP_MANAGER                                                                        -> {

            }
            else                                                                               -> {
            }
        }
    }

    private fun isActionModeActive() = _actionModeState.value == ActionModeState.STARTED

    private fun onFileItemClicked(fileInfo: FileInfo) {
        if (fileInfo.isDirectory) {
            onDirectoryClicked(fileInfo)
        }
        else {
            onFileClicked(fileInfo)
        }
    }

    private fun onFileClicked(fileInfo: FileInfo) {
        _viewFileEvent.postValue(Pair(fileInfo.filePath, fileInfo.extension))
    }

    fun handleLongClick(fileInfo: FileInfo, position: Int) {
        Log.e(TAG, "handleLongClick:position $position")
        multiSelectionHelper.toggleSelection(position, true)
        handleActionModeClick()
    }

    private fun handleActionModeClick() {
        val hasCheckedItems = multiSelectionHelper.hasSelectedItems()
        Log.e(TAG, "handleActionModeClick state:${_actionModeState.value}")
        if (hasCheckedItems && !isActionModeActive()) {
            _actionModeState.value = ActionModeState.STARTED
        }
        else if (!hasCheckedItems && isActionModeActive()) {
            endActionMode()
        }
        if (isActionModeActive()) {
            _selectedCount.value = multiSelectionHelper.getSelectedCount()
        }
    }

    private fun addNavigation(path: String?, category: Category) {
        setupNavigation(path, category)
        navigation.addLibSpecificNavigation(category, bucketName)
    }

    private fun addToBackStack(path: String?, category: Category) {
        backStackInfo.addToBackStack(path, category)
    }

    private fun hasBackStack() = backStackInfo.hasBackStack()

    fun onBackPress(): Boolean {
        if (hasBackStack()) {
            backStackInfo.remove()
            refreshList()
            return false
        }
        return true
    }

    private fun refreshList() {
        val backStack = backStackInfo.getCurrentBackStack()
        backStack?.let {
            reloadData(backStack.first, backStack.second)
        }
    }

    private fun onDirectoryClicked(fileInfo: FileInfo) {
        loadData(fileInfo.filePath, FILES)
    }

    fun switchView(viewMode: ViewMode) {
        _viewMode.value = viewMode
        storageModel.saveViewMode(viewMode)
    }

    fun onSortClicked() {
        _sortEvent.value = storageModel.getSortMode()
    }

    fun onSort(sortMode: SortMode) {
        storageModel.saveSortMode(sortMode)
        refreshList()
    }

    fun onMenuItemClick(itemId: Int) {

        when (itemId) {
            R.id.action_edit -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_RENAME)
                    val fileInfo = _fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
                    endActionMode()
                    fileInfo?.let {
                        _singleOpData.value = Pair(Operations.RENAME, fileInfo)
                    }
                }
            }

            R.id.action_hide -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_HIDE)
                    val fileInfo = _fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
                    endActionMode()
                    fileInfo?.let {
                        _singleOpData.value = Pair(Operations.HIDE, fileInfo)
                        onHideOperation(it)
                    }
                }
            }

        }
    }

    private fun onHideOperation(fileInfo: FileInfo) {
     val fileName = fileInfo.fileName
        val newName = if (fileName.startsWith(".")) {
            fileName.substring(1)
        }
        else {
            ".$fileName"
        }
        onOperation(Operations.HIDE, newName)
    }

    private fun endActionMode() {
        multiSelectionHelper.clearSelection()
        _actionModeState.value = ActionModeState.ENDED
        _refreshEvent.value = false
    }

    fun onOperation(operation: Operations?, name: String?) {
        when (operation) {
            Operations.RENAME, Operations.HIDE -> {
                val path = singleOpData.value?.second?.filePath
                if (path != null && name != null) {
                    storageModel.renameFile(operation, path, name)
                }
            }
        }
    }

    fun handleSafResult(uri: Uri, flags: Int) {
        storageModel.handleSafResult(uri, flags)

    }

    val navigationCallback = object : NavigationCallback {
        override fun onHomeClicked() {
        }

        override fun onNavButtonClicked(dir: String?) {
            if (navigation.shouldLoadDir(dir)) {
                Analytics.getLogger().navBarClicked(false)
            }
        }

        override fun onNavButtonClicked(category: Category, bucketName: String?) {
        }
    }

    val apkDialogListener = object : DialogHelper.ApkDialogListener {

        override fun onInstallClicked(path: String?) {
            val canInstall = InstallHelper.canInstallApp(AceApplication.appContext)
            apkPath = path
            _installAppEvent.value = Pair(canInstall, path)
        }

        override fun onCancelClicked() {
        }

        override fun onOpenApkClicked(path: String) {
        }

    }

    val multiSelectionListener = object : MultiSelectionHelper.MultiSelectionListener {
        override fun refresh() {
            _refreshEvent.value = true
        }
    }

    val singleOperationListener = object : DialogHelper.SingleOperationListener {

        override fun onPositiveClick(operation: Operations?, fileName: String?) {
        }

        override fun onNegativeClick(operation: Operations?) {
        }

    }

}