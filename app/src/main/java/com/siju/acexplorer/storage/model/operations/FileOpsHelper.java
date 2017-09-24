/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.storage.model.operations;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileOperations;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.SdkHelper;
import com.siju.acexplorer.model.root.RootDeniedException;
import com.siju.acexplorer.model.root.RootOperations;
import com.siju.acexplorer.model.root.RootUtils;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.StorageModelImpl;
import com.siju.acexplorer.storage.model.task.CreateZipService;
import com.siju.acexplorer.storage.model.task.DeleteTask;
import com.siju.acexplorer.storage.model.task.ExtractService;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.model.StorageUtils.isOnExtSdCard;
import static com.siju.acexplorer.model.helper.FileOperations.renameFolder;
import static com.siju.acexplorer.model.root.RootOperations.renameRoot;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_CONFLICT_DATA;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_POSITION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.WriteMode.INTERNAL;
import static com.siju.acexplorer.storage.model.operations.Operations.FILE_CREATION;
import static com.siju.acexplorer.storage.model.operations.Operations.FOLDER_CREATION;


public class FileOpsHelper {

    private static final String TAG = "FileOpsHelper";
    private static final String OPERATION_INTENT = "operation_intent";
    private final int INVALID_POS = -1;


    public interface FileOperationCallBack {
        void exists(Operations operation);

        void launchSAF(Operations operation, File file);

        void launchSAF(Operations operation, File oldFile, File newFile, int position);

        void opCompleted(Operations operation, File file, boolean result);

        void opCompleted(Operations operation, File oldFile, File newFile, int position, boolean
                result);

    }


    public static void mkDir(final File file, final boolean isRoot, FileOperationCallBack
            fileOperationCallBack) {
        RootTools.debugMode = true;
        if (file == null) {
            return;
        }

        if (file.exists()) {
            fileOperationCallBack.exists(FOLDER_CREATION);
        }

        OperationUtils.WriteMode mode = OperationUtils.checkFolder(file.getParent());
        switch (mode) {
            case ROOT:
                boolean result = FileOperations.mkdir(file);
                if (!result && isRoot) {
                    try {
                        String parentPath = file.getParent();
                        RootUtils.mountRW(parentPath);
                        RootUtils.mkDir(file.getAbsolutePath());
                        result = true;
                        RootUtils.mountRO(parentPath);
                    } catch (RootDeniedException e) {
                        result = false;
                        Logger.log(TAG, file.getAbsolutePath());
                    }
                }
                fileOperationCallBack.opCompleted(FOLDER_CREATION, file, result);
                break;

            case EXTERNAL:
                fileOperationCallBack.launchSAF(FOLDER_CREATION, file);
                break;
            case INTERNAL:
                boolean result1 = FileOperations.mkdir(file);
                fileOperationCallBack.opCompleted(FOLDER_CREATION, file, result1);
                break;

        }
    }


    public static void mkFile(final File file, final boolean isRoot, FileOperationCallBack
            fileOperationCallBack) {

        if (file == null) {
            return;
        }

        if (file.exists()) {
            fileOperationCallBack.exists(FILE_CREATION);
        }

        OperationUtils.WriteMode mode = OperationUtils.checkFolder(file.getParent());
        switch (mode) {
            case ROOT:
                boolean exists = false;
                try {
                    exists = RootOperations.fileExists(file.getAbsolutePath(), true);
                } catch (RootDeniedException e) {
                    e.printStackTrace();
                    fileOperationCallBack.opCompleted(FILE_CREATION, file, false);
                }
                Log.d(TAG, "doInBackground: exists=" + exists);
                if (exists) {
                    fileOperationCallBack.exists(FILE_CREATION);
                } else {
                    boolean result = FileOperations.mkfile(file);
                    if (!result && isRoot) {
                        try {
                            String parentPath = file.getParent();
                            RootUtils.mountRW(parentPath);
                            RootUtils.mkFile(file.getAbsolutePath());
                            result = RootUtils.fileExists(file.getAbsolutePath(), false);
                            RootUtils.mountRO(parentPath);
                        } catch (RootDeniedException e) {
                            result = false;
                            Logger.log(TAG, file.getAbsolutePath());
                        }
                    }
                    fileOperationCallBack.opCompleted(FILE_CREATION, file, result);
                }
                break;

            case EXTERNAL:
                fileOperationCallBack.launchSAF(FILE_CREATION, file);
                break;
            case INTERNAL:
                boolean result = FileOperations.mkfile(file);
                fileOperationCallBack.opCompleted(FILE_CREATION, file, result);
                break;
        }
    }


