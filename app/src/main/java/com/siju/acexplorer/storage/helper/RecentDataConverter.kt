package com.siju.acexplorer.storage.helper

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.RecentTimeData

object RecentDataConverter {

    fun getRecentItemList(list : ArrayList<RecentTimeData.RecentDataItem>?): ArrayList<FileInfo> {
        val fileList = arrayListOf<FileInfo>()
        if (list == null) {
            return fileList
        }
        for (item in list) {
            if (item is RecentTimeData.RecentDataItem.Header) {
                fileList.add(FileInfo.createDummyRecentItem())
            }
            else {
                item as RecentTimeData.RecentDataItem.Item
                fileList.add(item.fileInfo)
            }
        }
        return fileList
    }

    fun getRecentItemListWithoutHeader(list : ArrayList<RecentTimeData.RecentDataItem>?): ArrayList<FileInfo> {
        val fileList = arrayListOf<FileInfo>()
        if (list == null) {
            return fileList
        }
        for (item in list) {
            if (item is RecentTimeData.RecentDataItem.Item) {
                fileList.add(item.fileInfo)
            }
        }
        return fileList
    }

    fun getRecentItemListWithHeaderCount(list : ArrayList<RecentTimeData.RecentDataItem>?): Pair<ArrayList<Int>,ArrayList<FileInfo>> {
        val fileList = arrayListOf<FileInfo>()
        if (list == null) {
            return Pair(arrayListOf(), fileList)
        }
        val headerPosList = arrayListOf<Int>()
        for ((index,item) in list.withIndex()) {
            if (item is RecentTimeData.RecentDataItem.Item) {
                fileList.add(item.fileInfo)
            }
            else {
                fileList.add(FileInfo.createDummyRecentItem())
                headerPosList.add(index)
            }
        }
        return Pair(headerPosList, fileList)
    }
}