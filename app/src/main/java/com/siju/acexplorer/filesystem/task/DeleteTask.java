package com.siju.acexplorer.filesystem.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.RootNotPermittedException;
import com.siju.acexplorer.filesystem.utils.RootUtils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.filesystem.operations.Operations.DELETE;

public class DeleteTask extends AsyncTask<Void, Void, Integer> {

    private int totalFiles;
    private final ArrayList<FileInfo> deletedFilesList = new ArrayList<>();
    private final Context mContext;
    private final boolean mIsRootMode;
    private ArrayList<FileInfo> fileList = new ArrayList<>();
    private boolean mShowToast = true;


    public DeleteTask(Context context, boolean rootMode, ArrayList<FileInfo> fileList) {
        mContext = context;
        mIsRootMode = rootMode;
        this.fileList = fileList;
    }

    void setmShowToast() {
        mShowToast = false;
    }


    @Override
    protected Integer doInBackground(Void... params) {
        int deletedCount = 0;


        totalFiles = fileList.size();

        for (int i = 0; i < totalFiles; i++) {
            String path = fileList.get(i).getFilePath();
            boolean isDeleted = FileUtils.deleteFile(new File(path), mContext);

            if (!isDeleted) {
                if (mIsRootMode) {
                    try {
                        RootUtils.mountRW(path);
                        RootUtils.delete(path);
                        RootUtils.mountRO(path);
                        deletedFilesList.add(fileList.get(i));
                        deletedCount++;
                    } catch (RootNotPermittedException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                deletedFilesList.add(fileList.get(i));
                deletedCount++;
            }
        }


        return deletedCount;
    }

    @Override
    protected void onPostExecute(Integer filesDel) {
        int deletedFiles = filesDel;
        Intent intent = new Intent("refresh");
        intent.putExtra(KEY_OPERATION, DELETE);
        intent.putParcelableArrayListExtra("deleted_files", deletedFilesList);
        mContext.sendBroadcast(intent);
        if (mShowToast) {
            if (deletedFiles != 0) {
                FileUtils.showMessage(mContext, mContext.getResources().getQuantityString(R.plurals.number_of_files,
                        deletedFiles, deletedFiles) + " " + mContext.getString(R.string.msg_delete_success));
            }

            if (totalFiles != deletedFiles) {
                FileUtils.showMessage(mContext, mContext.getString(R.string.msg_delete_failure));
            }
        }

    }

}
