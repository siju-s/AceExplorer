package com.siju.acexplorer.filesystem.task;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.helper.RootHelper;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Siju on 27-08-2016.
 */
public class DeleteTask extends AsyncTask<ArrayList<FileInfo>, Void, Integer> {

    private String fileName;
    private String filePath;
    private int copyStatus = -1;
    //        private ProgressDialog progressDialog;
    private Dialog progressDialog;
    private Dialog deleteDialog;
    private int operation;
    private int currentFile = 0;
    private int filesCopied;
    private boolean isActionCancelled;
    TextView textFileName;
    private int totalFiles;
    private String sourcePath;
    ArrayList<String> paths = new ArrayList<>();
    ArrayList<FileInfo> deletedFilesList = new ArrayList<>();

    ArrayList<String> mimeTypes = new ArrayList<>();
    private Context mContext;
    private boolean mIsRootMode;


    public  DeleteTask(Context context, boolean rootMode) {
        this.operation = operation;
        mContext = context;
        mIsRootMode = rootMode;
//        sourcePath = mSourceFilePath;

    }

    @Override
    protected Integer doInBackground(ArrayList<FileInfo>... params) {
        int deletedCount = 0;
        ArrayList<FileInfo> fileInfo = params[0];


        totalFiles = fileInfo.size();

        for (int i = 0; i < totalFiles; i++) {
            String path = fileInfo.get(i).getFilePath();
//                int result = FileUtils.deleteTarget(path);
            boolean isDeleted = FileUtils.deleteFile(new File(path), mContext);

            if (!isDeleted) {
                if (mIsRootMode) {
                    RootTools.remount(new File(path).getParent(), "rw");
                    String s = RootHelper.runAndWait("rm -r \"" + path + "\"", true);
                    RootTools.remount(new File(path).getParent(), "ro");
                    paths.add(path);
                    mimeTypes.add(fileInfo.get(i).getMimeType());
                    deletedFilesList.add(fileInfo.get(i));
                    deletedCount++;
                }

            } else {
                paths.add(path);
                mimeTypes.add(fileInfo.get(i).getMimeType());
                deletedFilesList.add(fileInfo.get(i));
                deletedCount++;
            }
        }


        return deletedCount;
    }

    @Override
    protected void onPostExecute(Integer filesDel) {
        int deletedFiles = filesDel;
        Intent intent = new Intent("refresh");
        intent.putExtra(FileConstants.OPERATION, FileConstants.DELETE);
        intent.putParcelableArrayListExtra("deleted_files", deletedFilesList);
        mContext.sendBroadcast(intent);

        if (deletedFiles != 0) {
            FileUtils.showMessage(mContext, mContext.getResources().getQuantityString(R.plurals.number_of_files,
                    deletedFiles, deletedFiles) + " " + mContext.getString(R.string.msg_delete_success));
        }

        if (totalFiles != deletedFiles) {
            FileUtils.showMessage(mContext, mContext.getString(R.string.msg_delete_failure));
        }

    }

}
