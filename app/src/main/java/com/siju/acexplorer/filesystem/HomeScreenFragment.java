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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.siju.acexplorer.AceActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.billing.BillingHelper;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.groups.StoragesGroup;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.HomeLibraryInfo;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;
import com.siju.acexplorer.filesystem.storage.StorageUtils;
import com.siju.acexplorer.filesystem.theme.ThemeUtils;
import com.siju.acexplorer.filesystem.theme.Themes;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.permission.PermissionUtils;

import java.util.ArrayList;

import static android.R.attr.id;
import static com.siju.acexplorer.filesystem.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.filesystem.groups.Category.ADD;
import static com.siju.acexplorer.filesystem.groups.Category.AUDIO;
import static com.siju.acexplorer.filesystem.groups.Category.DOCS;
import static com.siju.acexplorer.filesystem.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.filesystem.groups.Category.FAVORITES;
import static com.siju.acexplorer.filesystem.groups.Category.FILES;
import static com.siju.acexplorer.filesystem.groups.Category.IMAGE;
import static com.siju.acexplorer.filesystem.groups.Category.VIDEO;

public class HomeScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>, View.OnClickListener, BaseFileList.RefreshData {


    private static final int COUNT_ZERO = 0;
    private View root;
    private int mResourceIds[];
    private String mLabels[];
    private Category categories[];
    private ArrayList<HomeLibraryInfo> homeLibraryInfoArrayList;
    private ArrayList<HomeLibraryInfo> tempLibraryInfoArrayList;
    private ArrayList<SectionItems> storagesList;
    private final String TAG = this.getClass().getSimpleName();
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private final int REQUEST_CODE = 1000;
    private AceActivity aceActivity;
    private SharedPreferences mSharedPreferences;
    private int mCurrentOrientation;
    private final Handler handler = new Handler();
    private final UriObserver mUriObserver = new UriObserver(handler);
    private TableLayout libraryContainer;
    private TableLayout storagesContainer;
    private int mGridColumns;
    private int spacing;
    private AdView mAdView;
    private boolean isPremium = true;
    private LinearLayout layoutLibrary;
    private LinearLayout layoutStorages;
    private NestedScrollView nestedScrollViewHome;
    private Themes theme;
    private Toolbar toolbar;
    private boolean isDualModeActive;


    @Override
    public void onAttach(Context context) {
        aceActivity = (AceActivity) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.homescreen, container, false);
        Logger.log(TAG, "onCreateView" + savedInstanceState);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        Logger.log(TAG, "onActivityCreated" + savedInstanceState);

        mCurrentOrientation = getResources().getConfiguration().orientation;
        homeLibraryInfoArrayList = new ArrayList<>();
        storagesList = new ArrayList<>();
        tempLibraryInfoArrayList = new ArrayList<>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        initializeViews();
        initializeListeners();
        initConstants();
        checkBillingStatus();
        setGridColumns();
        setTheme();

