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
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.BaseFileList;
import com.siju.acexplorer.filesystem.groups.StoragesGroup;
import com.siju.acexplorer.filesystem.helper.FileOpsHelper;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.operations.OperationProgress;
import com.siju.acexplorer.filesystem.operations.OperationUtils;
import com.siju.acexplorer.filesystem.operations.Operations;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.helper.RootHelper;
import com.siju.acexplorer.utils.Dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.filesystem.app.AppUtils.getAppIcon;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getInternalStorage;


public class PasteConflictChecker extends AsyncTask<Void, String, ArrayList<FileInfo>> {

    private final ArrayList<FileInfo> mFiles;
    private final ArrayList<FileInfo> mConflictFiles = new ArrayList<>();
    private int counter = 0;
    private boolean rootmode = false;
    private final String destinationDir;
    private boolean mIsMoveOperation = false;
    private final BaseFileList fragment;
    private boolean mLowStorage;
    private FileOpsHelper fileOpsHelper;


    public PasteConflictChecker(BaseFileList fragment, String currentDir, boolean
            rootMode, boolean isMoveOperation, ArrayList<FileInfo> files) {
        this.fragment = fragment;
        destinationDir = currentDir;
        this.rootmode = rootMode;
        this.mIsMoveOperation = isMoveOperation;
        mFiles = files;
        fileOpsHelper = new FileOpsHelper(fragment);
    }

    @Override
    public void onProgressUpdate(String... message) {
        Toast.makeText(fragment.getContext(), message[0], Toast.LENGTH_LONG).show();
    }


    @Override
    protected final ArrayList<FileInfo> doInBackground(Void... params) {

        long totalBytes = 0;

        for (int i = 0; i < mFiles.size(); i++) {
            FileInfo f1 = mFiles.get(i);

            if (f1.isDirectory()) {
                totalBytes = totalBytes + FileUtils.getFolderSize(new File(f1.getFilePath()));
            } else {
                totalBytes = totalBytes + new File(f1.getFilePath()).length();
            }
        }

        File f = new File(destinationDir);
        boolean isRootDir = !destinationDir.startsWith(getInternalStorage());
        List<String> externalSDList = new StoragesGroup(fragment.getContext()).getExternalSDList();

        for (String dir : externalSDList) {
            if (destinationDir.startsWith(dir)) {
                isRootDir = false;
            }
        }
        Logger.log("PasteCOnflict", "isROotdir=" + isRootDir);

        if (isRootDir || f.getFreeSpace() >= totalBytes) {

            ArrayList<FileInfo> listFiles = RootHelper.getFilesList(destinationDir,
                    rootmode, true, false);

            for (FileInfo fileInfo : listFiles) {
                for (FileInfo copiedFiles : mFiles) {
                    if (copiedFiles.getFileName().equals(fileInfo.getFileName())) {
                        mConflictFiles.add(copiedFiles);
                    }
                }
            }
        } else {
            mLowStorage = true;
            publishProgress(fragment.getString(R.string.storage_low));
        }

        return mConflictFiles;
    }

    private final ArrayList<CopyData> mCopyData = new ArrayList<>();

    private void showDialog() {

        Logger.log("TAG", "Counter=" + counter + " conflict size=" + mConflictFiles.size());
        if (counter == mConflictFiles.size() || mConflictFiles.size() == 0) {
            if (mFiles != null && mFiles.size() != 0) {

                OperationUtils.WriteMode mode = fileOpsHelper.checkWriteAccessMode(fragment.getContext(), new File(destinationDir));
                if (mode == OperationUtils.WriteMode.EXTERNAL) {
                    Operations operation = mIsMoveOperation ? Operations.CUT : Operations.COPY;
                    fileOpsHelper.formSAFIntentMoveCopy(destinationDir, mFiles, mCopyData, operation);
                } else if (mode == OperationUtils.WriteMode.INTERNAL || mode == OperationUtils.WriteMode.ROOT) {

                    if (!mIsMoveOperation) {

                        if (rootmode || new File(destinationDir).canWrite()) {

                            Intent intent = new Intent(fragment.getActivity(), CopyService.class);
                            intent.putParcelableArrayListExtra(OperationUtils.KEY_FILES, mFiles);
                            intent.putParcelableArrayListExtra(OperationUtils.KEY_CONFLICT_DATA, mCopyData);
                            intent.putExtra(OperationUtils.KEY_FILEPATH, destinationDir);
                            new OperationProgress().showCopyProgressDialog(fragment.getContext(), intent);
                        } else {
                            Toast.makeText(fragment.getContext(), fragment.getString(R.string.msg_operation_failed),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        new MoveFiles(fragment.getContext(), mFiles, mCopyData).executeOnExecutor
                                (AsyncTask.THREAD_POOL_EXECUTOR, destinationDir);
                    }
                }
            } else {

                Toast.makeText(fragment.getContext(), mIsMoveOperation ? fragment.getString(R.string.msg_move_failure) :
                        fragment.getString(R.string.msg_copy_failure), Toast.LENGTH_SHORT).show();
            }
        } else {

            String texts[] = new String[]{fragment.getString(R.string.dialog_title_paste_conflict),
                    fragment.getString(R.string.dialog_skip), fragment.getString(R.string.dialog_keep_both), fragment.getString(R
                    .string.dialog_replace)};
            final MaterialDialog materialDialog = new Dialogs().showCustomDialog(fragment.getContext(), R.layout.dialog_paste_conflict,
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
            String fileSize = Formatter.formatFileSize(fragment.getContext(), size);
            textFileDate.setText(fileModifiedDate);
            textFileSize.setText(fileSize);
            Drawable drawable = getAppIcon(fragment.getContext(), mConflictFiles.get(counter).getFilePath());
            if (drawable != null) {
                icon.setImageDrawable(drawable);
            }

            // POSITIVE BUTTON ->SKIP   NEGATIVE ->REPLACE    NEUTRAL ->KEEP BOTH
            if (sourceFile.getParent().equals(destinationDir)) {
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
