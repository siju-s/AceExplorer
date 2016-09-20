package com.siju.acexplorer.filesystem.task;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.helper.RootHelper;
import com.siju.acexplorer.utils.DialogUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Siju on 21-08-2016.
 */
public class PasteConflictChecker extends AsyncTask<ArrayList<FileInfo>, String, ArrayList<FileInfo>> {
    Boolean move;
    ArrayList<FileInfo> mFiles;
    ArrayList<FileInfo> mConflictFiles = new ArrayList<>();
    int counter = 0;
    //    Context mContext;
    boolean rootmode = false;
    int openMode = 0;
    private Fragment mFragment;
    private String mCurrentDir;
    private boolean mIsDrag;
    private boolean mIsMoveOperation = false;
    private boolean mIsDualPane;
    private BaseActivity mActivity;


    public PasteConflictChecker(BaseActivity context, Fragment fragment, String currentDir, boolean isDrag, boolean
            rootMode, boolean isMoveOperation, boolean isDualPane) {
        mActivity = context;
        mFragment = fragment;
        mCurrentDir = currentDir;
        mIsDrag = isDrag;
        this.rootmode = rootMode;
        this.mIsMoveOperation = isMoveOperation;
        mIsDualPane = isDualPane;
    }

    @Override
    public void onProgressUpdate(String... message) {
        Toast.makeText(mActivity, message[0], Toast.LENGTH_LONG).show();
    }

    @SafeVarargs
    @Override
    // Actual download method, run in the task thread
    protected final ArrayList<FileInfo> doInBackground(ArrayList<FileInfo>... params) {


        mFiles = params[0];
        long totalBytes = 0;

        for (int i = 0; i < mFiles.size(); i++) {
            FileInfo f1 = mFiles.get(i);

            if (f1.isDirectory()) {
                totalBytes = totalBytes + FileUtils.getFolderSize(new File(f1.getFilePath()));
            } else {
                totalBytes = totalBytes + new File(f1.getFilePath()).length();
            }
        }

        File f = new File(mCurrentDir);
        if (f.getUsableSpace() >= totalBytes) {

            ArrayList<FileInfo> listFiles = RootHelper.getFilesList(mActivity, mCurrentDir,
                    rootmode, true);

            for (FileInfo fileInfo : listFiles) {
                for (FileInfo copiedFiles : mFiles) {
                    if (copiedFiles.getFileName().equals(fileInfo.getFileName())) {
                        mConflictFiles.add(copiedFiles);
                    }
                }
            }
        } else publishProgress(mActivity.getString(R.string.storage_low));

        return mConflictFiles;
    }

    ArrayList<CopyData> mCopyData = new ArrayList<>();

