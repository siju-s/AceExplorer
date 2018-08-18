package com.siju.acexplorer.model.data;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.HiddenFileHelper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.helper.FileUtils.getCategoryFromExtension;
import static com.siju.acexplorer.model.helper.SortHelper.sortFiles;

class ImageDataFetcher {

    static ArrayList<FileInfo> fetchImages(Context context, Category category, long id, int sortMode,
                                           boolean showOnlyCount, boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList;
        switch (category) {
            case GENERIC_IMAGES:
            case IMAGE:
                fileInfoList = fetchImages(context, category, showOnlyCount);
                sortMode = 0;
                break;
            case FOLDER_IMAGES:
                fileInfoList = fetchBucketDetail(context, category, id, showHidden);
                break;
            default:
                fileInfoList = new ArrayList<>();
                break;
        }
        return sortFiles(fileInfoList, sortMode);
    }


    private static ArrayList<FileInfo> fetchImages(Context context, Category category, boolean isHome) {


        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.Media.BUCKET_ID,
                                           MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                                           MediaStore.Images.Media.DATA};

        String sortOrder = isHome ? null : MediaStore.Images.Media.BUCKET_ID;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        return getImageDataFromCursor(cursor, category, isHome);
    }

    private static ArrayList<FileInfo> getImageDataFromCursor(Cursor cursor, Category category, boolean isHome) {
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
            int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            int bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            List<Long> ids = new ArrayList<>();
            int count = 0;
            do {
                String path = cursor.getString(pathIndex);
                String bucketName = cursor.getString(bucketNameIndex);
                long bucketId = cursor.getLong(bucketIdIndex);

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

    static ArrayList<FileInfo> fetchGif(Context context, Category category, int sortMode, boolean isHome,
                                        boolean showHidden)
    {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String gif = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_GIF);
        String selectionArgs[] = new String[]{gif};
        String where = MediaStore.Images.Media.MIME_TYPE + " =?";

        Cursor cursor = context.getContentResolver().query(uri, null, where, selectionArgs,
                                                           null);
        return getGifDataFromCursor(cursor, category, sortMode, isHome, showHidden);
    }

    private static ArrayList<FileInfo> getGifDataFromCursor(Cursor cursor, Category category, int sortMode,
                                                            boolean isHome, boolean showHidden)
    {
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
            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
            int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
            int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                String path = cursor.getString(pathIndex);
                File file = new File(path);
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue;
                }
                long size = cursor.getLong(sizeIndex);
                long date = cursor.getLong(dateIndex);
                long fileId = cursor.getLong(fileIdIndex);
                String fileName = cursor.getString(titleIndex);
                String extension = FileUtils.getExtension(path);
                String nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension);
                category = getCategoryFromExtension(extension);
                fileInfoList.add(new FileInfo(category, fileId, nameWithExt, path, date, size,
                                              extension));

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sortFiles(fileInfoList, sortMode);
    }


    private static ArrayList<FileInfo> fetchBucketDetail(Context context, Category category, long bucketId,
                                                         boolean showHidden)
    {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Images.Media.BUCKET_ID + " =?";
        String selectionArgs[] = new String[]{String.valueOf(bucketId)};

        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs,
                                                           MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        return getBucketDataFromCursor(cursor, category, showHidden);
    }

    private static ArrayList<FileInfo> getBucketDataFromCursor(Cursor cursor, Category category, boolean showHidden) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }
        int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
        int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
        int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
        int imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
        int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor.moveToFirst()) {
            do {
                String path = cursor.getString(pathIndex);
                File file = new File(path);
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue;
                }
                long size = cursor.getLong(sizeIndex);
                long date = cursor.getLong(dateIndex);
                long imageId = cursor.getLong(imageIdIndex);
                long bucketId = cursor.getLong(bucketIdIndex);
                String fileName = cursor.getString(titleIndex);
                String extension = FileUtils.getExtension(path);
                String nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension);
                fileInfoList.add(new FileInfo(category, imageId, bucketId, nameWithExt, path, date, size,
                                              extension));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return fileInfoList;
    }

}
