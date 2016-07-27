package com.siju.filemanager;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siju.filemanager.common.Logger;
import com.siju.filemanager.common.SharedPreferenceWrapper;
import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileListAdapter;
import com.siju.filemanager.filesystem.FileListDualFragment;
import com.siju.filemanager.filesystem.FileListFragment;
import com.siju.filemanager.filesystem.HomeScreenFragment;
import com.siju.filemanager.filesystem.StoragesFragment;
import com.siju.filemanager.filesystem.model.FavInfo;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.utils.FileUtils;
import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;
import com.siju.filemanager.settings.SettingsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.siju.filemanager.filesystem.utils.FileUtils.getInternalStorage;


public class BaseActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        StoragesFragment.ActionModeCommunicator {

    private final String TAG = this.getClass().getSimpleName();
    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    String[] listDataHeader;
    List<String> mListHeader;
    HashMap<String, List<String>> listDataChild;
    ArrayList<SectionGroup> totalGroup;
    public static final String ACTION_VIEW_FOLDER_LIST = "folder_list";
    public static final String ACTION_DUAL_VIEW_FOLDER_LIST = "dual_folder_list";
    public static final String ACTION_DUAL_PANEL = "ACTION_DUAL_PANEL";
    public static final String ACTION_MAIN = "android.intent.action.MAIN";
    public static final String ACTION_VIEW_MODE = "view_mode";
    public static final String ACTION_GROUP_POS = "group_pos";
    public static final String ACTION_CHILD_POS = "child_pos";


    private DrawerLayout drawerLayout;
    private RelativeLayout relativeLayoutDrawerPane;
    private String mCurrentDir = getInternalStorage().getAbsolutePath();
    private String mCurrentDirDualPane = getInternalStorage().getAbsolutePath();
    public String STORAGE_ROOT, STORAGE_INTERNAL, STORAGE_EXTERNAL, DOWNLOADS, IMAGES, VIDEO,
            MUSIC, DOCS, SETTINGS,
            RATE;
    private SparseBooleanArray mSelectedItemPositions = new SparseBooleanArray();
    private static final int PASTE_OPERATION = 1;
    private static final int DELETE_OPERATION = 2;
    private static final int ARCHIVE_OPERATION = 3;
    private static final int DECRYPT_OPERATION = 4;

    private boolean mIsMoveOperation = false;
    private ArrayList<FileInfo> mFileList;
    private HashMap<String, Integer> mPathActionMap = new HashMap<>();
    private int mPasteAction = FileUtils.ACTION_NONE;
    private boolean isPasteConflictDialogShown;
    private String mSourceFilePath = null;
    private ArrayList<String> tempSourceFile = new ArrayList<>();
    private int tempConflictCounter = 0;
    private Dialog mPasteConflictDialog;

    private ArrayList<SectionItems> favouritesGroupChild = new ArrayList<>();
    public static final String KEY_FAV = "KEY_FAVOURITES";
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private SharedPreferences mSharedPreferences;
    private ArrayList<FavInfo> savedFavourites = new ArrayList<>();
    private View mViewSeperator;
    private int mCategory = FileConstants.CATEGORY.FILES.getValue();
    private ConstraintLayout mMainLayout;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 200;
    private static final int PREFS_REQUEST = 1000;

    private boolean mIsPermissionGranted;
    private Toolbar mToolbar;
    private int mViewMode = FileConstants.KEY_LISTVIEW;
    private boolean mIsFavGroup;
    private String mSelectedPath;
    private TextView textPathSelect;
    private final int MENU_FAVOURITES = 1;
    private boolean mIsFirstRun;
    public static final String PREFS_FIRST_RUN = "first_app_run";
    private boolean mIsDualPaneEnabledSettings = true;
    private boolean mIsPasteItemVisible;
    private boolean mIsHomeScreenEnabled;
    private boolean mIsHomePageAdded;
    private FrameLayout mFrameHomeScreen;
    private ActionBarDrawerToggle toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_default);

        checkPreferences();
        getSavedFavourites();
        initConstants();
        initViews();
//        checkScreenOrientation();
        initListeners();
        Logger.log("TAG", "on create--Activity");
        // If MarshMallow ask for permission
        if (useRunTimePermissions()) {
            checkPermissions();
        } else {
            mIsPermissionGranted = true;
            setUpInitialData();
        }
    }

 /*   @Override
    protected void onResume() {

        LocaleHelper.onCreate(this);
        super.onResume();
    }*/

    private void checkPreferences() {
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreference.getBoolean(PREFS_FIRST_RUN, true)) {
            // If app is first run
            mIsFirstRun = true;
            sharedPreference.edit().putBoolean(PREFS_FIRST_RUN, false).apply();
        }
        mIsHomeScreenEnabled = sharedPreference.getBoolean(FileConstants.PREFS_HOMESCREEN,
                true);
        mIsDualPaneEnabledSettings = sharedPreference.getBoolean(FileConstants.PREFS_DUAL_PANE,
                true);
        sharedPreference.registerOnSharedPreferenceChangeListener(this);
    }


    private void setUpInitialData() {
        prepareListData();
        setListAdapter();
        setUpPreferences();
        initialScreenSetup(mIsHomeScreenEnabled);
    }

    private void setUpPreferences() {
        mViewMode = sharedPreferenceWrapper.getViewMode(this);
    }

    /*********************************************************
     * PERMISSION CALLS
     ********************************************************/
    private boolean useRunTimePermissions() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    /**
     * Called for the 1st time when app is launched to check permissions
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager
                .PERMISSION_GRANTED) {
//            fabCreateMenu.setVisibility(View.GONE);
            requestPermission();
        } else {
            mIsPermissionGranted = true;
//            fabCreateMenu.setVisibility(View.VISIBLE);
            setUpInitialData();
        }

    }

    /**
     * Brings up the Permission Dialog
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                        .WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[]
                                                   grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:

                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    // Permission granted
                    Log.d("TAG", "Permission granted");
                    mIsPermissionGranted = true;
//                    fabCreateMenu.setVisibility(View.VISIBLE);
                    setUpInitialData();
                } else {
                    showRationale();
//                    fabCreateMenu.setVisibility(View.GONE);
                }
        }
    }

    private void showRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE)) {
            // Shows after user denied permission
            Snackbar.make(mMainLayout, getString(R.string.permission_deny), Snackbar
                    .LENGTH_INDEFINITE)
                    .setAction(getString(R.string.action_grant), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermission();
                        }
                    })
                    .show();
        } else {
            // Shows after user denied permission and checked Do Not Ask Again checkbox
            Snackbar.make(mMainLayout, getString(R.string.permission_request), Snackbar
                    .LENGTH_INDEFINITE)
                    .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                                requestPermission();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, SETTINGS_REQUEST);
                        }
                    })
                    .show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Called when user returns from the settings screen
        if (requestCode == SETTINGS_REQUEST) {
            checkPermissions();
        } else if (requestCode == PREFS_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra(FileConstants.PREFS_RESET, false)) {
                    resetFavouritesGroup();
                    expandableListView.smoothScrollToPosition(0);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);


    }


    private void getSavedFavourites() {
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        savedFavourites = sharedPreferenceWrapper.getFavorites(this);
    }


    private void initConstants() {
        STORAGE_ROOT = getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = getResources().getString(R.string.nav_menu_ext_storage);
        DOWNLOADS = getResources().getString(R.string.downloads);
        MUSIC = getResources().getString(R.string.nav_menu_music);
        VIDEO = getResources().getString(R.string.nav_menu_video);
        DOCS = getResources().getString(R.string.nav_menu_docs);
        IMAGES = getResources().getString(R.string.nav_menu_image);
        SETTINGS = getResources().getString(R.string.action_settings);
        RATE = getResources().getString(R.string.rate_us);
    }

/*    */

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     *//*
    private void checkScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && mIsDualPaneEnabledSettings) {
            mIsDualModeEnabled = true;
       *//*     mViewSeperator.setVisibility(View.VISIBLE);
            frameLayoutFabDual.setVisibility(View.VISIBLE);*//*
        }
       *//* else {
            mIsDualModeEnabled = false;
            mViewSeperator.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
        }*//*
    }*/
    private void initViews() {
        mFrameHomeScreen = (FrameLayout) findViewById(R.id.frame_home);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
//        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
//        mToolbar.setNavigationIcon(R.drawable.ic_menu_white);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setTitle(R.string.app_name);
        mMainLayout = (ConstraintLayout) findViewById(R.id.content_base);
//        mBottomToolbar = (Toolbar) findViewById(R.id.toolbar_bottom);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        relativeLayoutDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        // get the listview
        expandableListView = (ExpandableListView) findViewById(R.id.expand_list_drawer);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDrawer(true);
            }
        });


    }

    public void toggleToolbarVisibility(boolean isVisible, Toolbar toolbar) {
        if (isVisible) {
            mToolbar.setVisibility(View.GONE);
/*            toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string
                    .navigation_drawer_close);
//            toggle.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            toggle.setDrawerIndicatorEnabled(false);*/
//            toggle.syncState();
            setSupportActionBar(toolbar);
/*            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);*/
//             getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
        } else {
            mToolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(mToolbar);
/*            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
//            toggle.syncState();

       /*     toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string
                    .navigation_drawer_close);
*/
        }
    }

    public void toggleDrawer(boolean value) {
        if (value) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void initListeners() {
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int
                    childPosition, long
                                                id) {
                Log.d("TAG", "Group pos-->" + groupPosition + "CHILD POS-->" + childPosition);
                displaySelectedGroup(groupPosition, childPosition);
                return false;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
        registerForContextMenu(expandableListView);

    }

    private void prepareListData() {

        listDataChild = new HashMap<>();
        listDataHeader = getResources().getStringArray(R.array.expand_headers);
        mListHeader = Arrays.asList(listDataHeader);
        totalGroup = new ArrayList<>();
        initializeStorageGroup();
        initializeFavouritesGroup();
        initializeLibraryGroup();
        initializeOthersGroup();
    }

    public void initialScreenSetup(boolean isHomeScreenEnabled) {
        if (isHomeScreenEnabled) {
            // Fragment fragment = null;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, true);
            args.putBoolean(BaseActivity.PREFS_FIRST_RUN, mIsFirstRun);
//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
            homeScreenFragment.setArguments(args);
            ft.replace(R.id.frame_home, homeScreenFragment);
//            ft.addToBackStack(FileConstants.KEY_HOME);
            ft.commitAllowingStateLoss();
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, mCurrentDir);
            args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            args.putInt(BaseActivity.ACTION_GROUP_POS, 0); // Storage Group
            args.putInt(BaseActivity.ACTION_CHILD_POS, 1); // Internal Storage child
//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            StoragesFragment storagesFragment = new StoragesFragment();
            storagesFragment.setArguments(args);
            ft.replace(R.id.frame_home, storagesFragment, mCurrentDir);
//            ft.addToBackStack(mCurrentDir);
//            ft.commitAllowingStateLoss();
            ft.commit();
        }
    }

    private void createStoragesFragment(int groupPos, int childPos, String path) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putBoolean(FileConstants.KEY_HOME, false);
        args.putString(FileConstants.KEY_PATH, path);
        args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
        args.putInt(BaseActivity.ACTION_GROUP_POS, groupPos);
        args.putInt(BaseActivity.ACTION_CHILD_POS, childPos);

