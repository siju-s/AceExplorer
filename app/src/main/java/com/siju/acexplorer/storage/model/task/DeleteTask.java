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

package com.siju.acexplorer.storage.model.task;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.StorageUtils;
import com.siju.acexplorer.main.model.root.RootDeniedException;
import com.siju.acexplorer.main.model.root.RootUtils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.siju.acexplorer.main.model.StorageUtils.getDocumentFile;
import static com.siju.acexplorer.main.model.StorageUtils.isOnExtSdCard;
import static com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastLollipop;
import static com.siju.acexplorer.main.model.helper.SdkHelper.isKitkat;
import static com.siju.acexplorer.main.model.helper.UriHelper.getUriFromFile;

public class DeleteTask {

    private static final String TAG = "DeleteTask";

    private int totalFiles;
    private final ArrayList<FileInfo> deletedFilesList  = new ArrayList<>();
    private       ArrayList<FileInfo> fileList          = new ArrayList<>();
    private       Set<String>         filesToMediaIndex = new HashSet<>();

    private boolean mShowToast = true;


    public DeleteTask(Context context, ArrayList<FileInfo> fileList) {
        this.fileList = fileList;
    }

    private DeleteResultCallback deleteResultCallback;

    public void setDeleteResultCallback(DeleteResultCallback deleteResultCallback) {
        this.deleteResultCallback = deleteResultCallback;
    }

    public interface DeleteResultCallback {

        void onFileDeleted(int deletedCount, List<FileInfo> fileList, List<String> filestoMediaIndex, boolean showToast);
    }

    void setmShowToast() {
        mShowToast = false;
    }


    public void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                totalFiles = fileList.size();
                filesToMediaIndex.clear();
                deletedFilesList.clear();

                for (int i = 0; i < totalFiles; i++) {
                    String path = fileList.get(i).getFilePath();
                    boolean isDeleted = delete(new File(path));

                    if (!isDeleted) {
                        boolean isRootDir = StorageUtils.isRootDirectory(path);
                        if (!isRootDir) {
                            List<String> list = new ArrayList<>(filesToMediaIndex);
                            deleteResultCallback.onFileDeleted(totalFiles, deletedFilesList, list, mShowToast);
                            return;
                        }
                        boolean isRootMode = RootTools.isAccessGiven();
                        if (isRootMode) {
                            try {
                                RootUtils.mountRW(path);
                                RootUtils.delete(path);
                                RootUtils.mountRO(path);
                                deletedFilesList.add(fileList.get(i));
                                filesToMediaIndex.add(path);
                            } catch (RootDeniedException e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        deletedFilesList.add(fileList.get(i));
                    }
                }
                if (deleteResultCallback != null) {
                    List<String> list = new ArrayList<>(filesToMediaIndex);
                    deleteResultCallback.onFileDeleted(totalFiles, deletedFilesList, list, mShowToast);
                }
            }
        }).start();

    }


    private boolean delete(File file) {
        boolean fileDelete = deleteFile(file);

        if (file.delete() || fileDelete) {
            return true;
        }

        // Try with Storage Access Framework.
        if (isAtleastLollipop() && isOnExtSdCard(file)) {

            DocumentFile document = getDocumentFile(file, false);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (isKitkat()) {
            Context context = AceApplication.getAppContext();
            ContentResolver resolver = context.getContentResolver();

            try {
                Uri uri = getUriFromFile(file.getAbsolutePath(), context);
                if (uri != null) {
                    resolver.delete(uri, null, null);
                }
                return !file.exists();
            } catch (Exception e) {
                Logger.log(TAG, "Error when deleting file " + file.getAbsolutePath());
                return false;
            }
        }

        return !file.exists();
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    private boolean deleteFile(@NonNull final File file) {
        // First try the normal deletion.
        boolean isDeleted = false;
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File child : fileList) {
                    String path = child.getAbsolutePath();
                    isDeleted = deleteFile(child);
                    if (isDeleted) {
                        filesToMediaIndex.add(path);
                    }
                }
                String path = file.getAbsolutePath();
                isDeleted = file.delete();
                if (isDeleted) {
                    filesToMediaIndex.add(path);
                }
            }
        } else {
            String path = file.getAbsolutePath();
            isDeleted = file.delete();
            if (isDeleted) {
                filesToMediaIndex.add(path);
            }
        }
        return isDeleted;
    }


}
