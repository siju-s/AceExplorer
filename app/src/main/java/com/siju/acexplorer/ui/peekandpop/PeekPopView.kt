package com.siju.acexplorer.ui.peekandpop

import android.view.View
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category

interface PeekPopView {

    fun setPeekPopCallback(peekPopCallback: PeekPopCallback?)
    fun setPeekPopListener()
    fun setFileList(list : ArrayList<FileInfo>)
    fun addClickView(view: View, pos : Int, category: Category)
    fun stopAutoPlayVid()
    fun loadPeekView(position: Int, firstRun : Boolean = false)

    interface PeekPopCallback {
        fun onItemClick(view : View, fileInfo: FileInfo, pos : Int)
        fun canShowPeek() : Boolean
    }
}