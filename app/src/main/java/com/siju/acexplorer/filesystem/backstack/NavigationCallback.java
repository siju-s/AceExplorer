package com.siju.acexplorer.filesystem.backstack;

import android.view.View;

/**
 * Created by SJ on 23-01-2017.
 */

public interface NavigationCallback {

    void addViewToNavigation(View view);
    void clearNavigation();
    void onHomeClicked();
    void onNavButtonClicked(String dir);
}
