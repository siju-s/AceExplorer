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

import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.util.Log;

import com.siju.acexplorer.filesystem.FileListLoader;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.storage.StorageUtils;
import com.siju.acexplorer.model.groups.Category;

import java.util.ArrayList;

/**
 * Created by Siju on 03 September,2017
 */
public class LoaderHelper {

    private final Fragment fragment;

    public LoaderHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    public Loader<ArrayList<FileInfo>> createLoader(Category category, int id) {
        Log.d("LoaderHelper", "createLoader: Category:"+category + " id:"+id);
        switch (id) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 7:
            case 9:
            case 10:
            case 11:
                return new FileListLoader(fragment, null, category, false);
            case 5:
                String path = StorageUtils.getDownloadsDirectory();
                return new FileListLoader(fragment, path, category, false);
        }
        return null;
    }

    public Loader<ArrayList<FileInfo>> createLoader(String path, Category category, boolean isPicker) {
        return new FileListLoader(fragment, path, category, isPicker);
    }
}
