package com.siju.acexplorer.appmanager.helper

import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.common.SortMode
import java.util.*

object SortHelper {

    fun sort(files: ArrayList<AppInfo>, sortMode: SortMode): ArrayList<AppInfo> {
        val newList = ArrayList(files)

        when (sortMode) {
            SortMode.NAME -> Collections.sort(newList, comparatorByName)
            SortMode.NAME_DESC                  -> Collections.sort(newList, comparatorByNameDesc)
            SortMode.SIZE           -> Collections.sort(newList, comparatorBySizeApk)
            SortMode.SIZE_DESC -> Collections.sort(newList, comparatorBySizeApkDesc)
            SortMode.DATE           -> Collections.sort(newList, comparatorByDateApk)
            SortMode.DATE_DESC -> Collections.sort(newList, comparatorByDateApkDesc)
            else -> Collections.sort(newList, comparatorByName)
        }
        return newList
    }

    private val comparatorByName: Comparator<in AppInfo> = Comparator { file1, file2 ->
       return@Comparator compareFileNames(file1.name, file2.name)
    }
    private val comparatorByNameDesc: Comparator<in AppInfo> = Comparator { file1, file2 ->
        return@Comparator compareFileNamesDesc(file1.name, file2.name)
    }

    private fun compareFileNames(filename1: String?, filename2: String?): Int {
        if (filename1 == null && filename2 == null) {
            return 0
        } else if (filename1 == null) {
            return 1
        } else if (filename2 == null) {
            return -1
        }
        return filename1.lowercase()
            .compareTo(filename2.lowercase())
    }

    private fun compareFileNamesDesc(filename1: String?, filename2: String?): Int {
        if (filename1 == null && filename2 == null) {
            return 0
        } else if (filename1 == null) {
            return -1
        } else if (filename2 == null) {
            return 1
        }
        return filename2.lowercase()
            .compareTo(filename1.lowercase())
    }

    private val comparatorBySizeApk: Comparator<in AppInfo> = Comparator { file1, file2 ->
        val first = file1.size
        val second = file2.size
        first.compareTo(second)
    }
    private val comparatorBySizeApkDesc: Comparator<in AppInfo> = Comparator { file1, file2 ->
        val first = file1.size
        val second = file2.size
        second.compareTo(first)
    }
    private val comparatorByDateApk: Comparator<in AppInfo> = Comparator { file1, file2 ->
        val date1 = file1.updatedDate
        val date2 = file2.updatedDate
        date1.compareTo(date2)
    }
    private val comparatorByDateApkDesc: Comparator<in AppInfo> = Comparator { file1, file2 ->
        val date1 = file1.updatedDate
        val date2 = file2.updatedDate
        date2.compareTo(date1)
    }
}