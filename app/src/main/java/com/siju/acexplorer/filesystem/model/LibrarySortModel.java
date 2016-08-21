package com.siju.acexplorer.filesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Siju on 24-07-2016.
 */
public class LibrarySortModel implements Parcelable{
    private int categoryId;
    private String libraryName;
    private boolean isChecked;

    public LibrarySortModel(int categoryId,String libraryName, boolean isChecked) {
        this.libraryName = libraryName;
        this.isChecked = isChecked;
        this.categoryId = categoryId;
    }

    public LibrarySortModel() {

    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof LibrarySortModel)) return false;
        LibrarySortModel o = (LibrarySortModel) obj;
        return o.categoryId == this.categoryId;
    }


    protected LibrarySortModel(Parcel in) {
        categoryId = in.readInt();
        libraryName = in.readString();
        isChecked = in.readByte() != 0;
    }

    public static final Creator<LibrarySortModel> CREATOR = new Creator<LibrarySortModel>() {
        @Override
        public LibrarySortModel createFromParcel(Parcel in) {
            return new LibrarySortModel(in);
        }

        @Override
        public LibrarySortModel[] newArray(int size) {
            return new LibrarySortModel[size];
        }
    };

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(categoryId);
        parcel.writeString(libraryName);
        parcel.writeByte((byte) (isChecked ? 1 : 0));
    }
}
