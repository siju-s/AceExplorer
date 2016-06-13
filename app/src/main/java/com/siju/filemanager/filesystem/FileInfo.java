package com.siju.filemanager.filesystem;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileInfo  {

    private String fileName;
    private String filePath;
    private String fileDate;
    private String noOfFilesOrSize;
    private boolean isDirectory;

    public FileInfo(String fileName, String filePath, String fileDate, String noOfFilesOrSize, boolean isDirectory) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileDate = fileDate;
        this.noOfFilesOrSize = noOfFilesOrSize;
        this.isDirectory = isDirectory;
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


    public String getNoOfFilesOrSize() {
        return noOfFilesOrSize;
    }

    public void setNoOfFilesOrSize(String noOfFilesOrSize) {
        this.noOfFilesOrSize = noOfFilesOrSize;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}