//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
        StoragesFragment storagesFragment = new StoragesFragment();
        storagesFragment.setArguments(args);
        ft.replace(R.id.frame_home, storagesFragment);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    private void initializeStorageGroup() {
        ArrayList<SectionItems> storageGroupChild = new ArrayList<>();
        File systemDir = FileUtils.getRootDirectory();
        File rootDir = systemDir.getParentFile();
        File internalSD = getInternalStorage();
        File extSD = FileUtils.getExternalStorage();
        storageGroupChild.add(new SectionItems(STORAGE_ROOT, storageSpace(systemDir), R.drawable
                .ic_root_white,
                FileUtils
                        .getAbsolutePath(rootDir)));
        storageGroupChild.add(new SectionItems(STORAGE_INTERNAL, storageSpace(internalSD), R
                .drawable
                .ic_phone_white, FileUtils.getAbsolutePath(internalSD)));
        if (extSD != null) {
            storageGroupChild.add(new SectionItems(STORAGE_EXTERNAL, storageSpace(extSD), R
                    .drawable.ic_ext_white,
                    FileUtils.getAbsolutePath(extSD)));
        }
        totalGroup.add(new SectionGroup(mListHeader.get(0), storageGroupChild));
    }

    private void initializeFavouritesGroup() {
        if (mIsFirstRun) {
            String path = FileUtils
                    .getAbsolutePath(FileUtils.getDownloadsDirectory());
            favouritesGroupChild.add(new SectionItems(DOWNLOADS, path, R.drawable.ic_download,
                    path));
            FavInfo favInfo = new FavInfo();
            favInfo.setFileName(DOWNLOADS);
            favInfo.setFilePath(path);
            sharedPreferenceWrapper.addFavorite(this, favInfo);
        }

        if (savedFavourites != null && savedFavourites.size() > 0) {
            for (int i = 0; i < savedFavourites.size(); i++) {
                String savedPath = savedFavourites.get(i).getFilePath();
                favouritesGroupChild.add(new SectionItems(savedFavourites.get(i).getFileName(),
                        savedPath, R.drawable
                        .ic_fav_folder,
                        savedPath));
            }
        }
        totalGroup.add(new SectionGroup(mListHeader.get(1), favouritesGroupChild));
    }

    private void initializeLibraryGroup() {
        ArrayList<SectionItems> libraryGroupChild = new ArrayList<>();
        libraryGroupChild.add(new SectionItems(MUSIC, null, R.drawable.ic_music_white, null));
        libraryGroupChild.add(new SectionItems(VIDEO, null, R.drawable.ic_video_white, null));
        libraryGroupChild.add(new SectionItems(IMAGES, null, R.drawable.ic_photos_white, null));
        libraryGroupChild.add(new SectionItems(DOCS, null, R.drawable.ic_file_white, null));
        totalGroup.add(new SectionGroup(mListHeader.get(2), libraryGroupChild));
    }

    private void initializeOthersGroup() {
        ArrayList<SectionItems> othersGroupChild = new ArrayList<>();
        othersGroupChild.add(new SectionItems(RATE, "", R.drawable.ic_rate_white, null));
        othersGroupChild.add(new SectionItems(SETTINGS, "", R.drawable.ic_settings_white, null));
        totalGroup.add(new SectionGroup(mListHeader.get(3), othersGroupChild));
    }


    private String storageSpace(File file) {
        String freePlaceholder = " " + getResources().getString(R.string.msg_free) + " ";
        return FileUtils.getSpaceLeft(this, file) + freePlaceholder + FileUtils.getTotalSpace
                (this, file);
    }

    private void setListAdapter() {
        expandableListAdapter = new ExpandableListAdapter(this, totalGroup);
        // setting list mAdapter
        expandableListView.setAdapter(expandableListAdapter);
        for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }
    }


    /**
     * Called every time when a file item is clicked
     *
     * @param intent Contains path of the file whose children need to be shown
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && !intent.getAction().equals(ACTION_MAIN)) {
            Log.d(TAG, "On onNewIntent");
            StoragesFragment storagesFragment = (StoragesFragment)
                    getSupportFragmentManager()
                            .findFragmentById(R
                                    .id.frame_home);
            if (storagesFragment != null) {
                storagesFragment.createFragmentForIntent(intent);
            }
        }

//            boolean intentHandled = createFragmentForIntent(intent);
    }

    /**
     * Triggered on clicked on any Navigation drawer item group/child
     *
     * @param groupPos
     * @param childPos
     */
    private void displaySelectedGroup(int groupPos, int childPos) {
        switch (groupPos) {
            case 0:
            case 1:
            case 2:
                StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.frame_home);
                String path = totalGroup.get(groupPos).getmChildItems().get(childPos)
                        .getPath();
                if (storagesFragment != null) {
                    storagesFragment.displaySelectedGroup(groupPos, childPos, path);
                } else {
                    createStoragesFragment(groupPos, childPos, path);
                }
                drawerLayout.closeDrawer(relativeLayoutDrawerPane);
                break;
            case 3:
                switch (childPos) {
                    case 0: // Rate us
                        break;
                    case 1: // Settings
                        startActivityForResult(new Intent(this, SettingsActivity.class),
                                PREFS_REQUEST);
                        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
                        break;
                }

        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type =
                ExpandableListView.getPackedPositionType(info.packedPosition);

        int group =
                ExpandableListView.getPackedPositionGroup(info.packedPosition);

        int child =
                ExpandableListView.getPackedPositionChild(info.packedPosition);

        // Only for Favorites and if its not Downloads
        if (group == 1) {
            // Only create a context menu for child items
            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                // Array created earlier when we built the expandable list
//                String page = [group][child];

//                menu.setHeaderTitle(page);

                menu.add(0, MENU_FAVOURITES, 0, getString(R.string.delete_fav));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
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
                String path = totalGroup.get(groupPos).getmChildItems().get(childPos).getPath();
                String name = totalGroup.get(groupPos).getmChildItems().get(childPos)
                        .getmFirstLine();
                FavInfo favInfo = new FavInfo();
                favInfo.setFileName(name);
                favInfo.setFilePath(path);
                favouritesGroupChild.remove(childPos);
                sharedPreferenceWrapper.removeFavorite(this, favInfo);
                expandableListAdapter.notifyDataSetChanged();
                return true;


            default:
                return super.onContextItemSelected(menuItem);
        }
    }

