package com.siju.acexplorer.view;

/**
 * Created by sj on 28/10/17.
 */

public class ConflictFileInfo {

    private String fileName;
    private String filePath;
    private String fileDate;
    private String fileSize;

    public ConflictFileInfo(String fileName, String filePath, String fileDate, String fileSize) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileDate = fileDate;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }
}
