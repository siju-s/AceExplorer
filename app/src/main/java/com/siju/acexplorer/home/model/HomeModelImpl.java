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
import android.content.SharedPreferences;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.SharedPreferenceWrapper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.permission.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.FileConstants.PREFS_DUAL_PANE;
import static com.siju.acexplorer.model.FileConstants.PREFS_FIRST_RUN;
import static com.siju.acexplorer.model.groups.Category.ADD;
import static com.siju.acexplorer.model.groups.Category.AUDIO;
import static com.siju.acexplorer.model.groups.Category.DOCS;
import static com.siju.acexplorer.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.model.groups.Category.IMAGE;
import static com.siju.acexplorer.model.groups.Category.VIDEO;

/**
 * Created by Siju on 02 September,2017
 */
public class HomeModelImpl implements HomeModel {

    private final String TAG        = this.getClass().getSimpleName();
    private final int    COUNT_ZERO = 0;
    private Context context;
    private int     resourceIds[];
    private String labels[] = new String[]{"Images", "Audio", "Videos", "Docs",
            "Downloads", "Add"};
    private Category                categories[];
    private SharedPreferences       sharedPreferences;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private List<HomeLibraryInfo>   homeLibraryInfoArrayList;
    private HomeModel.Listener      listener;

    public HomeModelImpl() {
        this.context = AceApplication.getAppContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        homeLibraryInfoArrayList = new ArrayList<>();
        initConstants();
    }

    private void initConstants() {
        resourceIds = new int[]{R.drawable.ic_library_images, R.drawable.ic_library_music,
                R.drawable.ic_library_videos, R.drawable.ic_library_docs,
                R.drawable.ic_library_downloads, R.drawable.ic_library_add};
        categories = new Category[]{IMAGE, AUDIO, VIDEO,
                DOCS, DOWNLOADS, ADD};
    }

    @Override
    public void setListener(HomeModel.Listener listener) {
        this.listener = listener;
    }

    @Override
    public BillingStatus getBillingStatus() {
        return BillingManager.getInstance().getInAppBillingStatus();
    }


    @Override
    public void reloadLibraries(final List<LibrarySortModel> selectedLibs) {
        final List<HomeLibraryInfo> tempLibraryInfoArrayList = new ArrayList<>();
        tempLibraryInfoArrayList.addAll(homeLibraryInfoArrayList);
        homeLibraryInfoArrayList = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                for (int i = 0; i < selectedLibs.size(); i++) {

                    Category category = selectedLibs.get(i).getCategory();
                    int resourceId = getResourceIdForCategory(category);
                    String categoryName = selectedLibs.get(i).getLibraryName();
                    int count = 0;

                    for (int j = 0; j < tempLibraryInfoArrayList.size(); j++) {
                        if (tempLibraryInfoArrayList.get(j).getCategory().equals(selectedLibs.get
                                (i).getCategory())) {
                            count = tempLibraryInfoArrayList.get(j).getCount();
                            break;
                        }
                    }
                    homeLibraryInfoArrayList.add(new HomeLibraryInfo(category, categoryName,
                                                                     resourceId, count));
                }
                addPlusCategory();
                sharedPreferenceWrapper.saveLibrary(context, selectedLibs);
                listener.onLibrariesFetched(homeLibraryInfoArrayList);
            }
        }).start();

    }

    @Override
    public boolean getDualModeState() {
        return sharedPreferences.getBoolean(PREFS_DUAL_PANE, false);
    }


    @Override
    public void getLibraries() {
        Log.d(TAG, "getLibraries: ");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean isFirstRun = sharedPreferences.getBoolean(PREFS_FIRST_RUN, true);

                if (isFirstRun) {
                    addDefaultLibraries();
                } else {
                    addSavedLibraries();
                    addPlusCategory();
                }

                if (hasStoragePermission()) {
                    setupFavorites();
                }
                listener.onLibrariesFetched(homeLibraryInfoArrayList);
            }
        }).start();
    }

    private void addDefaultLibraries() {
        for (int i = 0; i < resourceIds.length; i++) {
            addToLibrary(new HomeLibraryInfo(categories[i], labels[i], resourceIds[i], COUNT_ZERO));
            LibrarySortModel model = new LibrarySortModel();
            model.setCategory(categories[i]);
            model.setLibraryName(labels[i]);
            model.setChecked(true);
            if (!model.getCategory().equals(ADD)) {
                sharedPreferenceWrapper.addLibrary(context, model);
            }
        }
        sharedPreferences.edit().putBoolean(PREFS_FIRST_RUN, false).apply();
    }


    private void addSavedLibraries() {
        ArrayList<LibrarySortModel> savedLibraries = sharedPreferenceWrapper.getLibraries(context);
        homeLibraryInfoArrayList.clear();
        for (int i = 0; i < savedLibraries.size(); i++) {
            Category category = savedLibraries.get(i).getCategory();
            int resourceId = getResourceIdForCategory(category);
            String name = savedLibraries.get(i).getLibraryName();//getCategoryName(context, category);
            addToLibrary(new HomeLibraryInfo(category, name, resourceId,
                                             COUNT_ZERO));
        }
    }

    private int getResourceIdForCategory(Category categoryId) {
        switch (categoryId) {
            case AUDIO:
                return R.drawable.ic_library_music;
            case VIDEO:
                return R.drawable.ic_library_videos;
            case IMAGE:
                return R.drawable.ic_library_images;
            case DOCS:
                return R.drawable.ic_library_docs;
            case DOWNLOADS:
                return R.drawable.ic_library_downloads;
            case ADD:
                return R.drawable.ic_library_add;
            case COMPRESSED:
                return R.drawable.ic_library_compressed;
            case FAVORITES:
                return R.drawable.ic_library_favorite;
            case PDF:
                return R.drawable.ic_library_pdf;
            case APPS:
                return R.drawable.ic_library_apk;
            case LARGE_FILES:
                return R.drawable.ic_library_large;
        }
        return 0;
    }

    private void addPlusCategory() {
        addToLibrary(new HomeLibraryInfo(ADD, labels[5], getResourceIdForCategory(ADD),
                                         COUNT_ZERO));
    }

    private void addToLibrary(HomeLibraryInfo homeLibraryInfo) {
        homeLibraryInfoArrayList.add(homeLibraryInfo);
    }


    private boolean hasStoragePermission() {
        return PermissionUtils.hasStoragePermission();
    }

    private void setupFavorites() {
        ArrayList<FavInfo> favorites = sharedPreferenceWrapper.getFavorites(context);
        if (favorites.size() > 0) {
            for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
                if (homeLibraryInfoArrayList.get(i).getCategory().equals(FAVORITES)) {
                    homeLibraryInfoArrayList.get(i).setCount(favorites.size());
                    break;
                }
            }
        }
    }


}
