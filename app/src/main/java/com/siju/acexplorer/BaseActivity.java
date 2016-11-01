package com.siju.acexplorer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.kobakei.ratethisapp.RateThisApp;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.FileListDualFragment;
import com.siju.acexplorer.filesystem.FileListFragment;
import com.siju.acexplorer.filesystem.HomeScreenFragment;
import com.siju.acexplorer.filesystem.helper.FileOpsHelper;
import com.siju.acexplorer.filesystem.model.BackStackModel;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.task.CopyService;
import com.siju.acexplorer.filesystem.task.DeleteTask;
import com.siju.acexplorer.filesystem.task.MoveFiles;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.settings.SettingsActivity;
import com.siju.acexplorer.utils.LocaleHelper;
import com.siju.acexplorer.utils.PermissionUtils;
import com.siju.acexplorer.utils.Utils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.siju.acexplorer.filesystem.utils.FileUtils.getInternalStorage;


public class BaseActivity extends AppCompatActivity implements
        View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    private ExpandableListAdapter expandableListAdapter;
    private ExpandableListView expandableListView;
    private List<String> mListHeader;
    private ArrayList<SectionGroup> totalGroup = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private NavigationView relativeLayoutDrawerPane;
    private String mCurrentDir;
    private String mCurrentDirDualPane;
    private String STORAGE_ROOT, STORAGE_INTERNAL, STORAGE_EXTERNAL, DOWNLOADS, IMAGES, VIDEO,
            MUSIC, DOCS, SETTINGS, RATE;
    private ArrayList<SectionItems> favouritesGroupChild = new ArrayList<>();
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private SharedPreferences mSharedPreferences;
    private ArrayList<FavInfo> savedFavourites = new ArrayList<>();
    private View mViewSeperator;
    private int mCategory = FileConstants.CATEGORY.FILES.getValue();
    private int mCategoryDual = FileConstants.CATEGORY.FILES.getValue();
    private CoordinatorLayout mMainLayout;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 200;
    private static final int PREFS_REQUEST = 1000;
    public static final int SAF_REQUEST = 3000;
    private Toolbar mToolbar;
    private final int MENU_FAVOURITES = 1;
    private boolean mIsFirstRun;
    public static final String PREFS_FIRST_RUN = "first_app_run";
    private boolean mIsDualPaneEnabled;
    private boolean mShowDualPane;
    private boolean mIsHomeScreenEnabled;
    private boolean mShowHidden;
    private boolean mIsHomePageAdded;
    private FrameLayout mFrameDualPane;
    private LinearLayout mNavigationLayout;
    private FloatingActionsMenu fabCreateMenu;
    private FloatingActionButton fabCreateFolder;
    private FloatingActionButton fabCreateFile;
    private FloatingActionsMenu fabCreateMenuDual;
    private FloatingActionButton fabCreateFolderDual;
    private FloatingActionButton fabCreateFileDual;
    private FrameLayout frameLayoutFab;
    private FrameLayout frameLayoutFabDual;
    private Toolbar mBottomToolbar;
    private LinearLayout navDirectory;
    private LinearLayout navDirectoryDualPane;
    // Returns true if user is currently navigating in Dual Panel fragment
    private boolean isDualPaneInFocus;
    private HorizontalScrollView scrollNavigation, scrollNavigationDualPane;
    private boolean isCurrentDirRoot;
    private boolean isCurrentDualDirRoot;
    private String mStartingDir;
    private String mStartingDirDualPane;
    private boolean mIsDualModeEnabled;
    private boolean mIsFromHomePage;
    private int mCurrentTheme = FileConstants.THEME_LIGHT;
    private ArrayList<String> mExternalSDPaths = new ArrayList<>();
    private ArrayList<BackStackModel> mBackStackList = new ArrayList<>();
    private ArrayList<BackStackModel> mBackStackListDual = new ArrayList<>();
    private ArrayList<SectionItems> storageGroupChild = new ArrayList<>();
    private int mCurrentOrientation;
    private boolean mIsRootMode;
    public int mOperation = -1;
    public String mOldFilePath;
    public String mNewFilePath;
    public ArrayList<FileInfo> mFiles = new ArrayList<>();
    public ArrayList<FileInfo> mTotalFiles = new ArrayList<>();
    public ArrayList<CopyData> mCopyData = new ArrayList<>();
    public FileOpsHelper mFileOpsHelper;
    public int mRenamedPosition;
    private Dialog mPermissionDialog;
    private boolean mIsTablet;
    private HomeScreenFragment mHomeScreenFragment;
    private String mCurrentLanguage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        Logger.log(TAG, "onCreate");

        initConstants();
        initViews();
        removeFragmentsOnPermissionRevoked(savedInstanceState);

        if (PermissionUtils.isAtLeastM()) {
            checkPermissions();
            Logger.log(TAG, "onCreate : useRuntimepermission");
        }
        setupInitialData();
        registerReceivers();
    }

    private void setTheme() {
        mCurrentTheme = ThemeUtils.getTheme(this);

        if (mCurrentTheme == FileConstants.THEME_DARK) {
            setTheme(R.style.Dark_AppTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

    }

    private void setLanguage() {
        mCurrentLanguage = LocaleHelper.getLanguage(this);

        if (!mCurrentLanguage.equals(Locale.getDefault().getLanguage())) {
            LocaleHelper.setLocale(this, mCurrentLanguage);
        }
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

    private void initViews() {
        mFrameDualPane = (FrameLayout) findViewById(R.id.frame_container_dual);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mNavigationLayout = (LinearLayout) findViewById(R.id.layoutNavigate);
        frameLayoutFab = (FrameLayout) findViewById(R.id.frameLayoutFab);
        frameLayoutFabDual = (FrameLayout) findViewById(R.id.frameLayoutFabDual);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(R.string.app_name);
        mMainLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        mBottomToolbar = (Toolbar) findViewById(R.id.toolbar_bottom);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        relativeLayoutDrawerPane = (NavigationView) findViewById(R.id.drawerPane);
        navDirectory = (LinearLayout) findViewById(R.id.navButtons);
        navDirectoryDualPane = (LinearLayout) findViewById(R.id.navButtonsDualPane);
        scrollNavigation = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
        scrollNavigationDualPane = (HorizontalScrollView) findViewById(R.id
                .scrollNavigationDualPane);
        mViewSeperator = findViewById(R.id.viewSeperator);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        DrawerArrowDrawable mArrowDrawable = new DrawerArrowDrawable(this);
        mToolbar.setNavigationIcon(mArrowDrawable);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        expandableListView = (ExpandableListView) findViewById(R.id.expand_list_drawer);
        View list_header = getLayoutInflater().inflate(R.layout.drawerlist_header, null);
        expandableListView.addHeaderView(list_header);

        fabCreateMenu = (FloatingActionsMenu) findViewById(R.id.fabCreate);
        fabCreateFolder = (FloatingActionButton) findViewById(R.id.fabCreateFolder);
        fabCreateFile = (FloatingActionButton) findViewById(R.id.fabCreateFile);

        fabCreateMenuDual = (FloatingActionsMenu) findViewById(R.id.fabCreateDual);
        fabCreateFolderDual = (FloatingActionButton) findViewById(R.id.fabCreateFolderDual);
        fabCreateFileDual = (FloatingActionButton) findViewById(R.id.fabCreateFileDual);


        setViewTheme();

        frameLayoutFabDual.getBackground().setAlpha(0);
        frameLayoutFab.getBackground().setAlpha(0);

        fabCreateMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener() {

            @Override
            public void onMenuExpanded() {
                if (fabCreateMenuDual != null) {
                    fabCreateMenuDual.setAlpha(0.10f);
                    fabCreateMenuDual.setEnabled(false);

                }
                frameLayoutFab.getBackground().setAlpha(240);
                frameLayoutFab.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        fabCreateMenu.collapse();
                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                frameLayoutFab.getBackground().setAlpha(0);
                if (fabCreateMenuDual != null) {
                    fabCreateMenuDual.setAlpha(1.0f);
                    fabCreateMenuDual.setEnabled(true);
                }
                frameLayoutFab.setOnTouchListener(null);
            }
        });

        fabCreateMenuDual.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener() {

            @Override
            public void onMenuExpanded() {
                frameLayoutFabDual.getBackground().setAlpha(240);
                frameLayoutFabDual.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        fabCreateMenuDual.collapse();
                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                frameLayoutFabDual.getBackground().setAlpha(0);
                frameLayoutFabDual.setOnTouchListener(null);
            }
        });

        mFileOpsHelper = new FileOpsHelper(this);

    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mLocaleListener, filter);
    }

    private void removeFragmentsOnPermissionRevoked(Bundle savedInstance) {

        if (savedInstance != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Remove previous fragments (case of the app was restarted after changed permission on android 6 and
            // higher)
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment != null) {
                        Logger.log(TAG, "onCreate--Fragment=" + fragment);
                        fragmentManager.beginTransaction().remove(fragment).commit();
                    }
                }
            }
        }
    }

    private void setViewTheme() {
        if (mCurrentTheme == FileConstants.THEME_DARK) {
            expandableListView.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_colorPrimary));
            mNavigationLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_colorPrimary));
            mBottomToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_colorPrimary));
            mToolbar.setPopupTheme(R.style.Dark_AppTheme_PopupOverlay);
            frameLayoutFab.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_overlay));
            frameLayoutFabDual.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_overlay));
            mMainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_colorPrimary));
        }
    }

    /**
     * Called for the 1st time when app is launched to check permissions
     */
    private void checkPermissions() {
        if (!PermissionUtils.hasRequiredPermissions()) {
            fabCreateMenu.setVisibility(View.GONE);
            requestPermission();
        } else {
            Logger.log(TAG, "checkPermissions ELSE");
            fabCreateMenu.setVisibility(View.VISIBLE);
        }

    }

    private void requestPermission() {
        Logger.log(TAG, "requestPermission");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                .WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:

                if (PermissionUtils.hasRequiredPermissions()) {
                    Logger.log(TAG, "Permission granted");
                    fabCreateMenu.setVisibility(View.VISIBLE);
                    setPermissionGranted();
                    dismissRationaleDialog();
                } else {
                    showRationale();
                    fabCreateMenu.setVisibility(View.GONE);
                }
        }
    }

    private void setPermissionGranted() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (mIsHomeScreenEnabled) {
            ((HomeScreenFragment) fragment).setPermissionGranted();
        }
    }

    private void setupInitialData() {
        checkPreferences();
        checkScreenOrientation();
        prepareListData();
        initialScreenSetup(mIsHomeScreenEnabled);
        getSavedFavourites();
        initializeGroups();
        initListeners();
        setListAdapter();
    }

    private void showRationale() {
        Log.d(TAG, "showRationale");

        final boolean showSettings;
        Button buttonGrant;
        TextView textViewPermissionHint;

        showSettings = !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE);
        if (mPermissionDialog == null) {
            mPermissionDialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
            mPermissionDialog.setContentView(R.layout.dialog_runtime_permissions);

        }
        mPermissionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "Rationale dismiss");
                if (!PermissionUtils.hasRequiredPermissions()) {
                    mPermissionDialog.dismiss();
                    finish();
                }
            }
        });
        buttonGrant = (Button) mPermissionDialog.findViewById(R.id.buttonGrant);
        textViewPermissionHint = (TextView) mPermissionDialog.findViewById(R.id.textPermissionHint);
        if (showSettings) {
            buttonGrant.setText(R.string.action_settings);
            textViewPermissionHint.setVisibility(View.VISIBLE);
        }
        buttonGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showSettings) {
                    requestPermission();
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, SETTINGS_REQUEST);
                }
            }
        });

        mPermissionDialog.show();
    }

    private void dismissRationaleDialog() {
        if (mPermissionDialog != null && mPermissionDialog.isShowing()) {
            mPermissionDialog.dismiss();
        }
    }

    private void checkPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPreferences.getBoolean(PREFS_FIRST_RUN, true)) {
            // If app is first run
            mIsFirstRun = true;
            mSharedPreferences.edit().putInt(FileConstants.KEY_SORT_MODE, FileConstants
                    .KEY_SORT_NAME).apply();
            mIsTablet = Utils.isTablet(this);
            if (mIsTablet) {
                Logger.log(TAG, "Istab");
                mSharedPreferences.edit().putBoolean(FileConstants.PREFS_DUAL_PANE, true).apply();
            }
        }
        mIsHomeScreenEnabled = mSharedPreferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        mShowHidden = mSharedPreferences.getBoolean(FileConstants.PREFS_HIDDEN, false);
        mIsDualPaneEnabled = mSharedPreferences.getBoolean(FileConstants.PREFS_DUAL_PANE,
                mIsTablet);
        mIsRootMode = mSharedPreferences.getBoolean(FileConstants.ROOT_ACCESS, true);
    }


    private void getSavedFavourites() {
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        savedFavourites = sharedPreferenceWrapper.getFavorites(this);
    }

    private void initListeners() {
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int
                    childPosition, long
                                                id) {
                Logger.log(TAG, "Group pos-->" + groupPosition + "CHILD POS-->" + childPosition);
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
        fabCreateFile.setOnClickListener(this);
        fabCreateFileDual.setOnClickListener(this);
        fabCreateFolder.setOnClickListener(this);
        fabCreateFolderDual.setOnClickListener(this);
    }

    private void prepareListData() {

        String[] listDataHeader = getResources().getStringArray(R.array.expand_headers);
        mListHeader = Arrays.asList(listDataHeader);
/*        Thread storageThread = new Thread(storageRunnable);
        storageThread.start();*/
        initializeStorageGroup();
    }

    private Runnable storageRunnable = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            initializeStorageGroup();
        }
    };

    private void initializeGroups() {
        initializeFavouritesGroup();
        initializeLibraryGroup();
        initializeOthersGroup();
    }

    private void initializeStorageGroup() {
        Logger.log(TAG, "initializeStorageGroup START");
        List<String> storagePaths = FileUtils.getStorageDirectories(this, true);

        File systemDir = FileUtils.getRootDirectory();
        File rootDir = systemDir.getParentFile();

        long spaceLeftRoot = getSpaceLeft(systemDir);
        long totalSpaceRoot = getTotalSpace(systemDir);
        int leftProgressRoot = (int) (((float) spaceLeftRoot / totalSpaceRoot) * 100);
        int progressRoot = 100 - leftProgressRoot;
        storageGroupChild.add(new SectionItems(STORAGE_ROOT, storageSpace(spaceLeftRoot, totalSpaceRoot), R.drawable
                .ic_root_white, FileUtils.getAbsolutePath(rootDir), progressRoot));

        for (String path : storagePaths) {
            File file = new File(path);
            int icon;
            String name;
            if ("/storage/emulated/legacy".equals(path) || "/storage/emulated/0".equals(path)) {
                name = STORAGE_INTERNAL;
                icon = R.drawable.ic_phone_white;

            } else if ("/storage/sdcard1".equals(path)) {
                name = STORAGE_EXTERNAL;
                icon = R.drawable.ic_ext_white;
                mExternalSDPaths.add(path);
            } else {
                name = file.getName();
                icon = R.drawable.ic_ext_white;
                mExternalSDPaths.add(path);
            }
            if (!file.isDirectory() || file.canExecute()) {
                long spaceLeft = getSpaceLeft(file);
                long totalSpace = getTotalSpace(file);
                int leftProgress = (int) (((float) spaceLeft / totalSpace) * 100);
                int progress = 100 - leftProgress;
                String spaceText = storageSpace(spaceLeft, totalSpace);
                storageGroupChild.add(new SectionItems(name, spaceText, icon, path, progress));
            }
        }
        Logger.log(TAG, "initializeStorageGroup END");

        totalGroup.add(new SectionGroup(mListHeader.get(0), storageGroupChild));
    }

    private long getSpaceLeft(File file) {
        return file.getFreeSpace();
    }

    private long getTotalSpace(File file) {
        return file.getTotalSpace();
    }

    private String storageSpace(long spaceLeft, long totalSpace) {
        String freePlaceholder = " " + getResources().getString(R.string.msg_free) + " ";
        return FileUtils.formatSize(this, spaceLeft) + freePlaceholder +
                FileUtils.formatSize(this, totalSpace);
    }

    public ArrayList<SectionItems> getStorageGroupList() {
        return storageGroupChild;
    }

    private void initializeFavouritesGroup() {
        if (mIsFirstRun) {
            String path = FileUtils
                    .getAbsolutePath(FileUtils.getDownloadsDirectory());
            favouritesGroupChild.add(new SectionItems(DOWNLOADS, path, R.drawable.ic_download,
                    path, 0));
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
                        savedPath, 0));
            }
        }
        totalGroup.add(new SectionGroup(mListHeader.get(1), favouritesGroupChild));
    }

    private void initializeLibraryGroup() {
        ArrayList<SectionItems> libraryGroupChild = new ArrayList<>();
        libraryGroupChild.add(new SectionItems(MUSIC, null, R.drawable.ic_music_white, null, 0));
        libraryGroupChild.add(new SectionItems(VIDEO, null, R.drawable.ic_video_white, null, 0));
        libraryGroupChild.add(new SectionItems(IMAGES, null, R.drawable.ic_photos_white, null, 0));
        libraryGroupChild.add(new SectionItems(DOCS, null, R.drawable.ic_file_white, null, 0));
        totalGroup.add(new SectionGroup(mListHeader.get(2), libraryGroupChild));
    }

    private void initializeOthersGroup() {
        ArrayList<SectionItems> othersGroupChild = new ArrayList<>();
        othersGroupChild.add(new SectionItems(RATE, "", R.drawable.ic_rate_white, null, 0));
        othersGroupChild.add(new SectionItems(SETTINGS, "", R.drawable.ic_settings_white, null, 0));
        totalGroup.add(new SectionGroup(mListHeader.get(3), othersGroupChild));
    }

    private void setListAdapter() {
        expandableListAdapter = new ExpandableListAdapter(this, totalGroup);
        expandableListView.setAdapter(expandableListAdapter);
        for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE
                && mIsDualPaneEnabled) {
            mIsDualModeEnabled = true;
        }
    }


    private void initialScreenSetup(boolean isHomeScreenEnabled) {
        if (isHomeScreenEnabled) {
            hideFab();
            mNavigationLayout.setVisibility(View.GONE);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, true);
            args.putBoolean(BaseActivity.PREFS_FIRST_RUN, mIsFirstRun);
            args.putBoolean(FileConstants.KEY_DUAL_ENABLED, mIsDualModeEnabled);
            mHomeScreenFragment = new HomeScreenFragment();
            mHomeScreenFragment.setArguments(args);
//            ft.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
            ft.replace(R.id.main_container, mHomeScreenFragment);
            ft.addToBackStack(null);
            Logger.log(TAG, "initialScreenSetup");
            ft.commitAllowingStateLoss();
        } else {
            showFab();
            // Initialising only if Home screen disabled
            currentScreenSetup(getInternalStorage().getAbsolutePath(), FileConstants.CATEGORY.FILES.getValue(), false);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, mCurrentDir);
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.replace(R.id.main_container, fileListFragment, mCurrentDir);
            ft.commitAllowingStateLoss();
            if (mIsDualModeEnabled) {
                toggleDualPaneVisibility(true);
                showDualPane();
            }
        }
    }

    private BroadcastReceiver mLocaleListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                restartApp(true);
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    protected void onResume() {
        Logger.log(TAG, "oNResume--mPermissionDialog" + mPermissionDialog + " " + PermissionUtils
                .hasRequiredPermissions());

        /**
         * This handles the scenario when snackbar is shown and user presses home and grants access to app and
         * returns to app. In that case,setupInitialData the data and dismiss the snackbar.
         */

        if (mPermissionDialog != null && mPermissionDialog.isShowing()) {
            if (PermissionUtils.hasRequiredPermissions()) {
                mPermissionDialog.dismiss();
                setPermissionGranted();
//                mSnackbar.dismiss();
         /*       setupInitialData();
                setUpInitialData();*/
            }
        }

        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Called when user returns from the settings screen
        if (requestCode == PREFS_REQUEST) {
            Logger.log(TAG, "OnActivityResult=" + resultCode);
            if (resultCode == RESULT_OK) {
                if (intent != null && intent.getBooleanExtra(FileConstants.PREFS_RESET, false)) {
                    resetFavouritesGroup();
                    expandableListView.smoothScrollToPosition(0);
                }
                onSharedPrefsChanged();

            }
        } else if (requestCode == SETTINGS_REQUEST) {
            // User clicked the Setting button and we have permissions,setupInitialData the data
            if (PermissionUtils.hasRequiredPermissions()) {
//                fabCreateMenu.setVisibility(View.VISIBLE);
                setPermissionGranted();
           /*     setupInitialData();
                setUpInitialData();*/
            } else {
                // User clicked the Setting button and we don't permissions,show snackbar again
                showRationale();
            }
        } else if (requestCode == SAF_REQUEST) {
            String uriString = mSharedPreferences.getString(FileConstants.SAF_URI, null);

            Uri oldUri = uriString != null ? Uri.parse(uriString) : null;

            if (resultCode == Activity.RESULT_OK) {
                Uri treeUri = intent.getData();
                Log.d(TAG, "tree uri=" + treeUri + " old uri=" + oldUri);
                // Get Uri from Storage Access Framework.
                // Persist URI - this is required for verification of writability.
                if (treeUri != null) {
                    mSharedPreferences.edit().putString(FileConstants.SAF_URI, treeUri.toString()).apply();
                    int takeFlags = intent.getFlags();
                    takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                    switch (mOperation) {
                        case FileConstants.DELETE:
                            new DeleteTask(this, mIsRootMode, mFiles).execute();
                            mFiles = new ArrayList<>();
                            break;
                        case FileConstants.COPY:
                            Intent intent1 = new Intent(this, CopyService.class);
                            intent1.putParcelableArrayListExtra("FILE_PATHS", mFiles);
                            intent1.putExtra("COPY_DIRECTORY", mNewFilePath);
                            intent.putParcelableArrayListExtra("ACTION", mCopyData);
                            intent1.putParcelableArrayListExtra("TOTAL_LIST", mTotalFiles);
                            new FileUtils().showCopyProgressDialog(this, intent1);
                            break;
                        case FileConstants.MOVE:
                            new MoveFiles(this, mFiles, mCopyData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                    mNewFilePath);
                            break;
                        case FileConstants.FOLDER_CREATE:
                            mFileOpsHelper.mkDir(mIsRootMode, new File(mNewFilePath));
                            break;
                        case FileConstants.RENAME:
                            mFileOpsHelper.renameFile(mIsRootMode, new File(mOldFilePath), new File(mNewFilePath),
                                    mRenamedPosition);
                            break;
                        case FileConstants.FILE_CREATE:
                            mFileOpsHelper.mkFile(mIsRootMode, new File(mNewFilePath));
                            break;
                        case FileConstants.EXTRACT:
                            mFileOpsHelper.extractFile(new File(mOldFilePath), new File(mNewFilePath));
                            break;
                        case FileConstants.COMPRESS:
                            mFileOpsHelper.compressFile(new File(mNewFilePath), mFiles);
                    }
                }
            }
            // If not confirmed SAF, or if still not writable, then revert settings.
            else {

                if (oldUri != null)
                    mSharedPreferences.edit().putString(FileConstants.SAF_URI, oldUri.toString()).apply();

                Toast.makeText(this, getString(R.string.access_denied_external), Toast.LENGTH_LONG).show();
                return;
            }

            mOperation = -1;
            mNewFilePath = null;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    public void updateFavourites(ArrayList<FavInfo> favInfoArrayList) {
        int count = 0;
        for (int i = 0; i < favInfoArrayList.size(); i++) {
            SectionItems favItem = new SectionItems(favInfoArrayList.get(i).getFileName(), favInfoArrayList.get(i)
                    .getFilePath(), R.drawable.ic_fav_folder, favInfoArrayList.get(i).getFilePath(), 0);
            if (!favouritesGroupChild.contains(favItem)) {
                favouritesGroupChild.add(favItem);
                count++;
            }
        }
        if (mIsHomeScreenEnabled && mHomeScreenFragment != null) {
            mHomeScreenFragment.updateFavoritesCount(count);
        }
        expandableListAdapter.notifyDataSetChanged();

    }

    public void removeFavourites(ArrayList<FavInfo> favInfoArrayList) {
        int count = 0;
        for (int i = 0; i < favInfoArrayList.size(); i++) {
            SectionItems favItem = new SectionItems(favInfoArrayList.get(i).getFileName(), favInfoArrayList.get(i)
                    .getFilePath(), R.drawable.ic_fav_folder, favInfoArrayList.get(i).getFilePath(), 0);
            if (favouritesGroupChild.contains(favItem)) {
                favouritesGroupChild.remove(favItem);
                count++;
            }
        }
        if (mIsHomeScreenEnabled && mHomeScreenFragment != null) {
            mHomeScreenFragment.removeFavorites(count);
        }
        expandableListAdapter.notifyDataSetChanged();
    }


    public void addHomeNavButton(boolean isFilesCategory) {
        if (mIsHomeScreenEnabled) {
            if (!isDualPaneInFocus) {
                navDirectory.removeAllViews();
            }
            ImageButton imageButton = new ImageButton(this);
            imageButton.setImageResource(R.drawable.ic_home_white);
            imageButton.setBackgroundColor(Color.parseColor("#00ffffff"));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            imageButton.setLayoutParams(params);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeFragmentFromBackStack();
                }
            });
            if (!isDualPaneInFocus) {
                navDirectory.addView(imageButton);
            } else {
                navDirectoryDualPane.addView(imageButton);
            }

            ImageView navArrow = new ImageView(this);
            params.leftMargin = 15;
            params.rightMargin = 20;
            navArrow.setImageResource(R.drawable.ic_arrow_nav);
            navArrow.setLayoutParams(params);
            if (!isDualPaneInFocus) {
                navDirectory.addView(navArrow);
            } else {
                navDirectoryDualPane.addView(navArrow);
            }
            addTitleText(isFilesCategory);
        } else {
            addTitleText(isFilesCategory);
        }

    }

    public void addTitleText(boolean isFilesCategory) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        if (!isFilesCategory) {
            String title = FileUtils.getTitleForCategory(this, mCategory);
            final TextView textView = new TextView(this);
            textView.setText(title);
            textView.setTextColor(ContextCompat.getColor(this, R.color.navButtons));
            textView.setTextSize(19);
            int paddingRight = getResources().getDimensionPixelSize(R.dimen.padding_60);
            textView.setPadding(0, 0, paddingRight, 0);
            textView.setLayoutParams(params);
            if (!isDualPaneInFocus) {
                navDirectory.addView(textView);
            } else {
                navDirectoryDualPane.addView(textView);
            }
        }
    }


    public void setNavDirectory(String path, boolean isDualPane) {
        String[] parts;
        parts = path.split(File.separator);
        isDualPaneInFocus = isDualPane;

        if (!isDualPaneInFocus) {
            navDirectory.removeAllViews();
            mCurrentDir = path;
        } else {
            navDirectoryDualPane.removeAllViews();
            mCurrentDirDualPane = path;
        }


        String dir = "";

        addHomeNavButton(true);

        // If root dir , parts will be 0
        if (parts.length == 0) {
            isCurrentDirRoot = true;
            mStartingDir = File.separator;
            setNavDir(File.separator, File.separator); // Add Root button
        } else {
            int count = 0;
            for (int i = 1; i < parts.length; i++) {
                dir += File.separator + parts[i];

//                if (!isCurrentDirRoot) {
                if (!isDualPaneInFocus) {
//                    Logger.log(TAG, "setNavDirectory--dir=" + dir + "  Starting dir=" + mStartingDir);

                    if (!dir.contains(mStartingDir)) {
                        continue;
                    }
                } else {
//                    Logger.log(TAG, "setNavDirectory--dir=" + dir + "  Starting DUAL dir=" + mStartingDirDualPane);

                    if (!dir.contains(mStartingDirDualPane)) {
                        continue;
                    }
                }
//                }
                /*Count check so that ROOT is added only once in Navigation
                  Handles the scenario :
                  1. When Fav item is a root child and if we click on any folder in that fav item
                     multiple ROOT blocks are not added to Navigation view*/
                if (!isDualPaneInFocus) {
                    if (isCurrentDirRoot && count == 0) {
                        setNavDir(File.separator, File.separator);
                    }
                } else {
                    if (isCurrentDualDirRoot && count == 0) {
                        setNavDir(File.separator, File.separator);
                    }
                }

                count++;
                setNavDir(dir, parts[i]);
            }
        }

    }

    private void setNavDir(String dir, String parts) {


        int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (dir.equals(getInternalStorage().getAbsolutePath())) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_INTERNAL, dir);
        } else if (dir.equals(File.separator)) {
            createNavButton(STORAGE_ROOT, dir);
        } else if (mExternalSDPaths != null && mExternalSDPaths.contains(dir)) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_EXTERNAL, dir);
        } else {
            ImageView navArrow = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT,
                    WRAP_CONTENT);
            layoutParams.leftMargin = 20;
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            navArrow.setImageResource(R.drawable.ic_arrow_nav);
            navArrow.setLayoutParams(layoutParams);

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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        if (text.equals(STORAGE_INTERNAL) || text.equals(STORAGE_EXTERNAL) ||
                text.equals(STORAGE_ROOT)) {
            ImageButton imageButton = new ImageButton(this);
            if (text.equals(STORAGE_INTERNAL)) {
                imageButton.setImageResource(R.drawable.ic_storage_white_nav);
            } else if (text.equals(STORAGE_EXTERNAL)) {
                imageButton.setImageResource(R.drawable.ic_ext_nav);
            } else {
                imageButton.setImageResource(R.drawable.ic_root_white_nav);
            }
            imageButton.setBackgroundColor(Color.parseColor("#00ffffff"));
            imageButton.setLayoutParams(params);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navButtonOnClick(view, dir);
                }
            });
            if (!isDualPaneInFocus) {
                navDirectory.addView(imageButton);
            } else {
                navDirectoryDualPane.addView(imageButton);
            }

        } else {
            final TextView textView = new TextView(this);
            textView.setText(text);
            textView.setAllCaps(true);
            textView.setTextColor(ContextCompat.getColor(this, R.color.navButtons));
            textView.setTextSize(15);
            params.leftMargin = 20;
            textView.setPadding(0, 0, 35, 0);
            textView.setLayoutParams(params);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Logger.log(TAG, "nav button onclick--dir=" + dir);
                    navButtonOnClick(view, dir);
                }
            });
            if (!isDualPaneInFocus) {
                navDirectory.addView(textView);
            } else {
                navDirectoryDualPane.addView(textView);
            }
        }


    }

    private void navButtonOnClick(View view, final String dir) {
        Logger.log(TAG, "Dir=" + dir);

        FileListFragment fileListFragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id
                .main_container);

        FileListDualFragment fileListDualFragment = (FileListDualFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_container_dual);

        boolean isDualPaneButtonClicked;
        LinearLayout parent = (LinearLayout) view.getParent();
        isDualPaneButtonClicked = parent.getId() != navDirectory.getId();

        if (!isDualPaneButtonClicked) {
            if (fileListFragment.isZipMode()) {
                if (fileListFragment.isInZipMode(dir)) {
                    int newSize = mBackStackList.size() - 1;

                    mBackStackList.remove(newSize);
                    mCurrentDir = mBackStackList.get(newSize - 1).getFilePath();
                    mCategory = mBackStackList.get(newSize - 1).getCategory();
                    fileListFragment.reloadList(true, mCurrentDir);
                    if (!mIsFromHomePage) {
                        setNavDirectory(mCurrentDir, false);
                    } else {
                        hideFab();
                    }
                }
            } else {
                if (!mCurrentDir.equals(dir)) {
                    mCurrentDir = dir;
                    int position = 0;
                    for (int i = 0; i < mBackStackList.size(); i++) {
                        if (mCurrentDir.equals(mBackStackList.get(i).getFilePath())) {
                            position = i;
                            break;
                        }
                    }
                    for (int j = mBackStackList.size() - 1; j > position; j--) {
                        mBackStackList.remove(j);
                    }

               /* if (fileListFragment.isZipMode()) {
                    fileListFragment.endZipMode();
                    fileListFragment.setCategory(FileConstants.CATEGORY.FILES.getValue());
                }*/
                    fileListFragment.reloadList(true, mCurrentDir);
                    setNavDirectory(mCurrentDir, false);
                }
            }
        } else {
            if (fileListDualFragment.isZipMode()) {
                if (fileListDualFragment.isInZipMode(dir)) {
                    int newSize = mBackStackListDual.size() - 1;

                    mBackStackListDual.remove(newSize);
                    mCurrentDirDualPane = mBackStackListDual.get(newSize - 1).getFilePath();
                    mCategoryDual = mBackStackListDual.get(newSize - 1).getCategory();
                    fileListFragment.reloadList(true, mCurrentDirDualPane);
                    if (!mIsFromHomePage) {
                        setNavDirectory(mCurrentDirDualPane, false);
                    } else {
                        hideFab();
                    }
                }
            } else {
                if (!mCurrentDirDualPane.equals(dir)) {
                    mCurrentDirDualPane = dir;
                    int position = 0;
                    for (int i = 0; i < mBackStackListDual.size(); i++) {
                        if (mCurrentDir.equals(mBackStackListDual.get(i).getFilePath())) {
                            position = i;
                            break;
                        }
                    }
                    for (int j = mBackStackListDual.size() - 1; j > position; j--) {
                        mBackStackListDual.remove(j);
                    }

                    fileListDualFragment.reloadList(true, mCurrentDirDualPane);
                    setNavDirectory(mCurrentDirDualPane, true);
                }
            }
        }

    }


    /**
     * Show dual pane in Landscape mode
     */
    private void showDualPane() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        if (fragment instanceof HomeScreenFragment) {
            mIsDualModeEnabled = true;
            ((HomeScreenFragment) fragment).setDualModeEnabled(true);
        }
        // For Files category only, show dual pane
        else if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
            mIsDualModeEnabled = true;
            isDualPaneInFocus = true;
            toggleDualPaneVisibility(true);
            ((FileListFragment) fragment).refreshSpan();
            currentScreenSetup(FileUtils.getInternalStorage().getAbsolutePath(), FileConstants
                    .CATEGORY.FILES.getValue(), isDualPaneInFocus);
            createDualFragment();
        }
    }

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
                if (view.getId() == R.id.fabCreateFolder || view.getId() == R.id
                        .fabCreateFolderDual) {
                    mOperation = FileConstants.FOLDER_CREATE;
                    String path = isDualPaneInFocus ? mCurrentDirDualPane : mCurrentDir;
                    new FileUtils().createDirDialog(this, mIsRootMode, path);
                } else {
                    mOperation = FileConstants.FILE_CREATE;
                    String path = isDualPaneInFocus ? mCurrentDirDualPane : mCurrentDir;
                    new FileUtils().createFileDialog(this, mIsRootMode, path);
                }
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                ((FileListFragment) fragment).setBackPressed();
                if (isDualPaneInFocus) {
                    Fragment dualFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
                    ((FileListDualFragment) dualFragment).setBackPressed();
                }
                break;
        }
    }

    private boolean checkIfSameItemClicked(String path, int category) {
        if (!isDualPaneInFocus)
            return mCurrentDir != null && mCurrentDir.equals(path) && mCategory == category;
        else
            return mCurrentDirDualPane != null && mCurrentDirDualPane.equals(path) && mCategoryDual
                    == category;

    }

    private void displaySelectedGroup(int groupPos, int childPos, String path) {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        switch (groupPos) {
            case 0:
            case 1:
                if (!isDualPaneInFocus)
                    isCurrentDirRoot = groupPos == 0 && childPos == 0;
                else
                    isCurrentDualDirRoot = groupPos == 0 && childPos == 0;


                mToolbar.setTitle(getString(R.string.app_name));
                if (fragment instanceof FileListFragment) {
//                    mNavigationLayout.setVisibility(View.VISIBLE);
                    showFab();
                    toggleDualPaneVisibility(true);
//                    fabCreateMenu.setVisibility(View.VISIBLE);
                }

                Logger.log(TAG, "displaySelectedGroup--mCurrentdir=" + mCurrentDir + "isdualpane" +
                        "=" + isDualPaneInFocus + " dual dir=" + mCurrentDirDualPane);
                if (!isDualPaneInFocus) {

                    if (mCurrentDir == null || !mCurrentDir.equals(path)) {
                        actionOnDrawerItemClick(path, groupPos);
                    }


                } else {
                    if (mCurrentDirDualPane == null || !mCurrentDirDualPane.equals(path)) {
                        actionOnDrawerItemClick(path, groupPos);
                    }

                }
                break;
            // When Library category item is clicked
            case 2:

                hideFab();
                toggleDualPaneVisibility(false);
                toggleNavigationVisibility(true);
                int category;

                switch (childPos) {
                    // When Audio item is clicked
                    case 0:
                        category = FileConstants.CATEGORY.AUDIO.getValue();
                        openCategoryItem(path, category);
                        break;
                    // When Video item is clicked
                    case 1:
                        category = FileConstants.CATEGORY.VIDEO.getValue();
                        openCategoryItem(path, category);
                        break;
                    // When Images item is clicked
                    case 2:
                        category = FileConstants.CATEGORY.IMAGE.getValue();
                        openCategoryItem(path, category);
                        break;
                    // When Documents item is clicked
                    case 3:
                        category = FileConstants.CATEGORY.DOCS.getValue();
                        openCategoryItem(path, category);
                        break;
                }
//                setTitleForCategory(mCategory);
                break;
        }
    }

    private void actionOnDrawerItemClick(String path, int groupPos) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (!isDualPaneInFocus) {
            mCurrentDir = path;
        } else {
            mCurrentDirDualPane = path;
        }
        initializeStartingDirectory();
        checkIfFavIsRootDir(groupPos);

        if (!isDualPaneInFocus) {
            mCategory = FileConstants.CATEGORY.FILES.getValue();
            displayInitialFragment(mCurrentDir, mCategory);
            if (fragment instanceof FileListFragment) {
                setNavDirectory(mCurrentDir, isDualPaneInFocus);
            }
            addToBackStack(mCurrentDir, mCategory);
        } else {
            mCategoryDual = FileConstants.CATEGORY.FILES.getValue();
            displayInitialFragment(mCurrentDirDualPane, mCategoryDual);
            if (fragment instanceof FileListFragment) {
                setNavDirectory(mCurrentDirDualPane, isDualPaneInFocus);
            }
            addToBackStack(mCurrentDirDualPane, mCategoryDual);
        }

        if (fragment instanceof HomeScreenFragment) {
            if (mIsDualModeEnabled) {
                toggleDualPaneVisibility(true);
                createDualFragment();
                currentScreenSetup(FileUtils.getInternalStorage().getAbsolutePath(),
                        FileConstants.CATEGORY.FILES.getValue(), true);
            }
        }
    }

    private void openCategoryItem(String path, int category) {
        if (!checkIfSameItemClicked(path, category)) {
            if (!isDualPaneInFocus) {
                mCategory = category;
                mCurrentDir = null;
                displayInitialFragment(null, mCategory);
                addToBackStack(mCurrentDir, mCategory);
            } else {
                mCategoryDual = category;
                mCurrentDirDualPane = null;
                displayInitialFragment(null, mCategoryDual);
                addToBackStack(mCurrentDirDualPane, mCategoryDual);
            }
        }
    }

    public void addToBackStack(String path, int category) {
        if (!isDualPaneInFocus) {
            mBackStackList.add(new BackStackModel(path, category));
            Logger.log(TAG, "Back stack--size=" + mBackStackList.size() + " Path=" + path + "Category=" + category);

        } else {
            mBackStackListDual.add(new BackStackModel(path, category));
            Logger.log(TAG, "Back stack DUAL--size=" + mBackStackList.size() + " Path=" + path +
                    "Category=" + category);
        }
    }

    private void checkIfFavIsRootDir(int groupPos) {
        if (groupPos == 1) {

            if (!isDualPaneInFocus) {
                if (!mCurrentDir.contains(getInternalStorage().getAbsolutePath()) &&
                        (mExternalSDPaths != null && !mExternalSDPaths.contains(mCurrentDir))) {
                    isCurrentDirRoot = true;
                    mStartingDir = File.separator;
                }
            } else {
                if (!mCurrentDirDualPane.contains(getInternalStorage().getAbsolutePath()) &&
                        (mExternalSDPaths != null && !mExternalSDPaths.contains(mCurrentDirDualPane))) {
                    isCurrentDualDirRoot = true;
                    mStartingDirDualPane = File.separator;
                }
            }
        }
    }

    public void initializeStartingDirectory() {
        if (!isDualPaneInFocus) {
            if (mCurrentDir.contains(FileUtils.getInternalStorage().getAbsolutePath())) {
                mStartingDir = FileUtils.getInternalStorage().getAbsolutePath();
                isCurrentDirRoot = false;
            } else if (mExternalSDPaths != null && mExternalSDPaths.size() > 0) {
                for (String path : mExternalSDPaths) {
                    if (mCurrentDir.contains(path)) {
                        mStartingDir = path;
                        isCurrentDirRoot = false;
                        return;
                    }
                }
                mStartingDir = File.separator;
            } else {
                mStartingDir = File.separator;
            }
            Logger.log(TAG, "initializeStartingDirectory--startingdir=" + mStartingDir);

        } else {
            if (mCurrentDirDualPane.contains(FileUtils.getInternalStorage().getAbsolutePath())) {
                mStartingDirDualPane = FileUtils.getInternalStorage().getAbsolutePath();
                isCurrentDualDirRoot = false;
            } else if (mExternalSDPaths != null && mExternalSDPaths.size() > 0) {
                for (String path : mExternalSDPaths) {
                    if (mCurrentDirDualPane.contains(path)) {
                        mStartingDirDualPane = path;
                        isCurrentDualDirRoot = false;
                        return;
                    }
                }
                mStartingDirDualPane = File.separator;
            } else {
                mStartingDirDualPane = File.separator;
            }
            Logger.log(TAG, "initializeStartingDirectory--startingdirDUAL=" + mStartingDirDualPane);
        }
    }

    private void setTitleForCategory(int category) {
        switch (category) {
            case 0:
                mToolbar.setTitle(R.string.app_name);
                break;
            case 1:
                mToolbar.setTitle(MUSIC);
                break;
            case 2:
                mToolbar.setTitle(VIDEO);
                break;
            case 3:
                mToolbar.setTitle(IMAGES);
                break;
            case 4:
                mToolbar.setTitle(DOCS);
                break;
            default:
                mToolbar.setTitle(R.string.app_name);
        }
    }

    private void displayInitialFragment(String directory, int category) {
        // update the main content by replacing fragments
        // Fragment fragment = null;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        args.putInt(FileConstants.KEY_CATEGORY, category);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof HomeScreenFragment) {
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.add(R.id.main_container, fileListFragment, directory);
            ft.hide(fragment);
            ft.addToBackStack(null);
            ft.commit();
//            ft.commitAllowingStateLoss();
            showFab();
            boolean value = mCategory == FileConstants.CATEGORY.FILES.getValue();
            toggleDualPaneVisibility(value);

            if (isDualPaneInFocus) {
                args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
                FileListDualFragment fileListDualFragment = new FileListDualFragment();
                fileListDualFragment.setArguments(args);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_container_dual, fileListDualFragment, directory);
                fragmentTransaction.commitAllowingStateLoss();
            }
        } else {

/*
            FileListFragment fileListFragment = new FileListFragment();



            fileListFragment.setArguments(args);
            ft.replace(R.id.main_container, fileListFragment, directory);
//            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
*/

            FileListDualFragment dualFragment = (FileListDualFragment)
                    getSupportFragmentManager()
                            .findFragmentById(R.id
                                    .frame_container_dual);

            if (isDualPaneInFocus) {

                if (dualFragment == null) {
                    args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
                    FileListDualFragment fileListDualFragment = new FileListDualFragment();
                    fileListDualFragment.setArguments(args);
                    ft.replace(R.id.frame_container_dual, fileListDualFragment, directory);
                    ft.commitAllowingStateLoss();
                } else {
                    dualFragment.setCategory(category);
                    if (dualFragment.isZipMode()) {
                        BackStackModel model = dualFragment.endZipMode();
                        mBackStackListDual.remove(model);
                    }

                    dualFragment.reloadList(false, directory);
                }
            } else {
                ((FileListFragment) fragment).setCategory(category);
                if (((FileListFragment) fragment).isZipMode()) {
                    BackStackModel model = ((FileListFragment) fragment).endZipMode();
                    mBackStackList.remove(model);
                }

                ((FileListFragment) fragment).reloadList(false, directory);
            }
        }
        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }


    public void showFab() {
        frameLayoutFab.setVisibility(View.VISIBLE);
    }

    public void hideFab() {
        frameLayoutFab.setVisibility(View.GONE);
        frameLayoutFabDual.setVisibility(View.GONE);
    }

    public void toggleNavigationVisibility(boolean isVisible) {
        mNavigationLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void currentScreenSetup(String directory, int category, boolean isDualPane) {
        setDir(directory, isDualPane);
        setCurrentCategory(category);
        addToBackStack(directory, category);
    }


    /**
     * Called every time when a file item is clicked
     *
     * @param intent Contains path of the file whose children need to be shown
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * Triggered on clicked on any Navigation drawer item group/child
     *
     * @param groupPos Group Pos can be 0->Storage 1-> Favorites 2->Library 3->Other
     * @param childPos Child pos
     */
    private void displaySelectedGroup(int groupPos, int childPos) {
        switch (groupPos) {
            case 0:
            case 1:
            case 2:
                String path = totalGroup.get(groupPos).getmChildItems().get(childPos)
                        .getPath();
                displaySelectedGroup(groupPos, childPos, path);
                drawerLayout.closeDrawer(relativeLayoutDrawerPane);
                break;
            case 3:
                switch (childPos) {
                    case 0: // Rate us
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        // Try Google play
                        intent.setData(Uri
                                .parse("market://details?id=" + getPackageName()));
                        if (FileUtils.checkAppForIntent(this, intent)) {
                            // Market (Google play) app seems not installed,
                            // let's try to open a webbrowser
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" +
                                    getPackageName()));
                            if (FileUtils.checkAppForIntent(this, intent)) {
                                Toast.makeText(this,
                                        getString(R.string.msg_error_not_supported),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                startActivity(intent);
                            }
                        } else {
                            startActivity(intent);
                        }
                        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
                        break;
                    case 1: // Settings
                        startActivityForResult(new Intent(this, SettingsActivity.class),
                                PREFS_REQUEST);
                        expandableListView.setSelection(0);
                        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
                        break;
                }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

            int type =
                    ExpandableListView.getPackedPositionType(info.packedPosition);

            int group =
                    ExpandableListView.getPackedPositionGroup(info.packedPosition);

            // Only for Favorites
            if (group == 1) {
                // Only create a context menu for child items
                if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    menu.add(0, MENU_FAVOURITES, 0, getString(R.string.delete_fav));
                }
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
                        .getFirstLine();
                FavInfo favInfo = new FavInfo();
                favInfo.setFileName(name);
                favInfo.setFilePath(path);
                favouritesGroupChild.remove(childPos);
                sharedPreferenceWrapper.removeFavorite(this, favInfo);
                if (mIsHomeScreenEnabled && mHomeScreenFragment != null) {
                    mHomeScreenFragment.removeFavorites(1);
                }
                expandableListAdapter.notifyDataSetChanged();
                return true;


            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.log(TAG, "onConfigurationChanged" + newConfig.orientation);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && mIsDualPaneEnabled) {
                showDualPane();
            } else {
                mIsDualModeEnabled = false;
                isDualPaneInFocus = false;
                if (fragment instanceof HomeScreenFragment) {
                    ((HomeScreenFragment) fragment).setDualModeEnabled(false);
                } else {
                    toggleDualPaneVisibility(false);
                }
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Dual pane mode to be shown only for File Category
     *
     * @param isFilesCategory True if files category
     */
    public void toggleDualPaneVisibility(boolean isFilesCategory) {
        if (isFilesCategory) {
            if (mIsDualModeEnabled) {
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id
                        .frame_container_dual);
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
            mBackStackListDual.clear();
        }

    }

    public void createDualFragment() {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        String internalStoragePath = getInternalStorage().getAbsolutePath();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, internalStoragePath);

        args.putString(FileConstants.KEY_PATH_OTHER, mCurrentDir);
        args.putBoolean(FileConstants.KEY_FOCUS_DUAL, true);

        args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
        FileListDualFragment dualFragment = new FileListDualFragment();
        dualFragment.setArguments(args);
        ft.replace(R.id.frame_container_dual, dualFragment);
//        ft.commit();
        ft.commitAllowingStateLoss();
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        Logger.log(TAG, "Onbackpress--fragment=" + fragment + " " +
                "mHomePageRemoved=" + mIsHomePageRemoved + "home added=" + mIsHomePageAdded + " " +
                "backstack=" + backStackEntryCount);
//        Logger.log(TAG, "Onbackpress--fab exp=" + fabCreateMenu.isExpanded()+"fabDUAL exp="+fabCreateMenuDual
// .isExpanded());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fabCreateMenu.isExpanded()) {
            fabCreateMenu.collapse();
        } else if (fabCreateMenuDual.isExpanded()) {
            fabCreateMenuDual.collapse();
        } else if (mIsHomePageRemoved) {
//            super.onBackPressed();
            if (backStackEntryCount != 0) {
                finish();
                /*getSupportFragmentManager().popBackStack();
                super.onBackPressed();
 */
            }
        } else if (mIsHomePageAdded) {
            initialScreenSetup(true);
            setTitleForCategory(100); // Setting title to App name
            mCurrentDir = null;
            mCurrentDirDualPane = null;
            mStartingDir = null;
            mStartingDirDualPane = null;
            isDualPaneInFocus = false;
            mFrameDualPane.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
//            cleanUpFileScreen();
            mIsDualModeEnabled = false;
            mIsHomePageAdded = false;
        } else if (fragment instanceof FileListFragment) {
            backOperation(fragment);
        } else {
            // Remove HomeScreen Frag & Exit App
            Logger.log(TAG, "Onbackpress--ELSE=");
            mCurrentDir = null;
            mStartingDir = null;
            mStartingDirDualPane = null;
            mCurrentDirDualPane = null;
            finish();
//            super.onBackPressed();
        }
    }


    private void backOperation(Fragment fragment) {
        Fragment dualFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
        if (isDualPaneInFocus) {
            if (((FileListDualFragment) dualFragment).isZipMode()) {

                if (((FileListDualFragment) dualFragment).checkZipMode()) {
                    int newSize = mBackStackListDual.size() - 1;

                    mBackStackListDual.remove(newSize);
                    mCurrentDirDualPane = mBackStackListDual.get(newSize - 1).getFilePath();
                    mCategoryDual = mBackStackListDual.get(newSize - 1).getCategory();
                    ((FileListDualFragment) dualFragment).reloadList(true, mCurrentDirDualPane);
                    if (!mIsFromHomePage) {
                        setNavDirectory(mCurrentDirDualPane, false);
                    } else {
                        hideFab();
                    }
                }
            } else if (mStartingDirDualPane == null) {
                removeFragmentFromBackStack();
            } else if (checkIfBackStackExists()) {
                ((FileListDualFragment) dualFragment).setCategory(mCategoryDual);
                ((FileListDualFragment) dualFragment).reloadList(true, mCurrentDirDualPane);
                if (mCategoryDual == FileConstants.CATEGORY.FILES.getValue()) {
                    setNavDirectory(mCurrentDirDualPane, true);
                }

            } else {
                removeFragmentFromBackStack();
                if (!mIsHomeScreenEnabled) {
                    finish();
                }
            }
        } else {
            if (((FileListFragment) fragment).isZipMode()) {

                if (((FileListFragment) fragment).checkZipMode()) {
                    int newSize = mBackStackList.size() - 1;

                    mBackStackList.remove(newSize);
                    mCurrentDir = mBackStackList.get(newSize - 1).getFilePath();
                    mCategory = mBackStackList.get(newSize - 1).getCategory();
                    ((FileListFragment) fragment).reloadList(true, mCurrentDir);
                    if (!mIsFromHomePage) {
                        setNavDirectory(mCurrentDir, false);
                    } else {
                        hideFab();
                    }
                }
            } else if (mStartingDir == null) {
                removeFragmentFromBackStack();
            } else if (checkIfBackStackExists()) {
                if (!isDualPaneInFocus) {
                    ((FileListFragment) fragment).setCategory(mCategory);
                    ((FileListFragment) fragment).reloadList(true, mCurrentDir);
                } else {
                    if (mCategoryDual == FileConstants.CATEGORY.GENERIC_LIST.getValue()) {
                        super.onBackPressed();
                    } else {
                        ((FileListDualFragment) dualFragment).setCategory(mCategoryDual);
                        ((FileListDualFragment) dualFragment).reloadList(true, mCurrentDirDualPane);
                    }
                }
                setTitleForCategory(mCategory);
                if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                    showFab();
                    if (!isDualPaneInFocus)
                        setNavDirectory(mCurrentDir, isDualPaneInFocus);
                    else
                        setNavDirectory(mCurrentDirDualPane, isDualPaneInFocus);

                }

            } else {
                removeFragmentFromBackStack();
                if (!mIsHomeScreenEnabled) {
                    finish();
                }
            }
        }

    }


    private boolean checkIfBackStackExists() {
        int backStackSize;
        if (!isDualPaneInFocus) {
            backStackSize = mBackStackList.size();
            Logger.log(TAG, "checkIfBackStackExists --size=" + backStackSize);
        } else {
            backStackSize = mBackStackListDual.size();
            Logger.log(TAG, "checkIfBackStackExists --DUAL size=" + backStackSize);
        }

        if (backStackSize == 1) {
            if (!isDualPaneInFocus) {
                mCurrentDir = mBackStackList.get(0).getFilePath();
                mCategory = mBackStackList.get(0).getCategory();
                Logger.log(TAG, "checkIfBackStackExists--Path=" + mCurrentDir + "  Category=" + mCategory);
                mIsFromHomePage = false;
                mBackStackList.clear();
            } else {
                mCurrentDirDualPane = mBackStackListDual.get(0).getFilePath();
                mCategoryDual = mBackStackListDual.get(0).getCategory();
                Logger.log(TAG, "checkIfBackStackExists--DUAL Path=" + mCurrentDirDualPane + "  " +
                        "Category=" + mCategoryDual);
                mBackStackListDual.clear();
            }
            return false;
        } else if (backStackSize > 1) {
            int newSize = backStackSize - 1;
            if (!isDualPaneInFocus) {
                mBackStackList.remove(newSize);
                mCurrentDir = mBackStackList.get(newSize - 1).getFilePath();
                mCategory = mBackStackList.get(newSize - 1).getCategory();
                if (FileUtils.checkIfFileCategory(mCategory) && !mIsFromHomePage) {
                    initializeStartingDirectory();
                } else {
                    hideFab();
                }

                Logger.log(TAG, "checkIfBackStackExists--Path=" + mCurrentDir + "  Category=" + mCategory);
                Logger.log(TAG, "checkIfBackStackExists --New size=" + mBackStackList.size());
            } else {
                mBackStackListDual.remove(newSize);
                mCurrentDirDualPane = mBackStackListDual.get(newSize - 1).getFilePath();
                mCategoryDual = mBackStackListDual.get(newSize - 1).getCategory();
                if (FileUtils.checkIfFileCategory(mCategoryDual)) {
                    initializeStartingDirectory();
                } else {
                    hideFab();
                }
                Logger.log(TAG, "checkIfBackStackExists--DUAL Path=" + mCurrentDirDualPane + "  " +
                        "Category=" + mCategoryDual);
                Logger.log(TAG, "checkIfBackStackExists --DUAL New size=" + mBackStackListDual.size());
            }
            return true;
        }
//        Logger.log(TAG, "checkIfBackStackExists --Path=" + mCurrentDir + "  Category=" + mCategory);
        return false;
    }

    public void setCurrentCategory(int category) {
        if (!isDualPaneInFocus) mCategory = category;
        else mCategoryDual = category;
    }

    public void setIsFromHomePage(boolean isFromHomePage) {
        mIsFromHomePage = isFromHomePage;
    }

    /**
     * Called from {@link #onBackPressed()} . Does the following:
     * 1. If homescreen enabled, returns to home screen
     * 2. If homescreen disabled, exits the app
     */
    private void removeFragmentFromBackStack() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        Logger.log(TAG, "RemoveFragmentFromBackStack --backstack==" + backStackCount +
                "home_enabled=" + mIsHomeScreenEnabled + " frag=" + fragment);
        mBackStackList.clear();
        mBackStackListDual.clear();
        cleanUpFileScreen();
