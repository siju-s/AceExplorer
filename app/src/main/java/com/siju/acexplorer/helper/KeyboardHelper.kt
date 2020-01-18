package com.siju.acexplorer.helper

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager


object KeyboardHelper {

    fun hideKeyboard(view : View?) {
        if (view == null) {
            return
        }
        val inputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}