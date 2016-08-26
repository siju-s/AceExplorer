package com.siju.acexplorer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.kobakei.ratethisapp.RateThisApp;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.FileListDualFragment;
import com.siju.acexplorer.filesystem.FileListFragment;
import com.siju.acexplorer.filesystem.HomeScreenFragment;
import com.siju.acexplorer.filesystem.model.BackStackModel;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.ui.CustomScrimInsetsFrameLayout;
import com.siju.acexplorer.filesystem.utils.FileOperations;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.settings.SettingsActivity;
import com.siju.acexplorer.utils.DialogUtils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.siju.acexplorer.filesystem.utils.FileUtils.getInternalStorage;


public class BaseActivity extends AppCompatActivity implements
        View.OnClickListener {

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
    private CustomScrimInsetsFrameLayout relativeLayoutDrawerPane;
    private String mCurrentDir;
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
    private int mCategoryDual = FileConstants.CATEGORY.FILES.getValue();
    private CoordinatorLayout mMainLayout;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 200;
    private static final int PREFS_REQUEST = 1000;

    private boolean mIsPermissionGranted;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private int mViewMode = FileConstants.KEY_LISTVIEW;
    private boolean mIsFavGroup;
    private String mSelectedPath;
    private TextView textPathSelect;
    private final int MENU_FAVOURITES = 1;
    private boolean mIsFirstRun;
    public static final String PREFS_FIRST_RUN = "first_app_run";
    private boolean mIsDualPaneEnabledSettings = true;
    private boolean mShowDualPane;
    private boolean mIsPasteItemVisible;
    private boolean mIsHomeScreenEnabled;
    private boolean mShowHidden;
    private boolean mIsHomePageAdded;
    private FrameLayout mFrameHomeScreen;
    private FrameLayout mFrameDualPane;
    private ActionBarDrawerToggle toggle;
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

    private String mStartingDir = null;//getInternalStorage().getAbsolutePath();
    private String mStartingDirDualPane = null;//getInternalStorage().getAbsolutePath();
    private FileListFragment mFileListFragment;
    private FileListDualFragment mFileListDualFragment;
    private boolean mIsDualModeEnabled;
    private boolean mIsFromHomePage;
    private int mCurrentTheme = FileConstants.THEME_LIGHT;
    private int mNavButtonDimensions;
    private int mButtonMinWidth;
    private ArrayList<String> mExternalSDPaths = new ArrayList<>();
    private ArrayList<BackStackModel> mBackStackList = new ArrayList<>();
    private ArrayList<BackStackModel> mBackStackListDual = new ArrayList<>();
    private int mCurrentOrientation;
    private boolean mIsRootMode;
    private int mOperation = -1;
    private View mFabView;
    private String mCreatePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initConstants();
        initViews();
//        checkScreenOrientation();

        Logger.log(TAG, "onCreate");
        // If MarshMallow ask for permission
        if (useRunTimePermissions()) {
            checkPermissions();
        } else {
            mIsPermissionGranted = true;
            setup();
            setUpInitialData();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }


    private void setup() {
        checkPreferences();
        getSavedFavourites();
        initListeners();
        if (mCurrentTheme != FileConstants.THEME_LIGHT)
            setApplicationTheme(false);
    }

 /*   @Override
    protected void onResume() {

        LocaleHelper.onCreate(this);
        super.onResume();
    }*/

    private void checkPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPreferences.getBoolean(PREFS_FIRST_RUN, true)) {
            // If app is first run
            mIsFirstRun = true;
            mSharedPreferences.edit().putInt(FileConstants.KEY_SORT_MODE, FileConstants
                    .KEY_SORT_NAME).apply();
        }
        mIsHomeScreenEnabled = mSharedPreferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        mShowHidden = mSharedPreferences.getBoolean(FileConstants.PREFS_HIDDEN, false);
        mIsDualPaneEnabledSettings = mSharedPreferences.getBoolean(FileConstants.PREFS_DUAL_PANE,
                true);
        mCurrentTheme = mSharedPreferences.getInt(FileConstants.CURRENT_THEME, 0);
        mIsRootMode = mSharedPreferences.getBoolean(FileConstants.ROOT_ACCESS, true);
    }


    private void setUpInitialData() {
        prepareListData();
        setListAdapter();
        setUpPreferences();
        checkScreenOrientation();
        initialScreenSetup(mIsHomeScreenEnabled);
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
            fabCreateMenu.setVisibility(View.GONE);
            requestPermission();
        } else {
            mIsPermissionGranted = true;
            fabCreateMenu.setVisibility(View.VISIBLE);
            setup();
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
                    Log.d(TAG, "Permission granted");
                    mIsPermissionGranted = true;
                    fabCreateMenu.setVisibility(View.VISIBLE);
                    setup();
                    setUpInitialData();
                } else {
                    showRationale();
                    fabCreateMenu.setVisibility(View.GONE);
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Called when user returns from the settings screen
        if (requestCode == PREFS_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (intent.getBooleanExtra(FileConstants.PREFS_RESET, false)) {
                    resetFavouritesGroup();
                    expandableListView.smoothScrollToPosition(0);
                }
                onSharedPrefsChanged();

            }
        } else if (requestCode == 3) {
            String p = mSharedPreferences.getString("URI", null);
            Uri oldUri = null;
//            if (p != null) oldUri = Uri.parse(p);
            Uri treeUri = null;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = intent.getData();
                // Persist URI - this is required for verification of writability.
                if (treeUri != null)
                    mSharedPreferences.edit().putString("URI", treeUri.toString()).apply();
            }
            // If not confirmed SAF, or if still not writable, then revert settings.
            else {
               /* DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false,
                        currentFolder);||!FileUtil.isWritableNormalOrSaf(currentFolder)
*/
                if (treeUri != null)
                    mSharedPreferences.edit().putString("URI", oldUri.toString()).apply();
                return;
            }

            FileListFragment fragment = (FileListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main_container);

            // After confirmation, update stored value of folder.
            // Persist access permissions.
            final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            switch (mOperation) {
                /*case DataUtils.DELETE://deletion
                    new DeleteTask(null, mainActivity).execute((oparrayList));
                    break;
                case DataUtils.COPY://copying
                    Intent intent1 = new Intent(con, CopyService.class);
                    intent1.putExtra("FILE_PATHS", (oparrayList));
                    intent1.putExtra("COPY_DIRECTORY", oppathe);
                    startService(intent1);
                    break;
                case DataUtils.MOVE://moving
                    new MoveFiles((oparrayList), ((Main) getFragment().getTab()), ((Main) getFragment().getTab()).getActivity(), 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
                    break;*/
                case FileConstants.FOLDER_CREATE://mkdir
                    mkDir(new File(mCreatePath));
                    break;
                case FileConstants.RENAME:
                    fragment.renameCallBack();
                    break;
                case FileConstants.FILE_CREATE:
                    mkFile(new File(mCreatePath));
                    break;
                case FileConstants.EXTRACT:
                    fragment.extractCallBack();
                    break;

                case FileConstants.COMPRESS:
                    fragment.compressCallBack();
//                    fr.compressFiles(new File(oppathe), oparrayList);
            }
            mOperation = -1;
            mFabView = null;
            mCreatePath = null;
        }
        super.onActivityResult(requestCode, resultCode, intent);


    }


    private void getSavedFavourites() {
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        savedFavourites = sharedPreferenceWrapper.getFavorites(this);
    }

    public void updateFavourites(String name, String path) {
        SectionItems favItem = new SectionItems(name, path, R.drawable.ic_fav_folder, path);
        if (!favouritesGroupChild.contains(favItem)) {
            favouritesGroupChild.add(favItem);
            expandableListAdapter.notifyDataSetChanged();
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
        mNavButtonDimensions = (int) getResources().getDimension(R.dimen.nav_button_width);
        mButtonMinWidth = (int) getResources().getDimension(R.dimen.nav_button_min_width);
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
        mFrameHomeScreen = (FrameLayout) findViewById(R.id.main_container);
        mFrameDualPane = (FrameLayout) findViewById(R.id.frame_container_dual);
//        mCollapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Set the padding to match the Status Bar height
//        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        mNavigationLayout = (LinearLayout) findViewById(R.id.layoutNavigate);

//        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
//        mToolbar.setNavigationIcon(R.drawable.ic_menu_white);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        mCollapsingToolbarLayout.setTitle(getString(R.string.app_name));

        mToolbar.setTitle(R.string.app_name);
        mMainLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        mBottomToolbar = (Toolbar) findViewById(R.id.toolbar_bottom);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        relativeLayoutDrawerPane = (CustomScrimInsetsFrameLayout) findViewById(R.id.drawerPane);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        // get the listview
        expandableListView = (ExpandableListView) findViewById(R.id.expand_list_drawer);
        View list_header = getLayoutInflater().inflate(R.layout.drawerlist_header, null);
        expandableListView.addHeaderView(list_header);
    /*    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDrawer(true);
            }
        });*/
        fabCreateMenu = (FloatingActionsMenu) findViewById(R.id.fabCreate);
        fabCreateFolder = (FloatingActionButton) findViewById(R.id.fabCreateFolder);
        fabCreateFile = (FloatingActionButton) findViewById(R.id.fabCreateFile);
        frameLayoutFab = (FrameLayout) findViewById(R.id.frameLayoutFab);
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


        fabCreateMenuDual = (FloatingActionsMenu) findViewById(R.id.fabCreateDual);
        fabCreateFolderDual = (FloatingActionButton) findViewById(R.id.fabCreateFolderDual);
        fabCreateFileDual = (FloatingActionButton) findViewById(R.id.fabCreateFileDual);


        frameLayoutFabDual = (FrameLayout) findViewById(R.id.frameLayoutFabDual);
        frameLayoutFabDual.getBackground().setAlpha(0);


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


        navDirectory = (LinearLayout) findViewById(R.id.navButtons);
        navDirectoryDualPane = (LinearLayout) findViewById(R.id.navButtonsDualPane);
        scrollNavigation = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
        scrollNavigationDualPane = (HorizontalScrollView) findViewById(R.id
                .scrollNavigationDualPane);
        mViewSeperator = findViewById(R.id.viewSeperator);


    }


    public void setNavDirectory(String path, boolean isDualPane) {
        String[] parts;
        parts = path.split("/");
        isDualPaneInFocus = isDualPane;

        if (!isDualPaneInFocus) {
            navDirectory.removeAllViews();
            mCurrentDir = path;
        } else {
            navDirectoryDualPane.removeAllViews();
            mCurrentDirDualPane = path;

        }

        FileListFragment fileListFragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id
                .main_container);
        mFileListFragment = fileListFragment;

        FileListDualFragment fileListDualFragment = (FileListDualFragment) getSupportFragmentManager()
                .findFragmentById(R.id
                        .frame_container_dual);
        mFileListDualFragment = fileListDualFragment;


        String dir = "";


        if (mIsHomeScreenEnabled) {
            ImageButton imageButton = new ImageButton(this);
            imageButton.setImageResource(R.drawable.ic_home_white_nav);
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
            params.gravity = Gravity.CENTER_VERTICAL;
            navArrow.setImageResource(R.drawable.ic_arrow_nav);
            navArrow.setLayoutParams(params);
            if (!isDualPaneInFocus) {
                navDirectory.addView(navArrow);
            } else {
                navDirectoryDualPane.addView(navArrow);
            }
        }
        // If root dir , parts will be 0
        if (parts.length == 0) {
            isCurrentDirRoot = true;
            mStartingDir = "/";
            setNavDir("/", "/"); // Add Root button
        } else {
            int count = 0;
            for (int i = 1; i < parts.length; i++) {
                dir += "/" + parts[i];
                Logger.log(TAG, "setNavDirectory--dir=" + dir + "  Starting dir=" + mStartingDir);

//                if (!isCurrentDirRoot) {
                if (!isDualPaneInFocus) {
                    if (!dir.contains(mStartingDir)) {
                        continue;
                    }
                } else {
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
                        setNavDir("/", "/");
                    }
                } else {
                    if (isCurrentDualDirRoot && count == 0) {
                        setNavDir("/", "/");
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
            createNavButton(STORAGE_INTERNAL, dir, mFileListFragment, mFileListDualFragment);
        } else if (dir.equals("/")) {
            createNavButton(STORAGE_ROOT, dir, mFileListFragment, mFileListDualFragment);
        } else if (mExternalSDPaths != null && mExternalSDPaths.contains(dir)) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_EXTERNAL, dir, mFileListFragment, mFileListDualFragment);
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
            createNavButton(parts, dir, mFileListFragment, mFileListDualFragment);
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

    private void createNavButton(String text, final String dir, final FileListFragment fileListFragment,
                                 final FileListDualFragment fileListDualFragment) {

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
//            imageButton.setBackgroundResource(0);
            imageButton.setBackgroundColor(Color.parseColor("#00ffffff"));
            imageButton.setLayoutParams(params);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navButtonOnClick(view, dir, fileListFragment, fileListDualFragment);
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
            textView.setLayoutParams(params);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navButtonOnClick(view, dir, fileListFragment, fileListDualFragment);
                }
            });
            if (!isDualPaneInFocus) {
                navDirectory.addView(textView);
            } else {
                navDirectoryDualPane.addView(textView);
            }
        }


    }

    private void navButtonOnClick(View view, final String dir, final FileListFragment fileListFragment, final
    FileListDualFragment fileListDualFragment) {
        Log.d(TAG, "Dir=" + dir);

        boolean isDualPaneButtonClicked;
        LinearLayout parent = (LinearLayout) view.getParent();
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
                    fileListFragment.clearZipMode();
                    fileListFragment.setCategory(FileConstants.CATEGORY.FILES.getValue());
                }*/
                fileListFragment.reloadList(false, mCurrentDir);
                setNavDirectory(mCurrentDir, isDualPaneButtonClicked);
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
                setNavDirectory(mCurrentDirDualPane, isDualPaneButtonClicked);
            }
        }

    }

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE
                && mIsDualPaneEnabledSettings) {

            mIsDualModeEnabled = true;
//            showDualPane();
//                 showDualPane();
       /*     mViewSeperator.setVisibility(View.VISIBLE);
            frameLayoutFabDual.setVisibility(View.VISIBLE);*/
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
                    createDirDialog();
                } else {
                    mOperation = FileConstants.FILE_CREATE;
                    createFileDialog();
                }
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


    public void createDirDialog() {

        String title = getString(R.string.new_folder);
        String texts[] = new String[]{getString(R.string.enter_name), "", title, getString(R.string.create), "",
                getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showEditDialog(this, texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (!FileUtils.validateFileName(fileName)) {
                    materialDialog.getInputEditText().setError(getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }
                fileName = fileName.trim();
                if (isDualPaneInFocus) {
                    fileName = mCurrentDirDualPane + "/" + fileName;
                } else {
                    fileName = mCurrentDir + "/" + fileName;
                }
                mkDir(new File(fileName));
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    public void mkDir(final File file) {
        /*final Toast toast=Toast.makeText(ma.getActivity(), R.string.creatingfolder, Toast.LENGTH_LONG);
        toast.show();*/
        FileOperations.mkdir(file, this, mIsRootMode, new FileOperations.ErrorCallBack() {
            @Override
            public void exists(final File file1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (toast != null) toast.cancel();
                        showMessage(getString(R.string.file_exists));
//                        if (ma != null && ma.getActivity() != null)
                        createDirDialog();

                    }
                });
            }

            @Override
            public void launchSAF(final File file) {
//                if (toast != null) toast.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                /*        mainActivity.oppathe = path.getPath();
                        mainActivity.operation = DataUtils.NEW_FOLDER;*/
//                        mFabView = view;
                        mCreatePath = file.getAbsolutePath();
                        guideDialogForLEXA(mCreatePath);
                    }
                });

            }

            @Override
            public void launchSAF(final File oldFile, final File newFile) {
//                if (toast != null) toast.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                /*        mainActivity.oppathe = path.getPath();
                        mainActivity.operation = DataUtils.NEW_FOLDER;*/
//                        mFabView = view;
                        mCreatePath = file.getAbsolutePath();
                        guideDialogForLEXA(mCreatePath);
                    }
                });

            }


            @Override
            public void done(File hFile, final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        if (toast != null) toast.cancel();
                        if (success) {
                            if (isDualPaneInFocus) {
                                Fragment dualFragment = getSupportFragmentManager().findFragmentById
                                        (R.id
                                                .frame_container_dual);

                                if (dualFragment != null) {
                                    ((FileListDualFragment) dualFragment).refreshList();
                                }
                            } else {

                                Fragment fragment = getSupportFragmentManager().findFragmentById
                                        (R.id.main_container);

                                if (fragment != null) {
                                    ((FileListFragment) fragment).refreshList();
                                }

                            }

                        } else
                            Toast.makeText(BaseActivity.this, R.string.msg_operation_failed,
                                    Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    public void createFileDialog() {

        String title = getString(R.string.new_file);
        String texts[] = new String[]{getString(R.string.enter_name), "", title, getString(R.string.create), "",
                getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showEditDialog(this, texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (!FileUtils.validateFileName(fileName)) {
                    materialDialog.getInputEditText().setError(getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }

                fileName = fileName.trim();
                fileName = fileName + ".txt";
                if (isDualPaneInFocus) {
                    fileName = mCurrentDirDualPane + "/" + fileName;
                } else {
                    fileName = mCurrentDir + "/" + fileName;
                }
                mkFile(new File(fileName));
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }


    public void mkFile(final File file) {
        /*final Toast toast=Toast.makeText(ma.getActivity(), R.string.creatingfolder, Toast.LENGTH_LONG);
        toast.show();*/
        FileOperations.mkfile(file, this, mIsRootMode, new FileOperations.ErrorCallBack() {
            @Override
            public void exists(final File file1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (toast != null) toast.cancel();
                        showMessage(getString(R.string.file_exists));
//                        if (ma != null && ma.getActivity() != null)
                        createFileDialog();

                    }
                });
            }

            @Override
            public void launchSAF(final File file) {
//                if (toast != null) toast.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                /*        mainActivity.oppathe = path.getPath();
                        mainActivity.operation = DataUtils.NEW_FOLDER;*/
//                        mFabView = view;
                        mCreatePath = file.getAbsolutePath();
                        guideDialogForLEXA(mCreatePath);
                    }
                });

            }

            @Override
            public void launchSAF(File oldFile, File newFile) {

            }


            @Override
            public void done(final File file, final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        if (toast != null) toast.cancel();
                        if (success) {
                            if (isDualPaneInFocus) {
                                Fragment dualFragment = getSupportFragmentManager().findFragmentById
                                        (R.id
                                                .frame_container_dual);

                                if (dualFragment != null) {
                                    ((FileListDualFragment) dualFragment).refreshList();
                                }
                            } else {

                                Fragment fragment = getSupportFragmentManager().findFragmentById
                                        (R.id.main_container);

                                if (fragment != null) {
                                    ((FileListFragment) fragment).refreshList();
                                }

                            }
                            FileUtils.scanFile(BaseActivity.this, file.getAbsolutePath());

                        } else
                            Toast.makeText(BaseActivity.this, R.string.msg_operation_failed,
                                    Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    public void guideDialogForLEXA(String path) {

        String title = getString(R.string.needsaccess);
        String texts[] = new String[]{title, getString(R.string.open), "", getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showCustomDialog(this, R.layout.dialog_saf, texts);
        View view = materialDialog.getCustomView();
        TextView textView = (TextView) view.findViewById(R.id.description);
        ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.sd_operate_step);
        textView.setText(getString(R.string.needs_access_summary, path));

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerStorageAccessFramework();
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(BaseActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 3);
    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String setDialogTitle(int id) {
        String title = "";
        switch (id) {
            case R.id.fabCreateFile:
                title = getString(R.string.new_file);
                break;
            case R.id.fabCreateFolder:
                title = getString(R.string.new_folder);
                break;
            case R.id.action_rename:
                title = getString(R.string.action_rename);
        }
        return title;

    }

    /**
     * Checks if same item clicked from navigation drawer
     *
     * @param path
     * @param category
     * @return
     */
    private boolean checkIfSameItemClicked(String path, int category) {
        if (!isDualPaneInFocus)
            return mCurrentDir != null && mCurrentDir.equals(path) && mCategory == category;
        else
            return mCurrentDirDualPane != null && mCurrentDirDualPane.equals(path) && mCategoryDual
                    == category;

    }

    public void displaySelectedGroup(int groupPos, int childPos, String path) {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
//        mIsFromHomePage = fragment instanceof HomeScreenFragment;

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
                    toggleNavBarFab(false);
                    toggleDualPaneVisibility(true);
//                    fabCreateMenu.setVisibility(View.VISIBLE);
                }

                mIsFavGroup = groupPos == 1;

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

                toggleNavBarFab(true);
//                mNavigationLayout.setVisibility(View.GONE);
                toggleDualPaneVisibility(false);
//                mCurrentDir = null;
//                fabCreateMenu.setVisibility(View.GONE);
                int category;

                switch (childPos) {
                    // When Audio item is clicked
                    case 0:
//                        mToolbar.setTitle(MUSIC);
                        category = FileConstants.CATEGORY.AUDIO.getValue();
                        openCategoryItem(path, category);
                        break;
                    // When Video item is clicked
                    case 1:
//                        mToolbar.setTitle(VIDEO);
                        category = FileConstants.CATEGORY.VIDEO.getValue();
                        openCategoryItem(path, category);
                        break;
                    // When Images item is clicked
                    case 2:
//                        mToolbar.setTitle(IMAGES);
                        category = FileConstants.CATEGORY.IMAGE.getValue();
                        openCategoryItem(path, category);
                        break;
                    // When Documents item is clicked
                    case 3:
//                        mToolbar.setTitle(DOCS);
                        category = FileConstants.CATEGORY.DOCS.getValue();
                        openCategoryItem(path, category);
                        break;
                }
                setTitleForCategory(mCategory);
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
//            Logger.log(TAG, "Back stack--contents=" + mBackStackList);

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
//                mCurrentDir = "/";
                    mStartingDir = "/";
                }
            } else {
                if (!mCurrentDirDualPane.contains(getInternalStorage().getAbsolutePath()) &&
                        (mExternalSDPaths != null && !mExternalSDPaths.contains(mCurrentDirDualPane))) {
                    isCurrentDualDirRoot = true;
//                mCurrentDir = "/";
                    mStartingDirDualPane = "/";
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
                mStartingDir = "/";
            } else {
                mStartingDir = "/";
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
                mStartingDirDualPane = "/";
            } else {
                mStartingDirDualPane = "/";
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
        args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof HomeScreenFragment) {
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.replace(R.id.main_container, fileListFragment, directory);
            ft.addToBackStack(null);
            ft.commit();
//            ft.commitAllowingStateLoss();
            toggleNavBarFab(false);
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
            ((FileListFragment) fragment).setCategory(category);
            ((FileListFragment) fragment).reloadList(isDualPaneInFocus, directory);
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
                    dualFragment.reloadList(isDualPaneInFocus, directory);
                }
            }
        }
        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }

    private void initListeners() {
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int
                    childPosition, long
                                                id) {
                Log.d(TAG, "Group pos-->" + groupPosition + "CHILD POS-->" + childPosition);
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
            toggleNavBarFab(true);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, true);
            args.putBoolean(BaseActivity.PREFS_FIRST_RUN, mIsFirstRun);
            args.putBoolean(FileConstants.PREFS_DUAL_ENABLED, mIsDualModeEnabled);
            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
            homeScreenFragment.setArguments(args);
//            ft.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
            ft.replace(R.id.main_container, homeScreenFragment);
            ft.commitAllowingStateLoss();
        } else {

            toggleNavBarFab(false);
            // Initialising only if Home screen disabled
            currentScreenSetup(getInternalStorage().getAbsolutePath(), FileConstants.CATEGORY.FILES.getValue(), false);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, mCurrentDir);
            args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            args.putInt(BaseActivity.ACTION_GROUP_POS, 0); // Storage Group
            args.putInt(BaseActivity.ACTION_CHILD_POS, 1); // Internal Storage child
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

    public void toggleNavBarFab(boolean isHomeScreenEnabled) {
        if (isHomeScreenEnabled) {
            mNavigationLayout.setVisibility(View.GONE);
            frameLayoutFab.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
        } else {
            mNavigationLayout.setVisibility(View.VISIBLE);
            frameLayoutFab.setVisibility(View.VISIBLE);
        }
    }

    private void currentScreenSetup(String directory, int category, boolean isDualPane) {
        setDir(directory, isDualPane);
        setCurrentCategory(category);
        addToBackStack(directory, category);
    }


    private void initializeStorageGroup() {
        List<String> storagePaths = FileUtils.getStorageDirectories(this, true);
        ArrayList<SectionItems> storageGroupChild = new ArrayList<>();
        File systemDir = FileUtils.getRootDirectory();
        File rootDir = systemDir.getParentFile();
      /*  File internalSD = getI
      pnternalStorage();
        File extSD = FileUtils.getExternalStorage();*/
        storageGroupChild.add(new SectionItems(STORAGE_ROOT, storageSpace(systemDir), R.drawable
                .ic_root_white,
                FileUtils
                        .getAbsolutePath(rootDir)));

        for (String path : storagePaths) {
            File file = new File(path);
            int icon;
            String name, storageSpace;
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
//                storage_count++;
                storageSpace = storageSpace(file);
                storageGroupChild.add(new SectionItems(name, storageSpace, icon, path));
            }

        }
 /*       storageGroupChild.add(new SectionItems(STORAGE_INTERNAL, storageSpace(internalSD), R
                .drawable
                .ic_phone_white, FileUtils.getAbsolutePath(internalSD)));
        if (extSD != null) {
            storageGroupChild.add(new SectionItems(STORAGE_EXTERNAL, storageSpace(extSD), R
                    .drawable.ic_ext_white,
                    FileUtils.getAbsolutePath(extSD)));
        }*/
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
/*        if (intent != null && !intent.getAction().equals(ACTION_MAIN)) {
            Log.d(TAG, "On onNewIntent");
            StoragesFragment storagesFragment = (StoragesFragment)
                    getSupportFragmentManager()
                            .findFragmentById(R
                                    .id.main_container);
            if (storagesFragment != null) {
                storagesFragment.createFragmentForIntent(intent);
            }
        }*/

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
                        if (checkAppForIntent(intent)) {
                            // Market (Google play) app seems not installed,
                            // let's try to open a webbrowser
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" +
                                    getPackageName()));
                            if (checkAppForIntent(intent)) {
                                // Well if this also fails, we have run out of
                                // options, inform the user.
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

    private boolean checkAppForIntent(Intent intent) {
        return getPackageManager().resolveActivity(intent, 0) == null;
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

        // Only for Favorites
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged" + newConfig.orientation);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && mIsDualPaneEnabledSettings) {
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
     * @param isFilesCategory
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
//        Logger.log(TAG, "Onbackpress--fab exp=" + fabCreateMenu.isExpanded()+"fabDUAL exp="+fabCreateMenuDual.isExpanded());

        mIsFavGroup = false;

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
            super.onBackPressed();
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
                    ((FileListDualFragment) dualFragment).reloadList(false, mCurrentDirDualPane);
                    if (!mIsFromHomePage) {
                        setNavDirectory(mCurrentDirDualPane, false);
                    }
                    else {
                        toggleNavBarFab(true);
                    }
                }
            } else if (mStartingDirDualPane == null) {
                removeFragmentFromBackStack();
            } else if (checkIfBackStackExists()) {
                ((FileListDualFragment) dualFragment).setCategory(mCategoryDual);
                ((FileListDualFragment) dualFragment).reloadList(false, mCurrentDirDualPane);
                if (mCategoryDual == FileConstants.CATEGORY.FILES.getValue()) {
                    setNavDirectory(mCurrentDirDualPane, false);
                }

            } else {
                removeFragmentFromBackStack();
            }
        } else {
            if (((FileListFragment) fragment).isZipMode()) {
                if (((FileListFragment) fragment).checkZipMode()) {
                    int newSize = mBackStackList.size() - 1;

                    mBackStackList.remove(newSize);
                    mCurrentDir = mBackStackList.get(newSize - 1).getFilePath();
                    mCategory = mBackStackList.get(newSize - 1).getCategory();
                    ((FileListFragment) fragment).reloadList(false, mCurrentDir);
                    if (!mIsFromHomePage) {
                        setNavDirectory(mCurrentDir, false);
                    }
                    else {
                        toggleNavBarFab(true);
                    }
                }
            } else if (mStartingDir == null) {
                removeFragmentFromBackStack();
            } else if (checkIfBackStackExists()) {
                if (!isDualPaneInFocus) {
                    ((FileListFragment) fragment).setCategory(mCategory);
                    ((FileListFragment) fragment).reloadList(false, mCurrentDir);
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
                    toggleNavBarFab(false);
                    if (!isDualPaneInFocus)
                        setNavDirectory(mCurrentDir, isDualPaneInFocus);
                    else
                        setNavDirectory(mCurrentDirDualPane, isDualPaneInFocus);

                }

            } else {
                removeFragmentFromBackStack();
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
                    toggleNavBarFab(true);
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
                    toggleNavBarFab(true);
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


    private void setToolBarTheme(int toolBarColor, int statusBarColor) {
        this.mToolbar.setBackgroundColor(toolBarColor);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(statusBarColor);
        }
    }

    private void setApplicationTheme(boolean themeLight) {
        if (themeLight) {
            setToolBarTheme(ContextCompat.getColor(this, R.color.colorPrimary),
                    ContextCompat.getColor(this, R.color.color_light_status_bar));
            mMainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_bg));

        } else {
            setToolBarTheme(ContextCompat.getColor(this, R.color.color_dark_bg),
                    ContextCompat.getColor(this, R.color.color_dark_status_bar));
            mMainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_bg));

        }

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
        Logger.log(TAG, "RemoveFragmentFromBackStack --backstack==" + backStackCount +
                "home_enabled=" + mIsHomeScreenEnabled);
        mBackStackList.clear();
        mBackStackListDual.clear();
        cleanUpFileScreen();
        super.onBackPressed();
    }

    private void cleanUpFileScreen() {
        if (mIsHomeScreenEnabled) {
            setTitleForCategory(100); // Setting title to App name
            toggleNavBarFab(true);
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
        unregisterForContextMenu(expandableListView);
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


    public void updateAdapter(SectionItems favItem) {
        if (!favouritesGroupChild.contains(favItem)) {
            favouritesGroupChild.add(favItem);
            expandableListAdapter.notifyDataSetChanged();
        }

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
        Log.d(TAG, "setDir=Dir=" + dir + "dualPane=" + isDualPaneInFocus);
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
        Log.d(TAG, "onPostResume" + mIsHomeSettingToggled);
        if (mIsHomeSettingToggled) {

            currentScreenSetup(FileUtils.getInternalStorage().getAbsolutePath(), FileConstants
                    .CATEGORY.FILES.getValue(), false);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, FileUtils.getInternalStorage().getAbsolutePath());
            args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            args.putInt(BaseActivity.ACTION_GROUP_POS, 0);
            args.putInt(BaseActivity.ACTION_CHILD_POS, 1);
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.replace(R.id.main_container, fileListFragment);
            ft.commit();
            mIsHomeSettingToggled = false;
        } else if (mShowDualPane) {
            showDualPane();
            mShowDualPane = false;
        }
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
                boolean isLightTheme = theme == 0;
                setApplicationTheme(isLightTheme);
            }
        }

        boolean showHidden = mSharedPreferences.getBoolean(FileConstants.PREFS_HIDDEN, false);

        if (showHidden != mShowHidden) {
            mShowHidden = showHidden;
            Log.d(TAG, "OnPrefschanged PREFS_HIDDEN" + mShowHidden);
            FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                    .findFragmentById(R
                            .id.main_container);
            FileListFragment dualPaneFragment = (FileListDualFragment) getSupportFragmentManager()
                    .findFragmentById(R
                            .id.frame_container_dual);
            if (singlePaneFragment != null) {
                singlePaneFragment.refreshList();
            }
            if (dualPaneFragment != null) {
                dualPaneFragment.refreshList();
            }

        }

        boolean isHomeScreenEnabled = mSharedPreferences.getBoolean(FileConstants
                .PREFS_HOMESCREEN, true);

        if (isHomeScreenEnabled != mIsHomeScreenEnabled) {
            mIsHomeScreenEnabled = isHomeScreenEnabled;
            Log.d(TAG, "OnPrefschanged PREFS_HOMESCREEN" + mIsHomeScreenEnabled);
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
            // If homescreen disabled
            if (!isHomeScreenEnabled) {

                // If user on Home page, replace it with FileListFragment
                if (fragment instanceof HomeScreenFragment) {
                    mIsHomeSettingToggled = true;
                } else {
                    Logger.log(TAG, "Nav directory count=" + navDirectory.getChildCount());
                    /* This duplicate statement is intentional as after removing 0th element i.e Home,
                       the arrow becomes the 0th element. So,we need to remove that also
                    */
                    navDirectory.removeViewAt(0);
                    navDirectory.removeViewAt(0);

                    if (navDirectoryDualPane != null && navDirectoryDualPane.getChildCount() > 2) {
                        navDirectoryDualPane.removeViewAt(0);
                        navDirectoryDualPane.removeViewAt(0);
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
                .PREFS_DUAL_PANE, true);
        if (isDualPaneEnabledSettings != mIsDualPaneEnabledSettings) {
            mIsDualPaneEnabledSettings = isDualPaneEnabledSettings;

            Log.d(TAG, "OnPrefschanged PREFS_DUAL_PANE" + mIsDualPaneEnabledSettings);

            if (!mIsDualPaneEnabledSettings) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id
                        .main_container);
                toggleDualPaneVisibility(false);
                if (fragment instanceof HomeScreenFragment) {
                    ((HomeScreenFragment) fragment).setDualModeEnabled(false);
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
            if (!favouritesGroupChild.get(i).getmSecondLine().equalsIgnoreCase(FileUtils
                    .getDownloadsDirectory().getAbsolutePath())) {
                favouritesGroupChild.remove(i);
            }
        }
        sharedPreferenceWrapper.resetFavourites(this);
        expandableListAdapter.notifyDataSetChanged();

    }
}