/*    */

    /**
     * Dual pane mode to be shown only for File Category
     *
     //     * @param isFilesCategory
     *//*
    private void toggleDualPaneVisibility(boolean isFilesCategory) {
        if (isFilesCategory) {
            if (mIsDualModeEnabled) {
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
                frameLayout.setVisibility(View.VISIBLE);
                frameLayoutFabDual.setVisibility(View.VISIBLE);
                mViewSeperator.setVisibility(View.VISIBLE);
                scrollNavigationDualPane.setVisibility(View.VISIBLE);
            }
        } else {
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
            scrollNavigationDualPane.setVisibility(View.GONE);
            isDualPaneInFocus = false;
        }

    }*/
   /* private void setNavDirectory() {
        String[] parts;
        if (!isDualPaneInFocus) {
            parts = mCurrentDir.split("/");
            navDirectory.removeAllViews();
            navigationLevelSinglePane = 0;
        } else {
            parts = mCurrentDirDualPane.split("/");
            navDirectoryDualPane.removeAllViews();
            navigationLevelDualPane = 0;
        }


        String dir = "";
        // If root dir , parts will be 0
        if (parts.length == 0) {
            isCurrentDirRoot = true;
            setNavDir("/", "/"); // Add Root button
        } else {
            int count = 0;
            for (int i = 1; i < parts.length; i++) {
                dir += "/" + parts[i];

                if (!isDualPaneInFocus) {
                    if (!dir.contains(mStartingDir)) {
                        continue;
                    }
                } else {
                    if (!dir.contains(mStartingDirDualPane)) {
                        continue;
                    }
                }
                *//*Count check so that ROOT is added only once in Navigation
                  Handles the scenario :
                  1. When Fav item is a root child and if we click on any folder in that fav item
                     multiple ROOT blocks are not added to Navigation view*//*
                if (isCurrentDirRoot && count == 0) {
                    setNavDir("/", "/");
                }
                count++;
                setNavDir(dir, parts[i]);
            }
        }

    }

    private void setNavDir(String dir, String parts) {


        int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

        if (mIsFavGroup) {
            createFragmentForFavGroup(dir);
        }

        if (dir.equals(getInternalStorage().getAbsolutePath())) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_INTERNAL, dir);
        } else if (dir.equals("/")) {
            createNavButton(STORAGE_ROOT, dir);
        } else if (FileUtils.getExternalStorage() != null && dir.equals(FileUtils
                .getExternalStorage()
                .getAbsolutePath())) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_EXTERNAL, dir);
        } else {
            ImageView navArrow = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT,
                    WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.weight = 1.0f;
            navArrow.setLayoutParams(layoutParams);
            navArrow.setBackgroundResource(R.drawable.ic_more_white);
            if (!isDualPaneInFocus) {
                navDirectory.addView(navArrow);
            } else {
                navDirectoryDualPane.addView(navArrow);
            }
            createNavButton(parts, dir);
            if (!isDualPaneInFocus) {
                scrollNavigation.postDelayed(new Runnable() {
                    public void run() {
                        HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id
                                .scrollNavigation);
                        hv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100L);
            } else {
                scrollNavigationDualPane.postDelayed(new Runnable() {
                    public void run() {
                        HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id
                                .scrollNavigationDualPane);
                        hv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100L);
            }
        }
    }

    private void createNavButton(String text, final String dir) {
        int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        final Button button = new Button(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        button.setText(text);
        if (Build.VERSION.SDK_INT < 23) {
            button.setTextAppearance(this, R.style.NavigationButton);
        } else {
            button.setTextAppearance(R.style.NavigationButton);
        }

        button.setBackgroundResource(
                android.R.color.transparent);
        if (!isDualPaneInFocus) {
            button.setTag(++navigationLevelSinglePane);
        } else {
            button.setTag(++navigationLevelDualPane);
        }
//        navigationLevelSinglePane++;
//        button.setTag(dir);
        Log.d("TAG", "Button tag SINGLE=" + navigationLevelSinglePane);
        Log.d("TAG", "Button tag DUAL=" + navDirectoryDualPane);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int level = (int) view.getTag();
//                String path  = (String) view.getTag();

//                if (!mCurrentDir.equals(path) && getSupportFragmentManager().findFragmentByTag
// (path) == null) {
//                    mCurrentDir = path;
//                    displayInitialFragment(path); // TODO Handle root case by passing /
//                }
                Log.d("TAG", "Button tag click=" + level);
                Log.d("TAG", "Dir=" + dir);

                int singlePaneCount = singlePaneFragments.size();
                int dualPaneCount = dualPaneFragments.size();

                boolean isDualPaneButtonClicked;
                LinearLayout parent = (LinearLayout) button.getParent();
                if (parent.getId() == navDirectory.getId()) {
                    isDualPaneButtonClicked = false;
                    Log.d(TAG, "Singlepane" + isDualPaneButtonClicked);
                } else {
                    isDualPaneButtonClicked = true;
                    Log.d(TAG, "Singlepane" + isDualPaneButtonClicked);
                }

                if (!isDualPaneButtonClicked) {
                    if (!mCurrentDir.equals(dir)) {
                        mCurrentDir = dir;

                        Fragment fragment = singlePaneFragments.get(level - 1);
                        replaceFragment(fragment, isDualPaneButtonClicked);
//                        removeFragments(level, dir);

                        for (int i = singlePaneFragments.size(); i > level; i--) {
                            singlePaneFragments.remove(i - 1);
                        }
                        for (int i = navDirectory.getChildCount(); i > level; i--) {
                            navDirectory.removeViewAt(i - 1);
                        }

                    }
                } else {
                    if (!mCurrentDirDualPane.equals(dir)) {
                        mCurrentDirDualPane = dir;

                        Fragment fragment = dualPaneFragments.get(level - 1);
                        replaceFragment(fragment, isDualPaneButtonClicked);
//                        removeFragments(level, dir);
                        for (int i = dualPaneFragments.size(); i > level; i--) {
                            dualPaneFragments.remove(i - 1);
                        }
                        for (int i = navDirectoryDualPane.getChildCount(); i > level; i--) {
                            navDirectoryDualPane.removeViewAt(i - 1);
                        }

                    }
                }
            }
        });
        if (!isDualPaneInFocus) {
            navDirectory.addView(button);
        } else {
            navDirectoryDualPane.addView(button);
        }
    }*/

/*    *//**
     * Create base fragment when Fav item is clicked
     *
     * @param dir
     *//*
    private void createFragmentForFavGroup(String dir) {

        if (!isDualPaneInFocus) {
            if (!dir.equals(mCurrentDir)) {
                FileListFragment fileListFragment = new FileListFragment();
                Bundle args = new Bundle();
                args.putString(FileConstants.KEY_PATH, dir);
                args.putInt(FileConstants.KEY_CATEGORY, mCategory);
                args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                fileListFragment.setArguments(args);
                singlePaneFragments.add(fileListFragment);
            }
        } else {
            if (!dir.equals(mCurrentDirDualPane)) {
                FileListDualFragment fileListDualFragment = new FileListDualFragment();
                Bundle args = new Bundle();
                args.putString(FileConstants.KEY_PATH, dir);
                args.putInt(FileConstants.KEY_CATEGORY, mCategory);
                args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                args.putBoolean(ACTION_DUAL_PANEL, true);
                fileListDualFragment.setArguments(args);
                dualPaneFragments.add(fileListDualFragment);
            }
        }
    }*/


   /* private void displayInitialFragment(String directory, int category) {
        // update the main content by replacing fragments
        // Fragment fragment = null;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        args.putInt(FileConstants.KEY_CATEGORY, category);
        args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
        if (isDualPaneInFocus) {
            args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
            FileListDualFragment fileListDualFragment = new FileListDualFragment();
            fileListDualFragment.setArguments(args);
            ft.replace(R.id.frame_container_dual, fileListDualFragment, directory);
            dualPaneFragments.add(fileListDualFragment);
        } else {
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.replace(R.id.frame_container, fileListFragment, directory);
            singlePaneFragments.add(fileListFragment);
        }

//        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }
*/
   /* *//**
     * Called when app opened 1st time
     *//*
    private void initialFragmentSetup(String directory, int category) {
        // update the main content by replacing fragments
        // Fragment fragment = null;
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        args.putInt(FileConstants.KEY_CATEGORY, category);
        args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);


        if (mIsDualModeEnabled) {
            isDualPaneInFocus = true;
            toggleDualPaneVisibility(true);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            String internalStoragePath = getInternalStorage().getAbsolutePath();
*//*            Bundle args = new Bundle();
            args.putString(FileConstants.KEY_PATH, internalStoragePath);
            args.putBoolean(FileConstants.KEY_DUAL_MODE, true);*//*
//            setNavDirectory();
*//*            FileListDualFragment dualFragment = new FileListDualFragment();
            dualPaneFragments.add(dualFragment);
            dualFragment.setArguments(args);*//*
//            ft.replace(R.id.frame_container_dual, dualFragment);
            args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
            FileListDualFragment fileListDualFragment = new FileListDualFragment();
            fileListDualFragment.setArguments(args);
            ft.replace(R.id.frame_container_dual, fileListDualFragment, directory);
            dualPaneFragments.add(fileListDualFragment);
            setNavDirectory();
            ft.commitAllowingStateLoss();
        }

        FileListFragment fileListFragment = new FileListFragment();
        args.putBoolean(FileConstants.KEY_DUAL_MODE, false);
        fileListFragment.setArguments(args);
        singlePaneFragments.add(fileListFragment);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                fileListFragment, directory).commitAllowingStateLoss();


//        ft.addToBackStack(null);

//        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }*/


    /**
     * Called whenever a file item is clicked
     *
     * @param //intent Intent received from {@link FileListFragment}
     * @return
     */
//    @Override
    /*public void createFragmentForIntent(Intent intent) {

        mIsFavGroup = false;
        if (intent.getAction() != null) {
            final String action = intent.getAction();
            Fragment targetFragment;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            isDualPaneInFocus = intent.getBooleanExtra(ACTION_DUAL_PANEL, false);
            mCategory = intent.getIntExtra(FileConstants.KEY_CATEGORY, FileConstants.CATEGORY
                    .FILES.getValue());
            int mode = intent.getIntExtra(ACTION_VIEW_MODE, FileConstants.KEY_LISTVIEW);


            if (!isDualPaneInFocus) {
                mCurrentDir = intent.getStringExtra(FileConstants.KEY_PATH);
                intent.putExtra(FileConstants.KEY_PATH_OTHER, mCurrentDirDualPane);

                targetFragment = new FileListFragment();
                if (mViewMode == mode) {
                    singlePaneFragments.add(targetFragment);
                } else {
                    mViewMode = mode;
                }
            } else {
                targetFragment = new FileListDualFragment();
                mCurrentDirDualPane = intent.getStringExtra(FileConstants.KEY_PATH);
                intent.putExtra(FileConstants.KEY_PATH_OTHER, mCurrentDir);

                dualPaneFragments.add(targetFragment);

            }

            intent.putExtra(FileConstants.KEY_FOCUS_DUAL, isDualPaneInFocus);


            if (action.equals(ACTION_VIEW_FOLDER_LIST)) {
                transaction.replace(R.id.frame_container, targetFragment, mCurrentDir);
            } else if (action.equals(ACTION_DUAL_VIEW_FOLDER_LIST)) {
                transaction.replace(R.id.frame_container_dual, targetFragment, mCurrentDirDualPane);
            }
            Logger.log("TAG", "createFragmentForIntent--currentdir=" + mCurrentDir);
            Logger.log("TAG", "createFragmentForIntent--currentDualdir=" + mCurrentDirDualPane);
            Logger.log("TAG", "createFragmentForIntent--Singlepane size=" + singlePaneFragments
                    .size());
            Logger.log("TAG", "createFragmentForIntent--Dualpane size=" + dualPaneFragments.size());


            // Set navigation directory for Files only
            if (mCategory == 0) {
                setNavDirectory();
            }

            targetFragment.setArguments(intent.getExtras());
            transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim
                    .slide_out_right);
//                transaction.addToBackStack(null);
            transaction.commit();

            return true;
        }
        return false;
    }*/
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
//        Logger.log(TAG, "Onbackpress--TOTAL FRAGMENTS count=" + count);

