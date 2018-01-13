package com.siju.acexplorer.model;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.siju.acexplorer.model.groups.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.helper.SortHelper.sortFiles;

public class VideoLoader {


    private static final String TAG = "VideoLoader";

    public static ArrayList<FileInfo> fetchVideos(Context context, Category category, long id, int sortMode,
                                                  boolean isHome, boolean showHidden) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        switch (category) {
            case GENERIC_VIDEOS:
            case VIDEO:
                fileInfoList = fetchVideos(context, category, isHome);
                if (fileInfoList.size() != 0) {
                    fileInfoList = sortFiles(fileInfoList, 0);
                }
                return fileInfoList;
            case FOLDER_VIDEOS:
                fileInfoList = fetchBucketDetail(context, category, id, showHidden);
                break;

        }
        if (fileInfoList.size() != 0) {
            fileInfoList = sortFiles(fileInfoList, sortMode);
        }
        return fileInfoList;
    }


    private static ArrayList<FileInfo> fetchVideos(Context context, Category category, boolean isHome) {
        Log.d(TAG, "fetchVideos() called with: category = [" + category + "], isHome = [" + isHome + "]");
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA};
        String sortOrder;
        if (isHome) {
            sortOrder = null;
        } else {
            sortOrder = MediaStore.Video.Media.BUCKET_ID;
        }
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (isHome) {
                    Log.d(TAG, "fetchVideos: category:"+category + " count:"+cursor.getCount());
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    cursor.close();
                    return fileInfoList;
                }
                List<Long> ids = new ArrayList<>();
                int count = 0;
                do {
                    int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
                    int bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                    String path = cursor.getString(pathIndex);
                    String bucketName = cursor.getString(bucketNameIndex);
                    long bucketId = cursor.getLong(bucketIdIndex);

                    if (!ids.contains(bucketId)) {
                        count = 1;
                        FileInfo fileInfo = new FileInfo(category, bucketId, bucketName, path, count);
                        fileInfoList.add(fileInfo);
                        Log.d(TAG, "fetchVideos: bucketName:"+bucketName + "id:"+bucketId);
                        ids.add(bucketId);
                    } else {
                        count++;
                        fileInfoList.get(ids.indexOf(bucketId)).setNumTracks(count);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fileInfoList;
    }


    private static ArrayList<FileInfo> fetchBucketDetail(Context context, Category category, long bucketId, boolean showHidden) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Video.Media.BUCKET_ID + " =?";
        String selectionArgs[] = new String[]{String.valueOf(bucketId)};

        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs,
                                                           MediaStore.Video.Media.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
                    int imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                    String path = cursor.getString(pathIndex);
                    File file = new File(path);
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }
                    String fileName = cursor.getString(titleIndex);
                    long size = cursor.getLong(sizeIndex);
                    long date = cursor.getLong(dateIndex);
                    long videoId = cursor.getLong(imageIdIndex);
                    long bucketId1 = cursor.getLong(bucketIdIndex);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(category, videoId, bucketId1, nameWithExt, path, date, size,
                                                  extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fileInfoList;
    }

}
