package com.siju.acexplorer.storage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.storage.model.StorageModel
import com.siju.acexplorer.storage.view.Navigation
import com.siju.acexplorer.storage.view.NavigationCallback
import com.siju.acexplorer.storage.view.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FileListViewModel(private val storageModel: StorageModel) : ViewModel() {
    private lateinit var navigationView: NavigationView
    private val navigation = Navigation(this)

    private val _fileData = MutableLiveData<ArrayList<FileInfo>>()

    val fileData: LiveData<ArrayList<FileInfo>>
        get() = _fileData

    val showFab = MutableLiveData<Boolean>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun loadData(path: String?, category: Category?) {
        Log.e(this.javaClass.name, "loadData: path $path , category $category")
        category?.let {
            uiScope.launch(Dispatchers.IO) {
                _fileData.postValue(storageModel.loadData(path, category))
            }
        }
    }

    fun getViewMode() = storageModel.getViewMode()

    fun setCategory(category: Category?) {
        category?.let {
            showFab.postValue(!CategoryHelper.checkIfLibraryCategory(category))
        }
    }

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