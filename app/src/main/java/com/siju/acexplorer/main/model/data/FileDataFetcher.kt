package com.siju.acexplorer.main.model.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.data.DataFetcher.Companion.canShowHiddenFiles
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.Category.FILES
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.FileUtils.getCategoryFromExtension
import com.siju.acexplorer.main.model.helper.RootHelper
import com.siju.acexplorer.main.model.helper.RootHelper.parseFilePermission
import com.siju.acexplorer.main.model.helper.SortHelper.sortFiles
import com.siju.acexplorer.main.model.root.RootUtils
import java.io.File

private const val TAG = "FileDataFetcher"

// DATA field is required to check path. Works fine till Android 12 even though deprecated
@Suppress("Deprecation")
class FileDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        return fetchFiles(path, getSortMode(context), canShowHiddenFiles(context),
                RootUtils.isRooted(context))
    }

    override fun fetchData(context: Context, path: String?, category: Category,
                           ringtonePicker: Boolean): ArrayList<FileInfo> {
        return fetchFiles(path, getSortMode(context), canShowHiddenFiles(context),
                RootUtils.isRooted(context), ringtonePicker)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        Log.d(TAG, "fetchCount:$path")
        return if (path == null) {
            0
        } else {
            getFileCount(path)
        }
    }

    private fun getFileCount(path: String): Int {
        val file = File(path)
        val list: Array<out String>? = file.list()
        return list?.size ?: 0
    }

    private fun fetchFiles(path: String?, sortMode: Int, showHidden: Boolean,
                           isRooted: Boolean,
                           ringtonePicker: Boolean = false): ArrayList<FileInfo> {
        Log.d(TAG, "fetchFiles: path:$path, sortMode:$sortMode, hidden:$showHidden")
        if (path == null) {
            return ArrayList()
        }
        val fileInfoList = getFilesList(path, isRooted, showHidden, ringtonePicker)
        sortFiles(fileInfoList, sortMode)
        return fileInfoList
    }

    companion object {
        fun getFilesList(path: String?, root: Boolean,
                         showHidden: Boolean, ringtonePicker: Boolean = false): ArrayList<FileInfo> {
            if (path == null) {
                return arrayListOf()
            }
            val fileInfoArrayList: ArrayList<FileInfo>
            val file = File(path)
            fileInfoArrayList = if (file.canRead()) {
                getAndroid11NonRootedListMedia(file, showHidden, ringtonePicker)
            } else {
                RootHelper.getRootedList(path, root, showHidden)
            }
            return fileInfoArrayList
        }

        private fun getNonRootedList(sourceFile: File, showHidden: Boolean,
                                     ringtonePicker: Boolean = false): ArrayList<FileInfo> {
            val listFiles = sourceFile.listFiles()
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
                } else {
                    size = file.length()
                    extension = FileUtils.getExtension(filePath)
                    category = getCategoryFromExtension(extension)
                    if (isNotRingtoneFile(ringtonePicker, extension)) {
                        continue
                    }
                }
                val date = file.lastModified()

                val fileInfo = FileInfo(category, file.name, filePath, date, size,
                        isDirectory, extension, parseFilePermission(file), false)
                filesList.add(fileInfo)
            }
            return filesList
        }

        private fun getAndroid11NonRootedListMedia(sourceFile: File, showHidden: Boolean,
                                     ringtonePicker: Boolean = false): ArrayList<FileInfo> {

//            val testDir = "/storage/emulated/0/Android"

            val uri: Uri = MediaStore.Files.getContentUri("external")
            val selection = MediaStore.Files.FileColumns.DATA + " LIKE " + "'%${sourceFile.absolutePath}%'" + " AND " +
                    MediaStore.Files.FileColumns.DATA + " NOT LIKE " + "'%${sourceFile.absolutePath}/%/%'"
//                    "(" + MediaStore.Files.FileColumns.RELATIVE_PATH + " = " + "'/'" +
//                    " OR " + MediaStore.Files.FileColumns.RELATIVE_PATH +
//            " = " + "'${sourceFile.name}/'" + ")"
            //MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME + " = " + "'${sourceFile.name}'"
            val selectionArgs = null//arrayOf("%${sourceFile.absolutePath}%")
            val time = System.currentTimeMillis()

            val cursor = AceApplication.appContext.contentResolver.query(uri, null, selection, selectionArgs, null)
            val filesList = ArrayList<FileInfo>()

            Log.d(TAG, "getNonRootedList: count:${cursor?.count}")
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                    val filePath = cursor.getString(columnIndex)

                    if (sourceFile.absolutePath.equals(filePath)) {
                        continue
                    }

                    var isDirectory = false
                    val size: Long
                    var extension: String? = null
                    var category = FILES
                    val file = File(filePath)
                    // Don't show hidden files by default
                    if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                        continue
                    }
                    if (file.isDirectory) {
                        isDirectory = true
                        size = getDirectorySize(file)
                    } else {
                        size = file.length()
                        extension = FileUtils.getExtension(filePath)
                        category = getCategoryFromExtension(extension)
                        if (isNotRingtoneFile(ringtonePicker, extension)) {
                            continue
                        }
                    }
                    val date = file.lastModified()
                    val fileInfo = FileInfo(category, file.name, filePath, date, size,
                        isDirectory, extension, parseFilePermission(file), false)
                    filesList.add(fileInfo)

                } while (cursor.moveToNext())
            }

            cursor?.close()
            Log.d(TAG, "getNonRootedList: timetaken:${System.currentTimeMillis() - time}")
            return filesList
        }

        private fun isNotRingtoneFile(ringtonePicker: Boolean,
                                      extension: String?) =
                ringtonePicker && !FileUtils.isFileMusic(extension)

        fun getDirectorySize(file: File, showHidden: Boolean = false): Long {
            var childFileListSize = 0
            val listFiles: Array<String?>? = if (!showHidden) {
                file.list { _, name ->
                    name != ".nomedia"
                }
            } else {
                file.list()
            }

            if (listFiles != null) {
                childFileListSize = listFiles.size
            }
            return childFileListSize.toLong()
        }
    }

}
