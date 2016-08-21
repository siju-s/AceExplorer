package com.siju.acexplorer.filesystem.task;

import com.siju.acexplorer.filesystem.model.FileInfo;

/**
 * Created by arpitkh996 on 25-01-2016.
 */
public class FileBundle {
    private FileInfo file,file2;
    private boolean move;
    public FileBundle(FileInfo file, FileInfo file2, boolean move) {
        this.file = file;
        this.file2 = file2;
        this.move=move;
    }

    public FileInfo getFile() {
        return file;
    }

    public FileInfo getFile2() {
        return file2;
    }

    public boolean isMove() {
        return move;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileBundle)) {
            return false;
        }
        if (this == obj || (this.file.equals(((FileBundle) obj).getFile()) && this.file2.equals(((FileBundle) obj).getFile2()))) {
            return true;
        }
        return false;    }
}
