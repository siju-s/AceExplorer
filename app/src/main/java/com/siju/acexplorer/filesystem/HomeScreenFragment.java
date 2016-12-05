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
import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.HomeLibraryInfo;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;

public class HomeScreenFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<ArrayList<FileInfo>>, View.OnClickListener, FileListFragment.RefreshData {

    private View root;
    private int mResourceIds[];
    private String mLabels[];
    private int mCategoryIds[];
    private ArrayList<HomeLibraryInfo> homeLibraryInfoArrayList;
    private ArrayList<HomeLibraryInfo> tempLibraryInfoArrayList;
    private ArrayList<SectionItems> homeStoragesInfoArrayList;
    private final String TAG = this.getClass().getSimpleName();
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private ArrayList<LibrarySortModel> savedLibraries = new ArrayList<>();
    private final int REQUEST_CODE = 1000;
    private BaseActivity mBaseActivity;
    private boolean mIsDualModeEnabled;
    private SharedPreferences mSharedPreferences;
    private int mCurrentOrientation;
    private final Handler handler = new Handler();
    private final UriObserver mUriObserver = new UriObserver(handler);
    private boolean mIsPermissionGranted = true;
    private TableLayout libraryContainer;
    private TableLayout storagesContainer;
    private int mGridColumns;
    private int spacing;
    private boolean mIsThemeDark;
    private AdView mAdView;
    private boolean isPremium;


    @Override
    public void onAttach(Context context) {
        mBaseActivity = (BaseActivity) context;
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
        setHasOptionsMenu(true);
        Logger.log(TAG, "onActivityCreated" + savedInstanceState);

        mCurrentOrientation = getResources().getConfiguration().orientation;
        mIsDualModeEnabled = getArguments().getBoolean(FileConstants.KEY_DUAL_ENABLED, false);
        mIsThemeDark = ThemeUtils.isDarkTheme(getContext());
        homeLibraryInfoArrayList = new ArrayList<>();
        homeStoragesInfoArrayList = new ArrayList<>();
        tempLibraryInfoArrayList = new ArrayList<>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (PermissionUtils.isAtLeastM() && !PermissionUtils.hasRequiredPermissions()) {
            mIsPermissionGranted = false;
        }
        initializeViews();
        initConstants();
        initializeLibraries();
        initializeStorageGroup();
        setupLoaders();
    }

    public void setPermissionGranted() {
        mIsPermissionGranted = true;
        setupLoaders();
    }