    public static void renameFile(final File oldFile, final File newFile, final int position,
                                  boolean isRooted, FileOperationCallBack fileOperationCallBack) {
        Logger.log(TAG, "Rename--oldFile=" + oldFile + " new file=" + newFile);
        if (newFile.exists()) {
            fileOperationCallBack.exists(Operations.RENAME);
        }

        OperationUtils.WriteMode mode = OperationUtils.checkFolder(oldFile.getParent());
        Logger.log(TAG, "Rename--mode=" + mode);

        switch (mode) {
            case ROOT:
                boolean result = renameFolder(oldFile, newFile);
                boolean fileCreated = !oldFile.exists() && newFile.exists();
                if (!result) {
                    if (!fileCreated && isRooted) {
                        try {
                            renameRoot(oldFile, newFile.getName());
                        } catch (RootDeniedException e) {
                            Logger.log(TAG, oldFile.getPath() + "\n" + newFile.getPath());
                        }

                        result = true;
                    }
                }
                fileOperationCallBack.opCompleted(Operations.RENAME, oldFile, newFile, position,
                        result);
                break;
            case EXTERNAL:
                fileOperationCallBack.launchSAF(Operations.RENAME, oldFile, newFile, position);
                break;
            case INTERNAL:
                boolean exists1 = FileUtils.isFileExisting(newFile.getAbsolutePath(), newFile
                        .getName());
                if (exists1) {
                    fileOperationCallBack.exists(Operations.RENAME);
                } else {
                    boolean result1 = renameFolder(oldFile, newFile);
                    boolean fileCreated1 = !oldFile.exists() && newFile.exists();
                    Logger.log(TAG, "Rename--filexists=" + fileCreated1 + "MODE=" + INTERNAL +
                            "result==" + result1);
                    fileOperationCallBack.opCompleted(Operations.RENAME, oldFile, newFile,
                            position, result1);
                }
                break;
        }
    }

    public void deleteFiles(ArrayList<FileInfo> files, boolean isRooted, DeleteTask.DeleteResultCallback deleteResultCallback) {
        if (files == null) {
            return;
        }

        OperationUtils.WriteMode mode = checkWriteAccessMode(new File(files.get(0).getFilePath())
                .getParentFile());

        switch (mode) {
            case EXTERNAL:
                formSAFIntentDelete(files.get(0).getFilePath(), files, Operations.DELETE);
                break;
            case INTERNAL:
            case ROOT:
                DeleteTask deleteTask = new DeleteTask(AceApplication.getAppContext(), isRooted, files);
                deleteTask.setDeleteResultCallback(deleteResultCallback);
                deleteTask.delete();
                break;
        }
    }


    public static void setPermissions(String path, boolean isDir, String permissions, StorageModelImpl.PermissionResultCallback
            permissionResultCallback) {

        String command = "chmod " + permissions + " " + path;
        if (isDir) {
            command = "chmod -R " + permissions + " \"" + path + "\"";
        }
        Command com = new Command(1, command) {
            @Override
            public void commandOutput(int i, String s) {
            }

            @Override
            public void commandTerminated(int i, String s) {
            }

            @Override
            public void commandCompleted(int i, int i2) {
            }
        };
        try {
            RootUtils.mountRW(path);
            RootTools.getShell(true).add(com);
            RootUtils.mountRO(path);
            permissionResultCallback.onPermissionsSet();
        } catch (Exception e1) {
            permissionResultCallback.onError();
            e1.printStackTrace();
        }

    }

