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

package com.siju.acexplorer.view;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.siju.acexplorer.R;
import com.siju.acexplorer.ads.AdHelper;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.home.view.HomeScreenFragment;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SharedPreferenceWrapper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.groups.DrawerGroup;
import com.siju.acexplorer.permission.PermissionHelper;
import com.siju.acexplorer.permission.PermissionResultCallback;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.premium.Premium;
import com.siju.acexplorer.premium.PremiumUtils;
import com.siju.acexplorer.storage.view.BaseFileList;
import com.siju.acexplorer.storage.view.DualPaneList;
import com.siju.acexplorer.storage.view.FileList;
import com.siju.acexplorer.storage.view.StoragesUiView;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.LocaleHelper;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.siju.acexplorer.model.FileConstants.PREFS_FIRST_RUN;
import static com.siju.acexplorer.model.StorageUtils.getInternalStorage;
import static com.siju.acexplorer.model.groups.Category.AUDIO;
import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.IMAGE;
import static com.siju.acexplorer.model.groups.Category.VIDEO;
import static com.siju.acexplorer.settings.SettingsPreferenceFragment.PREFS_LANGUAGE;
import static com.siju.acexplorer.theme.ThemeUtils.CURRENT_THEME;
import static com.siju.acexplorer.theme.ThemeUtils.PREFS_THEME;

/**
 * Created by Siju on 02 September,2017
 */
public class MainUiView extends DrawerLayout implements PermissionResultCallback,
        DrawerListener,
        StoragesUiView.FavoriteOperation {

    private final String TAG = this.getClass().getSimpleName();

    public static final int PERMISSIONS_REQUEST = 100;

    private SharedPreferenceWrapper sharedPreferenceWrapper;

    private Context context;
    private AppCompatActivity activity;
    private MainBridge bridge;
    private View viewSeparator;
    private SharedPreferences preferences;
    private FrameLayout frameDualPane;

    private PermissionHelper permissionHelper;
    private NavigationDrawer navigationDrawer;
    private HomeScreenFragment homeScreenFragment;

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
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        initViews();
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

    public void setBridgeRef(MainBridge mainBridge) {
        this.bridge = mainBridge;
    }

    void initialize() {
        currentTheme = bridge.getCurrentTheme();
        navigationDrawer = new NavigationDrawer(activity, this, currentTheme);
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
            if (fragment instanceof BaseFileList) {
                ((BaseFileList) fragment).performVoiceSearch(query);
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
            Category category = IMAGE;
            switch (intent.getAction()) {
                case ACTION_IMAGES:
                    category = IMAGE;
                    break;
                case ACTION_MUSIC:
                    category = AUDIO;
                    break;
                case ACTION_VIDEOS:
                    category = VIDEO;
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
        if (isHomeScreenEnabled) {
            displayHomeScreen();
        } else {
            displayFileList(getInternalStorage(), FILES);
            if (canDualModeBeAct) {
                showDualPane();
            }
        }
    }


    void handlePermissionResult(int requestCode) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                permissionHelper.onPermissionResult();
                break;
        }
    }


    boolean handleActivityResult(int requestCode, int resultCode, Intent intent) {
        navigationDrawer.onActivityResult(requestCode, resultCode, intent);
        return false;
    }


    void cleanUp() {
        unregisterReceivers();
        try {
            RootTools.closeAllShells();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BillingManager.getInstance().destroy();
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
        } else if (fragment instanceof BaseFileList) {
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

        if (((BaseFileList) fragment).isFabExpanded()) {
            ((BaseFileList) fragment).collapseFab();
        } else {
            boolean isHome = ((BaseFileList) fragment).onBackPressed();
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
        Bundle args = new Bundle();
        args.putBoolean(FileConstants.KEY_HOME, true);
        args.putBoolean(PREFS_FIRST_RUN, isFirstRun);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, canDualModeBeAct);
        homeScreenFragment = new HomeScreenFragment();
        homeScreenFragment.setArguments(args);
        homeScreenFragment.setDrawerListener(this);
        homeScreenFragment.setFavListener(this);
        ft.replace(R.id.main_container, homeScreenFragment);
        ft.addToBackStack(null);
        Logger.log(TAG, "displayHomeScreen");
        ft.commitAllowingStateLoss();
    }

    private void displayFileList(String directory, Category category) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        args.putSerializable(FileConstants.KEY_CATEGORY, category);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, canDualModeBeAct);

        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                .main_container);
        if (fragment == null || fragment instanceof HomeScreenFragment) {
            FileList baseFileList = new FileList();
            baseFileList.setArguments(args);
            baseFileList.setFavoriteListener(this);
            baseFileList.setDrawerListener(this);
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim
                    .enter_from_right, R.anim
                    .exit_to_left);
