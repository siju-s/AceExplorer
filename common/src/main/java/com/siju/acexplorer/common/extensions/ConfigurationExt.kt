package com.siju.acexplorer.extensions

import android.content.res.Configuration

fun Configuration?.isLandscape() : Boolean {
    if (this == null) {
        return false
    }
    return this.orientation == Configuration.ORIENTATION_LANDSCAPE
}