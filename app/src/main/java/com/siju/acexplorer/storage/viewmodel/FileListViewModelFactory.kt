package com.siju.acexplorer.storage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.storage.model.StorageModel

@Suppress("UNCHECKED_CAST")
class FileListViewModelFactory(private val storageModel: StorageModel, private val searchScreen: Boolean = false) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileListViewModel::class.java)) {
            return FileListViewModel(storageModel, searchScreen) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}