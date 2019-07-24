package com.siju.acexplorer.home.edit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.home.edit.model.CategoryEditModel

class CategoryEditViewModelFactory(private val categoryEditModel: CategoryEditModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryEditViewModel::class.java)) {
            return CategoryEditViewModel(categoryEditModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}