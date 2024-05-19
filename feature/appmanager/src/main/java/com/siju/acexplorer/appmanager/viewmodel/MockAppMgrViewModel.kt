package com.siju.acexplorer.appmanager.viewmodel

class MockAppMgrViewModel : AppMgr {
    override fun isActionModeActive(): Boolean {
        return false
    }
}