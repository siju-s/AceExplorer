package com.siju.acexplorer.home.edit.model

import java.util.*

interface CategoryEditModel {

    fun getCategories(): ArrayList<CategoryEditModelImpl.DataItem>

    fun saveCategories(categories: ArrayList<Int>)
}