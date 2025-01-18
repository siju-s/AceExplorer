package com.siju.acexplorer.appmanager.selection

import android.util.SparseBooleanArray
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.StateFlow

interface MultiSelection {

    fun selectedItemCount() : LiveData<Int>
    fun toggleSelection(position : Int)
    fun selectAll(size : Int)
    fun clearSelection()
    fun isSelectionMode() : Boolean
    fun setListener(listener : Listener)
    fun isSelected(position: Int) : Boolean
    fun getSelectedItemCount() : Int
    fun getSelectedItems(): StateFlow<Set<Int>>

    interface Listener {
        fun onSelectionChanged(position: Int)
        fun onNoItemsChecked()
        fun onAllItemsSelected()
    }
}