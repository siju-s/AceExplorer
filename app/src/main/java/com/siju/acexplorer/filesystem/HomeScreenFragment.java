package com.siju.acexplorer.filesystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.HomeLibraryInfo;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;
import com.siju.acexplorer.filesystem.ui.DividerItemDecoration;
import com.siju.acexplorer.filesystem.ui.HomeScreenGridLayoutManager;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;

public class HomeScreenFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<ArrayList<FileInfo>> {

    private View root;
    private int mResourceIds[];
    private String mLabels[];
    private int mCategoryIds[];
    private RecyclerView recyclerViewLibrary;
    private RecyclerView recyclerViewStorages;
    private HomeLibraryAdapter homeLibraryAdapter;
    private HomeStoragesAdapter homeStoragesAdapter;
    private ArrayList<HomeLibraryInfo> homeLibraryInfoArrayList;
    private ArrayList<HomeLibraryInfo> tempLibraryInfoArrayList;
    private ArrayList<SectionItems> homeStoragesInfoArrayList;
    public String STORAGE_INTERNAL, STORAGE_EXTERNAL;
    private final String TAG = this.getClass().getSimpleName();
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private ArrayList<LibrarySortModel> savedLibraries = new ArrayList<>();
    private final int REQUEST_CODE = 1000;
    private BaseActivity mBaseActivity;
    private boolean mIsDualModeEnabled;
    private SharedPreferences mSharedPreferences;
    private int mCurrentOrientation;
    private Handler handler = new Handler();
    private UriObserver mUriObserver = new UriObserver(handler);
    private boolean mIsPermissionGranted = true;


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

        homeLibraryAdapter = new HomeLibraryAdapter(getActivity(), homeLibraryInfoArrayList);
        homeStoragesAdapter = new HomeStoragesAdapter(getActivity(), homeStoragesInfoArrayList);
        recyclerViewLibrary.setAdapter(homeLibraryAdapter);
        recyclerViewStorages.setAdapter(homeStoragesAdapter);

        setupLoaders();
        initListeners();
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
        int mGridColumns = getResources().getInteger(R.integer.homescreen_columns);
//        Logger.log(TAG,"Grid columns="+mGridColumns);
        GridLayoutManager gridLayoutManagerLibrary = new HomeScreenGridLayoutManager(getActivity(), mGridColumns);
        recyclerViewLibrary.setLayoutManager(gridLayoutManagerLibrary);
    }


    private void initializeViews() {
        boolean isDarkTheme = ThemeUtils.isDarkTheme(getActivity());
        recyclerViewLibrary = (RecyclerView) root.findViewById(R.id.recyclerViewLibrary);
        recyclerViewStorages = (RecyclerView) root.findViewById(R.id.recyclerViewStorages);
        recyclerViewLibrary.setHasFixedSize(true);
        recyclerViewStorages.setHasFixedSize(true);
        LinearLayoutManager llmStorage = new LinearLayoutManager(getActivity());
        setGridColumns();
        recyclerViewLibrary.setItemAnimator(new DefaultItemAnimator());
        recyclerViewStorages.setLayoutManager(llmStorage);
        recyclerViewStorages.setItemAnimator(new DefaultItemAnimator());
        recyclerViewStorages.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                .VERTICAL, isDarkTheme));
        sharedPreferenceWrapper = new SharedPreferenceWrapper();

        CardView cardLibrary = (CardView) root.findViewById(R.id.cardLibrary);
        CardView cardStorage = (CardView) root.findViewById(R.id.cardStorage);
        NestedScrollView nestedScrollViewHome = (NestedScrollView) root.findViewById(R.id.scrollLayoutHome);

        if (isDarkTheme) {
            cardLibrary.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_colorPrimary));
            cardStorage.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_colorPrimary));
            nestedScrollViewHome.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_home_bg));
        }
    }


    private void getSavedLibraries() {
        savedLibraries = new ArrayList<>();
        savedLibraries = sharedPreferenceWrapper.getLibraries(getActivity());

    }

    private void initListeners() {
        homeLibraryAdapter.setOnItemClickListener(new HomeLibraryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int categoryId = homeLibraryInfoArrayList.get(position).getCategoryId();
                if (categoryId != FileConstants.CATEGORY.ADD.getValue()) {
                    homeLibraryInfoArrayList.get(position).getCategoryName();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager()
                            .beginTransaction();
                    Bundle args = new Bundle();
                    args.putBoolean(FileConstants.KEY_HOME, true);
                    args.putInt(FileConstants.KEY_CATEGORY, categoryId);
                    String path = null;
                    if (homeLibraryInfoArrayList.get(position).getCategoryId() == FileConstants
                            .CATEGORY.DOWNLOADS.getValue()) {
                        path = FileUtils.getDownloadsDirectory().getAbsolutePath();
                        args.putString(FileConstants.KEY_PATH, path);
                    }
                    mBaseActivity.setCurrentCategory(categoryId);
                    mBaseActivity.setIsFromHomePage(true);
                    mBaseActivity.addToBackStack(path, categoryId);

                    FileListFragment fileListFragment = new FileListFragment();
                    fileListFragment.setArguments(args);
                    ft.add(R.id.main_container, fileListFragment);
                    ft.hide(HomeScreenFragment.this);
                    ft.addToBackStack(null);
                    ft.commitAllowingStateLoss();
                } else {
                    Intent intent = new Intent(getActivity(), LibrarySortActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }

            }
        });

        homeStoragesAdapter.setOnItemClickListener(new HomeStoragesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                String currentDir = homeStoragesInfoArrayList.get(position).getPath();
                Bundle args = new Bundle();
                args.putBoolean(FileConstants.KEY_HOME, true);
                args.putString(FileConstants.KEY_PATH, currentDir);
                FileListFragment fileListFragment = new FileListFragment();
                fileListFragment.setArguments(args);
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
        mCategoryIds = new int[]{FileConstants.CATEGORY.IMAGE.getValue(),
                FileConstants.CATEGORY.AUDIO.getValue(),
                FileConstants.CATEGORY.VIDEO.getValue(),
                FileConstants.CATEGORY.DOCS.getValue(),
                FileConstants.CATEGORY.DOWNLOADS.getValue(),
                FileConstants.CATEGORY.ADD.getValue()};
        STORAGE_INTERNAL = getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = getResources().getString(R.string.nav_menu_ext_storage);
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
        boolean mIsFirstRun = mSharedPreferences.getBoolean
                (BaseActivity.PREFS_FIRST_RUN, true);
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
                    } else if (data.get(0).getCategoryId() == homeLibraryInfoArrayList.get(i).getCategoryId()) {
                        Log.d(TAG, "on onLoadFinished--category=" + loader.getId() + "size=" + data.get(0).getCount());
                        homeLibraryInfoArrayList.get(i).setCount(data.get(0).getCount());
                    }
                }
            }
            homeLibraryAdapter.updateAdapter(homeLibraryInfoArrayList);
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
                homeLibraryAdapter.updateAdapter(homeLibraryInfoArrayList);

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


    class UriObserver extends ContentObserver {
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
            if (!uri.equals(mUri)) {
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
