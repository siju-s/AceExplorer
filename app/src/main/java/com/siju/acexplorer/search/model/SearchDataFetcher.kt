package com.siju.acexplorer.search.model

import android.util.Log
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.FileDataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.RootHelper
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SearchDataFetcher(private val searchResultCallback: SearchResultCallback) {
    private var cancelSearch = false
    private var time1 : Long = 0
    private var searchData = ArrayList<FileInfo>()


    fun fetchData(path: String?, query: String) {
        //TODO 30 Jan 2020 Should use coroutine cancel somehow instead of a flag here
        time1 = System.currentTimeMillis()
        Log.e("SearchDataFetcher", "fetchData query:$query, cancel:$cancelSearch")
        searchData = ArrayList()
        cancelSearch = false
        path?.let {
            searchFile(path, query)
        }
    }

    private fun searchFile(path: String, query: String) {
        val file = File(path)
        if (file.canRead()) {
            getMatchingFiles(file, query, DataFetcher.canShowHiddenFiles(AceApplication.appContext))
        }
        Log.e("SearchDataFetcher", "Search completed, size : ${searchData.size},  time:${System.currentTimeMillis() - time1}")
    }

    fun cancelSearch() {
        cancelSearch = true
    }

    private fun getMatchingFiles(sourceFile: File, query: String, showHidden: Boolean) {
        val listFiles = sourceFile.listFiles() ?: return
        for (file in listFiles) {
            if (cancelSearch) {
                break
            }
//            Log.e("SearchDataFetcher", "getMatchingFiles : file:${file.name}, query:$query, cancel:$cancelSearch")
            if (isSearchResultFound(file, query)) {
//                Log.e("SearchDataFetcher", "FOUND : file:${file.name}, query:$query")
                val filePath = file.absolutePath
                var isDirectory = false
                val size: Long
                var extension: String? = null
                var category = Category.FILES

                // Don't show hidden files by default
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue
                }

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
                createSearchData(fileInfo)
                if (isDirectory) {
                    getMatchingFiles(file, query, DataFetcher.canShowHiddenFiles(AceApplication.appContext))
                }
            } else {
                if (file.isDirectory) {
                    getMatchingFiles(file, query, DataFetcher.canShowHiddenFiles(AceApplication.appContext))
                }
            }
        }
    }

    private fun createSearchData(fileInfo: FileInfo) {
        if (cancelSearch) {
            return
        }
        searchData.add(fileInfo)
        searchResultCallback.onSearchResultFound(searchData)
    }
    private fun isSearchResultFound(file: File, query: String) =
            file.name.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))

    interface SearchResultCallback {
        fun onSearchResultFound(result: ArrayList<FileInfo>)
    }
}