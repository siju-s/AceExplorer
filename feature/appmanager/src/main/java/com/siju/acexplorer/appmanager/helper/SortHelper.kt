package com.siju.acexplorer.appmanager.helper

import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.common.SortMode
import java.util.*

object SortHelper {

    fun sort(files: ArrayList<AppInfo>, sortMode: SortMode): ArrayList<AppInfo> {
        when (sortMode) {
            SortMode.NAME -> Collections.sort(files, comparatorByName)
            SortMode.NAME_DESC                  -> Collections.sort(files, comparatorByNameDesc)
            SortMode.SIZE           -> Collections.sort(files, comparatorBySizeApk)
            SortMode.SIZE_DESC -> Collections.sort(files, comparatorBySizeApkDesc)
            SortMode.DATE           -> Collections.sort(files, comparatorByDateApk)
            SortMode.DATE_DESC -> Collections.sort(files, comparatorByDateApkDesc)
            else -> Collections.sort(files, comparatorByName)
        }
        return files
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
        return filename1.toLowerCase(Locale.ROOT)
            .compareTo(filename2.toLowerCase(Locale.ROOT))
    }

    private fun compareFileNamesDesc(filename1: String?, filename2: String?): Int {
        if (filename1 == null && filename2 == null) {
            return 0
        } else if (filename1 == null) {
            return -1
        } else if (filename2 == null) {
            return 1
        }
        return filename2.toLowerCase(Locale.ROOT)
            .compareTo(filename1.toLowerCase(Locale.ROOT))
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