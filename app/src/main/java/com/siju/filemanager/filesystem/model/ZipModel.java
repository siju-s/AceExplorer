package com.siju.filemanager.filesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.zip.ZipEntry;

/**
 * Created by Siju on 27-07-2016.
 */
public class ZipModel implements Parcelable {

    boolean directory;
    ZipEntry entry;
    String name;
    long date, size;

    public ZipModel(ZipEntry entry, long date, long size, boolean directory) {
        this.directory = directory;
        this.entry = entry;
        if (entry != null) {
            name = entry.getName();
            this.date = date;
            this.size = size;

        }
    }

    public ZipEntry getEntry() {
        return entry;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public long getTime() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p1, int p2) {
        p1.writeString(name);
        p1.writeLong(size);
        p1.writeLong(date);
        p1.writeInt(isDirectory() ? 1 : 0);
    }

    public static final Parcelable.Creator<ZipModel> CREATOR =
            new Parcelable.Creator<ZipModel>() {
                public ZipModel createFromParcel(Parcel in) {
                    return new ZipModel(in);
                }

                public ZipModel[] newArray(int size) {
                    return new ZipModel[size];
                }
            };

    public ZipModel(Parcel im) {
        name = im.readString();
        size = im.readLong();
        date = im.readLong();
        int i = im.readInt();
        if (i == 0) {
            directory = false;
        } else {
            directory = true;
        }
        entry = new ZipEntry(name);
    }


}
