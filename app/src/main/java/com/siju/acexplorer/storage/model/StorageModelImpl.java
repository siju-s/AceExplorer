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

package com.siju.acexplorer.storage.model;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.SharedPreferenceWrapper;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.LargeBundleTransfer;
import com.siju.acexplorer.model.helper.RootHelper;
import com.siju.acexplorer.model.helper.SdkHelper;
import com.siju.acexplorer.model.root.RootUtils;
import com.siju.acexplorer.storage.model.operations.FileOpsHelper;
import com.siju.acexplorer.storage.model.operations.OperationUtils;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.model.task.CopyService;
import com.siju.acexplorer.storage.model.task.CreateZipService;
import com.siju.acexplorer.storage.model.task.DeleteTask;
import com.siju.acexplorer.storage.model.task.ExtractService;
import com.siju.acexplorer.storage.model.task.MoveService;
import com.siju.acexplorer.storage.model.task.PasteConflictChecker;
import com.siju.acexplorer.trash.TrashHelper;
import com.siju.acexplorer.trash.TrashModel;
import com.siju.acexplorer.view.dialog.DialogHelper;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.helper.LargeBundleTransfer.LARGE_BUNDLE_LIMIT;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.model.helper.PermissionsHelper.parse;
import static com.siju.acexplorer.storage.model.operations.FileOpsHelper.checkWriteAccessMode;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_COUNT;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_MEDIA_INDEX_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OLD_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_SHOW_RESULT;
import static com.siju.acexplorer.storage.model.operations.Operations.COMPRESS;
import static com.siju.acexplorer.storage.model.operations.Operations.DELETE;
import static com.siju.acexplorer.storage.model.operations.Operations.EXTRACT;
import static com.siju.acexplorer.storage.model.operations.Operations.FILE_CREATION;
import static com.siju.acexplorer.storage.model.operations.Operations.FOLDER_CREATION;
import static com.siju.acexplorer.storage.model.operations.Operations.HIDE;
import static com.siju.acexplorer.storage.model.operations.Operations.RENAME;

/**
 * Created by Siju on 02 September,2017
 */
@SuppressWarnings("FieldCanBeLocal")
public class StorageModelImpl implements StoragesModel {

    @SuppressWarnings("unused")
    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private Listener listener;
    private FileOpsHelper fileOpsHelper;
    private static final String EXT_TXT = ".txt";


    public StorageModelImpl() {
        this.context = AceApplication.getAppContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        fileOpsHelper = new FileOpsHelper();
        fileOpsHelper.setSAFListener(safDialogListener);

    }


    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public BillingStatus getBillingStatus() {
        return BillingManager.getInstance().getInAppBillingStatus();
    }


    @Override
    public Bundle getUserPrefs() {
        Bundle bundle = new Bundle();
        int gridCols = sharedPreferences.getInt(FileConstants.KEY_GRID_COLUMNS, 0);
        boolean isHomeScreenEnabled = sharedPreferences.getBoolean(FileConstants
                .PREFS_HOMESCREEN, true);
        int viewMode = sharedPreferenceWrapper.getViewMode(context);
        boolean showHidden = sharedPreferences.getBoolean(FileConstants.PREFS_HIDDEN, false);
        boolean isTrashEnabled = sharedPreferences.getBoolean(FileConstants.PREFS_TRASH, true);

        bundle.putInt(FileConstants.KEY_GRID_COLUMNS, gridCols);
        bundle.putBoolean(FileConstants.PREFS_HOMESCREEN, isHomeScreenEnabled);
        bundle.putInt(FileConstants.PREFS_VIEW_MODE, viewMode);
        bundle.putBoolean(FileConstants.PREFS_HIDDEN, showHidden);
        bundle.putBoolean(FileConstants.PREFS_TRASH, isTrashEnabled);

        return bundle;
    }

