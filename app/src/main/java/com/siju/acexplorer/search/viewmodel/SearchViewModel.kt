package com.siju.acexplorer.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.search.model.SearchModel
import com.siju.acexplorer.search.model.SearchModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "SearchViewModel"
private const val MIN_CHAR_QUERY = 2

class SearchViewModel(private val searchModel: SearchModel) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val searchResult : LiveData<ArrayList<FileInfo>>

    init {
        searchModel as SearchModelImpl
        searchResult = searchModel.searchResult
    }

    fun search(path : String?, query : String?, category: Category = Category.FILES) {
        if (query != null && query.isNotBlank() && query.length >= MIN_CHAR_QUERY) {
            var rootPath = path
            if (rootPath == null) {
                rootPath = StorageUtils.internalStorage
            }
            uiScope.launch(Dispatchers.IO) {
                searchModel.searchData(rootPath, query, category)
            }
        }
        else {
            searchModel.emptyQuerySearch()
        }
    }



}