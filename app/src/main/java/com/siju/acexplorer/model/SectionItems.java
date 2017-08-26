/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
