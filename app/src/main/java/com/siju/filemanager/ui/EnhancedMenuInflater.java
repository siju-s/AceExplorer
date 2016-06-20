package com.siju.filemanager.ui;

/**
 * Created by Siju on 20-06-2016.
 */

import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.view.menu.MenuItemImpl;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.siju.filemanager.R;


public class EnhancedMenuInflater {

    public static void inflate(MenuInflater inflater, Menu menu, boolean forceVisible) {
        inflater.inflate(R.menu.action_mode_bottom, menu);

        if (!forceVisible) {
            return;
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
