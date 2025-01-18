package com.siju.acexplorer.common.compose.data

sealed class MenuItem {
    data class ViewMode(val viewMode: com.siju.acexplorer.common.ViewMode) : MenuItem()
    data object Sort : MenuItem()
}