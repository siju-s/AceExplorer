package com.siju.filemanager;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.common.SharedPreferenceWrapper;
import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileListDualFragment;
import com.siju.filemanager.filesystem.FileListFragment;
import com.siju.filemanager.filesystem.HomeScreenFragment;
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
    private RelativeLayout relativeLayoutDrawerPane;
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
    private CoordinatorLayout mMainLayout;
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
    private String mStartingDir = getInternalStorage().getAbsolutePath();
    private String mStartingDirDualPane = getInternalStorage().getAbsolutePath();
    private FileListFragment mFileListFragment;
    private FileListDualFragment mFileListDualFragment;
    private boolean mIsDualModeEnabled;
    private int mPrevCategory;
    private boolean mIsFromHomePage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_content);

        initConstants();
        initViews();
//        checkScreenOrientation();

        Logger.log("TAG", "on create--Activity");
        // If MarshMallow ask for permission
        if (useRunTimePermissions()) {
            checkPermissions();
        } else {
            mIsPermissionGranted = true;
            setup();
            setUpInitialData();
        }
    }

    private void setup() {
        checkPreferences();
        getSavedFavourites();
        initListeners();
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
        checkScreenOrientation();
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
                    Log.d("TAG", "Permission granted");
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
//        mFrameHomeScreen = (FrameLayout) findViewById(R.id.frame_container);
        mFrameHomeScreen = (FrameLayout) findViewById(R.id.main_container);
/*        mFrameHomeScreen.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFrameHomeScreen.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = mFrameHomeScreen.getWidth(); //height is ready
                Logger.log(TAG,"Width inside observer="+width);
            }
        });*/
        mFrameDualPane = (FrameLayout) findViewById(R.id.frame_container_dual);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mNavigationLayout = (LinearLayout) findViewById(R.id.layoutNavigate);

//        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
//        mToolbar.setNavigationIcon(R.drawable.ic_menu_white);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setTitle(R.string.app_name);
        mMainLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        mBottomToolbar = (Toolbar) findViewById(R.id.toolbar_bottom);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        relativeLayoutDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        // get the listview
        expandableListView = (ExpandableListView) findViewById(R.id.expand_list_drawer);
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
//            parts = mCurrentDirDualPane.split("/");
            mCurrentDirDualPane = path;
            navDirectoryDualPane.removeAllViews();
        }

        FileListFragment fileListFragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id
                .main_container);
        mFileListFragment = fileListFragment;

        FileListDualFragment fileListDualFragment = (FileListDualFragment) getSupportFragmentManager()
                .findFragmentById(R.id
                        .frame_container_dual);
        mFileListDualFragment = fileListDualFragment;


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
                /*Count check so that ROOT is added only once in Navigation
                  Handles the scenario :
                  1. When Fav item is a root child and if we click on any folder in that fav item
                     multiple ROOT blocks are not added to Navigation view*/
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

      /*  if (mIsFavGroup) {
            createFragmentForFavGroup(dir);
        }
*/
        if (dir.equals(getInternalStorage().getAbsolutePath())) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_INTERNAL, dir, mFileListFragment, mFileListDualFragment);
        } else if (dir.equals("/")) {
            createNavButton(STORAGE_ROOT, dir, mFileListFragment, mFileListDualFragment);
        } else if (FileUtils.getExternalStorage() != null && dir.equals(FileUtils
                .getExternalStorage()
                .getAbsolutePath())) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_EXTERNAL, dir, mFileListFragment, mFileListDualFragment);
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

        Log.d("TAG", "Button tag DUAL=" + navDirectoryDualPane);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int level = (int) view.getTag();
//                String path  = (String) view.getTag();

