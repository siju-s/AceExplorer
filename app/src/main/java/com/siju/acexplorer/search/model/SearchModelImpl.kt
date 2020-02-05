package com.siju.acexplorer.search.model

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.SearchRecentSuggestions
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category

class SearchModelImpl(val context: Context) : SearchModel, SearchDataFetcher.SearchResultCallback {
    private val _searchResult = MutableLiveData<ArrayList<FileInfo>>()

    val searchResult : LiveData<ArrayList<FileInfo>>
    get() = _searchResult

    private val _recentSearchList = MutableLiveData<ArrayList<String>>()

    val recentSearchList : LiveData<ArrayList<String>>
    get() = _recentSearchList

    private val searchDataFetcher = SearchDataFetcher(this)

    override fun searchData(path : String, query : String, category: Category) {
        searchDataFetcher.cancelSearch()
        _searchResult.postValue(ArrayList())
        searchDataFetcher.fetchData(path, query)
    }

    override fun cancelSearch() {
        searchDataFetcher.cancelSearch()
    }

    override fun onSearchResultFound(result: ArrayList<FileInfo>) {
       _searchResult.postValue(result)
    }

    override fun emptyQuerySearch() {
        _searchResult.postValue(ArrayList())
    }

    override fun getRecentSearches(authority: String) {
        val contentUri = "content://" + authority + '/'.toString() + SearchManager.SUGGEST_URI_PATH_QUERY
        val uri = Uri.parse(contentUri)
        val selectionArgs = arrayOf("")
        val cursor = context.contentResolver.query(uri, null, null, selectionArgs, null)
        onRecentSearchFetched(cursor)
    }

    private fun onRecentSearchFetched(cursor: Cursor?) {
        if (cursor == null) {
            return
        }
        val recentList = java.util.ArrayList<String>()
        if (cursor.moveToFirst()) {
            val colIdx = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)
            do {
                val suggestion = cursor.getString(colIdx)
                recentList.add(suggestion)
            } while (cursor.moveToNext())
        }
        cursor.close()
        _recentSearchList.postValue(recentList)
    }

    override fun clearRecentSearches() {
        val suggestions = SearchRecentSuggestions(context, SearchSuggestionProvider.AUTHORITY,
                SearchSuggestionProvider.MODE)
        suggestions.clearHistory()
    }
}