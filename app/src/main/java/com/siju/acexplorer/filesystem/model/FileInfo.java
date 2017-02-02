package com.siju.acexplorer.filesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.siju.acexplorer.filesystem.groups.Category;

import static android.R.attr.type;

public class FileInfo implements Parcelable {

    private String fileName;
    private String filePath;
    private String fileDate;
    private String noOfFilesOrSize;
    private boolean isDirectory;
    private String extension;
    private long id = -1;
    private long bucketId = -1;
    private String permissions;
    private long date;
    private long size;
    private Category category;
    private int count;
    private int icon;


    public FileInfo(Category category, String fileName, String filePath, long fileDate, long noOfFilesOrSize, boolean isDirectory,
                    String extension, String permissions) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.date = fileDate;
        this.size = noOfFilesOrSize;
        this.isDirectory = isDirectory;
        this.extension = extension;
        this.category = category;
        this.permissions = permissions;
    }

    // Used for apk
    public FileInfo(Category category, long id, String fileName, String filePath, long fileDate, long size,
                    String extension) {
        this(category, fileName, filePath, fileDate, size, false, extension, null);
        this.id = id;
    }


    public FileInfo(Category category, long id, long bucketId, String fileName, String filePath, long fileDate, long size,
                    String extension) {
        this(category, id, fileName, filePath, fileDate, size, extension);
        this.bucketId = bucketId;
    }


    public FileInfo(Category category, String fileName, String filePath, int icon) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.icon = icon;
        this.category = category;
    }

    public FileInfo(Category category, int count) {
        this.category = category;
        this.count = count;
    }


    private FileInfo(Parcel in) {
        category = (Category) in.readSerializable();
        fileName = in.readString();
        filePath = in.readString();
        fileDate = in.readString();
        noOfFilesOrSize = in.readString();
        isDirectory = in.readByte() != 0;
        extension = in.readString();
        id = in.readLong();
        bucketId = in.readLong();
        permissions = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(category);
        parcel.writeString(fileName);
        parcel.writeString(filePath);
        parcel.writeString(fileDate);
        parcel.writeString(noOfFilesOrSize);
        parcel.writeByte((byte) (isDirectory ? 1 : 0));
        parcel.writeString(extension);
        parcel.writeLong(id);
        parcel.writeLong(bucketId);
        parcel.writeString(permissions);

    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof FileInfo)) return false;
        FileInfo o = (FileInfo) obj;
        return o.filePath.equals(this.filePath);
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

    public int getIcon() {
        return icon;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setIcon(int icon) {
//        this.icon = icon;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public Category getCategory() {
        return category;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setCategoryId(int categoryId) {
//        this.categoryId = categoryId;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public int getCount() {
        return count;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setCount(int count) {
//        this.count = count;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public long getBucketId() {
        return bucketId;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setBucketId(long bucketId) {
//        this.bucketId = bucketId;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public long getId() {
//        return id;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setId(long id) {
//        this.id = id;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public int getType() {
        return type;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setType(int type) {
//        this.type = type;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public String getExtension() {
        return extension;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setExtension(String extension) {
//        this.extension = extension;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

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

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public String getFileDate() {
//        return fileDate;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setDate(long date) {
//        this.date = date;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public long getSize() {
        return size;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setSize(long size) {
//        this.size = size;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public long getDate() {
        return date;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setFileDate(String fileDate) {
//        this.fileDate = fileDate;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)


    public String getNoOfFilesOrSize() {
        return noOfFilesOrSize;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setNoOfFilesOrSize(String noOfFilesOrSize) {
//        this.noOfFilesOrSize = noOfFilesOrSize;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public String getMimeType() {
//        return mimeType;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setMimeType(String mimeType) {
//        this.mimeType = mimeType;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public boolean isDirectory() {
        return isDirectory;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setDirectory(boolean directory) {
//        isDirectory = directory;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public String getPermissions() {
        return permissions;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setPermissions(String permissions) {
//        this.permissions = permissions;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    @Override
    public int describeContents() {
        return 0;
    }


}
