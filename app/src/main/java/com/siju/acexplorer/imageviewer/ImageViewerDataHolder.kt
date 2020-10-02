package com.siju.acexplorer.imageviewer

import android.net.Uri

class ImageViewerDataHolder private constructor() {

    private var uriList : ArrayList<Uri?>? = null
    private var pathList : ArrayList<String?>? = null

    fun setUriList(uriList: ArrayList<Uri?>) {
        this.uriList = uriList
    }

    fun setPathList(pathList: ArrayList<String?>) {
        this.pathList = pathList
    }

    fun getPathList(): ArrayList<String?> {
       pathList?.let {
           return it
       }
        return arrayListOf()
    }

    fun getUriList(): ArrayList<Uri?> {
        uriList?.let {
            return it
        }
        return arrayListOf()
    }

    companion object {
        private var instance: ImageViewerDataHolder? = null

        fun getInstance(): ImageViewerDataHolder? {
            if (instance == null) {
                instance = ImageViewerDataHolder()
            }
            return instance
        }
    }
}