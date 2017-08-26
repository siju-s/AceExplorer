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

package com.siju.acexplorer.filesystem.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.root.RootDeniedException;
import com.siju.acexplorer.filesystem.root.RootUtils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.filesystem.operations.Operations.DELETE;

public class DeleteTask extends AsyncTask<Void, Void, Integer> {

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

    void setmShowToast() {
        mShowToast = false;
    }


    @Override
    protected Integer doInBackground(Void... params) {
        int deletedCount = 0;


        totalFiles = fileList.size();

        for (int i = 0; i < totalFiles; i++) {
            String path = fileList.get(i).getFilePath();
            boolean isDeleted = FileUtils.deleteFile(new File(path), mContext);

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


        return deletedCount;
    }

    @Override
    protected void onPostExecute(Integer filesDel) {
        int deletedFiles = filesDel;
        Intent intent = new Intent(ACTION_OP_REFRESH);
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_OPERATION, DELETE);
        bundle.putParcelableArrayList(KEY_FILES, deletedFilesList);
        intent.putExtras(bundle);
        mContext.sendBroadcast(intent);
        if (mShowToast) {
            if (deletedFiles != 0) {
                FileUtils.showMessage(mContext, mContext.getResources().getQuantityString(R.plurals.number_of_files,
                        deletedFiles, deletedFiles) + " " + mContext.getString(R.string.msg_delete_success));
            }

            if (totalFiles != deletedFiles) {
                FileUtils.showMessage(mContext, mContext.getString(R.string.msg_delete_failure));
            }
        }

    }

}
