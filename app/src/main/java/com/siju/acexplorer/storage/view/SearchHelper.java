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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.siju.acexplorer.R;
import com.siju.acexplorer.storage.model.task.SearchTask;

/**
 * Created by Siju on 04 September,2017
 */
public class SearchHelper implements View.OnClickListener, SearchView.OnQueryTextListener,
        SearchTask.SearchHelper,
        com.siju.acexplorer.storage.view.custom.SearchView.Listener {

    private com.siju.acexplorer.storage.view.custom.SearchView searchView;
    private Context context;

    public SearchHelper(MenuControls menuControls, Context context) {
        this.context = context;
    }

    private void init() {
        searchView = (com.siju.acexplorer.storage.view.custom.SearchView) actionBar.findViewById(R.id
                .search_view);
        searchView.setListener(this);
    }

    @Override
    public void onQueryChange(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            fileListAdapter.filter(charSequence.toString());
        }
    }

    @Override
    public void onQuerySubmit(CharSequence charSequence) {

    }

    @Override
    public void onSearchEnabled(boolean isExpanded) {
        Log.d(TAG, "onSearchEnabled: " + isExpanded);
        if (isExpanded) {
            toolbarTitle.setVisibility(View.GONE);
            searchView.setHint(getString(R.string.action_search));
            imgNavigationIcon.setImageResource(R.drawable.ic_up_arrow);
        }
        else {
            toolbarTitle.setVisibility(View.VISIBLE);
            imgNavigationIcon.setImageResource(R.drawable.ic_drawer);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
    }

    private boolean isSearchVisible() {
        return searchView.isExpanded();
    }

    boolean endSearch() {
        if (isSearchVisible()) {
            hideKeyboard();
            hideSearchView();
            removeSearchTask();
            return true;
        }
        return false;
    }

    public void hideSearchView() {
        searchView.enableSearch(false);
    }

    public void performVoiceSearch(String query) {
//        searchView.setQuery(query, false);
    }

//    private  SearchTask searchTask;

    @Override
    public boolean onQueryTextChange(String query) {

        fileListAdapter.filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
/*        if (!query.isEmpty()) {
            if (searchTask == null) {
                searchTask = new SearchTask(this,query,currentDir);

            }
            else {
                searchTask.execute(query);
            }
        }
        hideSearchView();*/
        return false;
    }

    @SuppressWarnings("EmptyMethod")
    public void removeSearchTask() {

     /*   if (searchTask != null) {
            searchTask.searchAsync.cancel(true);
        }*/
    }

    public void setSearchHintColor() {
        searchView.getInput().setTextColor(ContextCompat.getColor(context, R.color
                .white));
    }

    public boolean isExpanded() {
        return searchView.isExpanded();
    }

    public void disableSearch() {
        searchView.enableSearch(false);
    }
}
