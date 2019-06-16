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

package com.siju.acexplorer.main.view;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.siju.acexplorer.R;
import com.siju.acexplorer.ads.AdHelper;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.home.view.HomeScreenFragment;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.SectionGroup;
import com.siju.acexplorer.main.model.SharedPreferenceWrapper;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.helper.SdkHelper;
import com.siju.acexplorer.permission.PermissionHelper;
import com.siju.acexplorer.permission.PermissionResultCallback;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.premium.Premium;
import com.siju.acexplorer.premium.PremiumUtils;
import com.siju.acexplorer.storage.view.BaseFileListFragment;
import com.siju.acexplorer.storage.view.DualPaneFragment;
import com.siju.acexplorer.storage.view.FileListFragment;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.LocaleHelper;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.siju.acexplorer.main.model.StorageUtils.getInternalStorage;
import static com.siju.acexplorer.main.model.groups.Category.FILES;
import static com.siju.acexplorer.main.model.groups.Category.GENERIC_IMAGES;
import static com.siju.acexplorer.main.model.groups.Category.GENERIC_MUSIC;
import static com.siju.acexplorer.main.model.groups.Category.GENERIC_VIDEOS;
import static com.siju.acexplorer.settings.SettingsPreferenceFragment.PREFS_LANGUAGE;
import static com.siju.acexplorer.theme.ThemeUtils.CURRENT_THEME;
import static com.siju.acexplorer.theme.ThemeUtils.PREFS_THEME;

/**
 * Created by Siju on 02 September,2017
 */
public class MainUiView extends DrawerLayout implements MainUi, PermissionResultCallback {

    private final String TAG = this.getClass().getSimpleName();

    public static final int PERMISSIONS_REQUEST = 100;

    private SharedPreferenceWrapper sharedPreferenceWrapper;

    private Context context;
    private AppCompatActivity activity;
    private View viewSeparator;
    private SharedPreferences preferences;
    private FrameLayout frameDualPane;

    private PermissionHelper permissionHelper;

    private static final int MENU_FAVORITES = 1;
    private int currentOrientation;
    private boolean isHomeScreenEnabled;
    private boolean showHidden;
    private boolean isHomePageAdded;
    private boolean canDualModeBeAct;
    private boolean isDualPaneEnabled;
    private boolean isDualPaneInFocus;
    private boolean isHomePageRemoved;
    private Theme currentTheme;
    private boolean isFirstRun;
    private String language;

    public MainUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

    }

    public static MainUiView inflate(ViewGroup parent) {
        return (MainUiView) LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main,
                parent, false);
    }


    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.activity_main, this, true);
        PreferenceManager.setDefaultValues(getContext(), R.xml.pref_settings, false);
