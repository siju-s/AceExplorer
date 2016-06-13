package com.siju.filemanager.filesystem;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.format.Formatter;

import com.siju.filemanager.R;
import com.siju.filemanager.group.StorageGroup;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private ArrayList<FileInfo> fileInfoArrayList;
    private String mPath;
    private Context mContext;

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

            if (listFiles != null) {
                for (File file1 : listFiles) {
                    boolean isDirectory = false;
                    String fileName = file1.getName();
                    String filePath = file1.getAbsolutePath();
                    String noOfFilesOrSize;
                    if (file1.isDirectory()) {
                        isDirectory = true;
                        int childFileListSize = file1.list().length;
                        noOfFilesOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files, childFileListSize, childFileListSize);
                    } else {
                        long size = file1.length();
                        noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
                    }
                    long date = file1.lastModified();
                    String fileModifiedDate = StorageGroup.convertDate(date);
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
}
