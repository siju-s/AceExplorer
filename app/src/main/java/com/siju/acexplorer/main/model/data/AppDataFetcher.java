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

import static com.siju.acexplorer.main.model.helper.SortHelper.sortFiles;

public class AppDataFetcher {

    public static ArrayList<FileInfo> fetchApk(Context context, Category category, int sortMode,
                                               boolean showOnlyCount, boolean showHidden)
    {
        String where = MediaStore.Files.FileColumns.DATA + " LIKE ?";
        String filter = ".apk";
        String[] selectionArgs = new String[]{"%" + filter};
        Uri uri = MediaStore.Files.getContentUri("external");

        Cursor cursor = context.getContentResolver().query(uri, null, where, selectionArgs,
                                                           null);
        return getApkCursorData(cursor, category, sortMode, showOnlyCount, showHidden);
    }

    private static ArrayList<FileInfo> getApkCursorData(Cursor cursor, Category category, int sortMode,
                                                        boolean showOnlyCount, boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }
        if (cursor.moveToFirst()) {
            if (showOnlyCount) {
                fileInfoList.add(new FileInfo(category, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            }
            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
            int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
            int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
            int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            do {
                String path = cursor.getString(pathIndex);
                File file = new File(path);
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue;
                }
                String fileName = cursor.getString(titleIndex);
                long size = cursor.getLong(sizeIndex);
                long date = cursor.getLong(dateIndex);
                long fileId = cursor.getLong(fileIdIndex);
                String extension = FileUtils.getExtension(path);
                String nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension);
                fileInfoList.add(new FileInfo(category, fileId, nameWithExt, path, date, size,
                                              extension));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sortFiles(fileInfoList, sortMode);
    }
}
