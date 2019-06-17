package com.siju.acexplorer.main.model.data

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.Category.FILES
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.FileUtils.getCategoryFromExtension
import com.siju.acexplorer.main.model.helper.RootHelper
import com.siju.acexplorer.main.model.helper.RootHelper.parseFilePermission
import com.siju.acexplorer.main.model.helper.SortHelper.sortFiles
import com.siju.acexplorer.main.model.root.RootUtils
import java.io.File

class FileDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        return fetchFiles(path, getSortMode(context), canShowHiddenFiles(context),
                          RootUtils.isRooted(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return 0
    }

    private fun fetchFiles(path: String?, sortMode: Int, showHidden: Boolean,
                           isRooted: Boolean): ArrayList<FileInfo> {
        if (path == null) {
            return ArrayList<FileInfo>()
        }
        val fileInfoList = getFilesList(path, isRooted, showHidden)
        sortFiles(fileInfoList, sortMode)
        return fileInfoList
    }

    companion object {
        fun getFilesList(path: String, root: Boolean,
                         showHidden: Boolean): ArrayList<FileInfo> {
            val fileInfoArrayList: ArrayList<FileInfo>
            val file = File(path)
            fileInfoArrayList = if (file.canRead()) {
                getNonRootedList(file, showHidden)
            }
            else {
                RootHelper.getRootedList(path, root, showHidden)
            }
            return fileInfoArrayList
        }

        private fun getNonRootedList(file: File, showHidden: Boolean): ArrayList<FileInfo> {
            val listFiles = file.listFiles()
            return getFilesList(listFiles, showHidden)
        }

        private fun getFilesList(listFiles: Array<File>?,
                                 showHidden: Boolean): ArrayList<FileInfo> {
            val filesList = ArrayList<FileInfo>()
            if (listFiles == null) {
                return filesList
            }
            for (file in listFiles) {
                val filePath = file.absolutePath
                var isDirectory = false
                val size: Long
                var extension: String? = null
                var category = FILES

                // Don't show hidden files by default
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue
                }
                if (file.isDirectory) {
                    isDirectory = true
                    size = getDirectorySize(file)
                }
                else {
                    size = file.length()
                    extension = FileUtils.getExtension(filePath)
                    category = getCategoryFromExtension(extension)
                }
                val date = file.lastModified()

                val fileInfo = FileInfo(category, file.name, filePath, date, size,
                                        isDirectory, extension, parseFilePermission(file), false)
                filesList.add(fileInfo)
            }
            return filesList
        }

        private fun getDirectorySize(file: File): Long {
            var childFileListSize = 0
            val list = file.list()
            if (list != null) {
                childFileListSize = list.size
            }
            return childFileListSize.toLong()
        }
    }

}
