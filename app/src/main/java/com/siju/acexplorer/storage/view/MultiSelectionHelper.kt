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

    fun selectAll(position: Int) {
        selectedItems.put(position, true)
    }

    fun unselectAll() {
        clearSelection()
    }

    private fun selectView(position: Int, value: Boolean) {
        if (value) {
            selectedItems.put(position, value)
        }
        else {
            selectedItems.delete(position)
        }
        multiSelectionListener?.refresh()
    }

    fun clearSelection() {
        selectedItems = SparseBooleanArray()
        multiSelectionListener?.refresh()
    }

    fun getSelectedCount() = selectedItems.size()

    fun setMultiSelectionListener(listener: MultiSelectionListener) {
        this.multiSelectionListener = listener
    }

    fun isSelected(position: Int) = selectedItems[position]

    fun hasSelectedItems() = selectedItems.size() > 0

    fun refresh() {
        multiSelectionListener?.refresh()
    }

    interface MultiSelectionListener {
        fun refresh()
    }

}