//                if (!mCurrentDir.equals(path) && getSupportFragmentManager().findFragmentByTag
// (path) == null) {
//                    mCurrentDir = path;
//                    displayInitialFragment(path); // TODO Handle root case by passing /
//                }
//                Log.d("TAG", "Button tag click=" + level);
                Log.d("TAG", "Dir=" + dir);

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

                        fileListFragment.reloadList(false, mCurrentDir);
                        setNavDirectory(mCurrentDir, isDualPaneButtonClicked);
                /*        for (int i = navDirectory.getChildCount(); i > level; i--) {
                            navDirectory.removeViewAt(i - 1);
                        }*/

                    }
                } else {
                    if (!mCurrentDirDualPane.equals(dir)) {
                        mCurrentDirDualPane = dir;
                        fileListDualFragment.reloadList(true, mCurrentDirDualPane);
                        setNavDirectory(mCurrentDirDualPane, isDualPaneButtonClicked);
//
//                        for (int i = navDirectoryDualPane.getChildCount(); i > level; i--) {
//                            navDirectoryDualPane.removeViewAt(i - 1);
//                        }

                    }
                }
            }
        });
        if (!isDualPaneInFocus) {
            navDirectory.addView(button);
        } else {
            navDirectoryDualPane.addView(button);
        }
    }

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && mIsDualPaneEnabledSettings) {
            mIsDualModeEnabled = true;
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
        mIsDualModeEnabled = true;
        isDualPaneInFocus = true;
        if (fragment instanceof HomeScreenFragment) {
            ((HomeScreenFragment) fragment).setDualModeEnabled(true);
        }
        // For Files category only, show dual pane
        else if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
            toggleDualPaneVisibility(true);
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

                final Dialog dialog = new Dialog(
                        this);
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
                        String fileName = rename.getText().toString();
                        boolean isDir = false;
                        if (!FileUtils.validateFileName(fileName)) {
                            rename.setError(getResources().getString(R.string
                                    .msg_error_valid_name));
                            return;
                        }
                        fileName = fileName.trim();
                        if (view.getId() == R.id.fabCreateFolder ||
                                view.getId() == R.id.fabCreateFolderDual) {
                            isDir = true;
                        } else {
                            fileName = fileName + ".txt";
                        }
                        FileListFragment singlePaneFragment = (FileListFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.main_container);
                        FileListFragment dualPaneFragment = (FileListDualFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container_dual);


                        int result;
                        /**
                         * In landscape mode, FabCreateFile is on Dual Pane side and
                         * FabCreateFileDual on Single pane
                         */
                        if (view.getId() == R.id.fabCreateFile || view.getId() == R.id
                                .fabCreateFileDual) {
                            if (view.getId() == R.id.fabCreateFile) {
                                if (mIsDualModeEnabled) {
                                    result = FileUtils.createFile(mCurrentDirDualPane, fileName);
                                } else {
                                    result = FileUtils.createFile(mCurrentDir, fileName);
                                }

                            } else {
                                result = FileUtils.createFile(mCurrentDir, fileName);
                            }
                            if (result == 0) {
                                showMessage(getString(R.string.msg_file_create_success));

                                if (singlePaneFragment != null) {
                                    singlePaneFragment.refreshList();
                                }
                                if (dualPaneFragment != null) {
                                    dualPaneFragment.refreshList();
                                }
                            } else if (result == -2) {
                                rename.setError(getResources().getString(R.string
                                        .file_exists));
                                return;
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

                                if (singlePaneFragment != null) {
                                    singlePaneFragment.refreshList();
                                }
                                if (dualPaneFragment != null) {
                                    dualPaneFragment.refreshList();
                                }
                            } else if (result == -2) {
                                rename.setError(getResources().getString(R.string
                                        .file_exists));
                                return;
//                                showMessage(getString(R.string.file_exists));
                            } else {
                                showMessage(getString(R.string.msg_folder_create_failure));
                            }
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

    public void displaySelectedGroup(int groupPos, int childPos, String path) {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (mPrevCategory != mCategory) {
            mPrevCategory = mCategory;
        }
        if (fragment instanceof HomeScreenFragment) {
            mIsFromHomePage = true;
        } else {
            mIsFromHomePage = false;
        }
        switch (groupPos) {
            case 0:
            case 1:
                isCurrentDirRoot = groupPos == 0 && childPos == 0;
//                mToolbar.setTitle(getString(R.string.app_name));
                if (fragment instanceof FileListFragment) {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                    toggleDualPaneVisibility(true);
                    fabCreateMenu.setVisibility(View.VISIBLE);
                }

                mIsFavGroup = groupPos == 1;

                if (!isDualPaneInFocus) {
//                    mStartingDir = path;

                    if (mCurrentDir == null) {
                        mCurrentDir = path;
                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDir, mCategory);
                        if (fragment instanceof FileListFragment) {
                            setNavDirectory(mCurrentDir, isDualPaneInFocus);
                        }
                    } else if (!mCurrentDir.equals(path)) {
                        mCurrentDir = path;
//                        singlePaneFragments.clear();
                        // For Favourites
                        if (groupPos == 1) {
                            if (!mCurrentDir.contains(getInternalStorage().getAbsolutePath()) &&
                                    (FileUtils.getExternalStorage() == null || !mCurrentDir
                                            .contains(FileUtils.getExternalStorage().getAbsolutePath()))) {
                                isCurrentDirRoot = true;
                                mCurrentDir = "/";
                            }
                        }


                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDir, mCategory);
                        if (fragment instanceof FileListFragment) {
                            setNavDirectory(mCurrentDir, isDualPaneInFocus);
                        }
                    }

                } else {
//                    mStartingDirDualPane = path;
                    if (mCurrentDirDualPane == null) {
                        mCurrentDirDualPane = path;
//                        dualPaneFragments.clear();
//                        setNavDirectory(mCurrentDirDualPane, isDualPaneInFocus);
                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDirDualPane, mCategory);
                    } else if (!mCurrentDirDualPane.equals(path)) {
                        mCurrentDirDualPane = mStartingDirDualPane;
//                        dualPaneFragments.clear();
                        // For Favourites
                        if (groupPos == 1) {
                            if (mCurrentDirDualPane.contains(getInternalStorage().getAbsolutePath
                                    ())) {
                                mStartingDirDualPane = getInternalStorage().getAbsolutePath();
                            } else if (FileUtils.getExternalStorage() != null && mCurrentDirDualPane
                                    .contains(FileUtils
                                            .getExternalStorage().getAbsolutePath())) {
                                mStartingDirDualPane = getInternalStorage().getAbsolutePath();
                            } else {
                                isCurrentDirRoot = true;
                                mStartingDirDualPane = "/";
                            }
                        }
//                        setNavDirectory(mCurrentDirDualPane, isDualPaneInFocus);
                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDirDualPane, mCategory);
                    }
                    if (fragment instanceof FileListFragment) {
                        setNavDirectory(mCurrentDirDualPane, isDualPaneInFocus);
                    }
                }
                break;
            // When Library category item is clicked
            case 2:
                mNavigationLayout.setVisibility(View.GONE);
                toggleDualPaneVisibility(false);
