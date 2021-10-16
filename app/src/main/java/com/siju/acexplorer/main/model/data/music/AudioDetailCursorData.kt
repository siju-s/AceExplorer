package com.siju.acexplorer.main.model.data.music

import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import java.io.File
import java.util.*

// DATA field is required to check path. Works fine till Android 12 even though deprecated
@Suppress("Deprecation")
object AudioDetailCursorData {

    fun getDetailCursorData(cursor: Cursor?, showHidden: Boolean): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()

        if (cursor == null) {
            return fileInfoList
        }

        if (cursor.moveToFirst()) {
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val audioIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            do {
                val fileName = cursor.getString(titleIndex)
                val size1 = cursor.getLong(sizeIndex)
                val date1 = cursor.getLong(dateIndex)
                val path = cursor.getString(pathIndex)
                val file = File(path)
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue
                }
                val audioId = cursor.getLong(audioIdIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val extension = FileUtils.getExtension(path)
                val nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension)
                fileInfoList.add(
                        FileInfo(Category.AUDIO, audioId, albumId, nameWithExt, path, date1, size1,
                                 extension))
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}