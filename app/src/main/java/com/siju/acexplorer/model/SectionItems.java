package com.siju.acexplorer.model;

public class SectionItems {

    private String firstLine;
    private String secondLine;
    private int progress;
    private int icon;
    private String path;


    public SectionItems(String firstLine, String secondLine, int icon, String path, int progress) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.icon = icon;
        this.path = path;
        this.progress = progress;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SectionItems)) return false;
        SectionItems o = (SectionItems) obj;
        return o.path.equals(this.path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public String getSecondLine() {
        return secondLine;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