/*        Logger.log(TAG, "onBackPressed--SINGLEPANELFRAG count=" + singlePaneCount);
        Logger.log(TAG, "onBackPressed--DUALPANELFRAG count=" + dualPaneCount);
        Logger.log(TAG, "onBackPressed--isDualPaneInFocus=" + isDualPaneInFocus);*/

        mIsFavGroup = false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            Logger.log(TAG, "Onbackpress--backstack count=" + 1);
            getSupportFragmentManager().popBackStack();
        }
      /*  else if (fabCreateMenu.isExpanded()) {
            fabCreateMenu.collapse();
        } */
        else {
            Logger.log(TAG, "Onbackpress--ELSE=");
            super.onBackPressed();
        }
    }

/*    private void replaceFragment(Fragment fragment, boolean isDualPane) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        Logger.log("TAG","Fragment tag ="+fragment.getTag());
        String path = fragment.getArguments().getString(FileConstants.KEY_PATH);

        Bundle args = fragment.getArguments();

        Logger.log("TAG", "Fragment bundle =" + path);

        if (isDualPane) {
            FileListDualFragment dualFragment = new FileListDualFragment();
            dualFragment.setArguments(fragment.getArguments());
            args.putString(FileConstants.KEY_PATH_OTHER, mCurrentDir);
            args.putBoolean(FileConstants.KEY_FOCUS_DUAL, true);
            fragmentTransaction.replace(R.id.frame_container_dual, dualFragment, path);
        } else {
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(fragment.getArguments());
            args.putString(FileConstants.KEY_PATH_OTHER, mCurrentDirDualPane);
            args.putBoolean(FileConstants.KEY_FOCUS_DUAL, false);
            fragmentTransaction.replace(R.id.frame_container, fileListFragment, path);
        }
        fragmentTransaction.commitAllowingStateLoss();

    }*/


 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG,"On Create options Activity="+mIsPasteItemVisible);
        getMenuInflater().inflate(R.menu.base, menu);
        mPasteItem = menu.findItem(R.id.action_paste);
        mPasteItem.setVisible(mIsPasteItemVisible);
        return true;
    }*/

/*    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "On Create options Activity=" + mIsPasteItemVisible);
        getMenuInflater().inflate(R.menu.base, menu);
        mPasteItem = menu.findItem(R.id.action_paste);
        mPasteItem.setVisible(mIsPasteItemVisible);
        return super.onPrepareOptionsMenu(menu);
    }*/

 /*   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_paste:
                pasteOperationCleanUp();
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        checkIfFileExists(mFileList.get(mSelectedItemPositions.keyAt(i))
                                .getFilePath(), new File
                                (mCurrentDir));
                    }
                    if (!isPasteConflictDialogShown) {
                        callAsyncTask();
                    } else {
                        showDialog(tempSourceFile.get(0));
                        isPasteConflictDialogShown = false;
                    }


                }
                break;


        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onDestroy() {
        sharedPreferenceWrapper.savePrefs(this, mViewMode);
        unregisterForContextMenu(expandableListView);
        super.onDestroy();
    }

    private void callAsyncTask() {
        new BackGroundOperationsTask(PASTE_OPERATION).execute(mPathActionMap);
    }

    private void pasteOperationCleanUp() {
        mPathActionMap.clear();
        isPasteConflictDialogShown = false;
        tempConflictCounter = 0;
        tempSourceFile = new ArrayList<>();
    }

    public void checkIfFileExists(String sourceFilePath, File destinationDir) {
        String[] destinationDirList = destinationDir.list();
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1,
                sourceFilePath.length());
        mSourceFilePath = sourceFilePath;
        // If source file is directory,compare source & destination directory names

        // If source file is file,compare source file name & destination directory children names
        for (int i = 0; i < destinationDirList.length; i++) {
            if (fileName.equals(destinationDirList[i])) {
                isPasteConflictDialogShown = true;
                tempSourceFile.add(sourceFilePath);
                break;
            } else {
                mPasteAction = FileUtils.ACTION_NONE;
                mPathActionMap.put(sourceFilePath, mPasteAction);
            }
        }


        Logger.log("TAG", "SOURCE==" + sourceFilePath + "isPasteConflictDialogShown==" +
                isPasteConflictDialogShown);

    }

    private void showDialog(final String sourceFilePath) {
        Context mContext = BaseActivity.this;
        mPasteConflictDialog = new Dialog(mContext);
        mPasteConflictDialog.setContentView(R.layout.dialog_paste_conflict);
//        mPasteConflictDialog.setTitle(getResources().getString(R.string
// .dialog_title_paste_conflict));
        mPasteConflictDialog.setCancelable(false);
        ImageView icon = (ImageView) mPasteConflictDialog.findViewById(R.id.imageFileIcon);
        TextView textFileName = (TextView) mPasteConflictDialog.findViewById(R.id.textFileName);
        TextView textFileDate = (TextView) mPasteConflictDialog.findViewById(R.id.textFileDate);
        TextView textFileSize = (TextView) mPasteConflictDialog.findViewById(R.id.textFileSize);
        Button buttonReplace = (Button) mPasteConflictDialog.findViewById(R.id.buttonReplace);
        Button buttonSkip = (Button) mPasteConflictDialog.findViewById(R.id.buttonSkip);
        Button buttonKeep = (Button) mPasteConflictDialog.findViewById(R.id.buttonKeepBoth);
        if (new File(sourceFilePath).isDirectory()) {
            buttonKeep.setVisibility(View.GONE);
        }
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1,
                sourceFilePath.length());

        textFileName.setText(fileName);
        File sourceFile = new File(sourceFilePath);
        long date = sourceFile.lastModified();
        String fileModifiedDate = FileUtils.convertDate(date);
        long size = sourceFile.length();
        String fileSize = Formatter.formatFileSize(mContext, size);
        textFileDate.setText(fileModifiedDate);
        textFileSize.setText(fileSize);
        Drawable drawable = FileUtils.getAppIcon(mContext, sourceFilePath);
        if (drawable != null) {
            icon.setImageDrawable(drawable);
        }
        mPasteConflictDialog.show();
//        buttonReplace.setOnClickListener(this);
//        buttonSkip.setOnClickListener(this);
//        buttonKeep.setOnClickListener(this);
        buttonReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfPasteConflictFinished(FileUtils.ACTION_REPLACE);
            }
        });

        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfPasteConflictFinished(FileUtils.ACTION_SKIP);
            }
        });

        buttonKeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfPasteConflictFinished(FileUtils.ACTION_KEEP);
            }
        });

    }


/*
    @Override
    public void onClick(final View view) {
        switch (view.getId()) {

            case R.id.fabCreateFileDual:
            case R.id.fabCreateFolderDual:
                fabCreateMenuDual.collapse();
            case R.id.fabCreateFile:
            case R.id.fabCreateFolder:
                if (view.getId() == R.id.fabCreateFolder || view.getId() == R.id.fabCreateFile) {
                    fabCreateMenu.collapse();
                }

                final Dialog dialog = new Dialog(
                        BaseActivity.this);
//                dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
                dialog.setContentView(R.layout.dialog_rename);
                dialog.setCancelable(true);
                // end of dialog declaration
                String title = setDialogTitle(view.getId());
                TextView dialogTitle = (TextView) dialog.findViewById(R.id.textDialogTitle);
                dialogTitle.setText(title);


                // define the contents of edit dialog
                final EditText rename = (EditText) dialog
                        .findViewById(R.id.editRename);

                rename.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

                // dialog save button to save the edited item
                Button saveButton = (Button) dialog
                        .findViewById(R.id.buttonRename);
                // for updating the list item
                saveButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        final CharSequence name = rename.getText();
                        if (name.length() == 0) {
                            rename.setError(getResources().getString(R.string
                                    .msg_error_valid_name));
                            return;
                        }
                        FileListFragment singlePaneFragment = (FileListFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container);
                        FileListFragment dualPaneFragment = (FileListDualFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container_dual);
                        String fileName = rename.getText().toString() + "";

                        int result;
                        */
