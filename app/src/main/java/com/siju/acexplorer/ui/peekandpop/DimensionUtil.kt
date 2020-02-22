package com.siju.acexplorer.ui.peekandpop

import android.content.Context
import android.util.TypedValue

/**
 * Created by Vincent on 30/01/2016.
 */
object DimensionUtil {

    fun convertDpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }
}