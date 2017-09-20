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
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.siju.acexplorer.R;
import com.siju.acexplorer.ads.AdHelper;
import com.siju.acexplorer.billing.BillingHelper;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.SharedPreferenceWrapper;
import com.siju.acexplorer.storage.view.BaseFileList;
import com.siju.acexplorer.storage.view.DualPaneList;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.storage.view.FileList;
import com.siju.acexplorer.home.view.HomeScreenFragment;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.groups.DrawerGroups;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.permission.PermissionHelper;
import com.siju.acexplorer.permission.PermissionResultCallback;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.premium.Premium;
import com.siju.acexplorer.premium.PremiumUtils;
import com.siju.acexplorer.storage.view.StoragesUiView;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.LocaleHelper;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.siju.acexplorer.model.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.model.FileConstants.PREFS_DUAL_PANE;
import static com.siju.acexplorer.model.FileConstants.PREFS_FIRST_RUN;
import static com.siju.acexplorer.model.FileConstants.PREFS_HIDDEN;
import static com.siju.acexplorer.model.FileConstants.PREFS_HOMESCREEN;
import static com.siju.acexplorer.model.FileConstants.PREFS_RESET;
import static com.siju.acexplorer.model.groups.Category.AUDIO;
import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.IMAGE;
import static com.siju.acexplorer.model.groups.Category.VIDEO;
import static com.siju.acexplorer.model.StorageUtils.getInternalStorage;
import static com.siju.acexplorer.settings.SettingsPreferenceFragment.PREFS_LANGUAGE;
import static com.siju.acexplorer.theme.ThemeUtils.CURRENT_THEME;
import static com.siju.acexplorer.theme.ThemeUtils.PREFS_THEME;

/**
 * Created by Siju on 02 September,2017
 */
public class MainUiView extends DrawerLayout implements PermissionResultCallback, DrawerListener, StoragesUiView.FavoriteOperation {

    public static final int PERMISSIONS_REQUEST = 100;

    private final String TAG = this.getClass().getSimpleName();

    private SharedPreferenceWrapper sharedPreferenceWrapper;

    private Context context;
    private AppCompatActivity activity;
    private MainBridge bridge;
    private View viewSeparator;
    private SharedPreferences preferences;
    private FrameLayout frameDualPane;

    private PermissionHelper permissionHelper;
    private NavigationDrawer navigationDrawer;
    private HomeScreenFragment mHomeScreenFragment;

    private final int MENU_FAVOURITES = 1;
    private int mCurrentOrientation;
    private boolean isHomeScreenEnabled;
    private boolean showHidden;
    private boolean isHomePageAdded;
    private boolean isDualModeActive;
    private boolean isDualPaneEnabled;
    private boolean mIsHomePageRemoved;
    private Theme currentTheme;
    private boolean isFirstRun;


