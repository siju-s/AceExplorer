/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siju.acexplorer.main.model.helper

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.model.ZipModel
import java.io.File
import java.util.*

object SortHelper {
    val comparatorByNameZip: Comparator<in FileInfo> = Comparator { file1, file2 ->
        // sort folders first
        if (file1.isDirectory && !file2.isDirectory) {
            return@Comparator -1
        }
        if (!file1.isDirectory && file2.isDirectory) {
            1
        } else compareFileNames(file1.fileName, file2.fileName)
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

    val comparatorByNameZipViewer: Comparator<in ZipModel> = Comparator { file1, file2 ->
        // sort folders first
        if (file1.isDirectory && !file2.isDirectory) {
            return@Comparator -1
        }
        if (!file1.isDirectory && file2.isDirectory) {
            1
        } else compareFileNames(file1.name, file2.name)
        // here both are folders or both are files : sort alpha
    }

    fun sortFiles(files: ArrayList<FileInfo>, sortMode: Int): ArrayList<FileInfo> {
        when (sortMode) {
            0 -> Collections.sort(files, comparatorByName)
            1 -> Collections.sort(files, comparatorByNameDesc)
            2 -> Collections.sort(files, comparatorByType)
            3 -> Collections.sort(files, comparatorByTypeDesc)
            4 -> Collections.sort(files, comparatorBySize)
            5 -> Collections.sort(files, comparatorBySizeDesc)
            6 -> Collections.sort(files, comparatorByDate)
            7 -> Collections.sort(files, comparatorByDateDesc)
        }
        return files
    }


    fun sortRecentCategory(files: ArrayList<FileInfo>) {
        Collections.sort(files, comparatorRecentCategory)
    }

    private val comparatorByName: Comparator<in FileInfo> = Comparator { file1, file2 ->
        if (file1.isDirectory && !file2.isDirectory) {
            return@Comparator -1
        }
        if (!file1.isDirectory && file2.isDirectory) {
            1
        } else compareFileNames(file1.fileName, file2.fileName)
    }
    private val comparatorByNameDesc: Comparator<in FileInfo> = Comparator { file1, file2 ->
        if (file1.isDirectory && !file2.isDirectory) {
            return@Comparator -1
        }
        if (!file1.isDirectory && file2.isDirectory) {
            1
        } else compareFileNamesDesc(file1.fileName, file2.fileName)
    }
    private val comparatorBySize: Comparator<in FileInfo> = Comparator { file1, file2 ->
        if (file1.isDirectory && !file2.isDirectory) {
            return@Comparator -1
        }
        if (!file1.isDirectory && file2.isDirectory) {
            return@Comparator 1
        }
        val filePath1 = file1.filePath
        val filePath2 = file2.filePath
        if (filePath1 == null && filePath2 == null) {
            return@Comparator 0
        }
        else if (filePath1 == null) {
            return@Comparator -1
        }
        else if (filePath2 == null) {
            return@Comparator 1
        }

        val first = getSize(File(filePath1))
        val second = getSize(File(filePath2))
        first.compareTo(second)
    }
    private val comparatorBySizeDesc: Comparator<in FileInfo> = Comparator { file1, file2 ->
        if (file1.isDirectory && !file2.isDirectory) {
            return@Comparator -1
        }
        if (!file1.isDirectory && file2.isDirectory) {
            return@Comparator 1
        }
        val filePath1 = file1.filePath
        val filePath2 = file2.filePath
        if (filePath1 == null && filePath2 == null) {
            return@Comparator 0
        }
        else if (filePath1 == null) {
            return@Comparator 1
        }
        else if (filePath2 == null) {
            return@Comparator -1
        }
        val first = getSize(File(filePath1))
        val second = getSize(File(filePath2))
        second.compareTo(first)
    }

    private val comparatorRecentCategory: Comparator<in FileInfo> = Comparator { file1, file2 ->
        val category1 = file1?.category
        val category2 = file2?.category
        // Expected order is Images, Audio, Video, Docs, Apps
        if (Category.IMAGE == category1 && Category.AUDIO == category2) {
            -1
        } else if (Category.IMAGE == category1 && Category.VIDEO == category2) {
            -1
        } else if (Category.IMAGE == category1 && Category.DOCS == category2) {
            -1
        } else if (Category.IMAGE == category1 && Category.APPS == category2) {
            -1
        } else if (Category.AUDIO == category1 && Category.IMAGE == category2) {
            1
        } else if (Category.AUDIO == category1 && Category.VIDEO == category2) {
            -1
        } else if (Category.AUDIO == category1 && Category.DOCS == category2) {
            -1
        } else if (Category.AUDIO == category1 && Category.APPS == category2) {
            -1
        } else if (Category.VIDEO == category1 && Category.IMAGE == category2) {
            1
        } else if (Category.VIDEO == category1 && Category.AUDIO == category2) {
            1
        } else if (Category.VIDEO == category1 && Category.DOCS == category2) {
            -1
        } else if (Category.VIDEO == category1 && Category.APPS == category2) {
            -1
        } else if (Category.DOCS == category1 && Category.IMAGE == category2) {
            1
        } else if (Category.DOCS == category1 && Category.AUDIO == category2) {
            1
        } else if (Category.DOCS == category1 && Category.VIDEO == category2) {
            1
        } else if (Category.DOCS == category1 && Category.APPS == category2) {
            -1
        } else if (category1 == category2) {
            0
        } else if (Category.APPS == category1 || Category.APPS == category2) {
            1
        } else {
            -1
        }
    }

    private fun getSize(file: File): Long {
        var size: Long = 0
        if (file.isFile) {
            size = file.length()
        } else if (file.isDirectory) {
            val list = file.listFiles()
            if (list != null) {
                size = list.size.toLong()
            }
        }
        return size
    }

    private val comparatorByType: Comparator<in FileInfo> = Comparator { file1, file2 ->
        var fileName1 = file1.fileName
        var fileName2 = file2.fileName

        if (fileName1 == null && fileName2 == null) {
            return@Comparator 0
        }
        else if (fileName1 == null) {
            return@Comparator -1
        }
        else if (fileName2 == null) {
            return@Comparator 1
        }

        val dotIndex1 = if (fileName1.isNullOrEmpty()) {
            -1
        }
        else {
            fileName1.lastIndexOf('.')
        }
        val dotIndex2 = if (fileName2.isNullOrEmpty()) {
            -1
        }
        else {
            fileName2.lastIndexOf('.')
        }
        when {
            dotIndex1 == -1 == (dotIndex2 == -1) -> { // both or neither
                fileName1 = fileName1.substring(dotIndex1 + 1)
                fileName2 = fileName2.substring(dotIndex2 + 1)
                fileName1.lowercase().compareTo(fileName2.lowercase())
            }
            dotIndex1 == -1 -> { // only s2 has an extension, so s1 goes first
                -1
            }
            else -> { // only s1 has an extension, so s1 goes second
                1
            }
        }
    }
    private val comparatorByTypeDesc: Comparator<in FileInfo> = Comparator { file1, file2 ->
        var fileName2 = file2.fileName
        var fileName1 = file1.fileName
        if (fileName1 == null && fileName2 == null) {
            return@Comparator 0
        }
        else if (fileName1 == null) {
            return@Comparator 1
        }
        else if (fileName2 == null) {
            return@Comparator -1
        }

        val dotIndex2 = fileName2.lastIndexOf('.')
        val dotIndex1 = fileName1.lastIndexOf('.')
        when {
            dotIndex2 == -1 == (dotIndex1 == -1) -> { // both or neither
                fileName2 = fileName2.substring(dotIndex2 + 1)
                fileName1 = fileName1.substring(dotIndex1 + 1)
                fileName2.lowercase().compareTo(fileName1.lowercase())
            }
            dotIndex2 == -1 -> { // only s2 has an extension, so s1 goes first
                -1
            }
            else -> { // only s1 has an extension, so s1 goes second
                1
            }
        }
    }
    private val comparatorByDate: Comparator<in FileInfo> = Comparator { file1, file2 ->
        val filePath1 = file1.filePath
        val filePath2 = file2.filePath
        if (filePath1 == null && filePath2 == null) {
            return@Comparator 0
        }
        else if (filePath1 == null) {
            return@Comparator -1
        }
        else if (filePath2 == null) {
            return@Comparator 1
        }

        val date1 = File(filePath1).lastModified()
        val date2 = File(filePath2).lastModified()
        date1.compareTo(date2)
    }
    private val comparatorByDateDesc: Comparator<in FileInfo> = Comparator { file1, file2 ->
        val filePath1 = file1.filePath
        val filePath2 = file2.filePath
        if (filePath1 == null && filePath2 == null) {
            return@Comparator 0
        }
        else if (filePath1 == null) {
            return@Comparator 1
        }
        else if (filePath2 == null) {
            return@Comparator -1
        }
        val date1 = File(filePath1).lastModified()
        val date2 = File(filePath2).lastModified()
        date2.compareTo(date1)
    }
}