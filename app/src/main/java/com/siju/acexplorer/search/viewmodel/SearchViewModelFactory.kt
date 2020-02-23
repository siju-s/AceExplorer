package com.siju.acexplorer.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.search.model.SearchModel

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(private val searchModel: SearchModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(searchModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}