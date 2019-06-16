package com.siju.acexplorer.storage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.model.StoragesModel

class FileListViewModel(private val storagesModel: StoragesModel) : ViewModel() {

    val fileData = MutableLiveData<ArrayList<FileInfo>>()

    fun loadData(path : String?, category: Category?) {
        storagesModel.loadData(path, category)
    }
}