package com.siju.acexplorer.home.edit.model

import java.util.*

interface CategoryEditModel {

//    fun getSavedCategories() : ArrayList<HomeLibraryInfo>
//
//    fun getUnsavedCategories() : ArrayList<HomeLibraryInfo>
//
//    fun saveCategories(categories: ArrayList<Category>)
    fun getCategories(): ArrayList<CategoryEditModelImpl.DataItem>

    fun saveCategories(categories: ArrayList<Int>)
}