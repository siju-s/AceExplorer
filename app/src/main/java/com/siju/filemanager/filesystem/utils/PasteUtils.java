package com.siju.filemanager.filesystem.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.FileListDualFragment;
import com.siju.filemanager.filesystem.FileListFragment;
import com.siju.filemanager.filesystem.model.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SIJU on 14-07-2016.
 */

public class PasteUtils {
    private Context mContext;
    private boolean mIsMoveOperation = false;
    private ArrayList<FileInfo> mFileList;
    private HashMap<String, Integer> mPathActionMap = new HashMap<>();
    private int mPasteAction = FileUtils.ACTION_NONE;
    private boolean isPasteConflictDialogShown;
    private String mSourceFilePath = null;
    private ArrayList<String> tempSourceFile = new ArrayList<>();
    private int tempConflictCounter = 0;
    private Dialog mPasteConflictDialog;
    private static final int PASTE_OPERATION = 1;
    private Fragment mFragment;
    private String mCurrentDir;
    private boolean mIsDrag;
    private Activity mActivity;


    public PasteUtils(Context context, Fragment fragment, String currentDir, boolean isDrag) {
        mContext = context;
        mFragment = fragment;
        mCurrentDir = currentDir;
        mIsDrag = isDrag;
        pasteOperationCleanUp();
    }

    PasteUtils(Activity activity, Context context, Fragment fragment, String currentDir, boolean
            isDrag) {
        mContext = context;
        mFragment = fragment;
        mCurrentDir = currentDir;
        mIsDrag = isDrag;
        mActivity = activity;
        pasteOperationCleanUp();

    }


    public void pasteOperationCleanUp() {
        mPathActionMap.clear();
        isPasteConflictDialogShown = false;
        tempConflictCounter = 0;
        tempSourceFile = new ArrayList<>();
    }

    public void setMoveOperation(boolean value) {
        mIsMoveOperation = value;
    }

    public boolean checkIfFileExists(String sourceFilePath, File destinationDir) {
        String[] destinationDirList = destinationDir.list();
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());
        mSourceFilePath = sourceFilePath;
        // If source file is directory,compare source & destination directory names

        if (destinationDirList.length == 0) {
            mPasteAction = FileUtils.ACTION_NONE;
            mPathActionMap.put(sourceFilePath, mPasteAction);
            Logger.log("TAG", "SOURCE==" + sourceFilePath + "isPasteConflictDialogShown==" + isPasteConflictDialogShown);
            return isPasteConflictDialogShown;
        } else {
            // If source file is file,compare source file name & destination directory children names
            for (int i = 0; i < destinationDirList.length; i++) {
                if (fileName.equals(destinationDirList[i])) {
                    isPasteConflictDialogShown = true;
                    tempSourceFile.add(sourceFilePath);
                    break;
                } else {
                    mPasteAction = FileUtils.ACTION_NONE;
                    mPathActionMap.put(sourceFilePath, mPasteAction);
                }
            }

        }

        Logger.log("TAG", "SOURCE==" + sourceFilePath + "isPasteConflictDialogShown==" + isPasteConflictDialogShown);
        return isPasteConflictDialogShown;


    }

    public void showDialog(final String sourceFilePath) {

        mPasteConflictDialog = new Dialog(mContext);
        mPasteConflictDialog.setContentView(R.layout.dialog_paste_conflict);
//        mPasteConflictDialog.setTitle(getResources().getString(R.string.dialog_title_paste_conflict));
        mPasteConflictDialog.setCancelable(false);
        ImageView icon = (ImageView) mPasteConflictDialog.findViewById(R.id.imageFileIcon);
        TextView textFileName = (TextView) mPasteConflictDialog.findViewById(R.id.textFileName);
        TextView textFileDate = (TextView) mPasteConflictDialog.findViewById(R.id.textFileDate);
        TextView textFileSize = (TextView) mPasteConflictDialog.findViewById(R.id.textFileSize);
        Button buttonReplace = (Button) mPasteConflictDialog.findViewById(R.id.buttonReplace);
        Button buttonSkip = (Button) mPasteConflictDialog.findViewById(R.id.buttonSkip);
        Button buttonKeep = (Button) mPasteConflictDialog.findViewById(R.id.buttonKeepBoth);
        if (new File(sourceFilePath).isDirectory()) {
            buttonKeep.setVisibility(View.GONE);
        }
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());

        textFileName.setText(fileName);
        File sourceFile = new File(sourceFilePath);
        long date = sourceFile.lastModified();
        String fileModifiedDate = FileUtils.convertDate(date);
        long size = sourceFile.length();
        String fileSize = Formatter.formatFileSize(mContext, size);
        textFileDate.setText(fileModifiedDate);
        textFileSize.setText(fileSize);
        Drawable drawable = FileUtils.getAppIcon(mContext, sourceFilePath);
        if (drawable != null) {
            icon.setImageDrawable(drawable);
        }
        mPasteConflictDialog.show();
