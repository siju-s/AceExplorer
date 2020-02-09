package com.siju.acexplorer.storage.view

import android.util.Log
import android.util.SparseBooleanArray
import com.siju.acexplorer.storage.model.RecentTimeData

class MultiSelectionHelper {
    private var multiSelectionListener: MultiSelectionListener? = null
    var selectedItems = SparseBooleanArray()
    private val recentItemSelectedList = hashMapOf<Int, RecentTimeData.HeaderType>()

    fun toggleSelection(position: Int, longPress: Boolean = false, recentHeaderType: RecentTimeData.HeaderType? = null) {
        if (longPress) {
            selectView(position, true, recentHeaderType)
        }
        else {
            selectView(position, !selectedItems.get(position), recentHeaderType)
        }
    }

    fun selectAll(position: Int, recentHeaderType: RecentTimeData.HeaderType? = null) {
        selectedItems.put(position, true)
        recentHeaderType?.let {
                recentItemSelectedList.put(position, recentHeaderType)
        }
    }

    fun unselectAll() {
        clearSelection()
    }

    private fun selectView(position: Int, value: Boolean, recentHeaderType: RecentTimeData.HeaderType? = null) {
        if (value) {
            selectedItems.put(position, value)
        }
        else {
            selectedItems.delete(position)
        }
        recentHeaderType?.let {
            if (value) {
                recentItemSelectedList.put(position, recentHeaderType)
            }
            else {
                recentItemSelectedList.remove(position)
            }
        }
        multiSelectionListener?.refresh()
    }

    fun isCompleteRecentHeaderSelected(headerType: RecentTimeData.HeaderType, count : Int): Boolean {
        var selectedCount = 0
        Log.d("Multi", "isCompleteRecentHeaderSelected:count:$count, selected:${recentItemSelectedList.size}")
        for ((_, header) in recentItemSelectedList) {
            if (header == headerType) {
                selectedCount++
            }
            if (selectedCount == count) {
                return true
            }
        }
        return false
    }

    fun clearSelection() {
        selectedItems = SparseBooleanArray()
        multiSelectionListener?.refresh()
        recentItemSelectedList.clear()
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