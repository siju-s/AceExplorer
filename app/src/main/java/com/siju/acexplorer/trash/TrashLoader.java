package com.siju.acexplorer.trash;


import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.helper.FileUtils.checkMimeType;
import static com.siju.acexplorer.model.helper.RootHelper.parseFilePermission;
import static com.siju.acexplorer.model.helper.SortHelper.sortFiles;

public class TrashLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private ArrayList<FileInfo> fileInfoList;
    private       int     sortMode;
    private String dir;

    public TrashLoader(@NonNull Context context) {
        super(context);
        sortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
        dir = TrashHelper.getTrashDir(context);
        Log.d("TrashLoader", "Dir: "+dir);
    }

    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
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
    }


    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }

    @Nullable
    @Override
    public ArrayList<FileInfo> loadInBackground() {
        fetchTrashFiles();
        if (fileInfoList != null) {
            fileInfoList = sortFiles(fileInfoList, sortMode);
        }
        return fileInfoList;
    }

    private void fetchTrashFiles() {

        fileInfoList = new ArrayList<>();
        File[] listFiles = new File(dir).listFiles();

        if (listFiles != null) {
            for (File file1 : listFiles) {
                String filePath = file1.getAbsolutePath();
                boolean isDirectory = false;
                long size;
                String extension = null;
                Category category = FILES;
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
                }

                long date = file1.lastModified();

                FileInfo fileInfo = new FileInfo(category, file1.getName(), filePath, date, size,
                                                 isDirectory, extension, parseFilePermission(file1), false);
                fileInfoList.add(fileInfo);
            }
        }
    }
}