        initializeLibraries();
        initializeStorageGroup();
        setupLibraryData();
        inflateLibraryItem();
    }

    private void initializeViews() {
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        libraryContainer = (TableLayout) root.findViewById(R.id.libraryContainer);
        storagesContainer = (TableLayout) root.findViewById(R.id.storagesContainer);
        layoutLibrary = (LinearLayout) root.findViewById(R.id.layoutLibrary);
        layoutStorages = (LinearLayout) root.findViewById(R.id.layoutStorages);
        nestedScrollViewHome = (NestedScrollView) root.findViewById(R.id.scrollLayoutHome);
        toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        aceActivity.syncDrawerState();
    }

    private void initializeListeners() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aceActivity.openDrawer();
            }
        });
    }

    private void initConstants() {
        mResourceIds = new int[]{R.drawable.ic_library_images, R.drawable.ic_library_music,
                R.drawable.ic_library_videos, R.drawable.ic_library_docs,
                R.drawable.ic_library_downloads, R.drawable.ic_library_add};
        mLabels = new String[]{getActivity().getString(R.string
                .nav_menu_image), getActivity().getString(R.string
                .nav_menu_music), getActivity().getString(R.string
                .nav_menu_video), getActivity().getString(R.string
                .home_docs), getActivity().getString(R.string
                .downloads), getActivity().getString(R.string
                .home_add)};
        categories = new Category[]{IMAGE, AUDIO, VIDEO,
                DOCS, DOWNLOADS, ADD};
    }


    private void checkBillingStatus() {
        BillingStatus billingStatus = BillingHelper.getInstance().getInAppBillingStatus();
        switch (billingStatus) {
            case PREMIUM:
                hideAds();
                break;
            case UNSUPPORTED:
            case FREE:
                if (getActivity() != null && !getActivity().isFinishing()) {
                    showAds();
                }
                break;
        }
    }

    private void setGridColumns() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        int libWidth = getResources().getDimensionPixelSize(R.dimen.home_library_width) +
                2 * getResources().getDimensionPixelSize(R.dimen.drawer_item_margin) +
                getResources().getDimensionPixelSize(R.dimen.padding_5);

        mGridColumns = getResources().getInteger(R.integer.homescreen_columns);//width / libWidth;//getResources()
        // .getInteger(R.integer.homescreen_columns);
