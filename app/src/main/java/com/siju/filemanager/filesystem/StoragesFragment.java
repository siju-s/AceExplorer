package com.siju.filemanager.filesystem;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.ExpandableListAdapter;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.common.SharedPreferenceWrapper;
import com.siju.filemanager.filesystem.model.FavInfo;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.utils.FileUtils;
import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.siju.filemanager.filesystem.utils.FileUtils.getInternalStorage;

/**
 * Created by SIJU on 20-07-2016.
 */
public class StoragesFragment extends Fragment implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final String ACTION_VIEW_FOLDER_LIST = "folder_list";
    public static final String ACTION_DUAL_VIEW_FOLDER_LIST = "dual_folder_list";
    public static final String ACTION_DUAL_PANEL = "ACTION_DUAL_PANEL";
    public static final String ACTION_VIEW_MODE = "view_mode";

    private String mCurrentDir ;
    private String mCurrentDirDualPane = getInternalStorage().getAbsolutePath();
    public String STORAGE_ROOT, STORAGE_INTERNAL, STORAGE_EXTERNAL, DOWNLOADS, IMAGES, VIDEO,
            MUSIC, DOCS, SETTINGS,
            RATE;
    private boolean mIsDualModeEnabled;
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
    MenuItem mPasteItem, mRenameItem, mInfoItem, mArchiveItem, mFavItem, mExtractItem, mHideItem;
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
    private FloatingActionsMenu fabCreateMenuDual;
    private FloatingActionButton fabCreateFolderDual;
    private FloatingActionButton fabCreateFileDual;
    private ArrayList<SectionItems> favouritesGroupChild = new ArrayList<>();
    public static final String KEY_FAV = "KEY_FAVOURITES";
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private SharedPreferences mSharedPreferences;
    private ArrayList<FavInfo> savedFavourites = new ArrayList<>();
    private View mViewSeperator;
    private int mCategory = FileConstants.CATEGORY.FILES.getValue();
    private LinearLayout mNavigationLayout;
    private ConstraintLayout mMainLayout;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 200;
    private static final int PREFS_REQUEST = 1000;

    private boolean mIsPermissionGranted;
    private Toolbar mToolbar;
    private int mViewMode = FileConstants.KEY_LISTVIEW;
    private boolean mIsFavGroup;
    private FrameLayout frameLayoutFabDual;
    private String mSelectedPath;
    private TextView textPathSelect;
    private final int MENU_FAVOURITES = 1;
    private boolean mIsFirstRun;
    public static final String PREFS_FIRST_RUN = "first_app_run";
    private boolean mIsDualPaneEnabledSettings = true;
    private boolean mIsPasteItemVisible;
    private boolean mIsFabOpen;

    private View root;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.navigation_top, container, false);
        return root;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        initializeViews();
        initListeners();
        checkScreenOrientation();

        if (getArguments() != null) {
            getArguments().getInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            String path = getArguments().getString(FileConstants.KEY_PATH);

            int groupPos = getArguments().getInt(BaseActivity.ACTION_GROUP_POS, -1);
            int childPos = getArguments().getInt(BaseActivity.ACTION_CHILD_POS, -1);
            if (groupPos != -1 && childPos != -1) {
                displaySelectedGroup(groupPos, childPos, path);

            }
        }
