package com.siju.acexplorer.filesystem.model;

/**
 * Created by Siju on 15-08-2016.
 */
public class BackStackModel {

    private String filePath;
    private int category;

    public BackStackModel(String filePath, int category) {
        this.filePath = filePath;
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
