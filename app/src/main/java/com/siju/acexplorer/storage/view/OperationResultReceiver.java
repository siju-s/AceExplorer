package com.siju.acexplorer.storage.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.model.helper.LargeBundleTransfer;
import com.siju.acexplorer.storage.model.operations.Operations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.siju.acexplorer.main.model.helper.MediaStoreHelper.removeBatchMedia;
import static com.siju.acexplorer.main.model.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_RELOAD_LIST;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_COUNT;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES_COUNT;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_MEDIA_INDEX_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OLD_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_SHOW_RESULT;

public class OperationResultReceiver extends BroadcastReceiver {


    private static final String      TAG           = "OperationResultReceiver";
    private              FilesView   filesView;
    private              Context     context;
    private              Set<String> mediaScanningPaths = new HashSet<>();
    private              boolean     mediaScannerActive;

    public OperationResultReceiver(Context context, FilesView filesView) {
        this.context = context;
        this.filesView = filesView;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_RELOAD_LIST.equals(action)) {
            String path = intent.getStringExtra(KEY_FILEPATH);
            onReloadList(path);
        } else if (ACTION_OP_REFRESH.equals(action)) {
            Bundle bundle = intent.getExtras();
            Operations operation = null;
            if (bundle != null) {
                operation = (Operations) bundle.getSerializable(KEY_OPERATION);
            }
            if (operation != null) {
                onOperationResult(intent, operation);
            }
        }
    }

    private void onReloadList(String path) {
        filesView.calculateScroll();
        if (path != null) {
            scanFile(AceApplication.getAppContext(), path);
        }
        filesView.refreshList();
    }

    private void onOperationResult(Intent intent, Operations operation) {
        Logger.log(TAG, "onOperationResult: " + operation);
        int count = intent.getIntExtra(KEY_FILES_COUNT, 0);

        switch (operation) {
            case DELETE:
                onDeleted(intent);
                break;
            case RENAME:
            case HIDE:
                onRename(intent, operation);
                break;
            case CUT:
                onCut(intent, count);
                break;
            case COPY:
                onCopy(count);
                break;
            case FOLDER_CREATION:
            case FILE_CREATION:
                filesView.dismissFAB();
                boolean isSuccess = intent.getBooleanExtra(KEY_RESULT, true);

                if (isSuccess) {
                    filesView.onFileCreated();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.msg_operation_failed), Toast
                            .LENGTH_LONG).show();

                }
                break;

        }
    }

    private void onCopy(int count) {
        if (count > 0 && getContext() != null) {
            Toast.makeText(getContext(), String.format(Locale.getDefault(), getContext().
                                                                                                getString(
                                                                                                        R.string.copied),
                                                       count), Toast.LENGTH_SHORT).show();
            filesView.refreshList();
        }
    }

    private void onCut(Intent intent, int count) {
        if (count > 0 && getContext() != null) {
            Toast.makeText(getContext(), String.format(Locale.getDefault(), getContext().
                                                                                                getString(
                                                                                                        R.string.moved),
                                                       count), Toast.LENGTH_SHORT).show();
            filesView.refreshList();
        }
        final ArrayList<String> oldFileList = intent.getStringArrayListExtra(KEY_OLD_FILES);
        deleteFromMediaStore(oldFileList);
    }

    private void onRename(Intent intent, Operations operation) {
        filesView.onRename(intent, operation);
    }

    private void onDeleted(Intent intent) {
        boolean isLargeBundle = false;
        List<FileInfo> deletedFilesList;
        List<String> filesToMediaIndex;

        deletedFilesList = intent.getParcelableArrayListExtra
                (KEY_FILES);
        filesToMediaIndex = intent.getStringArrayListExtra(KEY_MEDIA_INDEX_FILES);
        if (deletedFilesList == null) {
            deletedFilesList = LargeBundleTransfer.getFileData(AceApplication.getAppContext());
            isLargeBundle = true;
        }
        if (filesToMediaIndex == null) {
            filesToMediaIndex = LargeBundleTransfer.getStringData(AceApplication.getAppContext());
        }


//                for (FileInfo info : deletedFilesList) {
//                    scanMultipleFiles(getActivity().getApplicationContext(), info.getFilePath());
//                }
        int totalFiles = intent.getIntExtra(KEY_COUNT, 0);

        int deletedCount = deletedFilesList.size();
        showDeletedMessage(intent, totalFiles, deletedCount);
        filesView.onFilesDeleted(deletedFilesList);
        deleteFromMediaStore(filesToMediaIndex);
        if (isLargeBundle) {
            LargeBundleTransfer.removeFileData(AceApplication.getAppContext());
            LargeBundleTransfer.removeStringData(AceApplication.getAppContext());
        }
    }

    private void showDeletedMessage(Intent intent, int totalFiles, int deletedCount) {
        if (intent.getBooleanExtra(KEY_SHOW_RESULT, false)) {
            if (deletedCount != 0) {
                FileUtils.showMessage(getContext(), getContext().getResources().getQuantityString(R.
                                                                                                          plurals.number_of_files,
                                                                                                  deletedCount,
                                                                                                  deletedCount) + " " +
                                                    getContext().getResources().getString(R.string.msg_delete_success));
            }
            if (totalFiles != deletedCount) {
                FileUtils.showMessage(getContext(), getContext().getResources().getString(R.string.msg_delete_failure));
            }
        }
    }

    private void deleteFromMediaStore(final List<String> filesToMediaIndex) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                if (filesToMediaIndex.size() == 0) {
                    return;
                }
                Logger.log(TAG, "deleteFromMediaStore: " + filesToMediaIndex.size());
                String mediaScanningPath = new File(filesToMediaIndex.get(0)).getParent();
                addToMediaScanning(mediaScanningPath);
                Logger.log(TAG, "run CUT: mediaScanningPath" + mediaScanningPath + "size:" + mediaScanningPaths.size());
                removeBatchMedia(AceApplication.getAppContext(), filesToMediaIndex, null);
                removeFromMediaScanning(mediaScanningPath);
            }
        }).start();
    }

    private synchronized void addToMediaScanning(String path) {
        Log.d(TAG, "addToMediaScanning: path:" + path);
        mediaScannerActive = true;
        mediaScanningPaths.add(path);
    }

    private synchronized void removeFromMediaScanning(String path) {
        Log.d(TAG, "removeFromMediaScanning: path:" + path);
        mediaScanningPaths.remove(path);
        if (mediaScanningPaths.size() == 0) {
            mediaScannerActive = false;
        }
    }

    public Context getContext() {
        return context;
    }

    public boolean isMediaScannerActive() {
        return mediaScannerActive;
    }

    boolean isMediaScanningPath(String path) {
        for (String scannerPath : mediaScanningPaths) {
            if (path.equals(scannerPath)) {
                return true;
            }
        }
        return false;
    }
}
