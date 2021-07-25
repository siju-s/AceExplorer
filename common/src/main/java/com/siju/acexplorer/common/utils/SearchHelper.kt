package com.siju.acexplorer.common.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.siju.acexplorer.common.R

object SearchHelper {

    fun addSpanToText(name: String, searchedText: String, view : TextView?) {
        if (searchedText.isNotEmpty() && name.contains(searchedText, true)) {
            val startIdx = name.indexOf(searchedText, 0, true)
            val endIdx = startIdx + searchedText.length
            val spannable = SpannableString(name)
            val context = view?.context
            context?.let {
                spannable.setSpan(
                    BackgroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)),
                    startIdx,
                    endIdx,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
            view?.text = spannable
        }
        else {
            view?.text = name
        }
    }
}