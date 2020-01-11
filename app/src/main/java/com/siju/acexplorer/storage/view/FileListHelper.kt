package com.siju.acexplorer.storage.view

import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

interface FileListHelper {

    fun handleItemClick(fileInfo: FileInfo, position: Int)
    fun handleLongItemClick(fileInfo: FileInfo, second: Int)
    fun isDualModeEnabled() : Boolean
    fun isDragNotStarted() : Boolean
    fun onUpEvent()
    fun onMoveEvent()
    fun endActionMode()
    fun getCategory() : Category
    fun onDragDropEvent(pos: Int, data: ArrayList<FileInfo>)
    fun getActivityInstance() : AppCompatActivity
    fun isActionModeActive() : Boolean
}