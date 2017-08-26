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

package com.siju.acexplorer.filesystem.backstack;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.model.BackStackModel;

import java.util.ArrayList;


public class BackStackInfo {
    private final ArrayList<BackStackModel> backStack = new ArrayList<>();
    private static final String TAG = "BackStackInfo";

    private Category category;

    public void addToBackStack(String path, Category category) {
        backStack.add(new BackStackModel(path, category));
        Logger.log(TAG, "Back stack--size=" + backStack.size() + " Path=" + path + "Category=" + category);
    }

    public void clearBackStack() {
         backStack.clear();
    }

    public  ArrayList<BackStackModel> getBackStack() {
        return backStack;
    }

    public void removeEntryAtIndex(int index) {
        backStack.remove(index);
    }

    public String getDirAtPosition(int index) {
        return backStack.get(index).getFilePath();
    }

    public Category getCategoryAtPosition(int index) {
        return backStack.get(index).getCategory();
    }



}
