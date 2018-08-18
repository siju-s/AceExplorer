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

package com.siju.acexplorer.model.data;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;

import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;

import java.util.ArrayList;


public class MainLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    public static final int    INVALID_ID = -1;

    private MountUnmountReceiver mountUnmountReceiver;
    private ArrayList<FileInfo>  fileInfoList;

    private final Category category;
    private final String   currentDir;
    private final long     id;
    private final boolean  isRingtonePicker;
    private final boolean  showOnlyCount;


    public MainLoader(Context context, String path, Category category, boolean isRingtonePicker, long id, boolean showOnlyCount) {
        super(context);
        currentDir = path;
        this.category = category;
        this.id = id;
        this.showOnlyCount = showOnlyCount;
        this.isRingtonePicker = isRingtonePicker;
    }

    public MainLoader(Context context, String path, Category category, boolean showOnlyCount) {
        this(context, path, category, false, INVALID_ID, showOnlyCount);
    }


    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        }
        if (mountUnmountReceiver == null) {
            mountUnmountReceiver = new MountUnmountReceiver(this);
        }
        if (takeContentChanged() || fileInfoList == null) {
            forceLoad();
        }
    }


    @Override
    public void deliverResult(ArrayList<FileInfo> data) {
        if (isReset()) {
            return;
        }
        fileInfoList = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
        super.deliverResult(data);
    }

    @Override
    public ArrayList<FileInfo> loadInBackground() {
        return DataLoader.fetchDataByCategory(getContext(), category, currentDir, id, isRingtonePicker, showOnlyCount);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        fileInfoList = null;
        if (mountUnmountReceiver != null) {
            getContext().unregisterReceiver(mountUnmountReceiver);
            mountUnmountReceiver = null;
        }
    }


    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }


    private static class MountUnmountReceiver extends BroadcastReceiver {

        private static final String     SCHEME_FILE = "file";
        final                MainLoader loader;

        MountUnmountReceiver(MainLoader loader) {
            this.loader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme(SCHEME_FILE);
            this.loader.getContext().registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            loader.onContentChanged();
        }
    }
}
