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
    fun loadPeekView(peekButton: PeekButton = PeekButton.GENERIC, position: Int, firstRun : Boolean = false)
    fun isPeekMode(): Boolean
    fun endPeekMode()
    fun pausePeekMode()

    interface PeekPopCallback {
        fun onItemClick(view : View, fileInfo: FileInfo, pos : Int)
        fun canShowPeek() : Boolean
    }

    enum class PeekButton {
        GENERIC,
        PREVIOUS,
        NEXT
    }
}