//        buttonReplace.setOnClickListener(this);
//        buttonSkip.setOnClickListener(this);
//        buttonKeep.setOnClickListener(this);
        buttonReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfPasteConflictFinished(FileUtils.ACTION_REPLACE);
            }
        });

        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfPasteConflictFinished(FileUtils.ACTION_SKIP);
            }
        });

        buttonKeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfPasteConflictFinished(FileUtils.ACTION_KEEP);
            }
        });

    }

    /**
     * Shows another alert dialog when user clicks on any button Skip,Keep or Replace
     *
     * @param action Button action --> {@link FileUtils#ACTION_REPLACE,FileUtils#ACTION_SKIP,FileUtils#ACTION_KEEP}
     *               Calls Async task to do the copy operation once user resolves all conflicts
     */
    private void checkIfPasteConflictFinished(int action) {
        mPasteConflictDialog.dismiss();
        int count = ++tempConflictCounter;
        mPasteAction = action;
        mPathActionMap.put(mSourceFilePath, mPasteAction);
        Logger.log("TAG", "tempConflictCounter==" + tempConflictCounter + "tempSize==" + tempSourceFile.size());
        if (count < tempSourceFile.size()) {
            showDialog(tempSourceFile.get(count));
        } else {
            callAsyncTask();
        }
    }

    public void callAsyncTask() {
        new BackGroundOperationsTask(PASTE_OPERATION).execute(mPathActionMap);
    }


    public class BackGroundOperationsTask extends AsyncTask<HashMap<String, Integer>, Integer, Void> {

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
        TextView textFileSource;
        TextView textFileDest;
        TextView textFilesLeft;
        TextView textProgressPercent;
        ProgressBar pasteProgress;
        Progress progress;
        private int totalFiles;
        private String sourcePath;


        private BackGroundOperationsTask(int operation) {
            this.operation = operation;
            progress = new Progress(this);
            sourcePath = mSourceFilePath;

        }

        @Override
        protected void onPreExecute() {

            switch (operation) {

                case PASTE_OPERATION:
                    showProgressDialog();
                    break;

            }

        }

        private void showProgressDialog() {

            switch (operation) {
                case PASTE_OPERATION:
                    progressDialog = new Dialog(mContext);
                    progressDialog.setContentView(R.layout.dialog_progress_paste);
                    progressDialog.setCancelable(false);
                    TextView textTitle = (TextView) progressDialog.findViewById(R.id.textDialogTitle);
                    if (mIsMoveOperation) {
                        textTitle.setText(mContext.getString(R.string.msg_cut));
                    } else {
                        textTitle.setText(mContext.getString(R.string.msg_copy));
                    }

                    textFileName = (TextView) progressDialog.findViewById(R.id.textFileName);
                    textFileSource = (TextView) progressDialog.findViewById(R.id.textFileFromPath);
                    textFileDest = (TextView) progressDialog.findViewById(R.id.textFileToPath);
                    textFilesLeft = (TextView) progressDialog.findViewById(R.id.textFilesLeft);
                    textProgressPercent = (TextView) progressDialog.findViewById(R.id.textProgressPercent);
                    pasteProgress = (ProgressBar) progressDialog.findViewById(R.id.progressBarPaste);
                    Button buttonBackground = (Button) progressDialog.findViewById(R.id.buttonBg);

                    String fileName = mSourceFilePath.substring(mSourceFilePath.lastIndexOf("/") + 1, mSourceFilePath
                            .length());
                    textFileName.setText(fileName);
                    textFileSource.setText(mSourceFilePath);
                    textFileDest.setText(mCurrentDir);
                    progressDialog.show();
                    buttonBackground.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressDialog.dismiss();
                        }
                    });
                    break;
              /*  case DELETE_OPERATION:
                    deleteDialog = new Dialog(mContext);
                    deleteDialog.setContentView(R.layout.dialog_delete);
                    deleteDialog.setCancelable(false);
                    textFileName = (TextView) deleteDialog.findViewById(R.id.textFileNames);
                    break;*/

            }


        }

        @Override
        protected Void doInBackground(HashMap<String, Integer>... params) {

            HashMap<String, Integer> pathActions = params[0];
//            android.os.Debug.waitForDebugger();
            switch (operation) {

                case PASTE_OPERATION:
                    totalFiles = pathActions.size();
                    if (pathActions.size() > 0) {
                        currentFile = 0;
                        for (String key : pathActions.keySet()) {
                            int action = pathActions.get(key);
                            String sourcePath = key;
                            System.out.println("key : " + key);
                            System.out.println("value : " + pathActions.get(key));
                            if (action == FileUtils.ACTION_CANCEL) {
                                isActionCancelled = true;
                            } else {
                                currentFile++;
                                System.out.println("currentFile BG : " + currentFile);
                                Logger.log("TAG", "Destination dir==" + mCurrentDir);
                                copyStatus = FileUtils.copyToDirectory(mContext, sourcePath, mCurrentDir,
                                        mIsMoveOperation, action, progress);
                                System.out.println("copyStatus : " + copyStatus);

                                if (copyStatus == 0) {
                                    filesCopied++;
                                }

                            }


                        }
                    }
                    break;

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            textFilesLeft.setText(currentFile + "/" + totalFiles);
            int progress = values[0];
            textProgressPercent.setText(progress + "%");
            pasteProgress.setProgress(progress);

            if (progress == 100 && currentFile == totalFiles) {
//                    System.out.println("progress ELSE: " + progress + "currentFile:" + currentFile);
                progressDialog.dismiss();
            }


        }

        @Override
        protected void onPostExecute(Void result) {
            FileListFragment singlePaneFragment = (FileListFragment) mFragment
                    .getFragmentManager()
                    .findFragmentById(R
                            .id.main_container);

            FileListDualFragment dualPaneFragment = (FileListDualFragment) mFragment
                    .getFragmentManager()
                    .findFragmentById(R
                            .id.frame_container_dual);

            switch (operation) {

                case PASTE_OPERATION:

                    if (!mIsDrag) {
                        ((BaseActivity) mActivity).clearSelectedPos();
                        ((BaseActivity) mActivity).togglePasteVisibility(false);
                    }


                    if (mPathActionMap != null) {
                        if (mPathActionMap.size() != filesCopied) {

                            if (isActionCancelled) {
                                showMessage(mContext.getString(R.string.msg_operation_cancel));
                            }
                            if (mIsMoveOperation) {
                                showMessage(mContext.getString(R.string.msg_move_failure));
                            } else {
                                showMessage(mContext.getString(R.string.msg_copy_failure));
                            }

                        }
                    }

                    if (filesCopied != 0) {
                        // Refresh the list after cut/copy operation
                        if (singlePaneFragment != null) {
                            singlePaneFragment.refreshList();
                        }
                        if (dualPaneFragment != null) {
                            dualPaneFragment.refreshList();
                        }


                        if (mIsMoveOperation) {
                            showMessage(mContext.getResources().getQuantityString(R.plurals
                                            .number_of_files, filesCopied,
                                    filesCopied) + " " +
                                    mContext.getString(R.string.msg_move_success));

                        } else {
                            showMessage(mContext.getResources().getQuantityString(R.plurals.number_of_files, filesCopied,
                                    filesCopied) + " " +
                                    mContext.getString(R.string.msg_copy_success));
                        }

                    }
                    mIsMoveOperation = false;
                    progressDialog.dismiss();
                    filesCopied = 0;
                    break;

/*                case DELETE_OPERATION:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {

                        mSelectedItemPositions.clear();

                    }
//                    Toast.makeText(FilebrowserULTRAActivity.this,
//                            " Delete successfull !", Toast.LENGTH_SHORT).show();
//                    refreshList();
                    progressDialog.dismiss();

                    break;*/
            }
        }

        public class Progress {
            private BackGroundOperationsTask task;

            public Progress(BackGroundOperationsTask task) {
                this.task = task;
            }

            public void publish(int val) {
                task.publishProgress(val);
            }
        }


    }

    private void showMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }


}
