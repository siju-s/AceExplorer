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

import static com.siju.acexplorer.main.model.helper.FileUtils.getCategoryFromExtension;
import static com.siju.acexplorer.main.model.helper.SortHelper.sortFiles;

class RecentDataFetcher {

    private static final long MAX_RECENT_DAYS_IN_SECONDS = 7 * 24 * 3600; // 7 days

    static ArrayList<FileInfo> fetchRecent(Context context, Category category,
                                           boolean showOnlyCount, int sortMode, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = constructRecentSelectionArgument();

        Cursor cursor = context.getContentResolver().query(uri, null, selection, null,
                                                           null);
        return getDataFromCursor(cursor, category, showOnlyCount, sortMode, showHidden);
    }

    private static String constructRecentSelectionArgument() {
        long currentTimeMs = System.currentTimeMillis() / 1000;
        long pastTime = currentTimeMs - MAX_RECENT_DAYS_IN_SECONDS;
        String whereFilter =
                MediaStore.Files.FileColumns.MEDIA_TYPE + "!=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
        String whereDate = MediaStore.Files.FileColumns.DATE_ADDED + " BETWEEN " + pastTime + " AND " + currentTimeMs;
        return whereDate + " AND " + "(" + whereFilter + ")";
    }

    private static ArrayList<FileInfo> getDataFromCursor(Cursor cursor, Category category, boolean showOnlyCount,
                                                         int sortMode, boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }
        if (cursor.moveToFirst()) {
            if (showOnlyCount) {
                fileInfoList.clear();
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
                category = getCategoryFromExtension(extension);
                fileInfoList.add(new FileInfo(category, fileId, nameWithExt, path, date, size,
                                              extension));

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sortFiles(fileInfoList, sortMode);
    }
}
