package com.siju.acexplorer.storage.model

import android.content.Context
import android.util.Log
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.helper.DateUtils

object RecentTimeData {

    enum class HeaderType {
        TODAY,
        YESTERDAY,
        THIS_WEEK,
        THIS_MONTH;
    }

    fun getRecentTimeData(fileList: ArrayList<FileInfo>): ArrayList<RecentDataItem> {
        val dataMap = linkedMapOf<HeaderType, ArrayList<FileInfo>>()
        for (file in fileList) {
            val date = file.date * 1000
            when {
                DateUtils.isToday(date) -> mapRecentData(dataMap, file, HeaderType.TODAY)
                DateUtils.isYesterday(date) -> mapRecentData(dataMap, file, HeaderType.YESTERDAY)
                DateUtils.isThisWeek(date) -> mapRecentData(dataMap, file, HeaderType.THIS_WEEK)
                DateUtils.isThisMonth(date) -> mapRecentData(dataMap, file, HeaderType.THIS_MONTH)
            }
        }
        val recentData = ArrayList<RecentDataItem>()
        for ((headerType, itemList) in dataMap) {
            Log.e("RecentTimeData", "map: $headerType = ${itemList.size}")
            recentData.add(RecentDataItem.Header(headerType, itemList.size))
            for (item in itemList) {
                recentData.add(RecentDataItem.Item(headerType, item))
            }
        }
        return recentData
    }

    private fun mapRecentData(dataMap: HashMap<HeaderType, ArrayList<FileInfo>>, file: FileInfo,
                              headerType: HeaderType) {
        if (dataMap.containsKey(headerType)) {
            dataMap[headerType]?.add(file)
        } else {
            val list = arrayListOf<FileInfo>()
            list.add(file)
            dataMap[headerType] = list
        }
    }

    fun getHeaderName(context: Context, headerType: HeaderType): String {
        return when (headerType) {
            HeaderType.TODAY -> context.getString(R.string.recent_today)
            HeaderType.YESTERDAY -> context.getString(R.string.recent_yesterday)
            HeaderType.THIS_WEEK -> context.getString(R.string.recent_this_week)
            HeaderType.THIS_MONTH -> context.getString(R.string.recent_this_month)
        }
    }


    sealed class RecentDataItem {
        data class Item(val headerType: HeaderType, val fileInfo: FileInfo) : RecentDataItem() {

            override val id: String
                get() = fileInfo.filePath
        }

        data class Header(val headerType: HeaderType, val count: Int) : RecentDataItem() {
            override val id: String
                get() = headerType.name
        }

        abstract val id: String

    }
}