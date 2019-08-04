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
import com.siju.acexplorer.storage.model.*
import com.siju.acexplorer.storage.model.backstack.BackStackInfo
import com.siju.acexplorer.storage.model.operations.OperationAction
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.model.operations.PasteData
import com.siju.acexplorer.storage.model.task.PasteConflictChecker
import com.siju.acexplorer.storage.modules.zipviewer.ZipUtils
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback
import com.siju.acexplorer.storage.modules.zipviewer.view.ZipViewer
import com.siju.acexplorer.storage.modules.zipviewer.view.ZipViewerFragment
import com.siju.acexplorer.storage.view.*
import com.siju.acexplorer.utils.InstallHelper
import com.siju.acexplorer.utils.ScrollInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "FileListViewModel"

class FileListViewModel(private val storageModel: StorageModel) : ViewModel() {

    private var zipViewer: ZipViewer? = null
    private var isZipMode = false
    var apkPath: String? = null
    private lateinit var navigationView: NavigationView
    lateinit var category: Category
        private set

    var currentDir: String? = null
        private set

    private val navigation = Navigation(this)
    private var bucketName: String? = null
    private val backStackInfo = BackStackInfo()

    private val _viewFileEvent = MutableLiveData<Pair<String, String?>>()
    private val _sortEvent = MutableLiveData<SortMode>()

    val sortEvent: LiveData<SortMode>
        get() = _sortEvent

    val viewFileEvent: LiveData<Pair<String, String?>>
        get() = _viewFileEvent

    private val _openZipViewerEvent = MutableLiveData<Pair<String, ZipViewerCallback>>()
    val openZipViewerEvent: LiveData<Pair<String, ZipViewerCallback>>
        get() = _openZipViewerEvent

    private val _fileData = MutableLiveData<ArrayList<FileInfo>>()

    val fileData: LiveData<ArrayList<FileInfo>>
        get() = _fileData

    private val _singleOpData = MutableLiveData<Pair<Operations, FileInfo>>()

    private val _multiSelectionOpData = MutableLiveData<Pair<Operations, ArrayList<FileInfo>>>()

    private val _pasteOpData = MutableLiveData<Triple<Operations, Operations, ArrayList<FileInfo>>>()
    val pasteOpData: LiveData<Triple<Operations, Operations, ArrayList<FileInfo>>>
        get() = _pasteOpData

    val multiSelectionOpData: LiveData<Pair<Operations, ArrayList<FileInfo>>>
        get() = _multiSelectionOpData

    private val _pasteData = MutableLiveData<PasteData>()

    val pasteData: LiveData<PasteData>
        get() = _pasteData

    private val _noOpData = MutableLiveData<Pair<Operations, String>>()

    val noOpData: LiveData<Pair<Operations, String>>
        get() = _noOpData

    val singleOpData: LiveData<Pair<Operations, FileInfo>>
        get() = _singleOpData

    val showFab = MutableLiveData<Boolean>()

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

    private val _selectedFileInfo = MutableLiveData<Pair<Int, FileInfo?>>()

    val selectedFileInfo: LiveData<Pair<Int, FileInfo?>>
        get() = _selectedFileInfo

    private val _refreshEvent = MutableLiveData<Boolean>()

    val refreshEvent: LiveData<Boolean>
        get() = _refreshEvent

    val operationResult: LiveData<Pair<Operations, OperationAction>>

    private val _showPasteDialog = MutableLiveData<Triple<Operations, String, ArrayList<FileInfo>>>()

    val showPasteDialog: LiveData<Triple<Operations, String, ArrayList<FileInfo>>>
        get() = _showPasteDialog

    private val _showZipDialog = MutableLiveData<Triple<Operations, String, String>>()

    val showZipDialog: LiveData<Triple<Operations, String, String>>
        get() = _showZipDialog

    private val _showCompressDialog = MutableLiveData<Triple<Operations, String, ArrayList<FileInfo>>>()

    val showCompressDialog: LiveData<Triple<Operations, String, ArrayList<FileInfo>>>
        get() = _showCompressDialog

    private val _directoryClicked = MutableLiveData<Boolean>()
    val directoryClicked: LiveData<Boolean>
        get() = _directoryClicked

