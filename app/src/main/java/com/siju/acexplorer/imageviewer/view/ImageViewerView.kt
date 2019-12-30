package com.siju.acexplorer.imageviewer.view

import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.common.types.FileInfo

interface ImageViewerView {

    fun setActivity(activity : AppCompatActivity)
    fun setPosition(pos : Int)
    fun setFileInfoList(list : ArrayList<FileInfo>)
    fun inflate()
}