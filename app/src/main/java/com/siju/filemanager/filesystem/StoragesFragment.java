package com.siju.filemanager.filesystem;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.common.SharedPreferenceWrapper;
import com.siju.filemanager.filesystem.model.FavInfo;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.ui.DialogBrowseFragment;
import com.siju.filemanager.filesystem.ui.EnhancedMenuInflater;
import com.siju.filemanager.filesystem.utils.ExtractManager;
import com.siju.filemanager.filesystem.utils.FileUtils;
import com.siju.filemanager.model.SectionItems;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.siju.filemanager.filesystem.utils.FileUtils.getInternalStorage;


public class StoragesFragment extends Fragment implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        Toolbar.OnMenuItemClickListener,
        DialogBrowseFragment.SelectedPathListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final String ACTION_VIEW_FOLDER_LIST = "folder_list";
    public static final String ACTION_DUAL_VIEW_FOLDER_LIST = "dual_folder_list";
    public static final String ACTION_DUAL_PANEL = "ACTION_DUAL_PANEL";
    public static final String ACTION_VIEW_MODE = "view_mode";

    private String mCurrentDir = getInternalStorage().getAbsolutePath();
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
    private boolean mFromHomeScreen;
    private ArrayList<FileInfo> mLibraryList = new ArrayList<>();
    private ArrayList<FavInfo> mFavList = new ArrayList<>();
    private boolean mIsHomePageAdded;
    private boolean mIsHomePageRemoved;


    private View root;
    private BaseActivity mBaseActivity;




    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.storages_fragment, container, false);
        return root;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        initializeViews();
        initConstants();
        checkScreenOrientation();

        if (getArguments() != null) {
            getArguments().getInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
            String path = getArguments().getString(FileConstants.KEY_PATH);
            mFromHomeScreen = getArguments().getBoolean(FileConstants.KEY_HOME);
            mCategory = getArguments().getInt(FileConstants.KEY_CATEGORY, FileConstants.CATEGORY
                    .FILES.getValue());
            int groupPos = getArguments().getInt(BaseActivity.ACTION_GROUP_POS, -1);
            int childPos = getArguments().getInt(BaseActivity.ACTION_CHILD_POS, -1);
       if (groupPos != -1 && childPos != -1) {
                displaySelectedGroup(groupPos, childPos, path);

            }

            if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                setNavDirectory();
            } else {
                mLibraryList = getArguments().getParcelableArrayList(FileConstants
                        .KEY_LIB_SORTLIST);

                mNavigationLayout.setVisibility(View.GONE);
            }

            initialFragmentSetup(path, mCategory);
        }


    }

    @Override
    public void onPause() {
        Log.d(TAG,"onPause=");

        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume=");
        super.onResume();
    }


    public void setHomeScreenEnabled(boolean value) {
        mIsHomePageAdded = value;
    }

    public void removeHomeScreen(boolean value) {
        mIsHomePageRemoved = value;
    }


    private void initializeViews() {

        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        int singlePaneCount = singlePaneFragments.size();
                        int dualPaneCount = dualPaneFragments.size();
                        Logger.log(TAG, "onBackPressed--SINGLEPANELFRAG count=" + singlePaneCount);
                        Logger.log(TAG, "onBackPressed--DUALPANELFRAG count=" + dualPaneCount);
                        Logger.log(TAG, "onBackPressed--isDualPaneInFocus=" + isDualPaneInFocus);


                        if (mCategory == 0) {
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
                                    if (mIsHomePageAdded) {
                                        ((BaseActivity) getActivity()).initialScreenSetup(true);
                                    } else if (mIsHomePageRemoved) {
                                        getActivity().onBackPressed();
                                        getActivity().finish();
                                    } else if (mFromHomeScreen) {
                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction().remove(StoragesFragment.this)
                                                .commit();
                                        getActivity().onBackPressed();
                                    } else {
                                        getActivity().finish();
                                    }
                                } else {
                                    // Changing the current dir  to 1 up on back press
                                    mCurrentDir = new File(mCurrentDir).getParent();
                                    Log.d(TAG, "onBackPressed--mCurrentDir=" + mCurrentDir);
                                    int childCount = navDirectory.getChildCount();
                                    Log.d(TAG, "onBackPressed--Navbuttons count=" + childCount);
                                    navDirectory.removeViewAt(childCount - 1); // Remove view
                                    navDirectory.removeViewAt(childCount - 2); // Remove > symbol
                                    scrollNavigation.postDelayed(new Runnable() {
                                        public void run() {
                                            HorizontalScrollView hv = (HorizontalScrollView) root
                                                    .findViewById(R.id
                                                            .scrollNavigation);
                                            hv.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                                        }
                                    }, 100L);
                                    Fragment fragment = singlePaneFragments.get(singlePaneCount -
                                            2);

                                    replaceFragment(fragment, isDualPaneInFocus);
                                    singlePaneFragments.remove(singlePaneCount - 1);  // Removing
                                    // the last fragment

                                }

                            } else {
                                if (dualPaneCount == 1) {
                                    if (mIsHomePageAdded) {
                                        ((BaseActivity) getActivity()).initialScreenSetup(true);
                                    } else if (mIsHomePageRemoved) {
                                        getActivity().onBackPressed();
                                        getActivity().finish();
                                    } else if (mFromHomeScreen) {
                                        getActivity().onBackPressed();
                                    } else {
                                        getActivity().finish();
                                    }

//                                    getActivity().getSupportFragmentManager().popBackStack();
                                } else {
                                    mCurrentDirDualPane = new File(mCurrentDirDualPane).getParent();
                                    Log.d(TAG, "onBackPressed--mCurrentDirDual=" +
                                            mCurrentDirDualPane);
                                    int childCount = navDirectoryDualPane.getChildCount();
                                    Log.d(TAG, "onBackPressed--Navbuttonsdualpane childCount=" +
                                            childCount);
                                    navDirectoryDualPane.removeViewAt(childCount - 1); // Remove
                                    // view
                                    navDirectoryDualPane.removeViewAt(childCount - 2); // Remove
                                    // > symbol
                                    scrollNavigationDualPane.postDelayed(new Runnable() {
                                        public void run() {
                                            HorizontalScrollView hv = (HorizontalScrollView) root
                                                    .findViewById(R.id
                                                            .scrollNavigationDualPane);
                                            hv.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                                        }
                                    }, 100L);
                                    Fragment fragment = dualPaneFragments.get(dualPaneCount - 2);
                                    replaceFragment(fragment, isDualPaneInFocus);
                                    dualPaneFragments.remove(dualPaneCount - 1);  // Removing the
                                    // last fragment

                                }
                            }
                        } else {
                            if (mIsHomePageAdded) {
                                ((BaseActivity) getActivity()).initialScreenSetup(true);
                            } else if (mIsHomePageRemoved) {
                                getActivity().onBackPressed();
                                getActivity().finish();
                            } else {
                                getActivity().onBackPressed();
                            }
                        }
                    }
                    return true;
                }

                return false;
            }
        });

        mToolbar = (Toolbar) root.findViewById(R.id.toolbar);
