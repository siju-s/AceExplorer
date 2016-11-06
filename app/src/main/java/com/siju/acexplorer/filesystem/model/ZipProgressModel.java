package com.siju.acexplorer.filesystem.model;


import android.os.Parcel;
import android.os.Parcelable;

public class ZipProgressModel implements Parcelable {
    private int id;
    private int p1;
    private int p2;
    private long total;
    private long done;
    private boolean completed=false;
    private boolean move=false;
    private String name;

    private ZipProgressModel(Parcel in) {
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

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public String getName() {
//        return name;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

    public void setName(String name) {
        this.name = name;
    }

    public ZipProgressModel(){}

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public int getId() {
//        return id;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

    public void setId(int id) {
        this.id = id;
    }

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public int getP2() {
//        return p2;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

    public void setP2(int p2) {
        this.p2 = p2;
    }

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public boolean isMove() {
//        return move;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

    public void setMove(boolean move) {
        this.move = move;
    }

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public boolean isCompleted() {
//        return completed;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public long getDone() {
//        return done;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

    public void setDone(long done) {
        this.done = done;
    }

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public long getTotal() {
//        return total;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

    public void setTotal(long total) {
        this.total = total;
    }

// --Commented out by Inspection START (06-11-2016 11:08 PM):
//    public int getP1() {
//        return p1;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:08 PM)

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
