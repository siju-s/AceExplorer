package com.siju.acexplorer.storage.modules.zipviewer.view

interface ZipViewer {

    fun onDirectoryClicked(position: Int)
    fun onFileClicked(position: Int)
    fun onBackPress()
    fun navigateTo(dir : String?)
    fun endZipMode(dir: String?)
    fun loadData()
}