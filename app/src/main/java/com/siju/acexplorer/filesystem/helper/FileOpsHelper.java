package com.siju.acexplorer.filesystem.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.AceActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.BaseFileList;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.task.CreateZipTask;
import com.siju.acexplorer.filesystem.task.DeleteTask;
import com.siju.acexplorer.filesystem.task.ExtractService;
import com.siju.acexplorer.filesystem.utils.FileOperations;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.utils.Dialogs;
import com.siju.acexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;


public class FileOpsHelper {

    private final BaseFileList context;
    private final String TAG = this.getClass().getSimpleName();

    public FileOpsHelper(BaseFileList baseFileList) {
        context = baseFileList;
    }


    public void mkDir(final boolean rootMode, final String path, final String fileName) {
        FileOperations.mkdir(path, fileName, context, rootMode, new FileOperations.FileOperationCallBack() {
            @Override
            public void exists() {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.showMessage(context, mActivity.getString(R.string.file_exists));
                        String newFilePath = path + File.separator + fileName;

//                        if (ma != null && ma.getActivity() != null)
                        new Dialogs().createDirDialog(context, rootMode, newFilePath);

                    }
                });
            }

            @Override
            public void launchSAF(final File file) {
//                if (toast != null) toast.cancel();
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.mNewFilePath = path;
                        mActivity.mFileName = fileName;
                        mActivity.mOperation = FileConstants.FOLDER_CREATE;
                        showSAFDialog(mActivity.mNewFilePath);

                    }
                });

            }

            @Override
            public void launchSAF(File oldFile, File newFile) {

            }


            @Override
            public void opCompleted(File hFile, final boolean success) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (success) {
                            Intent intent = new Intent(FileConstants.REFRESH);
                            intent.putExtra(FileConstants.OPERATION, FileConstants.FOLDER_CREATE);
                            context.getActivity().sendBroadcast(intent);
                            String newFilePath = path + File.separator + fileName;

                            FileUtils.scanFile(context.getActivity().getApplicationContext(), newFilePath);

                        } else
                            Toast.makeText(context.getContext(), R.string.msg_operation_failed,
                                    Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }


    public void mkFile(final boolean rootMode, final File file) {
        /*final Toast toast=Toast.makeText(ma.getActivity(), R.string.creatingfolder, Toast.LENGTH_LONG);
        toast.show();*/
        FileOperations.mkfile(file, context, rootMode, new FileOperations.FileOperationCallBack() {
            @Override
            public void exists() {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (toast != null) toast.cancel();
                        FileUtils.showMessage(context.getContext(), context.getString(R.string.file_exists));

//                        if (ma != null && ma.getActivity() != null)
                        new Dialogs().createFileDialog(context, rootMode, file.getAbsolutePath());

                    }
                });
            }

            @Override
            public void launchSAF(final File file) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.mNewFilePath = file.getAbsolutePath();
                        mActivity.mOperation = FileConstants.FILE_CREATE;

                        showSAFDialog(mActivity.mNewFilePath);
                    }
                });

            }

            @Override
            public void launchSAF(File oldFile, File newFile) {

            }


            @Override
            public void opCompleted(final File file, final boolean success) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (success) {
                            Intent intent = new Intent(FileConstants.REFRESH);
                            intent.putExtra(FileConstants.OPERATION, FileConstants.FILE_CREATE);
                            context.getActivity().sendBroadcast(intent);
                            FileUtils.scanFile(context.getActivity().getApplicationContext(), file.getAbsolutePath());

                        } else
                            Toast.makeText(context.getContext(), R.string.msg_operation_failed,
                                    Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }


    public void renameFile(boolean rootmode, final File oldFile, final File newFile, final int position) {
        Logger.log(TAG, "Rename--oldFile=" + oldFile + " new file=" + newFile);
        FileOperations.rename(oldFile, newFile, rootmode, context, new FileOperations.FileOperationCallBack() {
            @Override
            public void exists() {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.showMessage(context, mActivity.getString(R.string.file_exists));
                    }
                });
            }

            @Override
            public void launchSAF(final File file) {

            }

            @Override
            public void launchSAF(final File oldFile, final File newFile) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.mRenamedPosition = position;
                        mActivity.mOldFilePath = oldFile.getAbsolutePath();
                        mActivity.mNewFilePath = newFile.getAbsolutePath();
                        mActivity.mOperation = FileConstants.RENAME;
                        showSAFDialog(mActivity.mNewFilePath);

                    }
                });

            }


            @Override
            public void opCompleted(final File file, final boolean success) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Intent intent = new Intent(FileConstants.REFRESH);
                            intent.putExtra(FileConstants.OPERATION, FileConstants.RENAME);
                            intent.putExtra("position", position);
                            intent.putExtra("old_file", oldFile.getAbsolutePath());
                            intent.putExtra("new_file", file.getAbsolutePath());
                            context.getActivity().sendBroadcast(intent);

                        } else
                            Toast.makeText(context.getContext(), R.string.msg_operation_failed,
                                    Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void deleteFiles(ArrayList<FileInfo> files, boolean isRooted) {
        if (files == null) return;

        int mode = checkWriteAccessMode(context.getContext(), new File(files.get(0).getFilePath()).getParentFile());
        if (mode == 2) {
            mActivity.mFiles = files;
            mActivity.mOperation = FileConstants.DELETE;
        } else if (mode == 1 || mode == 0)
            new DeleteTask(context.getContext(), isRooted, files).execute();
    }

    public void extractFile(File currentFile, File file) {
        int mode = checkWriteAccessMode(context.getContext(), file.getParentFile());
        if (mode == 2) {
            mActivity.mOldFilePath = currentFile.getAbsolutePath();
            mActivity.mNewFilePath = file.getAbsolutePath();
            mActivity.mOperation = FileConstants.EXTRACT;

        } else if (mode == 1) {
            Intent intent = new Intent(context.getActivity(), ExtractService.class);
            intent.putExtra("zip", currentFile.getPath());
            intent.putExtra("new_path", file.getAbsolutePath());
            new FileUtils().showExtractProgressDialog(context.getContext(), intent);
        } else
            Toast.makeText(context.getContext(), R.string.msg_operation_failed, Toast.LENGTH_SHORT).show();
    }

    public void compressFile(File newFile, ArrayList<FileInfo> files) {
        int mode = checkWriteAccessMode(context.getContext(), newFile.getParentFile());
        if (mode == 2) {
            mActivity.mNewFilePath = newFile.getAbsolutePath();
            mActivity.mFiles = files;
            mActivity.mOperation = FileConstants.COMPRESS;
        } else if (mode == 1) {
            Intent zipIntent = new Intent(context.getActivity(), CreateZipTask.class);
            zipIntent.putExtra("name", newFile.getAbsolutePath());
            zipIntent.putParcelableArrayListExtra("files", files);
            new FileUtils().showZipProgressDialog(context.getContext(), zipIntent);
        } else
            Toast.makeText(context.getContext(), R.string.msg_operation_failed, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("ConstantConditions")
    private void showSAFDialog(String path) {

        String title = mActivity.getString(R.string.needsaccess);
        String texts[] = new String[]{title, mActivity.getString(R.string.open), "", mActivity.getString(R.string
                .dialog_cancel)};
        final MaterialDialog materialDialog = new Dialogs().showCustomDialog(context.getContext(), R.layout.dialog_saf, texts);
        View view = materialDialog.getCustomView();
        TextView textView = (TextView) view.findViewById(R.id.description);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.sd_operate_step);
        textView.setText(context.getString(R.string.needs_access_summary, path));

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                triggerStorageAccessFramework();
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context.getContext(), context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (context.getActivity().getPackageManager().resolveActivity(intent, 0) != null) {
            context.startActivityForResult(intent, BaseFileList.SAF_REQUEST);
        } else {
            Toast.makeText(context, mActivity.getString(R.string.msg_error_not_supported), Toast.LENGTH_LONG).show();
        }
    }


    private int checkWriteAccessMode(Context context, final File folder) {
        if (Utils.isAtleastLollipop() && FileUtils.isOnExtSdCard(folder, context)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return FileConstants.WRITE_MODES.ROOT.getValue();
            }

            if (FileUtils.isFileNonWritable(folder, context)) {
                // On Android 5 and above, trigger storage access framework.
                showSAFDialog(folder.getAbsolutePath());
                return FileConstants.WRITE_MODES.EXTERNAL.getValue();
            }
            return FileConstants.WRITE_MODES.INTERNAL.getValue();
        } else if (Utils.isKitkat() && FileUtils.isOnExtSdCard(folder, context)) {
            // Assume that Kitkat workaround works
            return FileConstants.WRITE_MODES.INTERNAL.getValue();
        } else if (FileUtils.isWritable(new File(folder, "DummyFile"))) {
            return FileConstants.WRITE_MODES.INTERNAL.getValue();
        } else {
            return FileConstants.WRITE_MODES.ROOT.getValue();
        }
    }


}