    private val _scrollInfo = MutableLiveData<ScrollInfo>()

    val scrollInfo: LiveData<ScrollInfo>
        get() = _scrollInfo

    private var scrollPosition = hashMapOf<String?, ScrollInfo>()

    private var scrollToTop = false

    private val _homeClicked = MutableLiveData<Boolean>()
    val homeClicked: LiveData<Boolean>
        get() = _homeClicked

    init {
        val model = storageModel as StorageModelImpl
        operationResult = model.operationData
    }

    fun loadData(path: String?, category: Category) {
        Log.e(this.javaClass.name, "loadData: path $path , category $category")
        addNavigation(path, category)
        addToBackStack(path, category)
        setCategory(category)
        setCurrentDir(path)
        uiScope.launch(Dispatchers.IO) {
            val data = storageModel.loadData(path, category)
            Log.e(this.javaClass.name,
                  "onDataloaded loadData: data ${data.size} , category $category")
            _fileData.postValue(data)
            handleScrollPosition()
        }
    }

    //Reload data without adding to backstack again
    private fun reloadData(path: String?, category: Category) {
        Log.e(this.javaClass.name, "reloadData: path $path , category $category")
        addNavigation(path, category)
        setCategory(category)
        setCurrentDir(path)
        uiScope.launch(Dispatchers.IO) {
            _fileData.postValue(storageModel.loadData(path, category))
            handleScrollPosition()
        }
    }

    private fun handleScrollPosition() {
        if (scrollToTop) {
            _scrollInfo.postValue(ScrollInfo(0, 0))
            scrollToTop = false
            return
        }
        currentDir?.let {
            if (scrollPosition.containsKey(it)) {
                val scrollInfo = scrollPosition[it]
                _scrollInfo.postValue(scrollInfo)
            }
        }
    }

    fun saveScrollInfo(scrollInfo: ScrollInfo) {
        scrollPosition[currentDir] = scrollInfo
    }

    private fun removeScrolledPos() {
        scrollPosition.remove(currentDir)
    }

    fun getViewMode() = storageModel.getViewMode()

    private fun setCategory(category: Category) {
        this.category = category
        showFab.postValue(canShowFab(category))
    }

    private fun setCurrentDir(currentDir: String?) {
        this.currentDir = currentDir
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
        scrollToTop = true
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
                onFileClicked(fileInfo, position)
            }
            FILES, DOWNLOADS, COMPRESSED, FAVORITES, PDF, APPS, LARGE_FILES, RECENT_APPS       -> {
                onFileItemClicked(fileInfo, position)
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

    private fun onFileItemClicked(fileInfo: FileInfo, position: Int) {
        if (fileInfo.isDirectory) {
            onDirectoryClicked(fileInfo, position)
        }
        else {
            onFileClicked(fileInfo, position)
        }
    }

    private fun onDirectoryClicked(fileInfo: FileInfo, position: Int) {
        _directoryClicked.value = true
        if (isZipMode) {
            zipViewer?.onDirectoryClicked(position)
        }
        else {
            loadData(fileInfo.filePath, FILES)
        }
    }

    private fun onFileClicked(fileInfo: FileInfo, position: Int) {
        val path = fileInfo.filePath
        when {
            isZipFile(path) -> openZipViewer(path)
            isZipMode       -> zipViewer?.onFileClicked(position)
            else            -> _viewFileEvent.postValue(Pair(path, fileInfo.extension))
        }
    }

    private fun isZipFile(path: String) = !isZipMode && ZipUtils.isZipViewable(path)

    private fun openZipViewer(path: String?) {
        path?.let {
            isZipMode = true
            _openZipViewerEvent.value = Pair(it, zipCallback)
        }
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
            val selectedCount = multiSelectionHelper.getSelectedCount()
            if (selectedCount == 1) {
                val fileInfo = _fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
                _selectedFileInfo.value = Pair(selectedCount, fileInfo)
            }
            else {
                _selectedFileInfo.value = Pair(selectedCount, null)
            }
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
        var result  = true
        when {
            isZipMode                 -> result = handleZipModeBackPress()
            isActionModeActive()      -> result = handleActionModeBackPress()
            homeClicked.value == true -> result = true
            hasBackStack()            -> {
                backStackInfo.removeLastEntry()
                removeScrolledPos()
                refreshList()
                result = false
            }
        }
        if (result) {
            storageModel.onExit()
        }
        return result
    }

    private fun handleZipModeBackPress(): Boolean {
        if (homeClicked.value == true) {
            zipViewer?.navigateTo(null)
            return true
        }
        zipViewer?.onBackPress()
        return false
    }

    private fun handleActionModeBackPress() : Boolean {
        if (homeClicked.value == true) {
            return true
        }
        else if (!isPasteOperationPending()) {
            endActionMode()
        }
        return false
    }

    private fun refreshList() {
        val backStack = backStackInfo.getCurrentBackStack()
        backStack?.let {
            reloadData(backStack.first, backStack.second)
        }
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
        setScrollToTop(true)
        refreshList()
    }

    private fun setScrollToTop(value: Boolean) {
        this.scrollToTop = value
    }

    fun onMenuItemClick(itemId: Int) {

        when (itemId) {
            R.id.action_edit       -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_RENAME)
                    val fileInfo = _fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
                    endActionMode()
                    fileInfo?.let {
                        _singleOpData.value = Pair(Operations.RENAME, fileInfo)
                    }
                }
            }

