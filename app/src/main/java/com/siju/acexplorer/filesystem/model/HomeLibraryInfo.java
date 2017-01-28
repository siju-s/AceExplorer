package com.siju.acexplorer.filesystem.model;

import com.siju.acexplorer.filesystem.groups.Category;

public class HomeLibraryInfo {

    private final String categoryName;
    private final int resourceId;
    private int count;
    private Category category;

    public HomeLibraryInfo(Category category, String categoryName, int resourceId, int count) {
        this.category = category;
        this.categoryName = categoryName;
        this.resourceId = resourceId;
        this.count = count;
    }



    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

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