/**
 * In landscape mode, FabCreateFile is on Dual Pane side and
 * FabCreateFileDual on Single pane
 *//*

                        if (view.getId() == R.id.fabCreateFile || view.getId() == R.id
                                .fabCreateFileDual) {
                            if (view.getId() == R.id.fabCreateFile) {
                                if (mIsDualModeEnabled) {
                                    result = FileUtils.createFile(mCurrentDirDualPane, fileName +
                                            ".txt");
                                } else {
                                    result = FileUtils.createFile(mCurrentDir, fileName + ".txt");
                                }

                            } else {
                                result = FileUtils.createFile(mCurrentDir, fileName + ".txt");
                            }
                            if (result == 0) {
                                showMessage(getString(R.string.msg_file_create_success));
                            } else {
                                showMessage(getString(R.string.msg_file_create_failure));
                            }
                        } else {
                            if (view.getId() == R.id.fabCreateFolder) {
                                if (mIsDualModeEnabled) {
                                    result = FileUtils.createDir(mCurrentDirDualPane, fileName);
                                } else {
                                    result = FileUtils.createDir(mCurrentDir, fileName);
                                }

                            } else {
                                result = FileUtils.createDir(mCurrentDir, fileName);
                            }

                            if (result == 0) {
                                showMessage(getString(R.string.msg_folder_create_success));
                            } else {
                                showMessage(getString(R.string.msg_folder_create_failure));
                            }
                        }

                        if (singlePaneFragment != null) {
                            singlePaneFragment.refreshList();
                        }
                        if (dualPaneFragment != null) {
                            dualPaneFragment.refreshList();
                        }
                        dialog.dismiss();

                    }
                });

                // cancel button declaration
                Button cancelButton = (Button) dialog
                        .findViewById(R.id.buttonCancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        dialog.dismiss();

                    }
                });

                dialog.show();
                break;


//            case R.id.buttonReplace:
//                checkIfPasteConflictFinished(FileUtils.ACTION_REPLACE);
//                break;
//            case R.id.buttonSkip:
//                checkIfPasteConflictFinished(FileUtils.ACTION_SKIP);
//                break;
//            case R.id.buttonKeepBoth:
//                checkIfPasteConflictFinished(FileUtils.ACTION_KEEP);
//                break;
        }
    }
*/

//    private String setDialogTitle(int id) {
//        String title = "";
//        switch (id) {
//            case R.id.fabCreateFile:
//                title = getString(R.string.new_file);
//                break;
//            case R.id.fabCreateFolder:
//                title = getString(R.string.new_folder);
//                break;
//            case R.id.action_rename:
//                title = getString(R.string.action_rename);
//        }
//        return title;
//
//    }

    /**
     * Shows another alert dialog when user clicks on any button Skip,Keep or Replace
     *
     * @param action Button action -->
     *               {@link FileUtils#ACTION_REPLACE,FileUtils#ACTION_SKIP,FileUtils#ACTION_KEEP}
     *               Calls Async task to do the copy operation once user resolves all conflicts
     */
    private void checkIfPasteConflictFinished(int action) {
        mPasteConflictDialog.dismiss();
        int count = ++tempConflictCounter;
        mPasteAction = action;
        mPathActionMap.put(mSourceFilePath, mPasteAction);
        Logger.log("TAG", "tempConflictCounter==" + tempConflictCounter + "tempSize==" +
                tempSourceFile.size());
        if (count < tempSourceFile.size()) {
            showDialog(tempSourceFile.get(count));
        } else {
            callAsyncTask();
        }
    }


    public void setFileList(ArrayList<FileInfo> list) {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            storagesFragment.setFileList(list);
        }
    }

/*    public void startActionMode() {

//fabCreateMenu.setVisibility(View.GONE);
        mBottomToolbar.setVisibility(View.VISIBLE);
        mBottomToolbar.startActionMode(new ActionModeCallback());
//        mBottomToolbar.inflateMenu(R.menu.action_mode_bottom);
        mBottomToolbar.getMenu().clear();
        EnhancedMenuInflater.inflate(getMenuInflater(), mBottomToolbar.getMenu(), true, mCategory);
        mBottomToolbar.setOnMenuItemClickListener(this);
    }*/

    @Override
    public void startActionModeOperations() {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            storagesFragment.toggleFabVisibility(true);
            storagesFragment.setupBottomToolbar();
        }
    }

    @Override
    public void endActionMode() {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            storagesFragment.toggleFabVisibility(false);
        }
    }

    public void togglePasteVisibility(boolean isVisible) {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            storagesFragment.togglePasteVisibility(isVisible);
        }
    }

/*    public void togglePasteVisibility(boolean isVisible) {
        mPasteItem.setVisible(isVisible);
        mIsPasteItemVisible = isVisible;
    }*/

    public void updateAdapter(SectionItems favItem) {
        if (!favouritesGroupChild.contains(favItem)) {
            favouritesGroupChild.add(favItem);
            expandableListAdapter.notifyDataSetChanged();
        }

    }


    public ActionMode getActionMode() {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            return storagesFragment.getActionMode();
        }
        return null;
    }

    public void setSelectedCount(String title) {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            storagesFragment.setActionModeTitle(title);
        }
    }

    public void setFileListAdapter(FileListAdapter adapter) {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            storagesFragment.setFileListAdapter(adapter);
        }
    }

    public void setSelectedItemPos(SparseBooleanArray selectedItemPos) {
        StoragesFragment storagesFragment = (StoragesFragment) getSupportFragmentManager().findFragmentById(R.id
                .frame_home);
        if (storagesFragment != null) {
            storagesFragment.setSelectedItemPos(selectedItemPos);
        }
    }

   /* */

    /**
     * Toolbar menu item click listener
     * <p>
     * //     * @param item
     *
     * @return
     *//*
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_cut:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    showMessage(mSelectedItemPositions.size() + " " + getString(R.string
                            .msg_cut_copy));
                    mIsMoveOperation = true;
                    togglePasteVisibility(true);
                    supportInvalidateOptionsMenu();
                    mActionMode.finish();

                }
                break;
            case R.id.action_copy:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    mIsMoveOperation = false;
                    showMessage(mSelectedItemPositions.size() + " " + getString(R.string
                            .msg_cut_copy));
                    togglePasteVisibility(true);
                    supportInvalidateOptionsMenu();
                    mActionMode.finish();
                }
                break;
            case R.id.action_delete:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    ArrayList<String> filesToDelete = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        String path = mFileList.get(mSelectedItemPositions.keyAt(i)).getFilePath();
                        filesToDelete.add(path);
                    }
                    showDialog(filesToDelete);
                    mActionMode.finish();
                }
                break;
            case R.id.action_share:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    ArrayList<FileInfo> filesToShare = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        FileInfo info = mFileList.get(mSelectedItemPositions.keyAt(i));
                        if (!info.isDirectory()) {
                            filesToShare.add(info);
                        }
                    }
                    FileUtils.shareFiles(this, filesToShare, mCategory);
                    mActionMode.finish();
                }
                break;

            case R.id.action_select_all:
                if (mSelectedItemPositions != null) {
                    FileListFragment singlePaneFragment = (FileListFragment)
                            getSupportFragmentManager()
                                    .findFragmentById(R
                                            .id.frame_container);
                    if (mSelectedItemPositions.size() < fileListAdapter.getItemCount()) {

                        if (singlePaneFragment != null) {
                            singlePaneFragment.toggleSelectAll(true);
                        }
                    } else {
                        if (singlePaneFragment != null) {
                            singlePaneFragment.toggleSelectAll(false);
                        }
                    }
                }
                break;
        }
        return false;
    }*/
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /*    @Override
        public void getSelectedPath(String path) {
            mSelectedPath = path;
            if (textPathSelect != null) {
                textPathSelect.setText(mSelectedPath);
            }

        }*/
    private boolean mIsHomeSettingToggled;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onPostResume" + mIsHomeSettingToggled);
        if (mIsHomeSettingToggled) {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, FileUtils.getInternalStorage().getAbsolutePath());
            args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            args.putInt(BaseActivity.ACTION_GROUP_POS, 0);
            args.putInt(BaseActivity.ACTION_CHILD_POS, 1);

//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            StoragesFragment storagesFragment = new StoragesFragment();
            storagesFragment.setArguments(args);
            ft.replace(R.id.frame_home, storagesFragment);
