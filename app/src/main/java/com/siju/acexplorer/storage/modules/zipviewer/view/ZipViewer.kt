package com.siju.acexplorer.storage.modules.zipviewer.view

interface ZipViewer {

    fun onDirectoryClicked(position: Int)
    fun onFileClicked(position: Int)
    fun onBackPress()
    fun endZipMode(dir: String?)
    fun loadData()
}