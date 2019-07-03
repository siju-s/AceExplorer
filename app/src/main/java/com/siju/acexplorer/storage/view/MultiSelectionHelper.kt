package com.siju.acexplorer.storage.view

import android.util.SparseBooleanArray

class MultiSelectionHelper {
    private var multiSelectionListener: MultiSelectionListener? = null
    var selectedItems = SparseBooleanArray()

    fun toggleSelection(position: Int, longPress: Boolean = false) {
        if (longPress) {
            selectView(position, true)
        }
        else {
            selectView(position, !selectedItems.get(position))
        }
    }

    private fun selectView(position: Int, value: Boolean) {
        if (value) {
            selectedItems.put(position, value)
        }
        else {
            selectedItems.delete(position)
        }
        multiSelectionListener?.onItemSelected()
    }

    fun clearSelection() {
        selectedItems = SparseBooleanArray()
    }

    fun getSelectedCount() = selectedItems.size()

    fun setMultiSelectionListener(listener: MultiSelectionListener) {
        this.multiSelectionListener = listener
    }

    fun isSelected(position: Int) = selectedItems[position]

    fun hasSelectedItems() = selectedItems.size() > 0

    interface MultiSelectionListener {
        fun onItemSelected()
    }

}