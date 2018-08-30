package com.siju.acexplorer.main.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.HiddenFileHelper;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.groups.CategoryHelper;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.model.helper.SortHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.main.model.groups.Category.RECENT_DOCS;
import static com.siju.acexplorer.main.model.helper.FileUtils.isApk;

class RecentDataFetcher {

    private static final long MAX_RECENT_DAYS_IN_SECONDS = 7 * 24 * 3600; // 7 days

    static ArrayList<FileInfo> fetchRecent(Context context, Category category,
                                           boolean showOnlyCount, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = constructRecentTimeSelectionArgument() + " AND " + buildHasMimeTypeArguments();
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";

        Cursor cursor = context.getContentResolver().query(uri, null, selection, null,
                                                           sortOrder);
        return getDataFromCursor(cursor, category, showOnlyCount, showHidden);
    }

    @NonNull
    private static String buildHasMimeTypeArguments() {
        return MediaStore.Files.FileColumns.MIME_TYPE + " != " + "''";
    }

    static ArrayList<FileInfo> fetchRecentImages(Context context, Category category, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = constructRecentTimeSelectionArgument() + " AND " + getImagesMediaType();
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null,
                                                           sortOrder);
        return getDataFromCursor(cursor, category, false, showHidden);
    }

    static ArrayList<FileInfo> fetchRecentAudio(Context context, Category category, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = constructRecentTimeSelectionArgument() + " AND " + getAudioMediaType();
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null,
                                                           sortOrder);
        return getDataFromCursor(cursor, category, false, showHidden);
    }

    static ArrayList<FileInfo> fetchRecentVideos(Context context, Category category, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = constructRecentTimeSelectionArgument() + " AND " + getVideosMediaType();
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null,
                                                           sortOrder);
        return getDataFromCursor(cursor, category, false, showHidden);
    }

    static ArrayList<FileInfo> fetchRecentApps(Context context, Category category, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = MediaStore.Files.FileColumns.DATA + " LIKE ?";
        String selection =
                constructRecentTimeSelectionArgument() + " AND " + buildHasMimeTypeArguments() + " AND " + getDocs() +
                " AND " + where;
        String filter = ".apk";
        String[] selectionArgs = new String[]{"%" + filter};

        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs,
                                                           sortOrder);
        return getDataFromCursor(cursor, category, false, showHidden);
    }

    static ArrayList<FileInfo> fetchRecentDocs(Context context, Category category, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = constructRecentTimeSelectionArgument() + " AND " + buildHasMimeTypeArguments() + " AND " +
                           getDocs();
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null,
                                                           sortOrder);
        return getDataFromCursor(cursor, category, false, showHidden);
    }

    private static String constructRecentTimeSelectionArgument() {
        long currentTimeMs = System.currentTimeMillis() / 1000;
        long pastTime = currentTimeMs - MAX_RECENT_DAYS_IN_SECONDS;
        return MediaStore.Files.FileColumns.DATE_ADDED + " BETWEEN " + pastTime + " AND " + currentTimeMs;
    }

    private static String getImagesMediaType() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
    }

    private static String getVideosMediaType() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    }

    private static String getAudioMediaType() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
    }

    private static String getDocs() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
    }

    private static ArrayList<FileInfo> getDataFromCursor(Cursor cursor, Category category, boolean showOnlyCount,
                                                         boolean showHidden)
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
                if (shouldSkipApk(category, extension)) {
                    continue;
                }
                String nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension);
                fileInfoList.add(new FileInfo(CategoryHelper.getSubCategoryForRecentFromExtension(extension), fileId,
                                              nameWithExt, path, date, size,
                                              extension));

            } while (cursor.moveToNext());
        }
        cursor.close();
        if (CategoryHelper.isRecentGenericCategory(category)) {
            return getRecentCategoryList(fileInfoList);
        }
        return fileInfoList;
    }

    private static boolean shouldSkipApk(Category category, String extension) {
        return RECENT_DOCS.equals(category) && isApk(extension);
    }

    private static ArrayList<FileInfo> getRecentCategoryList(ArrayList<FileInfo> fileList) {
        SortHelper.sortRecentCategory(fileList);
        List<Category> categories = new ArrayList<>();
        int count = 0;
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        for (FileInfo fileInfo : fileList) {
            Category newCategory = CategoryHelper.getCategoryForRecentFromExtension(fileInfo.getExtension());
            if (!categories.contains(newCategory)) {
                count = 1;
                FileInfo itemFileInfo = new FileInfo(newCategory,
                                                     CategoryHelper.getSubCategoryForRecentFromExtension(
                                                             fileInfo.getExtension()),
                                                     count);
                fileInfoList.add(itemFileInfo);
                categories.add(newCategory);
            } else {
                count++;
                fileInfoList.get(categories.indexOf(newCategory)).setCount(count);
            }
        }
        return fileInfoList;
    }


}