    private void registerObservers(Uri uri) {
        // Uri will be null for fav category
        if (uri != null)
            getActivity().getContentResolver().registerContentObserver(uri, true, mUriObserver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.log(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
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


    private void initializeViews() {
        boolean isDarkTheme = ThemeUtils.isDarkTheme(getActivity());
        setGridColumns();
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        libraryContainer = (TableLayout) root.findViewById(R.id.libraryContainer);
        storagesContainer = (TableLayout) root.findViewById(R.id.storagesContainer);

        LinearLayout layoutLibrary = (LinearLayout) root.findViewById(R.id.layoutLibrary);
        LinearLayout layoutStorages = (LinearLayout) root.findViewById(R.id.layoutStorages);
        NestedScrollView nestedScrollViewHome = (NestedScrollView) root.findViewById(R.id.scrollLayoutHome);

        if (isDarkTheme) {
            layoutLibrary.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_colorPrimary));
            layoutStorages.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_colorPrimary));
            nestedScrollViewHome.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_home_bg));
        } else {
            layoutLibrary.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_home_lib));
            layoutStorages.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_home_lib));
            nestedScrollViewHome.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_home_bg));
        }
        isPremium = getArguments() != null && getArguments().getBoolean(FileConstants.KEY_PREMIUM, false);
        if (isPremium) {
            hideAds();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        showAds();
                    }
                }
            }, 2000);

        }

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


    private void getSavedLibraries() {
        savedLibraries = new ArrayList<>();
        savedLibraries = sharedPreferenceWrapper.getLibraries(getActivity());
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
        mCategoryIds = new int[]{FileConstants.CATEGORY.IMAGE.getValue(),
                FileConstants.CATEGORY.AUDIO.getValue(),
                FileConstants.CATEGORY.VIDEO.getValue(),
                FileConstants.CATEGORY.DOCS.getValue(),
                FileConstants.CATEGORY.DOWNLOADS.getValue(),
                FileConstants.CATEGORY.ADD.getValue()};
    }

    private void setupLoaders() {

        if (mIsPermissionGranted) {
            for (int i = 0; i < savedLibraries.size(); i++) {
                int categoryId = savedLibraries.get(i).getCategoryId();
                initLoaders(categoryId);
                registerObservers(getUriForCategory(categoryId));
            }

            ArrayList<FavInfo> mFavList = sharedPreferenceWrapper.getFavorites(getActivity());
            if (mFavList != null && mFavList.size() != 0) {
                for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
                    if (homeLibraryInfoArrayList.get(i).getCategoryId() == FileConstants.CATEGORY
                            .FAVORITES.getValue()) {
                        homeLibraryInfoArrayList.get(i).setCount(mFavList.size());
                        break;
                    }
                }
            }
        }
        inflateLibraryItem();

    }

    public void setDualModeEnabled(boolean isDualModeEnabled) {
        mIsDualModeEnabled = isDualModeEnabled;
    }

    private void initLoaders(int categoryId) {
        if (PermissionUtils.hasRequiredPermissions()) {
            getLoaderManager().initLoader(categoryId, null, this);
        }
    }

    private void restartLoaders(int categoryId) {
        getLoaderManager().restartLoader(categoryId, null, this);
    }

    private void initializeLibraries() {
        boolean mIsFirstRun = mSharedPreferences.getBoolean(BaseActivity.PREFS_FIRST_RUN, true);
        Log.d(TAG, "First run==" + mIsFirstRun);
        if (mIsFirstRun) {
            savedLibraries = new ArrayList<>();
            for (int i = 0; i < mResourceIds.length; i++) {
                homeLibraryInfoArrayList.add(new HomeLibraryInfo(mCategoryIds[i], mLabels[i],
                        mResourceIds[i], 0));
                LibrarySortModel model = new LibrarySortModel();
                model.setCategoryId(mCategoryIds[i]);
                model.setChecked(true);
                model.setLibraryName(mLabels[i]);
                if (model.getCategoryId() != FileConstants.CATEGORY.ADD.getValue()) {
                    savedLibraries.add(model);
                    sharedPreferenceWrapper.addLibrary(getActivity(), model);
                }
            }
            mSharedPreferences.edit().putBoolean(BaseActivity.PREFS_FIRST_RUN, false).apply();
        } else {
            getSavedLibraries();
            if (savedLibraries != null && savedLibraries.size() > 0) {
                for (int i = 0; i < savedLibraries.size(); i++) {

                    String libraryName = savedLibraries.get(i).getLibraryName();
                    int categoryId = savedLibraries.get(i).getCategoryId();
                    int resourceId = getResourceIdForCategory(categoryId);

                    homeLibraryInfoArrayList.add(new HomeLibraryInfo(categoryId,
                            libraryName, resourceId,
                            0));
                }

            }
            addToLibrary();
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
            if (homeLibraryInfoArrayList.get(i).getCategoryId() == FileConstants.CATEGORY.ADD.getValue()) {
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
            libraryItemContainer.setTag(homeLibraryInfoArrayList.get(i).getCategoryId());
            changeColor(imageLibrary, homeLibraryInfoArrayList.get(i).getCategoryId());


        }
        if (pos != 0) {
            libraryContainer.addView(tableRow);
        }

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

    private String roundOffCount(int count) {
        String roundedCount;
        if (count > 99999) {
            roundedCount = 99999 + "+";
        } else {
            roundedCount = "" + count;
        }
        return roundedCount;
    }

    private void changeColor(View itemView, int category) {
        if (mIsThemeDark) {
            switch (category) {
                case 1:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .audio_bg_dark));
                    break;
                case 2:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .video_bg_dark));
                    break;
                case 3:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .image_bg_dark));
                    break;
                case 4:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .docs_bg_dark));
                    break;
                case 5:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .downloads_bg_dark));
                    break;
                case 6:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .add_bg_dark));
                    break;
                case 7:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .compressed_bg_dark));
                    break;
                case 8:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .fav_bg_dark));
                    break;
                case 9:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .pdf_bg_dark));
                    break;
                case 10:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .apps_bg_dark));
                    break;
                case 11:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .large_files_bg_dark));
                    break;

                default:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .colorPrimary));

            }
        } else {
            switch (category) {
                case 1:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .audio_bg));
                    break;
                case 2:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .video_bg));
                    break;
                case 3:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .image_bg));
                    break;
                case 4:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .docs_bg));
                    break;
                case 5:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .downloads_bg));
                    break;
                case 6:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .add_bg));
                    break;
                case 7:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .compressed_bg));
                    break;
                case 8:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .fav_bg));
                    break;
                case 9:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .pdf_bg));
                    break;
                case 10:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .apps_bg));
                    break;
                case 11:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .large_files_bg));
                    break;

                default:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(getContext(), R.color
                            .colorPrimary));


            }
        }
    }


    public void updateFavoritesCount(int count) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            if (homeLibraryInfoArrayList.get(i).getCategoryId() == FileConstants.CATEGORY.FAVORITES.getValue()) {
                int count1 = homeLibraryInfoArrayList.get(i).getCount();
                homeLibraryInfoArrayList.get(i).setCount(count1 + count);
                updateCount(i, count1 + count);
//                homeLibraryAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeFavorites(int count) {
        for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {
            if (homeLibraryInfoArrayList.get(i).getCategoryId() == FileConstants.CATEGORY.FAVORITES.getValue()) {
                int count1 = homeLibraryInfoArrayList.get(i).getCount();
                homeLibraryInfoArrayList.get(i).setCount(count1 - count);
                updateCount(i, count1 - count);
//                homeLibraryAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void addToLibrary() {
        homeLibraryInfoArrayList.add(new HomeLibraryInfo(FileConstants.CATEGORY.ADD.getValue(),
                mLabels[5], getResourceIdForCategory(FileConstants.CATEGORY.ADD
                .getValue()),
                0));
    }

    private Uri getUriForCategory(int categoryId) {
        switch (categoryId) {
            case 1:
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            case 2:
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case 3:
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            case 4:
            case 7:
            case 9:
            case 10:
            case 5:
            case 11:
                return MediaStore.Files.getContentUri("external");
        }
        return null;
    }

    private int getCategoryForUri(Uri uri) {
        final String audioUri = "content://media/external/audio/media";
        final String imageUri = "content://media/external/images/media";
        final String videoUri = "content://media/external/videos/media";
        if (uri.toString().contains(audioUri)) {
            return 1;
        } else if (uri.toString().contains(videoUri)) {
            return 2;
        } else if (uri.toString().contains(imageUri)) {
            return 3;
        } else return 4;

    }


    private int getResourceIdForCategory(int categoryId) {
        switch (categoryId) {
            case 1:
                return R.drawable.ic_library_music;
            case 2:
                return R.drawable.ic_library_videos;
            case 3:
                return R.drawable.ic_library_images;
            case 4:
                return R.drawable.ic_library_docs;
            case 5:
                return R.drawable.ic_library_downloads;
            case 6:
                return R.drawable.ic_library_add;
            case 7:
                return R.drawable.ic_library_compressed;
            case 8:
                return R.drawable.ic_library_favorite;
            case 9:
                return R.drawable.ic_library_pdf;
            case 10:
                return R.drawable.ic_library_apk;
            case 11:
                return R.drawable.ic_library_large;
        }
        return 0;
    }

    private void initializeStorageGroup() {
        homeStoragesInfoArrayList = new ArrayList<>();
        ArrayList<SectionItems> sectionItems = mBaseActivity.getStorageGroupList();
        File rootDir = FileUtils.getRootDirectory().getParentFile();
        for (SectionItems items : sectionItems) {
            if (!items.getPath().equals(FileUtils.getAbsolutePath(rootDir))) {
                homeStoragesInfoArrayList.add(items);
            }
        }
        inflateStoragesItem();
    }

    private void inflateStoragesItem() {
        storagesContainer.removeAllViews();

        for (int i = 0; i < homeStoragesInfoArrayList.size(); i++) {
            RelativeLayout storageItemContainer = (RelativeLayout) View.inflate(getActivity(), R.layout.storage_item,
                    null);
            ProgressBar progressBarSpace = (ProgressBar) storageItemContainer
                    .findViewById(R.id.progressBarSD);
            ImageView imageStorage = (ImageView) storageItemContainer.findViewById(R.id.imageStorage);
            TextView textStorage = (TextView) storageItemContainer.findViewById(R.id.textStorage);
            TextView textSpace = (TextView) storageItemContainer.findViewById(R.id.textSpace);
            View homeStoragesDivider = storageItemContainer.findViewById(R.id.home_storages_divider);

            imageStorage.setImageResource(homeStoragesInfoArrayList.get(i).getIcon());
            textStorage.setText(homeStoragesInfoArrayList.get(i).getFirstLine());
            textSpace.setText(homeStoragesInfoArrayList.get(i).getSecondLine());
            progressBarSpace.setProgress(homeStoragesInfoArrayList.get(i).getProgress());

            storagesContainer.addView(storageItemContainer);
            storageItemContainer.setOnClickListener(this);
            storageItemContainer.setTag(homeStoragesInfoArrayList.get(i).getPath());
            if (i + 1 == homeStoragesInfoArrayList.size()) {
                homeStoragesDivider.setVisibility(View.GONE);
            }

        }


    }

    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "on onCreateLoader--" + id);
        switch (id) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 7:
            case 9:
            case 10:
            case 11:
                return new FileListLoader(this, getContext(), null, id);
            case 5:
                String path = FileUtils.getDownloadsDirectory().getAbsolutePath();
                return new FileListLoader(this, getContext(), path, id);

        }
        return null;

    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        if (data != null) {

            if (data.size() != 0) {
                for (int i = 0; i < homeLibraryInfoArrayList.size(); i++) {

                    if (loader.getId() == FileConstants.CATEGORY.DOWNLOADS.getValue() &&
                            loader.getId() == homeLibraryInfoArrayList.get(i).getCategoryId()) {

                        homeLibraryInfoArrayList.get(i).setCount(data.size());
                        updateCount(i, data.size());
                    } else if (data.get(0).getCategoryId() == homeLibraryInfoArrayList.get(i).getCategoryId()) {
                        Log.d(TAG, "on onLoadFinished--category=" + loader.getId() + "size=" + data.get(0).getCount());
                        homeLibraryInfoArrayList.get(i).setCount(data.get(0).getCount());
                        updateCount(i, data.get(0).getCount());

                    }
                }
            }
//            homeLibraryAdapter.updateAdapter(homeLibraryInfoArrayList);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.log(TAG, "OnActivityREsult==" + resultCode);
        if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            savedLibraries = new ArrayList<>();
            savedLibraries = data.getParcelableArrayListExtra(FileConstants.KEY_LIB_SORTLIST);
            if (savedLibraries != null) {
                tempLibraryInfoArrayList.addAll(homeLibraryInfoArrayList);
                homeLibraryInfoArrayList = new ArrayList<>();

                for (int i = 0; i < savedLibraries.size(); i++) {

                    String libraryName = savedLibraries.get(i).getLibraryName();
                    int categoryId = savedLibraries.get(i).getCategoryId();
                    int resourceId = getResourceIdForCategory(categoryId);
                    int count = 0;

                    for (int j = 0; j < tempLibraryInfoArrayList.size(); j++) {
                        if (tempLibraryInfoArrayList.get(j).getCategoryId() == savedLibraries.get
                                (i).getCategoryId()) {
                            count = tempLibraryInfoArrayList.get(j).getCount();
                            break;
                        }
                    }
                    homeLibraryInfoArrayList.add(new HomeLibraryInfo(categoryId,
                            libraryName, resourceId,
                            count));

                }
                setupLoaders();
                addToLibrary();
                inflateLibraryItem();
//                homeLibraryAdapter.updateAdapter(homeLibraryInfoArrayList);

                tempLibraryInfoArrayList = new ArrayList<>();
                tempLibraryInfoArrayList.addAll(homeLibraryInfoArrayList);
                sharedPreferenceWrapper.saveLibrary(getActivity(), savedLibraries);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);

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
    public void onDetach() {
        super.onDetach();
        mBaseActivity = null;
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

    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof Integer) {

            int categoryId = (int) view.getTag();
            if (categoryId != FileConstants.CATEGORY.ADD.getValue()) {
//                homeLibraryInfoArrayList.get(position).getCategoryName();
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                Bundle args = new Bundle();
                args.putBoolean(FileConstants.KEY_HOME, true);
                args.putInt(FileConstants.KEY_CATEGORY, categoryId);
                args.putBoolean(FileConstants.KEY_PREMIUM, isPremium);
                String path = null;
                if (categoryId == FileConstants
                        .CATEGORY.DOWNLOADS.getValue()) {
                    path = FileUtils.getDownloadsDirectory().getAbsolutePath();
                    args.putString(FileConstants.KEY_PATH, path);
                }
                mBaseActivity.setCurrentCategory(categoryId);
                mBaseActivity.setIsFromHomePage();
                mBaseActivity.addToBackStack(path, categoryId);

                FileListFragment fileListFragment = new FileListFragment();
                fileListFragment.setArguments(args);
                fileListFragment.setRefreshData(this);
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
                        .exit_to_left);
                ft.add(R.id.main_container, fileListFragment);
                ft.hide(HomeScreenFragment.this);
                ft.addToBackStack(null);
                ft.commitAllowingStateLoss();
            } else {
                Intent intent = new Intent(getActivity(), LibrarySortActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else {
            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            String currentDir = (String) view.getTag();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, true);
            args.putString(FileConstants.KEY_PATH, currentDir);
            args.putBoolean(FileConstants.KEY_PREMIUM, isPremium);
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            fileListFragment.setRefreshData(this);
//            ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim
// .slide_out_up);
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
                    .exit_to_left);
            ft.add(R.id.main_container, fileListFragment);
            ft.hide(HomeScreenFragment.this);
            ft.commitAllowingStateLoss();
            mBaseActivity.setCurrentCategory(FileConstants.CATEGORY.FILES.getValue());
            mBaseActivity.setDir(currentDir, false);
            mBaseActivity.addToBackStack(currentDir, FileConstants.CATEGORY.FILES.getValue());
            if (mIsDualModeEnabled) {
                mBaseActivity.toggleDualPaneVisibility(true);
                mBaseActivity.createDualFragment();
                mBaseActivity.setDir(currentDir, true);
                mBaseActivity.setCurrentCategory(FileConstants.CATEGORY.FILES.getValue());
                mBaseActivity.addToBackStack(currentDir, FileConstants.CATEGORY.FILES.getValue());
            }
        }
    }

    @Override
    public void refresh(int category) {
        Logger.log(TAG, "REFRESH");
        restartLoaders(category);
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
            if (!uri.equals(mUri) && PermissionUtils.hasRequiredPermissions()) {
                mUri = uri;
                int categoryId = getCategoryForUri(uri);
                Logger.log(TAG, "Observer Onchange" + uri + " cat id=" + categoryId);

                if (categoryId == 4) {
                    for (int i = 0; i < savedLibraries.size(); i++) {
                        int id = savedLibraries.get(i).getCategoryId();
                        if (id != 1 && id != 2 && id != 3 && id != 8) {
                            Logger.log(TAG, "Observer savedib cat id=" + id);
                            restartLoaders(id);
                        }
                    }
                } else {
                    restartLoaders(categoryId);
                }
            }
        }
    }


}
