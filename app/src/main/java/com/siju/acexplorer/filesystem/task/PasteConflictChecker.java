package com.siju.acexplorer.filesystem.task;

import android.content.Context;
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
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
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
    Context mContext;
    boolean rootmode = false;
    int openMode = 0;
    private Fragment mFragment;
    private String mCurrentDir;
    private boolean mIsDrag;
    private boolean mIsMoveOperation = false;
    private boolean mIsDualPane;


    public PasteConflictChecker(Context context, Fragment fragment, String currentDir, boolean isDrag, boolean
            rootMode, boolean isMoveOperation, boolean isDualPane) {
        mContext = context;
        mFragment = fragment;
        mCurrentDir = currentDir;
        mIsDrag = isDrag;
        this.rootmode = rootMode;
        this.mIsMoveOperation = isMoveOperation;
        mIsDualPane = isDualPane;
    }

    @Override
    public void onProgressUpdate(String... message) {
        Toast.makeText(mContext, message[0], Toast.LENGTH_LONG).show();
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

            ArrayList<FileInfo> listFiles = RootHelper.getFilesList(mContext, mCurrentDir,
                    rootmode, true);

            for (FileInfo fileInfo : listFiles) {
                for (FileInfo copiedFiles : mFiles) {
                    if (copiedFiles.getFileName().equals(fileInfo.getFileName())) {
                        mConflictFiles.add(copiedFiles);
                    }
                }
            }
        } else publishProgress(mContext.getString(R.string.storage_low));

        return mConflictFiles;
    }

    public void showDialog() {

        Logger.log("TAG", "Counter=" + counter + " confllict size=" + mConflictFiles.size());
        if (counter == mConflictFiles.size() || mConflictFiles.size() == 0) {

            if (mFiles != null && mFiles.size() != 0) {

                int mode = FileUtils.checkFolder(new File(mCurrentDir), mContext);
                if (mode == 2) {
                  /*  mainActivity.oparrayList = (ab);
                    mainActivity.operation = move ? FileConstants.MOVE : FileConstants.COPY;
                    mainActivity.oppathe = path;*/
                } else if (mode == 1 || mode == 0) {

                    if (!mIsMoveOperation) {

                        Intent intent = new Intent(mContext, CopyService.class);
                        intent.putParcelableArrayListExtra("FILE_PATHS", mFiles);
                        intent.putExtra("COPY_DIRECTORY", mCurrentDir);
                        intent.putExtra("MODE", openMode);
                        mContext.startService(intent);
                    } else {

                        new MoveFiles(mContext, mFragment, mFiles, mIsDualPane).executeOnExecutor
                                (AsyncTask.THREAD_POOL_EXECUTOR, mCurrentDir);
                    }
                }
            } else {

                Toast.makeText(mContext, mContext.getString(R.string.msg_move_failure), Toast.LENGTH_SHORT).show();
            }
        } else {

            String texts[] = new String[]{mContext.getString(R.string.dialog_title_paste_conflict),
                    mContext.getString(R.string.dialog_skip), mContext.getString(R.string.dialog_keep_both), mContext.getString(R
                    .string.dialog_replace)};
            final MaterialDialog materialDialog = new DialogUtils().showCustomDialog(mContext, R.layout.dialog_paste_conflict,
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
            String fileSize = Formatter.formatFileSize(mContext, size);
            textFileDate.setText(fileModifiedDate);
            textFileSize.setText(fileSize);
            Drawable drawable = FileUtils.getAppIcon(mContext, mConflictFiles.get(counter).getFilePath());
            if (drawable != null) {
                icon.setImageDrawable(drawable);
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