//        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((BaseActivity) getActivity()).toggleToolbarVisibility(true, mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ((BaseActivity) getActivity()).toggleToolbarVisibility(true, mToolbar);

        mToolbar.setNavigationIcon(R.drawable.ic_menu_white);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) getActivity()).toggleDrawer(true);
            }
        });


//        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R
// .drawable.ic_menu_white);

        mMainLayout = (ConstraintLayout) root.findViewById(R.id.content_base);
        mBottomToolbar = (Toolbar) root.findViewById(R.id.toolbar_bottom);
        mNavigationLayout = (LinearLayout) root.findViewById(R.id.layoutNavigate);


        navDirectory = (LinearLayout) root.findViewById(R.id.navButtons);
        navDirectoryDualPane = (LinearLayout) root.findViewById(R.id.navButtonsDualPane);
        scrollNavigation = (HorizontalScrollView) root.findViewById(R.id.scrollNavigation);
        scrollNavigationDualPane = (HorizontalScrollView) root.findViewById(R.id
                .scrollNavigationDualPane);
        mViewSeperator = root.findViewById(R.id.viewSeperator);

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

    @Override
    public void onDestroy() {
        ((BaseActivity) getActivity()).toggleToolbarVisibility(false, null);
        Logger.log(TAG,"On Destroy");
        super.onDestroy();
    }


    private void checkScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && mIsDualPaneEnabledSettings) {
            mIsDualModeEnabled = true;
            isDualPaneInFocus = true;
     mViewSeperator.setVisibility(View.VISIBLE);
            frameLayoutFabDual.setVisibility(View.VISIBLE);

        }
 else {
            mIsDualModeEnabled = false;
            mViewSeperator.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
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
//                fabCreateMenuDual.collapse();
            case R.id.fabCreateFile:
            case R.id.fabCreateFolder:
                if (view.getId() == R.id.fabCreateFolder || view.getId() == R.id.fabCreateFile) {
//                    fabCreateMenu.collapse();
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
                                                .id.main_container);
                        FileListFragment dualPaneFragment = (FileListDualFragment)
                                getActivity().getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container_dual);
                        String fileName = rename.getText().toString() + "";

                        int result;
