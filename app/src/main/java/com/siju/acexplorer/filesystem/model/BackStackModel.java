package com.siju.acexplorer.filesystem.model;

import com.siju.acexplorer.filesystem.groups.Category;

public class BackStackModel {

    private final String filePath;
    private final Category category;

    public BackStackModel(String filePath, Category category) {
        this.filePath = filePath;
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public Category getCategory() {
        return category;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof BackStackModel)) return false;
        BackStackModel o = (BackStackModel) obj;
        return o.filePath != null && o.filePath.equals(this.filePath);
    }
}
