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

package com.siju.acexplorer.main.model.helper;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;


public class RingtoneHelper {

    public static Uri getCustomRingtoneUri(ContentResolver contentResolver, String path, int type) {

        String musicType = type == 1 ? "is_ringtone" : type == 2 ? "is_notification" : "is_alarm";
        String uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
        Uri parse;
        Cursor query = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, musicType}, "_data=?", new String[]{path}, null);
        if (query != null) {
            try {
                if (query.getCount() != 0) {
                    query.moveToFirst();
                    int id = query.getInt(0);
                    /* If ringtone/notification/alarm flag not there in custom uri, add it .
                      This part was necessary to make it work on Note 2 (4.4.2) */
                    if (query.getInt(1) == 0 && type != 0) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(musicType, true);
                        contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues, "_id=" +
                                id, null);
                    }
                    parse = Uri.parse(uri + "/" + id);
                    query.close();
                    return parse;
                } else {
                    return insertAudioIntoMediaStore(contentResolver, path, type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return insertAudioIntoMediaStore(contentResolver, path, type);
        }
        return null;
    }

    private static Uri insertAudioIntoMediaStore(ContentResolver contentResolver, String path, int type) {
        File file = new File(path);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media.DATA, path);
        contentValues.put(MediaStore.Audio.Media.TITLE, file.getName());
        contentValues.put(MediaStore.Audio.Media.SIZE, file.length());
        contentValues.put(MediaStore.Audio.Media.DATE_MODIFIED, file.lastModified());
        contentValues.put("is_ringtone", type == 1);
        contentValues.put("is_notification", type == 2);
        contentValues.put("is_alarm", type == 4);
        contentValues.put("is_music", true);
        return contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

}
