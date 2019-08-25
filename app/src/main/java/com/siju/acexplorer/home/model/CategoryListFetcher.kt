package com.siju.acexplorer.home.model

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.siju.acexplorer.R
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.search.helper.SearchUtils
import java.io.File

private const val COUNT_ZERO = 0
private const val TAG = "CategoryListFetcher"

object CategoryListFetcher {

    private val resourceIds = listOf(R.drawable.ic_library_images, R.drawable.ic_library_music,
            R.drawable.ic_library_videos, R.drawable.ic_library_docs,
            R.drawable.ic_library_downloads, R.drawable.ic_library_recents)
    private val labels = arrayOf("Images", "Audio", "Videos", "Docs", "Downloads", "Recent")
    private val defaultCategories = listOf(Category.IMAGE, Category.AUDIO, Category.VIDEO, Category.DOCS,
            Category.DOWNLOADS, Category.RECENT)

    private val totalCategoryList = arrayOf(Category.IMAGE,
            Category.AUDIO,
            Category.VIDEO,
            Category.DOCS,
            Category.DOWNLOADS,
            Category.COMPRESSED,
            Category.FAVORITES,
            Category.PDF,
            Category.APPS,
            Category.LARGE_FILES,
            Category.RECENT)

    fun getCategories(context: Context): ArrayList<HomeLibraryInfo> {
        val homeLibraryInfoList = arrayListOf<HomeLibraryInfo>()
        if (isFirstRun(context)) {
            Log.e(TAG, "getCategories: addDefault")
            addDefaultLibs(context, homeLibraryInfoList)
            persistFirstRunPref(context)
        } else {
            Log.e(TAG, "getCategories: addSavedLibs")
            addSavedLibs(context, homeLibraryInfoList)
        }
        return homeLibraryInfoList
    }

    private fun addDefaultLibs(context: Context, homeLibraryInfoList: ArrayList<HomeLibraryInfo>) {
        val categoryIds = arrayListOf<Int>()
        for ((index, category) in defaultCategories.withIndex()) {
            addToList(HomeLibraryInfo(category, labels[index], resourceIds[index], COUNT_ZERO), homeLibraryInfoList)
            categoryIds.add(category.value)
        }
        saveCategoriesToPrefs(context, categoryIds)
    }

    private fun persistFirstRunPref(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(FileConstants.PREFS_FIRST_RUN, false).apply()
    }

    private fun addToList(homeLibraryInfo: HomeLibraryInfo, homeLibraryInfoList: ArrayList<HomeLibraryInfo>) {
        homeLibraryInfoList.add(homeLibraryInfo)
    }

    private fun addSavedLibs(context: Context, homeLibraryInfoList: ArrayList<HomeLibraryInfo>) {
        val categoryIds = CategorySaver.getSavedCategories(context)
        for (categoryId in categoryIds) {
            val category = CategoryHelper.getCategory(categoryId)
            val resId = CategoryHelper.getResourceIdForCategory(category)
            val name = CategoryHelper.getCategoryName(context, category)
            addToList(HomeLibraryInfo(category, name, resId, COUNT_ZERO), homeLibraryInfoList)
        }
    }

    private fun isFirstRun(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FileConstants.PREFS_FIRST_RUN, true)
    }

    private fun saveCategoriesToPrefs(context: Context, categoryIds: ArrayList<Int>) {
        Log.e(TAG, "saveCategoriesToPrefs:defaultCategories:${categoryIds.toArray()}")
        CategorySaver.saveCategories(context, categoryIds)
    }

    private fun getTotalCategoryList(context: Context): ArrayList<HomeLibraryInfo> {
        val categories = arrayListOf<HomeLibraryInfo>()
        for ((index, category) in totalCategoryList.withIndex()) {
            if (index == 5) {
                addPathCategories(categories)
            }
            categories.add(createCategoryInfo(context, category))
        }
        return categories
    }

    private fun addPathCategories(categories : ArrayList<HomeLibraryInfo>) {
        val cameraCategoryInfo  = createCategoryWithPath(SearchUtils.getCameraDirectory())
        cameraCategoryInfo?.let {
            categories.add(cameraCategoryInfo)
        }
        val screenshotCategoryInfo = createCategoryWithPath(SearchUtils.getScreenshotDirectory())
        screenshotCategoryInfo?.let {
            categories.add(screenshotCategoryInfo)
        }
        val whatsappCategoryInfo = createCategoryWithPath(SearchUtils.getWhatsappDirectory())
        whatsappCategoryInfo?.let {
            categories.add(whatsappCategoryInfo)
        }
        val telegramCategoryInfo = createCategoryWithPath(SearchUtils.getTelegramDirectory())
        telegramCategoryInfo?.let {
            categories.add(telegramCategoryInfo)
        }
    }

    private fun createCategoryWithPath(path: String?): HomeLibraryInfo? {
        if (File(path).exists()) {
            return HomeLibraryInfo.createCategoryWithPath(Category.FILES, "Whatsapp", 0, 0, path)
        }
        return null
    }

    fun getUnsavedCategoryList(context: Context): ArrayList<HomeLibraryInfo> {
        val totalCategories = getTotalCategoryList(context)
        val savedCategories: List<Category> = getCategories(context).map { it.category }
        val unsavedCategories = arrayListOf<HomeLibraryInfo>()
        for (category in totalCategories) {
            if (!savedCategories.contains(category.category)) {
                unsavedCategories.add(category)
            }
        }
        return unsavedCategories
    }

    private fun createCategoryInfo(context: Context, category: Category): HomeLibraryInfo {
        return HomeLibraryInfo(category, CategoryHelper.getCategoryName(context, category),
                CategoryHelper.getResourceIdForCategory(category))
    }


}