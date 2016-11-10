package com.siju.acexplorer.filesystem.task;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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


public class PasteConflictChecker extends AsyncTask<Void, String, ArrayList<FileInfo>> {

    private final ArrayList<FileInfo> mFiles;
    private final ArrayList<FileInfo> mConflictFiles = new ArrayList<>();
    private int counter = 0;
    private boolean rootmode = false;
    private final String mCurrentDir;
    private boolean mIsMoveOperation = false;
    private final BaseActivity mActivity;
    private ArrayList<FileInfo> mTotalFileList;
    private boolean mLowStorage;


    public PasteConflictChecker(BaseActivity context, String currentDir, boolean
            rootMode, boolean isMoveOperation,ArrayList<FileInfo> files) {
        mActivity = context;
        mCurrentDir = currentDir;
        this.rootmode = rootMode;
        this.mIsMoveOperation = isMoveOperation;
        mFiles = files;
    }

    @Override
    public void onProgressUpdate(String... message) {
        Toast.makeText(mActivity, message[0], Toast.LENGTH_LONG).show();
    }


    @Override
    protected final ArrayList<FileInfo> doInBackground(Void... params) {

        long totalBytes = 0;
        mTotalFileList  = new ArrayList<>();

        for (int i = 0; i < mFiles.size(); i++) {
            FileInfo f1 = mFiles.get(i);

            if (f1.isDirectory()) {

                ArrayList<FileInfo> listFiles = new RootHelper().getFilesListRecursively(mActivity, f1.getFilePath(),
                        rootmode);

                int childCount = listFiles.size();
                if (childCount == 0) {
                    mTotalFileList.add(f1);
                } else {
                    mTotalFileList.addAll(listFiles);
                }
                totalBytes = totalBytes + FileUtils.getFolderSize(new File(f1.getFilePath()));
            } else {
                totalBytes = totalBytes + new File(f1.getFilePath()).length();
                mTotalFileList.add(f1);
            }
        }

        File f = new File(mCurrentDir);
        if (f.getUsableSpace() >= totalBytes) {

            ArrayList<FileInfo> listFiles = RootHelper.getFilesList(mCurrentDir,
                    rootmode, true,false);

            for (FileInfo fileInfo : listFiles) {
                for (FileInfo copiedFiles : mFiles) {
                    if (copiedFiles.getFileName().equals(fileInfo.getFileName())) {
                        mConflictFiles.add(copiedFiles);
                    }
                }
            }
        } else  {
            mLowStorage = true;
            publishProgress(mActivity.getString(R.string.storage_low));
        }

        return mConflictFiles;
    }

    private final ArrayList<CopyData> mCopyData = new ArrayList<>();

    private void showDialog() {

        Logger.log("TAG", "Counter=" + counter + " conflict size=" + mConflictFiles.size());
        if (counter == mConflictFiles.size() || mConflictFiles.size() == 0) {
            if (mFiles != null && mFiles.size() != 0) {

                int mode = mActivity.mFileOpsHelper.checkWriteAccessMode(mActivity, new File(mCurrentDir));
                if (mode == 2) {
                    mActivity.mFiles = mFiles;
                    mActivity.mTotalFiles = mTotalFileList;
                    mActivity.mOperation = mIsMoveOperation ? FileConstants.MOVE : FileConstants.COPY;
                    mActivity.mCopyData = mCopyData;
                    mActivity.mNewFilePath = mCurrentDir;
                } else if (mode == 1 || mode == 0) {

                    if (!mIsMoveOperation) {

                        Intent intent = new Intent(mActivity, CopyService.class);
                        intent.putParcelableArrayListExtra("FILE_PATHS", mFiles);
                        intent.putParcelableArrayListExtra("ACTION",mCopyData);
                        intent.putExtra("COPY_DIRECTORY", mCurrentDir);
                        int openMode = 0;
                        intent.putExtra("MODE", openMode);
                        intent.putParcelableArrayListExtra("TOTAL_LIST",mTotalFileList);
                        new FileUtils().showCopyProgressDialog(mActivity,intent);

                    } else {
                        new MoveFiles(mActivity, mFiles,mCopyData).executeOnExecutor
                                (AsyncTask.THREAD_POOL_EXECUTOR, mCurrentDir);
                    }
                }
            } else {

                Toast.makeText(mActivity, mIsMoveOperation ? mActivity.getString(R.string.msg_move_failure) :
                        mActivity.getString(R.string.msg_copy_failure), Toast.LENGTH_SHORT).show();
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
                            mCopyData.add(new CopyData(mConflictFiles.get(counter).getFilePath()));
                            counter++;
                        } else {
                            for (int i = 0; i < mConflictFiles.size(); i++) {
//                                if (!mCopyData.contains(mConflictFiles.get(i).getFilePath())) {
                                    mCopyData.add(new CopyData(mConflictFiles.get(counter).getFilePath()));
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
        }
    }

    @Override
    protected void onPostExecute(ArrayList<FileInfo> strings) {
        super.onPostExecute(strings);
        if (!mLowStorage)
        showDialog();
    }
}
