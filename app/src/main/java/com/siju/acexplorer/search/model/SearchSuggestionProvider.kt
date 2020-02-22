package com.siju.acexplorer.search.model


import android.content.SearchRecentSuggestionsProvider

class SearchSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.siju.acexplorer.SearchSuggestionProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }
}