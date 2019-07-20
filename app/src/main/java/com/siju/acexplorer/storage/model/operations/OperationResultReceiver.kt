package com.siju.acexplorer.storage.model.operations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.storage.model.operations.OperationUtils.*
import java.util.*

private const val TAG = "OperationResultReceiver"

class OperationResultReceiver(private val operationHelper: OperationHelper) : BroadcastReceiver() {
    private val mediaScanningPaths = HashSet<String>()
    //
    //    private void onDeleted(Intent intent) {
    //        boolean isLargeBundle = false;
    //        List<FileInfo> deletedFilesList;
    //        List<String> filesToMediaIndex;
    //
    //        deletedFilesList = intent.getParcelableArrayListExtra
    //                (KEY_FILES);
    //        filesToMediaIndex = intent.getStringArrayListExtra(KEY_MEDIA_INDEX_FILES);
    //        if (deletedFilesList == null) {
    //            deletedFilesList = LargeBundleTransfer.getFileData(AceApplication.getAppContext());
    //            isLargeBundle = true;
    //        }
    //        if (filesToMediaIndex == null) {
    //            filesToMediaIndex = LargeBundleTransfer.getStringData(AceApplication.getAppContext());
    //        }
    //
    //
    ////                for (FileInfo info : deletedFilesList) {
    ////                    scanMultipleFiles(getActivity().getApplicationContext(), info.getFilePath());
    ////                }
    //        int totalFiles = intent.getIntExtra(KEY_COUNT, 0);
    //
    //        int deletedCount = deletedFilesList.size();
    //        showDeletedMessage(intent, totalFiles, deletedCount);
    //        operationHelper.onFilesDeleted(deletedFilesList);
    //        deleteFromMediaStore(filesToMediaIndex);
    //        if (isLargeBundle) {
    //            LargeBundleTransfer.removeFileData(AceApplication.getAppContext());
    //            LargeBundleTransfer.removeStringData(AceApplication.getAppContext());
    //        }
    //    }
    //
    //    private void showDeletedMessage(Intent intent, int totalFiles, int deletedCount) {
    //        if (intent.getBooleanExtra(KEY_SHOW_RESULT, false)) {
    //            if (deletedCount != 0) {
    //                FileUtils.showMessage(getContext(), getContext().getResources().getQuantityString(R.
    //                                                                                                          plurals.number_of_files,
    //                                                                                                  deletedCount,
    //                                                                                                  deletedCount) + " " +
    //                                                    getContext().getResources().getString(R.string.msg_delete_success));
    //            }
    //            if (totalFiles != deletedCount) {
    //                FileUtils.showMessage(getContext(), getContext().getResources().getString(R.string.msg_delete_failure));
    //            }
    //        }
    //    }
    //
    //    private void deleteFromMediaStore(final List<String> filesToMediaIndex) {
    //        new Thread(new Runnable() {
    //            @Override
    //            public void run() {
    //                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    //                if (filesToMediaIndex.size() == 0) {
    //                    return;
    //                }
    //                Logger.log(TAG, "deleteFromMediaStore: " + filesToMediaIndex.size());
    //                String mediaScanningPath = new File(filesToMediaIndex.get(0)).getParent();
    //                addToMediaScanning(mediaScanningPath);
    //                Logger.log(TAG, "run CUT: mediaScanningPath" + mediaScanningPath + "size:" + mediaScanningPaths.size());
    //                removeBatchMedia(AceApplication.getAppContext(), filesToMediaIndex, null);
    //                removeFromMediaScanning(mediaScanningPath);
    //            }
    //        }).start();
    //    }
    //
    //    private synchronized void addToMediaScanning(String path) {
    //        Log.d(TAG, "addToMediaScanning: path:" + path);
    //        mediaScannerActive = true;
    //        mediaScanningPaths.add(path);
    //    }
    //
    //    private synchronized void removeFromMediaScanning(String path) {
    //        Log.d(TAG, "removeFromMediaScanning: path:" + path);
    //        mediaScanningPaths.remove(path);
    //        if (mediaScanningPaths.size() == 0) {
    //            mediaScannerActive = false;
    //        }
    //    }


    val isMediaScannerActive: Boolean = false


    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_RELOAD_LIST -> {
                val path = intent.getStringExtra(KEY_FILEPATH)
                onReloadList(path)
            }
            ACTION_OP_REFRESH  -> {
                val bundle = intent.extras
                val operation = bundle?.getSerializable(KEY_OPERATION) as Operations?
                operation?.let { onOperationResult(intent, it) }
            }
        }
    }

    private fun onReloadList(path: String) {
        //        operationHelper.calculateScroll();
        //        if (path != null) {
        //            scanFile(AceApplication.getAppContext(), path);
        //        }
        //        operationHelper.reloadList();
    }

    private fun onOperationResult(intent: Intent, operation: Operations) {
        Logger.log(TAG, "onOperationResult: $operation")
        val count = intent.getIntExtra(KEY_FILES_COUNT, 0)

        when (operation) {
//            Operations.DELETE                  -> {
//            }
//            Operations.RENAME, Operations.HIDE -> onRename(intent, operation)
            Operations.CUT                     -> onCut(intent, count)
            Operations.COPY                    -> onCopyOperationCompleted(count)
        }//                onDeleted(intent);
    }

    private fun onCopyOperationCompleted(count: Int) {
        operationHelper.onCopyCompleted(Operations.COPY, count)
    }

    private fun onCut(intent: Intent, count: Int) {
        //        if (count > 0 && getContext() != null) {
        //            Toast.makeText(getContext(), String.format(Locale.getDefault(), getContext().
        //                                                                                                getString(
        //                                                                                                        R.string.moved),
        //                                                       count), Toast.LENGTH_SHORT).show();
        //            operationHelper.reloadList();
        //        }
        //        final ArrayList<String> oldFileList = intent.getStringArrayListExtra(KEY_OLD_FILES);
        //        deleteFromMediaStore(oldFileList);
    }

    private fun onRename(intent: Intent, operation: Operations) {
        //        operationHelper.onRename(intent, operation);
    }

    internal fun isMediaScanningPath(path: String): Boolean {
        for (scannerPath in mediaScanningPaths) {
            if (path == scannerPath) {
                return true
            }
        }
        return false
    }
}
