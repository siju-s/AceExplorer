package com.siju.acexplorer.search.model

import com.siju.acexplorer.main.model.groups.Category

interface SearchModel {
    fun searchData(path: String, query: String, category: Category)
    fun emptyQuerySearch()
}