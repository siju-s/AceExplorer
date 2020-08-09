package com.siju.acexplorer.main.model.data.recent

import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.helper.FileUtils.isApk
import com.siju.acexplorer.main.model.helper.SortHelper
import java.util.*

private const val MAX_RECENT_DAYS = 15 // 15 days

object RecentUtils {

    fun getRecentTimeSelectionArgument(): String {
        val currentTimeMs = Calendar.getInstance()
        val pastDateCalendar = Calendar.getInstance()
        pastDateCalendar.add(Calendar.DATE, -MAX_RECENT_DAYS)
        pastDateCalendar.set(Calendar.HOUR_OF_DAY, 0)
        return MediaStore.Files.FileColumns.DATE_MODIFIED + " BETWEEN " + pastDateCalendar.timeInMillis/1000 + " AND " + currentTimeMs.timeInMillis/1000
    }

    fun getImagesMediaType(): String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
    }

    fun getAudioMediaType(): String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
    }

    fun getVideosMediaType(): String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
    }

    fun shouldSkipApk(category: Category, extension: String?): Boolean {
        return Category.RECENT_DOCS == category && isApk(extension)
    }

    fun getRecentCategoryList(fileList: ArrayList<FileInfo>): ArrayList<FileInfo> {
        SortHelper.sortRecentCategory(fileList)
        val categories = ArrayList<Category>()
        var count = 0
        val fileInfoList = ArrayList<FileInfo>()
        for (fileInfo in fileList) {
            val newCategory = CategoryHelper.getCategoryForRecentFromExtension(fileInfo.extension)
            if (!categories.contains(newCategory)) {
                count = 1
                val itemFileInfo = FileInfo(newCategory,
                        CategoryHelper.getSubCategoryForRecentFromExtension(
                                fileInfo.extension),
                        count)
                fileInfoList.add(itemFileInfo)
                categories.add(newCategory)
            } else {
                count++
                fileInfoList[categories.indexOf(newCategory)].count = count
            }
        }
        return fileInfoList
    }
}