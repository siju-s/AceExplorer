package com.siju.acexplorer.filesystem.ui;

import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.view.menu.MenuItemImpl;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.FileConstants;


public class EnhancedMenuInflater {

    public static void inflate(MenuInflater inflater, Menu menu, int category) {
        inflater.inflate(R.menu.action_mode_bottom, menu);

        if (category != FileConstants.CATEGORY.FILES.getValue()) {
            menu.findItem(R.id.action_cut).setVisible(false);
            menu.findItem(R.id.action_copy).setVisible(false);
            if (category == FileConstants.CATEGORY.FAVORITES.getValue()) {
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
