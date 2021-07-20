/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siju.acexplorer.common.types

import android.os.Parcelable
import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.main.model.groups.Category
import kotlinx.parcelize.Parcelize

@Parcelize
class FileInfo(var category: Category? = null,
               var subcategory: Category? = null,
               var fileName: String? = null,
               var filePath: String? = null,
               var extension: String? = null,
               var permissions: String? = null,
               var title: String? = null,
               var count : Int = 0,
               var icon : Int = 0,
               var id : Long = -1L,
               var bucketId : Long = -1L,
               var numTracks: Long = 0L,
               var date : Long = 0L,
               var size : Long = 0L,
               var width : Long = 0L,
               var height: Long = 0L,
               var isDirectory: Boolean = false,
               var isRootMode : Boolean = false,
               var source: AppSource = AppSource.PLAYSTORE,
               var systemApp : Boolean = false)  : Parcelable {

    // For Images, Audio, Video
    constructor(category: Category?, id: Long, bucketId: Long, fileName: String?, filePath: String?,
                fileDate: Long, size: Long,
                extension: String?) : this(category, id, fileName, filePath, fileDate, size, extension) {
        this.bucketId = bucketId
    }

    // Used for apk
    constructor(category: Category?, id: Long, fileName: String?, filePath: String?, fileDate: Long,
                size: Long,
                extension: String?) : this(category, fileName, filePath, fileDate, size, false, extension, null, false) {
        this.id = id
    }

    constructor(category: Category?, fileName: String?, filePath: String?, fileDate: Long, noOfFilesOrSize: Long, isDirectory: Boolean,
                extension: String?, permissions: String?, rootMode: Boolean) : this() {
        this.fileName = fileName
        this.filePath = filePath
        date = fileDate
        size = noOfFilesOrSize
        this.isDirectory = isDirectory
        this.extension = extension
        this.category = category
        this.permissions = permissions
        isRootMode = rootMode
    }

    // For DialogPicker
    private constructor(category: Category, fileName: String, filePath: String, icon: Int) : this() {
        this.category = category
        this.fileName = fileName
        this.filePath = filePath
        this.icon = icon
    }

    // For HomeLib count
    constructor(category: Category?, count: Int) : this() {
        this.category = category
        this.count = count
    }

    // For HomeLib count
    constructor(category: Category?, subcategory: Category?, count: Int) : this() {
        this.category = category
        this.subcategory = subcategory
        this.count = count
    }

    //For camera
    constructor(category: Category?, subcategory: Category?, filePath: String?, count: Int) : this() {
        this.category = category
        this.subcategory = subcategory
        this.filePath = filePath
        this.count = count
        this.isDirectory = true
    }

    constructor(category: Category?, albumId: Long, title: String?, numTracks: Long) : this() {
        this.category = category
        id = albumId
        this.title = title
        this.numTracks = numTracks
    }

    // Folder Images
    constructor(category: Category?, bucketId: Long, bucketName: String?, path: String?, count: Int) : this() {
        this.category = category
        this.bucketId = bucketId
        fileName = bucketName
        filePath = path
        numTracks = count.toLong()
        this.isDirectory = true
    }

    // App manager
    private constructor(category: Category, name: String, packageName: String, source: AppSource, isSystemApp: Boolean, date: Long, size: Long) : this() {
        this.category = category
        fileName = name
        filePath = packageName
        this.source = source
        this.systemApp = isSystemApp
        this.date = date
        this.size = size
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other !is FileInfo) {
            return false
        }
        if (other.filePath == null && filePath == null) {
            return true
        } else if (other.filePath == null || filePath == null) {
            return false
        }
        return other.filePath == filePath
    }

    override fun hashCode(): Int {
        var result = category?.hashCode() ?: 0
        result = 31 * result + (subcategory?.hashCode() ?: 0)
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + (extension?.hashCode() ?: 0)
        result = 31 * result + (permissions?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + count
        result = 31 * result + icon
        result = 31 * result + id.hashCode()
        result = 31 * result + bucketId.hashCode()
        result = 31 * result + numTracks.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + isDirectory.hashCode()
        result = 31 * result + isRootMode.hashCode()
        return result
    }

    companion object {
        fun createImagePropertiesInfo(category: Category?, id: Long, bucketId: Long, fileName: String?, filePath: String?,
                                      fileDate: Long, size: Long,
                                      extension: String?, width: Long, height: Long): FileInfo {
            val info = FileInfo(category, id, bucketId, fileName, filePath, fileDate, size, extension)
            info.width = width
            info.height = height
            return info
        }

        fun createDummyRecentItem(): FileInfo {
            return FileInfo(Category.RECENT, 0)
        }

        fun createPicker(category: Category, fileName: String, filePath: String, icon: Int): FileInfo {
            return FileInfo(category, fileName, filePath, icon)
        }

        fun createCameraGenericInfo(category: Category?, subcategory: Category?, filePath: String?, count: Int): FileInfo {
            return FileInfo(category, subcategory, filePath, count)
        }

        fun createAppInfo(category: Category, name: String, packageName: String, isSystemApp : Boolean, source: AppSource, date: Long, size: Long): FileInfo {
            return FileInfo(category, name, packageName, source, isSystemApp, date, size)
        }
    }
}