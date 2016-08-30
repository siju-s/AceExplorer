package com.siju.acexplorer.filesystem;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipModel;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.helper.RootHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.siju.acexplorer.filesystem.utils.FileUtils.convertDate;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<FileInfo> fileInfoList;
    private String mPath;
    private Context mContext;
    private boolean showHidden;
    private int mCategory;
    private List<String> mZipRootFiles;
    private String mZipPath;
    private boolean mIsDualPaneInFocus;
    private Fragment mFragment;
    private boolean mInParentZip;
    private int mSortMode;
    private static final int FILE_OBSERVER_MASK = FileObserver.CREATE
            | FileObserver.DELETE | FileObserver.DELETE_SELF
            | FileObserver.MOVED_FROM | FileObserver.MOVED_TO
            | FileObserver.MODIFY | FileObserver.MOVE_SELF;

    private FileObserver mFileObserver;
    private MediaContentObserver mMediaContentObserver;
    private MountUnmountReceiver mMountUnmountReceiver;


    public FileListLoader(Context context, String path, int category, boolean showHidden, int sortMode) {
        super(context);
        mPath = path;
        mContext = getContext();
        mCategory = category;
        this.showHidden = showHidden;
        mSortMode = sortMode;

    }

    public FileListLoader(Fragment fragment, String path, int category, String zipPath, boolean
            isDualPaneInFocus, boolean isParentZip) {
//        Context context = fragment.getContext();
        super(fragment.getContext());
        mPath = path;
        Context context = fragment.getContext();
        mFragment = fragment;
        mContext = context;
        mCategory = category;
        showHidden = PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        mZipPath = zipPath;
        mIsDualPaneInFocus = isDualPaneInFocus;
        mInParentZip = isParentZip;
        mSortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }


    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        }


        if (mMediaContentObserver == null) {
            mMediaContentObserver = new MediaContentObserver(new Handler());
            mContext.getContentResolver().registerContentObserver(MediaStore.Files.getContentUri("external"), true,
                    mMediaContentObserver);
        }

        if (mMountUnmountReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
            intentFilter.addDataScheme("file");
            getContext().registerReceiver(mMountUnmountReceiver, intentFilter);
        }
       /* if (mFileObserver == null) {
            String observedPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileObserver = new FileObserver(observedPath, FILE_OBSERVER_MASK) {
                @Override
                public void onEvent(int event, String path) {
                    Log.d("FileObserver","path="+path+" event=="+event+" category="+mCategory);
                    if (path == null) return;
                    onContentChanged();
                }
            };
        }
        mFileObserver.startWatching();*/
        if (takeContentChanged() || fileInfoList == null)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (fileInfoList != null) {
            onReleaseResources();
            fileInfoList = null;
        }
    }

    protected void onReleaseResources() {

        /*if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;

        }*/

        if (mMediaContentObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mMediaContentObserver);
        }
        if (mMountUnmountReceiver != null) {
            getContext().unregisterReceiver(mMountUnmountReceiver);
        }


    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
