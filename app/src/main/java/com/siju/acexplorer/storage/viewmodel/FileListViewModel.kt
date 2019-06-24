package com.siju.acexplorer.storage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.Category.*
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.storage.model.StorageModel
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.model.backstack.BackStackInfo
import com.siju.acexplorer.storage.view.Navigation
import com.siju.acexplorer.storage.view.NavigationCallback
import com.siju.acexplorer.storage.view.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class FileListViewModel(private val storageModel: StorageModel) : ViewModel() {
    private lateinit var navigationView: NavigationView
    private lateinit var category: Category
    private val navigation = Navigation(this)
    private var bucketName: String? = null
    private val backStackInfo = BackStackInfo()

    private val _viewFileEvent = MutableLiveData<Pair<String, String>>()

    val viewFileEvent: LiveData<Pair<String, String>>
        get() = _viewFileEvent

    private val _fileData = MutableLiveData<ArrayList<FileInfo>>()

    val fileData: LiveData<ArrayList<FileInfo>>
        get() = _fileData

    val showFab = MutableLiveData<Boolean>()

    private val _viewMode = MutableLiveData<ViewMode>()

    val viewMode: LiveData<ViewMode>
        get() = _viewMode

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        _viewMode.value = storageModel.getViewMode()
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

    fun onHiddenFileSettingChanged(value : Boolean) {
        storageModel.saveHiddenFileSetting(value)
        val backStack = backStackInfo.getCurrentBackStack()
        backStack?.let {
            reloadData(backStack.first, backStack.second)
        }
    }

    fun shouldShowHiddenFiles() = storageModel.shouldShowHiddenFiles()


    fun handleItemClick(fileInfo: FileInfo) {
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

    private fun onFileItemClicked(fileInfo: FileInfo) {
        if (fileInfo.isDirectory) {
            onDirectoryClicked(fileInfo)
        }
        else {
            onFileClicked(fileInfo)
        }
    }

    private fun onFileClicked(fileInfo: FileInfo) {
        _viewFileEvent.postValue(Pair(fileInfo.filePath, fileInfo.extension.toLowerCase()))
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
            loadBackStack()
            return false
        }
        return true
    }

    private fun loadBackStack() {
        backStackInfo.remove()
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

}