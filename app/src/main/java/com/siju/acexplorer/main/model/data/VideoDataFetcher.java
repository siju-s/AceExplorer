package com.siju.acexplorer.main.model.data;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.HiddenFileHelper;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.helper.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.main.model.helper.SortHelper.sortFiles;

class VideoDataFetcher {


    static ArrayList<FileInfo> fetchVideos(Context context, Category category, long id, int sortMode,
                                           boolean showOnlyCount, boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList;
        switch (category) {
            case GENERIC_VIDEOS:
            case VIDEO:
                fileInfoList = fetchVideos(context, category, showOnlyCount);
                sortMode = 0;
                break;
            case FOLDER_VIDEOS:
                fileInfoList = fetchBucketDetail(context, category, id, showHidden);
                break;
            default:
                fileInfoList = new ArrayList<>();
                break;
        }
        return sortFiles(fileInfoList, sortMode);
    }


    private static ArrayList<FileInfo> fetchVideos(Context context, Category category, boolean isHome) {

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

        return getVideoCursorData(cursor, category, isHome);
    }

    private static ArrayList<FileInfo> getVideoCursorData(Cursor cursor, Category category, boolean isHome) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }
        if (cursor.moveToFirst()) {
            if (isHome) {
                fileInfoList.add(new FileInfo(category, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            }
            int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
            int bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            List<Long> ids = new ArrayList<>();
            int count = 0;
            do {
                long bucketId = cursor.getLong(bucketIdIndex);
                String path = cursor.getString(pathIndex);
                String bucketName = cursor.getString(bucketNameIndex);
                if (!ids.contains(bucketId)) {
                    count = 1;
                    FileInfo fileInfo = new FileInfo(category, bucketId, bucketName, path, count);
                    fileInfoList.add(fileInfo);
                    ids.add(bucketId);
                } else {
                    count++;
                    fileInfoList.get(ids.indexOf(bucketId)).setNumTracks(count);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return fileInfoList;
    }


    private static ArrayList<FileInfo> fetchBucketDetail(Context context, Category category, long bucketId,
                                                         boolean showHidden)
    {

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Video.Media.BUCKET_ID + " =?";
        String selectionArgs[] = new String[]{String.valueOf(bucketId)};

        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs,
                                                           MediaStore.Video.Media.DEFAULT_SORT_ORDER);

        return getBucketDetailCursorData(cursor, category, showHidden);
    }

    private static ArrayList<FileInfo> getBucketDetailCursorData(Cursor cursor, Category category, boolean showHidden) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }
        int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
        int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
        int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
        int imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
        int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        if (cursor.moveToFirst()) {
            do {
                String path = cursor.getString(pathIndex);
                File file = new File(path);
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue;
                }
                String fileName = cursor.getString(titleIndex);
                long size = cursor.getLong(sizeIndex);
                long date = cursor.getLong(dateIndex);
                long videoId = cursor.getLong(imageIdIndex);
                long bucketId = cursor.getLong(bucketIdIndex);
                String extension = FileUtils.getExtension(path);
                String nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension);
                fileInfoList.add(new FileInfo(category, videoId, bucketId, nameWithExt, path, date, size,
                                              extension));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return fileInfoList;
    }

}
