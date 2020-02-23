package com.siju.acexplorer.home.edit.model

import android.content.Context
import com.siju.acexplorer.home.model.CategoryEdit
import com.siju.acexplorer.home.model.CategoryListFetcher
import com.siju.acexplorer.home.model.CategorySaver
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.groups.Category

class CategoryEditModelImpl(val context: Context) : CategoryEditModel {

    private fun getSavedCategories(): ArrayList<HomeLibraryInfo> {
        return CategoryListFetcher.getCategories(context)
    }

    private fun getUnsavedCategories(): ArrayList<HomeLibraryInfo> {
        return CategoryListFetcher.getUnsavedCategoryList(context)
    }

    override fun saveCategories(categories: ArrayList<Int>) {
        CategorySaver.saveCategories(context, categories)
    }

    override fun getCategories(): ArrayList<DataItem> {
        val savedCategoryEdit = addSavedCategories()
        val unsavedCategoryEdit = addUnsavedCategories()
        return (savedCategoryEdit + unsavedCategoryEdit) as ArrayList<DataItem>
    }

    private fun addSavedCategories(): List<DataItem> {
        val dataItem = ArrayList<DataItem>()
        dataItem.add(DataItem.Header(CategoryEditType.SAVED))

        val categories = getSavedCategories()
        val savedCategoryItems = categories.map {
            DataItem.Content(CategoryEdit(it.category.value, CategoryEditType.SAVED, true, it.path))
        }
        return dataItem + savedCategoryItems
    }

    private fun addUnsavedCategories(): List<DataItem> {
        val dataItem = ArrayList<DataItem>()
        dataItem.add(DataItem.Header(CategoryEditType.OTHER))

        val unsavedCategoryList = getUnsavedCategories()
        val unsavedCategoryItems = unsavedCategoryList.map {
            DataItem.Content(CategoryEdit(it.category.value, CategoryEditType.OTHER, false, it.path))
        }
        return dataItem + unsavedCategoryItems
    }

    sealed class DataItem {

        data class Content(val categoryEdit: CategoryEdit) : DataItem() {
            override val id: String
                get() {
                    val categoryId = categoryEdit.categoryId
                    return if (categoryId == Category.FILES.value) {
                        categoryEdit.path.toString() + categoryEdit.headerType
                    } else {
                        categoryId.toString() + categoryEdit.headerType
                    }
                }

//            override fun equals(other: Any?): Boolean {
//                if (this === other) return true
//                if (other !is Content) return false
//                if (!super.equals(other)) return false
//
//                if (this.categoryEdit.headerType == other.categoryEdit.headerType) {
//                    return if (this.categoryEdit.categoryId == Category.FILES.value && other.categoryEdit.categoryId == Category.FILES.value) {
//                        this.categoryEdit.path == other.categoryEdit.path
//                    } else {
//                        this.categoryEdit.categoryId == other.categoryEdit.categoryId
//                    }
//                }
//                return false
//            }
//
//            override fun hashCode(): Int {
//                var result = super.hashCode()
//                result = 31 * result + categoryEdit.hashCode()
//                return result
//            }


        }

        data class Header(val headerType: CategoryEditType) : DataItem() {
            override val id: String
                get() = Int.MIN_VALUE.toString()

        }

        abstract val id: String

//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is DataItem) return false
//
//            if (this is Header && other is Header) {
//                return this.id == other.id
//            }
//            else if (this is Content && other is Content) {
//                return super.equals(this) == super.equals(other)
//            }
//            else if (this is Header && other is Content) {
//                return false
//            }
//            else if (this is Content && other is Header) {
//                return false
//            }
//            return false
//        }
//
//        override fun hashCode(): Int {
//            return id.hashCode()
//        }


    }


}