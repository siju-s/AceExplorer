package com.siju.filemanager.filesystem.model;

/**
 * Created by Siju on 14-08-2016.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class ZipProgressModel implements Parcelable {
    int id,p1,p2;
    long total,done;
    boolean completed=false,move=false;
    String name;

    protected ZipProgressModel(Parcel in) {
        id = in.readInt();
        p1 = in.readInt();
        p2 = in.readInt();
        total = in.readLong();
        done = in.readLong();
        completed = in.readByte() != 0;
        move = in.readByte() != 0;
        name = in.readString();
    }

    public static final Creator<ZipProgressModel> CREATOR = new Creator<ZipProgressModel>() {
        @Override
        public ZipProgressModel createFromParcel(Parcel in) {
            return new ZipProgressModel(in);
        }

        @Override
        public ZipProgressModel[] newArray(int size) {
            return new ZipProgressModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZipProgressModel(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getDone() {
        return done;
    }

    public void setDone(long done) {
        this.done = done;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(p1);
        dest.writeInt(p2);
        dest.writeLong(total);
        dest.writeLong(done);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeByte((byte) (move ? 1 : 0));
        dest.writeString(name);
    }
}
