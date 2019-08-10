package com.siju.acexplorer.appmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.appmanager.model.AppDetailModel

class AppDetailViewModelFactory(private val appDetailModel: AppDetailModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppDetailViewModel::class.java)) {
            return AppDetailViewModel(appDetailModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}