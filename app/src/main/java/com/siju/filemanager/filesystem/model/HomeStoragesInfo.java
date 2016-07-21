package com.siju.filemanager.filesystem.model;

/**
 * Created by Siju on 13-06-2016.
 */

public class HomeStoragesInfo {

    private String storageName;
    private int resourceId;
    private int progress;
    private String space;

    public HomeStoragesInfo(String storageName, int resourceId, int progress, String space) {
        this.storageName = storageName;
        this.resourceId = resourceId;
        this.progress = progress;
        this.space = space;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }
}
