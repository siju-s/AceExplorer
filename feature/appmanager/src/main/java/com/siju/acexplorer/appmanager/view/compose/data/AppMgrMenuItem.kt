package com.siju.acexplorer.appmanager.view.compose.data

sealed class AppMgrMenuItem {
    data class ViewMode(val viewMode: com.siju.acexplorer.common.ViewMode) : AppMgrMenuItem()
    data object Sort : AppMgrMenuItem()
    data class AppType(val appType: com.siju.acexplorer.appmanager.filter.AppType) :
        AppMgrMenuItem()

    data class AppSource(val source: com.siju.acexplorer.appmanager.filter.AppSource) :
        AppMgrMenuItem()
}