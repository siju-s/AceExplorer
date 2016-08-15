package com.siju.acexplorer.filesystem.model;

/**
 * Created by Siju on 13-06-2016.
 */

public class HomeLibraryInfo {

    private String categoryName;
    private int resourceId;
    private int count;
    private int categoryId;

    public HomeLibraryInfo(int categoryId,String categoryName, int resourceId, int count) {
        this.categoryName = categoryName;
        this.resourceId = resourceId;
        this.count = count;
        this.categoryId = categoryId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
