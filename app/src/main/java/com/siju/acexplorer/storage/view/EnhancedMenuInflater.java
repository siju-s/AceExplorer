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

import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.view.menu.MenuItemImpl;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.groups.Category;

import static com.siju.acexplorer.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.model.groups.Category.FILES;


public class EnhancedMenuInflater {

    public static void inflate(MenuInflater inflater, Menu menu, Category category) {
        inflater.inflate(R.menu.action_mode_bottom, menu);

        if (!category.equals(FILES)) {
            menu.findItem(R.id.action_cut).setVisible(false);
            menu.findItem(R.id.action_copy).setVisible(false);
            if (category.equals(FAVORITES)) {
                menu.findItem(R.id.action_share).setVisible(false);
            }
        }

        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            // check if app:showAsAction = "ifRoom"
            if (((MenuItemImpl) item).requestsActionButton()) {
                item.setShowAsAction(SupportMenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
    }
}
