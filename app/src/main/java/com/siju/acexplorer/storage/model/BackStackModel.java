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

package com.siju.acexplorer.storage.model;

import com.siju.acexplorer.model.groups.Category;

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
