package com.siju.acexplorer.home.edit.model

import com.siju.acexplorer.home.model.CategoryEdit
import java.util.*

interface CategoryEditModel {

//    fun getSavedCategories() : ArrayList<HomeLibraryInfo>
//
//    fun getUnsavedCategories() : ArrayList<HomeLibraryInfo>
//
//    fun saveCategories(categories: ArrayList<Category>)
    fun getCategories(): List<CategoryEdit>

    fun saveCategories(categories: ArrayList<Int>)
}