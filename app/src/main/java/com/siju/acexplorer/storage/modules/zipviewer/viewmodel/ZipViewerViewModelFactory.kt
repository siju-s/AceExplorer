package com.siju.acexplorer.storage.modules.zipviewer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback
import com.siju.acexplorer.storage.modules.zipviewer.model.ZipViewerModel

class ZipViewerViewModelFactory(private val model: ZipViewerModel, private val zipViewerCallback: ZipViewerCallback) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZipViewerViewModel::class.java)) {
            return ZipViewerViewModel(model, zipViewerCallback) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}