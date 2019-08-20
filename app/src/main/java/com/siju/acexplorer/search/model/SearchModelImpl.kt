package com.siju.acexplorer.search.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category

class SearchModelImpl(val context: Context) : SearchModel, SearchDataFetcher.SearchResultCallback {
    private val _searchResult = MutableLiveData<ArrayList<FileInfo>>()

    val searchResult : LiveData<ArrayList<FileInfo>>
    get() = _searchResult

    private val searchDataFetcher = SearchDataFetcher(this)

    override fun searchData(path : String, query : String, category: Category) {
        searchDataFetcher.fetchData(context, path, category, query)
    }
    override fun onSearchResultFound(result: ArrayList<FileInfo>) {
       _searchResult.postValue(result)
    }

    override fun emptyQuerySearch() {
        _searchResult.postValue(ArrayList<FileInfo>())
    }
}