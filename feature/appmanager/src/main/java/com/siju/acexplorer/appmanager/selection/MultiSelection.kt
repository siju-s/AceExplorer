package com.siju.acexplorer.appmanager.selection

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.StateFlow

interface MultiSelection {

    fun selectedItemCount() : LiveData<Int>
    fun toggleSelection(packageName: String)
    fun selectAll(packageNames: Collection<String>)
    fun clearSelection()
    fun isSelectionMode() : Boolean
    fun setListener(listener : Listener)
    fun isSelected(packageName: String): Boolean
    fun getSelectedItemCount() : Int
    fun getSelectedItems(): StateFlow<Set<String>>

    interface Listener {
        fun onSelectionChanged(position: Int)
        fun onNoItemsChecked()
        fun onAllItemsSelected()
    }
}