//                mCurrentDir = null;
                fabCreateMenu.setVisibility(View.GONE);

                switch (childPos) {
                    // When Audio item is clicked
                    case 0:
//                        mToolbar.setTitle(MUSIC);

                        mCategory = FileConstants.CATEGORY.AUDIO.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                    // When Video item is clicked
                    case 1:
//                        mToolbar.setTitle(VIDEO);
                        mCategory = FileConstants.CATEGORY.VIDEO.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                    // When Images item is clicked
                    case 2:
//                        mToolbar.setTitle(IMAGES);
                        mCategory = FileConstants.CATEGORY.IMAGE.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                    // When Documents item is clicked
                    case 3:
//                        mToolbar.setTitle(DOCS);
                        mCategory = FileConstants.CATEGORY.DOCS.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                }
                setTitleForCategory(mCategory);
                break;
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
            ft.commitAllowingStateLoss();
            toggleViews(false);
            boolean value = mCategory == FileConstants.CATEGORY.FILES.getValue();
            toggleDualPaneVisibility(value);

            if (isDualPaneInFocus) {
                args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
                FileListDualFragment fileListDualFragment = new FileListDualFragment();
                fileListDualFragment.setArguments(args);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_container_dual, fileListDualFragment, directory);
                ft.commitAllowingStateLoss();
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
            toggleViews(true);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, true);
            args.putBoolean(BaseActivity.PREFS_FIRST_RUN, mIsFirstRun);
            args.putBoolean(FileConstants.PREFS_DUAL_ENABLED, mIsDualModeEnabled);

//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
            homeScreenFragment.setArguments(args);
//            ft.replace(R.id.frame_container, homeScreenFragment);
            ft.replace(R.id.main_container, homeScreenFragment);

//            ft.addToBackStack(FileConstants.KEY_HOME);
            ft.commitAllowingStateLoss();
        } else {

            toggleViews(false);
            // Initialising only if Home screen disabled
            mCurrentDir = getInternalStorage().getAbsolutePath();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, mCurrentDir);
            args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            args.putInt(BaseActivity.ACTION_GROUP_POS, 0); // Storage Group
            args.putInt(BaseActivity.ACTION_CHILD_POS, 1); // Internal Storage child
