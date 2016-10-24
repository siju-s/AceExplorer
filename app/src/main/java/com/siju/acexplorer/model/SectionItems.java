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

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }

    public String getSecondLine() {
        return secondLine;
    }

    public void setSecondLine(String secondLine) {
        this.secondLine = secondLine;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
