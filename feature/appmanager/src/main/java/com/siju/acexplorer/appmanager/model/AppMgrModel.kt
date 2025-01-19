package com.siju.acexplorer.appmanager.model

import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.common.ViewMode
import kotlinx.coroutines.flow.StateFlow

interface AppMgrModel {

    fun getSystemApps() : ArrayList<AppInfo>
    fun getAllApps() : ArrayList<AppInfo>
    fun getUserApps() : ArrayList<AppInfo>
    fun saveViewMode(viewMode: ViewMode)
    fun getViewMode() : StateFlow<ViewMode>
}