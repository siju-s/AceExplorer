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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageButton;

import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.home.LibrarySortActivity;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.StorageUtils;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.view.custom.helper.SimpleItemTouchHelperCallback;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.ConfigurationHelper;

import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.groups.Category.ADD;
import static com.siju.acexplorer.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.model.groups.Category.FAVORITES;

/**
 * Created by Siju on 03 September,2017
 */
class HomeLibrary {

    private final String TAG                  = this.getClass().getSimpleName();
    static final  int    LIBSORT_REQUEST_CODE = 1000;

    private HomeUiView     homeUiView;
    private RecyclerView   libraryList;
    private CardView       layoutLibrary;
    private Context        context;
    private HomeLibAdapter homeLibAdapter;
    private List<HomeLibraryInfo> homeLibraryInfoArrayList = new ArrayList<>();
    private Theme       theme;
    private int         currentOrientation;
    private boolean     isActionModeActive;
    private ImageButton deleteButton;


    HomeLibrary(HomeUiView homeUiView, Theme theme) {
        this.homeUiView = homeUiView;
        this.context = homeUiView.getContext();
        this.theme = theme;
        init();
    }

    private void setTheme(Theme theme) {
        switch (theme) {
            case DARK:
                layoutLibrary.setCardBackgroundColor(ContextCompat.getColor(context, R.color
                        .dark_home_card_bg));
                break;
            case LIGHT:
                layoutLibrary.setCardBackgroundColor(ContextCompat.getColor(context, R.color
                        .light_home_card_bg));
                break;
        }
    }

    private void init() {
        libraryList = homeUiView.findViewById(R.id.libraryContainer);
        layoutLibrary = homeUiView.findViewById(R.id.cardViewLibrary);
        setTheme(theme);
        deleteButton = homeUiView.findViewById(R.id.deleteButton);
        if (theme == Theme.LIGHT) {
            deleteButton.setImageResource(R.drawable.ic_delete_black);
        } else {
            deleteButton.setImageResource(R.drawable.ic_delete_white);
        }
        currentOrientation = homeUiView.getConfiguration().orientation;
        initList();
        setListeners();
        homeUiView.getLibraries();
    }

    private void initList() {
        libraryList.setItemAnimator(new DefaultItemAnimator());
        libraryList.setHasFixedSize(true);
        libraryList.setNestedScrollingEnabled(false);
        homeLibAdapter = new HomeLibAdapter(context, homeLibraryInfoArrayList, theme);
        homeLibAdapter.setHasStableIds(true);
        libraryList.getItemAnimator().setChangeDuration(0);
        setGridColumns(homeUiView.getConfiguration());

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(homeLibAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(libraryList);
        libraryList.setAdapter(homeLibAdapter);

    }

    private void setListeners() {
        homeLibAdapter.setOnItemClickListener(new HomeLibAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (isActionModeActive()) {
                    itemClickActionMode(position, false);
                } else {
                    handleItemClick(position);
                }

            }
        });