//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
//            ft.replace(R.id.frame_container, fileListFragment, mCurrentDir);
            ft.replace(R.id.main_container, fileListFragment, mCurrentDir);
//
//            ft.addToBackStack(mCurrentDir);
//            ft.commitAllowingStateLoss();
            ft.commit();
            if (mIsDualModeEnabled) {
                toggleDualPaneVisibility(true);
                createDualFragment();
            }
        }
    }

    public void toggleViews(boolean isHomeScreenEnabled) {
        if (isHomeScreenEnabled) {
            mNavigationLayout.setVisibility(View.GONE);
            frameLayoutFab.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
        } else {
            mNavigationLayout.setVisibility(View.VISIBLE);
            frameLayoutFab.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged" + newConfig.orientation);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

  /*          if (mIsDualPaneEnabledSettings) {
                mIsDualModeEnabled = true;
                isDualPaneInFocus = true;
                if (fragment instanceof HomeScreenFragment) {
                    ((HomeScreenFragment) fragment).setDualModeEnabled(true);
                }

                // For Files category only, show dual pane
                else if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                    toggleDualPaneVisibility(true);
                    createDualFragment();

                }
            }*/
            showDualPane();


//            ft.addToBackStack(null);

        } else {
            mIsDualModeEnabled = false;
            isDualPaneInFocus = false;
            if (fragment instanceof HomeScreenFragment) {
                ((HomeScreenFragment) fragment).setDualModeEnabled(false);
            } else {
                toggleDualPaneVisibility(false);
            }
        }
        super.onConfigurationChanged(newConfig);
/*        if (fragment instanceof FileListFragment) {
            ((FileListFragment) fragment).refreshSpan();
        }*/
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
//        setNavDirectory(mCurrentDirDualPane, true);
        FileListDualFragment dualFragment = new FileListDualFragment();
//                dualPaneFragments.add(dualFragment);
        dualFragment.setArguments(args);
        ft.replace(R.id.frame_container_dual, dualFragment);
//                mViewSeperator.setVisibility(View.VISIBLE);
        ft.commitAllowingStateLoss();
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        Fragment dualFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

        Logger.log(TAG, "Onbackpress--fragment=" + fragment);

        mIsFavGroup = false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fabCreateMenu.isExpanded()) {
            fabCreateMenu.collapse();
        } else if (mIsHomePageRemoved) {
            super.onBackPressed();
        } else if (mIsHomePageAdded) {
            initialScreenSetup(true);
            mIsHomePageAdded = false;
        } else if (fragment instanceof FileListFragment) {
            if (isDualPaneInFocus) {
                if (!mStartingDirDualPane.equals(mCurrentDirDualPane) || mPrevCategory !=
                        mCategory) {
                    if (mPrevCategory == FileConstants.CATEGORY.FILES.getValue()) {
                        mCurrentDirDualPane = new File(mCurrentDirDualPane).getParent();
                    } else {
                        toggleViews(false);
                    }
                    ((FileListDualFragment) dualFragment).setCategory(mPrevCategory);
                    if (mPrevCategory != mCategory) {
                        mPrevCategory = mCategory;
                    }
//                    String parent = new File(mCurrentDirDualPane).getParent();

                    ((FileListDualFragment) dualFragment).reloadList(true, mCurrentDirDualPane);
                    setNavDirectory(mCurrentDirDualPane, true);
                } else {
                    removeFragmentFromBackStack();
                }
            } else {

                if (((FileListFragment) fragment).getIsZipMode()) {
                    if (((FileListFragment) fragment).checkZipMode()) {
                        ((FileListFragment) fragment).reloadList(false, mCurrentDir);
                        setNavDirectory(mCurrentDir, false);
                    }
                } else if (!mStartingDir.equals(mCurrentDir) && mCategory == FileConstants.CATEGORY
                        .FILES.getValue()) {
        /*            if (mPrevCategory == FileConstants.CATEGORY.FILES.getValue()) {

                        if (!mStartingDir.equals(mCurrentDir)) {
                            mCurrentDir = new File(mCurrentDir).getParent();
                        }
                        toggleViews(false);
                    } else {
                        toggleViews(true);
                    }
*/
                    if (!mStartingDir.equals(mCurrentDir)) {
                        if (isCurrentDirRoot) {
                            mCurrentDir = mStartingDir;
                            isCurrentDirRoot = false;
                        } else {
                            mCurrentDir = new File(mCurrentDir).getParent();
                        }
                    }
                    ((FileListFragment) fragment).setCategory(mCategory);
          /*          if (mPrevCategory != mCategory) {
                        mPrevCategory = mCategory;
                    }*/
                    ((FileListFragment) fragment).reloadList(false, mCurrentDir);
                    setNavDirectory(mCurrentDir, false);
                } else {

                    removeFragmentFromBackStack();
                }
            }

        } else {
            Logger.log(TAG, "Onbackpress--ELSE=");
            super.onBackPressed();
        }
    }

    public void setCurrentCategory(int category) {
        mCategory = category;
    }

    public void setIsFromHomePage(boolean isFromHomePage) {
        mIsFromHomePage = isFromHomePage;
    }

    private void removeFragmentFromBackStack() {
        setTitleForCategory(FileConstants.CATEGORY.FILES.getValue());
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        Logger.log(TAG,"On backpress--Backstack=="+backStackCount+"fromhome="+mIsFromHomePage);
//        FileListDualFragment fileListDualFragment = (FileListDualFragment) getSupportFragmentManager().
//                findFragmentById(R.id.frame_container_dual);
        if (!mIsFromHomePage) {
            if (mCategory != FileConstants.CATEGORY
                    .FILES.getValue()) {
                mCategory = FileConstants.CATEGORY
                        .FILES.getValue();
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id
                        .main_container);
                if (fragment instanceof FileListFragment) {
                    ((FileListFragment) fragment).setCategory(mCategory);
                    ((FileListFragment) fragment).reloadList(isDualPaneInFocus, mCurrentDir);
                }
                toggleViews(false);
            } else {
                getSupportFragmentManager().popBackStack();
                toggleViews(true);
                if (mIsDualModeEnabled && mIsDualPaneEnabledSettings) {
                    toggleDualPaneVisibility(false);
                }
            }
            mFrameDualPane.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
        } else {
            toggleViews(true);
            if (mIsDualModeEnabled && mIsDualPaneEnabledSettings) {
                toggleDualPaneVisibility(false);
            }
//            getSupportFragmentManager().popBackStack();

            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
//        sharedPreferenceWrapper.savePrefs(this, mViewMode);
        unregisterForContextMenu(expandableListView);
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


    private boolean mIsHomeSettingToggled;
    private boolean mIsHomePageRemoved;

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
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.replace(R.id.main_container, fileListFragment);
//                    ft.addToBackStack(null);
//                        ft.commitAllowingStateLoss();
            ft.commit();
            mIsHomeSettingToggled = false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case FileConstants.PREFS_HIDDEN:

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
                break;
            case FileConstants.PREFS_HOMESCREEN:
                boolean isHomeScreenEnabled = sharedPreferences.getBoolean(FileConstants
                        .PREFS_HOMESCREEN, true);
                if (isHomeScreenEnabled != mIsHomeScreenEnabled) {
                    mIsHomeScreenEnabled = isHomeScreenEnabled;
                    Log.d(TAG, "OnPrefschanged " +
                            "CALLED==getSupportFragmentManager=" + getSupportFragmentManager());
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

                    Log.d(TAG, "OnPrefschanged ==fragment=" + fragment);

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
                        ft.replace(R.id.frame_container, storagesFragment);
//                    ft.addToBackStack(null);
//                        ft.commitAllowingStateLoss();
                        ft.commit();*/

                        } else {
                            mIsHomePageRemoved = true;
                        }


                    } else {
                        mIsHomePageAdded = true;
                        mIsHomePageRemoved = false;

                    }
                }


                break;
            case FileConstants.PREFS_DUAL_PANE:
                mIsDualPaneEnabledSettings = sharedPreferences.getBoolean(FileConstants
                        .PREFS_DUAL_PANE, true);
//            isDualPaneInFocus = false;

                if (!mIsDualPaneEnabledSettings) {
                    toggleDualPaneVisibility(false);
                } else {
                    if (mCategory == FileConstants.CATEGORY.FILES.getValue() && mIsDualModeEnabled) {
                        isDualPaneInFocus = true;
                        toggleDualPaneVisibility(true);
                        createDualFragment();
                 /*   FragmentTransaction ft = getChildFragmentManager()
                            .beginTransaction();
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
                    ft.commitAllowingStateLoss();*/
                    }
                }

                break;
        }
    }

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
}

