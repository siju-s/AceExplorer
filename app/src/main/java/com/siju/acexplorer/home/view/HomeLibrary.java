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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.home.LibrarySortActivity;
import com.siju.acexplorer.home.model.HomeLibraryInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.StorageUtils;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.theme.Theme;

import java.util.List;

import static com.siju.acexplorer.model.groups.Category.ADD;
import static com.siju.acexplorer.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.model.groups.Category.FAVORITES;

/**
 * Created by Siju on 03 September,2017
 */
class HomeLibrary implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    static final int REQUEST_CODE = 1000;

    private final int MAX_LIMIT_ROUND_COUNT = 99999;
    private HomeUiView homeUiView;
    private TableLayout libraryContainer;
    private LinearLayout layoutLibrary;
    private Context context;
    private List<HomeLibraryInfo> homeLibraryInfoArrayList;
    private List<HomeLibraryInfo> tempLibraryInfoArrayList;
    private int spacing;
    private int gridColumns;
    private Activity activity;
    private Theme theme;
    private int currentOrientation;


    HomeLibrary(Activity activity, HomeUiView homeUiView, Theme theme) {
        this.activity = activity;
        this.homeUiView = homeUiView;
        this.context = homeUiView.getContext();
        init();
        setTheme(theme);
    }

    private void setTheme(Theme theme) {
        this.theme = theme;
        switch (theme) {
            case DARK:
                layoutLibrary.setBackgroundColor(ContextCompat.getColor(context, R.color
                        .dark_background));
                break;
            case LIGHT:
                layoutLibrary.setBackgroundColor(ContextCompat.getColor(context, R.color
                        .light_home_lib));
                break;
        }
    }

    private void init() {
        libraryContainer = homeUiView.findViewById(R.id.libraryContainer);
        layoutLibrary = homeUiView.findViewById(R.id.layoutLibrary);
        currentOrientation = context.getResources().getConfiguration().orientation;
        setGridColumns();
        homeUiView.getLibraries();

    }

    private void setGridColumns() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        int libWidth = context.getResources().getDimensionPixelSize(R.dimen.home_library_width) +
                2 * context.getResources().getDimensionPixelSize(R.dimen.drawer_item_margin) +
                context.getResources().getDimensionPixelSize(R.dimen.padding_5);

        gridColumns = context.getResources().getInteger(R.integer.homescreen_columns);//width / 
        // libWidth;//getResources()
        // .getInteger(R.integer.homescreen_columns);
