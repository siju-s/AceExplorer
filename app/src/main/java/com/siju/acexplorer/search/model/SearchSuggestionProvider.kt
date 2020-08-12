package com.siju.acexplorer.search.model


import android.content.SearchRecentSuggestionsProvider
import com.siju.acexplorer.BuildConfig

class SearchSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".SearchSuggestionProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }
}