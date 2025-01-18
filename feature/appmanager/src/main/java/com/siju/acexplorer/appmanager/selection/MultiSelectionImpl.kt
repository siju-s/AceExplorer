package com.siju.acexplorer.appmanager.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class MultiSelectionImpl @Inject constructor() : MultiSelection {
    private val selectedItemCount = MutableLiveData<Int>()
    private val _selectedItems = MutableStateFlow(setOf<Int>())
    private val selectedItems : StateFlow<Set<Int>> = _selectedItems
    private lateinit var listener : MultiSelection.Listener

    override fun setListener(listener: MultiSelection.Listener) {
        this.listener = listener
    }

    override fun selectedItemCount(): LiveData<Int> {
        return selectedItemCount
    }

    override fun toggleSelection(position: Int) {
        _selectedItems.update { selected ->
            if (selected.contains(position)) {
                selected - position
            } else {
                selected + position
            }
        }
        updateSelectionCount()
        notifySelectionState()
    }

    private fun updateSelectionCount() {
        selectedItemCount.postValue(_selectedItems.value.size)
    }

    private fun notifySelectionState() {
        if (isSelectionMode()) {
            listener.onSelectionChanged(0)
        } else {
            listener.onNoItemsChecked()
        }
    }

    override fun selectAll(size: Int) {
        _selectedItems.value = (0 until size).toSet()
        updateSelectionCount()
        listener.onAllItemsSelected()
    }

    override fun clearSelection() {
        _selectedItems.value = emptySet()
        updateSelectionCount()
        listener.onNoItemsChecked()
    }

    override fun isSelectionMode() = _selectedItems.value.isNotEmpty()

    override fun getSelectedItemCount(): Int {
        return _selectedItems.value.size
    }

    override fun isSelected(position: Int): Boolean {
        return _selectedItems.value.contains(position)
    }

    override fun getSelectedItems(): StateFlow<Set<Int>> {
        return selectedItems
    }
}