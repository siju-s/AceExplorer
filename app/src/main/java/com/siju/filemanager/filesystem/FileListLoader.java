package com.siju.filemanager.filesystem;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.format.Formatter;
import android.util.Log;

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

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private ArrayList<FileInfo> fileInfoList;
    private String mPath;
    private Context mContext;
    private boolean showHidden = false;

    public FileListLoader(Context context, String path) {
        super(context);
        mPath = path;
        mContext = context;
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
                    String fileModifiedDate = FileUtils.convertDate(date);


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
}
