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

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.MediaStoreHelper;
import com.siju.acexplorer.model.root.RootDeniedException;
import com.siju.acexplorer.model.root.RootUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.groups.Category.FILES;

public class DeleteTask {


    private int totalFiles;
    private final ArrayList<FileInfo> deletedFilesList = new ArrayList<>();
    private final Context mContext;
    private final boolean mIsRootMode;
    private ArrayList<FileInfo> fileList = new ArrayList<>();
    private boolean mShowToast = true;


    public DeleteTask(Context context, boolean rootMode, ArrayList<FileInfo> fileList) {
        mContext = context;
        mIsRootMode = rootMode;
        this.fileList = fileList;
    }

    private DeleteResultCallback deleteResultCallback;

    public void setDeleteResultCallback(DeleteResultCallback deleteResultCallback) {
        this.deleteResultCallback = deleteResultCallback;
    }

    public interface DeleteResultCallback {

        void onFileDeleted(int deletedCount, List<FileInfo> fileList, boolean showToast);
    }

    void setmShowToast() {
        mShowToast = false;
    }


    public void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);


                int deletedCount = 0;
                totalFiles = fileList.size();

                for (int i = 0; i < totalFiles; i++) {
                    String path = fileList.get(i).getFilePath();
                    boolean isDeleted = FileUtils.deleteFile(new File(path));

                    if (!isDeleted) {
                        if (mIsRootMode) {
                            try {
                                RootUtils.mountRW(path);
                                RootUtils.delete(path);
                                RootUtils.mountRO(path);
                                deletedFilesList.add(fileList.get(i));
                                deletedCount++;
                            } catch (RootDeniedException e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        deletedFilesList.add(fileList.get(i));
                        deletedCount++;
                    }
                }
                deleteFromMediaStore(deletedFilesList);
                if (deleteResultCallback != null) {
                    deleteResultCallback.onFileDeleted(totalFiles, deletedFilesList, mShowToast);
                }
            }
        }).start();

    }

    private static final String TAG = "DeleteTask";

    private void deleteFromMediaStore(ArrayList<FileInfo> deletedFilesList) {
        for (int i = 0; i < deletedFilesList.size() ; i++) {
            FileInfo fileInfo = deletedFilesList.get(i);
            Category category = fileInfo.getCategory();
            int type;
            if (category.equals(FILES)) {
                type = fileInfo.getType();
            } else {
                type = category.getValue();
            }
            Log.d(TAG, "deleteFromMediaStore: Type:"+type);
            int deleted = MediaStoreHelper.removeMedia(mContext, fileInfo.getFilePath(), type);
            Log.d(TAG, "deleteFromMediaStore: deleted:"+deleted);
        }
    }


}
