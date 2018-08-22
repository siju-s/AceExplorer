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

package com.siju.acexplorer.home.view;

import android.content.Intent;
import android.content.res.Configuration;

import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.main.model.groups.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 02 September,2017
 */
public interface HomeView {
    void onPause();

    void onResume();

    void onExit();

    void setListener(Listener listener);

    void onLibrariesFetched(List<HomeLibraryInfo> libraries);

    void onDataLoaded(int id, ArrayList<FileInfo> data);

    void handleActivityResult(int requestCode, int resultCode, Intent intent);

    void updateFavoritesCount(int size);

    void init();

    void removeFavorites(int size);

    void onPermissionGranted();

    void showDualMode();

    void hideDualPane();

    void setPremium();

    void onConfigChanged(Configuration newConfig);

    interface Listener {

        void getLibraries();

        void loadData(Category category);

        BillingStatus checkBillingStatus();

        void reloadLibraries(List<LibrarySortModel> selectedLibs);

        boolean getDualModeState();

        void saveLibs(List<LibrarySortModel> librarySortModels);
    }
}
