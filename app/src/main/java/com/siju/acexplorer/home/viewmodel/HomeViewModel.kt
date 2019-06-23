package com.siju.acexplorer.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.home.model.HomeModel
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(private val homeModel: HomeModel) : ViewModel() {

    private val _categories = MutableLiveData<ArrayList<HomeLibraryInfo>>()
    private val _storage = MutableLiveData<ArrayList<StorageItem>>()
    private val _categoryData = MutableLiveData<Pair<Int, HomeLibraryInfo>>()
    private val _categoryClickEvent = MutableLiveData<Pair<String?, Category>>()

    val storage: LiveData<ArrayList<StorageItem>>
        get() = _storage

    val categories: LiveData<ArrayList<HomeLibraryInfo>>
        get() = _categories

    val categoryData: LiveData<Pair<Int, HomeLibraryInfo>>
        get() = _categoryData

    val categoryClickEvent: LiveData<Pair<String?, Category>>
        get() = _categoryClickEvent

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    fun loadData() {
        fetchCategories()
        fetchStorageList()
    }

    private fun fetchCategories() {
        uiScope.launch(Dispatchers.IO) {
            _categories.postValue(homeModel.getCategories())
        }
    }

    private fun fetchStorageList() {
        uiScope.launch(Dispatchers.IO) {
            _storage.postValue(homeModel.getStorage())
        }
    }

    fun fetchCount(categoryInfoList: ArrayList<HomeLibraryInfo>) {
        uiScope.launch(Dispatchers.IO) {
            categoryInfoList.forEachIndexed { index, homeLibraryInfo ->
                val fileInfo = homeModel.loadCountForCategory(homeLibraryInfo.category)
                homeLibraryInfo.count = fileInfo.count
                _categoryData.postValue(Pair(index, homeLibraryInfo))
            }
        }
    }

    fun getCategoryGridColumns() = homeModel.getCategoryGridCols()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onCategoryClick(category: Category) {
        var path: String? = null
        var category1 = category
        when (category) {
            Category.DOWNLOADS -> path = StorageUtils.downloadsDirectory
            Category.AUDIO     -> category1 = Category.GENERIC_MUSIC
            Category.IMAGE     -> category1 = Category.GENERIC_IMAGES
            Category.VIDEO     -> category1 = Category.GENERIC_VIDEOS
            else               -> {
            }
        }
        _categoryClickEvent.postValue(Pair(path, category1))
    }

}