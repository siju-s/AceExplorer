package com.siju.acexplorer.search.model

import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.FileDataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.FileUtils.EXT_APK
import com.siju.acexplorer.main.model.helper.RootHelper
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SearchDataFetcher(private val searchResultCallback: SearchResultCallback) {
    private var searchHeaderMap = hashMapOf<Int, ArrayList<FileInfo>>()
    private var cancelSearch = false

    fun fetchData(path: String?, query: String) {
        //TODO 30 Jan 2020 Should use coroutine cancel somehow instead of a flag here
        Log.e("SearchDataFetcher", "fetchData query:$query, cancel:$cancelSearch")
        searchHeaderMap = HashMap()
        cancelSearch = false
        searchFile(path, query)
    }

    private fun searchFile(path: String?, query: String) {
        val file = File(path)
        if (file.canRead()) {
            getMatchingFiles(file, query)
        }
    }

    fun cancelSearch() {
        cancelSearch = true
    }

    private fun getMatchingFiles(sourceFile: File, query: String) {
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
                    getMatchingFiles(file, query)
                }
            } else {
                if (file.isDirectory) {
                    getMatchingFiles(file, query)
                }
            }
        }
    }

    private fun createSearchData(fileInfo: FileInfo) {
        if (cancelSearch) {
            return
        }
        val type = getType(fileInfo)
        if (searchHeaderMap.contains(type)) {
            searchHeaderMap[type]?.add(fileInfo)
        }
        else {
            val list = arrayListOf<FileInfo>()
            list.add(fileInfo)
            searchHeaderMap[type] = list
        }
        Log.e("SearchDataFetcher", "createSearchData : fileInfo:${fileInfo.isDirectory}, category : ${fileInfo.category}, type:$type,  value : ${fileInfo.fileName}")
        val searchData = ArrayList<SearchDataItem>()
        for ((headerType, itemList) in searchHeaderMap) {
            if (cancelSearch) {
                break
            }
            searchData.add(SearchDataItem.Header(headerType, itemList.size))
            val iterator = itemList.iterator()
            while (iterator.hasNext()) {
                searchData.add(SearchDataItem.Item(iterator.next()))
            }
        }
        if (!cancelSearch) {
            searchResultCallback.onSearchResultFound(searchData)
        }
    }

    private fun getType(fileInfo: FileInfo): Int {
        return when {
            fileInfo.isDirectory -> SearchHeaderType.FOLDER.value
            fileInfo.category == Category.IMAGE -> SearchHeaderType.IMAGE.value
            fileInfo.category == Category.VIDEO -> SearchHeaderType.VIDEO.value
            fileInfo.category == Category.AUDIO -> SearchHeaderType.AUDIO.value
            fileInfo.extension.endsWith(EXT_APK, true) -> SearchHeaderType.APP.value
            else -> SearchHeaderType.OTHER.value

        }
    }

    sealed class SearchDataItem {
        data class Item(val fileInfo: FileInfo?) : SearchDataItem() {

            override val id: String
                get() = fileInfo?.filePath.toString()
        }

        data class Header(val headerType: Int, val count : Int) : SearchDataItem() {
            override val id: String
                get() = headerType.toString()
        }

        abstract val id: String

    }

    private fun isSearchResultFound(file: File, query: String) =
            file.name.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))

    interface SearchResultCallback {
        fun onSearchResultFound(result: ArrayList<SearchDataItem>)
    }
}