package com.siju.acexplorer.storage.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.Category.*
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.search.helper.SearchUtils
import com.siju.acexplorer.storage.helper.RecentDataConverter.getRecentItemList
import com.siju.acexplorer.storage.helper.RecentDataConverter.getRecentItemListWithoutHeader
import com.siju.acexplorer.storage.model.*
import com.siju.acexplorer.storage.model.backstack.BackStackInfo
import com.siju.acexplorer.storage.model.operations.OperationAction
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.model.operations.PasteConflictCheckData
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback
import com.siju.acexplorer.storage.modules.zipviewer.view.ZipViewer
import com.siju.acexplorer.storage.modules.zipviewer.view.ZipViewerFragment
import com.siju.acexplorer.storage.presenter.OperationPresenter
import com.siju.acexplorer.storage.presenter.OperationPresenterImpl
import com.siju.acexplorer.storage.presenter.ZipPresenter
import com.siju.acexplorer.storage.presenter.ZipPresenterImpl
import com.siju.acexplorer.storage.view.*
import com.siju.acexplorer.utils.InstallHelper
import com.siju.acexplorer.utils.ScrollInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "FileListViewModel"
private const val ZIP_EXT = ".zip"

class FileListViewModel(private val storageModel: StorageModel, private val searchScreen: Boolean = false) : ViewModel() {

    private var zipViewer: ZipViewer? = null
    var apkPath: String? = null

    private var navigationView: NavigationView? = null

    lateinit var category: Category
        private set
    var currentDir: String? = null
        private set

    private val navigation = Navigation(this)

    private var bucketName: String? = null
    private val backStackInfo = BackStackInfo()
    private val _viewFileEvent = MutableLiveData<Pair<String, String?>>()
    private val _viewImageFileEvent = MutableLiveData<Pair<ArrayList<FileInfo>, Int>>()

    val viewImageFileEvent : LiveData<Pair<ArrayList<FileInfo>, Int>>
    get() = _viewImageFileEvent

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

    private val _recentFileData = MutableLiveData<Pair<Category, ArrayList<RecentTimeData.RecentDataItem>>>()

    val recentFileData : LiveData<Pair<Category, ArrayList<RecentTimeData.RecentDataItem>>>
        get() = _recentFileData

    val pasteOpData: LiveData<PasteOpData>

    val multiSelectionOpData: LiveData<Pair<Operations, ArrayList<FileInfo>>>

    val pasteConflictCheckData: LiveData<PasteConflictCheckData>

    val noOpData: LiveData<Pair<Operations, String>>

    val singleOpData: LiveData<Pair<Operations, FileInfo>>

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

    val multiSelectionHelper = MultiSelectionHelper()

    val actionModeState: LiveData<ActionModeState>
        get() = _actionModeState

    private val _selectedFileInfo = MutableLiveData<Pair<Int, FileInfo?>>()

    val selectedFileInfo: LiveData<Pair<Int, FileInfo?>>
        get() = _selectedFileInfo

    private val _refreshEvent = MutableLiveData<Boolean>()

    val refreshEvent: LiveData<Boolean>
        get() = _refreshEvent

    val operationResult: LiveData<Pair<Operations, OperationAction>>

    val showPasteDialog: LiveData<Triple<Operations, String, ArrayList<FileInfo>>>

    val showZipDialog: LiveData<Triple<Operations, String, String>>
        get() = zipPresenter.getShowZipDialogLiveData

    val showCompressDialog: LiveData<Triple<Operations, String, ArrayList<FileInfo>>>
        get() = zipPresenter.showCompressDialog

    private val _directoryClicked = MutableLiveData<Boolean>()

    val directoryClicked: LiveData<Boolean>
        get() = _directoryClicked
    private val _scrollInfo = MutableLiveData<ScrollInfo>()

    val scrollInfo: LiveData<ScrollInfo>
        get() = _scrollInfo

    private var scrollPosition = hashMapOf<String?, ScrollInfo>()

    private val _homeClicked = MutableLiveData<Boolean>()

    val homeClicked: LiveData<Boolean>
        get() = _homeClicked
    val dragEvent: LiveData<Triple<Category, Int, ArrayList<FileInfo>>>
        get() = operationPresenter.getDragEvent()

