package com.siju.acexplorer.home.edit.model

import android.content.Context
import com.siju.acexplorer.R

enum class CategoryEditType {
    SAVED,
    OTHER;

    companion object {

        fun getHeaderName(context: Context, type: CategoryEditType) =
                when (type) {
                    SAVED -> context.getString(R.string.saved_category)
                    OTHER -> context.getString(R.string.other_category)
                }
    }
}