package com.siju.acexplorer.common.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun convertDate(dateInMs: Long): String {
        val df2 = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date = Date(dateInMs)
        return df2.format(date)
    }
}