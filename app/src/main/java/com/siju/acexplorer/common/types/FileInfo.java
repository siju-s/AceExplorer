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

package com.siju.acexplorer.common.types;

import android.os.Parcel;
import android.os.Parcelable;

import com.siju.acexplorer.main.model.groups.Category;


@SuppressWarnings("unused")
public class FileInfo implements Parcelable {

    public static final Creator<FileInfo> CREATOR  = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
    private             Category          category;
    private             Category          subcategory;
    private             String            fileName;
    private             String            filePath;
    private             String            fileDate;
    private             String            noOfFilesOrSize;
    private             String            extension;
    private             String            permissions;
    private             String            title;
    private             int               count;
    private             int               icon;
    private             long              id       = -1;
    private             long              bucketId = -1;
    private             long              numTracks;
    private             long              date;
    private             long              size;
    private             long              width;
    private             long              height;

    private             boolean           isDirectory;
    private             boolean           isRootMode;


    // For Images, Audio, Video
    public FileInfo(Category category, long id, long bucketId, String fileName, String filePath,
                    long fileDate, long size,
                    String extension)
    {
        this(category, id, fileName, filePath, fileDate, size, extension);
        this.bucketId = bucketId;
    }


    public static FileInfo createImagePropertiesInfo(Category category, long id, long bucketId, String fileName, String filePath,
                                     long fileDate, long size,
                                     String extension, long width, long height) {
        FileInfo info = new FileInfo(category, id, bucketId, fileName, filePath, fileDate, size, extension);
        info.width = width;
        info.height = height;
        return info;
    }

    public static FileInfo createDummyRecentItem() {
        return new FileInfo(Category.RECENT, 0);
    }


    // Used for apk
    public FileInfo(Category category, long id, String fileName, String filePath, long fileDate,
                    long size,
                    String extension)
    {
        this(category, fileName, filePath, fileDate, size, false, extension, null, false);
        this.id = id;
    }

    public FileInfo(Category category, String fileName, String filePath, long fileDate, long
            noOfFilesOrSize, boolean isDirectory,
                    String extension, String permissions, boolean rootMode)
    {
        this.fileName = fileName;
        this.filePath = filePath;
        this.date = fileDate;
        this.size = noOfFilesOrSize;
        this.isDirectory = isDirectory;
        this.extension = extension;
        this.category = category;
        this.permissions = permissions;
        isRootMode = rootMode;
    }

    // For DialogPicker
    private FileInfo(Category category, String fileName, String filePath, int icon) {
        this.category = category;
        this.fileName = fileName;
        this.filePath = filePath;
        this.icon = icon;
    }

    public static FileInfo createPicker(Category category, String fileName, String filePath, int icon) {
        return new FileInfo(category, fileName, filePath, icon);
    }

    // For HomeLib count
    public FileInfo(Category category, int count) {
        this.category = category;
        this.count = count;
    }

    // For HomeLib count
    public FileInfo(Category category, Category subcategory, int count) {
        this.category = category;
        this.subcategory = subcategory;
        this.count = count;
    }

    //For camera
    public FileInfo(Category category, Category subcategory, String filePath, int count) {
        this.category = category;
        this.subcategory = subcategory;
        this.filePath = filePath;
        this.count = count;
    }

    public static FileInfo createCameraGenericInfo(Category category, Category subcategory, String filePath, int count) {
        return new FileInfo(category, subcategory, filePath, count);
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
        isRootMode = in.readByte() != 0;
        width = in.readLong();
        height = in.readLong();
    }

    public FileInfo(Category category, long albumId, String title, long numTracks) {
        this.category = category;
        this.id = albumId;
        this.title = title;
        this.numTracks = numTracks;
    }

    // Folder Images
    public FileInfo(Category category, long bucketId, String bucketName, String path, int count) {
        this.category = category;
        this.bucketId = bucketId;
        this.fileName = bucketName;
        this.filePath = path;
        this.numTracks = count;

    }

    // App manager
    private FileInfo(Category category, String name, String packageName, long date, long size) {
        this.category = category;
        this.fileName = name;
        this.filePath = packageName;
        this.date = date;
        this.size = size;

    }

    public static FileInfo createAppInfo(Category category, String name, String packageName, long date, long size) {
        return new FileInfo(category, name, packageName, date, size);
    }

    public boolean isRootMode() {
        return isRootMode;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FileInfo)) {
            return false;
        }
        FileInfo o = (FileInfo) obj;
        if (o.filePath == null && this.filePath == null) {
            return true;
        }
        else if (o.filePath == null || this.filePath == null) {
            return false;
        }
        return o.filePath.equals(this.filePath);
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Category getCategory() {
        return category;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public Category getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Category subcategory) {
        this.subcategory = subcategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getNumTracks() {
        return numTracks;
    }

    public void setNumTracks(long numTracks) {
        this.numTracks = numTracks;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    @Override
    public int describeContents() {
        return 0;
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
        parcel.writeByte((byte) (isRootMode ? 1 : 0));
        parcel.writeLong(width);
        parcel.writeLong(height);
    }


}
