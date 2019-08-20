package com.siju.acexplorer.search.model

import android.content.Context
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.FileDataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.RootHelper
import java.io.File

class SearchDataFetcher(private val searchResultCallback: SearchResultCallback) {
    private var filesList = ArrayList<FileInfo>()

    fun fetchData(context: Context, path: String?, category: Category, query: String) {
        filesList = ArrayList()
        searchFile(path, query)
    }

    private fun searchFile(path: String?, query: String) {
        val file = File(path)
        if (file.canRead()) {
             getMatchingFiles(file, query)
        }
    }

    private fun getMatchingFiles(sourceFile: File, query: String) {
        val listFiles = sourceFile.listFiles() ?: return
        for (file in listFiles) {
            Log.w("SearchDataFetcher", "getMatchingFiles : file:${file.name}, query:$query")
            if (isSearchResultFound(file, query)) {
                Log.e("SearchDataFetcher", "FOUND : file:${file.name}, query:$query")
                val filePath = file.absolutePath
                var isDirectory = false
                val size: Long
                var extension: String? = null
                var category = Category.FILES

                if (file.isDirectory) {
                    isDirectory = true
                    size = FileDataFetcher.getDirectorySize(file)
                } else {
                    size = file.length()
                    extension = FileUtils.getExtension(filePath)
                    category = FileUtils.getCategoryFromExtension(extension)
                }
                val date = file.lastModified()
                val fileInfo = FileInfo(category, file.name, filePath, date, size,
                        isDirectory, extension, RootHelper.parseFilePermission(file), false)
                filesList.add(fileInfo)
                searchResultCallback.onSearchResultFound(filesList)
                if (isDirectory) {
                    getMatchingFiles(file, query)
                }
            }
            else {
                if (file.isDirectory) {
                    getMatchingFiles(file, query)
                }
            }
        }
    }

    private fun isSearchResultFound(file: File, query: String) =
            file.name.toLowerCase().contains(query.toLowerCase())

    interface SearchResultCallback {
        fun onSearchResultFound(result : ArrayList<FileInfo>)
    }
}