    @Override
    public void startPasteOperation(final String currentDir, final boolean isMove, final boolean
            rooted,
                                    final ArrayList<FileInfo> info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PasteConflictChecker conflictChecker = new PasteConflictChecker(currentDir,
                        rooted, isMove, info);
                conflictChecker.setListener(pasteResultCallback);
                conflictChecker.execute();
            }
        }).start();
    }

    @Override
    public void moveToTrash(final ArrayList<FileInfo> filesToDelete, final String trashDir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPasteOperation(trashDir, filesToDelete, copyData, false);
            }
        }).start();
    }

    @Override
    public void restoreFiles(List<TrashModel> trashModelList) {
        Intent intent = new Intent(context, CopyService.class);

        intent.putParcelableArrayListExtra(OperationUtils.KEY_TRASH_DATA,
                                           (ArrayList<? extends Parcelable>) trashModelList);
//        if (files.size() > LARGE_BUNDLE_LIMIT) {
//            LargeBundleTransfer.storeFileData(context, files);
//        } else {
//            intent.putParcelableArrayListExtra(OperationUtils.KEY_FILES, (ArrayList<? extends
//                    Parcelable>) files);
//        }
        intent.putExtra(OperationUtils.KEY_IS_RESTORE, true);
        if (SdkHelper.isOreo()) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void handleSAFResult(Intent operationIntent, Uri treeUri, boolean rooted, int flags) {
        if (treeUri != null) {
            sharedPreferences.edit().putString(FileConstants.SAF_URI, treeUri.toString())
                    .apply();
            flags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent
                    .FLAG_GRANT_WRITE_URI_PERMISSION);

            // Persist URI - this is required for verification of writability.
            context.getContentResolver().takePersistableUriPermission(treeUri,
                    flags);
            fileOpsHelper.handleSAFOpResult(operationIntent, rooted, fileOperationCallBack,
                    deleteResultCallback, this);
        }

    }

    @Override
    public void saveOldSAFUri(String path) {
        sharedPreferences.edit().putString(FileConstants.SAF_URI, path).apply();
    }

    @Override
    public void createDir(String currentDir, String name, final boolean rooted) {

        if (FileUtils.isFileNameInvalid(name)) {
            listener.onInvalidName(Operations.FOLDER_CREATION);
            return;
        }

        final File file = new File(currentDir + File.separator + name);
        if (file.exists()) {
            listener.onFileExists(FOLDER_CREATION, context.getString(R.string.msg_file_exists));
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                listener.dismissDialog(FOLDER_CREATION);
                FileOpsHelper.mkDir(file, rooted, fileOperationCallBack);
            }
        }).start();
    }

    @Override
    public void createFile(String currentDir, String name, final boolean rooted) {

        if (FileUtils.isFileNameInvalid(name)) {
            listener.onInvalidName(Operations.FILE_CREATION);
            return;
        }

        final File file = new File(currentDir + File.separator + name + EXT_TXT);

        if (file.exists()) {
            listener.onFileExists(FILE_CREATION, context.getString(R.string.msg_file_exists));
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                listener.dismissDialog(FOLDER_CREATION);
                FileOpsHelper.mkFile(file, rooted, fileOperationCallBack);
            }
        }).start();
    }

    @Override
    public void deleteFiles(ArrayList<FileInfo> filesToDelete) {
        fileOpsHelper.deleteFiles(filesToDelete, RootUtils.isRooted(context), deleteResultCallback);
    }

    @Override
    public void onExtractPositiveClick(String currentFilePath, String newFileName, boolean isChecked,
                                       String selectedPath) {

        if (FileUtils.isFileNameInvalid(newFileName)) {
            listener.onInvalidName(Operations.EXTRACT);
            return;
        }
        File newFile, currentFile;
        if (isChecked) {
            newFile = new File(selectedPath + File.separator + newFileName);
        } else {
            selectedPath = new File(currentFilePath).getParent();
            newFile = new File(selectedPath + File.separator + newFileName);
        }

        if (FileUtils.isFileExisting(selectedPath, newFile.getName())) {
            listener.onFileExists(Operations.EXTRACT, context.getString(R.string
                    .msg_file_exists));
            return;
        }
        listener.dismissDialog(Operations.EXTRACT);
        currentFile = new File(currentFilePath);
        extractFile(currentFile, newFile);
    }


    public void extractFile(File currentFile, File file) {
        OperationUtils.WriteMode mode = checkWriteAccessMode(file.getParentFile());
        Context context = AceApplication.getAppContext();
        if (mode == OperationUtils.WriteMode.EXTERNAL) {
            fileOpsHelper.formSAFIntentExtract(file.getAbsolutePath(), Operations.EXTRACT, currentFile
                    .getAbsolutePath());
        } else if (mode == OperationUtils.WriteMode.INTERNAL) {
            Intent intent = new Intent(context, ExtractService.class);
            intent.putExtra(KEY_FILEPATH, currentFile.getPath());
            intent.putExtra(KEY_FILEPATH2, file.getAbsolutePath());
            listener.showExtractDialog(intent);
            if (SdkHelper.isOreo()) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } else {
            listener.onOperationFailed(EXTRACT);
        }
    }

    @Override
    public void onCompressPosClick(String newFilePath, ArrayList<FileInfo> paths) {
        File newFile = new File(newFilePath);
        String newFileName = newFile.getName();
        if (FileUtils.isFileNameInvalid(newFileName)) {
            listener.onInvalidName(Operations.EXTRACT);
            return;
        }

        String currentDir = newFile.getParent();

        if (FileUtils.isFileExisting(currentDir, newFile.getName())) {
            listener.onFileExists(Operations.EXTRACT, context.getString(R.string
                    .msg_file_exists));
            return;
        }
        listener.dismissDialog(Operations.COMPRESS);
        compressFile(newFile, paths);

    }

    public void compressFile(File newFile, ArrayList<FileInfo> files) {
        OperationUtils.WriteMode mode = checkWriteAccessMode(newFile.getParentFile());
        Context context = AceApplication.getAppContext();

        if (mode == OperationUtils.WriteMode.EXTERNAL) {
            fileOpsHelper.formSAFIntentCompress(newFile.getAbsolutePath(), files, Operations.COMPRESS);
        } else if (mode == OperationUtils.WriteMode.INTERNAL) {
            Intent zipIntent = new Intent(context, CreateZipService.class);
            zipIntent.putExtra(KEY_FILEPATH, newFile.getAbsolutePath());
            if (files.size() > LARGE_BUNDLE_LIMIT) {
                LargeBundleTransfer.storeFileData(context, files);
            } else {
                zipIntent.putParcelableArrayListExtra(KEY_FILES, files);
            }
            listener.showZipProgressDialog(zipIntent);
            if (SdkHelper.isOreo()) {
                context.startForegroundService(zipIntent);
            } else {
                context.startService(zipIntent);
            }
        } else {
            listener.onOperationFailed(COMPRESS);
        }
    }

    private FileInfo fileInfo;

    @Override
    public void hideUnHideFiles(ArrayList<FileInfo> fileInfo, ArrayList<Integer> pos) {
        for (int i = 0; i < fileInfo.size(); i++) {
            this.fileInfo = fileInfo.get(i);
            String fileName = fileInfo.get(i).getFileName();
            String renamedName;
            if (fileName.startsWith(".")) {
                renamedName = fileName.substring(1);
            } else {
                renamedName = "." + fileName;
            }
            String path = fileInfo.get(i).getFilePath();
            File oldFile = new File(path);
            String temp = path.substring(0, path.lastIndexOf(File.separator));

            File newFile = new File(temp + File.separator + renamedName);
            FileOpsHelper.renameFile(Operations.HIDE, oldFile, newFile, RootUtils.isRooted(context),
                    fileOperationCallBack);
        }
    }


    private FileOpsHelper.SAFDialog safDialogListener = new FileOpsHelper.SAFDialog() {
        @Override
        public void showDialog(String path, Intent data) {
            listener.showSAFDialog(path, data);
        }
    };

    private FileOpsHelper.FileOperationCallBack fileOperationCallBack = new FileOpsHelper
            .FileOperationCallBack() {

        @Override
        public void exists(Operations operation) {
            listener.onFileExists(operation, context.getString(R.string.msg_file_exists));
        }

        @Override
        public void launchSAF(Operations operation, File file) {
            fileOpsHelper.formSAFIntentCreation(file.getAbsolutePath(), operation);
        }

        @Override
        public void launchSAF(Operations operation, File oldFile, File newFile) {
            fileOpsHelper.formSAFIntentRename(oldFile.getAbsolutePath(), operation, newFile
                    .getAbsolutePath());
        }

        @Override
        public void opCompleted(Operations operation, File file, boolean success) {
            switch (operation) {
                case FILE_CREATION:
                case FOLDER_CREATION:
                    if (success) {
                        Intent intent = new Intent(ACTION_OP_REFRESH);
                        intent.putExtra(OperationUtils.KEY_OPERATION, operation);
                        context.sendBroadcast(intent);
                        scanFile(context, file.getAbsolutePath());
                    } else {
                        listener.onOperationFailed(operation);
                    }
                    break;
            }

        }

        @Override
        public void opCompleted(Operations operation, File oldFile, File newFile,
                                boolean success) {
            switch (operation) {
                case RENAME:
                case HIDE:
                    if (success) {
                        sharedPreferenceWrapper.updateFavoritePath(context, oldFile.getAbsolutePath(),
                                newFile.getAbsolutePath());
                        Intent intent = new Intent(ACTION_OP_REFRESH);
                        intent.putExtra(KEY_OPERATION, operation);
                        if (operation.equals(HIDE)) {
                            intent.putExtra(KEY_OLD_FILES, fileInfo);
                        }
                        intent.putExtra(KEY_FILEPATH, oldFile.getAbsolutePath());
                        intent.putExtra(KEY_FILEPATH2, newFile.getAbsolutePath());
                        context.sendBroadcast(intent);
                    } else {
                        listener.onOperationFailed(operation);
                    }
                    break;
            }
        }
    };


    public void startPasteOperation(String destinationDir, List<FileInfo> files, List<CopyData> copyData,
                                    boolean isMove) {

        if (isMove) {
            moveFiles(destinationDir, files, copyData);
        } else {
            copyFiles(destinationDir, files, copyData);
        }
    }

    private void checkPasteWriteMode(String destinationDir, List<FileInfo> files, List<CopyData> copyData, boolean isMove) {
        OperationUtils.WriteMode mode = checkWriteAccessMode(new File
                (destinationDir));
        switch (mode) {
            case EXTERNAL:
                Operations operation = isMove ? Operations.CUT : Operations.COPY;
                fileOpsHelper.formSAFIntentMoveCopy(destinationDir, (ArrayList<FileInfo>) files,
                        (ArrayList<CopyData>) copyData, operation);
                break;
            case ROOT:
            case INTERNAL:
                startPasteOperation(destinationDir, files, copyData, isMove);
                break;
        }
    }

    private List<FileInfo> files;
    private List<CopyData> copyData = new ArrayList<>();

    private DialogHelper.PasteConflictListener pasteConflictListener = new DialogHelper
            .PasteConflictListener() {

        @Override
        public void onPositiveButtonClick(Dialog dialog, Operations operation, List
                <FileInfo> destFiles, List<FileInfo>
                                                  conflictFiles, String destinationDir, boolean isMove, boolean isChecked) {

            boolean isEnd = false;
            if (isChecked) {
                files.removeAll(conflictFiles);
                isEnd = true;
            } else {
                files.remove(conflictFiles.get(0));
                conflictFiles.remove(0);
                if (conflictFiles.size() == 0) {
                    isEnd = true;
                }
            }

            if (isEnd) {
                dialog.dismiss();
            } else {
                listener.showConflictDialog(conflictFiles, destFiles, destinationDir, isMove,
                        this);
            }

        }

        @Override
        public void onNegativeButtonClick(Dialog dialog, Operations operation, List<FileInfo>
                destFiles, List<FileInfo>
                                                  conflictFiles, String destinationDir, boolean isMove, boolean isChecked) {
            boolean isEnd = false;
            if (isChecked) {
                isEnd = true;
            } else {
                conflictFiles.remove(0);
                if (conflictFiles.size() == 0) {
                    isEnd = true;
                }
            }

            if (isEnd) {
                dialog.dismiss();
                checkPasteWriteMode(destinationDir, files, copyData, isMove);
            } else {
                listener.showConflictDialog(conflictFiles, destFiles, destinationDir, isMove,
                        this);
            }
        }

        @Override
        public void onNeutralButtonClick(Dialog dialog, Operations operation, List<FileInfo>
                destFiles, List<FileInfo> conflictFiles, String destinationDir, boolean isMove,
                                         boolean isChecked) {
            boolean isEnd = false;
            if (isChecked) {
                for (FileInfo fileInfo : conflictFiles) {
                    copyData.add(new CopyData(fileInfo.getFilePath()));
                }
                isEnd = true;
            } else {
                copyData.add(new CopyData(conflictFiles.get(0).getFilePath()));
                conflictFiles.remove(0);
                if (conflictFiles.size() == 0) {
                    isEnd = true;
                }
            }

            if (isEnd) {
                dialog.dismiss();
                checkPasteWriteMode(destinationDir, files, copyData, isMove);
            } else {
                listener.showConflictDialog(conflictFiles, destFiles, destinationDir, isMove,
                        this);
            }
        }
    };

    private void copyFiles(String destinationDir, List<FileInfo> files, List<CopyData> copyData) {
        File destFile = new File(destinationDir);
        OperationUtils.WriteMode mode = checkWriteAccessMode(destFile);
        if (!FileUtils.isFileNonWritable(destFile) || mode.equals(OperationUtils.WriteMode.ROOT)) {
            if (mode.equals(OperationUtils.WriteMode.ROOT) && !RootTools.isAccessGiven()) {
                listener.onOperationFailed(Operations.COPY);
                return;
            }
            listener.showPasteProgressDialog(destinationDir, files, copyData, false);
            Intent intent = new Intent(context, CopyService.class);

            intent.putParcelableArrayListExtra(OperationUtils.KEY_CONFLICT_DATA,
                    (ArrayList<? extends Parcelable>) copyData);
            if (files.size() > LARGE_BUNDLE_LIMIT) {
                LargeBundleTransfer.storeFileData(context, files);
            } else {
                intent.putParcelableArrayListExtra(OperationUtils.KEY_FILES, (ArrayList<? extends
                        Parcelable>) files);
            }
            if (destinationDir.equals(TrashHelper.getTrashDir(context))) {
                intent.putExtra(OperationUtils.KEY_IS_TRASH, true);
            }
            intent.putExtra(OperationUtils.KEY_FILEPATH, destinationDir);
            if (SdkHelper.isOreo()) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } else {
            listener.onOperationFailed(Operations.COPY);
        }
    }

    private void moveFiles(String destinationDir, final List<FileInfo> files, final List<CopyData> copyData) {
        listener.showPasteProgressDialog(destinationDir, files, copyData, true);
        Intent intent = new Intent(context, MoveService.class);
        if (files.size() > LARGE_BUNDLE_LIMIT) {
            LargeBundleTransfer.storeFileData(context, files);
        } else {
            intent.putParcelableArrayListExtra(OperationUtils.KEY_FILES, (ArrayList<? extends
                    Parcelable>) files);
        }
        intent.putParcelableArrayListExtra(OperationUtils.KEY_CONFLICT_DATA,
                (ArrayList<? extends Parcelable>) copyData);
        intent.putExtra(OperationUtils.KEY_FILEPATH, destinationDir);
        if (SdkHelper.isOreo()) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void persistSortMode(int sortMode) {
        sharedPreferences.edit().putInt(FileConstants.KEY_SORT_MODE, sortMode).apply();
    }

    @Override
    public void persistTrashState(boolean value) {
        sharedPreferences.edit().putBoolean(FileConstants.PREFS_TRASH, value).apply();
    }



    public int getSortMode() {
        return sharedPreferences.getInt(FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }

    @Override
    public void getFilePermissions(final String filePath, final boolean isDir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String perm = RootHelper.getPermissions(filePath, isDir);
                ArrayList<Boolean[]> permissionList = parse(perm);
                listener.onPermissionsFetched(permissionList);
            }
        }).start();
    }

    @Override
    public void setPermissions(final String path, final boolean isDir, final String permissions) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOpsHelper.setPermissions(path, isDir, permissions, permissionResultCallback);
            }
        }).start();
    }

    @Override
    public void saveSettingsOnExit(int gridCols, int viewMode) {

        sharedPreferences.edit().putInt(FileConstants.KEY_GRID_COLUMNS, gridCols).apply();
        sharedPreferenceWrapper.savePrefs(context, viewMode);
    }

    @Override
    public void updateFavorites(ArrayList<FavInfo> favInfoArrayList) {
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        int count = sharedPreferenceWrapper.addFavorites(context, favInfoArrayList);
        if (count == 0) {
            listener.onFavExists();
        } else {
            listener.onFavAdded(count);
        }
    }

    @Override
    public void renameFile(final String filePath, final String parentDir, String name, final boolean rooted) {
        if (FileUtils.isFileNameInvalid(name)) {
            listener.onInvalidName(Operations.RENAME);
            return;
        }

        String extension = null;
        boolean isFile = false;
        if (new File(filePath).isFile()) {
            extension = filePath.substring(filePath.lastIndexOf("."), filePath.length());
            isFile = true;
        }
        String newFilePath;
        if (isFile) {
            newFilePath = parentDir + File.separator + name + extension;
        } else {
            newFilePath = parentDir + File.separator + name;
        }
        final File newFile = new File(newFilePath);

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOpsHelper.renameFile(RENAME, new File(filePath), newFile, rooted, fileOperationCallBack);
            }
        }).start();
    }

    private DeleteTask.DeleteResultCallback deleteResultCallback = new DeleteTask
            .DeleteResultCallback() {


        @Override
        public void onFileDeleted(int totalFiles, List<FileInfo> deletedFiles, List<String> filestoMediaIndex, boolean showToast) {
            Intent intent = new Intent(ACTION_OP_REFRESH);
            Bundle bundle = new Bundle();
            bundle.putSerializable(KEY_OPERATION, DELETE);
            if (deletedFiles.size() > LARGE_BUNDLE_LIMIT) {
                LargeBundleTransfer.storeFileData(context, deletedFiles);
                LargeBundleTransfer.storeStringData(context, filestoMediaIndex);
            } else {
                bundle.putParcelableArrayList(KEY_FILES, (ArrayList<? extends Parcelable>) deletedFiles);
                bundle.putStringArrayList(KEY_MEDIA_INDEX_FILES, (ArrayList<String>) filestoMediaIndex);
            }
            intent.putExtra(KEY_COUNT, totalFiles);
            intent.putExtra(KEY_SHOW_RESULT, showToast);
            intent.putExtras(bundle);
            context.sendBroadcast(intent);
        }
    };

    private PasteConflictChecker.PasteResultCallback pasteResultCallback = new
            PasteConflictChecker.PasteResultCallback() {


                @Override
                public void showConflictDialog(ArrayList<FileInfo> files, final List<FileInfo>
                        conflictFiles, List<FileInfo> destFiles, final String destinationDir, final boolean isMove) {
                    StorageModelImpl.this.files = files;
                    listener.showConflictDialog(conflictFiles, destFiles, destinationDir, isMove,
                            pasteConflictListener);
                }

                @Override
                public void onLowSpace() {
                    listener.onLowSpace();
                }

                @Override
                public void checkWriteMode(String destinationDir, List<FileInfo> files, boolean isMove) {
                    StorageModelImpl.this.checkPasteWriteMode(destinationDir, files, copyData, isMove);
                }
            };

    private PermissionResultCallback permissionResultCallback = new PermissionResultCallback() {
        @Override
        public void onError() {
            listener.onPermissionSetError();
        }

        @Override
        public void onPermissionsSet() {
            listener.onPermissionsSet();
        }
    };

    public interface PermissionResultCallback {

        void onError();

        void onPermissionsSet();
    }

}
