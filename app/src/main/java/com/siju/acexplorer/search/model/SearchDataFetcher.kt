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
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.ArrayList

class SearchDataFetcher(private val searchResultCallback: SearchResultCallback) {
    private val searchGeneration = AtomicLong(0)


    fun fetchData(paths: List<String>, query: String) {
        val generation = searchGeneration.incrementAndGet()
        val startedAt = System.currentTimeMillis()
        val searchData = ArrayList<FileInfo>()
        Log.d("SearchDataFetcher", "fetchData query:$query")
        paths.forEach { path ->
            if (!isCancelled(generation)) {
                searchFile(path, query, generation, searchData)
            }
        }
        Log.d(
            "SearchDataFetcher",
            "Search completed, size : ${searchData.size}, time:${System.currentTimeMillis() - startedAt}"
        )
    }

    private fun searchFile(
        path: String,
        query: String,
        generation: Long,
        searchData: ArrayList<FileInfo>
    ) {
        val file = File(path)
        if (file.canRead()) {
            getMatchingFiles(
                file,
                query,
                DataFetcher.canShowHiddenFiles(AceApplication.appContext),
                generation,
                searchData
            )
        }
    }

    fun cancelSearch() {
        searchGeneration.incrementAndGet()
    }

    private fun getMatchingFiles(
        sourceFile: File,
        query: String,
        showHidden: Boolean,
        generation: Long,
        searchData: ArrayList<FileInfo>
    ) {
        val listFiles = sourceFile.listFiles() ?: return
        for (file in listFiles) {
            if (isCancelled(generation)) {
                return
            }
//            Log.d("SearchDataFetcher", "getMatchingFiles : file:${file.name}, query:$query, cancel:$cancelSearch")
            if (isSearchResultFound(file, query)) {
//                Log.d("SearchDataFetcher", "FOUND : file:${file.name}, query:$query")
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
                createSearchData(fileInfo, generation, searchData)
                if (isDirectory) {
                    getMatchingFiles(file, query, showHidden, generation, searchData)
                }
            } else {
                if (file.isDirectory) {
                    getMatchingFiles(file, query, showHidden, generation, searchData)
                }
            }
        }
    }

    private fun createSearchData(
        fileInfo: FileInfo,
        generation: Long,
        searchData: ArrayList<FileInfo>
    ) {
        if (isCancelled(generation)) {
            return
        }
        searchData.add(fileInfo)
        searchResultCallback.onSearchResultFound(ArrayList(searchData))
    }

    private fun isCancelled(generation: Long): Boolean = generation != searchGeneration.get()
    private fun isSearchResultFound(file: File, query: String) =
            file.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))

    interface SearchResultCallback {
        fun onSearchResultFound(result: ArrayList<FileInfo>)
    }
}
