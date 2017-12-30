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

package com.siju.acexplorer.model.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


public class MediaStoreHelper {


    private static final String TAG = "MediaStoreHelper";

    public static void scanFile(Context context, String path) {

        MediaScannerConnection.scanFile(context,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i(TAG, "Scanned " + path);
//                        Log.i(TAG, "-> uri=" + uri);
                    }
                });
    }

    public static int removeMedia(Context context, String path, int category) {
        ContentResolver resolver = context.getContentResolver();
        int deleted;
        Uri uri;

        switch (category) {
            case 1:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                deleted = resolver.delete(uri, MediaStore.Audio.Media.DATA + "=?", new String[]{path});
                break;

            case 2:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                deleted = resolver.delete(uri, MediaStore.Video.Media.DATA + "=?", new String[]{path});
                break;

            case 3:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                deleted = resolver.delete(uri, MediaStore.Images.Media.DATA + "=?", new String[]{path});
                break;

            case 4:
                uri = MediaStore.Files.getContentUri("external");
                deleted = resolver.delete(uri, MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
                break;
            default:
                uri = MediaStore.Files.getContentUri("external");
                deleted = resolver.delete(uri, MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
                break;
        }
        Log.d(TAG, "removeMedia: uri:"+uri + " deleted:"+deleted + " path:"+path);
        resolver.notifyChange(uri, null);
        return deleted;
    }

    public static int updateMedia(Context context, String path, int category) {
        if (context == null) {
            return 0;
        }
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        int updated;
        Uri uri;
        String fileName = FileUtils.getFileNameWithoutExt(path);
        Log.d(TAG, "updateMedia: fileName:"+fileName);

        switch (category) {
            case 1:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                contentValues.put(MediaStore.Audio.Media.TITLE, fileName);
                updated = resolver.update(uri, contentValues, MediaStore.Audio.Media.DATA + "=?", new String[]{path});
                break;

            case 2:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                contentValues.put(MediaStore.Video.Media.TITLE, fileName);
                updated = resolver.update(uri, contentValues, MediaStore.Video.Media.DATA + "=?", new String[]{path});
                break;

            case 3:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                contentValues.put(MediaStore.Images.Media.TITLE, fileName);
                updated = resolver.update(uri, contentValues, MediaStore.Images.Media.DATA + "=?", new String[]{path});
                break;

            case 4:
                uri = MediaStore.Files.getContentUri("external");
                contentValues.put(MediaStore.Files.FileColumns.TITLE, fileName);
                updated = resolver.update(uri, contentValues, MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
                break;
            default:
                uri = MediaStore.Files.getContentUri("external");
                contentValues.put(MediaStore.Files.FileColumns.TITLE, fileName);
                updated = resolver.update(uri, contentValues, MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
                break;
        }
        Log.d(TAG, "updateMedia: uri:"+uri + " updated:"+updated);
        resolver.notifyChange(uri, null);
        return updated;
    }
}
