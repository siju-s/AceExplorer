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

package com.siju.acexplorer.storage.model.backstack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.button.MaterialButton;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.groups.StoragesGroup;
import com.siju.acexplorer.storage.view.StoragesUiView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.siju.acexplorer.main.model.StorageUtils.getInternalStorage;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.checkIfAnyMusicCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.getCategoryName;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericImagesCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericVideosCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isRecentCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isRecentGenericCategory;

@SuppressLint("InflateParams")
public class NavigationInfo {
    private static final String               TAG        = "NavigationInfo";
    private static final String               SEPARATOR  = "/";
    private              Context              context;
    private              LinearLayout         navDirectory;
    private              HorizontalScrollView scrollNavigation;
    private              NavigationCallback   navigationCallback;
    private              ArrayList<String>    externalSDPaths;
    private              String               currentDir;
    private              String               initialDir = getInternalStorage();
    private              String               STORAGE_INTERNAL, STORAGE_ROOT, STORAGE_EXTERNAL;
    private              boolean              isCurrentDirRoot;


    public NavigationInfo(StoragesUiView storagesUiView, NavigationCallback navigationCallback) {
        this.context = storagesUiView.getContext();
        navDirectory = storagesUiView.findViewById(R.id.navButtons);
        scrollNavigation = storagesUiView.findViewById(R.id.scrollNavigation);
        scrollNavigation.setBackgroundColor(ContextCompat.getColor(context, R.color
                .colorPrimary));
        this.navigationCallback = navigationCallback;
        STORAGE_ROOT = context.getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = context.getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = context.getResources().getString(R.string.nav_menu_ext_storage);
        externalSDPaths = StoragesGroup.getInstance().getExternalSDList();
    }

    public void setInitialDir(String currentDir) {
        if (currentDir.contains(getInternalStorage())) {
            initialDir = getInternalStorage();
            isCurrentDirRoot = false;
        }
        else if (externalSDPaths.size() > 0) {
            for (String path : externalSDPaths) {
                if (currentDir.contains(path)) {
                    initialDir = path;
                    isCurrentDirRoot = false;
                    return;
                }
            }
            initialDir = File.separator;
        }
        else {
            initialDir = File.separator;
        }
        Logger.log(TAG, "initializeStartingDirectory--startingdir=" + initialDir);

    }

//    private void checkIfFavIsRootDir() {
//
//        if (!currentDir.contains(getInternalStorage()) && !externalSDPaths.contains
//                (currentDir)) {
//            isCurrentDirRoot = true;
//            initialDir = File.separator;
//        }
//    }

