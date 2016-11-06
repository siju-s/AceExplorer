package com.siju.acexplorer.model;

public class SectionItems {

    private final String firstLine;
    private final String secondLine;
    private final int progress;
    private final int icon;
    private final String path;


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

// --Commented out by Inspection START (06-11-2016 11:07 PM):
//    public void setPath(String path) {
//        this.path = path;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:07 PM)

    public int getIcon() {
        return icon;
    }

// --Commented out by Inspection START (06-11-2016 11:07 PM):
//    public void setIcon(int icon) {
//        this.icon = icon;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:07 PM)

    public String getFirstLine() {
        return firstLine;
    }

    public String getSecondLine() {
        return secondLine;
    }

    public int getProgress() {
        return progress;
    }

// --Commented out by Inspection START (06-11-2016 11:07 PM):
//    public void setProgress(int progress) {
//        this.progress = progress;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:07 PM)
}
