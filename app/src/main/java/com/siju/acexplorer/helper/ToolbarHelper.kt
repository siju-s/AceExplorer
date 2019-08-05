package com.siju.acexplorer.helper

import androidx.appcompat.app.AppCompatActivity

object ToolbarHelper {

    fun showToolbarAsUp(activity : AppCompatActivity?) {
        activity?.let {
            activity.supportActionBar?.setHomeButtonEnabled(true)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    fun setToolbarTitle(activity : AppCompatActivity?, title : String?) {
        activity?.supportActionBar?.title = title
    }
}