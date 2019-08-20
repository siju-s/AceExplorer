package com.siju.acexplorer.search.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.main.model.groups.Category

class SearchModelImpl(val context: Context) : SearchModel, SearchDataFetcher.SearchResultCallback {
    private val _searchResult = MutableLiveData<ArrayList<SearchDataFetcher.SearchDataItem>>()

    val searchResult : LiveData<ArrayList<SearchDataFetcher.SearchDataItem>>
    get() = _searchResult

    private val searchDataFetcher = SearchDataFetcher(this)

    override fun searchData(path : String, query : String, category: Category) {
        searchDataFetcher.fetchData(context, path, category, query)
    }
    override fun onSearchResultFound(result: ArrayList<SearchDataFetcher.SearchDataItem>) {
       _searchResult.postValue(result)
    }

    override fun emptyQuerySearch() {
        _searchResult.postValue(ArrayList<SearchDataFetcher.SearchDataItem>())
    }
}