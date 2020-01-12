package com.siju.acexplorer.storage.helper

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.RecentTimeData

object RecentDataConverter {

    fun getRecentItemList(list : ArrayList<RecentTimeData.RecentDataItem>): ArrayList<FileInfo> {
        val fileList = arrayListOf<FileInfo>()
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
}