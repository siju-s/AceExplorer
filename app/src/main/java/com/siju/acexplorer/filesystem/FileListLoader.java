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
    private final int mCategory;
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

    FileListLoader(Fragment fragment, Context context, String path, int category) {
        super(context);
        mPath = path;
        mCategory = category;
        mShowHidden = PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        mSortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
        mFragment = fragment;

    }

    FileListLoader(Fragment fragment, String path, int category, String zipPath, boolean
            isDualPaneInFocus, boolean isParentZip) {
        super(fragment.getContext());
        Logger.log(TAG, "Zip" + "dir=" + zipPath);
        mPath = path;
        Context context = fragment.getContext();
        mFragment = fragment;
        mCategory = category;
        mShowHidden = PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        if (zipPath != null && zipPath.endsWith(File.separator))
            zipPath = zipPath.substring(0, zipPath.length() - 1);
        mZipPath = zipPath;
        mIsDualPaneInFocus = isDualPaneInFocus;
        mInParentZip = isParentZip;
        mSortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }

    public FileListLoader(Fragment fragment, Context context, String path, int category, boolean isRingtonePicker) {
        super(context);
        mPath = path;
        mCategory = category;
        mShowHidden = PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        mSortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
        mFragment = fragment;
        mIsRingtonePicker = isRingtonePicker;
    }

    FileListLoader(Fragment fragment, String path, String outputDir, String fileName,
                   ZipEntry zipEntry) {
        super(fragment.getContext());
        this.zip = true;
        this.outputDir = outputDir;
        mCategory = FileConstants.CATEGORY.ZIP_VIEWER.getValue();
        Logger.log(TAG, "Zip PARENT=" + path + " ENTRY=" + zipEntry);
        try {
            this.zipFile = new ZipFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mZipPath = zipEntry.getName();
        if (mZipPath.endsWith("/")) {
            mZipPath = mZipPath.substring(0, mZipPath.length() - 1);
        }
        mPath = path;
        mFragment = fragment;
        this.fileName = fileName;
        this.entry = zipEntry;
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
        if (entry != null && entry.getName().endsWith(".zip")) {
            if (zip) {
                try {
                    unzipEntry(zipFile, entry, outputDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    unzipRAREntry(rar, header, outputDir);
                } catch (IOException | RarException e) {
                    e.printStackTrace();
                }
            }
        } else {
            fetchDataByCategory();

        }
        return fileInfoList;
    }

    private final Comparator<? super FileInfo> comparatorByNameZip = new Comparator<FileInfo>() {

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

    private final Comparator<? super ZipModel> comparatorByNameZip1 = new Comparator<ZipModel>() {

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
        //TODO Change debug mode to False in Release build
        RootTools.debugMode = true;
        isRooted = RootUtils.isRooted(getContext());
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
                    fileInfoList.add(new FileInfo(mCategory, cursor.getCount()));
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

                    fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date1, size1, mCategory,
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

    /**
     * @param dir           Child path names. First time it will be null
     * @param parentZipPath Original zip path with.zip extension
     * @return Zip contents
     */
    private void getZipContents(String dir, String parentZipPath) {
        ZipFile zipfile;
        ArrayList<ZipModel> totalZipList;
        ArrayList<ZipModel> elements = new ArrayList<>();
        totalZipList = ((FileListFragment) mFragment).totalZipList;
        try {
//            if (totalZipList.size() == 0 || entry != null) {
            if (new File(parentZipPath).canRead()) {
                totalZipList = new ArrayList<>();
                zipfile = new ZipFile(parentZipPath);
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    totalZipList.add(new ZipModel(entry, entry.getTime(), entry.getSize(), entry
                            .isDirectory()));
                }
            } else {
                totalZipList = new ArrayList<>();
                ZipEntry zipEntry;
                Uri uri = Uri.parse(parentZipPath);
                ZipInputStream zipfile1 = new ZipInputStream(getContext().getContentResolver()
                        .openInputStream(uri));
                while ((zipEntry = zipfile1.getNextEntry()) != null) {
                    totalZipList.add(new ZipModel(zipEntry, zipEntry.getTime(), zipEntry.getSize(), zipEntry
                            .isDirectory()));
                }
            }
            ((FileListFragment) mFragment).totalZipList = totalZipList;

            ArrayList<String> strings = new ArrayList<>();
            for (ZipModel entry : totalZipList) {

                File file = new File(entry.getName());
                if (dir == null || dir.trim().length() == 0) {
                    String y = entry.getName();
                    System.out.println("entry name==" + y);

              /*      if (y.startsWith(File.separator))
                        y = y.substring(1, y.length());*/
                    if (file.getParent() == null || file.getParent().length() == 0 || file.getParent().equals(File
                            .separator)) {
                        System.out.println("entry if isdir==" + entry.isDirectory() + "y=" + y);
                        if (!strings.contains(y)) {
                            elements.add(new ZipModel(new ZipEntry(y), entry.getTime(), entry.getSize(),
                                    entry.isDirectory()));
                            strings.add(y);
                        }
                    } else {
                        boolean slash = false;
                        if (y.startsWith(File.separator)) {
                            slash = true;
                            y = y.substring(1, y.length());
                        }
                        String path = y.substring(0, y.indexOf(File.separator) + 1);
                        if (slash) {
                            path = "/" + path;
                        }
                        System.out.println("entry else path==" + path);
                        ZipModel zipObj;
                        if (!strings.contains(path)) {
                            zipObj = new ZipModel(new ZipEntry(path), entry.getTime(), entry
                                    .getSize(), true);
                            strings.add(path);
                            elements.add(zipObj);
                        }
                    }
                } else {
                    String y = entry.getName();
                    System.out.println("ZIP ITEM==" + y + "dir=" + dir);
                /*    if (y.startsWith(File.separator))
                        y = y.substring(1, y.length());*/

                    if (file.getParent() != null && (file.getParent().equals(dir) || file.getParent().equals(File
                            .separator + dir))) {
                        if (!strings.contains(y)) {
                            elements.add(new ZipModel(new ZipEntry(y), entry.getTime(), entry.getSize(), entry
                                    .isDirectory()));
                            strings.add(y);
                        }
                    } else {
                        if (y.startsWith(dir + File.separator) && y.length() > dir.length() + 1) {
                            String path1 = y.substring(dir.length() + 1, y.length());
                            System.out.println("path1==" + path1);

                            int index = dir.length() + 1 + path1.indexOf(File.separator);
                            String path = y.substring(0, index + 1);
                            System.out.println("path==" + path);

                            if (!strings.contains(path)) {
                                ZipModel zipObj = new ZipModel(new ZipEntry(y.substring(0, index + 1)), entry.getTime
                                        (), entry.getSize(), true);
                                strings.add(path);
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

        ((FileListFragment) mFragment).zipChildren = elements;
        for (ZipModel model : elements) {
            String name = model.getName();
            if (name.startsWith("/")) {
                name = name.substring(1, name.length());
            }
            boolean isDirectory = model.isDirectory();
            long size;
            if (isDirectory) {
                int count = 0;
                for (ZipModel zipmodel : totalZipList) {
                    String modelName = zipmodel.getEntry().getName();
                    if (modelName.startsWith(File.separator))
                        modelName = modelName.substring(1, modelName.length());
                    System.out.println("SIJU --Dir true--modelname" + modelName + " name=" + name);

                    if (modelName.startsWith(name)) {
                        count++;
                    }
                }
                size = count;
            } else {
                size = model.getSize();
            }
            int type = FileConstants.CATEGORY.COMPRESSED.getValue();
            long date = model.getTime();
            String extension;
            if (isDirectory) {
                name = name.substring(0, name.length() - 1);
                if (!mInParentZip) {
                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                }
                extension = null;
            } else {
                name = name.substring(name.lastIndexOf(File.separator) + 1);
                extension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            String path = parentZipPath + File.separator + name;

            FileInfo fileInfo = new FileInfo(name, path, date, size,
                    isDirectory, extension, type, RootHelper.parseFilePermission(new File(path)));
            fileInfoList.add(fileInfo);
        }
        Collections.sort(fileInfoList, comparatorByNameZip);
    }

    private void getRarContents(String dir, String parentZipPath) {
        ArrayList<FileHeader> elements = new ArrayList<>();
        try {
            Archive zipfile = new Archive(new File(parentZipPath));
            ArrayList<FileHeader> totalRarList;
            ((FileListFragment) mFragment).mArchive = zipfile;
            totalRarList = ((FileListFragment) mFragment).totalRarList;

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
        } catch (Exception ignored) {
        }

        for (FileHeader fileHeader : elements) {
            String name = fileHeader.getFileNameString();

            boolean isDirectory = fileHeader.isDirectory();
        /*    if (isDirectory) {
                name = name.substring(name.lastIndexOf(File.separator) + 1);
            }*/
            long size = fileHeader.getPackSize();
            int type = FileConstants.CATEGORY.COMPRESSED.getValue();
            Date date = fileHeader.getMTime();
            long date1 = date.getTime();
//            String noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
//            String fileModifiedDate = FileUtils.convertDate(date);
            String extension;
            if (isDirectory) {
                name = name.substring(0, name.length() - 1);
                if (!mInParentZip) {
                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                }
                extension = null;
            } else {
                name = name.substring(name.lastIndexOf(File.separator) + 1);
                extension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            String path = parentZipPath + File.separator + name;

            FileInfo fileInfo = new FileInfo(name, path, date1, size,
                    isDirectory, extension, type, RootHelper.parseFilePermission(new File(path)));
            fileInfoList.add(fileInfo);
        }
        Collections.sort(fileInfoList, comparatorByNameZip);
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir)
            throws IOException {

        File output = new File(outputDir, fileName);
        BufferedInputStream inputStream = new BufferedInputStream(
                zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(output));
        Logger.log("Extract", "zipfile=" + zipfile.getName() + " zipentry=" + entry + " stream=" + inputStream);
        Logger.log("Extract", "outputDir=" + outputDir + " filename=" + fileName);

        Logger.log("Extract", "Bytes START=" + inputStream.available());

        try {
            int len;
            byte buf[] = new byte[20480];
            while ((len = inputStream.read(buf)) > 0) {
                //System.out.println(id + " " + hash.get(id));
                outputStream.write(buf, 0, len);
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                //closing quietly
            }

            try {
                outputStream.close();
            } catch (IOException e) {
                //closing quietly
            }

        }
        getZipContents("", output.getAbsolutePath());
    }

    private void unzipRAREntry(Archive zipfile, FileHeader header, String outputDir)
            throws IOException, RarException {

        File output = new File(outputDir + "/" + header.getFileNameString().trim());
        FileOutputStream fileOutputStream = new FileOutputStream(output);
        zipfile.extractFile(header, fileOutputStream);
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
                    fileInfoList.add(new FileInfo(mCategory, cursor.getCount()));
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
                    fileInfoList.add(new FileInfo(mCategory, cursor.getCount()));
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
                    fileInfoList.add(new FileInfo(mCategory, cursor.getCount()));
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
                    fileInfoList.add(new FileInfo(mCategory, cursor.getCount()));
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
        if (mimeType == null) return mCategory;
        if (mimeType.startsWith("image")) {
            return FileConstants.CATEGORY.IMAGE.getValue();
        } else if (mimeType.startsWith("audio")) {
            return FileConstants.CATEGORY.AUDIO.getValue();
        } else if (mimeType.startsWith("video")) {
            return FileConstants.CATEGORY.VIDEO.getValue();
        }
        return mCategory;

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