//        gridColumns = Math.min(gridColumns,homeLibraryInfoArrayList.size());
        spacing = (width - gridColumns * libWidth) / gridColumns;
        Logger.log(TAG, "Grid columns=" + gridColumns + " width=" + width + " liub size=" +
                libWidth + "space=" +
                spacing);
    }

    private void inflateLibraryItem() {
        libraryContainer.removeAllViews();
        TableRow tableRow = new TableRow(context);
        int pos = 0;
        Log.d(TAG, "inflateLibraryItem: " + homeLibraryInfoArrayList.size());
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {

            RelativeLayout libraryItemContainer = (RelativeLayout) View.inflate(context, R.layout
                    .library_item, null);
            ImageView imageLibrary = libraryItemContainer.findViewById(R.id.imageLibrary);
            TextView textLibraryName = libraryItemContainer.findViewById(R.id.textLibrary);
            TextView textCount = libraryItemContainer.findViewById(R.id.textCount);
            imageLibrary.setImageResource(homeLibraryInfoArrayList.get(i).getResourceId());
            textLibraryName.setText(homeLibraryInfoArrayList.get(i).getCategoryName());
            if (homeLibraryInfoArrayList.get(i).getCategory().equals(ADD)) {
                textCount.setVisibility(View.GONE);
            } else {
                textCount.setVisibility(View.VISIBLE);
            }

            libraryItemContainer.setPadding(0, 0, spacing, 0);
            textCount.setText(roundOffCount(homeLibraryInfoArrayList.get(i).getCount()));
            int j = i + 1;
            if (j % gridColumns == 0) {
                tableRow.addView(libraryItemContainer);
                libraryContainer.addView(tableRow);
                tableRow = new TableRow(context);
                pos = 0;
            } else {
                tableRow.addView(libraryItemContainer);
                pos++;
            }
            libraryItemContainer.setOnClickListener(this);
            libraryItemContainer.setTag(homeLibraryInfoArrayList.get(i).getCategory());
            changeColor(imageLibrary, homeLibraryInfoArrayList.get(i).getCategory());
        }
        if (pos != 0) {
            libraryContainer.addView(tableRow);
        }
        Log.d(TAG, "inflateLibraryItem Completed: " + homeLibraryInfoArrayList.size());
    }

    private void changeColor(View itemView, Category category) {
        if (theme == Theme.DARK) {
            switch (category) {
                case AUDIO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .audio_bg_dark));
                    break;
                case VIDEO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .video_bg_dark));
                    break;
                case IMAGE:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .image_bg_dark));
                    break;
                case DOCS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .docs_bg_dark));
                    break;
                case DOWNLOADS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .downloads_bg_dark));
                    break;
                case ADD:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .add_bg_dark));
                    break;
                case COMPRESSED:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .compressed_bg_dark));
                    break;
                case FAVORITES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .fav_bg_dark));
                    break;
                case PDF:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .pdf_bg_dark));
                    break;
                case APPS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .apps_bg_dark));
                    break;
                case LARGE_FILES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .large_files_bg_dark));
                    break;

                default:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .colorPrimary));

            }
        } else {
            switch (category) {
                case AUDIO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .audio_bg));
                    break;
                case VIDEO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .video_bg));
                    break;
                case IMAGE:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .image_bg));
                    break;
                case DOCS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .docs_bg));
                    break;
                case DOWNLOADS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .downloads_bg));
                    break;
                case ADD:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .add_bg));
                    break;
                case COMPRESSED:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .compressed_bg));
                    break;
                case FAVORITES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .fav_bg));
                    break;
                case PDF:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .pdf_bg));
                    break;
                case APPS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .apps_bg));
                    break;
                case LARGE_FILES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .large_files_bg));
                    break;

                default:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                            (context, R.color
                                    .colorPrimary));
            }
        }
    }


    private String roundOffCount(int count) {
        String roundedCount;
        if (count > MAX_LIMIT_ROUND_COUNT) {
            roundedCount = MAX_LIMIT_ROUND_COUNT + "+";
        } else {
            roundedCount = "" + count;
        }
        return roundedCount;
    }

    void setLibraries(List<HomeLibraryInfo> libraries) {
        this.homeLibraryInfoArrayList = libraries;
        Log.d(TAG, "setLibraries: " + libraries.size());
        inflateLibraryItem();
    }

    private void updateCount(int index, int count) {

        TableRow tableRow = (TableRow) libraryContainer.getChildAt((index / gridColumns));
        int childIndex;
        if (index < gridColumns) {
            childIndex = index;
        } else {
            childIndex = (index % gridColumns);
        }
        RelativeLayout container = (RelativeLayout) tableRow.getChildAt(childIndex);
        TextView textCount = container.findViewById(R.id.textCount);
        textCount.setText(roundOffCount(count));

    }


    public void updateFavoritesCount(int count) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            if (isFavoritesCategory(homeLibraryInfoArrayList.get(i).getCategory())) {
                int count1 = homeLibraryInfoArrayList.get(i).getCount();
                homeLibraryInfoArrayList.get(i).setCount(count1 + count);
                updateCount(i, count1 + count);
                break;
            }
        }
    }

    public void removeFavorites(int count) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            if (isFavoritesCategory(homeLibraryInfoArrayList.get(i).getCategory())) {
                int count1 = homeLibraryInfoArrayList.get(i).getCount();
                homeLibraryInfoArrayList.get(i).setCount(count1 - count);
                updateCount(i, count1 - count);
                break;
            }
        }
    }

    void onDataLoaded(int id, List<FileInfo> data) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            int categoryId = homeLibraryInfoArrayList.get(i).getCategory().getValue();
            Log.d(TAG, "onDataLoaded: "+homeLibraryInfoArrayList.get(i).getCategory());
            if (id == categoryId) {
                int count;
                if (id == DOWNLOADS.getValue()) {
                    count = data.size();
                } else {
                    count = data.get(0).getCount();
                }
                homeLibraryInfoArrayList.get(i).setCount(count);
                updateCount(i, count);
            }
        }
    }


    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof Category) {

            Category category = (Category) tag;
            if (isAddCategory(category)) {
                Intent intent = new Intent(context, LibrarySortActivity.class);
                homeUiView.getFragment().startActivityForResult(intent, REQUEST_CODE);
            } else {
                String path = null;
                if (category.equals(DOWNLOADS)) {
                    path = StorageUtils.getDownloadsDirectory();
                }
                homeUiView.loadFileList(path, category);
            }
        }
    }


    private boolean isAddCategory(Category category) {
        return category.equals(ADD);
    }

    private boolean isFavoritesCategory(Category category) {
        return category.equals(FAVORITES);
    }

    void onOrientationChanged(int orientation) {
        if (currentOrientation != orientation) {
            currentOrientation = orientation;
            setGridColumns();
            inflateLibraryItem();
        }
    }
}
