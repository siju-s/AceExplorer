package com.siju.acexplorer.common

import android.util.SparseBooleanArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class MultiSelectionImpl @Inject constructor() : MultiSelection {
    private val selectedItemCount = MutableLiveData<Int>()
    private var selectedItems = SparseBooleanArray()
    private lateinit var listener : MultiSelection.Listener

    override fun setListener(listener: MultiSelection.Listener) {
        this.listener = listener
    }

    override fun selectedItemCount(): LiveData<Int> {
        return selectedItemCount
    }

    override fun toggleSelection(position: Int) {
        selectView(position, !selectedItems.get(position))
    }

    override fun selectAll(size: Int) {
        for (i in 0 until size) {
            selectedItems.put(i, true)
        }
        selectedItemCount.postValue(selectedItems.size())
        listener.onAllItemsSelected()
    }

    override fun clearSelection() {
        selectedItems = SparseBooleanArray()
        selectedItemCount.postValue(selectedItems.size())
        listener.onNoItemsChecked()
    }

    private fun selectView(position: Int, value: Boolean) {
        if (value) {
            selectedItems.put(position, value)
        }
        else {
            selectedItems.delete(position)
        }
        selectedItemCount.postValue(selectedItems.size())
        if (!isSelectionMode()) {
            listener.onNoItemsChecked()
        }
        else {
            listener.onSelectionChanged(position)
        }
    }

    override fun isSelectionMode() = selectedItems.size() > 0

    override fun getSelectedItemCount(): Int {
        return selectedItems.size()
    }

    override fun isSelected(position: Int): Boolean {
        return selectedItems[position]
    }

    override fun getSelectedItems(): SparseBooleanArray {
        return selectedItems
    }

}