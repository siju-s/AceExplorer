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

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FavInfo;
import com.siju.acexplorer.main.model.SharedPreferenceWrapper;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.permission.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentActivity;

import static com.siju.acexplorer.main.model.FileConstants.PREFS_ADD_RECENT;
import static com.siju.acexplorer.main.model.FileConstants.PREFS_DUAL_PANE;
import static com.siju.acexplorer.main.model.FileConstants.PREFS_FIRST_RUN;
import static com.siju.acexplorer.main.model.groups.Category.AUDIO;
import static com.siju.acexplorer.main.model.groups.Category.DOCS;
import static com.siju.acexplorer.main.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.main.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.main.model.groups.Category.IMAGE;
import static com.siju.acexplorer.main.model.groups.Category.RECENT;
import static com.siju.acexplorer.main.model.groups.Category.VIDEO;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.getCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.getCategoryName;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.getResourceIdForCategory;

/**
 * Created by Siju on 02 September,2017
 */
public class HomeModelImpl implements HomeModel {

    private final String TAG        = this.getClass().getSimpleName();
    private static final int    COUNT_ZERO = 0;
    private static final String ADD = "Add";
    private Context context;
    private int[] resourceIds;
    private static final String[] labels = new String[]{"Images", "Audio", "Videos", "Docs",
            "Downloads", "Recent"};
    private Category[] categories;
    private SharedPreferences       sharedPreferences;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private List<HomeLibraryInfo>   homeLibraryInfoArrayList;
    private HomeModel.Listener      listener;
    // Used for fetching strings using activity context (since app context will not reflect new language strings if changed)
    private FragmentActivity activity;
    private BillingManager billingManager;

    public HomeModelImpl(BillingManager billingManager) {
        this.context = AceApplication.getAppContext();
        this.billingManager = billingManager;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        homeLibraryInfoArrayList = new ArrayList<>();
        initConstants();
    }

    private void initConstants() {
        resourceIds = new int[]{R.drawable.ic_library_images, R.drawable.ic_library_music,
                R.drawable.ic_library_videos, R.drawable.ic_library_docs,
                R.drawable.ic_library_downloads, R.drawable.ic_library_recents};
        categories = new Category[]{IMAGE, AUDIO, VIDEO,
                DOCS, DOWNLOADS, RECENT};
    }

    @Override
    public void setListener(HomeModel.Listener listener) {
        this.listener = listener;
    }

    @Override
    public BillingStatus getBillingStatus() {
        return billingManager.getInAppBillingStatus();
    }

    @Override
    public BillingManager getBillingManager() {
        return billingManager;
    }

    @Override
    public void reloadLibraries(final List<LibrarySortModel> selectedLibs) {
        final List<HomeLibraryInfo> tempLibraryInfoArrayList = new ArrayList<>(homeLibraryInfoArrayList);
        homeLibraryInfoArrayList = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                for (int i = 0; i < selectedLibs.size(); i++) {

                    Category category = getCategory(selectedLibs.get(i).getCategoryId());
                    int resourceId = getResourceIdForCategory(category);
                    String categoryName = getCategoryName(activity.getBaseContext(), category);
                    int count = 0;

                    for (int j = 0; j < tempLibraryInfoArrayList.size(); j++) {
                        if (tempLibraryInfoArrayList.get(j).getCategory().equals(category)) {
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
    public void saveLibs(List<LibrarySortModel> librarySortModels) {
        sharedPreferenceWrapper.saveLibrary(context, librarySortModels);
    }

    @Override
    public void setActivityContext(FragmentActivity activity) {
        this.activity = activity;
    }


    @Override
    public void getLibraries() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                boolean isFirstRun = sharedPreferences.getBoolean(PREFS_FIRST_RUN, true);

                if (isFirstRun) {
                    addDefaultLibraries();
                } else {
                    addSavedLibraries();
                    addRecentCategory();
                 }
                addPlusCategory();

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
            model.setCategoryId(categories[i].getValue());
            model.setChecked(true);
           sharedPreferenceWrapper.addLibrary(context, model);
        }
        sharedPreferences.edit().putBoolean(PREFS_FIRST_RUN, false).apply();
        sharedPreferences.edit().putBoolean(PREFS_ADD_RECENT, true).apply();
    }

    private void addRecentCategory() {
        if (sharedPreferences.getBoolean(PREFS_ADD_RECENT,false)) {
            return;
        }
        addToLibrary(new HomeLibraryInfo(categories[5], labels[5], resourceIds[5], COUNT_ZERO));
        LibrarySortModel model = new LibrarySortModel();
        model.setCategoryId(categories[5].getValue());
        model.setChecked(true);
        sharedPreferenceWrapper.addLibrary(context, model);
        sharedPreferences.edit().putBoolean(PREFS_ADD_RECENT, true).apply();
    }


    private void addSavedLibraries() {
        boolean hasOldPrefs = sharedPreferenceWrapper.removeOldPrefs(context);
        Logger.log(TAG, "has old prefs:"+hasOldPrefs);
        if (hasOldPrefs) {
            addDefaultLibraries();
            return;
        }
        ArrayList<LibrarySortModel> savedLibraries = sharedPreferenceWrapper.getLibraries(context);
        homeLibraryInfoArrayList.clear();
        for (int i = 0; i < savedLibraries.size(); i++) {
            Category category = getCategory(savedLibraries.get(i).getCategoryId());
            int resourceId = getResourceIdForCategory(category);
            String name = getCategoryName(activity.getBaseContext(), category);
            addToLibrary(new HomeLibraryInfo(category, name, resourceId,
                                             COUNT_ZERO));
        }
    }


    private void addPlusCategory() {
        addToLibrary(new HomeLibraryInfo(Category.ADD, ADD, getResourceIdForCategory(Category.ADD),
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
