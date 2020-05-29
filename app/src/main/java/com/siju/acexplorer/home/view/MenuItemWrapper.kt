package com.siju.acexplorer.home.view

import android.view.MenuItem

class MenuItemWrapper {
    private var menuItem : MenuItem? = null


    fun setMenuItem(item : MenuItem) {
        this.menuItem = item
    }

    fun getMenuItem(): MenuItem? {
        return menuItem
    }

}