//*
//                         * In landscape mode, FabCreateFile is on Dual Pane side and
//                         * FabCreateFileDual on Single pane


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
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
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
                transaction.replace(R.id.main_container, targetFragment, mCurrentDir);
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
//                fabCreateMenu.setVisibility(View.VISIBLE);

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
//                        setNavDirectory();
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
//                fabCreateMenu.setVisibility(View.GONE);

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
            ft.replace(R.id.main_container, fileListFragment, directory);
            singlePaneFragments.add(fileListFragment);
        }

//        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
//        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }


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
            FragmentTransaction ft = getChildFragmentManager()
                    .beginTransaction();//getActivity().getSupportFragmentManager().beginTransaction();
//            String internalStoragePath = getInternalStorage().getAbsolutePath();
            Bundle args1 = new Bundle();
            args1.putString(FileConstants.KEY_PATH, null);
            args1.putBoolean(FileConstants.KEY_DUAL_MODE, true);

//            setNavDirectory();
            FileListDualFragment dualFragment = new FileListDualFragment();
            dualPaneFragments.add(dualFragment);
            dualFragment.setArguments(args);

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

        if (category != FileConstants.CATEGORY.FAVORITES.getValue() &&
                category != FileConstants.CATEGORY.FILES.getValue()) {
            args.putParcelableArrayList(FileConstants.KEY_LIB_SORTLIST, mLibraryList);
        }

        fileListFragment.setArguments(args);
        singlePaneFragments.add(fileListFragment);
        FragmentTransaction fragmentTransaction = getChildFragmentManager()
                .beginTransaction();
        fragmentTransaction.replace(R.id.main_container,
                fileListFragment, directory).commit();


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
            fragmentTransaction.replace(R.id.main_container, fileListFragment, path);
        }
        fragmentTransaction.commit();

    }

//
//*
//     * Dual pane mode to be shown only for File Category
//     *
//     * @param isFilesCategory


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
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_paste:
//
//     pasteOperationCleanUp();
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

