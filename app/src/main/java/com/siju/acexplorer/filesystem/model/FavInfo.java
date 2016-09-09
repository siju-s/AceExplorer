package com.siju.acexplorer.filesystem.model;

/**
 * Created by Siju on 27-06-2016.
 */

public class FavInfo {
    private String fileName;
    private String filePath;



    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Arraylist.remove(Object) method works only if this implemented
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof FavInfo)) return false;
        FavInfo o = (FavInfo) obj;
        return o.filePath.equalsIgnoreCase(this.filePath);
    }


}