//        removeFragmentsOnPermissionRevoked(savedInstanceState);
        registerReceivers();
    }

    private void initViews() {
        frameDualPane = findViewById(R.id.frame_container_dual);
        viewSeparator = findViewById(R.id.viewSeparator);
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        context.registerReceiver(localeListener, filter);
    }

    private void unregisterReceivers() {
        navigationDrawer.unregisterContextMenu();
        context.unregisterReceiver(localeListener);

    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }


    void initialize() {
        setupPermissions();
    }

    private void setupPermissions() {
        permissionHelper = new PermissionHelper(activity, this);
        permissionHelper.checkPermissions();
    }

    void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                    .main_container);
            Fragment dualFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

            if (isDualPaneInFocus && dualFragment != null) {
                ((BaseFileListFragment) dualFragment).performVoiceSearch(query);
            }
            else if (fragment instanceof BaseFileListFragment) {
                ((BaseFileListFragment) fragment).performVoiceSearch(query);
            }
        }
    }


    void onForeground() {
        permissionHelper.onResume();
    }

    private void setupInitialData() {
        initPreferences();
        checkScreenOrientation();
        if (!checkIfInAppShortcut(activity.getIntent())) {
            displayMainScreen(isHomeScreenEnabled);
        }
    }


    private void initPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
    }

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE
                && isDualPaneEnabled) {
            canDualModeBeAct = true;
        }
    }

    private static final String ACTION_IMAGES = "android.intent.action.SHORTCUT_IMAGES";
    private static final String ACTION_MUSIC = "android.intent.action.SHORTCUT_MUSIC";
    private static final String ACTION_VIDEOS = "android.intent.action.SHORTCUT_VIDEOS";


    private boolean checkIfInAppShortcut(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Category category = GENERIC_IMAGES;
            switch (intent.getAction()) {
                case ACTION_IMAGES:
                    category = GENERIC_IMAGES;
                    break;
                case ACTION_MUSIC:
                    category = GENERIC_MUSIC;
                    break;
                case ACTION_VIDEOS:
                    category = GENERIC_VIDEOS;
                    break;

            }
            if (PermissionUtils.hasRequiredPermissions()) {
                displayFileList(null, category);
            }
            isHomePageAdded = true;
            return true;
        }
        return false;
    }


    private void displayMainScreen(boolean isHomeScreenEnabled) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                .main_container);
        if (fragment != null) {
            return;
        }
        if (isHomeScreenEnabled) {
            displayHomeScreen();
        } else {
            displayFileList(INSTANCE.getInternalStorage(), FILES);
            if (canDualModeBeAct) {
                showDualPane();
            }
        }
    }


    void handlePermissionResult(int requestCode) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                permissionHelper.onPermissionResult(requestCode, permissions, grantResults);
                break;
        }
    }




    void cleanUp() {
        unregisterReceivers();
        try {
            RootTools.closeAllShells();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    boolean handleBackPress() {

        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
        Fragment dualFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

        Logger.log(TAG, "Onbackpress--fragment=" + fragment + " " +
                "mHomePageRemoved=" + isHomePageRemoved + "home added=" + isHomePageAdded + " isDualInFocus:" + isDualPaneInFocus);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isDualPaneInFocus) {
            return onStoragePaneBackPress(dualFragment);
        } else if (fragment instanceof BaseFileListFragment) {
            return onStoragePaneBackPress(fragment);
        } else {
            // Remove HomeScreen Frag & Exit App
            Logger.log(TAG, "Onbackpress--ELSE=");
            activity.finish();
        }
        return false;
    }

    private boolean onStoragePaneBackPress(Fragment fragment) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();

        if (((BaseFileListFragment) fragment).isFabExpanded()) {
            ((BaseFileListFragment) fragment).collapseFab();
        } else {
            boolean isHome = ((BaseFileListFragment) fragment).onBackPressed();
            if (isHome) {
                hideDualPane();
                if (isHomePageRemoved) {
                    if (backStackEntryCount != 0) {
                        activity.finish();
                    }
                } else if (isHomePageAdded) {
                    displayHomeScreen();
                    frameDualPane.setVisibility(View.GONE);
                    viewSeparator.setVisibility(View.GONE);
                    isHomePageAdded = false;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver localeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                restartApp(true);
            }
        }
    };

    private void restartApp(boolean isLocaleChangedPhoneSettings) {

        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        if (!isLocaleChangedPhoneSettings) {
            activity.startActivity(activity.getIntent());
        }

    }

    private void displayHomeScreen() {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        HomeScreenFragment homeScreenFragment = HomeScreenFragment.Companion.newInstance(isFirstRun, canDualModeBeAct);
        ft.replace(R.id.main_container, homeScreenFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void displayFileList(String directory, Category category) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                .main_container);

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();

        Fragment dualPaneFragment = activity.getSupportFragmentManager().findFragmentById(R.id
                .frame_container_dual);
        if (fragment == null || fragment instanceof HomeScreenFragment) {
            FileListFragment baseFileList = FileListFragment.Companion.newInstance(directory, category, canDualModeBeAct);
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim
                    .enter_from_right, R.anim
                    .exit_to_left);
            ft.replace(R.id.main_container, baseFileList, directory);
            ft.addToBackStack(null);
            ft.commit();
        }
        else if (isDualPaneInFocus && dualPaneFragment != null) {
            ((DualPaneFragment) dualPaneFragment).reloadList(directory, category);
        }
        else {
            ((BaseFileListFragment) fragment).reloadList(directory, category);
        }
    }




    private void showDualPane() {
        if (canDualModeBeAct) {
            frameDualPane.setVisibility(View.VISIBLE);
            viewSeparator.setVisibility(View.VISIBLE);
        }
    }

    private void hideDualPane() {
        frameDualPane.setVisibility(View.GONE);
        viewSeparator.setVisibility(View.GONE);
        isDualPaneInFocus = false;
    }

    private void createDualFragment() {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        String internalStoragePath = INSTANCE.getInternalStorage();
        DualPaneFragment dualFragment = DualPaneFragment.newInstance(internalStoragePath, FILES, true);
        ft.replace(R.id.frame_container_dual, dualFragment);
        ft.commitAllowingStateLoss();
    }

    void resetFavoritesData() {
        sharedPreferenceWrapper.resetFavourites(getContext());
    }


    @Override
    public void onPermissionGranted(String[] permissionName) {
        if (activity == null) {
            return;
        }
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);

        Logger.log(TAG, "onPermissionGranted: " + fragment);
        if (fragment == null) {
            return;
        }
        if (fragment instanceof HomeScreenFragment) {
            ((HomeScreenFragment) fragment).onPermissionGranted();
        } else {
            ((BaseFileListFragment) fragment).onPermissionGranted();
        }
    }

    @Override
    public void onPermissionDeclined(String[] permissionName) {

    }





    public void onConfigChanged(Configuration newConfig) {
        if (SdkHelper.isAtleastNougat()) {
            if (activity.isInMultiWindowMode()) {
                return;
            }
        }
        if (currentOrientation != newConfig.orientation) {
            currentOrientation = newConfig.orientation;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
            isDualPaneEnabled = preferences.getBoolean(FileConstants
                    .PREFS_DUAL_PANE, false);
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && isDualPaneEnabled) {
                canDualModeBeAct = true;
                if (fragment instanceof BaseFileListFragment) {
                    showDualPane();
                    ((BaseFileListFragment) fragment).showDualPane();
                    createDualFragment();
                } else {
                    ((HomeScreenFragment) fragment).showDualMode();
                }
            } else {
                canDualModeBeAct = false;
                hideDualPane();
            }
            fragment.onConfigurationChanged(newConfig);
        }


        super.onConfigurationChanged(newConfig);
    }





    @Override
    public void onIntentReceived(Intent intent) {

    }

    @Override
    public void setListener(Listener listener) {

    }

    public void passUserPrefs(Bundle userPrefs) {
        isHomeScreenEnabled = userPrefs.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        isDualPaneEnabled = userPrefs.getBoolean(FileConstants.PREFS_DUAL_PANE, false);
        isFirstRun = userPrefs.getBoolean(FileConstants.PREFS_FIRST_RUN, true);
        language = userPrefs.getString(PREFS_LANGUAGE, Locale.getDefault().getLanguage());
        Analytics.getLogger().dualPaneState(isDualPaneEnabled);
        setupInitialData();
    }

    @Override
    public void onExit() {

    }

    @Override
    public void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    public void onBillingUnSupported() {
        PremiumUtils.onStart(getContext());
        if (PremiumUtils.shouldShowPremiumDialog()) {
            Premium premium = new Premium(activity, bridge.getBillingManager());
            premium.showPremiumDialog();
        }
    }

    public void onFreeVersion() {
        AdHelper.setupAds(activity);
    }

    public void onPremiumVersion() {
        navigationDrawer.setPremium();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
        Fragment dualFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

        if (fragment == null) {
            return;
        }
        if (fragment instanceof HomeScreenFragment) {
            ((HomeScreenFragment) fragment).setPremium();
        } else {
            ((BaseFileListFragment) fragment).setPremium();
            if (canDualModeBeAct && dualFragment != null) {
                ((BaseFileListFragment) dualFragment).setPremium();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    public void getTotalGroupData() {
        bridge.getTotalGroupData();
    }

    public void onTotalGroupDataFetched(final ArrayList<SectionGroup> totalData) {
        navigationDrawer.onTotalGroupDataFetched(totalData);
    }

    public void onDrawerIconClicked(boolean dualPane) {
        openDrawer(dualPane);
    }

    public void syncDrawer() {
        navigationDrawer.syncDrawerState();
    }



//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Logger.log(TAG, "onSharedPreferenceChanged: " + key + "this:"+this);
//        if (PREFS_FIRST_RUN.equals(key)) {
//            return;
//        }
//        preferenceKeySet.add(key);
//    }

    void checkForPreferenceChanges() {
        checkIsThemeChanged();
        isLanguageChanged();
        isHiddenChanged();
        isHomeSettingChanged();
        isDualSettingChanged();
    }

    private void isDualSettingChanged() {
        boolean isDualPaneEnabledSettings = preferences.getBoolean(FileConstants
                .PREFS_DUAL_PANE, false);


        if (isDualPaneEnabled == isDualPaneEnabledSettings) {
            return;
        }
        Logger.log(TAG, "OnPrefschanged PREFS_DUAL_PANE" + isDualPaneEnabledSettings);
        if (!isDualPaneEnabledSettings) {
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);

            hideDualPane();
            if (fragment instanceof BaseFileListFragment) {
                ((BaseFileListFragment) fragment).hideDualPane();
                ((BaseFileListFragment) fragment).refreshSpan(); // For changing the no of
                // columns in non-dual mode
            } else {
                ((HomeScreenFragment) fragment).hideDualPane();
            }
            isDualPaneEnabled = false;
            canDualModeBeAct = false;
        } else {
            isDualPaneEnabled = true;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                canDualModeBeAct = true;
            }
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);

            if (fragment instanceof BaseFileListFragment) {
                showDualPane();
                createDualFragment();
                ((BaseFileListFragment) fragment).showDualPane();
            } else {
                ((HomeScreenFragment) fragment).showDualMode();
            }
        }
    }

    private void isHomeSettingChanged() {
        boolean isHomeScreenEnabled = preferences.getBoolean(FileConstants.PREFS_HOMESCREEN,
                true);


        if (isHomeScreenEnabled != this.isHomeScreenEnabled) {
            this.isHomeScreenEnabled = isHomeScreenEnabled;
            Logger.log(TAG, "OnPrefschanged PREFS_HOMESCREEN" + this.isHomeScreenEnabled);
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);

            Fragment dualFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

            // If homescreen disabled
            if (!isHomeScreenEnabled) {

                // If user on Home page, replace it with BaseFileListFragment
                if (fragment instanceof HomeScreenFragment) {
                    displayFileList(INSTANCE.getInternalStorage(), FILES);
                    isHomePageRemoved = true;
                } else {
                    ((BaseFileListFragment) fragment).removeHomeFromNavPath();
                    if (canDualModeBeAct && dualFragment != null) {
                        ((DualPaneFragment) dualFragment).removeHomeFromNavPath();
                    }
                    // Set a flag so that it can be removed on backPress
                    isHomePageRemoved = true;
                    isHomePageAdded = false;
                }

            } else {
                if (fragment instanceof BaseFileListFragment) {
                    ((BaseFileListFragment) fragment).addHomeNavPath();
                }

                if (canDualModeBeAct && dualFragment != null) {
                    ((DualPaneFragment) dualFragment).addHomeNavPath();
                }
                // Clearing the flags necessary as user can click checkbox multiple times
                isHomePageAdded = true;
                isHomePageRemoved = false;
            }
        }
    }

    private void isHiddenChanged() {
        boolean showHidden = preferences.getBoolean(FileConstants.PREFS_HIDDEN, false);

        if (showHidden != this.showHidden) {
            this.showHidden = showHidden;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
            Fragment dualPaneFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
            if (fragment instanceof BaseFileListFragment) {
                ((BaseFileListFragment) fragment).setHidden(showHidden);
                ((BaseFileListFragment) fragment).refreshList();
            }
            else if (fragment instanceof HomeScreenFragment) {
                ((HomeScreenFragment) fragment).refreshList();
            }

            if (canDualModeBeAct && dualPaneFragment != null) {
                ((BaseFileListFragment) dualPaneFragment).setHidden(showHidden);
                ((BaseFileListFragment) dualPaneFragment).refreshList();
            }
        }
    }

    private void isLanguageChanged() {
        String language = preferences.getString(PREFS_LANGUAGE, Locale
                .getDefault().getLanguage());
        if (this.language.equals(language)) {
            return;

        }
        Logger.log(TAG, "OnPrefschanged PREFS_LANGUAGE");
        LocaleHelper.persist(getContext(), language);
        restartApp(false);
    }

    private void checkIsThemeChanged() {
        String value = preferences.getString(PREFS_THEME, "");
        int themePos = Integer.parseInt(value);
        Theme theme = Theme.getTheme(themePos);
        if (!theme.equals(currentTheme)) {
            currentTheme = theme;
            preferences.edit().putInt(CURRENT_THEME, currentTheme.getValue()).apply();
            restartApp(false);
        }
    }


    public void showDualFrame() {
        showDualPane();
        createDualFragment();
    }

    public void setDualPaneFocusState(boolean state) {
        isDualPaneInFocus = state;
//        Log.d(TAG, "setDualPaneFocusState() called with: state = [" + state + "]");
    }

    public void switchView(int viewMode, boolean isDual) {
        if (isDualPaneEnabled) {
            Fragment fragment;
            if (isDual) {
                fragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
            } else {
                fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
            }
            if (fragment != null) {
                ((BaseFileListFragment) fragment).switchView(viewMode);
            }
        }
    }


    public void refreshList(boolean isDual) {
        if (isDualPaneEnabled) {
            Fragment fragment;
            if (isDual) {
                fragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
            } else {
                fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
            }
            if (fragment != null) {
                ((BaseFileListFragment) fragment).refreshList();
            }
        }
    }


    public void onSearchClicked() {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
        Fragment dualFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

        if (isDualPaneInFocus && fragment instanceof FileListFragment) {
            ((BaseFileListFragment)fragment).collapseSearchView();
        }
        else if (!isDualPaneInFocus && dualFragment != null) {
            ((DualPaneFragment)dualFragment).collapseSearchView();
        }
    }

    public void onMultiWindowChanged(boolean isInMultiWindowMode, Configuration newConfig) {
            currentOrientation = newConfig.orientation;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
            isDualPaneEnabled = preferences.getBoolean(FileConstants
                                                               .PREFS_DUAL_PANE, false);
            if (!isInMultiWindowMode && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && isDualPaneEnabled) {
                canDualModeBeAct = true;
                if (fragment instanceof BaseFileListFragment) {
                    showDualPane();
                    ((BaseFileListFragment) fragment).showDualPane();
                    createDualFragment();
                } else {
                    ((HomeScreenFragment) fragment).showDualMode();
                }
            } else {
                canDualModeBeAct = false;
                hideDualPane();
            }
            fragment.onConfigurationChanged(newConfig);
    }

}