/*
    public void toggleFabVisibility(boolean flag) {
        if (flag) {
            fabCreateMenu.setVisibility(View.GONE);
        } else {
            // FAB should be visible only for Files Category
            if (mCategory == 0) {
                fabCreateMenu.setVisibility(View.VISIBLE);
            }
        }
    }
*/




    public void setupBottomToolbar() {
        mBottomToolbar.setVisibility(View.VISIBLE);
        mBottomToolbar.startActionMode(new ActionModeCallback());
//        mBottomToolbar.inflateMenu(R.menu.action_mode_bottom);
        mBottomToolbar.getMenu().clear();
        EnhancedMenuInflater.inflate(getActivity().getMenuInflater(), mBottomToolbar.getMenu(),
                true,
                mCategory);
        mBottomToolbar.setOnMenuItemClickListener(this);
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public void togglePasteVisibility(boolean isVisible) {
        mPasteItem.setVisible(isVisible);
        mIsPasteItemVisible = isVisible;
    }

/*
*
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
                    showMessage(mSelectedItemPositions.size() + " " + getString(R.string
                            .msg_cut_copy));
                    mIsMoveOperation = true;
                    togglePasteVisibility(true);
                    getActivity().supportInvalidateOptionsMenu();
                    mActionMode.finish();

                }
                break;
            case R.id.action_copy:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    mIsMoveOperation = false;
                    showMessage(mSelectedItemPositions.size() + " " + getString(R.string
                            .msg_cut_copy));
                    togglePasteVisibility(true);
                    getActivity().supportInvalidateOptionsMenu();
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
                    FileUtils.shareFiles(getActivity(), filesToShare, mCategory);
                    mActionMode.finish();
                }
                break;

            case R.id.action_select_all:
                if (mSelectedItemPositions != null) {
                    FileListFragment singlePaneFragment = (FileListFragment)
                            getChildFragmentManager()
                                    .findFragmentById(R
                                            .id.main_container);
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
    }

    @Override
    public void getSelectedPath(String path) {
        mSelectedPath = path;
        if (textPathSelect != null) {
            textPathSelect.setText(mSelectedPath);
        }

    }

    public void setActionModeTitle(String title) {
        mActionMode.setTitle(title);
    }

    public void setFileList(ArrayList<FileInfo> list) {
        mFileList = list;
    }

    public void setFileListAdapter(FileListAdapter adapter) {
        fileListAdapter = adapter;
    }
/*
*
     * Triggered on long press click on item*//**/


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
            builder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_archive_white), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mArchiveItem.setTitle(builder);

            SpannableStringBuilder favBuilder = new SpannableStringBuilder(" " + "  " + getString
                    (R.string
                            .add_fav));
            // replace "*" with icon
            favBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_favorite_white), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mFavItem.setTitle(favBuilder);

            SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                    getString(R.string
                            .hide));
            // replace "*" with icon
            hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_hide_white), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mHideItem.setTitle(hideBuilder);
        }

        @SuppressLint("NewApi")
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
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
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable
                                        .ic_unhide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mHideItem.setTitle(hideBuilder);
   mHideItem.setTitle(getString(R.string.unhide));
                        mHideItem.setIcon(R.drawable.ic_unhide_white);

                    } else {
                        SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                                "" + getString(R.string
                                .hide));
                        // replace "*" with icon
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable
                                        .ic_hide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mHideItem.setTitle(hideBuilder);
        mHideItem.setTitle(getString(R.string.hide));
                        mHideItem.setIcon(R.drawable.ic_hide_white);

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
                                getActivity());
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
                                        getChildFragmentManager()
                                                .findFragmentById(R
                                                        .id.main_container);
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
                                getChildFragmentManager()
                                        .findFragmentById(R
                                                .id.main_container);
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
            FileListFragment fileListFragment = (FileListFragment) getChildFragmentManager()
                    .findFragmentById(R.id
                            .main_container);
            if (fileListFragment != null) {
                fileListFragment.clearSelection();
                fileListFragment.toggleDummyView(false);
            }

            mActionMode = null;
            mBottomToolbar.setVisibility(View.GONE);
          // FAB should be visible only for Files Category
            if (mCategory == 0) {
//                fabCreateMenu.setVisibility(View.VISIBLE);
            }

        }
    }

    private void hideUnHideFiles(ArrayList<FileInfo> fileInfo) {
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
        FileListFragment singlePaneFragment = (FileListFragment) getChildFragmentManager()
                .findFragmentById(R
                        .id.main_container);
        if (singlePaneFragment != null) {
            singlePaneFragment.refreshList();
        }
    }

    private void showExtractOptions(final String currentFilePath, final String currentDir) {

        final File currentFile = new File(currentFilePath);
        final String currentFileName = currentFilePath.substring(currentFilePath.lastIndexOf("/")
                + 1, currentFilePath.lastIndexOf("."));
        final Dialog dialog = new Dialog(
                getActivity());
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
                FragmentManager fm = getFragmentManager();
                DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
                dialogFragment.show(fm, "Browse Fragment");
            }
        });

        buttonExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioButtonSpecify.isChecked()) {
                    new ExtractManager(StoragesFragment.this)
                            .extract(currentFile, mSelectedPath, currentFileName);
                } else {
                    new ExtractManager(StoragesFragment.this)
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

        sharedPreferenceWrapper.addFavorite(getActivity(), favInfo);
        SectionItems favItem = new SectionItems(name, path, R.drawable.ic_fav_folder, path);
        ((BaseActivity) getActivity()).updateAdapter(favItem);

    }

    private void updateMediaStore(long id, String renamedFilePath) {
        ContentValues values = new ContentValues();
        switch (mCategory) {
            case 1:
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);
//                values.put(MediaStore.Audio.Media.TITLE, title);
                values.put(MediaStore.Audio.Media.DATA, renamedFilePath);
                String audioId = "" + id;
                getActivity().getContentResolver().update(musicUri, values, MediaStore.Audio
                        .Media._ID
                        + "= ?", new String[]{audioId});
                break;

            case 2:
                Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);