//            ft.hide(fragment);
//            ft.add(R.id.main_container, baseFileList, directory);

            ft.replace(R.id.main_container, baseFileList, directory);
            ft.addToBackStack(null);
            ft.commit();
            Logger.log(TAG, "displayFileList");

        } else {
            ((BaseFileList) fragment).reloadList(directory, category);
        }
    }

    @Override
    public void updateFavorites(ArrayList<FavInfo> favList) {
        navigationDrawer.updateFavorites(favList);
        if (isHomeScreenEnabled && homeScreenFragment != null) {
            homeScreenFragment.updateFavoritesCount(favList.size());
        }
    }

    @Override
    public void removeFavorites(ArrayList<FavInfo> favList) {
        navigationDrawer.removeFavorites(favList);
        if (isHomeScreenEnabled && homeScreenFragment != null) {
            homeScreenFragment.removeFavorites(favList.size());
        }
    }


    void onStorageItemClicked(String path) {
/*        initializeStartingDirectory();
        checkIfFavIsRootDir(groupPos);*/
        displayFileList(path, FILES);
    }

    void onLibraryItemClicked(int childPos) {
        Category category = Category.getCategory(childPos);
        displayFileList(null, category);
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
        String internalStoragePath = getInternalStorage();
        DualPaneList dualFragment = DualPaneList.newInstance(internalStoragePath, FILES, true);
        dualFragment.setFavoriteListener(this);
        dualFragment.setDrawerListener(this);
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
            ((BaseFileList) fragment).onPermissionGranted();
        }
    }

    @Override
    public void onPermissionDeclined(String[] permissionName) {

    }


    public void onCreateContextMenu(ContextMenu menu,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        if (menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

            int type =
                    ExpandableListView.getPackedPositionType(info.packedPosition);

            int group =
                    ExpandableListView.getPackedPositionGroup(info.packedPosition);

            // Only for Favorites
            if (group == DrawerGroup.FAVORITES.getValue()) {
                // Only create a context menu for child items
                if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    menu.add(0, MENU_FAVORITES, 0, getContext().getString(R.string.delete_fav));
                }
            }
        }
    }


    void onContextItemSelected(MenuItem menuItem) {
        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();

        int groupPos = 0, childPos = 0;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        }

        switch (menuItem.getItemId()) {
            case MENU_FAVORITES:
                FavInfo favInfo = navigationDrawer.removeFavorite(groupPos, childPos);
                sharedPreferenceWrapper.removeFavorite(getContext(), favInfo);
                if (isHomeScreenEnabled && homeScreenFragment != null) {
                    homeScreenFragment.removeFavorites(1);
                }
        }
    }

    public void onConfigChanged(Configuration newConfig) {
        Logger.log(TAG, "onConfigChanged" + newConfig.orientation);
        if (currentOrientation != newConfig.orientation) {
            currentOrientation = newConfig.orientation;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);
            isDualPaneEnabled = preferences.getBoolean(FileConstants
                    .PREFS_DUAL_PANE, false);
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && isDualPaneEnabled) {
                canDualModeBeAct = true;
                if (fragment instanceof BaseFileList) {
                    showDualPane();
                    ((BaseFileList) fragment).showDualPane();
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


    private void openDrawer() {
        navigationDrawer.openDrawer();
    }


    public void passUserPrefs(Bundle userPrefs) {
        isHomeScreenEnabled = userPrefs.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        isDualPaneEnabled = userPrefs.getBoolean(FileConstants.PREFS_DUAL_PANE, false);
        isFirstRun = userPrefs.getBoolean(PREFS_FIRST_RUN, true);
        language = userPrefs.getString(PREFS_LANGUAGE, Locale.getDefault().getLanguage());
        Analytics.getLogger().dualPaneState(isDualPaneEnabled);
        setupInitialData();
    }

    public void onBillingUnSupported() {
        PremiumUtils.onStart(getContext());
        if (PremiumUtils.shouldShowPremiumDialog()) {
            Premium premium = new Premium(activity);
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
            ((BaseFileList) fragment).setPremium();
            if (canDualModeBeAct && dualFragment != null) {
                ((BaseFileList) dualFragment).setPremium();
            }
        }
    }

    public void getTotalGroupData() {
        bridge.getTotalGroupData();
    }

    public void onTotalGroupDataFetched(final ArrayList<SectionGroup> totalData) {
        navigationDrawer.onTotalGroupDataFetched(totalData);
    }

    @Override
    public void onDrawerIconClicked() {
        openDrawer();
    }

    @Override
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
            if (fragment instanceof BaseFileList) {
                ((BaseFileList) fragment).hideDualPane();
                ((BaseFileList) fragment).refreshSpan(); // For changing the no of
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

            if (fragment instanceof BaseFileList) {
                showDualPane();
                createDualFragment();
                ((BaseFileList) fragment).showDualPane();
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

                // If user on Home page, replace it with BaseFileList
                if (fragment instanceof HomeScreenFragment) {
                    displayFileList(getInternalStorage(), FILES);
                    isHomePageRemoved = true;
                } else {
                    ((BaseFileList) fragment).removeHomeFromNavPath();
                    if (canDualModeBeAct && dualFragment != null) {
                        ((DualPaneList) dualFragment).removeHomeFromNavPath();
                    }
                    // Set a flag so that it can be removed on backPress
                    isHomePageRemoved = true;
                    isHomePageAdded = false;
                }

            } else {
                if (fragment instanceof BaseFileList) {
                    ((BaseFileList) fragment).addHomeNavPath();
                }

                if (canDualModeBeAct && dualFragment != null) {
                    ((DualPaneList) dualFragment).addHomeNavPath();
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
            Logger.log(TAG, "OnPrefschanged PREFS_HIDDEN" + this.showHidden);
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_container);

            Fragment dualPaneFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
            if (fragment instanceof BaseFileList) {
                ((BaseFileList) fragment).setHidden(showHidden);
                ((BaseFileList) fragment).refreshList();
            }

            if (canDualModeBeAct && dualPaneFragment != null) {
                ((BaseFileList) dualPaneFragment).setHidden(showHidden);
                ((BaseFileList) dualPaneFragment).refreshList();
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
        LocaleHelper.setLocale(getContext(), language);
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
                ((BaseFileList) fragment).switchView(viewMode);
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
                ((BaseFileList) fragment).refreshList();
            }
        }
    }
}