    public MainUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

    }

    public static MainUiView inflate(ViewGroup parent) {
        return (MainUiView) LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .activity_main, parent,
                false);
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

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    private void setupPermissions() {
        permissionHelper = new PermissionHelper(activity, this);
        permissionHelper.checkPermissions();
    }

    public void setBridgeRef(MainBridge mainBridge) {
        this.bridge = mainBridge;
    }

    void initialize() {
        currentTheme = bridge.getCurrentTheme();
        navigationDrawer = new NavigationDrawer(activity,this, currentTheme);
        setupPermissions();
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        context.registerReceiver(localeListener, filter);
    }

    private void unregisterReceivers() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        navigationDrawer.unregisterContextMenu();
        context.unregisterReceiver(localeListener);

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
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE
                && isDualPaneEnabled) {
            isDualModeActive = true;
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
        }
        else {
            displayFileList(getInternalStorage(), FILES);
            if (isDualModeActive) {
                toggleDualPaneVisibility(true);
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

    void handleActivityResult(int requestCode, int resultCode, Intent intent) {

        if (!BillingHelper.getInstance().onActivityResult(requestCode, resultCode, intent)) {
            navigationDrawer.onActivityResult(requestCode, resultCode, intent);
            // TODO: 02/09/17 Need to pass to activity using super?
//            bridge.passActivityResult(requestCode, resultCode, intent);
        }
    }


    void cleanUp() {
        unregisterReceivers();
        try {
            RootTools.closeAllShells();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BillingHelper.getInstance().disposeBilling();
    }


    boolean handleBackPress() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();

        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                .main_container);

        Logger.log(TAG, "Onbackpress--fragment=" + fragment + " " +
                "mHomePageRemoved=" + mIsHomePageRemoved + "home added=" + isHomePageAdded + " " +
                "backstack=" + backStackEntryCount);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (fragment instanceof BaseFileList) {
            if (((BaseFileList) fragment).isFabExpanded()) {
                ((BaseFileList) fragment).collapseFab();
            }
            else if (mIsHomePageRemoved) {
                if (backStackEntryCount != 0) {
                    activity.finish();
                }
            }
            else if (isHomePageAdded) {
                displayHomeScreen();
                frameDualPane.setVisibility(View.GONE);
                viewSeparator.setVisibility(View.GONE);
                isHomePageAdded = false;
            }
            else {
                boolean isHome = ((BaseFileList) fragment).onBackPressed();
                if (isHome) {
                    toggleDualPaneVisibility(false);
                    return true;
                }
            }

        }
        else {
            // Remove HomeScreen Frag & Exit App
            Logger.log(TAG, "Onbackpress--ELSE=");
            activity.finish();
//            super.onBackPressed();
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
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, isDualModeActive);
        mHomeScreenFragment = new HomeScreenFragment();
        mHomeScreenFragment.setArguments(args);
        mHomeScreenFragment.setDrawerListener(this);
        ft.replace(R.id.main_container, mHomeScreenFragment);
        ft.addToBackStack(null);
        Logger.log(TAG, "initialScreenSetup");
        ft.commitAllowingStateLoss();
    }

    private void displayFileList(String directory, Category category) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        args.putSerializable(FileConstants.KEY_CATEGORY, category);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, isDualModeActive);

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
            ft.replace(R.id.main_container, baseFileList, directory);
            ft.addToBackStack(null);
            ft.commit();
        }
        else {
            ((BaseFileList) fragment).reloadList(directory, category);
        }
    }

    @Override
    public void updateFavorites(ArrayList<FavInfo> favList) {
        navigationDrawer.updateFavourites(favList);
        if (isHomeScreenEnabled && mHomeScreenFragment != null) {
            mHomeScreenFragment.updateFavoritesCount(favList.size());
        }
    }

    @Override
    public void removeFavorites(ArrayList<FavInfo> favList) {
        navigationDrawer.removeFavourites(favList);
        if (isHomeScreenEnabled && mHomeScreenFragment != null) {
            mHomeScreenFragment.removeFavorites(favList.size());
        }
    }


    void onStorageItemClicked(int groupPos, String path) {
/*        initializeStartingDirectory();
        checkIfFavIsRootDir(groupPos);*/
        displayFileList(path, FILES);
    }

    void onLibraryItemClicked(int childPos) {
        Category category = Category.getCategory(childPos);
        displayFileList(null, category);
    }


    /**
     * Dual pane mode to be shown only for File Category
     *
     * @param isFilesCategory True if files category
     */
    public void toggleDualPaneVisibility(boolean isFilesCategory) {
        if (isFilesCategory) {
            showDualPane();
        }
        else {
            hideDualPane();
        }
    }

    private void showDualPane() {
        if (isDualModeActive) {
            frameDualPane.setVisibility(View.VISIBLE);
            viewSeparator.setVisibility(View.VISIBLE);
        }
    }

    private void hideDualPane() {
        frameDualPane.setVisibility(View.GONE);
        viewSeparator.setVisibility(View.GONE);
    }


    public void createDualFragment() {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        String internalStoragePath = getInternalStorage();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, internalStoragePath);
        args.putSerializable(KEY_CATEGORY, FILES);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, true);

        DualPaneList dualFragment = new DualPaneList();
        dualFragment.setArguments(args);
        ft.replace(R.id.frame_container_dual, dualFragment);
        ft.commit();
    }

    void onPreferenceChange() {
        Log.d(TAG, "onPreferenceChange: " + preferenceKey);

        if (preferenceKey == null) {
            return;
        }
        switch (preferenceKey) {
            case PREFS_THEME:
                String value = preferences.getString(PREFS_THEME, "");
                int themePos = Integer.valueOf(value);
                Theme theme = Theme.getTheme(themePos);
                if (!theme.equals(currentTheme)) {
                    currentTheme = theme;
                    preferences.edit().putInt(CURRENT_THEME, currentTheme.getValue()).apply();
                    restartApp(false);
                }
                break;
            case PREFS_LANGUAGE:

                String language = preferences.getString(PREFS_LANGUAGE, Locale
                        .getDefault().getLanguage());
                LocaleHelper.setLocale(getContext(), language);
                restartApp(false);
                break;

            case PREFS_HIDDEN:
                boolean showHidden = preferences.getBoolean(FileConstants.PREFS_HIDDEN, false);

                if (showHidden != this.showHidden) {
                    this.showHidden = showHidden;
                    Logger.log(TAG, "OnPrefschanged PREFS_HIDDEN" + this.showHidden);
                    Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                            .main_container);

           /* BaseFileList dualPaneFragment = (FileListDualFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.frame_container_dual);*/
                    if (fragment instanceof BaseFileList) {
                        ((BaseFileList) fragment).refreshList();
                    }
     /*       if (dualPaneFragment != null) {
                dualPaneFragment.refreshList();
            }*/
                }
                break;
            case PREFS_HOMESCREEN:

                boolean isHomeScreenEnabled = preferences.getBoolean(FileConstants
                        .PREFS_HOMESCREEN, true);

                if (isHomeScreenEnabled != this.isHomeScreenEnabled) {

                    this.isHomeScreenEnabled = isHomeScreenEnabled;
                    Logger.log(TAG, "OnPrefschanged PREFS_HOMESCREEN" + this.isHomeScreenEnabled);
                    Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                            .main_container);
                    // If homescreen disabled
                    if (!isHomeScreenEnabled) {

                        // If user on Home page, replace it with BaseFileList
                        if (fragment instanceof HomeScreenFragment) {
                            displayFileList(getInternalStorage(), FILES);
                            mIsHomePageRemoved = true;
                        }
                        else {
                            ((BaseFileList) fragment).removeHomeFromNavPath();
                            // Set a flag so that it can be removed on backPress
                            mIsHomePageRemoved = true;
                            isHomePageAdded = false;
                        }

                    }
                    else {
                        // Clearing the flags necessary as user can click checkbox multiple times
                        isHomePageAdded = true;
                        mIsHomePageRemoved = false;
                    }
                }
                break;

            case PREFS_DUAL_PANE:
                boolean isDualPaneEnabledSettings = preferences.getBoolean(FileConstants
                        .PREFS_DUAL_PANE, false);

                Logger.log(TAG, "OnPrefschanged PREFS_DUAL_PANE" + isDualPaneEnabledSettings);

                if (!isDualPaneEnabledSettings) {
                    Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                            .main_container);
                    toggleDualPaneVisibility(false);
                    if (fragment instanceof BaseFileList) {
                        ((BaseFileList) fragment).refreshSpan(); // For changing the no of
                        // columns in
                        // non-dual mode
                    }

                    isDualModeActive = false;
                }
                else {
                    checkScreenOrientation();
                    Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                            .main_container);
                    if (fragment instanceof BaseFileList) {
                        toggleDualPaneVisibility(true);
                        ((BaseFileList) fragment).showDualPane();
                    }
                    createDualFragment();
                }
                break;

            case PREFS_RESET:
                resetFavouritesGroup();
                break;
        }

        preferenceKey = null;
    }


    private void resetFavouritesGroup() {
        navigationDrawer.resetFavouritesGroup();
        sharedPreferenceWrapper.resetFavourites(getContext());
    }


    @Override
    public void onPermissionGranted(String[] permissionName) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                .main_container);
        if (fragment instanceof HomeScreenFragment) {
            ((HomeScreenFragment) fragment).onPermissionGranted();
        }
        else {
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
            if (group == DrawerGroups.FAVORITES.getValue()) {
                // Only create a context menu for child items
                if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    menu.add(0, MENU_FAVOURITES, 0, getContext().getString(R.string.delete_fav));
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
            case MENU_FAVOURITES:
                FavInfo favInfo = navigationDrawer.removeFavorite(groupPos, childPos);
                sharedPreferenceWrapper.removeFavorite(getContext(), favInfo);
                if (isHomeScreenEnabled && mHomeScreenFragment != null) {
                    mHomeScreenFragment.removeFavorites(1);
                }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.log(TAG, "onConfigurationChanged" + newConfig.orientation);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id
                    .main_container);
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && isDualPaneEnabled) {
                isDualModeActive = true;
                if (fragment instanceof BaseFileList) {
                    toggleDualPaneVisibility(true);
                    ((BaseFileList) fragment).showDualPane();
                    createDualFragment();
                }
                else {
                    ((HomeScreenFragment) fragment).showDualMode();
                }
            }
            else {
                isDualModeActive = false;
                toggleDualPaneVisibility(false);
            }
        }
        super.onConfigurationChanged(newConfig);
    }


    public void openDrawer() {
        navigationDrawer.openDrawer();
    }


    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {


                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String
                        key) {
                    preferenceKey = key;
                }
            };

    private String preferenceKey;

    public void passUserPrefs(Bundle userPrefs) {
        isHomeScreenEnabled = userPrefs.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        isDualPaneEnabled = userPrefs.getBoolean(FileConstants.PREFS_DUAL_PANE, false);
        isFirstRun = userPrefs.getBoolean(PREFS_FIRST_RUN, true);
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





}
