package com.siju.filemanager.model;

/**
 * Created by Siju on 12-06-2016.
 */

public class SectionItems {

    private String mFirstLine;
    private String mSecondLine;
    private int mImage;


    public SectionItems(String mFirstLine, String mSecondLine, int mImage) {
        this.mFirstLine = mFirstLine;
        this.mSecondLine = mSecondLine;
        this.mImage = mImage;
    }

    public int getmImage() {
        return mImage;
    }

    public void setmImage(int mImage) {
        this.mImage = mImage;
    }

    public String getmFirstLine() {
        return mFirstLine;
    }

    public void setmFirstLine(String mFirstLine) {
        this.mFirstLine = mFirstLine;
    }

    public String getmSecondLine() {
        return mSecondLine;
    }

    public void setmSecondLine(String mSecondLine) {
        this.mSecondLine = mSecondLine;
    }
}
