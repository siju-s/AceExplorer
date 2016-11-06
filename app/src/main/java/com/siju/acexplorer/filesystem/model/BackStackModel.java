package com.siju.acexplorer.filesystem.model;

public class BackStackModel {

    private final String filePath;
    private final int category;

    public BackStackModel(String filePath, int category) {
        this.filePath = filePath;
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getCategory() {
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