    val showDragDialog: LiveData<Triple<String?, ArrayList<FileInfo>, DialogHelper.DragDialogListener>>
        get() = operationPresenter.showDragDialog

    private val operationPresenter: OperationPresenter

    private val multiSelectionListener = object : MultiSelectionHelper.MultiSelectionListener {
        override fun refresh() {
            _refreshEvent.value = true
        }
    }

    private val zipPresenter: ZipPresenter

    val pasteConflictListener: DialogHelper.PasteConflictListener
        get() = operationPresenter.getPasteConflictListener

    val zipOperationCallback: OperationHelper.ZipOperationCallback
        get() = zipPresenter.zipOperationCallback

    private val _navigationClicked = MutableLiveData<Boolean>()

    val navigationClicked: LiveData<Boolean>
        get() = _navigationClicked

    val refreshData : LiveData<Boolean>

    init {
        val model = storageModel as StorageModelImpl
        operationResult = model.operationData
        zipPresenter = ZipPresenterImpl(this, navigation, backStackInfo)
        multiSelectionHelper.setMultiSelectionListener(multiSelectionListener)
        operationPresenter = OperationPresenterImpl(this, multiSelectionHelper, model)
        multiSelectionOpData = operationPresenter.getMultiSelectionOpData()
        pasteOpData = operationPresenter.getPasteOpData()
        singleOpData = operationPresenter.getSingleOpData()
        noOpData = operationPresenter.getNoOpData()
        showPasteDialog = operationPresenter.showPasteDialog()
        pasteConflictCheckData = operationPresenter.getPasteData()
        refreshData = model._refreshData
    }

    fun loadData(path: String?, category: Category, fromSearch: Boolean = false) {
        Log.d(this.javaClass.name, "loadData: path $path , category $category")
        val newCategory = if (fromSearch) {
           getNewSearchCategory(category)
        }
        else {
            category
        }
        addNavigation(path, newCategory)
        addToBackStack(path, newCategory)
        setCategory(newCategory)
        setCurrentDir(path)
        if (RecentTimeHelper.isRecentTimeLineCategory(newCategory)) {
            uiScope.launch(Dispatchers.IO) {
                val data = storageModel.loadRecentData(path, newCategory)
                Log.d(this.javaClass.name,
                        "onDataloaded loadData: data ${data.size} , category $newCategory")
                _recentFileData.postValue(Pair(newCategory, data))
                handleScrollPosition()
            }
        }
        else {
            uiScope.launch(Dispatchers.IO) {
                val data = storageModel.loadData(path, newCategory)
                Log.d(this.javaClass.name,
                        "onDataloaded loadData: data ${data.size} , category $newCategory")
                _fileData.postValue(data)
                handleScrollPosition()
            }
        }
    }

    private fun getNewSearchCategory(category: Category) : Category {
        return when(category) {
            RECENT_IMAGES -> SEARCH_RECENT_IMAGES
            RECENT_VIDEOS -> SEARCH_RECENT_VIDEOS
            RECENT_AUDIO -> SEARCH_RECENT_AUDIO
            RECENT_DOCS -> SEARCH_RECENT_DOCS
            else -> category
        }
    }

