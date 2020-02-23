@file:Suppress("UNCHECKED_CAST")

package com.siju.acexplorer.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.home.model.HomeModel

class HomeViewModelFactory(private val homeModel: HomeModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(homeModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}