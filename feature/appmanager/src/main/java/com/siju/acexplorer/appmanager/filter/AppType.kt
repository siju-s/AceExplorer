package com.siju.acexplorer.appmanager.filter

import com.siju.acexplorer.appmanager.R

enum class AppType(val value : Int, val resourceId : Int) {
    ALL_APPS(0, R.string.all_apps),
    USER_APP(1, R.string.user_apps),
    SYSTEM_APP(2, R.string.preinstalled_apps)
}