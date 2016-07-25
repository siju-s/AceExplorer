package com.siju.filemanager.filesystem;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.siju.filemanager.R;
import com.siju.filemanager.common.SharedPreferenceWrapper;
import com.siju.filemanager.filesystem.model.FavInfo;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import eu.chainfire.libsuperuser.Shell;

import static com.siju.filemanager.filesystem.utils.FileUtils.convertDate;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private ArrayList<FileInfo> fileInfoList;
    private String mPath;
    private Context mContext;
    private boolean showHidden;
    private int mCategory;
    private List<String> mZipRootFiles;

    public FileListLoader(Context context, String path, int category) {
        super(context);
        mPath = path;
        mContext = context;
        mCategory = category;
        showHidden = PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
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
            case 4:
            case 7:
            case 9:
            case 10:
                fetchByCategory(mCategory);
                break;
            case 8:
                fetchFavorites();
                break;


        }
    }

    private ArrayList<FileInfo> fetchFiles() {
        File file = new File(mPath);
        String fileExtension = mPath.substring(mPath.lastIndexOf(".") + 1);
        boolean isRootAccessGranted = false;
        boolean isRoot = false;
        if (file.canRead()) {
            Log.d("TAG", "yeah");
        } else {
            Log.d("TAG", " NOOOO");

        }
        if (!mPath.contains(FileUtils.getInternalStorage().getAbsolutePath())) {
            if (FileUtils.getExternalStorage() == null) {
                isRoot = true;

      /*          isRootAccessGranted = RootHelperWrapper.canRunRootCommands();
                if (isRootAccessGranted) {
                    RootHelperWrapper wrapper = new RootHelperWrapper();
                    wrapper.execute();
                }*/
            } else if (FileUtils.getExternalStorage() != null && !mPath
                    .contains(FileUtils
                            .getExternalStorage().getAbsolutePath())) {
                isRoot = true;

              /*  isRootAccessGranted = RootHelperWrapper.canRunRootCommands();
                if (isRootAccessGranted) {
                    RootHelperWrapper wrapper = new RootHelperWrapper();
                    wrapper.execute();
                }*/
            }
        }


        if (fileExtension.equalsIgnoreCase("zip")) {
//            fileInfoList = fetchZipContent();
            HashMap<String, List<String>> contents = retrieveListing(file);
            fileInfoList = getZipFilesFromMap(contents);
            return fileInfoList;
        } else {
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
                                if (!showHidden) {
                                    File[] nonHiddenList = file1.listFiles(new FilenameFilter() {
                                        @Override
                                        public boolean accept(File file, String name) {
                                            return (!name.startsWith("."));
                                        }
                                    });
                                    childFileListSize = nonHiddenList.length;
                                } else {
                                    childFileListSize = file1.list().length;
                                }
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
/*                            if (extension.equals("zip")) {
                                String[] list = file1.list();
                                ZipFile zipFile = null;
                                try {
                                    zipFile = new ZipFile(file1);
                                    Enumeration entries = zipFile.entries();
                                    while (entries.hasMoreElements()) {
                                        final ZipEntry entry = (ZipEntry) entries.nextElement();
                                        InputStream stream = zipFile.getInputStream(entry);
                                       Logger.log("TAG","File: %s Size %d Modified on %TD %n"+ entry
                                                .getName()+
                                               entry.getSize());
                                   *//*     extractEntry(entry, file.getInputStream
                                                (entry));*//*
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }*/
                        }
                        long date = file1.lastModified();
                        String fileModifiedDate = convertDate(date);


                        FileInfo fileInfo = new FileInfo(fileName, filePath, fileModifiedDate, noOfFilesOrSize,
                                isDirectory, extension, type);
                        fileInfoList.add(fileInfo);


                    }
                    return fileInfoList;

                } else if (isRoot) {
                    boolean suAvailable = Shell.SU.available();
                    if (suAvailable) {

                        try {
                            String line;
                            Process process = Runtime.getRuntime().exec("su");
                            OutputStream stdin = process.getOutputStream();
                            InputStream stderr = process.getErrorStream();
                            InputStream stdout = process.getInputStream();

                           /* byte [] b = new byte[12];
                             b = "ls -lF\n";*/
                            String command = "ls -lF\n";
                            byte[] utf8Bytes = command.getBytes("UTF8");

                            stdin.write(utf8Bytes);
                            stdin.write("exit\n".getBytes("UTF-8"));
                            stdin.flush();

                            stdin.close();
                            BufferedReader br =
                                    new BufferedReader(new InputStreamReader(stdout));
                            while ((line = br.readLine()) != null) {
                                Log.d("[Output]", line);
                            }
                            br.close();
                            br =
                                    new BufferedReader(new InputStreamReader(stderr));
                            while ((line = br.readLine()) != null) {
                                Log.e("[Error]", line);
                            }
                            br.close();

                            process.waitFor();
                            process.destroy();

                        } catch (Exception ex) {
                        }

                      /*  String [] commands = new String[1];
                        commands[0] = "ls -pl " + file;
                        List<String> suResult = Shell.SU.run(commands);
                        StringBuilder sb = new StringBuilder();
                        for (String line : suResult) {
                            sb.append(line).append((char)10);
                        }*/
//                        Logger.log("TAG","List=="+sb);
/*                        for (String line : suResult) {
        *//*                    boolean isDirectory = false;
                            String fileName = file1.getName();
                            String filePath = file1.getAbsolutePath();
                            String noOfFilesOrSize = null;
                            String extension = null;
                            int type = 0;*//*


                        }*/
                    }
                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }


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
                    true, null, FileConstants.CATEGORY.FAVORITES.getValue());
            fileInfoList.add(fileInfo);
        }
        return fileInfoList;
    }


    private ArrayList<FileInfo> testFetchFiles() {
        Uri uri = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.Files.FileColumns.DATA + " =? ";
        String[] selectionArgs = new String[]{mPath};


        String sortOrder = MediaStore.Files.FileColumns.TITLE;
        Cursor cursor = mContext.getContentResolver().query(uri, null, selection, selectionArgs, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);

//                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long fileId = cursor.getLong(fileIdIndex);
                String path = cursor.getString(pathIndex);
                int type = FileConstants.CATEGORY.DOCS.getValue();
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                fileName = tokens[0];
//                String extension = tokens[1];
                String nameWithExt = fileName + "." + extension;
                fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            return null;
        }
        return fileInfoList;
    }


    private ArrayList<FileInfo> fetchZipContent() {
        try {
            ZipFile zipFile = new ZipFile(mPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            if (entries != null) {

                while (entries.hasMoreElements()) {
                    String noOfFilesOrSize = null;
                    ZipEntry entry = entries.nextElement();
                    String fileName = entry.getName();
                    long time = entry.getTime();
                    String fileModifiedDate = convertDate(time);
                    String extension = null;
                    int type = 0;
                    boolean isDirectory = entry.isDirectory();
                    if (isDirectory) {
                        int childFileListSize = 0;
//                        if (file1.list() != null) {
//                            childFileListSize = file1.list().length;
//                        }

                        if (childFileListSize == 0) {
                            noOfFilesOrSize = mContext.getResources().getString(R.string.empty);
                        } else {
                            noOfFilesOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files,
                                    childFileListSize, childFileListSize);
                        }
//                        }
                        FileInfo fileInfo = new FileInfo(fileName, fileName, fileModifiedDate, noOfFilesOrSize,
                                isDirectory, extension, type);
                        fileInfoList.add(fileInfo);
                    }
         /*           else {
                        extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                        long size = entry.getSize();
                        noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
//                        extension = filePath.substring(filePath.lastIndexOf(".") + 1);
//                        type = checkMimeType(filePath);
                    }*/


                }
                return fileInfoList;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public HashMap<String, List<String>> retrieveListing(File zipFile) {
        HashMap<String, List<String>> contents = new HashMap<>();
        try {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    String directory = ze.getName();
                    if (!contents.containsKey(directory)) {
                        contents.put(directory, new ArrayList<String>());
                    }
                } else {
                    String file = ze.getName();
                    int pos = file.lastIndexOf("/");
                    if (pos != -1) {
                        String directory = file.substring(0, pos + 1);
                        String fileName = file.substring(pos + 1);
                        if (!contents.containsKey(directory)) {
                            contents.put(directory, new ArrayList<String>());
                            List<String> fileNames = contents.get(directory);
                            fileNames.add(fileName);
                        } else {
                            List<String> fileNames = contents.get(directory);
                            fileNames.add(fileName);
                        }
                    } else {
                        if (!contents.containsKey("root")) {
                            contents.put("root", new ArrayList<String>());
                        }
                        List<String> fileNames = contents.get("root");
                        fileNames.add(file);
                    }
                }
                zin.closeEntry();
            }

            zin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    private ArrayList<FileInfo> getZipFilesFromMap(HashMap<String, List<String>> contents) {
        for (Map.Entry<String, List<String>> entry : contents.entrySet()) {
            int type = 0;
            String directory = entry.getKey();
            List<String> values = entry.getValue();
            // do something with key and/or tab
            String filePath = mPath + "/" + directory;
            String noOfFilesOrSize;
            if (values.size() == 0) {
                noOfFilesOrSize = mContext.getResources().getString(R.string.empty);
            } else {
                noOfFilesOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files,
                        values.size(), values.size());
            }

            boolean isDirectory = true;
            String date = null;
            fileInfoList.add(new FileInfo(directory, filePath, null, noOfFilesOrSize, isDirectory, null, type));
        }
        return fileInfoList;
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
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, sortOrder);
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
                where = MediaStore.Files.FileColumns.MIME_TYPE + " =?" ;
                selectionArgs = new String[]{ pdf1 };

                break;
            case 10:
                String apk = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants
                        .EXT_APK);
//                where = MediaStore.Files.FileColumns.MIME_TYPE + " = '" + apk + "'";
                where = MediaStore.Files.FileColumns.MIME_TYPE + " =?" ;
                selectionArgs = new String[]{ apk };
                break;
            case 11:
                long size = 104857600; // 100 MB
                where = MediaStore.Files.FileColumns.SIZE + " > " + size;
                break;
        }
        Log.d("Loader","Category id=="+category+" where=="+where);

        Cursor cursor;
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        if (category == 9 || category == 10) {
/*            Uri newUri = Uri.parse("content://external/apk");
            cursor = mContext.getContentResolver().query(newUri, null, null, null,
                    null);*/
            cursor = mContext.getContentResolver().query(uri, null, where, selectionArgs,
                    sortOrder);
        }
        else {
            cursor = mContext.getContentResolver().query(uri, null, where, null, sortOrder);
        }
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);

//                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                long fileId = cursor.getLong(fileIdIndex);
                String path = cursor.getString(pathIndex);
                int type = mCategory;
                String date = FileUtils.convertDate(date1 * 1000); // converting it to ms
                String size = Formatter.formatFileSize(mContext, size1);
                String extension = path.substring(path.lastIndexOf(".") + 1);
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                fileName = tokens[0];
//                String extension = tokens[1];
                String nameWithExt = fileName + "." + extension;
                fileInfoList.add(new FileInfo(fileId, nameWithExt, path, date, size, type, extension));

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            return null;
        }
        return fileInfoList;
    }


}