//                values.put(MediaStore.Video.Media.TITLE, title);
                values.put(MediaStore.Video.Media.DATA, renamedFilePath);
                String videoId = "" + id;
                getActivity().getContentResolver().update(videoUri, values, MediaStore.Video.Media._ID
                        + "= ?", new String[]{videoId});
                break;

            case 3:
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);
//                values.put(MediaStore.Images.Media.TITLE, title);

                values.put(MediaStore.Images.Media.DATA, renamedFilePath);
                String imageId = "" + id;
                getActivity().getContentResolver().update(imageUri, values, MediaStore.Images.Media._ID
                        + "= ?", new String[]{imageId});
                break;

            case 4:
                Uri filesUri = MediaStore.Files.getContentUri("external");
                values.put(MediaStore.Files.FileColumns.DATA, renamedFilePath);
                String fileId = "" + id;
                getActivity().getContentResolver().update(filesUri, values, MediaStore.Files.FileColumns._ID
                        + "= ?", new String[]{fileId});
                break;

            default:
                break;
        }
    }


    private void showDialog(FileInfo fileInfo) {
        final Dialog dialog = new Dialog(
                getActivity());
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
            Drawable apkIcon = FileUtils.getAppIconForFolder(getActivity(), fileName);
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
                Drawable apkIcon = FileUtils.getAppIcon(getActivity(), path);
                imageFileIcon.setImageDrawable(apkIcon);
            } else {
                imageFileIcon.setImageResource(R.drawable.ic_doc_white);
            }
        }
    }


    private void showDialog(final ArrayList<String> paths) {
        final Dialog deleteDialog = new Dialog(getActivity());
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

    }

    private class BgOperationsTask extends AsyncTask<ArrayList<String>, Void, Integer> {

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
            FileListFragment singlePaneFragment = (FileListFragment) getChildFragmentManager()
                    .findFragmentById(R
                            .id.main_container);
            FileListDualFragment dualPaneFragment = (FileListDualFragment)
                    getChildFragmentManager()
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
        ContentResolver contentResolver = getActivity().getContentResolver();
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
        FileListFragment fileListFragment = (FileListFragment) getChildFragmentManager()
                .findFragmentById(R.id
                        .main_container);
        if (fileListFragment != null) {
            fileListFragment.refreshList();
        }


    }

//    @Override
//    public void startActionMode() {
//        fabCreateMenu.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void endActionMode() {
//        // FAB should be visible only for Files Category
//        if (mCategory == 0) {
//            fabCreateMenu.setVisibility(View.VISIBLE);
//        }
//    }



    public interface CreateFileListFragment {
        void createFragmentForIntent(Intent intent);
    }


    public interface ActionModeCommunicator {
        void startActionModeOperations();

        void endActionMode();
    }
}
