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

import com.siju.acexplorer.model.helper.FileUtils;

public class CopyData implements Parcelable {
    private final String filePath;
    private final int    action;

    CopyData(String filePath) {
        this.filePath = filePath;
        this.action = FileUtils.ACTION_KEEP;
    }

    private CopyData(Parcel in) {
        filePath = in.readString();
        action = in.readInt();
    }

    public static final Creator<CopyData> CREATOR = new Creator<CopyData>() {
        @Override
        public CopyData createFromParcel(Parcel in) {
            return new CopyData(in);
        }

        @Override
        public CopyData[] newArray(int size) {
            return new CopyData[size];
        }
    };


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CopyData)) {
            return false;
        }
        CopyData o = (CopyData) obj;
        return o.filePath.equals(this.filePath);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getFilePath() {
        return filePath;
    }

    public int getAction() {
        return action;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeInt(action);
    }
}
