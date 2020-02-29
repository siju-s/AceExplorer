package com.siju.acexplorer.storage.view

import com.siju.acexplorer.main.model.groups.Category

interface BaseListAdapter {

    fun setMultiSelectionHelper(multiSelectionHelper: MultiSelectionHelper)
    fun getMultiSelectionHelper() : MultiSelectionHelper?
    fun setDraggedPosition(pos: Int)
    fun clearDragPosition()
    fun setMainCategory(category: Category?)
    fun refresh()
}