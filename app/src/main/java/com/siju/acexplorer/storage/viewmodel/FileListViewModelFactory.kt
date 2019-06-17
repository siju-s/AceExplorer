package com.siju.acexplorer.storage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.storage.model.StorageModel

class FileListViewModelFactory(private val storageModel: StorageModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileListViewModel::class.java)) {
            return FileListViewModel(storageModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}