/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.siju.acexplorer.filesystem.task;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.operations.OperationUtils;
import com.siju.acexplorer.filesystem.operations.Operations;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILES;

public class MoveFiles extends AsyncTask<String, Void, Integer> {
    private final ArrayList<FileInfo> filesToMove;
    private final ArrayList<CopyData> copyData;
    private final Context context;
    private String destinationDir;
    private ArrayList<String> filesMovedList;

    public MoveFiles(Context context, ArrayList<FileInfo> filesToMove, ArrayList<CopyData> copyData) {
        this.context = context;
        this.filesToMove = filesToMove;
        this.copyData =  copyData;
     }

    @Override
    protected Integer doInBackground(String... strings) {
        destinationDir = strings[0];
        int filesMoved = 0;

        if (filesToMove.size() == 0) return 0;
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