        homeLibAdapter.setOnItemLongClickListener(new HomeLibAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                itemClickActionMode(position, true);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray sparseBooleanArray = homeLibAdapter.getSelectedItemPositions();
                List<HomeLibraryInfo> fileInfoList = new ArrayList<>();
                for (int i = 0; i < sparseBooleanArray.size(); i++) {
                    fileInfoList.add(homeLibraryInfoArrayList.get(sparseBooleanArray.keyAt(i)));
                }
                homeLibraryInfoArrayList.removeAll(fileInfoList);
                homeLibAdapter.updateAdapter(homeLibraryInfoArrayList);
                endActionMode();
                reloadAftDelete();
            }
        });
    }

    private void reloadAftDelete() {
        List<LibrarySortModel> sortModelList = new ArrayList<>();
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            Category category = homeLibraryInfoArrayList.get(i).getCategory();
            if (category.equals(ADD)) {
                continue;
            }
            LibrarySortModel model = new LibrarySortModel();
            model.setChecked(true);
            model.setCategory(category);
            model.setLibraryName(homeLibraryInfoArrayList.get(i).getCategoryName());
            sortModelList.add(model);
        }
        homeUiView.reloadLibs(sortModelList);
    }

    private void handleItemClick(int position) {
        Category category = homeLibraryInfoArrayList.get(position).getCategory();
        if (isAddCategory(category)) {
            Analytics.getLogger().addLibClicked();
            Intent intent = new Intent(context, LibrarySortActivity.class);
            homeUiView.getFragment().startActivityForResult(intent, LIBSORT_REQUEST_CODE);
        } else {
            String path = null;
            if (category.equals(DOWNLOADS)) {
                path = StorageUtils.getDownloadsDirectory();
            }
            homeUiView.loadFileList(path, category);
        }
    }

    private void itemClickActionMode(int position, boolean isLongPress) {
        if (position == homeLibraryInfoArrayList.size() - 1) {
            return;
        }
        homeLibAdapter.toggleSelection(position, isLongPress);

        boolean hasCheckedItems = homeLibAdapter.getSelectedCount() > 0;
        if (hasCheckedItems && !isActionModeActive) {
            startActionMode();
        } else if (!hasCheckedItems && isActionModeActive) {
            endActionMode();
        }
    }

    private void startActionMode() {
        isActionModeActive = true;
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void endActionMode() {
        deleteButton.setVisibility(View.GONE);
        isActionModeActive = false;
        homeLibAdapter.clearSelection();
    }

    private boolean isActionModeActive() {
        return isActionModeActive;
    }


    private void setGridColumns(Configuration configuration) {
        int gridColumns = ConfigurationHelper.getHomeGridCols(configuration);//context.getResources().getInteger(R.integer.homescreen_columns);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, gridColumns);
        Log.d(TAG, "setGridColumns: " + gridColumns);
        libraryList.setLayoutManager(gridLayoutManager);
    }

    private void inflateLibraryItem() {
        Log.d(TAG, "inflateLibraryItem: " + homeLibraryInfoArrayList.size()); // TODO: 02/11/17
        // NPE here in dual mode when orientation change from LAND->PORT
        List<String> libNames = new ArrayList<>();
        for (HomeLibraryInfo libraryInfo : homeLibraryInfoArrayList) {
            libNames.add(libraryInfo.getCategoryName());
        }

        homeLibAdapter.updateAdapter(homeLibraryInfoArrayList);
        Analytics.getLogger().homeLibsDisplayed(homeLibraryInfoArrayList.size(), libNames);
    }


    void setLibraries(List<HomeLibraryInfo> libraries) {
        this.homeLibraryInfoArrayList = libraries;
        Log.d(TAG, "setLibraries: " + libraries.size());
        inflateLibraryItem();
    }

    List<HomeLibraryInfo> getLibraries() {
        return homeLibraryInfoArrayList;
    }


    void updateFavoritesCount(int count) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            if (isFavoritesCategory(homeLibraryInfoArrayList.get(i).getCategory())) {
                homeLibAdapter.updateFavCount(i, count);
                break;
            }
        }
    }

    void onDataLoaded(int id, List<FileInfo> data) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            int categoryId = homeLibraryInfoArrayList.get(i).getCategory().getValue();
            if (id == categoryId) {
                int count;
                if (id == DOWNLOADS.getValue()) {
                    count = data.size();
                } else {
                    count = data.get(0).getCount();
                }
                homeLibAdapter.updateCount(i, count);
            }
        }
    }


    private boolean isAddCategory(Category category) {
        return category.equals(ADD);
    }

    private boolean isFavoritesCategory(Category category) {
        return category.equals(FAVORITES);
    }

    void onOrientationChanged(Configuration configuration) {
        int orientation = configuration.orientation;
        Log.d(TAG, "onOrientationChanged: old:"+currentOrientation + " neew:"+orientation);
        if (currentOrientation != orientation) {
            currentOrientation = orientation;
            setGridColumns(configuration);
            inflateLibraryItem();
        }
    }

}
