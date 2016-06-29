package com.siju.filemanager.filesystem;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.siju.filemanager.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static com.siju.filemanager.filesystem.FileUtils.convertDate;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private ArrayList<FileInfo> fileInfoList;
    private String mPath;
    private Context mContext;
    private boolean showHidden = false;
    private int mCategory;

    public FileListLoader(Context context, String path, int category) {
        super(context);
        mPath = path;
        mContext = context;
        mCategory = category;
    }

    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
//        android.os.Debug.waitForDebugger();

        fileInfoList = new ArrayList<>();
        fetchDataByCategory();
        return fileInfoList;

    }

    private boolean checkIfRootDir(File file) {
        if (!file.getAbsolutePath().contains(FileUtils.getInternalStorage().getAbsolutePath())) {
            return true;
        }
        return false;
    }

    private String getPermissionOfFile(File file) {
        ProcessBuilder processBuilder = new ProcessBuilder("ls", "-l").directory(new File(file.getParent()));// TODO
        // CHECK IF THE FILE IS SD CARD PARENT IS NULL
        Log.d("TAG", "dir:-" + processBuilder.directory());
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter out = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        out.flush();
        String resultLine = null;
        try {
            resultLine = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (resultLine != null) {
            resultLine = resultLine.substring(1, 9);
        }
        Log.d("TAG", "Result==" + resultLine);
        return resultLine;
    }

    private int checkMimeType(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        int value = 0;
        if (mimeType != null) {
            if (mimeType.indexOf("image") == 0) {
                value = FileConstants.CATEGORY.IMAGE.getValue();
            } else if (mimeType.indexOf("video") == 0) {
                value = FileConstants.CATEGORY.VIDEO.getValue();
            } else if (mimeType.indexOf("audio") == 0) {
                value = FileConstants.CATEGORY.AUDIO.getValue();
            }
        }
//        Logger.log("TAG", "Mime type=" + value);
        return value;
    }

    Comparator<? super File> comparatorByName = new Comparator<File>() {

        public int compare(File file1, File file2) {
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

    //    private Comparator<? super String> getSortMode(int sortmode) {
//
//        switch (sortmode) {
//            case 1:
//
//                return name;
//
//            default:
//                return name;
//
//        }
//
//    }
    private void fetchDataByCategory() {
        switch (mCategory) {
            case 0:
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
            case 4:
                fetchDocuments();
                break;
        }
    }

    private ArrayList<FileInfo> fetchFiles() {
        File file = new File(mPath);

        if (file.exists()) {
            File[] listFiles = file.listFiles();

            if (listFiles != null) {
                Arrays.sort(listFiles, comparatorByName);
                for (File file1 : listFiles) {
                    boolean isDirectory = false;
                    String fileName = file1.getName();
                    String filePath = file1.getAbsolutePath();
                    String noOfFilesOrSize = null;
                    String extension = null;
                    int type = 0;

                    // Dont show hidden files by default
                    if (file1.getName().startsWith(".") && !showHidden) {
                        continue;
                    }
                    if (file1.isDirectory()) {

                        isDirectory = true;
                        int childFileListSize = 0;
//                        if (file1.list() == null) {
//                            noOfFilesOrSize = getPermissionOfFile(file1);
//                        }
//                        else {
                        if (file1.list() != null) {
                            childFileListSize = file1.list().length;
                        }

                        if (childFileListSize == 0) {
                            noOfFilesOrSize = mContext.getResources().getString(R.string.empty);
                        } else {
                            noOfFilesOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files,
                                    childFileListSize, childFileListSize);
                        }
//                        }
                    } else {
                        long size = file1.length();
                        noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
                        extension = filePath.substring(filePath.lastIndexOf(".") + 1);
                        type = checkMimeType(filePath);
                    }
                    long date = file1.lastModified();
                    String fileModifiedDate = convertDate(date);


                    FileInfo fileInfo = new FileInfo(fileName, filePath, fileModifiedDate, noOfFilesOrSize,
                            isDirectory, extension, type);
                    fileInfoList.add(fileInfo);


                }
                return fileInfoList;

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private ArrayList<FileInfo> fetchMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, null, where.toString(), null, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
                int audioIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long albumId = cursor.getLong(audioIdIndex);
                String path = cursor.getString(pathIndex);
                int type = FileConstants.CATEGORY.AUDIO.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                fileInfoList.add(new FileInfo(albumId, fileName, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            return null;
        }
        return fileInfoList;
    }

    private ArrayList<FileInfo> fetchImages() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long bucketId = cursor.getLong(bucketIdIndex);
                String path = cursor.getString(pathIndex);
                int type = FileConstants.CATEGORY.IMAGE.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                fileInfoList.add(new FileInfo(bucketId, fileName, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            return null;
        }
        return fileInfoList;
    }

    /**
     * Fetch all videos
     * @return
     */
    private ArrayList<FileInfo> fetchVideos() {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
                int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long bucketId = cursor.getLong(bucketIdIndex);
                String path = cursor.getString(pathIndex);
                int type = FileConstants.CATEGORY.VIDEO.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                fileInfoList.add(new FileInfo(bucketId, fileName, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            return null;
        }
        return fileInfoList;
    }


    /**
     * Fetch all the docs from device
     * Formats as in {@link FileConstants}
     * @return
     */
    private ArrayList<FileInfo> fetchDocuments() {
        Uri uri = MediaStore.Files.getContentUri("external");

        String doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOC);
        String docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOCX);
        String txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TEXT);
        String html = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_HTML);
        String pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PDF);
        String xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLS);
        String xlxs = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLXS);
        String ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPT);
        String pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPTX);


        String where = MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "("
                + "'" + doc + "'" + ","
                + "'" + docx + "'" + ","
                + "'" + txt + "'" + ","
                + "'" + html + "'" + ","
                + "'" + pdf + "'" + ","
                + "'" + xls + "'" + ","
                + "'" + xlxs + "'" + ","
                + "'" + ppt + "'" + ","
                + "'" + pptx + "'" + " )";


        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, null, where, null, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);

                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long bucketId = cursor.getLong(bucketIdIndex);
                String path = cursor.getString(pathIndex);
                int type = FileConstants.CATEGORY.DOCS.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                fileInfoList.add(new FileInfo(bucketId, fileName, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            return null;
        }
        return fileInfoList;
    }


}
