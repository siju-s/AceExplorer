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

package com.siju.acexplorer.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.base.view.BaseActivity;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.SharedPreferenceWrapper;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.storage.view.custom.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;

import static com.siju.acexplorer.main.model.groups.Category.APPS;
import static com.siju.acexplorer.main.model.groups.Category.AUDIO;
import static com.siju.acexplorer.main.model.groups.Category.COMPRESSED;
import static com.siju.acexplorer.main.model.groups.Category.DOCS;
import static com.siju.acexplorer.main.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.main.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.main.model.groups.Category.IMAGE;
import static com.siju.acexplorer.main.model.groups.Category.LARGE_FILES;
import static com.siju.acexplorer.main.model.groups.Category.PDF;
import static com.siju.acexplorer.main.model.groups.Category.RECENT;
import static com.siju.acexplorer.main.model.groups.Category.VIDEO;

public class LibrarySortActivity extends BaseActivity implements OnStartDragListener {
    private ItemTouchHelper         itemTouchHelper;
    private SharedPreferenceWrapper sharedPreferenceWrapper;

    private       ArrayList<LibrarySortModel> savedLibraries = new ArrayList<>();
    private final ArrayList<LibrarySortModel> totalLibraries = new ArrayList<>();

    private int[]      resourceIds;
    private String[]   mLabels;
    private Category[] categories;


    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_sort);

        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        initConstants();
        initializeLibraries();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView libSortList = findViewById(R.id.libSortList);
        LibrarySortAdapter mSortAdapter = new LibrarySortAdapter(this, totalLibraries);

        libSortList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setAutoMeasureEnabled(false);
        libSortList.setLayoutManager(llm);
        libSortList.setAdapter(mSortAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mSortAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(libSortList);
    }

    private void initConstants() {
        resourceIds = new int[]{R.drawable.ic_library_images, R.drawable.ic_library_music,
                R.drawable.ic_library_videos, R.drawable.ic_library_docs,
                R.drawable.ic_library_downloads,
                R.drawable.ic_library_compressed, R.drawable.ic_library_favorite,
                R.drawable.ic_library_pdf, R.drawable.ic_library_apk, R.drawable.ic_library_large,
        R.drawable.ic_library_gif,
        R.drawable.ic_library_recents};
        // No Add Label to be shown
        mLabels = new String[]{getString(R.string.nav_menu_image), getString(R.string.nav_menu_music),
                getString(R.string.nav_menu_video), getString(R.string.home_docs),
                getString(R.string.downloads), getString(R.string.compressed),
                getString(R.string.nav_header_favourites), getString(R.string.pdf),
                                  getString(R.string.apk), getString(R.string.library_large),
        getString(R.string.library_gif),
                getString(R.string.library_recent)};
        categories = new Category[]{IMAGE,
                AUDIO,
                VIDEO,
                DOCS,
                DOWNLOADS,
                COMPRESSED,
                FAVORITES,
                PDF,
                APPS,
                LARGE_FILES,
        RECENT};
    }


    private void initializeLibraries() {
        savedLibraries = sharedPreferenceWrapper.getLibraries(this);
        if (savedLibraries != null) {
            for (int j = 0; j < savedLibraries.size(); j++) {
                int categoryId = savedLibraries.get(j).getCategoryId();
                totalLibraries.add(new LibrarySortModel(categoryId));
            }
        }


        for (int i = 0; i < resourceIds.length; i++) {
            LibrarySortModel model = new LibrarySortModel(categories[i].getValue());
            if (!totalLibraries.contains(model)) {
                model.setChecked(false);
                totalLibraries.add(model);
            }
        }

    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.library_sort, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ok:
                savedLibraries = new ArrayList<>();
                for (int i = 0; i < totalLibraries.size(); i++) {
                    if (totalLibraries.get(i).isChecked()) {
                        savedLibraries.add(totalLibraries.get(i));
                    }
                }
                Log.d(this.getClass().getSimpleName(), "savedLibraries: Aft"+savedLibraries.size());

                Intent dataIntent = new Intent();
                dataIntent.putParcelableArrayListExtra(FileConstants.KEY_LIB_SORTLIST,
                                                       savedLibraries);
                setResult(RESULT_OK, dataIntent);
                finish();
                break;

            case R.id.action_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
