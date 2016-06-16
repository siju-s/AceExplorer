package com.siju.filemanager;

import android.content.Intent;
import android.content.res.Configuration;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileListDualFragment;
import com.siju.filemanager.filesystem.FileListFragment;
import com.siju.filemanager.filesystem.FileUtils;
import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.siju.filemanager.filesystem.FileUtils.getInternalStorage;


public class BaseActivity extends AppCompatActivity {

    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    String[] listDataHeader;
    List<String> mListHeader;
    HashMap<String, List<String>> listDataChild;
    //    private ArrayList<SectionGroup> sectionGroupArrayList;
    ArrayList<SectionGroup> storageGroup;
    public static final String ACTION_VIEW_FOLDER_LIST = "folder_list";
    public static final String ACTION_DUAL_VIEW_FOLDER_LIST = "dual_folder_list";
    private DrawerLayout drawerLayout;
    private RelativeLayout relativeLayoutDrawerPane;
    private String mCurrentDir = FileUtils.getInternalStorage().getAbsolutePath();
    public String STORAGE_ROOT, STORAGE_INTERNAL, STORAGE_EXTERNAL, DOWNLOADS;
    private boolean mIsDualMode;
    private LinearLayout navDirectory;
    private String mStartingDir = FileUtils.getInternalStorage().getAbsolutePath();
    private HorizontalScrollView scrollNavigation;
    private int navigationLevelSinglePane = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        STORAGE_ROOT = getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = getResources().getString(R.string.nav_menu_ext_storage);
        DOWNLOADS = getResources().getString(R.string.downloads);
        checkScreenOrientation();
        initViews();
        initListeners();
        Logger.log("TAG", "on create--Activity");
        prepareListData();
        setListAdapter();
        setNavDirectory();
        displayView(mCurrentDir);
    }

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
        scrollNavigation = (HorizontalScrollView) findViewById(R.id.scrollNavigation);

    }

    private void initListeners() {
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("TAG", "Group pos-->" + groupPosition + "CHILD POS-->" + childPosition);
                displayView(groupPosition, childPosition);
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
    }

    private void initializeStorageGroup() {
        ArrayList<SectionItems> storageGroupChild = new ArrayList<>();
        File root = FileUtils.getRootDirectory();
        File internalSD = getInternalStorage();
        File extSD = FileUtils.getExternalStorage();
        storageGroupChild.add(new SectionItems(STORAGE_ROOT, storageSpace(root), R.drawable.ic_root_black, FileUtils.getAbsolutePath(root)));
        storageGroupChild.add(new SectionItems(STORAGE_INTERNAL, storageSpace(internalSD), R.drawable.ic_storage_black, FileUtils.getAbsolutePath(internalSD)));
        if (extSD != null) {
            storageGroupChild.add(new SectionItems(STORAGE_EXTERNAL, storageSpace(extSD), R.drawable.ic_ext_sd_black, FileUtils.getAbsolutePath(extSD)));
        }
        storageGroup.add(new SectionGroup(mListHeader.get(0), storageGroupChild));
    }

    private void initializeBookMarksGroup() {
        ArrayList<SectionItems> bookmarksGroupChild = new ArrayList<>();
        bookmarksGroupChild.add(new SectionItems(DOWNLOADS, null, R.drawable.ic_download_black, FileUtils.getAbsolutePath(FileUtils.getDownloadsDirectory())));
        storageGroup.add(new SectionGroup(mListHeader.get(1), bookmarksGroupChild));
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


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean intentHandled = createFragmentForIntent(intent);
//        Log.d(TAG, "On onNewIntent");
    }

    private void displayView(int groupPos, int childPos) {

        switch (groupPos) {
            case 0:
            case 1:
                mCurrentDir = mStartingDir = storageGroup.get(groupPos).getmChildItems().get(childPos).getPath();
                setNavDirectory();
                displayView(mCurrentDir);
                break;

        }

    }

    private void setNavDirectory() {
        String[] parts = mCurrentDir.split("/");

        navDirectory.removeAllViews();
        navigationLevelSinglePane = 0;
        int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

        String dir = "";
        for (int i = 1; i < parts.length; i++) {
            dir += "/" + parts[i];

            if (!dir.contains(mStartingDir)) {
                continue;
            }

            if (dir.equals(FileUtils.getInternalStorage().getAbsolutePath())) {
                createNavButton(STORAGE_INTERNAL, dir);
            } else if (dir.equals("/system")) {
                createNavButton(STORAGE_ROOT, dir);
            } else if (FileUtils.getExternalStorage() != null && dir.equals(FileUtils.getExternalStorage().getAbsolutePath())) {
                createNavButton(STORAGE_EXTERNAL, dir);
            } else {
                ImageView navArrow = new ImageView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT,
                        WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.weight = 1.0f;
                navArrow.setLayoutParams(layoutParams);
                navArrow.setBackgroundResource(R.drawable.ic_more_white);
                navDirectory.addView(navArrow);
                createNavButton(parts[i], dir);

                scrollNavigation.postDelayed(new Runnable() {
                    public void run() {
                        HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
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
        button.setTag(++navigationLevelSinglePane);
        Log.d("TAG", "Button tag=" + navigationLevelSinglePane);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int level = (int) view.getTag();
                Log.d("TAG", "Button tag click=" + level);
                boolean isUpNavigation = checkIfUpNavigation(level);
                if (isUpNavigation) {
                    removeFragments(level);
                } else {
                    // Check If user tries to load the same directory
                    if (!mCurrentDir.equals(dir)) {
                        mCurrentDir = dir;
                        displayView(dir); // TODO Handle root case by passing /
                    }
                }
            }
        });
        navDirectory.addView(button);
    }


    /**
     *
     */
    private void displayView(String directory) {
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
        } else {
            FileListFragment fileListFragment = new FileListFragment();
            fileListFragment.setArguments(args);
            ft.replace(R.id.frame_container, fileListFragment, directory);
        }

        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }

    private void removeFragments(int level) {

        int fragCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = fragCount; i > level; i--) {
            getSupportFragmentManager().popBackStack();
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
//        Log.d(TAG, "createFragmentForIntent");
        if (intent.getAction() != null) {
            final String action = intent.getAction();
            Fragment targetFragment = null;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mCurrentDir = intent.getStringExtra(FileConstants.KEY_PATH);

            if (action.equals(ACTION_VIEW_FOLDER_LIST)) {
                targetFragment = new FileListFragment();
                intent.putExtra(FileConstants.KEY_DUAL_MODE, false);
                transaction.replace(R.id.frame_container, targetFragment, mCurrentDir);
            } else if (action.equals(ACTION_DUAL_VIEW_FOLDER_LIST)) {
                targetFragment = new FileListDualFragment();
                intent.putExtra(FileConstants.KEY_DUAL_MODE, true);
                transaction.replace(R.id.frame_container_dual, targetFragment, mCurrentDir);
            }

            setNavDirectory();
            if (targetFragment != null) {
                targetFragment.setArguments(intent.getExtras());
                transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                transaction.addToBackStack(null);
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
        Log.d("TAG", "Onbackpress--Frag count=" + count);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (navigationLevelSinglePane != 0)
            navigationLevelSinglePane--;

        if (count == 1) {
            finish();
        } else {
            scrollNavigation.postDelayed(new Runnable() {
                public void run() {
                    HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id.scrollNavigation);
                    hv.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                }
            }, 100L);
            super.onBackPressed();
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        /*else {
            super.onBackPressed();

        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("TAG", "On config" + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.VISIBLE);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            String internalStoragePath = getInternalStorage().getAbsolutePath();
            Bundle args = new Bundle();
            args.putString(FileConstants.KEY_PATH, internalStoragePath);
            args.putBoolean(FileConstants.KEY_DUAL_MODE, true);

            FileListDualFragment dualFragment = new FileListDualFragment();
            dualFragment.setArguments(args);
            ft.replace(R.id.frame_container_dual, dualFragment);
//            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        } else {
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.GONE);
        }
        super.onConfigurationChanged(newConfig);
    }
}
