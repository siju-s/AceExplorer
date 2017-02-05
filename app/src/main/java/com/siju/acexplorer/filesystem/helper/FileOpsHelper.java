package com.siju.acexplorer.filesystem.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.BaseFileList;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.operations.OperationProgress;
import com.siju.acexplorer.filesystem.operations.OperationUtils;
import com.siju.acexplorer.filesystem.operations.Operations;
import com.siju.acexplorer.filesystem.task.CreateZipTask;
import com.siju.acexplorer.filesystem.task.DeleteTask;
import com.siju.acexplorer.filesystem.task.ExtractService;
import com.siju.acexplorer.filesystem.utils.FileOperations;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.utils.Dialogs;
import com.siju.acexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_CONFLICT_DATA;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_POSITION;
import static com.siju.acexplorer.filesystem.operations.Operations.FILE_CREATION;
import static com.siju.acexplorer.filesystem.operations.Operations.FOLDER_CREATION;
import static com.siju.acexplorer.filesystem.operations.Operations.RENAME;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.isOnExtSdCard;


public class FileOpsHelper {

    private final BaseFileList context;
    private final String TAG = this.getClass().getSimpleName();

    public FileOpsHelper(BaseFileList baseFileList) {
        context = baseFileList;
    }


    public void mkDir(final File file, final boolean isRoot) {
        FileOperations.mkdir(context.getContext(), file, isRoot, new FileOperations.FileOperationCallBack() {
            @Override
            public void exists() {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.showMessage(context.getContext(), context.getString(R.string.file_exists));
                        new Dialogs().createDirDialog(context, isRoot, file.getAbsolutePath());

                    }
                });
            }