//        setNavDirectory();


    }

    private void initializeViews() {

        mToolbar = (Toolbar) root.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
//        mMainLayout = (ConstraintLayout)root. findViewById(R.id.content_base);
        mBottomToolbar = (Toolbar) root.findViewById(R.id.toolbar_bottom);
        mNavigationLayout = (LinearLayout) root.findViewById(R.id.layoutNavigate);
        fabCreateMenu = (FloatingActionsMenu) root.findViewById(R.id.fabCreate);
        fabCreateFolder = (FloatingActionButton) root.findViewById(R.id.fabCreateFolder);
        fabCreateFile = (FloatingActionButton) root.findViewById(R.id.fabCreateFile);
        final FrameLayout frameLayout = (FrameLayout) root.findViewById(R.id.frameLayoutFab);
        frameLayout.getBackground().setAlpha(0);

        fabCreateMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener() {


            @Override
            public void onMenuExpanded() {
                if (fabCreateMenuDual != null) {
                    fabCreateMenuDual.setAlpha(0.10f);
                    fabCreateMenuDual.setEnabled(false);

                }
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
                if (fabCreateMenuDual != null) {
                    fabCreateMenuDual.setAlpha(1.0f);
                    fabCreateMenuDual.setEnabled(true);
                }
                frameLayout.setOnTouchListener(null);
            }
        });


        fabCreateMenuDual = (FloatingActionsMenu) root.findViewById(R.id.fabCreateDual);
        fabCreateFolderDual = (FloatingActionButton) root.findViewById(R.id.fabCreateFolderDual);
        fabCreateFileDual = (FloatingActionButton) root.findViewById(R.id.fabCreateFileDual);

        frameLayoutFabDual = (FrameLayout) root.findViewById(R.id.frameLayoutFabDual);
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


        navDirectory = (LinearLayout) root.findViewById(R.id.navButtons);
        navDirectoryDualPane = (LinearLayout) root.findViewById(R.id.navButtonsDualPane);
        scrollNavigation = (HorizontalScrollView) root.findViewById(R.id.scrollNavigation);
        scrollNavigationDualPane = (HorizontalScrollView) root.findViewById(R.id
                .scrollNavigationDualPane);
        mViewSeperator = root.findViewById(R.id.viewSeperator);

    }

    private void initListeners() {
        fabCreateFile.setOnClickListener(this);
        fabCreateFolder.setOnClickListener(this);
        fabCreateFileDual.setOnClickListener(this);
        fabCreateFolderDual.setOnClickListener(this);

    }

    /**
     * Checks if orientation is landscape when app is run 1st time to enable Dual Panel
     */
    private void checkScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && mIsDualPaneEnabledSettings) {
            mIsDualModeEnabled = true;
            isDualPaneInFocus = true;
       /*     mViewSeperator.setVisibility(View.VISIBLE);
            frameLayoutFabDual.setVisibility(View.VISIBLE);*/
        }
       /* else {
            mIsDualModeEnabled = false;
            mViewSeperator.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
        }*/
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
            ImageView navArrow = new ImageView(getActivity());
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
                        HorizontalScrollView hv = (HorizontalScrollView) root.findViewById(R.id
                                .scrollNavigation);
                        hv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100L);
            } else {
                scrollNavigationDualPane.postDelayed(new Runnable() {
                    public void run() {
                        HorizontalScrollView hv = (HorizontalScrollView) root.findViewById(R.id
                                .scrollNavigationDualPane);
                        hv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100L);
            }
        }
    }

    private void createNavButton(String text, final String dir) {
        int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        final Button button = new Button(getActivity());
        button.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        button.setText(text);
        if (Build.VERSION.SDK_INT < 23) {
            button.setTextAppearance(getActivity(), R.style.NavigationButton);
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
                        getActivity());
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
                                getActivity().getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container);
                        FileListFragment dualPaneFragment = (FileListDualFragment)
                                getActivity().getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container_dual);
                        String fileName = rename.getText().toString() + "";

                        int result;
                        /**
                         * In landscape mode, FabCreateFile is on Dual Pane side and
                         * FabCreateFileDual on Single pane
                         */
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

    public void createFragmentForIntent(Intent intent) {

        mIsFavGroup = false;
        if (intent.getAction() != null) {
            final String action = intent.getAction();
            Fragment targetFragment;
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
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

//            return true;
        }
//        return false;
    }

    public void displaySelectedGroup(int groupPos, int childPos, String path) {
        switch (groupPos) {
            case 0:
            case 1:
                mToolbar.setTitle(getString(R.string.app_name));
                mNavigationLayout.setVisibility(View.VISIBLE);
                toggleDualPaneVisibility(true);
                isCurrentDirRoot = false;
                fabCreateMenu.setVisibility(View.VISIBLE);

                if (groupPos == 1) {
                    mIsFavGroup = true;
                } else {
                    mIsFavGroup = false;
                }

                if (!isDualPaneInFocus) {
                    mStartingDir = path;

                    if (mCurrentDir == null) {
                        mCurrentDir = mStartingDir;
                        singlePaneFragments.clear();
                        setNavDirectory();
                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDir, mCategory);
                    } else if (!mCurrentDir.equals(mStartingDir)) {
                        mCurrentDir = mStartingDir;
                        singlePaneFragments.clear();
                        // For Favourites
                        if (groupPos == 1) {
                            if (mCurrentDir.contains(getInternalStorage().getAbsolutePath())) {
                                mStartingDir = getInternalStorage().getAbsolutePath();
                            } else if (FileUtils.getExternalStorage() != null && mCurrentDir
                                    .contains(FileUtils
                                            .getExternalStorage().getAbsolutePath())) {
                                mStartingDir = getInternalStorage().getAbsolutePath();
                            } else {
                                isCurrentDirRoot = true;
                                mStartingDir = "/";
                            }
                        }

                        setNavDirectory();
                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDir, mCategory);
                    }
                } else {
                    mStartingDirDualPane = path;
                    if (mCurrentDirDualPane == null) {
                        mCurrentDirDualPane = mStartingDirDualPane;
                        dualPaneFragments.clear();
                        setNavDirectory();
                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDirDualPane, mCategory);
                    } else if (!mCurrentDirDualPane.equals(mStartingDirDualPane)) {
                        mCurrentDirDualPane = mStartingDirDualPane;
                        dualPaneFragments.clear();
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
                        setNavDirectory();
                        mCategory = FileConstants.CATEGORY.FILES.getValue();
                        displayInitialFragment(mCurrentDirDualPane, mCategory);
                    }
                }
                break;
            // When Library category item is clicked
            case 2:
                mNavigationLayout.setVisibility(View.GONE);
                toggleDualPaneVisibility(false);
                mCurrentDir = null;
                fabCreateMenu.setVisibility(View.GONE);

                switch (childPos) {
                    // When Audio item is clicked
                    case 0:
                        mToolbar.setTitle(MUSIC);
                        mCategory = FileConstants.CATEGORY.AUDIO.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                    // When Video item is clicked
                    case 1:
                        mToolbar.setTitle(VIDEO);
                        mCategory = FileConstants.CATEGORY.VIDEO.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                    // When Images item is clicked
                    case 2:
                        mToolbar.setTitle(IMAGES);
                        mCategory = FileConstants.CATEGORY.IMAGE.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                    // When Documents item is clicked
                    case 3:
                        mToolbar.setTitle(DOCS);
                        mCategory = FileConstants.CATEGORY.DOCS.getValue();
                        displayInitialFragment(null, mCategory);
                        break;
                }
                break;
        }


    }


    private void displayInitialFragment(String directory, int category) {
        // update the main content by replacing fragments
        // Fragment fragment = null;

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
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
//        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }


    /**
     * Called when app opened 1st time
     */
    private void initialFragmentSetup(String directory, int category) {
        // update the main content by replacing fragments
        // Fragment fragment = null;
//        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        args.putInt(FileConstants.KEY_CATEGORY, category);
        args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);


        if (mIsDualModeEnabled) {
            isDualPaneInFocus = true;
            toggleDualPaneVisibility(true);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//            String internalStoragePath = getInternalStorage().getAbsolutePath();
/*            Bundle args = new Bundle();
            args.putString(FileConstants.KEY_PATH, internalStoragePath);
            args.putBoolean(FileConstants.KEY_DUAL_MODE, true);*/
//            setNavDirectory();
/*            FileListDualFragment dualFragment = new FileListDualFragment();
            dualPaneFragments.add(dualFragment);
            dualFragment.setArguments(args);*/
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
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                fileListFragment, directory).commitAllowingStateLoss();


//        ft.addToBackStack(null);

//        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }

    private void replaceFragment(Fragment fragment, boolean isDualPane) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager()
                .beginTransaction();
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

    }


    /**
     * Dual pane mode to be shown only for File Category
     *
     * @param isFilesCategory
     */
    private void toggleDualPaneVisibility(boolean isFilesCategory) {
        if (isFilesCategory) {
            if (mIsDualModeEnabled) {
                FrameLayout frameLayout = (FrameLayout) root.findViewById(R.id
                        .frame_container_dual);
                frameLayout.setVisibility(View.VISIBLE);
                frameLayoutFabDual.setVisibility(View.VISIBLE);
                mViewSeperator.setVisibility(View.VISIBLE);
                scrollNavigationDualPane.setVisibility(View.VISIBLE);
            }
        } else {
            FrameLayout frameLayout = (FrameLayout) root.findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
            scrollNavigationDualPane.setVisibility(View.GONE);
            isDualPaneInFocus = false;
        }

    }

    /**
     * Create base fragment when Fav item is clicked
     *
     * @param dir
     */
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
    }

    private void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "On Create options Activity=" + mIsPasteItemVisible);
        getActivity().getMenuInflater().inflate(R.menu.base, menu);
        mPasteItem = menu.findItem(R.id.action_paste);
        mPasteItem.setVisible(mIsPasteItemVisible);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_paste:
