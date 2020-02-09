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
            Log.d(TAG, "getCategories: addDefault")
            addDefaultLibs(context, homeLibraryInfoList)
            persistFirstRunPref(context)
        } else {
            Log.d(TAG, "getCategories: addSavedLibs")
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
            val category = Category.valueOf(categoryId)
            val resId = CategoryHelper.getResourceIdForCategory(category)
            val name = CategoryHelper.getCategoryName(context, category)
            category?.let {
                addToList(HomeLibraryInfo(category, name, resId, COUNT_ZERO), homeLibraryInfoList)
            }
        }
    }

    private fun isFirstRun(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FileConstants.PREFS_FIRST_RUN, true)
    }

    private fun saveCategoriesToPrefs(context: Context, categoryIds: ArrayList<Int>) {
        Log.d(TAG, "saveCategoriesToPrefs:defaultCategories:${categoryIds.toArray()}")
        CategorySaver.saveCategories(context, categoryIds)
    }

    private fun getTotalCategoryList(context: Context): ArrayList<HomeLibraryInfo> {
        val categories = arrayListOf<HomeLibraryInfo>()
        for ((index, category) in totalCategoryList.withIndex()) {
            if (index == 5) {
                addPathCategories(context, categories)
            }
            categories.add(createCategoryInfo(context, category))
        }
        return categories
    }

    private fun addPathCategories(context: Context, categories: ArrayList<HomeLibraryInfo>) {
        val cameraCategoryInfo = createCategoryWithPath(context, Category.CAMERA, SearchUtils.getCameraDirectory())
        cameraCategoryInfo?.let {
            categories.add(cameraCategoryInfo)
        }
        val screenshotCategoryInfo = createCategoryWithPath(context, Category.SCREENSHOT, SearchUtils.getScreenshotDirectory())
        screenshotCategoryInfo?.let {
            categories.add(screenshotCategoryInfo)
        }
        val whatsappCategoryInfo = createCategoryWithPath(context, Category.WHATSAPP, SearchUtils.getWhatsappDirectory())
        whatsappCategoryInfo?.let {
            categories.add(whatsappCategoryInfo)
        }
        val telegramCategoryInfo = createCategoryWithPath(context, Category.TELEGRAM, SearchUtils.getTelegramDirectory())
        telegramCategoryInfo?.let {
            categories.add(telegramCategoryInfo)
        }
    }

    private fun createCategoryWithPath(context: Context, category: Category, path: String?): HomeLibraryInfo? {
        if (File(path).exists()) {
            return createCategoryInfo(context, category)
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