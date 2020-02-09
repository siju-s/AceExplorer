package com.siju.acexplorer.home.edit.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.home.edit.model.CategoryEditModel
import com.siju.acexplorer.home.edit.model.CategoryEditModelImpl
import com.siju.acexplorer.home.edit.model.CategoryEditType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CategoryEditViewModel(private val
                            categoryEditModel: CategoryEditModel) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _categories = MutableLiveData<ArrayList<CategoryEditModelImpl.DataItem>>()

    val categories: LiveData<ArrayList<CategoryEditModelImpl.DataItem>>
        get() = _categories

    private val _itemsEdited = MutableLiveData<Boolean>()

    val itemsEdited: LiveData<Boolean>
        get() = _itemsEdited

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

    fun removeCategory(item: CategoryEditModelImpl.DataItem.Content, checkedItemCount: Int) {
        Log.d("CategoryEditVM", "Remove category:$item, checkedCount : $checkedItemCount")
        val categories = _categories.value
        categories?.remove(item)
        val newItem = item
        newItem.categoryEdit.checked = false
        newItem.categoryEdit.headerType = CategoryEditType.OTHER
        categories?.add(checkedItemCount + 1, newItem) //Extra 1 for headerType
        _itemsEdited.postValue(true)
        _categories.postValue(categories)
    }

    fun addCategory(item: CategoryEditModelImpl.DataItem.Content, checkedItemCount: Int) {
        Log.d("CategoryEditVM", "addCategory:$item, checkedCount : $checkedItemCount")
        val categories = _categories.value
        categories?.remove(item)
        val newItem = item
        newItem.categoryEdit.checked = true
        newItem.categoryEdit.headerType = CategoryEditType.SAVED
        categories?.add(checkedItemCount + 1, newItem) //Extra 1 for headerType
        _itemsEdited.postValue(true)
        _categories.postValue(categories)
    }

    fun setItemEditComplete() {
        _itemsEdited.value = false
    }
}