//           /*     pasteOperationCleanUp();
           /*     if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
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
                break;*/


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("TAG", "On config" + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                mIsDualPaneEnabledSettings) {
            // For Files category only, show dual pane
            if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                isDualPaneInFocus = true;
                mIsDualModeEnabled = true;
                toggleDualPaneVisibility(true);
                FragmentTransaction ft = getChildFragmentManager()
                        .beginTransaction();
                String internalStoragePath = getInternalStorage().getAbsolutePath();
                Bundle args = new Bundle();
                args.putString(FileConstants.KEY_PATH, internalStoragePath);

                args.putString(FileConstants.KEY_PATH_OTHER, mCurrentDir);
                args.putBoolean(FileConstants.KEY_FOCUS_DUAL, true);

                args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
                setNavDirectory();
                FileListDualFragment dualFragment = new FileListDualFragment();
                dualPaneFragments.add(dualFragment);
                dualFragment.setArguments(args);
                ft.replace(R.id.frame_container_dual, dualFragment);
//                mViewSeperator.setVisibility(View.VISIBLE);
                ft.commitAllowingStateLoss();
            }

//            ft.addToBackStack(null);

        } else {
            isDualPaneInFocus = false;
            mIsDualModeEnabled = false;
            dualPaneFragments.clear();
            toggleDualPaneVisibility(false);

        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(FileConstants.PREFS_DUAL_PANE)) {
            mIsDualPaneEnabledSettings = sharedPreferences.getBoolean(FileConstants
                    .PREFS_DUAL_PANE, true);
//            isDualPaneInFocus = false;

            if (!mIsDualPaneEnabledSettings) {
                toggleDualPaneVisibility(false);
            } else {
                if (mCategory == FileConstants.CATEGORY.FILES.getValue() && mIsDualModeEnabled) {
                    isDualPaneInFocus = true;
                    toggleDualPaneVisibility(true);
                    FragmentTransaction ft = getChildFragmentManager()
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
                    ft.commitAllowingStateLoss();
                }
            }

        }
    }

    public interface CreateFileListFragment {
        void createFragmentForIntent(Intent intent);
    }
}