    public void showDialog() {

        Logger.log("TAG", "Counter=" + counter + " confllict size=" + mConflictFiles.size());
        if (counter == mConflictFiles.size() || mConflictFiles.size() == 0) {
            if (mFiles != null && mFiles.size() != 0) {

                int mode = mActivity.mFileOpsHelper.checkFolder(mActivity, new File(mCurrentDir));
                if (mode == 2) {
                    mActivity.mFiles = mFiles;
                    mActivity.mOperation = mIsMoveOperation ? FileConstants.MOVE : FileConstants.COPY;
                    mActivity.mCopyData = mCopyData;
                    mActivity.mNewFilePath = mCurrentDir;
                } else if (mode == 1 || mode == 0) {

                    if (!mIsMoveOperation) {

                        Intent intent = new Intent(mActivity, CopyService.class);
                        intent.putParcelableArrayListExtra("FILE_PATHS", mFiles);
                        intent.putParcelableArrayListExtra("ACTION",mCopyData);
                        intent.putExtra("COPY_DIRECTORY", mCurrentDir);
                        intent.putExtra("MODE", openMode);
                        mActivity.startService(intent);
                    } else {
                        new MoveFiles(mActivity, mFiles,mCopyData).executeOnExecutor
                                (AsyncTask.THREAD_POOL_EXECUTOR, mCurrentDir);
                    }
                }
            } else {

                Toast.makeText(mActivity, mActivity.getString(R.string.msg_move_failure), Toast.LENGTH_SHORT).show();
            }
        } else {

            String texts[] = new String[]{mActivity.getString(R.string.dialog_title_paste_conflict),
                    mActivity.getString(R.string.dialog_skip), mActivity.getString(R.string.dialog_keep_both), mActivity.getString(R
                    .string.dialog_replace)};
            final MaterialDialog materialDialog = new DialogUtils().showCustomDialog(mActivity, R.layout.dialog_paste_conflict,
                    texts);


            final CheckBox checkBox = (CheckBox) materialDialog.findViewById(R.id.checkBox);
            ImageView icon = (ImageView) materialDialog.findViewById(R.id.imageFileIcon);
            TextView textFileName = (TextView) materialDialog.findViewById(R.id.textFileName);
            TextView textFileDate = (TextView) materialDialog.findViewById(R.id.textFileDate);
            TextView textFileSize = (TextView) materialDialog.findViewById(R.id.textFileSize);
            /*String fileName = mFiles.get(counter).getFilePath().substring(sourceFilePath.lastIndexOf("/") + 1,
                    sourceFilePath
                    .length());*/



            String fileName = mConflictFiles.get(counter).getFileName();
            textFileName.setText(fileName);
            File sourceFile = new File(mConflictFiles.get(counter).getFilePath());
            long date = sourceFile.lastModified();
            String fileModifiedDate = FileUtils.convertDate(date);
            long size = sourceFile.length();
            String fileSize = Formatter.formatFileSize(mActivity, size);
            textFileDate.setText(fileModifiedDate);
            textFileSize.setText(fileSize);
            Drawable drawable = FileUtils.getAppIcon(mActivity, mConflictFiles.get(counter).getFilePath());
            if (drawable != null) {
                icon.setImageDrawable(drawable);
            }

            // POSITIVE BUTTON ->SKIP   NEGATIVE ->REPLACE    NEUTRAL ->KEEP BOTH
            if (sourceFile.getParent().equals(mCurrentDir)) {
                if (mIsMoveOperation) {
                    materialDialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
                    materialDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
                } else {
                    materialDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
                }
            }

            if (new File(mConflictFiles.get(counter).getFilePath()).isDirectory()) {
                materialDialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
            }


            materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (counter < mConflictFiles.size()) {

                        if (!checkBox.isChecked()) {

                            mFiles.remove(mConflictFiles.get(counter));
                            counter++;

                        } else {
                            for (int j = counter; j < mConflictFiles.size(); j++) {

                                mFiles.remove(mConflictFiles.get(j));
                            }
                            counter = mConflictFiles.size();
                        }

                        materialDialog.dismiss();
                        showDialog();
                    }
                }
            });

            materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (counter < mConflictFiles.size()) {
                        if (!checkBox.isChecked()) {
                            counter++;
                        } else {
                            counter = mConflictFiles.size();
                        }
                        materialDialog.dismiss();
                        showDialog();
                    }
                }
            });

            materialDialog.getActionButton(DialogAction.NEUTRAL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (counter < mConflictFiles.size()) {
                        if (!checkBox.isChecked()) {
                            mCopyData.add(new CopyData(mConflictFiles.get(counter).getFilePath(), FileUtils
                                    .ACTION_KEEP));
                            counter++;
                        } else {
                            for (int i = 0; i < mConflictFiles.size(); i++) {
//                                if (!mCopyData.contains(mConflictFiles.get(i).getFilePath())) {
                                    mCopyData.add(new CopyData(mConflictFiles.get(counter).getFilePath(), FileUtils
                                            .ACTION_KEEP));
//                                }
                            }
                            counter = mConflictFiles.size();
                        }
                        materialDialog.dismiss();
                        showDialog();
                    }
                }
            });


            materialDialog.show();
        /*    if (ab.get(0).getParent().equals(path)) {
                View negative = y.getActionButton(DialogAction.NEGATIVE);
                negative.setEnabled(false);
            }*/
        }
    }

    @Override
    protected void onPostExecute(ArrayList<FileInfo> strings) {
        super.onPostExecute(strings);
        showDialog();
    }
}
