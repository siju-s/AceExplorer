package com.siju.filemanager.model;

import java.util.ArrayList;

/**
 * Created by Siju on 12-06-2016.
 */

public class SectionGroup {
    private String mHeader;
    private ArrayList<SectionItems> mChildItems;

    public SectionGroup(String mHeader, ArrayList<SectionItems> mChildItems) {

        this.mHeader = mHeader;
        this.mChildItems = mChildItems;
    }

    public String getmHeader() {
        return mHeader;
    }

    public void setmHeader(String mHeader) {
        this.mHeader = mHeader;
    }

    public ArrayList<SectionItems> getmChildItems() {
        return mChildItems;
    }

    public void setmChildItems(ArrayList<SectionItems> mChildItems) {
        this.mChildItems = mChildItems;
    }


}
