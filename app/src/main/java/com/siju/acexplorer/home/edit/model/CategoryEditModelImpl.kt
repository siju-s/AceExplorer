package com.siju.acexplorer.home.edit.model

import android.content.Context
import com.siju.acexplorer.home.model.CategoryEdit
import com.siju.acexplorer.home.model.CategoryListFetcher
import com.siju.acexplorer.home.model.CategorySaver

class CategoryEditModelImpl(val context: Context) : CategoryEditModel {

//    override fun getSavedCategories(): ArrayList<HomeLibraryInfo> {
//        return CategoryListFetcher.getCategories(context)
//    }
//
//    override fun getUnsavedCategories(): ArrayList<HomeLibraryInfo> {
//       return CategoryListFetcher.getUnsavedCategoryList(context)
//    }
//
    override fun saveCategories(categories: ArrayList<Int>) {
        CategorySaver.saveCategories(context, categories)
    }

    override fun getCategories(): List<CategoryEdit> {
        val categories =  CategoryListFetcher.getCategories(context)
        val savedCategoryEdit = categories.map { CategoryEdit(it.category.value, true) }
        val unsavedCategoryEdit = CategoryListFetcher.getUnsavedCategoryList(context).map {
            CategoryEdit(it.category.value)
        }
        return savedCategoryEdit + unsavedCategoryEdit
    }
}