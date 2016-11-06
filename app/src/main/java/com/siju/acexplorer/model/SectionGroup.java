package com.siju.acexplorer.model;

import java.util.ArrayList;

public class SectionGroup {
    private final String mHeader;
    private final ArrayList<SectionItems> mChildItems;

    public SectionGroup(String mHeader, ArrayList<SectionItems> mChildItems) {

        this.mHeader = mHeader;
        this.mChildItems = mChildItems;
    }

    public String getmHeader() {
        return mHeader;
    }

// --Commented out by Inspection START (06-11-2016 11:07 PM):
//    public void setmHeader(String mHeader) {
//        this.mHeader = mHeader;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:07 PM)

    public ArrayList<SectionItems> getmChildItems() {
        return mChildItems;
    }

// --Commented out by Inspection START (06-11-2016 11:07 PM):
//    public void setmChildItems(ArrayList<SectionItems> mChildItems) {
//        this.mChildItems = mChildItems;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:07 PM)


}
