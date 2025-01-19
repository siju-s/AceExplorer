package com.siju.acexplorer.appmanager.filter

import com.siju.acexplorer.appmanager.R

enum class AppSource(var value: Int, val resourceId: Int) {
    ALL(0, R.string.all_apps),
    PLAYSTORE(1, R.string.play_store),
    AMAZON_APPSTORE(2, R.string.amazon_play_store),
    SYSTEM(3, R.string.all_apps),
    UNKNOWN(4, R.string.unknown_source)
}