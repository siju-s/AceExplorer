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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.billing.BillingHelper;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.HomeLibraryInfo;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.permission.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 02 September,2017
 */
public class StorageModelImpl implements StoragesModel {

    private final String TAG = this.getClass().getSimpleName();
    private final int COUNT_ZERO = 0;
    private Context context;
    private int resourceIds[];
    private String labels[];
    private Category categories[];
    private SharedPreferences sharedPreferences;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private List<HomeLibraryInfo> homeLibraryInfoArrayList;
    private Listener listener;


    public StorageModelImpl() {
        this.context = AceApplication.getAppContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        homeLibraryInfoArrayList = new ArrayList<>();
    }


    @Override
    public void getFiles() {

    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public BillingStatus getBillingStatus() {
        return BillingHelper.getInstance().getInAppBillingStatus();
    }

    @Override
    public void reloadLibraries(final List<LibrarySortModel> selectedLibs) {
        final List<HomeLibraryInfo> tempLibraryInfoArrayList = new ArrayList<>();
        tempLibraryInfoArrayList.addAll(homeLibraryInfoArrayList);
        homeLibraryInfoArrayList = new ArrayList<>();

    }

    @Override
    public Bundle getUserPrefs() {
        Bundle bundle = new Bundle();
        int gridCols = sharedPreferences.getInt(FileConstants.KEY_GRID_COLUMNS, 0);
        boolean isHomeScreenEnabled = sharedPreferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        int viewMode = sharedPreferenceWrapper.getViewMode(context);
        bundle.putInt(FileConstants.KEY_GRID_COLUMNS, gridCols);
        bundle.putBoolean(FileConstants.PREFS_HOMESCREEN, isHomeScreenEnabled);
        bundle.putInt(FileConstants.PREFS_VIEW_MODE, viewMode);
        return bundle;
    }


    private boolean hasStoragePermission() {
        return PermissionUtils.hasStoragePermission();
    }



}
