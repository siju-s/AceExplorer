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

package com.siju.acexplorer.main.model;

import com.siju.acexplorer.main.model.groups.Category;

@SuppressWarnings("unused")
public class StorageItem {

    private final String                   firstLine;
    private final String                   secondLine;
    private       int                      progress;
    private       int                      icon;
    private       String                   path;
    private       Category                 category;
    private       StorageUtils.StorageType storageType;


    public StorageItem(String firstLine, String secondLine, int icon, String path, int progress,
                       Category category, StorageUtils.StorageType storageType) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.icon = icon;
        this.path = path;
        this.progress = progress;
        this.category = category;
        this.storageType = storageType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StorageItem)) {
            return false;
        }
        StorageItem o = (StorageItem) obj;
        return o.path.equals(this.path);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public StorageUtils.StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageUtils.StorageType storageType) {
        this.storageType = storageType;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
