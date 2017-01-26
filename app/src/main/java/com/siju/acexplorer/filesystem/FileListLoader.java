package com.siju.acexplorer.filesystem;


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
import android.webkit.MimeTypeMap;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipModel;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.RootUtils;
import com.siju.acexplorer.helper.RootHelper;
import com.siju.acexplorer.helper.root.RootTools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<FileInfo> fileInfoList;

    private final String mPath;
    private boolean mShowHidden;
    private final Category category;
    private String mZipPath;
    private boolean mIsDualPaneInFocus;
    private Fragment mFragment;
    private boolean mInParentZip;
    private int mSortMode;
    private boolean mIsRingtonePicker;
    private MountUnmountReceiver mMountUnmountReceiver;
    private String outputDir;
    private ZipFile zipFile;
    private String fileName;
    private boolean zip;
    private ZipEntry entry = null;
    private Archive rar;
    private FileHeader header;
    private boolean isRooted;


     public FileListLoader(Fragment fragment, String path, Category category, boolean isRingtonePicker) {
        super(fragment.getContext());
        mPath = path;
        this.category = category;
        mShowHidden = PreferenceManager.getDefaultSharedPreferences(fragment.getContext()).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        mSortMode = PreferenceManager.getDefaultSharedPreferences(fragment.getContext()).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
        mFragment = fragment;
        mIsRingtonePicker = isRingtonePicker;
    }



    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        }

        if (mMountUnmountReceiver == null) {
            mMountUnmountReceiver = new MountUnmountReceiver(this);
        }
        if (takeContentChanged() || fileInfoList == null)
            forceLoad();
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

        if (fileInfoList != null) {
            fileInfoList = null;
            mFragment = null;
        }
        if (mMountUnmountReceiver != null) {
            getContext().unregisterReceiver(mMountUnmountReceiver);
            mMountUnmountReceiver = null;
        }
    }


    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }

    private boolean isHomeFragment() {
        return mFragment instanceof HomeScreenFragment;
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
                fetchApk();
                break;
            case 4:
            case 7:
            case 9:
            case 11:
                fetchByCategory(category);
                break;
            case 12:

                break;
            case 8:
                fetchFavorites();
                break;
        }
    }

    private void fetchFiles() {
        File file = new File(mPath);
        String fileExtension = mPath.substring(mPath.lastIndexOf(".") + 1);

//        if (file.canRead()) {

        if (fileExtension.equalsIgnoreCase("zip")) {
            getZipContents("", file.getAbsolutePath());
        } else if (fileExtension.equalsIgnoreCase("rar")) {
            getRarContents("", file.getAbsolutePath());
        } else {
            fileInfoList = RootHelper.getFilesList(mPath,
                    isRooted, mShowHidden, mIsRingtonePicker);
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        }
       /* } else {
            fileInfoList = RootHelper.getFilesList(mPath,
                    true, mShowHidden, mIsRingtonePicker);
            fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
        }*/
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
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    int mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                    String path = cursor.getString(pathIndex);
                    String fileName = cursor.getString(titleIndex);

                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long fileId = cursor.getLong(fileIdIndex);

                    String mimeType = cursor.getString(mimeTypeIndex);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;

                    fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date1, size1, category,
                            extension, mimeType));

                } while (cursor.moveToNext());
            }
            cursor.close();

            Logger.log(this.getClass().getSimpleName(), "Apk list size=" + fileInfoList.size());
            if (fileInfoList.size() != 0) {
                fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
            }
        }

    }

    private void fetchFavorites() {
        SharedPreferenceWrapper wrapper = new SharedPreferenceWrapper();
        ArrayList<FavInfo> favList = wrapper.getFavorites(getContext());
        for (FavInfo favInfo : favList) {
            String path = favInfo.getFilePath();
            File file = new File(path);
            String fileName = file.getName();
            long childFileListSize = 0;

            if (file.list() != null) {
                if (!mShowHidden) {
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

      /*      if (childFileListSize == 0) {
                noOfFilesOrSize = mContext.getResources().getString(R.string.empty);
            } else {
                noOfFilesOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files,
                        childFileListSize, childFileListSize);
            }*/
            long date = file.lastModified();

            FileInfo fileInfo = new FileInfo(fileName, path, date, childFileListSize,
                    true, null, FileConstants.CATEGORY.FAVORITES.getValue(), RootHelper.parseFilePermission(new File
                    (path)));
            fileInfoList.add(fileInfo);
        }
        fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
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
                    long audioId = cursor.getLong(audioIdIndex);
                    long albumId = cursor.getLong(albumIdIndex);
                    int type = FileConstants.CATEGORY.AUDIO.getValue();
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(audioId, albumId, nameWithExt, path, date1, size1, type,
                            extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
            if (fileInfoList.size() != 0) {
                fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
            }
        }
    }

    private void fetchImages() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        long startTime = System.currentTimeMillis();
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (isHomeFragment()) {
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                    int imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long imageId = cursor.getLong(imageIdIndex);
                    long bucketId = cursor.getLong(bucketIdIndex);
                    String path = cursor.getString(pathIndex);
                    int type = FileConstants.CATEGORY.IMAGE.getValue();
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(imageId, bucketId, nameWithExt, path, date1, size1, type,
                            extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
            long endTime = System.currentTimeMillis();
            float timetaken = (float) ((endTime - startTime) / 1000);
            Logger.log(this.getClass().getSimpleName(), "Size = " + fileInfoList.size() + "END Time Taken" +
                    timetaken);
            if (fileInfoList.size() != 0) {
                fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
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
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
                    int videoIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long videoId = cursor.getLong(videoIdIndex);
                    long bucketId = cursor.getLong(bucketIdIndex);

                    String path = cursor.getString(pathIndex);
                    int type = FileConstants.CATEGORY.VIDEO.getValue();
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(videoId, bucketId, nameWithExt, path, date1, size1, type,
                            extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
            if (fileInfoList.size() != 0) {
                fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
            }
        }
    }


    /**
     * Fetch all the docs from device
     * Formats as in {@link com.siju.acexplorer.filesystem.FileConstants.CATEGORY}
     *
     * @return Files
     */
    private void fetchByCategory(int category) {
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
                where = MediaStore.Files.FileColumns.MIME_TYPE + " =?";
                selectionArgs = new String[]{pdf1};

                break;
            case 11:
                long size = 104857600; // 100 MB
                where = MediaStore.Files.FileColumns.SIZE + " >?";
                selectionArgs = new String[]{String.valueOf(size)};
                break;
        }

        Cursor cursor;
        if (category == 9 || category == 11) {
            cursor = getContext().getContentResolver().query(uri, null, where, selectionArgs,
                    null);
        } else {
            cursor = getContext().getContentResolver().query(uri, null, where, null, null);
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (isHomeFragment()) {
                    fileInfoList.clear();
                    fileInfoList.add(new FileInfo(this.category, cursor.getCount()));
                    return;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    int mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);

                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    long fileId = cursor.getLong(fileIdIndex);
                    String path = cursor.getString(pathIndex);
                    String mimeType = cursor.getString(mimeTypeIndex);
                    int type = getTypeForMime(mimeType);

                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;
                    fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date1, size1, type,
                            extension, mimeType));

                } while (cursor.moveToNext());
            }
            cursor.close();
            if (fileInfoList.size() != 0) {
                fileInfoList = FileUtils.sortFiles(fileInfoList, mSortMode);
            }
        }
    }

    private int getTypeForMime(String mimeType) {
        if (mimeType == null) return category;
        if (mimeType.startsWith("image")) {
            return FileConstants.CATEGORY.IMAGE.getValue();
        } else if (mimeType.startsWith("audio")) {
            return FileConstants.CATEGORY.AUDIO.getValue();
        } else if (mimeType.startsWith("video")) {
            return FileConstants.CATEGORY.VIDEO.getValue();
        }
        return category;

    }

    private static class MountUnmountReceiver extends BroadcastReceiver {

        final FileListLoader mLoader;

        MountUnmountReceiver(FileListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            mLoader.getContext().registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mLoader.onContentChanged();

        }
    }
}