//        mGridColumns = Math.min(mGridColumns,homeLibraryInfoArrayList.size());
        spacing = (width - mGridColumns * libWidth) / mGridColumns;
        Logger.log(TAG, "Grid columns=" + mGridColumns + " width=" + width + " liub size=" + libWidth + "space=" +
                spacing);
    }

    private void setTheme() {
        theme = Themes.getTheme(ThemeUtils.getTheme(getContext()));
        switch (theme) {
            case DARK:
                layoutLibrary.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_colorPrimary));
                layoutStorages.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_colorPrimary));
                nestedScrollViewHome.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_home_bg));
                break;
            case LIGHT:
                layoutLibrary.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_home_lib));
                layoutStorages.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_home_lib));
                nestedScrollViewHome.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_home_bg));
                break;
        }
    }

    private void initializeLibraries() {
        boolean mIsFirstRun = mSharedPreferences.getBoolean(AceActivity.PREFS_FIRST_RUN, true);

        if (mIsFirstRun) {
            addDefaultLibraries();
        } else {
            addSavedLibraries();
            addLibrary();
        }
    }

    private void addDefaultLibraries() {
        for (int i = 0; i < mResourceIds.length; i++) {
            addToLibrary(new HomeLibraryInfo(categories[i], mLabels[i], mResourceIds[i], COUNT_ZERO));
            LibrarySortModel model = new LibrarySortModel();
            model.setCategory(categories[i]);
            model.setLibraryName(mLabels[i]);
            model.setChecked(true);
            if (!model.getCategory().equals(ADD)) {
                sharedPreferenceWrapper.addLibrary(getActivity(), model);
            }
        }
        mSharedPreferences.edit().putBoolean(AceActivity.PREFS_FIRST_RUN, false).apply();
    }


    private void addSavedLibraries() {
        ArrayList<LibrarySortModel> savedLibraries = sharedPreferenceWrapper.getLibraries(getActivity());
        for (int i = 0; i < savedLibraries.size(); i++) {
            Category category = savedLibraries.get(i).getCategory();
            int resourceId = getResourceIdForCategory(category);
            addToLibrary(new HomeLibraryInfo(category, savedLibraries.get(i).getLibraryName(), resourceId,
                    COUNT_ZERO));
        }
    }


    private void addLibrary() {
        addToLibrary(new HomeLibraryInfo(ADD, mLabels[5], getResourceIdForCategory(ADD), COUNT_ZERO));
    }

    private void addToLibrary(HomeLibraryInfo homeLibraryInfo) {
        homeLibraryInfoArrayList.add(homeLibraryInfo);
    }


    private void initializeStorageGroup() {
        storagesList = StoragesGroup.getInstance().getStoragesList();
        inflateStoragesItem();
    }


    private void inflateStoragesItem() {
        storagesContainer.removeAllViews();

        for (int i = 0; i < storagesList.size(); i++) {
            RelativeLayout storageItemContainer = (RelativeLayout) View.inflate(getActivity(), R.layout.storage_item,
                    null);
            ProgressBar progressBarSpace = (ProgressBar) storageItemContainer
                    .findViewById(R.id.progressBarSD);
            ImageView imageStorage = (ImageView) storageItemContainer.findViewById(R.id.imageStorage);
            TextView textStorage = (TextView) storageItemContainer.findViewById(R.id.textStorage);
            TextView textSpace = (TextView) storageItemContainer.findViewById(R.id.textSpace);
            View homeStoragesDivider = storageItemContainer.findViewById(R.id.home_storages_divider);

            imageStorage.setImageResource(storagesList.get(i).getIcon());
            textStorage.setText(storagesList.get(i).getFirstLine());
            textSpace.setText(storagesList.get(i).getSecondLine());
            progressBarSpace.setProgress(storagesList.get(i).getProgress());

            storagesContainer.addView(storageItemContainer);
            storageItemContainer.setOnClickListener(this);
            storageItemContainer.setTag(storagesList.get(i).getPath());
            if (i + 1 == storagesList.size()) {
                homeStoragesDivider.setVisibility(View.GONE);
            }

        }
    }


    private void setupLibraryData() {

        if (hasStoragePermission()) {
            for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
                Category category = homeLibraryInfoArrayList.get(i).getCategory();
                initLoaders(category);
                registerObservers(getUriForCategory(category));
            }
            setupFavorites();
        }
    }

    private void initLoaders(Category category) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_CATEGORY, category);
        getLoaderManager().initLoader(category.getValue(), args, this);
    }

    private void registerObservers(Uri uri) {
        // Uri will be null for fav category
        if (uri != null) {
            getActivity().getContentResolver().registerContentObserver(uri, true, mUriObserver);
        }
    }


    private void setupFavorites() {
        ArrayList<FavInfo> favorites = sharedPreferenceWrapper.getFavorites(getActivity());
        if (favorites.size() > 0) {
            for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
                if (homeLibraryInfoArrayList.get(i).getCategory().equals(FAVORITES)) {
                    homeLibraryInfoArrayList.get(i).setCount(favorites.size());
                    break;
                }
            }
        }
    }

    private void inflateLibraryItem() {
        libraryContainer.removeAllViews();
        TableRow tableRow = new TableRow(getActivity());
        int pos = 0;
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {

            RelativeLayout libraryItemContainer = (RelativeLayout) View.inflate(getActivity(), R.layout.library_item,
                    null);
            ImageView imageLibrary = (ImageView) libraryItemContainer.findViewById(R.id.imageLibrary);
            TextView textLibraryName = (TextView) libraryItemContainer.findViewById(R.id.textLibrary);
            TextView textCount = (TextView) libraryItemContainer.findViewById(R.id.textCount);
            imageLibrary.setImageResource(homeLibraryInfoArrayList.get(i).getResourceId());
            textLibraryName.setText(homeLibraryInfoArrayList.get(i).getCategoryName());
            if (homeLibraryInfoArrayList.get(i).getCategory().equals(ADD)) {
                textCount.setVisibility(View.GONE);
            } else {
                textCount.setVisibility(View.VISIBLE);
            }
            libraryItemContainer.setPadding(0, 0, spacing, 0);
 /*           RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageLibrary.getLayoutParams();
            layoutParams.setMargins(0,0,spacing,0);*/
            textCount.setText(roundOffCount(homeLibraryInfoArrayList.get(i).getCount()));
            int j = i + 1;
            if (j % mGridColumns == 0) {
                tableRow.addView(libraryItemContainer);
                libraryContainer.addView(tableRow);
                tableRow = new TableRow(getActivity());
                pos = 0;
            } else {
                tableRow.addView(libraryItemContainer);
                pos++;
            }
            libraryItemContainer.setOnClickListener(this);
            libraryItemContainer.setTag(homeLibraryInfoArrayList.get(i).getCategory());
            changeColor(imageLibrary, homeLibraryInfoArrayList.get(i).getCategory());
        }
        if (pos != 0) {
            libraryContainer.addView(tableRow);
        }

    }


    private String roundOffCount(int count) {
        String roundedCount;
        if (count > 99999) {
            roundedCount = 99999 + "+";
        } else {
            roundedCount = "" + count;
        }
        return roundedCount;
    }

    private void changeColor(View itemView, Category category) {
        if (theme == Themes.DARK) {
            switch (category) {
                case AUDIO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .audio_bg_dark));
                    break;
                case VIDEO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .video_bg_dark));
                    break;
                case IMAGE:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .image_bg_dark));
                    break;
                case DOCS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .docs_bg_dark));
                    break;
                case DOWNLOADS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .downloads_bg_dark));
                    break;
                case ADD:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .add_bg_dark));
                    break;
                case COMPRESSED:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .compressed_bg_dark));
                    break;
                case FAVORITES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .fav_bg_dark));
                    break;
                case PDF:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .pdf_bg_dark));
                    break;
                case APPS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .apps_bg_dark));
                    break;
                case LARGE_FILES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .large_files_bg_dark));
                    break;

                default:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .colorPrimary));

            }
        } else {
            switch (category) {
                case AUDIO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .audio_bg));
                    break;
                case VIDEO:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .video_bg));
                    break;
                case IMAGE:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .image_bg));
                    break;
                case DOCS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .docs_bg));
                    break;
                case DOWNLOADS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .downloads_bg));
                    break;
                case ADD:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .add_bg));
                    break;
                case COMPRESSED:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .compressed_bg));
                    break;
                case FAVORITES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .fav_bg));
                    break;
                case PDF:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .pdf_bg));
                    break;
                case APPS:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .apps_bg));
                    break;
                case LARGE_FILES:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .large_files_bg));
                    break;

                default:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .colorPrimary));
            }
        }
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof Category) {

            Category category = (Category) tag;
            if (isAddCategory(category)) {
                Intent intent = new Intent(getActivity(), LibrarySortActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            } else if (category.equals(DOWNLOADS)) {
                displayFileListFrag(StorageUtils.getDownloadsDirectory(), category);
            } else {
                displayFileListFrag(null, category);
            }

        } else {
            displayFileListFrag((String) tag, FILES);
        }
    }

    public void onPermissionGranted() {
        setupLibraryData();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.log(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    private boolean hasStoragePermission() {
        return PermissionUtils.hasStoragePermission();
    }

    private void hideAds() {
        LinearLayout adviewLayout = (LinearLayout) root.findViewById(R.id.adviewLayout);
        if (adviewLayout.getChildCount() != 0) {
            adviewLayout.removeView(mAdView);
        }

    }

    private void showAds() {
        // DYNAMICALLY CREATE AD START
        LinearLayout adviewLayout = (LinearLayout) root.findViewById(R.id.adviewLayout);
        // Create an ad.
        if (mAdView == null) {
            mAdView = new AdView(getActivity().getApplicationContext());
            mAdView.setAdSize(AdSize.BANNER);
            mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
            // DYNAMICALLY CREATE AD END
            AdRequest adRequest = new AdRequest.Builder().build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
            // Add the AdView to the view hierarchy. The view will have no size until the ad is loaded.
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


    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    private void restartLoaders(Category category) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_CATEGORY, category);
        getLoaderManager().restartLoader(category.getValue(), args, this);
    }


    private void updateCount(int index, int count) {

        TableRow tableRow = (TableRow) libraryContainer.getChildAt((index / mGridColumns));
        int childIndex;
        if (index < mGridColumns) {
            childIndex = index;
        } else {
            childIndex = (index % mGridColumns);
        }
        RelativeLayout container = (RelativeLayout) tableRow.getChildAt(childIndex);
        TextView textCount = (TextView) container.findViewById(R.id.textCount);
        textCount.setText(roundOffCount(count));

    }


    public void updateFavoritesCount(int count) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            if (isFavoritesCategory(homeLibraryInfoArrayList.get(i).getCategory())) {
                int count1 = homeLibraryInfoArrayList.get(i).getCount();
                homeLibraryInfoArrayList.get(i).setCount(count1 + count);
                updateCount(i, count1 + count);
                break;
            }
        }
    }

    public void removeFavorites(int count) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            if (isFavoritesCategory(homeLibraryInfoArrayList.get(i).getCategory())) {
                int count1 = homeLibraryInfoArrayList.get(i).getCount();
                homeLibraryInfoArrayList.get(i).setCount(count1 - count);
                updateCount(i, count1 - count);
                break;
            }
        }
    }


    private Uri getUriForCategory(Category category) {
        switch (category) {
            case AUDIO:
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            case VIDEO:
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case IMAGE:
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            case DOCS:
            case DOWNLOADS:
            case COMPRESSED:
            case PDF:
            case APPS:
            case LARGE_FILES:
                return MediaStore.Files.getContentUri("external");
        }
        return null;
    }

    private Category getCategoryForUri(Uri uri) {
        final String audioUri = "content://media/external/audio/media";
        final String imageUri = "content://media/external/images/media";
        final String videoUri = "content://media/external/videos/media";
        if (uri.toString().contains(audioUri)) {
            return AUDIO;
        } else if (uri.toString().contains(videoUri)) {
            return VIDEO;
        } else if (uri.toString().contains(imageUri)) {
            return IMAGE;
        } else return DOCS;

    }


    private int getResourceIdForCategory(Category categoryId) {
        switch (categoryId) {
            case AUDIO:
                return R.drawable.ic_library_music;
            case VIDEO:
                return R.drawable.ic_library_videos;
            case IMAGE:
                return R.drawable.ic_library_images;
            case DOCS:
                return R.drawable.ic_library_docs;
            case DOWNLOADS:
                return R.drawable.ic_library_downloads;
            case ADD:
                return R.drawable.ic_library_add;
            case COMPRESSED:
                return R.drawable.ic_library_compressed;
            case FAVORITES:
                return R.drawable.ic_library_favorite;
            case PDF:
                return R.drawable.ic_library_pdf;
            case APPS:
                return R.drawable.ic_library_apk;
            case LARGE_FILES:
                return R.drawable.ic_library_large;
        }
        return 0;
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "on onCreateLoader--" + id);
        Category category = (Category) args.getSerializable(KEY_CATEGORY);
        String path = null;
        switch (id) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 7:
            case 9:
            case 10:
            case 11:
                return new FileListLoader(this, path, category, false);
            case 5:
                path = StorageUtils.getDownloadsDirectory();
                return new FileListLoader(this, path, category, false);
        }
        return null;

    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        if (data != null) {

            if (data.size() > 0) {
                for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {

                    if (loader.getId() == DOWNLOADS.getValue() &&
                            loader.getId() == homeLibraryInfoArrayList.get(i).getCategory().getValue()) {

                        homeLibraryInfoArrayList.get(i).setCount(data.size());
                        updateCount(i, data.size());
                    } else if (data.get(0).getCategory().equals(homeLibraryInfoArrayList.get(i).getCategory())) {
                        Log.d(TAG, "on onLoadFinished--category=" + loader.getId() + "size=" + data.get(0).getCount());
                        homeLibraryInfoArrayList.get(i).setCount(data.get(0).getCount());
                        updateCount(i, data.get(0).getCount());

                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.log(TAG, "OnActivityREsult==" + resultCode);
        if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            ArrayList<LibrarySortModel> selectedLibs = data.getParcelableArrayListExtra(FileConstants.KEY_LIB_SORTLIST);
            if (selectedLibs != null) {

                refreshLibraryData(selectedLibs);
                setupLibraryData();
                addLibrary();
                inflateLibraryItem();
                sharedPreferenceWrapper.saveLibrary(getActivity(), selectedLibs);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void refreshLibraryData(ArrayList<LibrarySortModel> selectedLibs) {
        tempLibraryInfoArrayList = new ArrayList<>();
        tempLibraryInfoArrayList.addAll(homeLibraryInfoArrayList);
        homeLibraryInfoArrayList = new ArrayList<>();
        for (int i = 0; i < selectedLibs.size(); i++) {

            Category category = selectedLibs.get(i).getCategory();
            int resourceId = getResourceIdForCategory(category);
            String categoryName = selectedLibs.get(i).getLibraryName();
            int count = 0;

            for (int j = 0; j < tempLibraryInfoArrayList.size(); j++) {
                if (tempLibraryInfoArrayList.get(j).getCategory().equals(selectedLibs.get
                        (i).getCategory())) {
                    count = tempLibraryInfoArrayList.get(j).getCount();
                    break;
                }
            }
            homeLibraryInfoArrayList.add(new HomeLibraryInfo(category, categoryName, resourceId, count));
        }
    }

    private boolean isAddCategory(Category category) {
        return category.equals(ADD);
    }

    private boolean isFavoritesCategory(Category category) {
        return category.equals(FAVORITES);
    }

    private void displayFileListFrag(String path, Category category) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();
        Bundle args = new Bundle();
        args.putBoolean(FileConstants.KEY_HOME, true);
        args.putSerializable(KEY_CATEGORY, category);
        args.putBoolean(FileConstants.KEY_PREMIUM, isPremium);
        args.putString(FileConstants.KEY_PATH, path);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, isDualModeActive);

/*        String path = null;
        if (categoryId == FileConstants
                .CATEGORY.DOWNLOADS.getValue()) {
            path = FileUtils.getDownloadsDirectory().getAbsolutePath();
            args.putString(FileConstants.KEY_PATH, path);
        }*/
//        aceActivity.setCurrentCategory(categoryId);
//        aceActivity.setIsFromHomePage();
//        aceActivity.addToBackStack(path, categoryId);

        FileList baseFileList = new FileList();
        baseFileList.setArguments(args);
        baseFileList.setRefreshData(this);
//        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
//                .exit_to_left);
        ft.replace(R.id.main_container, baseFileList);
        ft.addToBackStack(null);
        ft.commit();

    }

    public void setDualMode(boolean value) {
        isDualModeActive = value;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        Logger.log(TAG, "onConfigurationChanged " + newConfig.orientation);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            setGridColumns();
            inflateLibraryItem();
        }
    }

    @Override
    public void refresh(Category category) {
        Logger.log(TAG, "REFRESH");
        restartLoaders(category);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        aceActivity = null;
    }

    @Override
    public void onDestroyView() {
        Logger.log(TAG, "onDestroyView");
        getActivity().getContentResolver().unregisterContentObserver(mUriObserver);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }


    private class UriObserver extends ContentObserver {
        private Uri mUri;

        UriObserver(Handler handler) {

            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (!uri.equals(mUri) && hasStoragePermission()) {
                mUri = uri;
                Category category = getCategoryForUri(uri);
                Logger.log(TAG, "Observer Onchange" + uri + " cat id=" + category);

                if (category == DOCS) {
                    for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
                        Category category1 = homeLibraryInfoArrayList.get(i).getCategory();
                        if (isCategoryGenericUri(category1)) {
                            Logger.log(TAG, "Observer savedib cat id=" + id);
                            restartLoaders(category1);
                        }
                    }
                } else {
                    restartLoaders(category);
                }
            }
        }
    }

    private boolean isCategoryGenericUri(Category category) {
        return !category.equals(AUDIO) && !category.equals(IMAGE) &&
                !category.equals(VIDEO) && !category.equals(FAVORITES);
    }


}
