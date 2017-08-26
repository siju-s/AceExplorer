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

package com.siju.acexplorer.filesystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.helper.SimpleItemTouchHelperCallback;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;

import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.groups.Category.APPS;
import static com.siju.acexplorer.filesystem.groups.Category.AUDIO;
import static com.siju.acexplorer.filesystem.groups.Category.COMPRESSED;
import static com.siju.acexplorer.filesystem.groups.Category.DOCS;
import static com.siju.acexplorer.filesystem.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.filesystem.groups.Category.FAVORITES;
import static com.siju.acexplorer.filesystem.groups.Category.IMAGE;
import static com.siju.acexplorer.filesystem.groups.Category.LARGE_FILES;
import static com.siju.acexplorer.filesystem.groups.Category.PDF;
import static com.siju.acexplorer.filesystem.groups.Category.VIDEO;

public class LibrarySortActivity extends BaseActivity implements OnStartDragListener {
    private ItemTouchHelper mItemTouchHelper;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private ArrayList<LibrarySortModel> savedLibraries = new ArrayList<>();
    private final ArrayList<LibrarySortModel> totalLibraries = new ArrayList<>();
    private int mResourceIds[];
    private String mLabels[];
    private Category categories[];


    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_sort);

        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        initConstants();
        initializeLibraries();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.nav_header_collections));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView mRecyclerViewLibrarySort = (RecyclerView) findViewById(R.id.recyclerViewLibrarySort);
        LibrarySortAdapter mSortAdapter = new LibrarySortAdapter(this, totalLibraries);

        mRecyclerViewLibrarySort.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setAutoMeasureEnabled(false);
        mRecyclerViewLibrarySort.setLayoutManager(llm);
        mRecyclerViewLibrarySort.setAdapter(mSortAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mSortAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerViewLibrarySort);
    }

    private void initConstants() {
        mResourceIds = new int[]{R.drawable.ic_library_images, R.drawable.ic_library_music,
                R.drawable.ic_library_videos, R.drawable.ic_library_docs,
                R.drawable.ic_library_downloads,
                R.drawable.ic_library_compressed, R.drawable.ic_library_favorite,
                R.drawable.ic_library_pdf, R.drawable.ic_library_apk, R.drawable.ic_library_large};
        // No Add Label to be shown
        mLabels = new String[]{getString(R.string
                .nav_menu_image), getString(R.string
                .nav_menu_music), getString(R.string
                .nav_menu_video), getString(R.string
                .home_docs), getString(R.string
                .downloads), getString(R.string
                .compressed), getString(R.string
                .nav_header_favourites), getString(R.string
                .pdf), getString(R.string
                .apk), getString(R.string
                .library_large)};
        categories = new Category[]{IMAGE,
                AUDIO,
                VIDEO,
                DOCS,
                DOWNLOADS,
                COMPRESSED,
                FAVORITES,
                PDF,
                APPS,
                LARGE_FILES};
    }


    private void initializeLibraries() {
        savedLibraries = sharedPreferenceWrapper.getLibraries(this);
        if (savedLibraries != null) {
            for (int j = 0; j < savedLibraries.size(); j++) {
                totalLibraries.add(new LibrarySortModel(savedLibraries.get(j).getCategory(),
                        savedLibraries.get(j).getLibraryName()));
            }
        }

        for (int i = 0; i < mResourceIds.length; i++) {
            LibrarySortModel model = new LibrarySortModel(categories[i], mLabels[i]);
            if (!totalLibraries.contains(model)) {
                model.setChecked(false);
                totalLibraries.add(model);
            }
        }
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
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