//                    ft.addToBackStack(null);
//                        ft.commitAllowingStateLoss();
            ft.commit();
            mIsHomeSettingToggled = false;
        } else if (mIsHomePageAdded) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_home);
            ((StoragesFragment) fragment).setHomeScreenEnabled(true);
            mIsHomePageAdded = false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(FileConstants.PREFS_HIDDEN)) {

            FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                    .findFragmentById(R
                            .id.frame_container);
            FileListFragment dualPaneFragment = (FileListDualFragment) getSupportFragmentManager()
                    .findFragmentById(R
                            .id.frame_container_dual);
            if (singlePaneFragment != null) {
                singlePaneFragment.refreshList();
            }
            if (dualPaneFragment != null) {
                dualPaneFragment.refreshList();
            }
        } else if (key.equals(FileConstants.PREFS_HOMESCREEN)) {
            boolean isHomeScreenEnabled = sharedPreferences.getBoolean(FileConstants
                    .PREFS_HOMESCREEN, true);
            if (isHomeScreenEnabled != mIsHomeScreenEnabled) {
                mIsHomeScreenEnabled = isHomeScreenEnabled;
                Log.d(TAG, "OnPrefschanged " +
                        "CALLED==getSupportFragmentManager=" + getSupportFragmentManager());
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_home);
                Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(mCurrentDir);

                Log.d(TAG, "OnPrefschanged ==fragment=" + fragment);
                Log.d(TAG, "OnPrefschanged==fragment by tag=" + fragment1);

                if (!isHomeScreenEnabled) {

                    if (fragment instanceof HomeScreenFragment) {
//                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();

                        mIsHomeSettingToggled = true;
/*                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        Bundle args = new Bundle();
                        args.putBoolean(FileConstants.KEY_HOME, false);
                        args.putString(FileConstants.KEY_PATH, FileUtils.getInternalStorage().getAbsolutePath());
                        args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                        args.putInt(BaseActivity.ACTION_GROUP_POS, 0);
                        args.putInt(BaseActivity.ACTION_CHILD_POS, 1);

//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                        StoragesFragment storagesFragment = new StoragesFragment();
                        storagesFragment.setArguments(args);
                        ft.replace(R.id.frame_home, storagesFragment);
//                    ft.addToBackStack(null);
//                        ft.commitAllowingStateLoss();
                        ft.commit();*/

                    } else {
                        ((StoragesFragment) fragment).removeHomeScreen(true);
                    }


                } else {
                    mIsHomePageAdded = true;

                }
            }


        }
       /* else if (key.equals(FileConstants.PREFS_DUAL_PANE)) {
            mIsDualPaneEnabledSettings = sharedPreferences.getBoolean(FileConstants
                    .PREFS_DUAL_PANE, true);
//            isDualPaneInFocus = false;

            if (!mIsDualPaneEnabledSettings) {
                toggleDualPaneVisibility(false);
            } else {
                if (mCategory == FileConstants.CATEGORY.FILES.getValue() && mIsDualModeEnabled) {
                    isDualPaneInFocus = true;
                    toggleDualPaneVisibility(true);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    String internalStoragePath = getInternalStorage().getAbsolutePath();
                    Bundle args = new Bundle();
                    args.putString(FileConstants.KEY_PATH, internalStoragePath);
                    args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
                    setNavDirectory();
                    FileListDualFragment dualFragment = new FileListDualFragment();
                    dualPaneFragments.add(dualFragment);
                    dualFragment.setArguments(args);
                    ft.replace(R.id.frame_container_dual, dualFragment);
//                mViewSeperator.setVisibility(View.VISIBLE);
                    ft.commitAllowingStateLoss();
                }
            }

        }*/
    }


   /* */

    /**
     * Triggered on long press click on item
     *//*
    private final class ActionModeCallback implements ActionMode.Callback {


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            MenuInflater inflater = mActionMode.getMenuInflater();
            inflater.inflate(R.menu.action_mode, menu);

            mRenameItem = menu.findItem(R.id.action_rename);
            mInfoItem = menu.findItem(R.id.action_info);
            mArchiveItem = menu.findItem(R.id.action_archive);
            mFavItem = menu.findItem(R.id.action_fav);
            mExtractItem = menu.findItem(R.id.action_extract);
            mHideItem = menu.findItem(R.id.action_hide);
            setupMenu(menu);

            return true;
        }

        private void setupMenu(Menu menu) {
            MenuItem item = menu.findItem(R.id.action_archive);
            SpannableStringBuilder builder = new SpannableStringBuilder(" " + "  " + getString(R
                    .string
                    .action_archive));
            // replace "*" with icon
            builder.setSpan(new ImageSpan(BaseActivity.this, R.drawable.ic_archive_white), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mArchiveItem.setTitle(builder);

            SpannableStringBuilder favBuilder = new SpannableStringBuilder(" " + "  " + getString
                    (R.string
                            .add_fav));
            // replace "*" with icon
            favBuilder.setSpan(new ImageSpan(BaseActivity.this, R.drawable.ic_favorite_white), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mFavItem.setTitle(favBuilder);

            SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                    getString(R.string
                            .hide));
            // replace "*" with icon
            hideBuilder.setSpan(new ImageSpan(BaseActivity.this, R.drawable.ic_hide_white), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mHideItem.setTitle(hideBuilder);
        }

        @SuppressLint("NewApi")
        @Override
        public boolean
        onPrepareActionMode(ActionMode mode, Menu menu) {
            // Dont show Fav and Archive option for Non file mode
            if (mCategory != 0) {
                mArchiveItem.setVisible(false);
                mFavItem.setVisible(false);
                mHideItem.setVisible(false);
            }
            if (mSelectedItemPositions.size() > 1) {
                mRenameItem.setVisible(false);
                mInfoItem.setVisible(false);
                mFavItem.setVisible(false);

            } else {
                mRenameItem.setVisible(true);
                mInfoItem.setVisible(true);
                if (mSelectedItemPositions.size() == 1) {
                    boolean isDirectory = mFileList.get(mSelectedItemPositions.keyAt(0))
                            .isDirectory();
                    String extension = mFileList.get(mSelectedItemPositions.keyAt(0))
                            .getExtension();
                    if (extension != null && extension.equalsIgnoreCase("zip")) {
                        mExtractItem.setVisible(true);
                    }
                    if (!isDirectory) {
                        mFavItem.setVisible(false);
                    }
                    String fileName = mFileList.get(mSelectedItemPositions.keyAt(0)).getFileName();
                    if (fileName.startsWith(".")) {
                        SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                                "" + getString(R.string
                                .unhide));
                        // replace "*" with icon
                        hideBuilder.setSpan(new ImageSpan(BaseActivity.this, R.drawable
                                        .ic_unhide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mHideItem.setTitle(hideBuilder);
                     *//*   mHideItem.setTitle(getString(R.string.unhide));
                        mHideItem.setIcon(R.drawable.ic_unhide_white);*//*
                    } else {
                        SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                                "" + getString(R.string
                                .hide));
                        // replace "*" with icon
                        hideBuilder.setSpan(new ImageSpan(BaseActivity.this, R.drawable
                                        .ic_hide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mHideItem.setTitle(hideBuilder);
                *//*        mHideItem.setTitle(getString(R.string.hide));
                        mHideItem.setIcon(R.drawable.ic_hide_white);*//*
                    }
                }
            }

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rename:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        final String filePath = mFileList.get(mSelectedItemPositions.keyAt(0))
                                .getFilePath();
//                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1,
// filePath.length());
                        String fileName = mFileList.get(mSelectedItemPositions.keyAt(0))
                                .getFileName();

                        final long id = mFileList.get(mSelectedItemPositions.keyAt(0)).getId();
//                        String extension = null;
                        String extension = mFileList.get(mSelectedItemPositions.keyAt(0))
                                .getExtension();
                        boolean file = false;
                        if (new File(filePath).isFile()) {
                            String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                            fileName = tokens[0];
//                            extension = tokens[1];
                            file = true;
                        }
                        final boolean isFile = file;
                        final String ext = extension;

                        final Dialog dialog = new Dialog(
                                BaseActivity.this);
//                dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
                        dialog.setContentView(R.layout.dialog_rename);
                        dialog.setCancelable(true);
                        // end of dialog declaration

                        // define the contents of edit dialog
                        final EditText rename = (EditText) dialog
                                .findViewById(R.id.editRename);

                        rename.setText(fileName);
                        rename.setFocusable(true);
                        // dialog save button to save the edited item
                        Button saveButton = (Button) dialog
                                .findViewById(R.id.buttonRename);
                        // for updating the list item
                        saveButton.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                final CharSequence name = rename.getText();
                                if (name.length() == 0) {
                                    rename.setError(getResources().getString(R.string
                                            .msg_error_valid_name));
                                    return;
                                }
                                FileListFragment singlePaneFragment = (FileListFragment)
                                        getSupportFragmentManager()
                                                .findFragmentById(R
                                                        .id.frame_container);
                                String renamedName;
                                if (isFile) {
                                    renamedName = rename.getText().toString() + "." + ext;
                                } else {
                                    renamedName = rename.getText().toString();
                                }
//                                if (mCategory == 0) {
                                int result = FileUtils.renameTarget(filePath, renamedName);
                                String temp = filePath.substring(0, filePath.lastIndexOf("/"));
                                String newFileName = temp + "/" + renamedName;
//                                }
//                            else {
//                                    renamedName = rename.getText().toString();
                                //For mediastore, we just need title and not extension
                                if (mCategory != 0) {
                                    updateMediaStore(id, newFileName);
                                }
//                                }

                                if (singlePaneFragment != null) {
                                    singlePaneFragment.refreshList();
                                }
                                dialog.dismiss();

                            }
                        });

                        // cancel button declaration
                        Button cancelButton = (Button) dialog
                                .findViewById(R.id.buttonCancel);
                        cancelButton.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                dialog.dismiss();

                            }
                        });

                        dialog.show();
                    }

                    mActionMode.finish();
                    return true;

                case R.id.action_info:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo fileInfo = mFileList.get(mSelectedItemPositions.keyAt(0));
                        showDialog(fileInfo);

                    }
                    mActionMode.finish();
                    return true;
                case R.id.action_archive:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo fileInfo = mFileList.get(mSelectedItemPositions.keyAt(0));
                        int result = FileUtils.createZipFile(fileInfo.getFilePath());
                        FileListFragment singlePaneFragment = (FileListFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container);
                        if (result == 0) {
                            showMessage(getString(R.string.msg_zip_success));
                            if (singlePaneFragment != null) {
                                singlePaneFragment.refreshList();
                            }

                        } else {
                            showMessage(getString(R.string.msg_zip_failure));
                        }
                    }
                    mActionMode.finish();
                    return true;
                case R.id.action_fav:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            FileInfo info = mFileList.get(mSelectedItemPositions.keyAt(i));
                            updateFavouritesGroup(info);
                        }
                        showMessage(getString(R.string.msg_added_to_fav));
                    }
                    mActionMode.finish();
                    return true;

                case R.id.action_extract:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo fileInfo = mFileList.get(mSelectedItemPositions.keyAt(0));
                        String currentFile = fileInfo.getFilePath();
                        showExtractOptions(currentFile, mCurrentDir);
                    }

                    mActionMode.finish();
                    return true;

                case R.id.action_hide:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        ArrayList<FileInfo> infoList = new ArrayList<>();
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            infoList.add(mFileList.get(mSelectedItemPositions.keyAt(i)));
                        }
                        hideUnHideFiles(infoList);
                    }
                    mActionMode.finish();
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            FileListFragment fileListFragment = (FileListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id
                            .frame_container);
            if (fileListFragment != null) {
                fileListFragment.clearSelection();
                fileListFragment.toggleDummyView(false);
            }

            mActionMode = null;
            mBottomToolbar.setVisibility(View.GONE);
  *//*          // FAB should be visible only for Files Category
            if (mCategory == 0) {
                fabCreateMenu.setVisibility(View.VISIBLE);
            }*//*
        }
    }*/

   /* private void hideUnHideFiles(ArrayList<FileInfo> fileInfo) {
        for (int i = 0; i < fileInfo.size(); i++) {
            String fileName = fileInfo.get(i).getFileName();
            String renamedName;
            if (fileName.startsWith(".")) {
                renamedName = fileName.substring(1);
            } else {
                renamedName = "." + fileName;
            }

            int result = FileUtils.renameTarget(fileInfo.get(i).getFilePath(), renamedName);
        }
        FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                .findFragmentById(R
                        .id.frame_container);
        if (singlePaneFragment != null) {
            singlePaneFragment.refreshList();
        }
    }

    private void showExtractOptions(final String currentFilePath, final String currentDir) {

        final File currentFile = new File(currentFilePath);
        final String currentFileName = currentFilePath.substring(currentFilePath.lastIndexOf("/")
                + 1, currentFilePath.lastIndexOf("."));
        final Dialog dialog = new Dialog(
                BaseActivity.this);
        dialog.setContentView(R.layout.dialog_extract);
        dialog.setCancelable(true);
        mSelectedPath = null;

        final RadioButton radioButtonSpecify = (RadioButton) dialog.findViewById(R.id
                .radioButtonSpecifyPath);
        textPathSelect = (TextView) dialog.findViewById(R.id.textPathSelect);
        RadioGroup radioGroupPath = (RadioGroup) dialog.findViewById(R.id.radioGroupPath);
        Button buttonExtract = (Button) dialog.findViewById(R.id.buttonExtract);
        Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
        EditText editFileName = (EditText) dialog.findViewById(R.id.editFileName);
        editFileName.setText(currentFileName);

        dialog.show();
        radioGroupPath.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radioButtonCurrentPath) {
                    textPathSelect.setVisibility(View.GONE);
                } else {
                    textPathSelect.setVisibility(View.VISIBLE);
                }
            }
        });

        textPathSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
                dialogFragment.show(fm, "Browse Fragment");
            }
        });

        buttonExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioButtonSpecify.isChecked()) {
                    new ExtractManager(BaseActivity.this)
                            .extract(currentFile, mSelectedPath, currentFileName);
                } else {
                    new ExtractManager(BaseActivity.this)
                            .extract(currentFile, currentDir, currentFileName);
                }
                dialog.dismiss();


            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }

    private void updateFavouritesGroup(FileInfo info) {

        String name = info.getFileName();
        String path = info.getFilePath();
        FavInfo favInfo = new FavInfo();
        favInfo.setFileName(name);
        favInfo.setFilePath(path);

        sharedPreferenceWrapper.addFavorite(this, favInfo);
        SectionItems favItem = new SectionItems(name, path, R.drawable.ic_fav_folder, path);
        if (!favouritesGroupChild.contains(favItem)) {
            favouritesGroupChild.add(favItem);
            expandableListAdapter.notifyDataSetChanged();
        }


    }*/
    private void resetFavouritesGroup() {

        for (int i = favouritesGroupChild.size() - 1; i > 0; i--) {
            if (!favouritesGroupChild.get(i).getmSecondLine().equalsIgnoreCase(FileUtils
                    .getDownloadsDirectory().getAbsolutePath())) {
                favouritesGroupChild.remove(i);
            }
        }
        sharedPreferenceWrapper.resetFavourites(this);
        expandableListAdapter.notifyDataSetChanged();

    }

   /* private void updateMediaStore(long id, String renamedFilePath) {
        ContentValues values = new ContentValues();
        switch (mCategory) {
            case 1:
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);
//                values.put(MediaStore.Audio.Media.TITLE, title);
                values.put(MediaStore.Audio.Media.DATA, renamedFilePath);
                String audioId = "" + id;
                getContentResolver().update(musicUri, values, MediaStore.Audio.Media._ID
                        + "= ?", new String[]{audioId});
                break;

            case 2:
                Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);

//                values.put(MediaStore.Video.Media.TITLE, title);
                values.put(MediaStore.Video.Media.DATA, renamedFilePath);
                String videoId = "" + id;
                getContentResolver().update(videoUri, values, MediaStore.Video.Media._ID
                        + "= ?", new String[]{videoId});
                break;

            case 3:
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);
//                values.put(MediaStore.Images.Media.TITLE, title);

                values.put(MediaStore.Images.Media.DATA, renamedFilePath);
                String imageId = "" + id;
                getContentResolver().update(imageUri, values, MediaStore.Images.Media._ID
                        + "= ?", new String[]{imageId});
                break;

            case 4:
                Uri filesUri = MediaStore.Files.getContentUri("external");
                values.put(MediaStore.Files.FileColumns.DATA, renamedFilePath);
                String fileId = "" + id;
                getContentResolver().update(filesUri, values, MediaStore.Files.FileColumns._ID
                        + "= ?", new String[]{fileId});
                break;

            default:
                break;
        }
    }


    private void showDialog(FileInfo fileInfo) {
        final Dialog dialog = new Dialog(
                BaseActivity.this);
        dialog.setContentView(R.layout.dialog_file_properties);
        dialog.setCancelable(true);
        ImageView imageFileIcon = (ImageView) dialog.findViewById(R.id.imageFileIcon);
        TextView textFileName = (TextView) dialog.findViewById(R.id.textFileName);
        ImageButton imageButtonClose = (ImageButton) dialog.findViewById(R.id.imageButtonClose);
        TextView textPath = (TextView) dialog.findViewById(R.id.textPath);
        TextView textFileSize = (TextView) dialog.findViewById(R.id.textFileSize);
        TextView textDateModified = (TextView) dialog.findViewById(R.id.textDateModified);
        TextView textHidden = (TextView) dialog.findViewById(R.id.textHidden);
        TextView textReadable = (TextView) dialog.findViewById(R.id.textReadable);
        TextView textWriteable = (TextView) dialog.findViewById(R.id.textWriteable);
        TextView textHiddenPlaceHolder = (TextView) dialog.findViewById(R.id.textHiddenPlaceHolder);
        TextView textReadablePlaceHolder = (TextView) dialog.findViewById(R.id
                .textReadablePlaceHolder);
        TextView textWriteablePlaceHolder = (TextView) dialog.findViewById(R.id
                .textWriteablePlaceHolder);
        TextView textMD5 = (TextView) dialog.findViewById(R.id.textMD5);
        TextView textMD5Placeholder = (TextView) dialog.findViewById(R.id.textMD5PlaceHolder);

        String path = fileInfo.getFilePath();
        String fileName = fileInfo.getFileName();
        String dateModified = fileInfo.getFileDate();
        String fileNoOrSize = fileInfo.getNoOfFilesOrSize();
        boolean isReadable = new File(path).canRead();
        boolean isWriteable = new File(path).canWrite();
        boolean isHidden = new File(path).isHidden();

        textFileName.setText(fileName);
        textPath.setText(path);
        textFileSize.setText(fileNoOrSize);
        textDateModified.setText(dateModified);

        if (mCategory != 0) {
            textMD5.setVisibility(View.GONE);
            textMD5Placeholder.setVisibility(View.GONE);
            textReadablePlaceHolder.setVisibility(View.GONE);
            textWriteablePlaceHolder.setVisibility(View.GONE);
            textHiddenPlaceHolder.setVisibility(View.GONE);
            textReadable.setVisibility(View.GONE);
            textWriteable.setVisibility(View.GONE);
            textHidden.setVisibility(View.GONE);
        } else {
            textReadable.setText(isReadable ? getString(R.string.yes) : getString(R.string.no));
            textWriteable.setText(isWriteable ? getString(R.string.yes) : getString(R.string.no));
            textHidden.setText(isHidden ? getString(R.string.yes) : getString(R.string.no));
        }


        dialog.show();

        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (new File(path).isDirectory()) {
            textMD5.setVisibility(View.GONE);
            textMD5Placeholder.setVisibility(View.GONE);
            Drawable apkIcon = FileUtils.getAppIconForFolder(this, fileName);
            if (apkIcon != null) {
                imageFileIcon.setImageDrawable(apkIcon);
            } else {
                imageFileIcon.setImageResource(R.drawable.ic_folder);
            }
        } else {
            if (mCategory == 0) {
                String md5 = FileUtils.getFastHash(path);
                textMD5.setText(md5);
            }

            if (fileInfo.getType() == FileConstants.CATEGORY.IMAGE.getValue() ||
                    fileInfo.getType() == FileConstants.CATEGORY.VIDEO.getValue()) {
                Uri imageUri = Uri.fromFile(new File(path));
                Glide.with(this).load(imageUri).centerCrop()
                        .crossFade(2)
                        .into(imageFileIcon);
            } else if (fileInfo.getExtension().equals(FileConstants.APK_EXTENSION)) {
                Drawable apkIcon = FileUtils.getAppIcon(this, path);
                imageFileIcon.setImageDrawable(apkIcon);
            } else {
                imageFileIcon.setImageResource(R.drawable.ic_doc_white);
            }
        }
    }


    private void showDialog(final ArrayList<String> paths) {
        final Dialog deleteDialog = new Dialog(this);
        deleteDialog.setContentView(R.layout.dialog_delete);
        deleteDialog.setCancelable(false);
        TextView textFileName = (TextView) deleteDialog.findViewById(R.id.textFileNames);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            stringBuilder.append(path);
            stringBuilder.append("\n\n");
            if (i == 9 && paths.size() > 10) {
                int rem = paths.size() - 10;
                stringBuilder.append("+" + rem + " " + getString(R.string.more));
                break;
            }
        }
        textFileName.setText(stringBuilder.toString());
        Button buttonOk = (Button) deleteDialog.findViewById(R.id.buttonOk);
        Button buttonCancel = (Button) deleteDialog.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BgOperationsTask(DELETE_OPERATION).execute(paths);
                deleteDialog.dismiss();
            }
        });
        deleteDialog.show();

    }*/

    public void clearSelectedPos() {
        if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {
            mSelectedItemPositions.clear();
        }
    }

    public class BackGroundOperationsTask extends AsyncTask<HashMap<String, Integer>, Integer,
            Void> {

        private String fileName;
        private String filePath;
        private int copyStatus = -1;
        //        private ProgressDialog progressDialog;
        private Dialog progressDialog;
        private Dialog deleteDialog;
        private int operation;
        private int currentFile = 0;
        private int filesCopied;
        private boolean isActionCancelled;
        TextView textFileName;
        TextView textFileSource;
        TextView textFileDest;
        TextView textFilesLeft;
        TextView textProgressPercent;
        ProgressBar pasteProgress;
        Progress progress;
        private int totalFiles;
        private String sourcePath;


        private BackGroundOperationsTask(int operation) {
            this.operation = operation;
            progress = new Progress(this);
            sourcePath = mSourceFilePath;

        }

        @Override
        protected void onPreExecute() {
            Context mContext = BaseActivity.this;
            switch (operation) {

                case PASTE_OPERATION:
                    showProgressDialog();
                    break;

            }

        }

        private void showProgressDialog() {
            Context mContext = BaseActivity.this;
            switch (operation) {
                case PASTE_OPERATION:
                    progressDialog = new Dialog(mContext);
                    progressDialog.setContentView(R.layout.dialog_progress_paste);
                    progressDialog.setCancelable(false);
                    TextView textTitle = (TextView) progressDialog.findViewById(R.id
                            .textDialogTitle);
                    if (mIsMoveOperation) {
                        textTitle.setText(mContext.getString(R.string.msg_cut));
                    } else {
                        textTitle.setText(mContext.getString(R.string.msg_copy));
                    }

                    textFileName = (TextView) progressDialog.findViewById(R.id.textFileName);
                    textFileSource = (TextView) progressDialog.findViewById(R.id.textFileFromPath);
                    textFileDest = (TextView) progressDialog.findViewById(R.id.textFileToPath);
                    textFilesLeft = (TextView) progressDialog.findViewById(R.id.textFilesLeft);
                    textProgressPercent = (TextView) progressDialog.findViewById(R.id
                            .textProgressPercent);
                    pasteProgress = (ProgressBar) progressDialog.findViewById(R.id
                            .progressBarPaste);
                    Button buttonBackground = (Button) progressDialog.findViewById(R.id.buttonBg);

                    String fileName = mSourceFilePath.substring(mSourceFilePath.lastIndexOf("/")
                            + 1, mSourceFilePath
                            .length());
                    textFileName.setText(fileName);
                    textFileSource.setText(mSourceFilePath);
                    textFileDest.setText(mCurrentDir);
                    progressDialog.show();
                    buttonBackground.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressDialog.dismiss();
                        }
                    });
                    break;
                case DELETE_OPERATION:
                    deleteDialog = new Dialog(mContext);
                    deleteDialog.setContentView(R.layout.dialog_delete);
                    deleteDialog.setCancelable(false);
                    textFileName = (TextView) deleteDialog.findViewById(R.id.textFileNames);
                    break;

            }


        }

        @Override
        protected Void doInBackground(HashMap<String, Integer>... params) {

            HashMap<String, Integer> pathActions = params[0];
//            android.os.Debug.waitForDebugger();
            switch (operation) {

                case PASTE_OPERATION:
                    totalFiles = pathActions.size();
                    if (pathActions.size() > 0) {
                        currentFile = 0;
                        for (String key : pathActions.keySet()) {
                            int action = pathActions.get(key);
                            String sourcePath = key;
                            System.out.println("key : " + key);
                            System.out.println("value : " + pathActions.get(key));
                            if (action == FileUtils.ACTION_CANCEL) {
                                isActionCancelled = true;
                            } else {

                                currentFile++;
                                System.out.println("currentFile BG : " + currentFile);

                                /*copyStatus = FileUtils.copyToDirectory(BaseActivity.this,
                                sourcePath, mCurrentDir,
                                        mIsMoveOperation, action, progress);*/
                                System.out.println("copyStatus : " + copyStatus);

                                if (copyStatus == 0) {
                                    filesCopied++;
                                }

                            }


                        }
                    }
                    break;

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            textFilesLeft.setText(currentFile + "/" + totalFiles);
            int progress = values[0];
            textProgressPercent.setText(progress + "%");
            pasteProgress.setProgress(progress);

            if (progress == 100 && currentFile == totalFiles) {
//                    System.out.println("progress ELSE: " + progress + "currentFile:" +
// currentFile);
                progressDialog.dismiss();
            }


        }

        @Override
        protected void onPostExecute(Void result) {
            FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                    .findFragmentById(R
                            .id.frame_container);

            switch (operation) {

                case PASTE_OPERATION:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {
                        mSelectedItemPositions.clear();
                    }
//                    togglePasteVisibility(false);
                    if (mPathActionMap != null) {
                        if (mPathActionMap.size() != filesCopied) {

                            if (isActionCancelled) {
                                showMessage(getString(R.string.msg_operation_cancel));
                            }
                            if (mIsMoveOperation) {
                                showMessage(getString(R.string.msg_move_failure));
                            } else {
                                showMessage(getString(R.string.msg_copy_failure));
                            }

                        }
                    }

                    if (filesCopied != 0) {
                        // Refresh the list after cut/copy operation
                        if (singlePaneFragment != null) {
                            singlePaneFragment.refreshList();
                        }
                        if (mIsMoveOperation) {
                            showMessage(getResources().getQuantityString(R.plurals
                                            .number_of_files, filesCopied,
                                    filesCopied) + " " +
                                    getString(R.string.msg_move_success));

                        } else {
                            showMessage(getResources().getQuantityString(R.plurals
                                            .number_of_files, filesCopied,
                                    filesCopied) + " " +
                                    getString(R.string.msg_copy_success));
                        }

                    }
                    mIsMoveOperation = false;
                    progressDialog.dismiss();
                    filesCopied = 0;
                    break;

                case DELETE_OPERATION:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {

                        mSelectedItemPositions.clear();

                    }
//                    Toast.makeText(FilebrowserULTRAActivity.this,
//                            " Delete successfull !", Toast.LENGTH_SHORT).show();
//                    refreshList();
                    progressDialog.dismiss();

                    break;
            }
        }

        public class Progress {
            private BackGroundOperationsTask task;

            public Progress(BackGroundOperationsTask task) {
                this.task = task;
            }

            public void publish(int val) {
                task.publishProgress(val);
            }
        }


    }


  /*  private class BgOperationsTask extends AsyncTask<ArrayList<String>, Void, Integer> {

        private String fileName;
        private String filePath;
        private int copyStatus = -1;
        //        private ProgressDialog progressDialog;
        private Dialog progressDialog;
        private Dialog deleteDialog;
        private int operation;
        private int currentFile = 0;
        private int filesCopied;
        private boolean isActionCancelled;
        TextView textFileName;
        private int totalFiles;
        private String sourcePath;


        private BgOperationsTask(int operation) {
            this.operation = operation;
            sourcePath = mSourceFilePath;

        }

        @Override
        protected Integer doInBackground(ArrayList<String>... params) {
            int deletedCount = 0;
            ArrayList<String> paths = params[0];
            totalFiles = paths.size();
            for (int i = 0; i < totalFiles; i++) {
                int result = FileUtils.deleteTarget(paths.get(i));
                if (result == 0) {
                    deletedCount++;
                    refreshList(paths.get(i));
                }
            }
            return deletedCount;
        }

        @Override
        protected void onPostExecute(Integer filesDel) {
            FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                    .findFragmentById(R
                            .id.frame_container);
            FileListDualFragment dualPaneFragment = (FileListDualFragment)
                    getSupportFragmentManager()
                            .findFragmentById(R
                                    .id.frame_container_dual);
            int deletedFiles = filesDel;
            switch (operation) {

                case DELETE_OPERATION:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {
                        mSelectedItemPositions.clear();
                    }

                    if (deletedFiles != 0) {
                        showMessage(getResources().getQuantityString(R.plurals.number_of_files,
                                deletedFiles,
                                deletedFiles) + " " +
                                getString(R.string.msg_delete_success));
                        if (singlePaneFragment != null) {
                            singlePaneFragment.refreshList();
                        }
                        if (dualPaneFragment != null) {
                            dualPaneFragment.refreshList();
                        }
                    }

                    if (totalFiles != deletedFiles) {
                        showMessage(getString(R.string.msg_delete_failure));
                    }
                    break;

            }
        }

    }

    private void refreshList(String path) {
        ContentResolver contentResolver = getContentResolver();
        switch (mCategory) {

            case 1:
                contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Audio.AudioColumns.DATA + "=?", new String[]{path});
                break;
            case 2:
                contentResolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Video.VideoColumns.DATA + "=?", new String[]{path});
                break;
            case 3:
                contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.ImageColumns.DATA + "=?", new String[]{path});
                break;
            case 4:
                contentResolver.delete(MediaStore.Files.getContentUri("external"),
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
                break;
        }
    }

    public void refreshFileList() {
        FileListFragment fileListFragment = (FileListFragment) getSupportFragmentManager()
                .findFragmentById(R.idonsh
                        .frame_container);
        if (fileListFragment != null) {
            fileListFragment.refreshList();
        }


    }*/

}

