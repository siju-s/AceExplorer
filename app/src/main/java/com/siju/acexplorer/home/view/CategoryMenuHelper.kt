package com.siju.acexplorer.home.view

import android.view.View

interface CategoryMenuHelper {

    fun getCategoryView() : View?
    fun disableTab()
    fun enableTab()
    fun setToolbarTitle()
}