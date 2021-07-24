package com.siju.acexplorer.common

import android.util.SparseBooleanArray
import androidx.lifecycle.LiveData

interface MultiSelection {

    fun selectedItemCount() : LiveData<Int>
    fun toggleSelection(position : Int)
    fun selectAll(size : Int)
    fun clearSelection()
    fun isSelectionMode() : Boolean
    fun setListener(listener : Listener)
    fun isSelected(position: Int) : Boolean
    fun getSelectedItemCount() : Int
    fun getSelectedItems(): SparseBooleanArray

    interface Listener {
        fun onSelectionChanged(position: Int)
        fun onNoItemsChecked()
        fun onAllItemsSelected()
    }
}