//        android.os.Debug.waitForDebugger();

        fileInfoList = new ArrayList<>();
        fetchDataByCategory();
        return fileInfoList;

    }

    Comparator<? super FileInfo> comparatorByNameZip = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {
            // sort folders first
            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;

            // here both are folders or both are files : sort alpha
            return file1.getFileName().toLowerCase()
                    .compareTo(file2.getFileName().toLowerCase());
        }

    };

    Comparator<? super ZipModel> comparatorByNameZip1 = new Comparator<ZipModel>() {

        public int compare(ZipModel file1, ZipModel file2) {
            // sort folders first
            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;

            // here both are folders or both are files : sort alpha
            return file1.getName().toLowerCase()
                    .compareTo(file2.getName().toLowerCase());
        }

    };


    private void fetchDataByCategory() {
        switch (mCategory) {
            case 0:
            case 5:
                fetchFiles();
                break;
            case 1:
                fetchMusic();
                break;
            case 2:
                fetchVideos();
                break;
            case 3:
                fetchImages();
                break;
            case 10:
                Logger.log("SIJU", "apk category");
                fetchApk();
                break;
            case 4:
            case 7:
            case 9:
            case 11:
                fetchByCategory(mCategory);
                break;
            case 12:
                if (mPath.endsWith("rar"))
                    getRarContents(mZipPath, mPath);
                else
                    getZipContents(mZipPath, mPath);
                break;
            case 8:
                fetchFavorites();
                break;


        }
    }

    private ArrayList<FileInfo> fetchFiles() {
        File file = new File(mPath);
        String fileExtension = mPath.substring(mPath.lastIndexOf(".") + 1);

        if (file.canRead()) {

            if (fileExtension.equalsIgnoreCase("zip")) {
                getZipContents("", file.getAbsolutePath());
                return fileInfoList;
            } else if (fileExtension.equalsIgnoreCase("rar")) {
                getRarContents("", file.getAbsolutePath());
            } else {
                fileInfoList = RootHelper.getFilesList(mContext, mPath,
                        true, showHidden);
                fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
            }
        } else {
//            boolean rootAvailable  = RootTools.isRootAvailable();
            fileInfoList = com.siju.acexplorer.helper.RootHelper.getFilesList(mContext, mPath,
                    true,
                    showHidden);
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        }
        return fileInfoList;


    }

    private ArrayList<FileInfo> fetchApk() {
        // exclude media files, they would be here also.
        String where = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
//                selectionArgs = new String[]{ apk };
//                String test = "/storage/emulated/0/Apps";
        String[] selectionArgs = null;
        Uri uri = MediaStore.Files.getContentUri("external");
        long startTime = System.currentTimeMillis();
        Logger.log(this.getClass().getSimpleName(), "Starting time=" + startTime / 1000);

//        String sortOrder = MediaStore.Files.FileColumns.N + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, null, where, null,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                int mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                String path = cursor.getString(pathIndex);
//                String fileName = cursor.getString(titleIndex);

                if (path.endsWith(".apk")) {
//                    Log.d(TAG, "path=" + path);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long fileId = cursor.getLong(fileIdIndex);

                    String mimeType = cursor.getString(mimeTypeIndex);
                    int type = mCategory;
                    String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                    String size = Formatter.formatFileSize(mContext, size1);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                    String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                    fileName = tokens[0];
//                String extension = tokens[1];
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date, size, type,
                            extension, mimeType));
                }

            } while (cursor.moveToNext());
            cursor.close();
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        }
        long endTime = System.currentTimeMillis();
        long timetaken = (endTime - startTime) / 1000;
        Logger.log(this.getClass().getSimpleName(), "End time=" + endTime + "Time taken=" + timetaken);

        return fileInfoList;

    }

    private ArrayList<FileInfo> fetchFavorites() {
        SharedPreferenceWrapper wrapper = new SharedPreferenceWrapper();
        ArrayList<FavInfo> favList = wrapper.getFavorites(mContext);
        for (FavInfo favInfo : favList) {
            String path = favInfo.getFilePath();
            File file = new File(path);
            String fileName = file.getName();
            String noOfFilesOrSize;
            int childFileListSize = 0;

            if (file.list() != null) {
                if (!showHidden) {
                    File[] nonHiddenList = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String name) {
                            return (!name.startsWith("."));
                        }
                    });
                    childFileListSize = nonHiddenList.length;
                } else {
                    childFileListSize = file.list().length;
                }
            }

            if (childFileListSize == 0) {
                noOfFilesOrSize = mContext.getResources().getString(R.string.empty);
            } else {
                noOfFilesOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files,
                        childFileListSize, childFileListSize);
            }
            long date = file.lastModified();
            String fileModifiedDate = convertDate(date);


            FileInfo fileInfo = new FileInfo(fileName, path, fileModifiedDate, noOfFilesOrSize,
                    true, null, FileConstants.CATEGORY.FAVORITES.getValue(), RootHelper.parseFilePermission(new File
                    (path)));
            fileInfoList.add(fileInfo);
        }
        fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        return fileInfoList;
    }

    /**
     * @param dir           Child path names. First time it will be null
     * @param parentZipPath Original zip path with.zip extension
     * @return
     */
    public ArrayList<FileInfo> getZipContents(String dir, String parentZipPath) {
        ZipFile zipfile = null;
        ArrayList<ZipModel> totalZipList = new ArrayList<>();
        ArrayList<ZipModel> elements = new ArrayList<>();
        if (mIsDualPaneInFocus) {
            totalZipList = ((FileListDualFragment) mFragment).totalZipList;
        } else {
            totalZipList = ((FileListFragment) mFragment).totalZipList;
        }
        try {
            if (totalZipList.size() == 0) {
                if (new File(parentZipPath).canRead()) {
                    zipfile = new ZipFile(parentZipPath);
                    for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                        ZipEntry entry = (ZipEntry) e.nextElement();
                        totalZipList.add(new ZipModel(entry, entry.getTime(), entry.getSize(), entry
                                .isDirectory()));
                    }
                } else {
                    ZipEntry zipEntry;
                    Uri uri = Uri.parse(parentZipPath);
                    ZipInputStream zipfile1 = new ZipInputStream(mContext.getContentResolver()
                            .openInputStream(uri));
                    while ((zipEntry = zipfile1.getNextEntry()) != null) {
                        totalZipList.add(new ZipModel(zipEntry, zipEntry.getTime(), zipEntry.getSize(), zipEntry
                                .isDirectory()));
                    }
                }
                if (mIsDualPaneInFocus) {
                    ((FileListDualFragment) mFragment).totalZipList = totalZipList;
                } else {
                    ((FileListFragment) mFragment).totalZipList = totalZipList;
                }
            }

            ArrayList<String> strings = new ArrayList<>();
            for (ZipModel entry : totalZipList) {

//                i++;
                String s = entry.getName();

                File file = new File(entry.getName());
                if (dir == null || dir.trim().length() == 0) {
                    String y = entry.getName();
                    if (y.startsWith("/"))
                        y = y.substring(1, y.length());
                    if (file.getParent() == null || file.getParent().length() == 0 || file.getParent().equals("/")) {
                        if (!strings.contains(y)) {
                            elements.add(new ZipModel(new ZipEntry(y), entry.getTime(), entry.getSize(),
                                    entry.isDirectory()));
                            strings.add(y);
                        }
                    } else {
                        String path = y.substring(0, y.indexOf("/") + 1);
                        if (!strings.contains(path)) {
                            ZipModel zipObj = new ZipModel(new ZipEntry(path), entry.getTime(), entry
                                    .getSize(), true);
                            strings.add(path);
                            elements.add(zipObj);
                        }

                    }
                } else {
                    String y = entry.getName();
                    System.out.println("ZIP ITEM==" + y);
                    if (entry.getName().startsWith("/"))
                        y = y.substring(1, y.length());

                    if (file.getParent() != null && (file.getParent().equals(dir) || file.getParent().equals("/" + dir))) {
                        if (!strings.contains(y)) {
                            elements.add(new ZipModel(new ZipEntry(y), entry.getTime(), entry.getSize(), entry.isDirectory()));
                            strings.add(y);
                        }
                    } else {
                        if (y.startsWith(dir + "/") && y.length() > dir.length() + 1) {
                            String path1 = y.substring(dir.length() + 1, y.length());

                            int index = dir.length() + 1 + path1.indexOf("/");
                            String path = y.substring(0, index + 1);
                            if (!strings.contains(path)) {
                                ZipModel zipObj = new ZipModel(new ZipEntry(y.substring(0, index + 1)), entry.getTime(), entry.getSize(), true);
                                strings.add(path);
                                //System.out.println(path);
                                elements.add(zipObj);
                            }
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(elements, comparatorByNameZip1);

        if (mIsDualPaneInFocus) {
            ((FileListDualFragment) mFragment).zipChildren = elements;
        } else {
            ((FileListFragment) mFragment).zipChildren = elements;
        }
        for (ZipModel model : elements) {
            String name = model.getName();

            boolean isDirectory = model.isDirectory();
        /*    if (isDirectory) {
                name = name.substring(name.lastIndexOf("/") + 1);
            }*/
            long size = model.getSize();
            int type = FileConstants.CATEGORY.COMPRESSED.getValue();
            long date = model.getTime();
            String noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
            String fileModifiedDate = FileUtils.convertDate(date);
            String extension;
            if (isDirectory) {
                name = name.substring(0, name.length() - 1);
                if (!mInParentZip) {
                    name = name.substring(name.lastIndexOf("/") + 1);
                }
                extension = null;
            } else {
                name = name.substring(name.lastIndexOf("/") + 1);
                extension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            String path = parentZipPath + "/" + name;

            FileInfo fileInfo = new FileInfo(name, path, fileModifiedDate, noOfFilesOrSize,
                    isDirectory, extension, type, RootHelper.parseFilePermission(new File(path)));
            fileInfoList.add(fileInfo);
        }
        Collections.sort(fileInfoList, comparatorByNameZip);
        return fileInfoList;

    }

    private ArrayList<FileInfo> getRarContents(String dir, String parentZipPath) {
        ArrayList<FileHeader> elements = new ArrayList<FileHeader>();
        try {
            Archive zipfile = new Archive(new File(parentZipPath));
            ArrayList<FileHeader> totalRarList;
            if (mIsDualPaneInFocus) {
                ((FileListDualFragment) mFragment).mArchive = zipfile;
                totalRarList = ((FileListDualFragment) mFragment).totalRarList;
            } else {
                ((FileListFragment) mFragment).mArchive = zipfile;
                totalRarList = ((FileListFragment) mFragment).totalRarList;

            }

            if (totalRarList.size() == 0) {

                FileHeader fh = zipfile.nextFileHeader();
                while (fh != null) {
                    totalRarList.add(fh);
                    fh = zipfile.nextFileHeader();
                }
            }
            if (dir == null || dir.trim().length() == 0 || dir.equals("")) {

                for (FileHeader header : totalRarList) {
                    String name = header.getFileNameString();

                    if (!name.contains("\\")) {
                        elements.add(header);

                    }
                }
            } else {
                for (FileHeader header : totalRarList) {
                    String name = header.getFileNameString();
                    if (name.substring(0, name.lastIndexOf("\\")).equals(dir)) {
                        elements.add(header);
                    }
                }
            }
        } catch (Exception e) {
        }

        for (FileHeader fileHeader : elements) {
            String name = fileHeader.getFileNameString();

            boolean isDirectory = fileHeader.isDirectory();
        /*    if (isDirectory) {
                name = name.substring(name.lastIndexOf("/") + 1);
            }*/
            long size = fileHeader.getPackSize();
            int type = FileConstants.CATEGORY.COMPRESSED.getValue();
            Date date = fileHeader.getMTime();
            String noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
            String fileModifiedDate = FileUtils.convertDate(date);
            String extension;
            if (isDirectory) {
                name = name.substring(0, name.length() - 1);
                if (!mInParentZip) {
                    name = name.substring(name.lastIndexOf("/") + 1);
                }
                extension = null;
            } else {
                name = name.substring(name.lastIndexOf("/") + 1);
                extension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            String path = parentZipPath + "/" + name;

            FileInfo fileInfo = new FileInfo(name, path, fileModifiedDate, noOfFilesOrSize,
                    isDirectory, extension, type, RootHelper.parseFilePermission(new File(path)));
            fileInfoList.add(fileInfo);
        }
        Collections.sort(fileInfoList, comparatorByNameZip);
        return fileInfoList;

    }

   /* private ArrayList<FileInfo> getTarContents(String dir, String parentZipPath) {

        TarArchiveEntry entry = new TarArchiveEntry(new File(parentZipPath));

    }*/


    private ArrayList<FileInfo> fetchMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATE_MODIFIED, MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA};
        String where = (MediaStore.Audio.Media.TITLE + " != ''") +
                " AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
        //        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, projection, where, null,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
                int audioIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

//                String fileName = cursor.getString(titleIndex);

                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                String path = cursor.getString(pathIndex);
                long audioId = cursor.getLong(audioIdIndex);
                long albumId = cursor.getLong(albumIdIndex);
                int type = FileConstants.CATEGORY.AUDIO.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
//                String extension = path.substring(path.lastIndexOf(".") + 1);
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                fileName = tokens[0];
                String extension = tokens[1];
                String nameWithExt = fileName + "." + extension;
                fileInfoList.add(new FileInfo(audioId, albumId, nameWithExt, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        } else {
            return null;
        }
        return fileInfoList;
    }

    private ArrayList<FileInfo> fetchImages() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                int imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

//                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long imageId = cursor.getLong(imageIdIndex);
                long bucketId = cursor.getLong(bucketIdIndex);
                String path = cursor.getString(pathIndex);
                int type = FileConstants.CATEGORY.IMAGE.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                fileName = tokens[0];
                String extension = tokens[1];
                String nameWithExt = fileName + "." + extension;
                fileInfoList.add(new FileInfo(imageId, bucketId, nameWithExt, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        } else {
            return null;
        }
        return fileInfoList;
    }

    /**
     * Fetch all videos
     *
     * @return
     */
    private ArrayList<FileInfo> fetchVideos() {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
                int videoIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

//                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long videoId = cursor.getLong(videoIdIndex);
                long bucketId = cursor.getLong(bucketIdIndex);

                String path = cursor.getString(pathIndex);
                int type = FileConstants.CATEGORY.VIDEO.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                fileName = tokens[0];
//                String extension = tokens[1];
                String nameWithExt = fileName + "." + extension;
                fileInfoList.add(new FileInfo(videoId, bucketId, nameWithExt, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        } else {
            return null;
        }
        return fileInfoList;
    }


    /**
     * Fetch all the docs from device
     * Formats as in {@link FileConstants}
     *
     * @return
     */
    private ArrayList<FileInfo> fetchByCategory(int category) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = null;
        String[] selectionArgs = new String[0];


        switch (category) {
            case 4:
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

            case 7:
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

            case 9:
                String pdf1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants
                        .EXT_PDF);
//                where = MediaStore.Files.FileColumns.MIME_TYPE + " = '" + pdf1 + "'";
                where = MediaStore.Files.FileColumns.MIME_TYPE + " =?";
                selectionArgs = new String[]{pdf1};

                break;
            case 11:
                long size = 104857600; // 100 MB
                where = MediaStore.Files.FileColumns.SIZE + " >?";
                selectionArgs = new String[]{String.valueOf(size)};
                break;
        }
//        Log.d("Loader","Category id=="+category+" where=="+where);

        Cursor cursor;
//        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        if (category == 9 || category == 11) {
/*            Uri newUri = Uri.parse("content://external/apk");
            cursor = mContext.getContentResolver().query(newUri, null, null, null,
                    null);*/
            cursor = mContext.getContentResolver().query(uri, null, where, selectionArgs,
                    null);
        } else {
            cursor = mContext.getContentResolver().query(uri, null, where, null, null);
        }
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                int mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);

//                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long fileId = cursor.getLong(fileIdIndex);
                String path = cursor.getString(pathIndex);
                String mimeType = cursor.getString(mimeTypeIndex);
                int type = mCategory;
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                fileName = tokens[0];
//                String extension = tokens[1];
                String nameWithExt = fileName + "." + extension;
//                if (category == FileConstants.CATEGORY.APPS.getValue() && path.endsWith(".apk")) {
//                    Log.d(TAG, "Category=" + mCategory + " path=" + path + "Mime type=" + mimeType);
                fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date, size, type,
                        extension, mimeType));
//                } /*else {
                 /*   fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date, size, type,
                            extension,mimeType));*/
//                }*/

            } while (cursor.moveToNext());
            cursor.close();
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        } else {
            return null;
        }
        return fileInfoList;
    }

    class MediaContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MediaContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            Logger.log(TAG,"uri change=="+uri);
            onContentChanged();
            // do s.th.
            // depending on the handler you might be on the UI
            // thread, so be cautious!

        }
    }

    public class MountUnmountReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("VolumeStateObserver", "onReceive " + intent.getAction());
//        this.mDataObserver.c.c();
            String str = null;
            Object parcelableExtra = intent.getParcelableExtra("storage_volume");
      /*  if (parcelableExtra != null) {
            str = bz.b(parcelableExtra);
            if (str == null) {
                Log.d("VolumeStateObserver", "onReceive path is null");
            }
        }*/
            Bundle bundle;
            if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")) {
//                this.mDataObserver.setChanged();
                bundle = new Bundle();
                bundle.putString("KEY_EVENT", "android.intent.action.MEDIA_MOUNTED");
                bundle.putString("KEY_PATH", str);
                onContentChanged();
//                this.mDataObserver.notifyObservers(bundle);
            } else if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {
//                this.mDataObserver.setChanged();
                bundle = new Bundle();
                bundle.putString("KEY_EVENT", "android.intent.action.MEDIA_UNMOUNTED");
                bundle.putString("KEY_PATH", str);
                onContentChanged();

//                this.mDataObserver.notifyObservers(bundle);
            }

        }

    }
}