            R.id.action_hide       -> {
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

            R.id.action_info       -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_PROPERTIES)
                    val fileInfo = _fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
                    endActionMode()
                    fileInfo?.let {
                        _singleOpData.value = Pair(Operations.INFO, fileInfo)
                    }
                }
            }

            R.id.action_delete     -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_DELETE)
                    val filesToDelete = arrayListOf<FileInfo>()
                    val selectedItems = multiSelectionHelper.selectedItems
                    for (i in 0 until selectedItems.size()) {
                        val fileInfo = _fileData.value?.get(selectedItems.keyAt(i))
                        fileInfo?.let { filesToDelete.add(it) }
                    }
                    endActionMode()
                    _multiSelectionOpData.value = Pair(Operations.DELETE, filesToDelete)
                }
            }

            R.id.action_share      -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_SHARE)
                    val filesToShare = arrayListOf<FileInfo>()
                    val selectedItems = multiSelectionHelper.selectedItems
                    for (i in 0 until selectedItems.size()) {
                        val fileInfo = _fileData.value?.get(selectedItems.keyAt(i))
                        if (fileInfo?.isDirectory == false) {
                            filesToShare.add(fileInfo)
                        }
                    }
                    endActionMode()
                    _multiSelectionOpData.value = Pair(Operations.SHARE, filesToShare)
                }
            }

            R.id.action_copy       -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_COPY)
                    val filesToCopy = arrayListOf<FileInfo>()
                    val selectedItems = multiSelectionHelper.selectedItems
                    for (i in 0 until selectedItems.size()) {
                        val fileInfo = _fileData.value?.get(selectedItems.keyAt(i))
                        fileInfo?.let { filesToCopy.add(it) }
                    }
                    endActionMode()
                    _multiSelectionOpData.value = Pair(Operations.COPY, filesToCopy)
                }
            }

            R.id.action_cut        -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_CUT)
                    val filesToMove = arrayListOf<FileInfo>()
                    val selectedItems = multiSelectionHelper.selectedItems
                    for (i in 0 until selectedItems.size()) {
                        val fileInfo = _fileData.value?.get(selectedItems.keyAt(i))
                        fileInfo?.let { filesToMove.add(it) }
                    }
                    endActionMode()
                    _multiSelectionOpData.value = Pair(Operations.CUT, filesToMove)
                }
            }


            R.id.action_paste      -> {
                val operations = _multiSelectionOpData.value?.first
                if (operations == Operations.COPY || operations == Operations.CUT) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_PASTE)
                    val list = _multiSelectionOpData.value?.second
                    endActionMode()
                    list?.let {
                        _pasteOpData.value = Triple(Operations.PASTE, operations, list)
                    }
                }
            }

            R.id.action_extract    -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_EXTRACT)
                    val fileInfo = _fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
                    endActionMode()
                    fileInfo?.let {
                        _singleOpData.value = Pair(Operations.EXTRACT, fileInfo)
                    }
                }
            }

            R.id.action_archive    -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_ARCHIVE)
                    val filesToArchive = arrayListOf<FileInfo>()
                    val selectedItems = multiSelectionHelper.selectedItems
                    for (i in 0 until selectedItems.size()) {
                        val fileInfo = _fileData.value?.get(selectedItems.keyAt(i))
                        fileInfo?.let { filesToArchive.add(it) }
                    }
                    endActionMode()
                    _multiSelectionOpData.value = Pair(Operations.COMPRESS, filesToArchive)
                }
            }

            R.id.action_fav        -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_ADD_FAV)
                    val favList = arrayListOf<FileInfo>()
                    val selectedItems = multiSelectionHelper.selectedItems
                    for (i in 0 until selectedItems.size()) {
                        val fileInfo = _fileData.value?.get(selectedItems.keyAt(i))
                        if (fileInfo?.isDirectory == true) {
                            favList.add(fileInfo)
                        }
                    }
                    endActionMode()
                    _multiSelectionOpData.value = Pair(Operations.FAVORITE, favList)
                }
            }

            R.id.action_delete_fav -> {
                if (multiSelectionHelper.hasSelectedItems()) {
                    Analytics.getLogger().operationClicked(Analytics.Logger.EV_DELETE_FAV)
                    val favList = arrayListOf<FileInfo>()
                    val selectedItems = multiSelectionHelper.selectedItems
                    for (i in 0 until selectedItems.size()) {
                        val fileInfo = _fileData.value?.get(selectedItems.keyAt(i))
                        fileInfo?.let { favList.add(it) }
                    }
                    endActionMode()
                    _multiSelectionOpData.value = Pair(Operations.DELETE_FAVORITE, favList)
                }
            }

        }
    }

    fun onFABClicked(operation: Operations, path: String?) {
        when (operation) {
            Operations.FOLDER_CREATION, Operations.FILE_CREATION -> {
                Analytics.getLogger().operationClicked(Analytics.Logger.EV_FAB)
                path?.let {
                    _noOpData.value = Pair(operation, path)
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
            Operations.FOLDER_CREATION         -> {
                val path = noOpData.value?.second
                if (path != null && name != null) {
                    storageModel.createFolder(operation, path, name)
                }
            }

            Operations.FILE_CREATION           -> {
                val path = noOpData.value?.second
                if (path != null && name != null) {
                    storageModel.createFile(operation, path, name)
                }
            }

            Operations.COMPRESS                -> {
                val filesToArchive = multiSelectionOpData.value?.second
                filesToArchive?.let {
                    currentDir?.let { currentDir ->
                        val destinationDir = "$currentDir/$name.zip"
                        storageModel.compressFile(destinationDir, filesToArchive,
                                                  zipOperationCallback)
                    }
                }
            }
        }

    }

    fun handleSafResult(uri: Uri, flags: Int) {
        storageModel.handleSafResult(uri, flags)

    }

    fun deleteFiles(filesToDelete: ArrayList<FileInfo?>) {
        uiScope.launch(Dispatchers.IO) {
            val files = arrayListOf<String>()
            for (fileInfo in filesToDelete) {
                fileInfo?.let {
                    files.add(it.filePath)
                }
            }
            storageModel.deleteFiles(Operations.DELETE, files)
        }
    }

    fun onPaste(path: String, operationData: Pair<Operations, ArrayList<FileInfo>>) {
        uiScope.launch(Dispatchers.IO) {
            val pasteConflictChecker = PasteConflictChecker(path, false,
                                                            operationData.first,
                                                            operationData.second)
            pasteConflictChecker.setListener(pasteResultListener)
            pasteConflictChecker.run()
        }
    }

    fun onExtractOperation(operation: Operations?, newFileName: String?, destinationDir: String) {
        val path = singleOpData.value?.second?.filePath
        if (path != null && newFileName != null) {
            storageModel.extractFile(path, destinationDir, newFileName, zipOperationCallback)
        }
    }

    fun addToFavorite(favList: ArrayList<FileInfo>) {
        val favPathList = ArrayList<String>()
        for (fav in favList) {
            favPathList.add(fav.filePath)
        }
        uiScope.launch(Dispatchers.IO) {
            storageModel.addToFavorite(favPathList)
        }
    }

    fun removeFavorite(favList: ArrayList<FileInfo>) {
        val favPathList = ArrayList<String>()
        for (fav in favList) {
            favPathList.add(fav.filePath)
        }
        uiScope.launch(Dispatchers.IO) {
            storageModel.deleteFavorite(favPathList)
        }
    }

    fun setZipViewer(zipViewer: ZipViewerFragment) {
        this.zipViewer = zipViewer
        loadZipData()
    }

    private fun loadZipData() {
        zipViewer?.loadData()
    }

    private fun isPasteOperationPending() = multiSelectionOpData.value?.first == Operations.CUT ||
            multiSelectionOpData.value?.first == Operations.COPY

    val navigationCallback = object : NavigationCallback {
        override fun onHomeClicked() {
            if (isActionModeActive()) {
                endActionMode()
            }
            _homeClicked.value = true
        }

        override fun onNavButtonClicked(dir: String?) {
            if (navigation.shouldLoadDir(dir)) {
                Analytics.getLogger().navBarClicked(false)
            }
            if (isActionModeActive() && !isPasteOperationPending()) {
                endActionMode()
            }
            if (isZipMode) {
                zipViewer?.navigateTo(dir)
            }
            else {
                setCurrentDir(dir)
                removeBackStackEntries(dir)
                refreshList()
            }
        }

        override fun onNavButtonClicked(category: Category, bucketName: String?) {
            if (isActionModeActive() && !isPasteOperationPending()) {
                endActionMode()
            }
            removeBackStackEntriesCategory(category)
            refreshList()
            navigation.addLibSpecificNavigation(category, bucketName)
        }
    }

    private fun removeBackStackEntries(currentDir: String?) {
        var position = 0
        val backStack = backStackInfo.getBackStack()
        for (i in backStack.indices) {
            if (currentDir == backStack[i].filePath) {
                position = i
                break
            }
        }
        for (j in backStack.size - 1 downTo position + 1) {
            backStackInfo.removeEntryAtIndex(j)
        }
    }

    private fun removeBackStackEntriesCategory(category: Category) {
        var position = 0
        val backStack = backStackInfo.getBackStack()
        for (i in backStack.indices) {
            if (category == backStack[i].category) {
                position = i
                break
            }
        }
        for (j in backStack.size - 1 downTo position + 1) {
            backStackInfo.removeEntryAtIndex(j)
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

    private val pasteResultListener = object : PasteConflictChecker.PasteResultCallback {
        override fun showConflictDialog(files: java.util.ArrayList<FileInfo>,
                                        conflictFiles: java.util.ArrayList<FileInfo>,
                                        destFiles: java.util.ArrayList<FileInfo>,
                                        destinationDir: String, operation: Operations) {
            Analytics.getLogger().conflictDialogShown()
            _pasteData.postValue(
                    PasteData(files, conflictFiles, destFiles, destinationDir, operation))
        }

        override fun checkWriteMode(destinationDir: String, files: java.util.ArrayList<FileInfo>,
                                    operation: Operations) {
            storageModel.checkPasteWriteMode(destinationDir, files, pasteActionInfo, operation,
                                             pasteOperationCallback)
        }


        override fun onLowSpace() {
        }

    }

    private val pasteActionInfo = arrayListOf<PasteActionInfo>()
    val pasteConflictListener = object : DialogHelper.PasteConflictListener {

        override fun onSkipClicked(pasteData: PasteData, isChecked: Boolean) {
            var end = false
            val filesToPaste = pasteData.files
            if (isChecked) {
                filesToPaste.removeAll(pasteData.conflictFiles)
                end = true
            }
            else {
                filesToPaste.remove(pasteData.conflictFiles[0])
                pasteData.destFiles.removeAt(0)
                pasteData.conflictFiles.removeAt(0)
                if (pasteData.conflictFiles.isEmpty()) {
                    end = true
                }
            }

            if (end) {
                if (filesToPaste.isNotEmpty()) {
                    storageModel.checkPasteWriteMode(pasteData.destinationDir, filesToPaste,
                                                     pasteActionInfo,
                                                     pasteData.operations,
                                                     pasteOperationCallback)
                }
            }
            else {
                _pasteData.postValue(
                        PasteData(filesToPaste, pasteData.conflictFiles, pasteData.destFiles,
                                  pasteData.destinationDir, pasteData.operations))
            }
        }

        override fun onReplaceClicked(pasteData: PasteData, isChecked: Boolean) {
            var end = false
            val filesToPaste = pasteData.files
            if (isChecked) {
                end = true
            }
            else {
                pasteData.destFiles.removeAt(0)
                pasteData.conflictFiles.removeAt(0)
                if (pasteData.conflictFiles.isEmpty()) {
                    end = true
                }
            }

            if (end) {
                storageModel.checkPasteWriteMode(pasteData.destinationDir, filesToPaste,
                                                 pasteActionInfo,
                                                 pasteData.operations, pasteOperationCallback)
            }
            else {
                _pasteData.postValue(
                        PasteData(filesToPaste, pasteData.conflictFiles, pasteData.destFiles,
                                  pasteData.destinationDir, pasteData.operations))
            }
        }

        override fun onKeepBothClicked(pasteData: PasteData, isChecked: Boolean) {
            var end = false
            val filesToPaste = pasteData.files
            if (isChecked) {
                for (fileInfo in pasteData.conflictFiles) {
                    pasteActionInfo.add(PasteActionInfo(fileInfo.filePath))
                }
                end = true
            }
            else {
                pasteActionInfo.add(PasteActionInfo(pasteData.conflictFiles.get(0).filePath))
                pasteData.destFiles.removeAt(0)
                pasteData.conflictFiles.removeAt(0)
                if (pasteData.conflictFiles.isEmpty()) {
                    end = true
                }
            }

            if (end) {
                storageModel.checkPasteWriteMode(pasteData.destinationDir, filesToPaste,
                                                 pasteActionInfo, pasteData.operations,
                                                 pasteOperationCallback)
            }
            else {
                _pasteData.postValue(
                        PasteData(filesToPaste, pasteData.conflictFiles, pasteData.destFiles,
                                  pasteData.destinationDir, pasteData.operations))
            }
        }

    }

    val pasteOperationCallback = object : OperationHelper.PasteOperationCallback {

        override fun onPasteActionStarted(operation: Operations, destinationDir: String,
                                          files: ArrayList<FileInfo>) {
            when (operation) {
                Operations.COPY -> {
                    _showPasteDialog.postValue(Triple(operation, destinationDir, files))
                }
            }
        }
    }

    private val zipOperationCallback = object : OperationHelper.ZipOperationCallback {
        override fun onZipOperationStarted(operation: Operations, destinationDir: String,
                                           filesToArchive: ArrayList<FileInfo>) {
            _showCompressDialog.postValue(Triple(operation, destinationDir, filesToArchive))
        }

        override fun onZipOperationStarted(operation: Operations, sourceFilePath: String,
                                           destinationDir: String) {
            _showZipDialog.postValue(Triple(operation, sourceFilePath, destinationDir))
        }
    }

    private val zipCallback = object : ZipViewerCallback {
        override fun removeZipScrollPos(newPath: String) {

        }

        override fun onZipModeEnd(dir: String?) {
            if (dir != null && dir.isNotEmpty()) {
                currentDir = dir
            }
            isZipMode = false
            backStackInfo.removeLastEntry()
            reloadData(currentDir, category)
        }

        override fun calculateZipScroll(dir: String) {
        }

        override fun onZipContentsLoaded(data: ArrayList<FileInfo>) {
            _fileData.postValue(data)
        }

        override fun openZipViewer(currentDir: String) {
        }

        override fun setNavDirectory(path: String, isHomeScreenEnabled: Boolean,
                                     category: Category) {
            navigation.setNavDirectory(path, category)
        }

        override fun addToBackStack(path: String, category: Category) {
            backStackInfo.addToBackStack(path, category)
        }

        override fun removeFromBackStack() {
            backStackInfo.removeLastEntry()
        }

        override fun setInitialDir(path: String) {
            navigation.setInitialDir(path, FILES)
        }

    }


}