    public void addHomeNavButton(boolean isHomeScreenEnabled, Category category) {

        clearNavigation();
        if (isHomeScreenEnabled) {
            MaterialButton imageButton = (MaterialButton) LayoutInflater.from(context).inflate(R.layout.material_button_icon, null);
            imageButton.setIconResource(R.drawable.ic_home_white_48);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Analytics.getLogger().navBarClicked(true);
                    navigationCallback.onHomeClicked();
                }
            });
            addViewToNavigation(imageButton);
            addArrowView();

            addTitleText(category);
        }
        else {
            addTitleText(category);
        }
    }

    private void addArrowView() {
        MaterialButton navArrow = (MaterialButton) LayoutInflater.from(context).inflate(R.layout.navigation_arrow, null);
        addViewToNavigation(navArrow);
    }

    private void addTitleText(final Category category) {
        if (category.equals(Category.GENERIC_MUSIC) || category.equals(Category.GENERIC_VIDEOS) ||
                category.equals(Category.GENERIC_IMAGES) || Category.RECENT.equals(category)) {
            addLibSpecificTitleText(category, null);
            return;
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        if (!category.equals(Category.FILES) && !category.equals(Category.DOWNLOADS)) {
            String title = getCategoryName(context, category).toUpperCase(Locale.getDefault());
            MaterialButton button = (MaterialButton) LayoutInflater.from(context).inflate(R.layout.material_button, null);
            button.setText(title);
            addViewToNavigation(button);
        }
    }

    private void addLibSpecificTitleText(final Category category, final String bucketName) {
        String title;
        if (bucketName == null) {
            title = getCategoryName(context, category).toUpperCase(Locale.getDefault());
        }
        else {
            title = bucketName.toUpperCase(Locale.getDefault());
        }
        MaterialButton button = (MaterialButton) LayoutInflater.from(context).inflate(R.layout.material_button, null);
        button.setText(title);

        addViewToNavigation(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.log(TAG, "nav button onclick--bucket=" + bucketName + " category:" + category);
                navigationCallback.onNavButtonClicked(category, bucketName);
            }
        });
        scrollNavigation();
    }


    public void addLibSpecificNavButtons(boolean isHomeScreenEnabled, Category category, String bucketName) {

        if (checkIfAnyMusicCategory(category)) {
            addHomeNavButton(isHomeScreenEnabled, Category.GENERIC_MUSIC);
            switch (category) {
                case ALBUM_DETAIL:
                case ALBUMS:
                    addArrowView();
                    addLibSpecificTitleText(Category.ALBUMS, null);
                    if (bucketName != null) {
                        addArrowView();
                        addLibSpecificTitleText(category, bucketName);
                    }
                    break;
                case ARTIST_DETAIL:
                case ARTISTS:
                    addArrowView();
                    addLibSpecificTitleText(Category.ARTISTS, null);
                    if (bucketName != null) {
                        addArrowView();
                        addLibSpecificTitleText(category, bucketName);
                    }
                    break;
                case GENRE_DETAIL:
                case GENRES:
                    addArrowView();
                    addLibSpecificTitleText(Category.GENRES, null);
                    if (bucketName != null) {
                        addArrowView();
                        addLibSpecificTitleText(category, bucketName);
                    }
                    break;
                case ALARMS:
                case NOTIFICATIONS:
                case RINGTONES:
                case ALL_TRACKS:
                case PODCASTS:
                    addArrowView();
                    addLibSpecificTitleText(category, null);
                    break;

            }
        }
        else if (isRecentCategory(category) || isRecentGenericCategory(category)) {
            addHomeNavButton(isHomeScreenEnabled, Category.RECENT);
            switch (category) {
                case RECENT_IMAGES:
                case RECENT_VIDEOS:
                case RECENT_AUDIO:
                case RECENT_DOCS:
                case RECENT_APPS:
                    addArrowView();
                    addLibSpecificTitleText(category, null);
                    break;
            }

        }
        else if (category.equals(Category.FOLDER_VIDEOS) || isGenericVideosCategory(category)) {
            addHomeNavButton(isHomeScreenEnabled, Category.GENERIC_VIDEOS);
            if (category.equals(Category.FOLDER_VIDEOS)) {
                addArrowView();
                addLibSpecificTitleText(category, bucketName);
            }
        }
        else if (category.equals(Category.FOLDER_IMAGES) || isGenericImagesCategory(category)) {
            addHomeNavButton(isHomeScreenEnabled, Category.GENERIC_IMAGES);
            if (category.equals(Category.FOLDER_IMAGES)) {
                addArrowView();
                addLibSpecificTitleText(category, bucketName);
            }
        }

    }


    public void setNavDirectory(String path, boolean isHomeScreenEnabled, Category category) {
        String[] parts;
        parts = path.split(SEPARATOR);

        clearNavigation();
        currentDir = path;
        String dir;
        addHomeNavButton(isHomeScreenEnabled, category);
        // If root dir , parts will be 0
        if (parts.length == 0) {

            isCurrentDirRoot = true;
            initialDir = File.separator;
            setNavDir(File.separator, File.separator); // Add Root button
        }
        else {
            int count = 0;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                dir = stringBuilder.append(File.separator).append(parts[i]).toString();

                if (!dir.contains(initialDir)) {
                    continue;
                }
                /*Count check so that ROOT is added only once in Navigation
                  Handles the scenario :
                  1. When Fav item is a root child and if we click on any folder in that fav item
                     multiple ROOT blocks are not added to Navigation view*/
                if (isCurrentDirRoot && count == 0) {
                    setNavDir(File.separator, File.separator);
                }

                count++;
                setNavDir(dir, parts[i]);
            }
        }

    }

    private void setNavDir(String dir, String parts) {

        if (getInternalStorage().equals(dir)) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_INTERNAL, dir);
        }
        else if (File.separator.equals(dir)) {
            createNavButton(STORAGE_ROOT, dir);
        }
        else if (externalSDPaths.contains(dir)) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_EXTERNAL, dir);
        }
        else {
            addArrowView();
            createNavButton(parts, dir);
            scrollNavigation();

        }
    }

    private void createNavButton(String text, final String dir) {

        if (text.equals(STORAGE_INTERNAL) || text.equals(STORAGE_EXTERNAL) ||
                text.equals(STORAGE_ROOT)) {
            MaterialButton button = (MaterialButton) LayoutInflater.from(context).inflate(R.layout.material_button_icon, null);
            if (text.equals(STORAGE_INTERNAL)) {
                button.setIconResource(R.drawable.ic_storage_white_48);
            }
            else if (text.equals(STORAGE_EXTERNAL)) {
                button.setIconResource(R.drawable.ic_ext_sd_white_48);
            }
            else {
                button.setIconResource(R.drawable.ic_root_white_48);
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dir != null) {
                        navButtonOnClick(dir);
                    }
                }
            });
            addViewToNavigation(button);
        }
        else {
            MaterialButton button = (MaterialButton) LayoutInflater.from(context).inflate(R.layout.material_button, null);
            button.setText(text);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Logger.log(TAG, "nav button onclick--dir=" + dir);
                    if (dir != null) {
                        navButtonOnClick(dir);
                    }
                }
            });
            addViewToNavigation(button);
        }
    }

    private void navButtonOnClick(final String dir) {
        Logger.log(TAG, "Dir=" + dir + " currentDir=" + currentDir);
        if (!currentDir.equals(dir)) {
            Analytics.getLogger().navBarClicked(false);
            navigationCallback.onNavButtonClicked(dir);

        }
    }


    public void removeHomeFromNavPath() {
        Logger.log(TAG, "Nav directory count=" + navDirectory.getChildCount());

        for (int i = 0; i < Math.min(navDirectory.getChildCount(), 2); i++) {
            navDirectory.removeViewAt(0);
        }
    }

    private void addViewToNavigation(View view) {
        navDirectory.addView(view);
    }

    private void clearNavigation() {
        navDirectory.removeAllViews();
    }

    public void showNavigationView() {
        scrollNavigation.setVisibility(View.VISIBLE);
    }

    public void hideNavigationView() {
        scrollNavigation.setVisibility(View.GONE);
    }

    private void scrollNavigation() {
        scrollNavigation.postDelayed(new Runnable() {
            public void run() {
                scrollNavigation.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);
    }


}
