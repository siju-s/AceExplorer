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

package com.siju.acexplorer.storage.view;

import android.content.Intent;
import android.os.Bundle;

import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.HomeLibraryInfo;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;
import com.siju.acexplorer.model.groups.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 04 September,2017
 */
public interface StoragesUi {

    void onPause();

    void onResume();

    void onExit();

    void setListener(Listener listener);

    void onLibrariesFetched(List<HomeLibraryInfo> libraries);

    void onDataLoaded(ArrayList<FileInfo> data);

    void handleActivityResult(int requestCode, int resultCode, Intent intent);

    void updateFavoritesCount(int size);

    void init();

    boolean onBackPress();

    void onViewDestroyed();



    interface Listener {

        void getLibraries();

        void loadData(String currentDir, Category category, boolean isPicker);

        BillingStatus checkBillingStatus();

        void reloadLibraries(List<LibrarySortModel> selectedLibs);

        Bundle getUserPrefs();
    }
}
