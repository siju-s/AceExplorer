package com.siju.filemanager.filesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileInfo implements Parcelable {

    private String fileName;
    private String filePath;
    private String fileDate;
    private String noOfFilesOrSize;
    private boolean isDirectory;
    private String extension;
    private int type;
    private long id = -1;
    private long bucketId = -1;
    private String mimeType;

    public FileInfo(String fileName, String filePath, String fileDate, String noOfFilesOrSize, boolean isDirectory,
                    String extension, int type) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileDate = fileDate;
        this.noOfFilesOrSize = noOfFilesOrSize;
        this.isDirectory = isDirectory;
        this.extension = extension;
        this.type = type;
    }

    public FileInfo(long id, String fileName, String filePath, String fileDate, String noOfFilesOrSize, int type,
                    String extension,String mimeType) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileDate = fileDate;
        this.noOfFilesOrSize = noOfFilesOrSize;
        this.type = type;
        this.extension = extension;
        this.mimeType = mimeType;
    }

    /**
     * Used for Audio,Videos,Images
     * @param id
     * @param bucketId
     * @param fileName
     * @param filePath
     * @param fileDate
     * @param noOfFilesOrSize
     * @param type
     * @param extension
     */
    public FileInfo(long id, long bucketId, String fileName, String filePath, String fileDate, String noOfFilesOrSize,
                    int type,
                    String extension) {
        this.id = id;
        this.bucketId = bucketId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileDate = fileDate;
        this.noOfFilesOrSize = noOfFilesOrSize;
        this.type = type;
        this.extension = extension;
    }



    protected FileInfo(Parcel in) {
        fileName = in.readString();
        filePath = in.readString();
        fileDate = in.readString();
        noOfFilesOrSize = in.readString();
        isDirectory = in.readByte() != 0;
        extension = in.readString();
        type = in.readInt();
        id = in.readLong();
        bucketId = in.readLong();
        mimeType = in.readString();
    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    public long getBucketId() {
        return bucketId;
    }

    public void setBucketId(long bucketId) {
        this.bucketId = bucketId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fileName);
        parcel.writeString(filePath);
        parcel.writeString(fileDate);
        parcel.writeString(noOfFilesOrSize);
        parcel.writeByte((byte) (isDirectory ? 1 : 0));
        parcel.writeString(extension);
        parcel.writeInt(type);
        parcel.writeLong(id);
        parcel.writeLong(bucketId);
        parcel.writeString(mimeType);

    }
}
