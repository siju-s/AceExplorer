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

package com.siju.acexplorer.home.model;

import com.siju.acexplorer.model.groups.Category;

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