//        super.onBackPressed();
        Logger.log(TAG, "RemoveFragmentFromBackStack--frag=" + fragment);
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();

        ft.remove(fragment);
        if (mHomeScreenFragment != null) {
            ft.show(mHomeScreenFragment);
            toggleNavigationVisibility(false);
        }
        ft.commitAllowingStateLoss();

    }

    private void cleanUpFileScreen() {
        if (mIsHomeScreenEnabled) {
            setTitleForCategory(100); // Setting title to App name
            hideFab();
            mCurrentDir = null;
            mCurrentDirDualPane = null;
            mStartingDir = null;
            mStartingDirDualPane = null;
            isDualPaneInFocus = false;
            mFrameDualPane.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onDestroy() {
        Logger.log(TAG, "onDestroy");

        unregisterForContextMenu(expandableListView);
        unregisterReceiver(mLocaleListener);
        mSharedPreferences.edit().putInt(FileConstants.CURRENT_THEME, mCurrentTheme).apply();
        if (mIsRootMode) {
            try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }


    public void toggleFab(boolean isActionMode) {
        if (isActionMode) {
            fabCreateMenu.setVisibility(View.GONE);
        } else {
            fabCreateMenu.setVisibility(View.VISIBLE);
        }
    }

    public void setCurrentDir(String dir, boolean isDualPaneInFocus) {
        if (isDualPaneInFocus) {
            mCurrentDirDualPane = dir;
        } else {
            mCurrentDir = dir;
        }
        this.isDualPaneInFocus = isDualPaneInFocus;
    }

    public void setDir(String dir, boolean isDualPaneInFocus) {
        Logger.log(TAG, "setDir=Dir=" + dir + "dualPane=" + isDualPaneInFocus);
        if (isDualPaneInFocus) {
            mCurrentDirDualPane = dir;
            mStartingDirDualPane = dir;
        } else {
            mCurrentDir = dir;
            mStartingDir = dir;
        }
        this.isDualPaneInFocus = isDualPaneInFocus;
    }


    private boolean mIsHomeSettingToggled;
    private boolean mIsHomePageRemoved;

    /**
     * Using this method since user can go to Settings page and can ENABLE HOMESCREEN  or
     * ENABLE DUAL PANE.We can't add fragments without loss of state that time. Hence,we keep
     * a flag on value change and while coming back from Settings we add Fragments accordingly
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Logger.log(TAG, "onPostResume" + mIsHomeSettingToggled);
        if (mIsHomeSettingToggled) {

            currentScreenSetup(FileUtils.getInternalStorage().getAbsolutePath(), FileConstants
                    .CATEGORY.FILES.getValue(), false);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, FileUtils.getInternalStorage().getAbsolutePath());
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.replace(R.id.main_container, fileListFragment);
            ft.commit();
            mIsHomeSettingToggled = false;
            mIsHomePageRemoved = true;
        } else if (mShowDualPane) {
            showDualPane();
            mShowDualPane = false;
        }
    }

    private void restartApp(boolean isLocaleChanged) {

        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        overridePendingTransition(enter_anim, exit_anim);
        finish();
        overridePendingTransition(enter_anim, exit_anim);
        if (!isLocaleChanged)
            startActivity(getIntent());

    }


    /**
     * Called on return from Settings screen
     */
    private void onSharedPrefsChanged() {

        String value = mSharedPreferences.getString(FileConstants.PREFS_THEME, "");
        if (!value.isEmpty()) {
            int theme = Integer.valueOf(value);
            if (theme != mCurrentTheme) {
                mCurrentTheme = theme;
                restartApp(false);
            }
        }

        String language = mSharedPreferences.getString(LocaleHelper.SELECTED_LANGUAGE, Locale.getDefault()
                .getLanguage());
        if (!language.equals(mCurrentLanguage)) {
            mCurrentLanguage = language;
            LocaleHelper.setLocale(this, language);
            restartApp(false);
        }

        boolean showHidden = mSharedPreferences.getBoolean(FileConstants.PREFS_HIDDEN, false);

        if (showHidden != mShowHidden) {
            mShowHidden = showHidden;
            Logger.log(TAG, "OnPrefschanged PREFS_HIDDEN" + mShowHidden);
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

            FileListFragment dualPaneFragment = (FileListDualFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.frame_container_dual);
            if (fragment instanceof FileListFragment) {
                ((FileListFragment) fragment).refreshList();
            }
            if (dualPaneFragment != null) {
                dualPaneFragment.refreshList();
            }
        }

        boolean isHomeScreenEnabled = mSharedPreferences.getBoolean(FileConstants
                .PREFS_HOMESCREEN, true);

        if (isHomeScreenEnabled != mIsHomeScreenEnabled) {

            mIsHomeScreenEnabled = isHomeScreenEnabled;
            Logger.log(TAG, "OnPrefschanged PREFS_HOMESCREEN" + mIsHomeScreenEnabled);
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
            // If homescreen disabled
            if (!isHomeScreenEnabled) {

                // If user on Home page, replace it with FileListFragment
                if (fragment instanceof HomeScreenFragment) {
                    mIsHomeSettingToggled = true;
                } else {
                    Logger.log(TAG, "Nav directory count=" + navDirectory.getChildCount());

                    for (int i = 0; i < Math.min(navDirectory.getChildCount(), 2); i++) {
                        navDirectory.removeViewAt(0);
                    }

                    if (navDirectoryDualPane.getChildCount() > 0) {
                        for (int i = 0; i < Math.min(navDirectoryDualPane.getChildCount(), 2); i++) {
                            navDirectoryDualPane.removeViewAt(0);
                        }
                    }
                    // Set a flag so that it can be removed on backPress
                    mIsHomePageRemoved = true;
                    mIsHomePageAdded = false;
                }

            } else {
                // Clearing the flags necessary as user can click checkbox multiple times
                mIsHomeSettingToggled = false;
                mIsHomePageAdded = true;
                mIsHomePageRemoved = false;
            }
        }

        boolean isDualPaneEnabledSettings = mSharedPreferences.getBoolean(FileConstants
                .PREFS_DUAL_PANE, mIsTablet);
        if (isDualPaneEnabledSettings != mIsDualPaneEnabled) {
            mIsDualPaneEnabled = isDualPaneEnabledSettings;

            Logger.log(TAG, "OnPrefschanged PREFS_DUAL_PANE" + mIsDualPaneEnabled);

            if (!mIsDualPaneEnabled) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id
                        .main_container);
                toggleDualPaneVisibility(false);
                if (fragment instanceof HomeScreenFragment) {
                    ((HomeScreenFragment) fragment).setDualModeEnabled(false);
                } else {
                    ((FileListFragment) fragment).refreshSpan(); // For changing the no of columns in non-dual mode
                }

                mIsDualModeEnabled = false;
                mShowDualPane = false;
            } else {
                checkScreenOrientation();
                if (mCategory == FileConstants.CATEGORY.FILES.getValue() && mIsDualModeEnabled) {
                    mShowDualPane = true;
                }
            }
        }

    }

    private void resetFavouritesGroup() {

        for (int i = favouritesGroupChild.size() - 1; i >= 0; i--) {
            if (!favouritesGroupChild.get(i).getSecondLine().equalsIgnoreCase(FileUtils
                    .getDownloadsDirectory().getAbsolutePath())) {
                favouritesGroupChild.remove(i);
            }
        }
        sharedPreferenceWrapper.resetFavourites(this);
        expandableListAdapter.notifyDataSetChanged();

    }


}

