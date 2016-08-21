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
import android.support.v4.app.Fragment;

import com.siju.acexplorer.filesystem.FileListDualFragment;
import com.siju.acexplorer.filesystem.FileListFragment;
import com.siju.acexplorer.filesystem.model.FileInfo;

import java.io.File;
import java.util.ArrayList;

public class MoveFiles extends AsyncTask<String, Void, Integer> {
    ArrayList<FileInfo> files;
    Context context;
    int mode;
    private String mCurrentDir;
    private Fragment mFragment;
    private boolean mIsDualPane;

    public MoveFiles(Context context, Fragment fragment, ArrayList<FileInfo> files,
                     boolean isDualPane) {
        this.context = context;
        this.files = files;
        mFragment = fragment;
        mIsDualPane = isDualPane;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        mCurrentDir = strings[0];
        int filesMoved = 0;

        if (files.size() == 0) return 0;

        for (FileInfo f : files) {
            File file = new File(mCurrentDir + "/" + f.getFileName());
            File file1 = new File(f.getFilePath());
            if (!file1.renameTo(file)) {
                filesMoved++;
            }
        }
        return filesMoved;
    }

    @Override
    public void onPostExecute(Integer count) {

        if (files.size() != count) {
            Intent intent = new Intent(context, CopyService.class);
            intent.putExtra("FILE_PATHS", (files));
            intent.putExtra("COPY_DIRECTORY", mCurrentDir);
            intent.putExtra("move", true);
            intent.putExtra("MODE", mode);
            context.startService(intent);
        } else {
            if (mIsDualPane) {
                ((FileListDualFragment) mFragment).refreshList();
            } else {
                ((FileListFragment) mFragment).refreshList();
            }

        }
/*        if (b) {

            for (FileInfo f : files) {
                FileUtils.scanFile(context, f.getFilePath());
                FileUtils.scanFile(context, path + "/" + f.getFileName());

            }
        } */
    }
}
