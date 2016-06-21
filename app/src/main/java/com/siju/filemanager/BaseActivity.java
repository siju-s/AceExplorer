package com.siju.filemanager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileInfo;
import com.siju.filemanager.filesystem.FileListAdapter;
import com.siju.filemanager.filesystem.FileListDualFragment;
import com.siju.filemanager.filesystem.FileListFragment;
import com.siju.filemanager.filesystem.FileUtils;
import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;
import com.siju.filemanager.ui.EnhancedMenuInflater;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.siju.filemanager.filesystem.FileUtils.getInternalStorage;


public class BaseActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    String[] listDataHeader;
    List<String> mListHeader;
    HashMap<String, List<String>> listDataChild;
    ArrayList<SectionGroup> storageGroup;
    public static final String ACTION_VIEW_FOLDER_LIST = "folder_list";
    public static final String ACTION_DUAL_VIEW_FOLDER_LIST = "dual_folder_list";
    public static final String ACTION_DUAL_PANEL = "ACTION_DUAL_PANEL";

    private DrawerLayout drawerLayout;
    private RelativeLayout relativeLayoutDrawerPane;
    private String mCurrentDir = getInternalStorage().getAbsolutePath();
    private String mCurrentDirDualPane = getInternalStorage().getAbsolutePath();
    public String STORAGE_ROOT, STORAGE_INTERNAL, STORAGE_EXTERNAL, DOWNLOADS, IMAGES, VIDEO, MUSIC, DOCS;
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
    MenuItem mPasteItem;
    private static final int PASTE_OPERATION = 1;
    private static final int DELETE_OPERATION = 2;
    private static final int ARCHIVE_OPERATION = 3;
    private static final int DECRYPT_OPERATION = 4;
    private boolean mIsMoveOperation = false;
    private ArrayList<FileInfo> mFileList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        STORAGE_ROOT = getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = getResources().getString(R.string.nav_menu_ext_storage);
        DOWNLOADS = getResources().getString(R.string.downloads);
        MUSIC = getResources().getString(R.string.nav_menu_music);
        VIDEO = getResources().getString(R.string.nav_menu_video);
        DOCS = getResources().getString(R.string.nav_menu_docs);
        IMAGES = getResources().getString(R.string.nav_menu_image);


        checkScreenOrientation();
        initViews();
        initListeners();
        Logger.log("TAG", "on create--Activity");
        prepareListData();
        setListAdapter();
        setNavDirectory();
        displayInitialFragment(mCurrentDir);
    }

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mIsDualMode = true;
        } else {
            mIsDualMode = false;
        }
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBottomToolbar = (Toolbar) findViewById(R.id.toolbar_bottom);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
    }

    private void prepareListData() {

        listDataChild = new HashMap<>();
        listDataHeader = getResources().getStringArray(R.array.expand_headers);
        mListHeader = Arrays.asList(listDataHeader);
        storageGroup = new ArrayList<>();
        initializeStorageGroup();
        initializeBookMarksGroup();
        initializeLibraryGroup();
    }

    private void initializeStorageGroup() {
        ArrayList<SectionItems> storageGroupChild = new ArrayList<>();
        File systemDir = FileUtils.getRootDirectory();
        File rootDir = systemDir.getParentFile();
        File internalSD = FileUtils.getInternalStorage();
        File extSD = FileUtils.getExternalStorage();
        storageGroupChild.add(new SectionItems(STORAGE_ROOT, storageSpace(systemDir), R.drawable.ic_root_black,
                FileUtils
                        .getAbsolutePath(rootDir)));
        storageGroupChild.add(new SectionItems(STORAGE_INTERNAL, storageSpace(internalSD), R.drawable
                .ic_storage_black, FileUtils.getAbsolutePath(internalSD)));
        if (extSD != null) {
            storageGroupChild.add(new SectionItems(STORAGE_EXTERNAL, storageSpace(extSD), R.drawable.ic_ext_sd_black,
                    FileUtils.getAbsolutePath(extSD)));
        }
        storageGroup.add(new SectionGroup(mListHeader.get(0), storageGroupChild));
    }

    private void initializeBookMarksGroup() {
        ArrayList<SectionItems> bookmarksGroupChild = new ArrayList<>();
        bookmarksGroupChild.add(new SectionItems(DOWNLOADS, null, R.drawable.ic_download_black, FileUtils
                .getAbsolutePath(FileUtils.getDownloadsDirectory())));
        storageGroup.add(new SectionGroup(mListHeader.get(1), bookmarksGroupChild));
    }

    private void initializeLibraryGroup() {
        ArrayList<SectionItems> libraryGroupChild = new ArrayList<>();
        libraryGroupChild.add(new SectionItems(MUSIC, null, R.drawable.ic_music_black, null));
        libraryGroupChild.add(new SectionItems(VIDEO, null, R.drawable.ic_video_black, null));
        libraryGroupChild.add(new SectionItems(IMAGES, null, R.drawable.ic_photo_black, null));
        libraryGroupChild.add(new SectionItems(DOCS, null, R.drawable.ic_doc_black, null));
        storageGroup.add(new SectionGroup(mListHeader.get(2), libraryGroupChild));
    }


    private String storageSpace(File file) {
        String freePlaceholder = " " + getResources().getString(R.string.msg_free) + " ";
        return FileUtils.getSpaceLeft(this, file) + freePlaceholder + FileUtils.getTotalSpace(this, file);
    }

    private void setListAdapter() {
        expandableListAdapter = new ExpandableListAdapter(this, storageGroup);
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
                    mStartingDir = storageGroup.get(groupPos).getmChildItems().get(childPos).getPath();

                    if (!mCurrentDir.equals(mStartingDir)) {
                        mCurrentDir = mStartingDir;
                        singlePaneFragments.clear();
                        setNavDirectory();
                        displayInitialFragment(mCurrentDir);
                    }
                } else {
                    mStartingDirDualPane = storageGroup.get(groupPos).getmChildItems().get(childPos).getPath();
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
            Logger.log("TAG", "createFragmentForIntent--currentdir="+mCurrentDir);


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
        mPasteItem = menu.findItem(R.id.action_paste);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_paste:
                ArrayList<String> filePaths = new ArrayList<>();
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        if (mSelectedItemPositions.valueAt(i)) {
                            filePaths.add(mFileList.get(mSelectedItemPositions.keyAt(i)).getFilePath());
                            new BackGroundOperationsTask(PASTE_OPERATION).execute(filePaths);
                        }

                    }
                }
                break;
        }
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void setFileList(ArrayList<FileInfo> list) {
        mFileList = list;
    }

    public void startActionMode() {

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
                break;
            case R.id.action_share:
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
            return true;
        }

        @SuppressLint("NewApi")
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rename:

                    mActionMode.finish();
                    return true;

                case R.id.action_info:

                    mActionMode.finish();
                    return true;
                case R.id.action_archive:

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
//            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        } else {
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.GONE);
            scrollNavigationDualPane.setVisibility(View.GONE);
            isDualPaneInFocus = false;
        }
        super.onConfigurationChanged(newConfig);
    }

    private class BackGroundOperationsTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

        private String fileName;
        private String filePath;
        private int copyStatus = -1;
        private ProgressDialog progressDialog;
        private int operation;
        private int progressCount = 0;
        private int filesCopied;

        private BackGroundOperationsTask(int operation) {
            this.operation = operation;
        }

        @Override
        protected void onPreExecute() {
            Context mContext = BaseActivity.this;
            switch (operation) {

                case PASTE_OPERATION:
                    progressDialog = new ProgressDialog(mContext);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    if (mIsMoveOperation) {
                        progressDialog.setMessage(mContext.getString(R.string.msg_cut));
                    } else {
                        progressDialog.setMessage(mContext.getString(R.string.msg_copy));
                    }
                    progressDialog.show();
                    progressDialog.setMax(mSelectedItemPositions.size());
                    progressDialog.setProgress(0);
                    break;

                case DELETE_OPERATION:

                    progressDialog = ProgressDialog.show(mContext, "Please wait",
                            getString(R.string.msg_delete), true, false);
                    break;
            }

        }

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {

            ArrayList<String> paths = params[0];
//            android.os.Debug.waitForDebugger();
            switch (operation) {

                case PASTE_OPERATION:
                    if (paths.size() > 0) {
                        for (int i = 0; i < paths.size(); i++) {
                            copyStatus = FileUtils.copyToDirectory(paths.get(i), mCurrentDir, mIsMoveOperation);
                            if (copyStatus == 0) {
                                filesCopied++;
                            }
                            progressCount++;
                            progressDialog.setProgress((i
                                    / paths.size()));
                        }
                    }
                    break;

            }
            return null;
        }


        @Override
        protected void onPostExecute(final ArrayList<String> file) {
            FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_container);

            switch (operation) {

                case PASTE_OPERATION:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {
                        mSelectedItemPositions.clear();
                    }
                    togglePasteVisibility(false);

                    // On Copy/Move success
                    if (copyStatus == 0) {
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
                    } else {
                        // On Copy/Move failure
                        if (mIsMoveOperation) {
                            showMessage(getString(R.string.msg_move_failure));
                        } else {
                            showMessage(getString(R.string.msg_copy_failure));
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


    }
}

