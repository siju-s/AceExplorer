package com.siju.acexplorer.helper

import java.util.*

private const val WEEK_TIME_MS = (7 * 24 * 3600 * 1000).toLong()
private const val ONE_DAY_TIME_MS = (24 * 3600 * 1000).toLong()

object DateUtils {

    fun isYesterday(ms: Long): Boolean {
        val now = Calendar.getInstance()
        return now.timeInMillis - ms <= ONE_DAY_TIME_MS
    }

    fun isToday(ms: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = ms
        now.add(Calendar.DATE, 0)
        return now[Calendar.YEAR] == date[Calendar.YEAR] && now[Calendar.MONTH] == date[Calendar.MONTH]
                && now[Calendar.DATE] == date[Calendar.DATE]
    }

    fun isThisWeek(ms: Long): Boolean {
        val now = Calendar.getInstance()
        return now.timeInMillis - ms <= WEEK_TIME_MS
    }

    fun isThisMonth(ms: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = ms
        return (now[Calendar.YEAR] == date[Calendar.YEAR]
                && now[Calendar.MONTH] == date[Calendar.MONTH])
    }
}