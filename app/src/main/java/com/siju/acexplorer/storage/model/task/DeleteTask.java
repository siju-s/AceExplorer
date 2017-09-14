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
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.root.RootDeniedException;
import com.siju.acexplorer.model.root.RootUtils;
import com.siju.acexplorer.model.helper.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.Operations.DELETE;

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
          if (deleteResultCallback != null) {
              deleteResultCallback.onFileDeleted(deletedCount, deletedFilesList, mShowToast);
          }
              }
          }).start();

          Intent intent = new Intent(ACTION_OP_REFRESH);
          Bundle bundle = new Bundle();
          bundle.putSerializable(KEY_OPERATION, DELETE);
          bundle.putParcelableArrayList(KEY_FILES, deletedFilesList);
          intent.putExtras(bundle);
          mContext.sendBroadcast(intent);
          if (mShowToast) {
              if (deletedCount != 0) {
                  FileUtils.showMessage(mContext, mContext.getResources().getQuantityString(R.plurals.number_of_files,
                          deletedCount, deletedCount) + " " + mContext.getString(R.string.msg_delete_success));
              }

              if (totalFiles != deletedCount) {
                  FileUtils.showMessage(mContext, mContext.getString(R.string.msg_delete_failure));
              }
          }
      }

}
