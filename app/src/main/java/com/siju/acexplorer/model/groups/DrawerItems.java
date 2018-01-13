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

package com.siju.acexplorer.model.groups;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.preference.PreferenceManager;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.model.SharedPreferenceWrapper;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.model.FileConstants.PREFS_FIRST_RUN;
import static com.siju.acexplorer.model.StorageUtils.getDownloadsDirectory;
import static com.siju.acexplorer.model.groups.DrawerGroup.FAVORITES;
import static com.siju.acexplorer.model.groups.DrawerGroup.LIBRARY;
import static com.siju.acexplorer.model.groups.DrawerGroup.STORAGE;
import static com.siju.acexplorer.model.groups.DrawerGroup.TOOLS;

public class DrawerItems {


    private Context context;
    private final ArrayList<SectionGroup> totalGroupData          = new ArrayList<>();
    private final ArrayList<SectionItems> favouritesGroupChild    = new ArrayList<>();
    private       SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();


    public DrawerItems() {
        this.context = AceApplication.getAppContext();
    }

    public void getTotalGroupData(final DrawerItemsCallback itemsCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                initializeStorageGroup();
                initializeFavouritesGroup();
                initializeLibraryGroup();
                initializeToolsGroup();
                itemsCallback.onTotalGroupDataFetched(totalGroupData);
            }
        }).start();

    }

    private void initializeStorageGroup() {
        populateDrawerItems(new SectionGroup(StoragesGroup.getInstance().
                getStorageGroupData(), STORAGE));
    }

    private void initializeFavouritesGroup() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (context);
        boolean isFirstRun = sharedPreferences.getBoolean(PREFS_FIRST_RUN, true);
        if (isFirstRun) {
            addDefaultFavorites();
        } else {
            addSavedFavorites();
        }
        populateDrawerItems(new SectionGroup(favouritesGroupChild, FAVORITES));
    }

    private void addDefaultFavorites() {

        String path = getDownloadsDirectory();
        favouritesGroupChild.add(new SectionItems(null, path, R.drawable.ic_download,
                                                  getDownloadsDirectory(), 0, Category.FAVORITES, null));
        FavInfo favInfo = new FavInfo();
        favInfo.setFilePath(path);
        sharedPreferenceWrapper.addFavorite(context, favInfo);
    }

    private void addSavedFavorites() {
        ArrayList<FavInfo> savedFavourites = sharedPreferenceWrapper.getFavorites(context);

        if (savedFavourites != null && savedFavourites.size() > 0) {
            for (int i = 0; i < savedFavourites.size(); i++) {
                String savedPath = savedFavourites.get(i).getFilePath();
                if (!new File(savedPath).exists()) {
                    continue;
                }
                favouritesGroupChild.add(new SectionItems(null,
                                                          savedPath, R.drawable
                                                                  .ic_fav_folder,
                                                          savedPath, 0, Category.FAVORITES, null));
            }
        }
    }

    private void populateDrawerItems(SectionGroup group) {
        totalGroupData.add(group);
    }

    private void initializeLibraryGroup() {

        populateDrawerItems(new SectionGroup(addLibraryItems(), LIBRARY));
    }

    private void initializeToolsGroup() {

        populateDrawerItems(new SectionGroup(addToolGroupItems(), TOOLS));
    }

    private ArrayList<SectionItems> addToolGroupItems() {
        ArrayList<SectionItems> toolsGroupChild = new ArrayList<>();
        toolsGroupChild.add(new SectionItems(null, null, R.drawable.ic_app_manager,
                                               null, 0, Category.APP_MANAGER, null));
        return toolsGroupChild;
    }

    private ArrayList<SectionItems> addLibraryItems() {
        ArrayList<SectionItems> libraryGroupChild = new ArrayList<>();
        libraryGroupChild.add(new SectionItems(null, null, R.drawable.ic_music_white,
                                               null, 0, Category.AUDIO, null));
        libraryGroupChild.add(new SectionItems(null, null, R.drawable.ic_video_white,
                                               null, 0, Category.VIDEO, null));
        libraryGroupChild.add(new SectionItems(null, null, R.drawable.ic_photos_white,
                                               null, 0, Category.IMAGE, null));
        libraryGroupChild.add(new SectionItems(null, null, R.drawable.ic_file_white,
                                               null, 0, Category.DOCS, null));
        return libraryGroupChild;
    }

    public interface DrawerItemsCallback {

        void onTotalGroupDataFetched(ArrayList<SectionGroup> totalData);
    }


}
