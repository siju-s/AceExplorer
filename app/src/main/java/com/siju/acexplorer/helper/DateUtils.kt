package com.siju.acexplorer.helper

import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "MMM yyyy"
private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
object DateUtils {

    fun isToday(ms: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = ms
        return now[Calendar.YEAR] == date[Calendar.YEAR] && now[Calendar.MONTH] == date[Calendar.MONTH]
                && now[Calendar.DATE] == date[Calendar.DATE]
    }

    fun isYesterday(ms: Long): Boolean {
        val yestDate = Calendar.getInstance()
        yestDate.add(Calendar.DATE, -1)
        val inputDate = Calendar.getInstance()
        inputDate.timeInMillis = ms
        return yestDate.get(Calendar.DATE) == inputDate.get(Calendar.DATE)
    }

    fun isThisWeek(ms: Long): Boolean {
        val now = Calendar.getInstance()
        val inputDate = Calendar.getInstance()
        inputDate.timeInMillis = ms
        return now.get(Calendar.WEEK_OF_YEAR) == inputDate.get(Calendar.WEEK_OF_YEAR)
    }

    fun isThisMonth(ms: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = ms
        return (now[Calendar.YEAR] == date[Calendar.YEAR]
                && now[Calendar.MONTH] == date[Calendar.MONTH])
    }

    fun getMonthYear(ms : Long) : String {
        val date = Date(ms)
        return dateFormat.format(date)
    }

}