    //Reload data without adding to backstack again
    private fun reloadData(path: String?, category: Category) {
        Log.d(this.javaClass.name, "reloadData: path $path , category $category")
        addNavigation(path, category)
        setCategory(category)
        setCurrentDir(path)
        if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
            uiScope.launch(Dispatchers.IO) {
                val data = storageModel.loadRecentData(path, category)
                _recentFileData.postValue(Pair(category, data))
                handleScrollPosition()
            }
        }
        else {
            uiScope.launch(Dispatchers.IO) {
                _fileData.postValue(storageModel.loadData(path, category))
                handleScrollPosition()
            }
        }
    }

    private fun handleScrollPosition() {
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

    fun getViewMode(category: Category) : ViewMode {
        return when {
            CategoryHelper.isDefaultGalleryImageCategory(category) -> storageModel.getImageViewMode()
            CategoryHelper.isDefaultGalleryVideoCategory(category) -> storageModel.getVideoViewMode()
            else -> storageModel.getViewMode()
        }
    }

    fun setCategory(category: Category) {
        this.category = category
        showFab.postValue(canShowFab(category))
        operationPresenter.category = category
    }

    private fun setCurrentDir(currentDir: String?) {
        this.currentDir = currentDir
        operationPresenter.currentDir = currentDir
    }

    private fun canShowFab(category: Category) = !CategoryHelper.checkIfLibraryCategory(category)

    fun addHomeButton() {
        navigationView?.addHomeButton()
    }

    fun addGenericTitle(category: Category) {
        navigationView?.addGenericTitle(category)
    }

    fun addLibraryTitle(category: Category) {
        navigationView?.addLibraryTitle(category)
    }

    fun createNavButtonStorage(storageType: StorageUtils.StorageType, dir: String) {
        when (storageType) {
            StorageUtils.StorageType.ROOT -> navigationView?.createRootStorageButton(dir)
            StorageUtils.StorageType.INTERNAL -> navigationView?.createInternalStorageButton(dir)
            StorageUtils.StorageType.EXTERNAL -> navigationView?.createExternalStorageButton(dir)
        }
    }

    fun createNavButtonStorageParts(path: String, dirName: String) {
        navigationView?.createNavButtonStorageParts(path, dirName)
    }

    fun setNavigationView(navigationView: NavigationView) {
        this.navigationView = navigationView
    }

    private fun setInitialDir(path: String?, category: Category) {
        navigation.setInitialDir(path, category)
    }

    private fun setNavDirectory(path: String?, category: Category) {
        navigation.setNavDirectory(path, category)
    }

    private fun createNavigationForCategory(category: Category) {
        navigation.createNavigationForCategory(category)
    }

    fun createLibraryTitleNavigation(category: Category, bucketName: String?) {
        navigationView?.createLibraryTitleNavigation(category, bucketName)
    }

    private fun setupNavigation(path: String?, category: Category) {
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


    fun handleItemClick(fileInfo: FileInfo, position: Int, fromSearch : Boolean = false) {
        Log.d(TAG, "handleItemClick: category:$category")
        if (isActionModeActive()) {
            if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
                handleRecentSelection(position)
            }
            else {
                multiSelectionHelper.toggleSelection(position, false)
            }
            handleActionModeClick(fileInfo)
            return
        }
        when (category) {
            AUDIO, VIDEO, IMAGE, DOCS, PODCASTS, ALBUM_DETAIL, ARTIST_DETAIL, GENRE_DETAIL, FOLDER_IMAGES,
            FOLDER_VIDEOS, ALL_TRACKS, RECENT_AUDIO, RECENT_DOCS, RECENT_IMAGES, RECENT_VIDEOS,
            IMAGES_ALL, VIDEO_ALL, RECENT_ALL, LARGE_FILES_AUDIO, LARGE_FILES_VIDEOS, LARGE_FILES_IMAGES,
            LARGE_FILES_DOC, CAMERA_IMAGES, CAMERA_VIDEO, SEARCH_FOLDER_IMAGES, SEARCH_FOLDER_VIDEOS, SEARCH_FOLDER_AUDIO,
            SEARCH_FOLDER_DOCS, DOCS_OTHER, SCREENSHOT, RECENT_FOLDER, RECENT_IMAGES_FOLDER, RECENT_VIDEOS_FOLDER, RECENT_AUDIO_FOLDER, RECENT_DOC_FOLDER,
            SEARCH_RECENT_IMAGES, SEARCH_RECENT_VIDEOS, SEARCH_RECENT_AUDIO, SEARCH_RECENT_DOCS-> {
                onFileClicked(fileInfo, position)
            }
            FILES, CAMERA, LARGE_FILES_ALL, LARGE_FILES_COMPRESSED, LARGE_FILES_APP, LARGE_FILES_OTHER, DOWNLOADS, COMPRESSED, FAVORITES, PDF, APPS, RECENT_APPS -> {
                onFileItemClicked(fileInfo, position)
            }

            GENERIC_MUSIC -> {
                fileInfo.subcategory?.let { loadData(null, it) }
            }

            ALBUMS -> {
                bucketName = fileInfo.title
                loadData(fileInfo.id.toString(), ALBUM_DETAIL)
            }

            ARTISTS -> {
                bucketName = fileInfo.title
                loadData(fileInfo.id.toString(), ARTIST_DETAIL)
            }

            GENRES -> {
                bucketName = fileInfo.title
                loadData(fileInfo.id.toString(), GENRE_DETAIL)
            }

            GENERIC_IMAGES -> {
                bucketName = fileInfo.fileName
                loadData(fileInfo.bucketId.toString(), FOLDER_IMAGES)
            }

            GENERIC_VIDEOS -> {
                bucketName = fileInfo.fileName
                loadData(fileInfo.bucketId.toString(), FOLDER_VIDEOS)
            }

            RECENT, LARGE_FILES -> {
                fileInfo.category?.let { loadData(null, it, fromSearch) }
            }

            CAMERA_GENERIC ->  {
                fileInfo.category?.let { loadData(SearchUtils.getCameraDirectory(), it) }
            }

            WHATSAPP, TELEGRAM -> {
                fileInfo.category?.let { loadData(fileInfo.filePath, it) }
            }

            APP_MANAGER -> {

            }
            else -> {
            }
        }
    }

    private fun handleRecentSelection(position: Int) {
        val data = recentFileData.value?.second?.get(position)
        if (data is RecentTimeData.RecentDataItem.Item) {
            multiSelectionHelper.toggleSelection(position, false, data.headerType)

        }
    }

    fun isActionModeActive() = _actionModeState.value == ActionModeState.STARTED

    private fun onFileItemClicked(fileInfo: FileInfo, position: Int) {
        if (fileInfo.isDirectory) {
            onDirectoryClicked(fileInfo, position)
        } else {
            onFileClicked(fileInfo, position)
        }
    }

    private fun onDirectoryClicked(fileInfo: FileInfo, position: Int) {
        _directoryClicked.value = true
        if (zipPresenter.isZipMode) {
            zipViewer?.onDirectoryClicked(position)
        } else {
            loadData(fileInfo.filePath, FILES)
        }
    }

    private fun onFileClicked(fileInfo: FileInfo, position: Int) {
        if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
            handleRecentItemFileClicked(fileInfo, position)
            return
        }
        val path = fileInfo.filePath
        Log.d(TAG, "onFileClicked:category:${fileInfo.category}, rootCategory:$category, path:$path")
        when {
            isZipFile(path) -> openZipViewer(path)
            zipPresenter.isZipMode -> zipViewer?.onFileClicked(position)
            CategoryHelper.isAnyImagesCategory(fileInfo.category) -> {
                fileData.value?.let {
                    _viewImageFileEvent.postValue(Pair(it, position))
                }
            }
            else -> {
                path?.let {
                    _viewFileEvent.postValue(Pair(path, fileInfo.extension))
                }
            }
        }
    }

    private fun handleRecentItemFileClicked(fileInfo: FileInfo, position: Int) {
        val path = fileInfo.filePath
        Log.d(TAG, "handleRecentItemFileClicked:category:${fileInfo.category}, dir:${fileInfo.isDirectory}, rootCategory:$category, path:$path")
        if (fileInfo.isDirectory) {
            onDirectoryClicked(fileInfo, position)
        } else {
            when {
                isZipFile(path) -> openZipViewer(path)
                zipPresenter.isZipMode -> zipViewer?.onFileClicked(position)
                CategoryHelper.isAnyImagesCategory(fileInfo.category) -> {
                    recentFileData.value?.let {
                        val list = if (RecentTimeHelper.isRecentSearchCategory(category)) {
                            getRecentItemListWithoutHeader(it.second)
                        }
                        else {
                            getRecentItemList(it.second)
                        }
                        _viewImageFileEvent.postValue(Pair(list, position))
                    }
                }
                else -> {
                    path?.let {
                        _viewFileEvent.postValue(Pair(path, fileInfo.extension))
                    }
                }
            }
        }
    }

    private fun isZipFile(path: String?) : Boolean {
        path ?: return false
        return !zipPresenter.isZipMode && path.toLowerCase(Locale.ROOT).endsWith(ZIP_EXT)
    }

    private fun openZipViewer(path: String?) {
        path?.let {
            zipPresenter.isZipMode = true
            _openZipViewerEvent.value = Pair(it, zipPresenter.zipCallback)
        }
    }

    fun handleLongClick(fileInfo: FileInfo, position: Int) {
        Log.d(TAG, "handleLongClick:position $position, canLongpress:${canLongPress()}, category:$category, actionMode:${actionModeState.value}")
        if (CategoryHelper.isSortOrActionModeUnSupported(category)) {
            return
        }
        if (canLongPress()) {
            if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
                handleRecentSelection(position)
            }
            else {
                multiSelectionHelper.toggleSelection(position, true)
            }
            handleActionModeClick(fileInfo)
            operationPresenter.setLongPressedTime(System.currentTimeMillis())
            if (isActionModeActive() && multiSelectionHelper.getSelectedCount() >= 1) {
                operationPresenter.onDragStarted()
            }
        }
    }

    fun handleActionModeClick(fileItem: FileInfo?) {
        val hasCheckedItems = multiSelectionHelper.hasSelectedItems()
        Log.d(TAG, "handleActionModeClick state:${_actionModeState.value}")
        if (hasCheckedItems && !isActionModeActive()) {
            _actionModeState.value = ActionModeState.STARTED
            operationPresenter.clearDraggedData()
        } else if (!hasCheckedItems && isActionModeActive()) {
            endActionMode()
        }
        if (isActionModeActive()) {
            val selectedCount = multiSelectionHelper.getSelectedCount()
            fileItem?.let { operationPresenter.toggleDragData(it) }
            if (selectedCount == 1) {
                val fileInfo = getFileInfo(category)
                _selectedFileInfo.value = Pair(selectedCount, fileInfo)
            } else {
                _selectedFileInfo.value = Pair(selectedCount, null)
            }
        }
    }

    private fun getFileInfo(category: Category) : FileInfo? {
        if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
            val fileList = _recentFileData.value?.second
            fileList?.let {
                val recentDataItem = it[multiSelectionHelper.selectedItems.keyAt(0)]
                recentDataItem as RecentTimeData.RecentDataItem.Item
                return recentDataItem.fileInfo
            }

        }
        else {
            return _fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
        }
        return null
    }

    private fun canLongPress() = !zipPresenter.isZipMode

    private fun addNavigation(path: String?, category: Category) {
        setupNavigation(path, category)
        navigation.addLibSpecificNavigation(category, bucketName)
    }

    private fun addToBackStack(path: String?, category: Category) {
        backStackInfo.addToBackStack(path, category)
    }

    private fun hasBackStack() : Boolean {
        return if (searchScreen) {
            backStackInfo.getBackStack().size >= 1
        }
        else {
            backStackInfo.hasBackStack()
        }
    }

    fun onBackPress(): Boolean {
        var result = true
        when {
            zipPresenter.isZipMode -> result = handleZipModeBackPress()
            isActionModeActive() -> result = handleActionModeBackPress()
            homeClicked.value == true -> result = true
            hasBackStack() -> {
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

    private fun handleActionModeBackPress(): Boolean {
        if (homeClicked.value == true) {
            return true
        }
        endActionMode()
        return false
    }

    fun refreshList() {
        val backStack = backStackInfo.getCurrentBackStack()
        backStack?.let {
            reloadData(backStack.first, backStack.second)
        }
    }

    fun switchView(viewMode: ViewMode) {
        Log.d(TAG, "switchView:${category}")
        when {
            CategoryHelper.isDefaultGalleryImageCategory(category) -> storageModel.saveImageViewMode(viewMode)
            CategoryHelper.isDefaultGalleryVideoCategory(category) -> storageModel.saveVideoViewMode(viewMode)
            else -> storageModel.saveViewMode(viewMode)
        }
        _viewMode.value = viewMode
    }

    fun onSortClicked() {
        _sortEvent.value = storageModel.getSortMode()
    }

    fun onSort(sortMode: SortMode) {
        storageModel.saveSortMode(sortMode)
        refreshList()
    }

    fun onFABClicked(operation: Operations, path: String?) {
        operationPresenter.onFabClicked(operation, path)
    }

    fun endActionMode() {
        multiSelectionHelper.clearSelection()
        _actionModeState.value = ActionModeState.ENDED
        operationPresenter.onDragEnded()
        _refreshEvent.value = false
    }

    fun handleSafResult(uri: Uri, flags: Int) {
        storageModel.handleSafResult(uri, flags)
    }

    fun deleteFiles(filesToDelete: ArrayList<FileInfo>) {
        uiScope.launch(Dispatchers.IO) {
            val files = arrayListOf<String>()
            for (fileInfo in filesToDelete) {
                fileInfo.filePath?.let { it1 -> files.add(it1) }
            }
            storageModel.deleteFiles(Operations.DELETE, files)
        }
    }

    fun onPaste(path: String, operationData: Pair<Operations, ArrayList<FileInfo>>) {
        uiScope.launch(Dispatchers.IO) {
            operationPresenter.checkPasteConflict(path, operationData)
        }
    }

    fun onExtractOperation(newFileName: String?, destinationDir: String) {
        val path = singleOpData.value?.second?.filePath
        if (path != null && newFileName != null) {
            storageModel.extractFile(path, destinationDir, newFileName, zipPresenter.zipOperationCallback)
        }
    }

    fun addToFavorite(favList: ArrayList<FileInfo>) {
        val favPathList = ArrayList<String>()
        for (fav in favList) {
            fav.filePath?.let { favPathList.add(it) }
        }
        uiScope.launch(Dispatchers.IO) {
            storageModel.addToFavorite(favPathList)
        }
    }

    fun removeFavorite(favList: ArrayList<FileInfo>) {
        val favPathList = ArrayList<String>()
        for (fav in favList) {
            fav.filePath?.let { favPathList.add(it) }
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

    private fun refreshDataOnNavClicked() {
        reloadData(currentDir, category)
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
                Analytics.logger.navBarClicked(false)
            }
            _navigationClicked.value = true
            if (isActionModeActive() && !isPasteOperationPending()) {
                endActionMode()
            }
            if (zipPresenter.isZipMode) {
                zipViewer?.navigateTo(dir)
            } else {
                setCurrentDir(dir)
                removeBackStackEntries(dir)
                refreshDataOnNavClicked()
            }
        }

        override fun onNavButtonClicked(category: Category, bucketName: String?) {
            _navigationClicked.value = true
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

    fun onUpTouchEvent() {
        operationPresenter.onUpTouchEvent()
    }

    fun onMoveTouchEvent() {
        operationPresenter.onMoveTouchEvent(category)
    }

    fun isDragNotStarted() = operationPresenter.isDragNotStarted()

    fun onDragDropEvent(pos: Int) {
        operationPresenter.onDragDropEvent(pos)
    }

    fun onOperation(operation: Operations?, newFileName: String?) {
        operationPresenter.onOperation(operation, newFileName)
    }

    fun onMenuItemClick(itemId: Int) {
        operationPresenter.handleMenuItemClick(itemId)
    }

    fun onZipContentsLoaded(data: ArrayList<FileInfo>) {
        _fileData.postValue(data)
    }

    fun isZipMode() = zipPresenter.isZipMode

    fun onZipModeEnd(dir: String?) {
        if (dir != null && dir.isNotEmpty()) {
            setCurrentDir(dir)
        }
        if (category != SEARCH_RECENT_DOCS) {
            reloadData(dir, category)
        }
        showFab()
    }

    fun isDualModeEnabled() = storageModel.isDualModeEnabled()
    fun hasNoBackStackEntry() = backStackInfo.getBackStack().isEmpty()

    fun clearBackStack() {
       backStackInfo.clearBackStack()
    }

    fun setPermissions(path: String, permissions: String, dir: Boolean) {
        storageModel.setPermissions(path, permissions, dir)
    }

    fun onPause() {
        storageModel.onPause()
    }

    fun onResume() {
        storageModel.onResume()
    }

    fun setRefreshStateFalse() {
        storageModel as StorageModelImpl
        storageModel.setRefreshDataFalse()
    }

    fun setFileData(data: ArrayList<FileInfo>?) {
        _fileData.postValue(data)
        setCategory(FILES)
    }

    fun showFab() {
        if (canShowFab(category)) {
            showFab.postValue(true)
        }
    }

    fun copyTo(destDir: String?) {
        destDir?.let {
            operationPresenter.onPaste(Operations.COPY, destDir)
        }
    }

    fun cutTo(destDir: String?) {
        destDir?.let {
            operationPresenter.onPaste(Operations.CUT, destDir)
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

        override fun onOpenApkClicked(path: String?) {
            openZipViewer(path)
        }
    }

}