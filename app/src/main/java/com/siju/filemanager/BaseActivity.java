package com.siju.filemanager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.FavInfo;
import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileInfo;
import com.siju.filemanager.filesystem.FileListAdapter;
import com.siju.filemanager.filesystem.FileListDualFragment;
import com.siju.filemanager.filesystem.FileListFragment;
import com.siju.filemanager.filesystem.FileUtils;
import com.siju.filemanager.filesystem.SharedPreference;
import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;
import com.siju.filemanager.ui.EnhancedMenuInflater;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.siju.filemanager.filesystem.FileUtils.getInternalStorage;


public class BaseActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener,
        View.OnClickListener {

    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    String[] listDataHeader;
    List<String> mListHeader;
    HashMap<String, List<String>> listDataChild;
    ArrayList<SectionGroup> totalGroup;
    public static final String ACTION_VIEW_FOLDER_LIST = "folder_list";
    public static final String ACTION_DUAL_VIEW_FOLDER_LIST = "dual_folder_list";
    public static final String ACTION_DUAL_PANEL = "ACTION_DUAL_PANEL";

    private DrawerLayout drawerLayout;
    private RelativeLayout relativeLayoutDrawerPane;
    private String mCurrentDir = getInternalStorage().getAbsolutePath();
    private String mCurrentDirDualPane = getInternalStorage().getAbsolutePath();
    public String STORAGE_ROOT, STORAGE_INTERNAL, STORAGE_EXTERNAL, DOWNLOADS, IMAGES, VIDEO, MUSIC, DOCS, SETTINGS,
            RATE;
    private boolean mIsDualMode;
    private LinearLayout navDirectory;
    private String mStartingDir = getInternalStorage().getAbsolutePath();
    private HorizontalScrollView scrollNavigation, scrollNavigationDualPane;
    private int navigationLevelSinglePane = 0;
    private int navigationLevelDualPane = 0;
    private String mStartingDirDualPane = getInternalStorage().getAbsolutePath();
    private LinearLayout navDirectoryDualPane;
    // Returns true if user is currently navigating in Dual Panel fragment
    private boolean isDualPaneInFocus;
    private List<Fragment> singlePaneFragments = new ArrayList<>();
    private List<Fragment> dualPaneFragments = new ArrayList<>();
    private boolean isCurrentDirRoot;
    private Toolbar mBottomToolbar;
    private ActionMode mActionMode;
    private FileListAdapter fileListAdapter;
    private SparseBooleanArray mSelectedItemPositions = new SparseBooleanArray();
    MenuItem mPasteItem, mRenameItem, mInfoItem;
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
    private FloatingActionsMenu fabCreateMenu;
    private FloatingActionButton fabCreateFolder;
    private FloatingActionButton fabCreateFile;
    private ArrayList<SectionItems> favouritesGroupChild = new ArrayList<>();
    public static final String KEY_FAV = "KEY_FAVOURITES";
    private SharedPreference sharedPreference;
    private ArrayList<FavInfo> savedFavourites = new ArrayList<>();
    private View mViewSeperator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        getSavedFavourites();
        initConstants();
        initViews();
        checkScreenOrientation();
        initListeners();
        Logger.log("TAG", "on create--Activity");
        prepareListData();
        setListAdapter();
        setNavDirectory();
        displayInitialFragment(mCurrentDir);
    }

    private void getSavedFavourites() {
        sharedPreference = new SharedPreference();
        savedFavourites = sharedPreference.getFavorites(this);
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

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mIsDualMode = true;
            mViewSeperator.setVisibility(View.VISIBLE);
        } else {
            mIsDualMode = false;
            mViewSeperator.setVisibility(View.GONE);

        }
    }


    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBottomToolbar = (Toolbar) findViewById(R.id.toolbar_bottom);

        fabCreateMenu = (FloatingActionsMenu) findViewById(R.id.fabCreate);
        fabCreateFolder = (FloatingActionButton) findViewById(R.id.fabCreateFolder);
        fabCreateFile = (FloatingActionButton) findViewById(R.id.fabCreateFile);
        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayoutFab);
        frameLayout.getBackground().setAlpha(0);
        fabCreateMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener() {


            @Override
            public void onMenuExpanded() {
                frameLayout.getBackground().setAlpha(240);
                frameLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        fabCreateMenu.collapse();
                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                frameLayout.getBackground().setAlpha(0);
                frameLayout.setOnTouchListener(null);
            }
        });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        relativeLayoutDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        // get the listview
        expandableListView = (ExpandableListView) findViewById(R.id.expand_list_drawer);
        navDirectory = (LinearLayout) findViewById(R.id.navButtons);
        navDirectoryDualPane = (LinearLayout) findViewById(R.id.navButtonsDualPane);
        scrollNavigation = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
        scrollNavigationDualPane = (HorizontalScrollView) findViewById(R.id.scrollNavigationDualPane);
        mViewSeperator = findViewById(R.id.viewSeperator);

    }

    private void initListeners() {
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long
                    id) {
                Log.d("TAG", "Group pos-->" + groupPosition + "CHILD POS-->" + childPosition);
                displaySelectedGroup(groupPosition, childPosition);
                return false;
            }
        });
        fabCreateFile.setOnClickListener(this);
        fabCreateFolder.setOnClickListener(this);
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

    private void initializeStorageGroup() {
        ArrayList<SectionItems> storageGroupChild = new ArrayList<>();
        File systemDir = FileUtils.getRootDirectory();
        File rootDir = systemDir.getParentFile();
        File internalSD = FileUtils.getInternalStorage();
        File extSD = FileUtils.getExternalStorage();
        storageGroupChild.add(new SectionItems(STORAGE_ROOT, storageSpace(systemDir), R.drawable
                .ic_root_white,
                FileUtils
                        .getAbsolutePath(rootDir)));
        storageGroupChild.add(new SectionItems(STORAGE_INTERNAL, storageSpace(internalSD), R.drawable
                .ic_phone_white, FileUtils.getAbsolutePath(internalSD)));
        if (extSD != null) {
            storageGroupChild.add(new SectionItems(STORAGE_EXTERNAL, storageSpace(extSD), R.drawable.ic_ext_white,
                    FileUtils.getAbsolutePath(extSD)));
        }
        totalGroup.add(new SectionGroup(mListHeader.get(0), storageGroupChild));
    }

    private void initializeFavouritesGroup() {

        String path = FileUtils
                .getAbsolutePath(FileUtils.getDownloadsDirectory());
        favouritesGroupChild.add(new SectionItems(DOWNLOADS, path, R.drawable.d_613854,
                path));
        if (savedFavourites != null && savedFavourites.size() > 0) {
            for (int i = 0; i < savedFavourites.size(); i++) {
                String savedPath = savedFavourites.get(i).getFilePath();
                favouritesGroupChild.add(new SectionItems(savedFavourites.get(i).getFileName(), savedPath, R.drawable
                        .f_613854,
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
        othersGroupChild.add(new SectionItems(RATE, null, R.drawable.ic_rate_white, null));
        othersGroupChild.add(new SectionItems(SETTINGS, null, R.drawable.ic_settings_white, null));
        totalGroup.add(new SectionGroup(mListHeader.get(3), othersGroupChild));
    }


    private String storageSpace(File file) {
        String freePlaceholder = " " + getResources().getString(R.string.msg_free) + " ";
        return FileUtils.getSpaceLeft(this, file) + freePlaceholder + FileUtils.getTotalSpace(this, file);
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
     * Called everytime when a file item is clicked
     *
     * @param intent Contains path of the file whose children need to be shown
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean intentHandled = createFragmentForIntent(intent);
//        Log.d(TAG, "On onNewIntent");
    }

    private void displaySelectedGroup(int groupPos, int childPos) {

        switch (groupPos) {
            case 0:
            case 1:
                if (!isDualPaneInFocus) {
                    mStartingDir = totalGroup.get(groupPos).getmChildItems().get(childPos).getPath();

                    if (!mCurrentDir.equals(mStartingDir)) {
                        mCurrentDir = mStartingDir;
                        singlePaneFragments.clear();
                        setNavDirectory();
                        displayInitialFragment(mCurrentDir);
                    }
                } else {
                    mStartingDirDualPane = totalGroup.get(groupPos).getmChildItems().get(childPos).getPath();
                    if (!mCurrentDirDualPane.equals(mStartingDirDualPane)) {
                        mCurrentDirDualPane = mStartingDirDualPane;
                        dualPaneFragments.clear();
                        setNavDirectory();
                        displayInitialFragment(mCurrentDirDualPane);
                    }
                }
                break;

        }

    }

    private void setNavDirectory() {
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
                if (isCurrentDirRoot) {
                    setNavDir("/", "/");
                }
                setNavDir(dir, parts[i]);
            }
        }

    }

    private void setNavDir(String dir, String parts) {


        int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

        if (dir.equals(getInternalStorage().getAbsolutePath())) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_INTERNAL, dir);
        } else if (dir.equals("/")) {
            createNavButton(STORAGE_ROOT, dir);
        } else if (FileUtils.getExternalStorage() != null && dir.equals(FileUtils.getExternalStorage()
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
                        HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
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

//                if (!mCurrentDir.equals(path) && getSupportFragmentManager().findFragmentByTag(path) == null) {
//                    mCurrentDir = path;
//                    displayInitialFragment(path); // TODO Handle root case by passing /
//                }
                Log.d("TAG", "Button tag click=" + level);
                Log.d("TAG", "Dir=" + dir);
//                boolean isUpNavigation = checkIfUpNavigation(level);
//                if (isUpNavigation) {

                int singlePaneCount = singlePaneFragments.size();
                int dualPaneCount = dualPaneFragments.size();

                if (!isDualPaneInFocus) {
                    if (!mCurrentDir.equals(dir)) {
                        mCurrentDir = dir;

                        Fragment fragment = singlePaneFragments.get(level - 1);
                        replaceFragment(fragment);
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
                        replaceFragment(fragment);
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
    }


    /**
     *
     */
    private void displayInitialFragment(String directory) {
        // update the main content by replacing fragments
        // Fragment fragment = null;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        if (mIsDualMode) {
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

    private void removeFragments(int level, String newPath) {

//        int fragCount = getSupportFragmentManager().getBackStackEntryCount();
//        for (int i = fragCount; i > level; i--) {
//            getSupportFragmentManager().popBackStack();
//
//        }
//        String newPathParts[] = newPath.split("/");
//        int count = newPathParts.length;
//        String currentDirParts[] = mCurrentDir.split("/");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (!isDualPaneInFocus) {
            int fragCount = singlePaneFragments.size();
            for (int i = fragCount; i > level; i--) {
                Fragment fragment = singlePaneFragments.get(i - 1); // Since list starts from 0, so i-1
                singlePaneFragments.remove(fragment);
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//                if (getSupportFragmentManager().findFragmentById(R.id.frame_container).)
//                getSupportFragmentManager().popBackStack();
//                getSupportFragmentManager().findFragmentById().

            }

        } else {
            int fragCount = dualPaneFragments.size();
            for (int i = fragCount; i > level; i--) {
                Fragment fragment = dualPaneFragments.get(i - 1); // Since list starts from 0, so i-1
                dualPaneFragments.remove(fragment);
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//                getSupportFragmentManager().popBackStack();

            }

        }


    }

    private boolean checkIfUpNavigation(int level) {
        if (getSupportFragmentManager().getBackStackEntryCount() > level) {
            return true;
        } else {
            return false;
        }
    }


    private boolean createFragmentForIntent(Intent intent) {

        if (intent.getAction() != null) {
            final String action = intent.getAction();
            Fragment targetFragment = null;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            isDualPaneInFocus = intent.getBooleanExtra(ACTION_DUAL_PANEL, false);


            if (action.equals(ACTION_VIEW_FOLDER_LIST)) {
                targetFragment = new FileListFragment();

//                intent.putExtra(FileConstants.KEY_DUAL_MODE, false);
                transaction.replace(R.id.frame_container, targetFragment, mCurrentDirDualPane);
            } else if (action.equals(ACTION_DUAL_VIEW_FOLDER_LIST)) {
                targetFragment = new FileListDualFragment();
//                intent.putExtra(FileConstants.KEY_DUAL_MODE, true);
                transaction.replace(R.id.frame_container_dual, targetFragment, mCurrentDir);
            }

            if (!isDualPaneInFocus) {
                mCurrentDir = intent.getStringExtra(FileConstants.KEY_PATH);
                singlePaneFragments.add(targetFragment);
            } else {
                mCurrentDirDualPane = intent.getStringExtra(FileConstants.KEY_PATH);
                dualPaneFragments.add(targetFragment);

            }
            Logger.log("TAG", "createFragmentForIntent--currentdir=" + mCurrentDir);


            setNavDirectory();
            if (targetFragment != null) {
                targetFragment.setArguments(intent.getExtras());
                transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//                transaction.addToBackStack(null);
                transaction.commit();
            }
            return true;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Logger.log("TAG", "Onbackpress--TOTAL FRAGMENTS count=" + count);


        int singlePaneCount = singlePaneFragments.size();
        int dualPaneCount = dualPaneFragments.size();

//        Logger.log("TAG", "Onbackpress--SINGLEPANEL count=" + singlePaneCount);
//        Logger.log("TAG", "Onbackpress--DUALPANEL count=" + dualPaneCount);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fabCreateMenu.isExpanded()) {
            fabCreateMenu.collapse();
        } else {
            if (!isDualPaneInFocus) {

                if (navigationLevelSinglePane != 0) {
                    navigationLevelSinglePane--;
                }

            } else {
                if (navigationLevelDualPane != 0) {
                    navigationLevelDualPane--;
                }
            }

            if (!isDualPaneInFocus) {
                if (singlePaneCount == 1) {
//                    finish();
//                    getSupportFragmentManager().popBackStack();
//                    getSupportFragmentManager().popBackStack();
                    super.onBackPressed();

                } else {
                    // Changing the current dir  to 1 up on back press
                    mCurrentDir = new File(mCurrentDir).getParent();
                    Log.d("TAG", "Onbackpress--mCurrentDir=" + mCurrentDir);
                    int childCount = navDirectory.getChildCount();
                    Log.d("TAG", "Onbackpress--Navbuttons count=" + childCount);
                    navDirectory.removeViewAt(childCount - 1); // Remove view
                    navDirectory.removeViewAt(childCount - 2); // Remove > symbol
                    scrollNavigation.postDelayed(new Runnable() {
                        public void run() {
                            HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
                            hv.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                        }
                    }, 100L);
//                    if (singlePaneCount == 2) {
//                        Fragment fragment = singlePaneFragments.get(singlePaneCount - 1);
//                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//                    }
//                    else {
                    Fragment fragment = singlePaneFragments.get(singlePaneCount - 2);
                    replaceFragment(fragment);
//                    }
                    singlePaneFragments.remove(singlePaneCount - 1);  // Removing the last fragment

                }

            } else {
                if (dualPaneCount == 1) {
//                    finish();
                    super.onBackPressed();
                } else {
                    mCurrentDirDualPane = new File(mCurrentDirDualPane).getParent();
                    Log.d("TAG", "Onbackpress--mCurrentDirDual=" + mCurrentDirDualPane);
                    int childCount = navDirectoryDualPane.getChildCount();
                    Log.d("TAG", "Onbackpress--Navbuttonsdualpane childCount=" + childCount);
                    navDirectoryDualPane.removeViewAt(childCount - 1); // Remove view
                    navDirectoryDualPane.removeViewAt(childCount - 2); // Remove > symbol
                    scrollNavigationDualPane.postDelayed(new Runnable() {
                        public void run() {
                            HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id
                                    .scrollNavigationDualPane);
                            hv.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                        }
                    }, 100L);
                    Fragment fragment = dualPaneFragments.get(dualPaneCount - 2);
                    replaceFragment(fragment);
                    dualPaneFragments.remove(dualPaneCount - 1);  // Removing the last fragment

                }


//                getSupportFragmentManager().beginTransaction().remove()

//                if (getSupportFragmentManager().getBackStackEntryAt(count-1).getName() == R.id.frame_container_dual) {
//                    Logger.log("TAG","TRUE");
//
//                }
//                else {
//                    Logger.log("TAG","FALSE"+getSupportFragmentManager().getBackStackEntryAt(co.unt-1).getId());
//
//                }
            }
//            super.onBackPressed();
        }


//        if (count == 1) {
//            finish();
//        } else {
//            int childCount = navDirectory.getChildCount();
//            Log.d("TAG", "Onbackpress--childCount count=" + childCount);
//            navDirectory.removeViewAt(childCount - 1); // Remove view
//            navDirectory.removeViewAt(childCount - 2); // Remove > symbol
//            scrollNavigation.postDelayed(new Runnable() {
//                public void run() {
//                    HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
//                    hv.fullScroll(HorizontalScrollView.FOCUS_LEFT);
//                }
//            }, 100L);
//            super.onBackPressed();
//        }

        /*else {
            super.onBackPressed();

        }*/
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        Logger.log("TAG","Fragment tag ="+fragment.getTag());
        String path = fragment.getArguments().getString(FileConstants.KEY_PATH);
        Logger.log("TAG", "Fragment bundle =" + path);
        if (isDualPaneInFocus) {
            FileListDualFragment dualFragment = new FileListDualFragment();
            dualFragment.setArguments(fragment.getArguments());
            fragmentTransaction.replace(R.id.frame_container_dual, dualFragment, path);
        } else {
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(fragment.getArguments());
            fragmentTransaction.replace(R.id.frame_container, fileListFragment, path);
        }
        fragmentTransaction.commitAllowingStateLoss();

    }

    private void removeSpecificFragmentFromBackStack(int backStackCount, int containerId) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_paste:
                pasteOperationCleanUp();
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        checkIfFileExists(mFileList.get(mSelectedItemPositions.keyAt(i)).getFilePath(), new File
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
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());
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


        Logger.log("TAG", "SOURCE==" + sourceFilePath + "isPasteConflictDialogShown==" + isPasteConflictDialogShown);

    }

    private void showDialog(final String sourceFilePath) {
        Context mContext = BaseActivity.this;
        mPasteConflictDialog = new Dialog(mContext);
        mPasteConflictDialog.setContentView(R.layout.dialog_paste_conflict);
//        mPasteConflictDialog.setTitle(getResources().getString(R.string.dialog_title_paste_conflict));
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
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());

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


    @Override
    public void onClick(final View view) {
        switch (view.getId()) {

            case R.id.fabCreateFile:
            case R.id.fabCreateFolder:
                fabCreateMenu.collapse();
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

                // dialog save button to save the edited item
                Button saveButton = (Button) dialog
                        .findViewById(R.id.buttonRename);
                // for updating the list item
                saveButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        final CharSequence name = rename.getText();
                        if (name.length() == 0) {
                            rename.setError(getResources().getString(R.string.msg_error_valid_name));
                            return;
                        }
                        FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                                .findFragmentById(R
                                        .id.frame_container);
                        String fileName = rename.getText().toString() + "";

                        int result;
                        if (view.getId() == R.id.fabCreateFile) {
                            result = FileUtils.createFile(mCurrentDir, fileName + ".txt");
                            if (result == 0) {
                                showMessage(getString(R.string.msg_file_create_success));
                            } else {
                                showMessage(getString(R.string.msg_file_create_failure));
                            }
                        } else {
                            result = FileUtils.createDir(mCurrentDir, fileName);
                            if (result == 0) {
                                showMessage(getString(R.string.msg_folder_create_success));
                            } else {
                                showMessage(getString(R.string.msg_folder_create_failure));
                            }
                        }

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
     * Shows another alert dialog when user clicks on any button Skip,Keep or Replace
     *
     * @param action Button action --> {@link FileUtils#ACTION_REPLACE,FileUtils#ACTION_SKIP,FileUtils#ACTION_KEEP}
     *               Calls Async task to do the copy operation once user resolves all conflicts
     */
    private void checkIfPasteConflictFinished(int action) {
        mPasteConflictDialog.dismiss();
        int count = ++tempConflictCounter;
        mPasteAction = action;
        mPathActionMap.put(mSourceFilePath, mPasteAction);
        Logger.log("TAG", "tempConflictCounter==" + tempConflictCounter + "tempSize==" + tempSourceFile.size());
        if (count < tempSourceFile.size()) {
            showDialog(tempSourceFile.get(count));
        } else {
            callAsyncTask();
        }
    }


    public void setFileList(ArrayList<FileInfo> list) {
        mFileList = list;
    }

    public void startActionMode() {

        fabCreateMenu.setVisibility(View.GONE);
        mBottomToolbar.setVisibility(View.VISIBLE);
        mBottomToolbar.startActionMode(new ActionModeCallback());
//        mBottomToolbar.inflateMenu(R.menu.action_mode_bottom);
        mBottomToolbar.getMenu().clear();
        EnhancedMenuInflater.inflate(getMenuInflater(), mBottomToolbar.getMenu(), true);
        mBottomToolbar.setOnMenuItemClickListener(this);
    }

    private void togglePasteVisibility(boolean isVisible) {
        mPasteItem.setVisible(isVisible);
    }


    public ActionMode getActionMode() {
        return mActionMode;
    }

    public void setFileListAdapter(FileListAdapter adapter) {
        fileListAdapter = adapter;
    }

    public void setSelectedItemPos(SparseBooleanArray selectedItemPos) {
        mSelectedItemPositions = selectedItemPos;
        if (selectedItemPos.size() > 1) {
            mRenameItem.setVisible(false);
            mInfoItem.setVisible(false);

        } else {
            mRenameItem.setVisible(true);
            mInfoItem.setVisible(true);
        }
    }

    /**
     * Toolbar menu item click listener
     *
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_cut:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    showMessage(mSelectedItemPositions.size() + " " + getString(R.string.msg_cut_copy));
                    mIsMoveOperation = true;
                    togglePasteVisibility(true);
                    mActionMode.finish();
                }
                break;
            case R.id.action_copy:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    mIsMoveOperation = false;
                    showMessage(mSelectedItemPositions.size() + " " + getString(R.string.msg_cut_copy));
                    togglePasteVisibility(true);
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
                    FileUtils.shareFiles(this, filesToShare);
                    mActionMode.finish();
                }
                break;
        }
        return false;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Triggered on long press click on item
     */
    private final class ActionModeCallback implements ActionMode.Callback {


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            MenuInflater inflater = mActionMode.getMenuInflater();
            inflater.inflate(R.menu.action_mode, menu);
            mPasteItem = menu.findItem(R.id.action_paste);
            mRenameItem = menu.findItem(R.id.action_rename);
            mInfoItem = menu.findItem(R.id.action_info);
            return true;
        }

        @SuppressLint("NewApi")
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            if (mSelectedItemPositions.size() > 1) {
                mRenameItem.setVisible(false);
                mInfoItem.setVisible(false);

            } else {
                mRenameItem.setVisible(true);
                mInfoItem.setVisible(true);
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rename:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        final String filePath = mFileList.get(mSelectedItemPositions.keyAt(0)).getFilePath();
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                        String extension = null;
                        boolean file = false;
                        if (new File(filePath).isFile()) {
                            String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                            fileName = tokens[0];
                            extension = tokens[1];
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
                        // dialog save button to save the edited item
                        Button saveButton = (Button) dialog
                                .findViewById(R.id.buttonRename);
                        // for updating the list item
                        saveButton.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                final CharSequence name = rename.getText();
                                if (name.length() == 0) {
                                    rename.setError(getResources().getString(R.string.msg_error_valid_name));
                                    return;
                                }
                                FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container);
                                String renamedName;
                                if (isFile) {
                                    renamedName = rename.getText().toString() + "." + ext;
                                } else {
                                    renamedName = rename.getText().toString();
                                }
                                FileUtils.renameTarget(filePath, renamedName);

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
                        FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
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
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            FileListFragment fileListFragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id
                    .frame_container);
            if (fileListFragment != null) {
                fileListFragment.clearSelection();
            }
            mActionMode = null;
            mBottomToolbar.setVisibility(View.GONE);
            fabCreateMenu.setVisibility(View.VISIBLE);
        }
    }

    private void updateFavouritesGroup(FileInfo info) {

        String name = info.getFileName();
        String path = info.getFilePath();
        FavInfo favInfo = new FavInfo();
        favInfo.setFileName(name);
        favInfo.setFilePath(path);
        sharedPreference.addFavorite(this, favInfo);
        favouritesGroupChild.add(new SectionItems(name, null, R.drawable.ic_folder_white, path));
        expandableListAdapter.notifyDataSetChanged();

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
        textReadable.setText(isReadable ? getString(R.string.yes) : getString(R.string.no));
        textWriteable.setText(isWriteable ? getString(R.string.yes) : getString(R.string.no));
        textHidden.setText(isHidden ? getString(R.string.yes) : getString(R.string.no));

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
            String md5 = FileUtils.getFastHash(path);
            textMD5.setText(md5);
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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("TAG", "On config" + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.VISIBLE);
            scrollNavigationDualPane.setVisibility(View.VISIBLE);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            String internalStoragePath = getInternalStorage().getAbsolutePath();
            Bundle args = new Bundle();
            args.putString(FileConstants.KEY_PATH, internalStoragePath);
            args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
            isDualPaneInFocus = true;
            setNavDirectory();
            FileListDualFragment dualFragment = new FileListDualFragment();
            dualPaneFragments.add(dualFragment);
            dualFragment.setArguments(args);
            ft.replace(R.id.frame_container_dual, dualFragment);
            mViewSeperator.setVisibility(View.VISIBLE);

//            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        } else {
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
            scrollNavigationDualPane.setVisibility(View.GONE);
            isDualPaneInFocus = false;
        }
        super.onConfigurationChanged(newConfig);
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
            stringBuilder.append("\n");
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

    }

    public class BackGroundOperationsTask extends AsyncTask<HashMap<String, Integer>, Integer, Void> {

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
                    TextView textTitle = (TextView) progressDialog.findViewById(R.id.textDialogTitle);
                    if (mIsMoveOperation) {
                        textTitle.setText(mContext.getString(R.string.msg_cut));
                    } else {
                        textTitle.setText(mContext.getString(R.string.msg_copy));
                    }

                    textFileName = (TextView) progressDialog.findViewById(R.id.textFileName);
                    textFileSource = (TextView) progressDialog.findViewById(R.id.textFileFromPath);
                    textFileDest = (TextView) progressDialog.findViewById(R.id.textFileToPath);
                    textFilesLeft = (TextView) progressDialog.findViewById(R.id.textFilesLeft);
                    textProgressPercent = (TextView) progressDialog.findViewById(R.id.textProgressPercent);
                    pasteProgress = (ProgressBar) progressDialog.findViewById(R.id.progressBarPaste);
                    Button buttonBackground = (Button) progressDialog.findViewById(R.id.buttonBg);

                    String fileName = mSourceFilePath.substring(mSourceFilePath.lastIndexOf("/") + 1, mSourceFilePath
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
                                copyStatus = FileUtils.copyToDirectory(BaseActivity.this, sourcePath, mCurrentDir,
                                        mIsMoveOperation, action, progress);
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
//                    System.out.println("progress ELSE: " + progress + "currentFile:" + currentFile);
                progressDialog.dismiss();
            }


        }

        @Override
        protected void onPostExecute(Void result) {
            FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R
                    .id.frame_container);

            switch (operation) {

                case PASTE_OPERATION:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {
                        mSelectedItemPositions.clear();
                    }
                    togglePasteVisibility(false);
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
                            showMessage(getResources().getQuantityString(R.plurals.number_of_files, filesCopied,
                                    filesCopied) + " " +
                                    getString(R.string.msg_move_success));

                        } else {
                            showMessage(getResources().getQuantityString(R.plurals.number_of_files, filesCopied,
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


    public class BgOperationsTask extends AsyncTask<ArrayList<String>, Void, Integer> {

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
                }
            }
            return deletedCount;
        }

        @Override
        protected void onPostExecute(Integer filesDel) {
            FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R
                    .id.frame_container);
            int deletedFiles = filesDel;
            switch (operation) {

                case DELETE_OPERATION:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {
                        mSelectedItemPositions.clear();
                    }

                    if (deletedFiles != 0) {
                        showMessage(getResources().getQuantityString(R.plurals.number_of_files, deletedFiles,
                                deletedFiles) + " " +
                                getString(R.string.msg_delete_success));
                        if (singlePaneFragment != null) {
                            singlePaneFragment.refreshList();
                        }
                    }

                    if (totalFiles != deletedFiles) {
                        showMessage(getString(R.string.msg_delete_failure));
                    }
                    break;

            }
        }
    }
}

