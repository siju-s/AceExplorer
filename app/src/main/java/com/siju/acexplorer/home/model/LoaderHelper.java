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

import android.content.Context;
import android.support.v4.content.Loader;

import com.siju.acexplorer.appmanager.AppLoader;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.StorageUtils;
import com.siju.acexplorer.model.data.MainLoader;
import com.siju.acexplorer.model.groups.Category;

import java.util.ArrayList;

/**
 * Created by Siju on 03 September,2017
 */
public class LoaderHelper {

    private Context context;

    public LoaderHelper(Context context) {
        this.context = context;
    }

    public Loader<ArrayList<FileInfo>> createLoader(Category category, boolean showOnlyCount) {
        String path = null;
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
                break;
            case DOWNLOADS:
                path = StorageUtils.getDownloadsDirectory();
                break;
        }
        return new MainLoader(context, path, category, showOnlyCount);
    }

    public Loader<ArrayList<FileInfo>> createLoader(String path, Category category, boolean isPicker, long id) {
        if (context == null) {
            return null;
        }
        if (Category.APP_MANAGER.equals(category)) {
            return new AppLoader(context);
        }
        return new MainLoader(context, path, category, isPicker, id, false);
    }
}
