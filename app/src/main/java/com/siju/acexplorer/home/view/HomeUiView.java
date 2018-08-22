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

package com.siju.acexplorer.home.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.storage.view.FileList;
import com.siju.acexplorer.storage.view.StoragesUiView;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.theme.ThemeUtils;
import com.siju.acexplorer.main.view.AceActivity;
import com.siju.acexplorer.main.view.DrawerListener;

import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.home.view.HomeLibrary.LIBSORT_REQUEST_CODE;
import static com.siju.acexplorer.main.model.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.main.model.groups.StoragesGroup.STORAGE_EMULATED_0;


public class HomeUiView extends CoordinatorLayout {

    private final String TAG = this.getClass().getSimpleName();
    private AdView mAdView;
    private boolean isPremium = true;
    private NestedScrollView                 nestedScrollViewHome;
    private Theme                            theme;
    private Toolbar                          toolbar;
    private boolean                          isDualModeActive;
    private Fragment                         fragment;
    private HomeBridge                       bridge;
    private HomeLibrary                      library;
    private DrawerListener                   drawerListener;
    private StoragesUiView.FavoriteOperation favListener;

    public HomeUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public static HomeUiView inflate(ViewGroup parent) {
        return (HomeUiView) LayoutInflater.from(parent.getContext()).inflate(R.layout.homescreen,
                                                                             parent,  false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        nestedScrollViewHome = findViewById(R.id.scrollLayoutHome);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setTheme();
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar() {
        getActivity().setSupportActionBar(toolbar);
        getActivity().getSupportActionBar().setHomeButtonEnabled(true);
        getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        if (drawerListener != null) {
            drawerListener.syncDrawer();
        }
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return fragment;
    }

    private AppCompatActivity getActivity() {
        return (AppCompatActivity) fragment.getActivity();
    }

    public void setBridgeRef(HomeBridge homeBridge) {
        this.bridge = homeBridge;
    }

    void initialize() {
        setupToolbar();

        library = new HomeLibrary(this, theme);
        new HomeStorages(this, theme);

        checkBillingStatus();
        initializeListeners();
        isDualModeActive = bridge.getDualModeState() && getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void checkBillingStatus() {
        BillingStatus billingStatus = bridge.checkBillingStatus();
        switch (billingStatus) {
            case PREMIUM:
                onPremiumVersion();
                break;
            case UNSUPPORTED:
            case FREE:
                onFreeVersion();
                break;
        }
    }

    Configuration getConfiguration() {
        return ((AceActivity)getActivity()).getConfiguration();
    }

    private void initializeListeners() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerListener.onDrawerIconClicked();
            }
        });
    }


    private void onFreeVersion() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            showAds();
        }
    }

    private void onPremiumVersion() {
        hideAds();
    }


    private void setTheme() {
        theme = Theme.getTheme(ThemeUtils.getTheme(getContext())); // TODO: 04/09/17 View should not interact with Model
        switch (theme) {
            case DARK:
                nestedScrollViewHome.setBackgroundColor(ContextCompat.getColor(getContext(), R
                        .color.dark_background));
                break;
            case LIGHT:
                nestedScrollViewHome.setBackgroundColor(ContextCompat.getColor(getContext(), R
                        .color.light_home_bg));
                break;
        }
    }

    void getLibraries() {
        bridge.getLibraries();
    }


    public void onPermissionGranted() {
        List<HomeLibraryInfo> libraryInfoList = library.getLibraries();
        if (libraryInfoList == null) {
            getLibraries();
        } else {
            loadData(libraryInfoList);
        }
    }

    private void loadData(List<HomeLibraryInfo> libraries) {
        if (hasStoragePermission()) {
            for (int i = 0; i < libraries.size(); i++) {
                Category category = libraries.get(i).getCategory();
                if (!category.equals(Category.ADD)) {
                    bridge.loadData(category);
                }
            }
        }
    }

    public void onLibrariesFetched(List<HomeLibraryInfo> libraries) {
        library.setLibraries(libraries);
        loadData(libraries);
    }


    private boolean hasStoragePermission() {
        return PermissionUtils.hasStoragePermission();
    }

    private void hideAds() {
        LinearLayout adviewLayout = findViewById(R.id.adviewLayout);
        if (adviewLayout.getChildCount() != 0) {
            adviewLayout.removeView(mAdView);
        }
    }

    private void showAds() {
        // DYNAMICALLY CREATE AD START
        LinearLayout adviewLayout = findViewById(R.id.adviewLayout);
        // Create an ad.
        if (mAdView == null) {
            mAdView = new AdView(AceApplication.getAppContext());
            mAdView.setAdSize(AdSize.BANNER);
            mAdView.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));
            // DYNAMICALLY CREATE AD END
            AdRequest adRequest = new AdRequest.Builder().build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
            // Add the AdView to the view hierarchy. The view will have no size until the ad is
            // loaded.
            adviewLayout.addView(mAdView);
        } else {
            ((LinearLayout) mAdView.getParent()).removeAllViews();
            adviewLayout.addView(mAdView);
            // Reload Ad if necessary.  Loaded ads are lost when the activity is paused.
            if (!mAdView.isLoading()) {
                AdRequest adRequest = new AdRequest.Builder().build();
                // Start loading the ad in the background.
                mAdView.loadAd(adRequest);
            }
        }
        // DYNAMICALLY CREATE AD END
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }


    public void setPremium() {
        isPremium = true;
        hideAds();
    }


    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    public void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
    }


    public void onDataLoaded(int id, List<FileInfo> data) {
        if (data != null && data.size() > 0) {
            library.onDataLoaded(id, data);
        }
    }

    void reloadLibs(List<LibrarySortModel> selectedLibs) {
        bridge.reloadLibraries(selectedLibs);
    }


    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.log(TAG, "OnActivityREsult==" + resultCode);
        if (requestCode == LIBSORT_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            ArrayList<LibrarySortModel> selectedLibs = data.getParcelableArrayListExtra
                    (FileConstants.KEY_LIB_SORTLIST);
            if (selectedLibs != null) {
                bridge.reloadLibraries(selectedLibs);
            }
        }
    }


    void loadFileList(String path, Category category) {
        if (path == null) {
            Analytics.getLogger().libDisplayed(category.name());
        } else {
            if (path.equals(STORAGE_EMULATED_0)) {
                Analytics.getLogger().storageDisplayed();
            } else {
                Analytics.getLogger().extStorageDisplayed();
            }
        }
        getDualModeState();
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();
        Bundle args = new Bundle();
        args.putBoolean(FileConstants.KEY_HOME, true);
        args.putSerializable(KEY_CATEGORY, category);
        args.putBoolean(FileConstants.KEY_PREMIUM, isPremium);
        args.putString(FileConstants.KEY_PATH, path);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, isDualModeActive);

        FileList baseFileList = new FileList();
        baseFileList.setDrawerListener(drawerListener);
        baseFileList.setFavoriteListener(favListener);
        baseFileList.setArguments(args);
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
                .exit_to_left);
//        ft.add(R.id.main_container, baseFileList);
//        ft.hide(fragment);
        ft.replace(R.id.main_container, baseFileList);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void setDualMode() {
        isDualModeActive = true;
    }


    private void getDualModeState() {
        isDualModeActive = bridge.getDualModeState() && ((AceActivity)getActivity()).getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    public void onConfigChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        library.onOrientationChanged(newConfig);
    }


    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
    }


    public void removeFavorite(int size) {
        library.removeFavorites(size);
    }

    public void setDrawerListener(DrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }

    public void setFavListener(StoragesUiView.FavoriteOperation favListener) {
        this.favListener = favListener;
    }

    public void hideDualPane() {
        isDualModeActive = false;
    }

    public void updateFavoriteCount(int size) {
        library.updateFavoritesCount(size);
    }

    public void saveLibs(List<LibrarySortModel> librarySortModels) {
        bridge.saveLibs(librarySortModels);
    }
}
