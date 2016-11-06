package com.siju.acexplorer.filesystem.model;

public class HomeLibraryInfo {

    private final String categoryName;
    private final int resourceId;
    private int count;
    private final int categoryId;

    public HomeLibraryInfo(int categoryId,String categoryName, int resourceId, int count) {
        this.categoryName = categoryName;
        this.resourceId = resourceId;
        this.count = count;
        this.categoryId = categoryId;
    }

    public int getCategoryId() {
        return categoryId;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setCategoryId(int categoryId) {
//        this.categoryId = categoryId;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public String getCategoryName() {
        return categoryName;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setCategoryName(String categoryName) {
//        this.categoryName = categoryName;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public int getResourceId() {
        return resourceId;
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setResourceId(int resourceId) {
//        this.resourceId = resourceId;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
