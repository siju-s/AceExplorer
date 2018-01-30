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

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;

import com.siju.acexplorer.logging.Logger;

import java.util.ArrayList;
import java.util.List;


public class MediaStoreHelper {


    private static final String TAG = "MediaStoreHelper";

    public static void scanFile(Context context, String path) {
        if (context == null) {
            return;
        }
        MediaScannerConnection.scanFile(context,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
//                        Log.i(TAG, "Scanned " + path);
//                        Log.i(TAG, "-> uri=" + uri);
                    }
                });
    }

    public static void scanMultipleFiles(Context context, String[] paths) {
        if (context == null) {
            return;
        }
        MediaScannerConnection.scanFile(context, paths, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
//                        Log.i(TAG, "Scanned " + path);
//                        Log.i(TAG, "-> uri=" + uri);
                    }
                });
    }

    public static int removeMedia(Context context, String path, int category) {
        if (context == null) {
            return 0;
        }
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
        //Log.d(TAG, "removeMedia: uri:"+uri + " deleted:"+deleted + " path:"+path);
        resolver.notifyChange(uri, null);
        return deleted;
    }



    public static int removeBatchMedia(Context context, List<String> paths, int[] category) {
        if (context == null) {
            return 0;
        }

        ContentResolver resolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
        for (int i = 0 ; i < paths.size() ; i++) {
            Uri uri;
            if (category == null) {
                uri = MediaStore.Files.getContentUri("external");
            } else {
                switch (category[i]) {
                    case 1:
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case 2:
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case 3:
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case 4:
                        uri = MediaStore.Files.getContentUri("external");
                        break;
                    default:
                        uri = MediaStore.Files.getContentUri("external");
                        break;
                }
            }


            ContentProviderOperation contentProviderOperation = ContentProviderOperation.newDelete(
                    uri).withSelection(MediaStore.Files.FileColumns.DATA + " =? ",
                    new String[]{paths.get(i)}).build();
            operationList.add(contentProviderOperation);
        }

        ContentProviderResult[] contentProviderResults = new ContentProviderResult[0];

        try {
            contentProviderResults = resolver.applyBatch("media", operationList);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        Logger.log(TAG, "removeBatchMedia: deleted:"+contentProviderResults.length);
        return contentProviderResults.length;
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

        if (fileName == null) {
            return 0;
        }
        //Log.d(TAG, "updateMedia: fileName:"+fileName);

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
        //Log.d(TAG, "updateMedia: uri:"+uri + " updated:"+updated);
        resolver.notifyChange(uri, null);
        return updated;
    }
}
