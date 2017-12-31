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

package com.siju.acexplorer.model;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.siju.acexplorer.home.view.HomeScreenFragment;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.RootHelper;
import com.siju.acexplorer.model.root.RootUtils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.LARGE_FILES;
import static com.siju.acexplorer.model.groups.Category.PDF;
import static com.siju.acexplorer.model.helper.FileUtils.checkMimeType;
import static com.siju.acexplorer.model.helper.RootHelper.parseFilePermission;
import static com.siju.acexplorer.model.helper.SortHelper.sortFiles;


public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private       Fragment             fragment;
    private       MountUnmountReceiver mountUnmountReceiver;
    private final Category             category;

    private ArrayList<FileInfo> fileInfoList;

    private final String  currentDir;
    private       int     sortMode;
    private       boolean showHidden;
    private       boolean isRingtonePicker;
    private       boolean isRooted;


    public FileListLoader(Fragment fragment, String path, Category category, boolean isRingtonePicker) {
        super(fragment.getContext());
        currentDir = path;
        this.category = category;
        showHidden = PreferenceManager.getDefaultSharedPreferences(fragment.getContext()).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        sortMode = PreferenceManager.getDefaultSharedPreferences(fragment.getContext()).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
        this.fragment = fragment;
        this.isRingtonePicker = isRingtonePicker;
    }


    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        }

        if (mountUnmountReceiver == null) {
            mountUnmountReceiver = new MountUnmountReceiver(this);
        }
        if (takeContentChanged() || fileInfoList == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(ArrayList<FileInfo> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            return;
        }

        fileInfoList = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        fileInfoList = null;
        fragment = null;

        if (mountUnmountReceiver != null) {
            getContext().unregisterReceiver(mountUnmountReceiver);
            mountUnmountReceiver = null;
        }
    }


    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }

    private boolean isHomeFragment() {
        return fragment instanceof HomeScreenFragment;
    }


    @Override
    public ArrayList<FileInfo> loadInBackground() {
        fileInfoList = new ArrayList<>();
        fetchDataByCategory();
        return fileInfoList;
    }


    private void fetchDataByCategory() {
        //TODO Change debug mode to False in Release build
        RootTools.debugMode = true;
        isRooted = RootUtils.isRooted(getContext());
        switch (category) {
            case FILES:
            case DOWNLOADS:
                fetchFiles();
                break;
            case AUDIO:
                fetchMusic();
                break;
            case VIDEO:
                fetchVideos();
                break;
            case IMAGE:
                fetchImages();
                break;
            case APPS:
                fetchApk();
                break;
            case DOCS:
            case COMPRESSED:
            case PDF:
            case LARGE_FILES:
                fetchDocumentsByCategory(category);
                break;
            case FAVORITES:
                fetchFavorites();
                break;
        }
    }

    private void fetchFiles() {
        fileInfoList = getFilesList(currentDir, isRooted, showHidden, isRingtonePicker);
        fileInfoList = sortFiles(fileInfoList, sortMode);
    }

    public static ArrayList<FileInfo> getFilesList(String path, boolean root,
                                                   boolean showHidden, boolean isRingtonePicker) {
        File file = new File(path);
        ArrayList<FileInfo> fileInfoArrayList;
        if (file.canRead()) {
            fileInfoArrayList = getNonRootedList(file, showHidden, isRingtonePicker);
        } else {
            fileInfoArrayList = RootHelper.getRootedList(path, root, showHidden);
        }
        return fileInfoArrayList;
    }

    private static ArrayList<FileInfo> getNonRootedList(File file, boolean showHidden,
                                                        boolean isRingtonePicker) {

        ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();
        File[] listFiles = file.listFiles();

        if (listFiles != null) {
            for (File file1 : listFiles) {
                String filePath = file1.getAbsolutePath();
                boolean isDirectory = false;
                long size;
                String extension = null;
                Category category = FILES;

                // Dont show hidden files by default
                if (file1.isHidden() && !showHidden) {
                    continue;
                }
                if (file1.isDirectory()) {

                    isDirectory = true;
                    int childFileListSize = 0;
                    String[] list = file1.list();
                    if (list != null) {
                        childFileListSize = list.length;
                    }
                    size = childFileListSize;
                } else {
                    size = file1.length();
                    extension = filePath.substring(filePath.lastIndexOf(".") + 1);
                    category = checkMimeType(extension);
                    if (isRingtonePicker && !FileUtils.isFileMusic(filePath)) {
                        continue;
                    }
                }

                long date = file1.lastModified();

                FileInfo fileInfo = new FileInfo(category, file1.getName(), filePath, date, size,
                                                 isDirectory, extension, parseFilePermission(file1), false);
                fileInfoArrayList.add(fileInfo);
            }
        }
        return fileInfoArrayList;
    }


    private void fetchApk() {

        String where = MediaStore.Files.FileColumns.DATA + " LIKE ?";
        String filter = ".apk";
        String[] selectionArgs = new String[]{"%" + filter};
        Uri uri = MediaStore.Files.getContentUri("external");
        long startTime = System.currentTimeMillis();
        Logger.log(this.getClass().getSimpleName(), "Starting time=" + startTime / 1000);

        Cursor cursor = getContext().getContentResolver().query(uri, null, where, selectionArgs,
                                                                null);
        if (cursor != null) {

            if (cursor.moveToFirst()) {
                if (isHomeFragment()) {
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    cursor.close();
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    String path = cursor.getString(pathIndex);
                    File file = new File(path);
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }

                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long fileId = cursor.getLong(fileIdIndex);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;

                    fileInfoList.add(new FileInfo(category, fileId, nameWithExt, path, date1, size1,
                                                  extension));

                } while (cursor.moveToNext());
            }
            cursor.close();

            Logger.log(this.getClass().getSimpleName(), "Apk list size=" + fileInfoList.size());
            if (fileInfoList.size() != 0) {
                fileInfoList = sortFiles(fileInfoList, sortMode);
            }
        }

    }

    private void fetchFavorites() {
        SharedPreferenceWrapper wrapper = new SharedPreferenceWrapper();
        ArrayList<FavInfo> favList = wrapper.getFavorites(getContext());
        if (isHomeFragment()) {
            fileInfoList.add(new FileInfo(category, favList.size()));
            return;
        }
        for (FavInfo favInfo : favList) {
            String path = favInfo.getFilePath();
            File file = new File(path);
            String fileName = file.getName();
            long childFileListSize = 0;
            if (file.isHidden() && !showHidden) {
                continue;
            }
            String [] filesList = file.list();
            if (filesList != null) {
                if (!showHidden) {
                    File[] nonHiddenList = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String name) {
                            return (!name.startsWith("."));
                        }
                    });
                    if (nonHiddenList != null) {
                        childFileListSize = nonHiddenList.length;
                    }
                } else {
                    childFileListSize = filesList.length;
                }
            }


            long date = file.lastModified();

            FileInfo fileInfo = new FileInfo(FILES, fileName, path, date, childFileListSize,
                                             true, null, parseFilePermission(new File(path)), false);
            fileInfoList.add(fileInfo);
        }
        fileInfoList = sortFiles(fileInfoList, sortMode);
        Log.d(TAG, "fetchFavorites: " + fileInfoList.size());
    }


    private void fetchMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, MediaStore
                .Audio
                .Media.ALBUM_ID,
                MediaStore.Audio.Media.DATE_MODIFIED, MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null,
                                                                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (isHomeFragment()) {
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    cursor.close();
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
                    int audioIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    String path = cursor.getString(pathIndex);
                    File file = new File(path);
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }
                    long audioId = cursor.getLong(audioIdIndex);
                    long albumId = cursor.getLong(albumIdIndex);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;

                    fileInfoList.add(new FileInfo(category, audioId, albumId, nameWithExt, path, date1, size1,
                                                  extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
            if (fileInfoList.size() != 0) {
                fileInfoList = sortFiles(fileInfoList, sortMode);
            }
        }
    }

    private void fetchImages() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (isHomeFragment()) {
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    cursor.close();
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                    int imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    String path = cursor.getString(pathIndex);
                    File file = new File(path);
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }
                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long imageId = cursor.getLong(imageIdIndex);
                    long bucketId = cursor.getLong(bucketIdIndex);

                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(category, imageId, bucketId, nameWithExt, path, date1, size1,
                                                  extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
            if (fileInfoList.size() != 0) {
                fileInfoList = sortFiles(fileInfoList, sortMode);
            }
        }
    }

    private void fetchVideos() {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (isHomeFragment()) {
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    cursor.close();
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
                    int videoIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                    String path = cursor.getString(pathIndex);
                    File file = new File(path);
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }
                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long videoId = cursor.getLong(videoIdIndex);
                    long bucketId = cursor.getLong(bucketIdIndex);

                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(category, videoId, bucketId, nameWithExt, path, date1, size1,
                                                  extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
            if (fileInfoList.size() != 0) {
                fileInfoList = sortFiles(fileInfoList, sortMode);
            }
        }
    }


    /**
     * Fetch all the docs from device
     *
     * @return Files
     */
    private void fetchDocumentsByCategory(Category category) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = null;
        String[] selectionArgs = new String[0];


        switch (category) {
            case DOCS:
                String doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOC);
                String docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOCX);
                String txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TEXT);
                String html = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_HTML);
                String pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PDF);
                String xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLS);
                String xlxs = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLXS);
                String ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPT);
                String pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPTX);


                where = MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "("
                        + "'" + doc + "'" + ","
                        + "'" + docx + "'" + ","
                        + "'" + txt + "'" + ","
                        + "'" + html + "'" + ","
                        + "'" + pdf + "'" + ","
                        + "'" + xls + "'" + ","
                        + "'" + xlxs + "'" + ","
                        + "'" + ppt + "'" + ","
                        + "'" + pptx + "'" + " )";
                break;

            case COMPRESSED:
                String zip = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_ZIP);
                String tar = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TAR);
                String tgz = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TGZ);
                String rar = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_RAR);


                where = MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "("
                        + "'" + zip + "'" + ","
                        + "'" + tar + "'" + ","
                        + "'" + tgz + "'" + ","
                        + "'" + rar + "'" + ")";
                break;

            case PDF:
                String pdf1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants
                                                                                          .EXT_PDF);
                where = MediaStore.Files.FileColumns.MIME_TYPE + " =?";
                selectionArgs = new String[]{pdf1};

                break;
            case LARGE_FILES:
                long size = 104857600; // 100 MB
                where = MediaStore.Files.FileColumns.SIZE + " >?";
                selectionArgs = new String[]{String.valueOf(size)};
                break;
        }

        Cursor cursor;
        if (category.equals(PDF) || category.equals(LARGE_FILES)) {
            cursor = getContext().getContentResolver().query(uri, null, where, selectionArgs,
                                                             null);
        } else {
            cursor = getContext().getContentResolver().query(uri, null, where, null, null);
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (isHomeFragment()) {
                    fileInfoList.clear();
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    cursor.close();
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    String path = cursor.getString(pathIndex);
                    File file = new File(path);
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }
                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long fileId = cursor.getLong(fileIdIndex);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    category = checkMimeType(extension);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(category, fileId, nameWithExt, path, date1, size1,
                                                  extension));

                } while (cursor.moveToNext());
            }
            cursor.close();
            if (fileInfoList.size() != 0) {
                fileInfoList = sortFiles(fileInfoList, sortMode);
            }
        }
    }

    private static class MountUnmountReceiver extends BroadcastReceiver {

        final FileListLoader loader;

        MountUnmountReceiver(FileListLoader loader) {
            this.loader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            this.loader.getContext().registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            loader.onContentChanged();
        }
    }
}
