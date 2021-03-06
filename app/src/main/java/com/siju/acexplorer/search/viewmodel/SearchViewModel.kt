package com.siju.acexplorer.search.viewmodel

import android.provider.SearchRecentSuggestions
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.search.model.SearchModel
import com.siju.acexplorer.search.model.SearchModelImpl
import com.siju.acexplorer.search.model.SearchSuggestionProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SearchViewModel"
private const val MIN_CHAR_QUERY = 2

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchModel: SearchModel) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val searchResult : LiveData<ArrayList<FileInfo>>
    val recentSearchList : LiveData<ArrayList<String>>

    init {
        searchModel as SearchModelImpl
        searchResult = searchModel.searchResult
        recentSearchList = searchModel.recentSearchList
    }

    fun search(path : String?, query : String?, category: Category = Category.FILES) {
        if (query != null && query.isNotBlank() && query.length >= MIN_CHAR_QUERY) {
            Log.d(TAG, "Search query:$query")
            var rootPath = path
            if (rootPath == null) {
                rootPath = StorageUtils.internalStorage
            }
            uiScope.launch(Dispatchers.IO) {
                searchModel.searchData(rootPath, query, category)
            }
        }
        else if (query.isNullOrBlank()){
            searchModel.cancelSearch()
            searchModel.emptyQuerySearch()
        }
    }

    fun fetchRecentSearches() {
        uiScope.launch(Dispatchers.IO) {
            searchModel.getRecentSearches(SearchSuggestionProvider.AUTHORITY)
        }
    }

    fun clearRecentSearch() {
        uiScope.launch(Dispatchers.IO) {
            searchModel.clearRecentSearches()
        }
    }

    fun saveQuery(searchRecentSuggestions: SearchRecentSuggestions, query: String) {
        searchRecentSuggestions.saveRecentQuery(query, null)
    }
}