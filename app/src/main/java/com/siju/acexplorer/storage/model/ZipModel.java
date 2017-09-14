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

package com.siju.acexplorer.storage.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.zip.ZipEntry;

public class ZipModel implements Parcelable {

    private final boolean directory;
    private final ZipEntry entry;
    private String name;
    private long date;
    private long size;

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

    private ZipModel(Parcel im) {
        name = im.readString();
        size = im.readLong();
        date = im.readLong();
        int i = im.readInt();
        directory = i != 0;
        entry = new ZipEntry(name);
    }


}
