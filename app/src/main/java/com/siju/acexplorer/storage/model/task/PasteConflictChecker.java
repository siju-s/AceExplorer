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

import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.FileListLoader;
import com.siju.acexplorer.model.groups.StoragesGroup;
import com.siju.acexplorer.model.helper.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.StorageUtils.getInternalStorage;


public class PasteConflictChecker {

    private final ArrayList<FileInfo> totalFiles;
    private final ArrayList<FileInfo> conflictFiles = new ArrayList<>();
    private final ArrayList<FileInfo> destFiles = new ArrayList<>();

    private boolean rootmode;
    private final String destinationDir;
    private boolean isMove = false;


    public PasteConflictChecker(String currentDir, boolean
            rootMode, boolean isMoveOperation, ArrayList<FileInfo> files) {
        destinationDir = currentDir;
        this.rootmode = rootMode;
        this.isMove = isMoveOperation;
        this.totalFiles = files;
    }


    public void execute() {

        long totalBytes = calculateSize();
        boolean isRootDir = checkIfRootDir();

        Logger.log("PasteCOnflict", "isROotdir=" + isRootDir);

        File f = new File(destinationDir);

        if (isRootDir || f.getFreeSpace() >= totalBytes) {
            findConflictFiles();
            if (conflictFiles.size() == 0) {
                pasteResultCallback.checkWriteMode(destinationDir, totalFiles, isMove);
            } else {
                pasteResultCallback.showConflictDialog(totalFiles, conflictFiles, destFiles, destinationDir, isMove);
            }
        }
        else {
            pasteResultCallback.onLowSpace();
        }

    }

    private long calculateSize() {
        long totalBytes = 0;
        for (int i = 0; i < totalFiles.size(); i++) {
            FileInfo f1 = totalFiles.get(i);

            if (f1.isDirectory()) {
                totalBytes = totalBytes + FileUtils.getFolderSize(new File(f1.getFilePath()));
            }
            else {
                totalBytes = totalBytes + new File(f1.getFilePath()).length();
            }
        }
        return totalBytes;
    }

    private boolean checkIfRootDir() {
        boolean isRootDir = !destinationDir.startsWith(getInternalStorage());
        List<String> externalSDList = StoragesGroup.getInstance().getExternalSDList();

        for (String dir : externalSDList) {
            if (destinationDir.startsWith(dir)) {
                isRootDir = false;
            }
        }
        return isRootDir;
    }

    private void findConflictFiles() {
        ArrayList<FileInfo> listFiles = FileListLoader.getFilesList(destinationDir,
                rootmode, true, false);

        for (FileInfo fileInfo : listFiles) {
            for (FileInfo copiedFiles : totalFiles) {
                if (copiedFiles.getFileName().equals(fileInfo.getFileName())) {
                    conflictFiles.add(copiedFiles);
                    destFiles.add(fileInfo);
                }
            }
        }
    }



   /* private void showDialog() {

        Logger.log("TAG", "Counter=" + counter + " conflict size=" + conflictFiles.size());
        if (counter == conflictFiles.size() || conflictFiles.size() == 0) {
            if (totalFiles != null && totalFiles.size() != 0) {
                checkWriteMode();
            }
            else {
                Toast.makeText(context, isMove ? context.getString(R.string
                        .msg_move_failure) :
                        context.getString(R.string.msg_copy_failure), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            pasteResultCallback.showConflictDialog(conflictFiles, destinationDir, isMove);
        }
    }*/




    private PasteResultCallback pasteResultCallback;
    public void setListener(PasteResultCallback pasteResultCallback) {
        this.pasteResultCallback = pasteResultCallback;
    }

    public interface PasteResultCallback {
        void showConflictDialog(ArrayList<FileInfo> files, final List<FileInfo> conflictFiles,
                                List<FileInfo> destFiles, final String destinationDir, final boolean isMove);

        void onLowSpace();

        void checkWriteMode(String destinationDir, List<FileInfo> files,
                   boolean isMove);
    }
}
