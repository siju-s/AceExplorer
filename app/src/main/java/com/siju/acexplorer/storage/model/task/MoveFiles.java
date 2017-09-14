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
import android.os.AsyncTask;

import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.OperationUtils;
import com.siju.acexplorer.storage.model.operations.Operations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;

public class MoveFiles extends AsyncTask<String, Void, Integer> {
    private final List<FileInfo> filesToMove;
    private final List<CopyData> copyData;
    private final Context context;
    private String destinationDir;
    private ArrayList<String> filesMovedList;

    public MoveFiles(Context context, List<FileInfo> filesToMove, List<CopyData> copyData) {
        this.context = context;
        this.filesToMove = filesToMove;
        this.copyData =  copyData;
     }

    @Override
    protected Integer doInBackground(String... strings) {
        destinationDir = strings[0];
        int filesMoved = 0;

        if (filesToMove.size() == 0) {
            return 0;
        }
        filesMovedList = new ArrayList<>();

        for (FileInfo f : filesToMove) {
            int action = FileUtils.ACTION_NONE;

            if (copyData != null) {
                for (CopyData copyData1 : copyData) {
                    if (copyData1.getFilePath().equals(f.getFilePath())) {
                        action = copyData1.getAction();
                        break;
                    }
                }
            }
            String fileName = f.getFileName();
            String path = destinationDir + "/" + fileName;
            if (action == FileUtils.ACTION_KEEP) {
                String fileNameWithoutExt = fileName.substring(0, fileName.
                        lastIndexOf("."));
                path = destinationDir + "/" + fileNameWithoutExt + "(2)" + "." + f.getExtension();
            }
            File file = new File(path);
            File file1 = new File(f.getFilePath());
            if (file1.renameTo(file)) {
                filesMoved++;
                filesMovedList.add(file.getAbsolutePath());
            }
        }

        return filesMoved;
    }

    @Override
    public void onPostExecute(Integer count) {

        if (count > 0) {
            Intent intent = new Intent(ACTION_OP_REFRESH);
            intent.putExtra(OperationUtils.KEY_OPERATION, Operations.CUT);
            intent.putStringArrayListExtra(KEY_FILES, filesMovedList);
            context.sendBroadcast(intent);
        }

        if (filesToMove.size() != count) {
            boolean canWrite = new File(destinationDir).canWrite();

            Logger.log("TAG", "File size"+ filesToMove.size()+" moved=="+count + " Can write = "+canWrite);
            Intent copyIntent = new Intent(context, CopyService.class);
            copyIntent.putParcelableArrayListExtra(KEY_FILES, filesToMove);
            copyIntent.putParcelableArrayListExtra(OperationUtils.KEY_CONFLICT_DATA, copyData);
            copyIntent.putExtra(OperationUtils.KEY_FILEPATH, destinationDir);
            context.startService(copyIntent);
        }
    }
}
