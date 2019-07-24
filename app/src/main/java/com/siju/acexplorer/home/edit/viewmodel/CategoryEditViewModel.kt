package com.siju.acexplorer.home.edit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.home.edit.model.CategoryEditModel
import com.siju.acexplorer.home.model.CategoryEdit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class CategoryEditViewModel(private val
        categoryEditModel: CategoryEditModel) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _categories = MutableLiveData<List<CategoryEdit>>()

    val categories : LiveData<List<CategoryEdit>>
    get() = _categories

    fun fetchCategories() {
        uiScope.launch(Dispatchers.IO) {
            _categories.postValue(categoryEditModel.getCategories())
        }
    }

    fun saveCategories(categories: ArrayList<Int>) {
        uiScope.launch(Dispatchers.IO) {
            categoryEditModel.saveCategories(categories)
        }
    }
}

