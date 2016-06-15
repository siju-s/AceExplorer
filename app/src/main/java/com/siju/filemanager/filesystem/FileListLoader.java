package com.siju.filemanager.filesystem;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.format.Formatter;

import com.siju.filemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private ArrayList<FileInfo> fileInfoArrayList;
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
        if (fileInfoArrayList != null) {
            deliverResult(fileInfoArrayList);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
//        android.os.Debug.waitForDebugger();

        fileInfoArrayList = new ArrayList<>();
        File file = new File(mPath);

        if (file.exists()) {
            File[] listFiles = file.listFiles();
            Arrays.sort(listFiles, comparatorByName);

            if (listFiles != null) {
                for (File file1 : listFiles) {
                    boolean isDirectory = false;
                    String fileName = file1.getName();
                    String filePath = file1.getAbsolutePath();
                    String noOfFilesOrSize;

                    // Dont show hidden files by default
                    if (file1.getName().startsWith(".") && !showHidden) {
                        continue;
                    }
                    if (file1.isDirectory()) {
                        isDirectory = true;
                        int childFileListSize = file1.list().length;
                        if (childFileListSize == 0) {
                            noOfFilesOrSize = mContext.getResources().getString(R.string.empty);
                        } else {
                            noOfFilesOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files, childFileListSize, childFileListSize);
                        }
                    } else {
                        long size = file1.length();
                        noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
                    }
                    long date = file1.lastModified();
                    String fileModifiedDate = FileUtils.convertDate(date);
                    FileInfo fileInfo = new FileInfo(fileName, filePath, fileModifiedDate, noOfFilesOrSize, isDirectory);
                    fileInfoArrayList.add(fileInfo);

                }
                return fileInfoArrayList;
            } else {
                return null;
            }
        } else {
            return null;
        }
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
