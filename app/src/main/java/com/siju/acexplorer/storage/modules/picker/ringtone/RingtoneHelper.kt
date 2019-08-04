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

package com.siju.acexplorer.storage.modules.picker.ringtone


import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import java.io.File


object RingtoneHelper {

    fun getCustomRingtoneUri(contentResolver: ContentResolver, path: String,
                             ringtoneType: Int): Uri? {
        val musicType = when (ringtoneType) {
            RingtoneManager.TYPE_RINGTONE     -> MediaStore.Audio.Media.IS_RINGTONE
            RingtoneManager.TYPE_NOTIFICATION -> MediaStore.Audio.Media.IS_NOTIFICATION
            RingtoneManager.TYPE_ALARM        -> MediaStore.Audio.Media.IS_ALARM
            else                              -> MediaStore.Audio.Media.IS_MUSIC
        }
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()
        val parsedUri: Uri?
        val selection = MediaStore.Audio.Media.DATA + " =?"
        val selectionArgs = arrayOf(path)
        val cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                           arrayOf(MediaStore.Audio.Media._ID, musicType),
                                           selection,
                                           selectionArgs, null)

        parsedUri = if (cursor == null || cursor.count == 0) {
            insertAudioIntoMediaStore(contentResolver, path, ringtoneType)
        }
        else {
            updateRingtoneUri(contentResolver, cursor, uri, ringtoneType, musicType)
        }
        cursor?.close()
        return parsedUri
    }

    private fun updateRingtoneUri(contentResolver: ContentResolver,
                                  cursor: Cursor,
                                  uri: String,
                                  ringtoneType: Int, mediaType: String): Uri? {
        try {
            if (cursor.count != 0) {
                cursor.moveToFirst()
                val id = cursor.getInt(0)
                /* If ringtone/notification/alarm flag not there in custom uri, add it .
                      This part was necessary to make it work on Note 2 (4.4.2) */
                if (cursor.getInt(1) == 0 && ringtoneType != 0) {
                    val contentValues = ContentValues()
                    contentValues.put(mediaType, true)
                    val selection = MediaStore.Audio.Media._ID + " = $id"
                    contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                           contentValues, selection, null)
                }
                return Uri.parse("$uri/$id")
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun insertAudioIntoMediaStore(contentResolver: ContentResolver, path: String,
                                          ringtoneType: Int): Uri? {
        val file = File(path)
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DATA, path)
            put(MediaStore.Audio.Media.TITLE, file.name)
            put(MediaStore.Audio.Media.SIZE, file.length())
            put(MediaStore.Audio.Media.DATE_MODIFIED, file.lastModified())
        }
        when (ringtoneType) {
            RingtoneManager.TYPE_RINGTONE     -> contentValues.put(
                    MediaStore.Audio.Media.IS_RINGTONE, 1)
            RingtoneManager.TYPE_NOTIFICATION -> contentValues.put(
                    MediaStore.Audio.Media.IS_NOTIFICATION, 1)
            RingtoneManager.TYPE_ALARM        -> contentValues.put(MediaStore.Audio.Media.IS_ALARM,
                                                                   1)
            RingtoneManager.TYPE_ALL          -> {
                with(contentValues) {
                    put(MediaStore.Audio.Media.IS_RINGTONE, 1)
                    put(MediaStore.Audio.Media.IS_NOTIFICATION, 1)
                    put(MediaStore.Audio.Media.IS_ALARM, 1)
                }
            }
        }
        contentValues.put(MediaStore.Audio.Media.IS_MUSIC, true)
        return contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

}
