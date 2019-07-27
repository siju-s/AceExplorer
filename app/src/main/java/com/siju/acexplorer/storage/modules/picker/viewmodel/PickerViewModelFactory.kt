package com.siju.acexplorer.storage.modules.picker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.storage.modules.picker.model.PickerModel

class PickerViewModelFactory(private val pickerModel: PickerModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PickerViewModel::class.java)) {
            return PickerViewModel(pickerModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}