            @Override
            public void launchSAF(final File file) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        formSAFIntentCreation(file.getAbsolutePath(), Operations.FOLDER_CREATION);
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
                            Intent intent = new Intent(ACTION_OP_REFRESH);
                            intent.putExtra(OperationUtils.KEY_OPERATION, FOLDER_CREATION);
                            context.getActivity().sendBroadcast(intent);
                            scanFile(context.getActivity().getApplicationContext(), file.getAbsolutePath());

                        } else
                            Toast.makeText(context.getContext(), R.string.msg_operation_failed,
                                    Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }


    public void mkFile(final File file, final boolean isRoot) {
        FileOperations.mkfile(context.getContext(), file, isRoot, new FileOperations.FileOperationCallBack() {
            @Override
            public void exists() {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.showMessage(context.getContext(), context.getString(R.string.file_exists));
                        new Dialogs().createFileDialog(context, isRoot, file.getAbsolutePath());
                    }
                });
            }

            @Override
            public void launchSAF(final File file) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        formSAFIntentCreation(file.getAbsolutePath(), FILE_CREATION);

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
                            Intent intent = new Intent(ACTION_OP_REFRESH);
                            intent.putExtra(OperationUtils.KEY_OPERATION, FILE_CREATION);
                            context.getActivity().sendBroadcast(intent);
                            scanFile(context.getActivity().getApplicationContext(), file.getAbsolutePath());

                        } else
                            Toast.makeText(context.getContext(), R.string.msg_operation_failed,
                                    Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }


    public void renameFile(final File oldFile, final File newFile, final int position, boolean rootmode) {
        Logger.log(TAG, "Rename--oldFile=" + oldFile + " new file=" + newFile);
        FileOperations.rename(context.getContext(), oldFile, newFile, rootmode, new FileOperations.FileOperationCallBack() {
            @Override
            public void exists() {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.showMessage(context.getContext(), context.getString(R.string.file_exists));
                    }
                });
            }

            @Override
            public void launchSAF(final File file) {

            }

            @Override
            public void launchSAF(final File oldFile, final File newFile) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        formSAFIntentRename(oldFile.getAbsolutePath(), RENAME, newFile.getAbsolutePath(),
                                position);
                    }
                });

            }


            @Override
            public void opCompleted(final File file, final boolean success) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Intent intent = new Intent(ACTION_OP_REFRESH);
                            intent.putExtra(KEY_OPERATION, RENAME);
                            intent.putExtra(KEY_POSITION, position);
                            intent.putExtra(KEY_FILEPATH, oldFile.getAbsolutePath());
                            intent.putExtra(KEY_FILEPATH2, file.getAbsolutePath());
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

        OperationUtils.WriteMode mode = checkWriteAccessMode(context.getContext(), new File(files.get(0).getFilePath()).getParentFile());
        if (mode == OperationUtils.WriteMode.EXTERNAL) {
            formSAFIntentDelete(files.get(0).getFilePath(), files, Operations.DELETE);
        } else if (mode == OperationUtils.WriteMode.INTERNAL || mode == OperationUtils.WriteMode.ROOT)
            new DeleteTask(context.getContext(), isRooted, files).execute();
    }

    public void extractFile(File currentFile, File file) {
        OperationUtils.WriteMode mode = checkWriteAccessMode(context.getContext(), file.getParentFile());
        if (mode == OperationUtils.WriteMode.EXTERNAL) {
            formSAFIntentExtract(file.getAbsolutePath(), Operations.EXTRACT, currentFile.getAbsolutePath());
        } else if (mode == OperationUtils.WriteMode.INTERNAL) {
            Intent intent = new Intent(context.getActivity(), ExtractService.class);
            intent.putExtra(KEY_FILEPATH, currentFile.getPath());
            intent.putExtra(KEY_FILEPATH2, file.getAbsolutePath());
            new OperationProgress().showExtractProgressDialog(context.getContext(), intent);
        } else
            Toast.makeText(context.getContext(), R.string.msg_operation_failed, Toast.LENGTH_SHORT).show();
    }

    public void compressFile(File newFile, ArrayList<FileInfo> files) {
        OperationUtils.WriteMode mode = checkWriteAccessMode(context.getContext(), newFile.getParentFile());
        if (mode == OperationUtils.WriteMode.EXTERNAL) {
            formSAFIntentCompress(newFile.getAbsolutePath(), files, Operations.COMPRESS);
        } else if (mode == OperationUtils.WriteMode.INTERNAL) {
            Intent zipIntent = new Intent(context.getActivity(), CreateZipTask.class);
            zipIntent.putExtra(KEY_FILEPATH, newFile.getAbsolutePath());
            zipIntent.putParcelableArrayListExtra(KEY_FILES, files);
            new OperationProgress().showZipProgressDialog(context.getContext(), zipIntent);
        } else
            Toast.makeText(context.getContext(), R.string.msg_operation_failed, Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void formSAFIntentCreation(String path, Operations operations) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(KEY_FILEPATH, path);
        intent.putExtra(KEY_OPERATION, operations);
        showSAFDialog(intent, path);
    }

    private void formSAFIntentExtract(String path, Operations operations, String newFile) {
        formSAFIntentRename(path, operations, newFile, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void formSAFIntentRename(String path, Operations operations, String newFile, int position) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(KEY_FILEPATH, path);
        intent.putExtra(KEY_FILEPATH2, newFile);
        intent.putExtra(KEY_POSITION, position);
        intent.putExtra(KEY_OPERATION, operations);
        showSAFDialog(intent, path);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void formSAFIntentCompress(String path, ArrayList<FileInfo> files, Operations operations) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(KEY_FILEPATH, path);
        intent.putExtra(KEY_FILES, files);
        intent.putExtra(KEY_OPERATION, operations);
        showSAFDialog(intent, path);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void formSAFIntentMoveCopy(String destinationDir, ArrayList<FileInfo> files,
                                      ArrayList<CopyData> copyData, Operations operations) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(KEY_FILES, files);
        intent.putExtra(KEY_CONFLICT_DATA, copyData);
        intent.putExtra(KEY_FILEPATH, destinationDir);
        intent.putExtra(KEY_OPERATION, operations);
        showSAFDialog(intent, destinationDir);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void formSAFIntentDelete(String path, ArrayList<FileInfo> files, Operations operations) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(KEY_FILES, files);
        intent.putExtra(KEY_OPERATION, operations);
        showSAFDialog(intent, path);
    }


    @SuppressWarnings("ConstantConditions")
    private void showSAFDialog(final Intent intent, String path) {

        String title = context.getString(R.string.needsaccess);
        String texts[] = new String[]{title, context.getString(R.string.open), "", context.getString(R.string
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
                triggerStorageAccessFramework(intent);
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
    private void triggerStorageAccessFramework(Intent intent) {
        if (context.getActivity().getPackageManager().resolveActivity(intent, 0) != null) {
            context.startActivityForResult(intent, BaseFileList.SAF_REQUEST);
        } else {
            Toast.makeText(context.getContext(), context.getString(R.string.msg_error_not_supported), Toast.LENGTH_LONG).show();
        }
    }


    public OperationUtils.WriteMode checkWriteAccessMode(Context context, final File folder) {
        if (Utils.isAtleastLollipop() && isOnExtSdCard(folder, context)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return OperationUtils.WriteMode.ROOT;
            }

            if (FileUtils.isFileNonWritable(folder, context)) {
                // On Android 5 and above, trigger storage access framework.
                return OperationUtils.WriteMode.EXTERNAL;
            }
            return OperationUtils.WriteMode.INTERNAL;
        } else if (Utils.isKitkat() && isOnExtSdCard(folder, context)) {
            // Assume that Kitkat workaround works
            return OperationUtils.WriteMode.INTERNAL;
        } else if (FileUtils.isWritable(new File(folder, "DummyFile"))) {
            return OperationUtils.WriteMode.INTERNAL;
        } else {
            return OperationUtils.WriteMode.ROOT;
        }
    }


}
