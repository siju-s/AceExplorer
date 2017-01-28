package com.siju.acexplorer.filesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.siju.acexplorer.filesystem.groups.Category;

public class LibrarySortModel implements Parcelable{
    private String libraryName;
    private boolean isChecked;
    private Category category;

    public LibrarySortModel(Category category,String libraryName) {
        this.category = category;
        this.libraryName = libraryName;
        this.isChecked = true;
    }

    public LibrarySortModel() {

    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof LibrarySortModel)) return false;
        LibrarySortModel o = (LibrarySortModel) obj;
        return o.category.equals(this.category);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    private LibrarySortModel(Parcel in) {
        category = (Category) in.readSerializable();
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

        parcel.writeSerializable(category);
        parcel.writeString(libraryName);
        parcel.writeByte((byte) (isChecked ? 1 : 0));
    }
}
