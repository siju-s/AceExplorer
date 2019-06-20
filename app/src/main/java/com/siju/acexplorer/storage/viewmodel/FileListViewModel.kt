package com.siju.acexplorer.storage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.model.StorageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FileListViewModel(private val storageModel: StorageModel) : ViewModel() {

    private val _fileData = MutableLiveData<ArrayList<FileInfo>>()

    val fileData: LiveData<ArrayList<FileInfo>>
        get() = _fileData

    val category = MutableLiveData<Category>()

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun loadData(path: String?, category: Category?) {
        Log.e(this.javaClass.name, "loadData: path $path , category $category")
        category?.let {
            uiScope.launch(Dispatchers.IO) {
                _fileData.postValue(storageModel.loadData(path, category))
            }
        }
    }

    fun getViewMode() = storageModel.getViewMode()

    fun setCategory(categoryValue: Category?) {
         category.postValue(categoryValue)
    }
}