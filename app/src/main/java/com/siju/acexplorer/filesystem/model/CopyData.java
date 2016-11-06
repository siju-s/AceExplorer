package com.siju.acexplorer.filesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CopyData implements Parcelable {
    private final String filePath;
    private final int action;

    public CopyData(String filePath) {
        this.filePath = filePath;
        this.action = com.siju.acexplorer.filesystem.utils.FileUtils.ACTION_KEEP;
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


    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CopyData)) return false;
        CopyData o = (CopyData) obj;
        return o.filePath.equals(this.filePath);
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
