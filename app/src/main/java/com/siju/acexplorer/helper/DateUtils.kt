package com.siju.acexplorer.helper

import java.util.*

object DateUtils {
    fun isYesterday(ms: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = ms
        now.add(Calendar.DATE, -1)
        return now[Calendar.YEAR] == date[Calendar.YEAR] && now[Calendar.MONTH] == date[Calendar.MONTH]
                && now[Calendar.DATE] == date[Calendar.DATE]
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
        val date = Calendar.getInstance()
        date.timeInMillis = ms
        return now[Calendar.YEAR] == date[Calendar.YEAR] && now[Calendar.MONTH] == date[Calendar.MONTH]
                && now[Calendar.WEEK_OF_YEAR] == date[Calendar.WEEK_OF_YEAR]
    }

    fun isThisMonth(ms: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = ms
        return (now[Calendar.YEAR] == date[Calendar.YEAR]
                && now[Calendar.MONTH] == date[Calendar.MONTH])
    }
}