    public void extractFile(File currentFile, File file) {
        OperationUtils.WriteMode mode = checkWriteAccessMode(file.getParentFile());
        Context context = AceApplication.getAppContext();
        if (mode == OperationUtils.WriteMode.EXTERNAL) {
            formSAFIntentExtract(file.getAbsolutePath(), Operations.EXTRACT, currentFile
                    .getAbsolutePath());
        } else if (mode == OperationUtils.WriteMode.INTERNAL) {
            Intent intent = new Intent(context, ExtractService.class);
            intent.putExtra(KEY_FILEPATH, currentFile.getPath());
            intent.putExtra(KEY_FILEPATH2, file.getAbsolutePath());
            new OperationProgress().showExtractProgressDialog(context, intent);
        } else {
            Toast.makeText(context, R.string.msg_operation_failed, Toast
                    .LENGTH_SHORT).show();
        }
    }

    public void compressFile(File newFile, ArrayList<FileInfo> files) {
        OperationUtils.WriteMode mode = checkWriteAccessMode(newFile.getParentFile());
        Context context = AceApplication.getAppContext();

        if (mode == OperationUtils.WriteMode.EXTERNAL) {
            formSAFIntentCompress(newFile.getAbsolutePath(), files, Operations.COMPRESS);
        } else if (mode == OperationUtils.WriteMode.INTERNAL) {
            Intent zipIntent = new Intent(context, CreateZipService.class);
            zipIntent.putExtra(KEY_FILEPATH, newFile.getAbsolutePath());
            zipIntent.putParcelableArrayListExtra(KEY_FILES, files);
            new OperationProgress().showZipProgressDialog(context, zipIntent);
        } else {
            Toast.makeText(context, R.string.msg_operation_failed, Toast
                    .LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void formSAFIntentCreation(String path, Operations operations) {
        Intent operationIntent = new Intent(OPERATION_INTENT);
        operationIntent.putExtra(KEY_FILEPATH, path);
        operationIntent.putExtra(KEY_OPERATION, operations);
        safDialogListener.showDialog(path, operationIntent);
    }

    private void formSAFIntentExtract(String path, Operations operations, String newFile) {
        formSAFIntentRename(path, operations, newFile, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void formSAFIntentRename(String path, Operations operations, String newFile, int
            position) {
        Intent operationIntent = new Intent(OPERATION_INTENT);
        operationIntent.putExtra(KEY_FILEPATH, path);
        operationIntent.putExtra(KEY_FILEPATH2, newFile);
        operationIntent.putExtra(KEY_POSITION, position);
        operationIntent.putExtra(KEY_OPERATION, operations);
        safDialogListener.showDialog(path, operationIntent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void formSAFIntentCompress(String path, ArrayList<FileInfo> files, Operations
            operations) {
        Intent operationIntent = new Intent(OPERATION_INTENT);
        operationIntent.putExtra(KEY_FILEPATH, path);
        operationIntent.putExtra(KEY_FILES, files);
        operationIntent.putExtra(KEY_OPERATION, operations);
        safDialogListener.showDialog(path, operationIntent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void formSAFIntentMoveCopy(String destinationDir, ArrayList<FileInfo> files,
                                      ArrayList<CopyData> copyData, Operations operations) {
        Intent operationIntent = new Intent(OPERATION_INTENT);
        operationIntent.putExtra(KEY_FILES, files);
        operationIntent.putExtra(KEY_CONFLICT_DATA, copyData);
        operationIntent.putExtra(KEY_FILEPATH, destinationDir);
        operationIntent.putExtra(KEY_OPERATION, operations);
        safDialogListener.showDialog(destinationDir, operationIntent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void formSAFIntentDelete(String path, ArrayList<FileInfo> files, Operations
            operations) {
        Intent operationIntent = new Intent(OPERATION_INTENT);
        operationIntent.putExtra(KEY_FILES, files);
        operationIntent.putExtra(KEY_OPERATION, operations);
        safDialogListener.showDialog(path, operationIntent);

    }


    public static OperationUtils.WriteMode checkWriteAccessMode(final File folder) {
        if (SdkHelper.isAtleastLollipop() && isOnExtSdCard(folder)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return OperationUtils.WriteMode.ROOT;
            }

            if (FileUtils.isFileNonWritable(folder)) {
                // On Android 5 and above, trigger storage access framework.
                return OperationUtils.WriteMode.EXTERNAL;
            }
            return OperationUtils.WriteMode.INTERNAL;
        } else if (SdkHelper.isKitkat() && isOnExtSdCard(folder)) {
            // Assume that Kitkat workaround works
            return OperationUtils.WriteMode.INTERNAL;
        } else if (FileUtils.isWritable(new File(folder, "DummyFile"))) {
            return OperationUtils.WriteMode.INTERNAL;
        } else {
            return OperationUtils.WriteMode.ROOT;
        }
    }


    public void handleSAFOpResult(Intent intent, boolean isRooted, FileOperationCallBack
            fileOperationCallBack, DeleteTask.DeleteResultCallback deleteResultCallback,
                                  StorageModelImpl storageModel) {
        Operations operation = (Operations) intent.getSerializableExtra(KEY_OPERATION);

        switch (operation) {

            case DELETE:
                ArrayList<FileInfo> files = intent.getParcelableArrayListExtra(KEY_FILES);
                DeleteTask deleteTask = new DeleteTask(AceApplication.getAppContext(), isRooted, files);
                deleteTask.setDeleteResultCallback(deleteResultCallback);
                deleteTask.delete();
                break;

            case COPY:

                ArrayList<FileInfo> copiedFiles = intent.getParcelableArrayListExtra(KEY_FILES);
                ArrayList<CopyData> copyData = intent.getParcelableArrayListExtra
                        (KEY_CONFLICT_DATA);
                String destinationPath = intent.getStringExtra(KEY_FILEPATH);
                storageModel.startPasteOperation(destinationPath, copiedFiles, copyData, false);
                break;

            case CUT:
                ArrayList<FileInfo> movedFiles = intent.getParcelableArrayListExtra(KEY_FILES);
                ArrayList<CopyData> moveData = intent.getParcelableArrayListExtra
                        (KEY_CONFLICT_DATA);
                String destinationMovePath = intent.getStringExtra(KEY_FILEPATH);
                storageModel.startPasteOperation(destinationMovePath, movedFiles, moveData, true);
                break;

            case FOLDER_CREATION:
                String path = intent.getStringExtra(KEY_FILEPATH);
                mkDir(new File(path), isRooted, fileOperationCallBack);
                break;

            case FILE_CREATION:
                String newFilePathCreate = intent.getStringExtra(KEY_FILEPATH);
                mkFile(new File(newFilePathCreate), isRooted, fileOperationCallBack);
                break;

            case RENAME:
                String oldFilePath = intent.getStringExtra(KEY_FILEPATH);
                String newFilePath = intent.getStringExtra(KEY_FILEPATH2);
                int position = intent.getIntExtra(KEY_POSITION, INVALID_POS);
                renameFile(new File(oldFilePath), new File(newFilePath),
                        position, isRooted, fileOperationCallBack);
                break;

            case EXTRACT:
                String oldFilePath1 = intent.getStringExtra(KEY_FILEPATH);
                String newFilePath1 = intent.getStringExtra(KEY_FILEPATH2);
                extractFile(new File(oldFilePath1), new File(newFilePath1));
                break;

            case COMPRESS:
                ArrayList<FileInfo> compressedFiles = intent.getParcelableArrayListExtra(KEY_FILES);
                String destinationCompressPath = intent.getStringExtra(KEY_FILEPATH);
                compressFile(new File(destinationCompressPath), compressedFiles);
                break;
        }
    }

    private SAFDialog safDialogListener;


    public void setSAFListener(SAFDialog safListener) {
        safDialogListener = safListener;
    }


    public interface SAFDialog {
        void showDialog(String path, Intent data);
    }


}
