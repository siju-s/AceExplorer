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

import com.siju.acexplorer.appmanager.AppLoader;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.FileListLoader;
import com.siju.acexplorer.model.StorageUtils;
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
//        Log.d("LoaderHelper", "createLoader: Category:"+category + " id:"+id);
        switch (category) {
            case AUDIO:
            case VIDEO:
            case IMAGE:
            case DOCS:
            case COMPRESSED:
            case FAVORITES:
            case PDF:
            case APPS:
            case LARGE_FILES:
            case GIF:
            case RECENT:
            case GENERIC_MUSIC:
            case ALL_TRACKS:
            case ALBUMS:
            case ARTISTS:
            case GENRES:
            case ALARMS:
            case NOTIFICATIONS:
            case RINGTONES:
            case PODCASTS:
                return new FileListLoader(fragment, null, category, false, FileListLoader.INVALID_ID);
            case DOWNLOADS:
                String path = StorageUtils.getDownloadsDirectory();
                return new FileListLoader(fragment, path, category, false, FileListLoader.INVALID_ID);
        }
        return null;
    }

    public Loader<ArrayList<FileInfo>> createLoader(String path, Category category, boolean isPicker, long id) {
//        Log.d(this.getClass().getSimpleName(), "createLoader: "+path);
        if (fragment == null) {
            return null;
        }
        if (category.equals(Category.APP_MANAGER)) {
            return new AppLoader(fragment.getContext());
        }
        return new FileListLoader(fragment, path, category, isPicker, id);
    }
}
