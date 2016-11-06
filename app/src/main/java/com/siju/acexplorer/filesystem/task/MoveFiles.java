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
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class MoveFiles extends AsyncTask<String, Void, Integer> {
    private final ArrayList<FileInfo> files;
    private final ArrayList<CopyData> copyData;
    private final Context context;
    private String mCurrentDir;

    public MoveFiles(Context context,ArrayList<FileInfo> files,ArrayList<CopyData> copyData) {
        this.context = context;
        this.files = files;
        this.copyData =  copyData;
     }

    @Override
    protected Integer doInBackground(String... strings) {
        mCurrentDir = strings[0];
        int filesMoved = 0;

        if (files.size() == 0) return 0;

        for (FileInfo f : files) {
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
            String path = mCurrentDir + "/" + fileName;
            if (action == FileUtils.ACTION_KEEP) {
                String fileNameWithoutExt = fileName.substring(0, fileName.
                        lastIndexOf("."));
                path = mCurrentDir + "/" + fileNameWithoutExt + "(2)" + "." + f.getExtension();
            }
            File file = new File(path);
            File file1 = new File(f.getFilePath());
            if (file1.renameTo(file)) {
                filesMoved++;
            }
        }

        return filesMoved;
    }

    @Override
    public void onPostExecute(Integer count) {

        if (files.size() != count) {
            boolean canWrite = new File(mCurrentDir).canWrite();

            Logger.log("TAG", "File size"+files.size()+" moved=="+count + " Can write = "+canWrite);
            Intent intent = new Intent(context, CopyService.class);
            intent.putExtra("FILE_PATHS", files);
            intent.putExtra("COPY_DIRECTORY", mCurrentDir);
            intent.putExtra("move", true);
            intent.putExtra("MODE", 1);
            context.startService(intent);
        } else {
            Intent intent = new Intent("refresh");
            intent.putExtra(FileConstants.OPERATION, FileConstants.MOVE);
            context.sendBroadcast(intent);
        }
/*        if (b) {

            for (FileInfo f : files) {
                FileUtils.scanFile(context, f.getFilePath());
                FileUtils.scanFile(context, path + "/" + f.getFileName());

            }
        } */
    }
}
