package com.siju.acexplorer.trash;


import android.os.Parcel;
import android.os.Parcelable;

public class TrashModel implements Parcelable{

    private String destination;
    private String source;

    public TrashModel(String destination, String source) {
        this.destination = destination;
        this.source = source;
    }

    protected TrashModel(Parcel in) {
        destination = in.readString();
        source = in.readString();
    }

    public static final Creator<TrashModel> CREATOR = new Creator<TrashModel>() {
        @Override
        public TrashModel createFromParcel(Parcel in) {
            return new TrashModel(in);
        }

        @Override
        public TrashModel[] newArray(int size) {
            return new TrashModel[size];
        }
    };

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
          dest.writeString(destination);
          dest.writeString(source);
    }
}
