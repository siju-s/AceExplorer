package com.siju.acexplorer.appmanager.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class MultiSelectionImpl @Inject constructor() : MultiSelection {
    private val selectedItemCount = MutableLiveData<Int>()
    private val _selectedItems = MutableStateFlow(setOf<String>())
    private val selectedItems : StateFlow<Set<String>> = _selectedItems
    private lateinit var listener : MultiSelection.Listener

    override fun setListener(listener: MultiSelection.Listener) {
        this.listener = listener
    }

    override fun selectedItemCount(): LiveData<Int> {
        return selectedItemCount
    }

    override fun toggleSelection(packageName: String) {
        _selectedItems.update { selected ->
            if (selected.contains(packageName)) {
                selected - packageName
            } else {
                selected + packageName
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

    override fun selectAll(packageNames: Collection<String>) {
        _selectedItems.value = packageNames.toSet()
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

    override fun isSelected(packageName: String): Boolean {
        return _selectedItems.value.contains(packageName)
    }

    override fun getSelectedItems(): StateFlow<Set<String>> {
        return selectedItems
    }
}
