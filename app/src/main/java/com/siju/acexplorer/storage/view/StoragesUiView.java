/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.storage.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.appmanager.AppInfoActivity;
import com.siju.acexplorer.base.view.BaseActivity;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.SharedPreferenceWrapper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.LargeBundleTransfer;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.storage.model.BackStackModel;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.storage.model.backstack.BackStackInfo;
import com.siju.acexplorer.storage.model.backstack.NavigationCallback;
import com.siju.acexplorer.storage.model.backstack.NavigationInfo;
import com.siju.acexplorer.storage.model.operations.OperationProgress;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.modules.picker.view.DialogBrowseFragment;
import com.siju.acexplorer.storage.modules.zip.ZipCommunicator;
import com.siju.acexplorer.storage.modules.zip.ZipViewer;
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager;
import com.siju.acexplorer.storage.view.custom.CustomLayoutManager;
import com.siju.acexplorer.storage.view.custom.DividerItemDecoration;
import com.siju.acexplorer.storage.view.custom.GridItemDecoration;
import com.siju.acexplorer.storage.view.custom.recyclerview.FastScrollRecyclerView;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.ui.peekandpop.PeekAndPop;
import com.siju.acexplorer.utils.ConfigurationHelper;
import com.siju.acexplorer.view.AceActivity;
import com.siju.acexplorer.view.DrawerListener;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.model.FileConstants.ADS;
import static com.siju.acexplorer.model.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.model.groups.Category.ALBUM_DETAIL;
import static com.siju.acexplorer.model.groups.Category.ARTIST_DETAIL;
import static com.siju.acexplorer.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.FOLDER_IMAGES;
import static com.siju.acexplorer.model.groups.Category.FOLDER_VIDEOS;
import static com.siju.acexplorer.model.groups.Category.GENRE_DETAIL;
import static com.siju.acexplorer.model.groups.CategoryHelper.checkIfFileCategory;
import static com.siju.acexplorer.model.groups.CategoryHelper.isSortOrActionModeUnSupported;
import static com.siju.acexplorer.model.groups.CategoryHelper.showLibSpecificNavigation;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.removeBatchMedia;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.removeMedia;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.updateMedia;
import static com.siju.acexplorer.model.helper.SdkHelper.isAtleastNougat;
import static com.siju.acexplorer.model.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.model.helper.UriHelper.grantUriPermission;
import static com.siju.acexplorer.model.helper.ViewHelper.viewFile;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_RELOAD_LIST;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_COUNT;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES_COUNT;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_MEDIA_INDEX_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OLD_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_SHOW_RESULT;
import static com.siju.acexplorer.storage.model.operations.Operations.HIDE;
import static com.siju.acexplorer.view.dialog.DialogHelper.openWith;

/**
 * Created by Siju on 02 September,2017
 */
@SuppressLint("ClickableViewAccessibility")
public class StoragesUiView extends CoordinatorLayout implements View.OnClickListener,
                                                                 NavigationCallback,
                                                                 FileListAdapter.SearchCallback
{

    private final        String  TAG             = this.getClass().getSimpleName();
    private static final int     DIALOG_FRAGMENT = 5000;
    private static final int     SAF_REQUEST     = 2000;
    private static final boolean isRootMode      = true;

    private       ArrayList<FileInfo>     draggedData            = new ArrayList<>();
    private       SparseBooleanArray      mSelectedItemPositions = new SparseBooleanArray();
    private final HashMap<String, Bundle> scrollPosition         = new HashMap<>();

    private Fragment                   fragment;
    private CoordinatorLayout          mMainLayout;
    private FastScrollRecyclerView     fileList;
    private TextView                   mTextEmpty;
    private View                       mItemView;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout         mSwipeRefreshLayout;
    private Button                     buttonPathSelect;
    private DividerItemDecoration      dividerItemDecoration;
    private GridItemDecoration         mGridItemDecoration;
    private AdView                     mAdView;
    private FloatingActionsMenu        fabCreateMenu;
    private FloatingActionButton       fabCreateFolder;
    private FloatingActionButton       fabCreateFile;
    private FloatingActionButton       fabOperation;
    private FrameLayout                frameLayoutFab;
    private SharedPreferences          preferences;

    private StorageBridge       bridge;
    private FileListAdapter     fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private Category            category;
    private NavigationInfo      navigationInfo;
    private BackStackInfo       backStackInfo;
    private Theme               currentTheme;
    private DrawerListener      drawerListener;
    private MenuControls        menuControls;
    private DragHelper          dragHelper;

    private boolean isHomeScreenEnabled;
    private String  filePath;
    private String  currentDir;
    private boolean isActionModeActive;
    private boolean showHidden;
    private boolean mIsDualModeEnabled;
    private boolean isDragStarted;
    private long    mLongPressedTime;
    private int viewMode = ViewMode.LIST;
    private boolean isZipViewer;
    private boolean shouldStopAnimation = true;
    private boolean isPremium           = true;

    private int        gridCols;
    private String     mLastSinglePaneDir;
    private boolean    mInstanceStateExists;
    private int        mCurrentOrientation;
    private String     mSelectedPath;
    private FileInfo   fileInfo;
    private boolean    isHomeClicked;
    private PeekAndPop peekAndPop;
    private long       id;


    public StoragesUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public static StoragesUiView inflate(ViewGroup parent) {
        return (StoragesUiView) LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list,
                                                                                 parent, false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initializeViews();
    }

    private void initializeViews() {
        mMainLayout = findViewById(R.id.main_content);
        fileList = findViewById(R.id.recyclerViewFileList);
        mTextEmpty = findViewById(R.id.textEmpty);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        int colorResIds[] = {R.color.colorPrimaryDark, R.color.colorPrimary, R.color
                .colorPrimaryDark};
        mSwipeRefreshLayout.setColorSchemeResources(colorResIds);
        mSwipeRefreshLayout.setDistanceToTriggerSync(500);
        frameLayoutFab = findViewById(R.id.frameLayoutFab);
        fabCreateMenu = findViewById(R.id.fabCreate);
        fabCreateFolder = findViewById(R.id.fabCreateFolder);
        fabCreateFile = findViewById(R.id.fabCreateFile);
        fabOperation = findViewById(R.id.fabOperation);
    }


    private void setTheme() {
        currentTheme = ((BaseActivity) getActivity()).getCurrentTheme();
        switch (currentTheme) {
            case DARK:
                mMainLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color
                        .dark_background));

                frameLayoutFab.setBackgroundColor(ContextCompat.getColor(getContext(), R.color
                        .dark_overlay));
                break;
        }
        frameLayoutFab.getBackground().setAlpha(0);
    }


    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    AppCompatActivity getActivity() {
        return (AppCompatActivity) fragment.getActivity();
    }

    public void setBridgeRef(StorageBridge bridge) {
        this.bridge = bridge;
    }

    void initialize() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        dragHelper = new DragHelper(getContext(), this);
        setTheme();
        fileList.setOnDragListener(dragHelper.getDragEventListener());
        checkBillingStatus();
        registerReceivers();

        navigationInfo = new NavigationInfo(this, this);
        backStackInfo = new BackStackInfo();

        mCurrentOrientation = ((AceActivity) getActivity()).getConfiguration().orientation;
        getPreferences();
        getArgs();
        menuControls = new MenuControls(getActivity(), this, currentTheme);
        menuControls.setCategory(category);
        menuControls.setCurrentDir(currentDir);
        setupList();
        if (shouldShowPathNavigation()) {
            navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, category);
        } else {
            navigationInfo.addHomeNavButton(isHomeScreenEnabled, category);
        }
        backStackInfo.addToBackStack(currentDir, category);
        refreshList();
        initializeListeners();
        createDualFrag();
    }

    private void checkBillingStatus() {
        BillingStatus billingStatus = bridge.checkBillingStatus();
        isPremium = billingStatus == BillingStatus.PREMIUM;
        switch (billingStatus) {
            case PREMIUM:
                onPremiumVersion();
                break;
            case UNSUPPORTED:
            case FREE:
                onFreeVersion();
                break;
        }
    }


    private boolean hasStoragePermission() {
        return PermissionUtils.hasStoragePermission();
    }

    private void onFreeVersion() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        showAds();
    }

    private void onPremiumVersion() {
        hideAds();
    }

    private BroadcastReceiver adsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(ADS)) {
                isPremium = intent.getBooleanExtra(FileConstants.KEY_PREMIUM, false);
                if (isPremium) {
                    onPremiumVersion();
                } else {
                    onFreeVersion();
                }
            }
        }
    };


    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter(ADS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(adsReceiver, intentFilter);
        if (!mInstanceStateExists) {
            IntentFilter filter = new IntentFilter(ACTION_RELOAD_LIST);
            filter.addAction(ACTION_OP_REFRESH);
            getActivity().registerReceiver(mReloadListReceiver, filter);
        }
    }


    private void hideAds() {
        LinearLayout adviewLayout = findViewById(R.id.adviewLayout);
        if (adviewLayout.getChildCount() != 0) {
            adviewLayout.removeView(mAdView);
        }
    }

    private void showAds() {
        // DYNAMICALLY CREATE AD START
        LinearLayout adviewLayout = findViewById(R.id.adviewLayout);
        // Create an ad.
        if (mAdView == null) {
            mAdView = new AdView(AceApplication.getAppContext());
            mAdView.setAdSize(AdSize.BANNER);
            mAdView.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));
            // DYNAMICALLY CREATE AD END
            AdRequest adRequest = new AdRequest.Builder().build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
            // Add the AdView to the view hierarchy. The view will have no size until the ad is
            // loaded.
            adviewLayout.addView(mAdView);
        } else {
            ((LinearLayout) mAdView.getParent()).removeAllViews();
            adviewLayout.addView(mAdView);
            // Reload Ad if necessary.  Loaded ads are lost when the activity is paused.
            if (!mAdView.isLoading()) {
                AdRequest adRequest = new AdRequest.Builder().build();
                // Start loading the ad in the background.
                mAdView.loadAd(adRequest);
            }
        }
        // DYNAMICALLY CREATE AD END
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeListeners() {

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                removeScrolledPos(currentDir);
                refreshList();
            }
        });

        setupFab();

        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION) {
                    return;
                }
                switch (view.getId()) {
                    case R.id.imagePeekView:
                    case R.id.autoPlayView:
                    case R.id.imageIcon:
                        if (isActionModeActive() && !menuControls.isPasteOp()) {
                            itemClickActionMode(position, false);
                            return;
                        }
                        handleItemClick(position);
                        break;
                    case R.id.imageButtonInfo:
                        menuControls.showInfoDialog(fileInfoList.get(position), category);
                        break;
                    case R.id.imageButtonShare:
                        ArrayList<FileInfo> files = new ArrayList<>();
                        files.add(fileInfoList.get(position));
                        menuControls.shareFiles(files, category);
                        break;
                    default:
                        if (isActionModeActive() && !menuControls.isPasteOp()) {
                            itemClickActionMode(position, false);
                        } else {
                            handleItemClick(position);
                        }
                        break;
                }

            }

            @Override
            public boolean canShowPeek() {
                return !isActionModeActive();
            }
        });
        fileListAdapter.setOnItemLongClickListener(new FileListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Logger.log(TAG, "On long click" + isDragStarted);
                if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION ||
                        isSortOrActionModeUnSupported(category)) {
                    return;
                }

                if (!isZipViewer && !menuControls.isPasteOp()) {
                    itemClickActionMode(position, true);
                    mLongPressedTime = System.currentTimeMillis();

                    if (isActionModeActive && fileListAdapter.getSelectedCount() >= 1) {
                        mSwipeRefreshLayout.setEnabled(false);
                        mItemView = view;
                        isDragStarted = true;
                    }
                }
            }
        });


        fileList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        if (shouldStopAnimation) {
                            stopAnimation();
                            shouldStopAnimation = false;
                        }
                        break;
                }
            }
        });


        fileList.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                int event = motionEvent.getActionMasked();
                if (shouldStopAnimation) {
                    stopAnimation();
                    shouldStopAnimation = false;
                }

                if (!isDragStarted) {
                    return false;
                }

                if (event == MotionEvent.ACTION_UP || event == MotionEvent.ACTION_CANCEL) {
                    isDragStarted = false;
                    mLongPressedTime = 0;
                } else if (event == MotionEvent.ACTION_MOVE && mLongPressedTime !=
                        0) {
                    long timeElapsed = System.currentTimeMillis() - mLongPressedTime;

                    if (timeElapsed > 1500) {
                        mLongPressedTime = 0;
                        isDragStarted = false;
//                        Logger.log(TAG, "On touch drag path size=" + draggedData.size());
                        if (draggedData.size() > 0) {
                            Intent intent = new Intent();
                            intent.putParcelableArrayListExtra(FileConstants.KEY_PATH, draggedData);
                            intent.putExtra(KEY_CATEGORY, category.getValue());
                            ClipData data = ClipData.newIntent("", intent);
                            int count = fileListAdapter.getSelectedCount();
                            View.DragShadowBuilder shadowBuilder = dragHelper.getDragShadowBuilder
                                    (mItemView, count);
                            if (isAtleastNougat()) {
                                view.startDragAndDrop(data, shadowBuilder, draggedData, 0);
                            } else {
                                view.startDrag(data, shadowBuilder, draggedData, 0);
                            }
                        }
                    }
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction()
                        == MotionEvent.ACTION_CANCEL) {
                    view.performClick();
                }
                return false;
            }
        });
    }

    private void setupFab() {
        fabCreateFile.setOnClickListener(this);
        fabCreateFolder.setOnClickListener(this);
        fabOperation.setOnClickListener(this);

        fabCreateMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener()
        {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onMenuExpanded() {
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
                frameLayoutFab.setOnTouchListener(null);
            }
        });
    }

    /**
     * Show dual pane in Landscape mode
     */
    public void showDualPane() {
        // For Files category only, show dual pane
        mIsDualModeEnabled = true;
        refreshSpan(((AceActivity) getActivity()).getConfiguration());
    }

    private void getPreferences() {
        Bundle bundle = bridge.getUserPrefs();
        gridCols = bundle.getInt(FileConstants.KEY_GRID_COLUMNS, 0);
        isHomeScreenEnabled = bundle.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        showHidden = bundle.getBoolean(FileConstants.PREFS_HIDDEN, false);
        viewMode = bundle.getInt(FileConstants.PREFS_VIEW_MODE, ViewMode.LIST);
    }

    private void getArgs() {
        if (getArguments() != null) {
            currentDir = getArguments().getString(FileConstants.KEY_PATH);
            category = (Category) getArguments().getSerializable(KEY_CATEGORY);
            isZipViewer = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
            mIsDualModeEnabled = getArguments().getBoolean(FileConstants.KEY_DUAL_ENABLED, false);

            if (checkIfLibraryCategory(category)) {
                hideFab();
            } else {
                showFab();
            }
            navigationInfo.showNavigationView();
            if (shouldShowPathNavigation()) {
                navigationInfo.setInitialDir(currentDir);
            }
            mLastSinglePaneDir = currentDir;
        }
    }

    private Bundle getArguments() {
        return fragment.getArguments();
    }

    private void createDualFrag() {
        if (mIsDualModeEnabled && fragment instanceof FileList) {
            bridge.showDualFrame();
            showDualPane();
        }
    }

    private boolean checkIfLibraryCategory(Category category) {
        return !category.equals(FILES) && !category.equals(DOWNLOADS);
    }

    private void showFab() {
        frameLayoutFab.setVisibility(View.VISIBLE);
    }

    private void hideFab() {
        frameLayoutFab.setVisibility(View.GONE);
    }


    private void setupList() {
        fileList.setHasFixedSize(true);
        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            fileList.setLayoutManager(layoutManager);
        } else {
            refreshSpan(((AceActivity) getActivity()).getConfiguration());
        }
        fileList.setItemAnimator(new DefaultItemAnimator());
        peekAndPop = new PeekAndPop.Builder(getActivity()).peekLayout(R.layout.peek_pop).
                parentViewGroupToDisallowTouchEvents(fileList).build();
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList,
                                              category, viewMode, peekAndPop);
        fileListAdapter.setSearchCallback(this);
    }

    public void refreshList() {
        Logger.log(TAG, "refreshList");
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        if (!hasStoragePermission()) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(true);
        if (isZipMode()) {
            zipViewer.loadData();
        } else {
            bridge.loadData(currentDir, category, id);
        }
    }


    @Override
    public void updateList(ArrayList<FileInfo> fileInfoArrayList) {
        this.fileInfoList = fileInfoArrayList;
    }


    private String extension;
    private String bucketName;

    private void handleItemClick(int position) {
        Log.d(TAG, "handleItemClick: " + category);
        bucketName = null;
        switch (category) {
            case AUDIO:
            case VIDEO:
            case IMAGE:
            case DOCS:
            case ALARMS:
            case NOTIFICATIONS:
            case PODCASTS:
            case RINGTONES:
            case ALBUM_DETAIL:
            case ARTIST_DETAIL:
            case GENRE_DETAIL:
            case FOLDER_IMAGES:
            case FOLDER_VIDEOS:
            case ALL_TRACKS:
            case RECENT:
            case GIF:
                this.extension = fileInfoList.get(position).getExtension().toLowerCase();
                viewFile(getContext(), fileInfoList.get(position).getFilePath(),
                         extension, alertDialogListener);
                break;
            case FILES:
            case DOWNLOADS:
            case COMPRESSED:
            case FAVORITES:
            case PDF:
            case APPS:
            case LARGE_FILES:
            case TRASH:
                genericFileItemClick(position);
                break;
            case GENERIC_MUSIC:
                category = fileInfoList.get(position).getSubcategory();
                reloadList(null, category);
                break;
            case ALBUMS:
                category = ALBUM_DETAIL;
                id = fileInfoList.get(position).getId();
                bucketName = fileInfoList.get(position).getTitle();
                reloadList(null, category);
                break;
            case ARTISTS:
                category = ARTIST_DETAIL;
                id = fileInfoList.get(position).getId();
                bucketName = fileInfoList.get(position).getTitle();
                reloadList(null, category);
                break;
            case GENRES:
                category = GENRE_DETAIL;
                id = fileInfoList.get(position).getId();
                bucketName = fileInfoList.get(position).getTitle();
                reloadList(null, category);
                break;
            case GENERIC_IMAGES:
                category = FOLDER_IMAGES;
                id = fileInfoList.get(position).getBucketId();
                bucketName = fileInfoList.get(position).getFileName();
                reloadList(null, category);
                break;
            case GENERIC_VIDEOS:
                category = FOLDER_VIDEOS;
                id = fileInfoList.get(position).getBucketId();
                bucketName = fileInfoList.get(position).getFileName();
                reloadList(null, category);
                break;
            case APP_MANAGER:
                AppInfoActivity.openAppInfo(getContext(), fileInfoList.get(position).getFilePath());
                break;
        }
    }

    private void genericFileItemClick(int position) {
        if (fileInfoList.get(position).isDirectory()) {
            onDirectoryClicked(position);
        } else {
            onFileClicked(position);
        }
    }

    private void onDirectoryClicked(int position) {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);

        if (isZipMode()) {
            zipViewer.onDirectoryClicked(position);
        } else {
            calculateScroll(currentDir);
            String path = fileInfoList.get(position).getFilePath();
            category = FILES;
            reloadList(path, category);
        }
    }

    private void onFileClicked(int position) {
        String filePath = fileInfoList.get(position).getFilePath();
        extension = fileInfoList.get(position).getExtension().toLowerCase();

        if (!isZipMode() && isZipViewable(filePath)) {
            openZipViewer(filePath);
        } else {
            if (isZipMode()) {
                zipViewer.onFileClicked(position);
            } else {
                this.filePath = filePath;
                viewFile(getContext(), filePath, extension, alertDialogListener);
            }
        }
    }


    private boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith(".zip");
    }


    public void setPremium() {
        isPremium = true;
        hideAds();
    }


    public void onPause() {
        pauseAds();
        pauseAutoPlayVid();
    }

    private void pauseAutoPlayVid() {
        if (isPeekMode()) {
            fileListAdapter.stopAutoPlayVid();
        }
    }


    public void onResume() {
        resumeAds();
    }


    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        Logger.log(TAG, "OnActivityREsult==" + resultCode);
        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    mSelectedPath = intent.getStringExtra("PATH");
                    if (buttonPathSelect != null) {
                        buttonPathSelect.setText(mSelectedPath);
                    }
                }
                break;
            case SAF_REQUEST:
                String uriString = preferences.getString(FileConstants.SAF_URI, null);
                Uri oldUri = uriString != null ? Uri.parse(uriString) : null;

                if (resultCode == Activity.RESULT_OK) {
                    Analytics.getLogger().SAFResult(true);
                    Uri treeUri = intent.getData();
                    bridge.handleSAFResult(operationIntent, treeUri, isRooted(), intent.getFlags());

                } else {
                    Analytics.getLogger().SAFResult(false);
                    // If not confirmed SAF, or if still not writable, then revert settings.
                    if (oldUri != null) {
                        bridge.saveOldSAFUri(oldUri.toString());
                    }

                    Toast.makeText(getContext(), getResources().getString(R.string
                                                                                  .access_denied_external),
                                   Toast.LENGTH_LONG).show();
                }
        }
    }

    private void addItemDecoration() {

        switch (viewMode) {
            case ViewMode.LIST:
                if (dividerItemDecoration == null) {
                    dividerItemDecoration = new DividerItemDecoration(getActivity(), currentTheme);
                } else {
                    fileList.removeItemDecoration(dividerItemDecoration);
                }
                fileList.addItemDecoration(dividerItemDecoration);
                break;
            case ViewMode.GRID:
                if (mGridItemDecoration == null) {
                    mGridItemDecoration = new GridItemDecoration(getContext(), currentTheme,
                                                                 gridCols);
                } else {
                    fileList.removeItemDecoration(mGridItemDecoration);
                }
                fileList.addItemDecoration(mGridItemDecoration);
                break;
        }
    }


    private ZipViewer zipViewer;

    private void openZipViewer(String path) {
        String extension = path.substring(path.lastIndexOf("."), path.length());
        Analytics.getLogger().zipViewer(extension);
        calculateScroll(currentDir);
        isZipViewer = true;
        zipViewer = new ZipViewer(zipCommunicator, fragment, path);
        refreshList();
    }


    private final BroadcastReceiver mReloadListReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(ACTION_RELOAD_LIST)) {
                calculateScroll(currentDir);
                String path = intent.getStringExtra(KEY_FILEPATH);
                Logger.log(TAG, "New zip PAth=" + path);
                if (path != null) {
                    scanFile(AceApplication.getAppContext(), path);
                }
                refreshList();
            } else if (action.equals(ACTION_OP_REFRESH)) {

                Bundle bundle = intent.getExtras();
                Operations operation = null;
                if (bundle != null) {
                    operation = (Operations) bundle.getSerializable(KEY_OPERATION);
                }
                if (operation != null) {
                    onOperationResult(intent, operation);
                }
            }
        }
    };

    private void deleteFromMediaStore(final List<String> filesToMediaIndex) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                if (filesToMediaIndex.size() == 0) {
                    return;
                }
                Logger.log(TAG, "deleteFromMediaStore: " + filesToMediaIndex.size());
                String mediaScanningPath = new File(filesToMediaIndex.get(0)).getParent();
                addToMediaScanning(mediaScanningPath);
                Logger.log(TAG, "run CUT: mediaScanningPath" + mediaScanningPath + "size:" + mediaScanningPaths.size());
                removeBatchMedia(AceApplication.getAppContext(), filesToMediaIndex, null);
                removeFromMediaScanning(mediaScanningPath);
            }
        }).start();
    }

    private boolean isMediaScanning;
    private Set<String> mediaScanningPaths = new HashSet<>();

    private void onOperationResult(Intent intent, Operations operation) {
        Logger.log(TAG, "onOperationResult: " + operation);
        int count = intent.getIntExtra(KEY_FILES_COUNT, 0);

        switch (operation) {
            case DELETE:
                boolean isLargeBundle = false;
                List<FileInfo> deletedFilesList;
                List<String> filesToMediaIndex;

                deletedFilesList = intent.getParcelableArrayListExtra
                        (KEY_FILES);
                filesToMediaIndex = intent.getStringArrayListExtra(KEY_MEDIA_INDEX_FILES);
                if (deletedFilesList == null) {
                    deletedFilesList = LargeBundleTransfer.getFileData(AceApplication.getAppContext());
                    isLargeBundle = true;
                }
                if (filesToMediaIndex == null) {
                    filesToMediaIndex = LargeBundleTransfer.getStringData(AceApplication.getAppContext());
                }


//                for (FileInfo info : deletedFilesList) {
//                    scanMultipleFiles(getActivity().getApplicationContext(), info.getFilePath());
//                }
                int totalFiles = intent.getIntExtra(KEY_COUNT, 0);

                int deletedCount = deletedFilesList.size();
                if (intent.getBooleanExtra(KEY_SHOW_RESULT, false)) {

                    if (deletedCount != 0) {
                        FileUtils.showMessage(getContext(), getResources().getQuantityString(R.
                                                                                                     plurals.number_of_files, deletedCount, deletedCount) + " " +
                                getResources().getString(R.string.msg_delete_success));
                    }

                    if (totalFiles != deletedCount) {
                        FileUtils.showMessage(getContext(), getResources().getString(R.string.msg_delete_failure));
                    }
                }
                fileInfoList.removeAll(deletedFilesList);
                removeFavorite(deletedFilesList);
                fileListAdapter.setStopAnimation(true);
                fileListAdapter.updateAdapter(fileInfoList);
                deleteFromMediaStore(filesToMediaIndex);
                if (isLargeBundle) {
                    LargeBundleTransfer.removeFileData(AceApplication.getAppContext());
                    LargeBundleTransfer.removeStringData(AceApplication.getAppContext());
                }
                break;

            case RENAME:
            case HIDE:
                dismissDialog();
                String oldFile = intent.getStringExtra(KEY_FILEPATH);
                final String newFile = intent.getStringExtra(KEY_FILEPATH2);
                if (operation.equals(HIDE)) {
                    fileInfo = intent.getParcelableExtra(KEY_OLD_FILES);
                }
                if (fileInfo == null) {
                    return;
                }
                int position = fileInfoList.indexOf(fileInfo);
                if (position == -1) {
                    return;
                }
                final Category category = fileInfoList.get(position).getCategory();
                if (!oldFile.equals(fileInfoList.get(position).getFilePath())) {
                    return;
                }

                removeMedia(AceApplication.getAppContext(), oldFile, category.getValue());
//                Log.d(TAG, "onOperationResult: NewUri:"+insertUri);
                scanFile(AceApplication.getAppContext(), newFile);

                fileListAdapter.setStopAnimation(true);
                Logger.log(TAG, "Position changed=" + position);
                if (!showHidden && new File(newFile).getName().startsWith(".")) {
                    fileInfoList.remove(position);
                    fileListAdapter.setList(fileInfoList);
                    fileListAdapter.notifyItemRemoved(position);
                } else {
                    fileInfoList.get(position).setFilePath(newFile);
                    fileInfoList.get(position).setFileName(new File(newFile).getName());
                    fileListAdapter.notifyItemChanged(position);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMedia(AceApplication.getAppContext(), newFile, category.getValue());
                    }
                }, 1000); // Intentional delay to let mediascanner index file first and then lets change the title
                break;
            case CUT:
                if (count > 0 && getContext() != null) {
                    Toast.makeText(getContext(), String.format(Locale.getDefault(), getContext().
                            getString(R.string.moved), count), Toast.LENGTH_SHORT).show();
                    refreshList();
                }
                final ArrayList<String> oldFileList = intent.getStringArrayListExtra(KEY_OLD_FILES);
                deleteFromMediaStore(oldFileList);
                break;
            case COPY:
                if (count > 0 && getContext() != null) {
                    Toast.makeText(getContext(), String.format(Locale.getDefault(), getContext().
                            getString(R.string.copied), count), Toast.LENGTH_SHORT).show();
                    refreshList();
                }
                break;
            case FOLDER_CREATION:
            case FILE_CREATION:
                dismissDialog();
                boolean isSuccess = intent.getBooleanExtra(KEY_RESULT, true);

                if (!isSuccess) {
                    Toast.makeText(getActivity(), getContext().getString(R.string.msg_operation_failed), Toast
                            .LENGTH_LONG).show();
                } else {
                    computeScroll();
                    refreshList();
                }
                break;

        }
    }

    private boolean isPeekMode() {
        return peekAndPop.getPeekView().isShown();
    }

    private void endPeekMode() {
        fileListAdapter.stopAutoPlayVid();
    }

    boolean isTrashEnabled() {
        return false;
//        return isTrashEnabled; TODO COmmented this for future use
    }

    /**
     * @return false to avoid call to super.onBackPressed()
     */
    public boolean onBackPressed() {

        if (isPeekMode()) {
            endPeekMode();
        } else if (menuControls.isSearch()) {
            menuControls.endSearch();
            return false;
        } else if (isZipMode()) {
            if (isHomeClicked) {
                zipViewer.onBackPressed(null);
                return true;
            } else {
                zipViewer.onBackPressed();
            }
        } else if (isActionModeActive() && !menuControls.isPasteOp()) {
            menuControls.endActionMode();
        } else {
            return backOperation();
        }

        return false;
    }

    private boolean backOperation() {

        if (checkIfBackStackExists()) {
            removeScrolledPos(currentDir);
            backStackInfo.removeEntryAtIndex(backStackInfo.getBackStack().size() - 1);

            String currentDir = backStackInfo.getDirAtPosition(backStackInfo.getBackStack().size
                    () - 1);
            Category currentCategory = backStackInfo.getCategoryAtPosition(backStackInfo
                                                                                   .getBackStack
                                                                                           ()
                                                                                   .size() - 1);
            category = currentCategory;
            this.currentDir = currentDir;
            menuControls.setCategory(category);
            menuControls.setCurrentDir(currentDir);
            menuControls.setupSortVisibility();
            bucketName = null;
            Log.d(TAG, "backOperation: category:" + category + "bucketName:" + bucketName);

            if (checkIfFileCategory(currentCategory)) {
//                navigationInfo.setInitialDir();
                if (shouldShowPathNavigation()) {
                    navigationInfo.setInitialDir(currentDir);
                    navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled,
                                                   currentCategory);
                } else {
                    navigationInfo.addHomeNavButton(isHomeScreenEnabled, currentCategory);
                }
            } else if (showLibSpecificNavigation(currentCategory)) {
                navigationInfo.addLibSpecificNavButtons(isHomeScreenEnabled, category, bucketName);
            } else {
                navigationInfo.addHomeNavButton(isHomeScreenEnabled, currentCategory);
                hideFab();
            }
            refreshList();
//            menuControls.setTitleForCategory(currentCategory);
            if (currentCategory.equals(FILES)) {
                showFab();
            }

            return false;
        } else {
            removeDualFileFragment();
            if (!isHomeScreenEnabled) {
                getActivity().finish();
            }
            return true;
        }
    }

    private boolean checkIfBackStackExists() {
        int backStackSize = backStackInfo.getBackStack().size();
        Logger.log(TAG, "checkIfBackStackExists --size=" + backStackSize + "homeCLicked:" + isHomeClicked);

        if (isHomeClicked) {
            return false;
        }

        if (backStackSize == 1) {
            backStackInfo.clearBackStack();
            return false;
        } else if (backStackSize > 1) {
            return true;
        }
        return false;
    }


    private boolean shouldShowPathNavigation() {
        return category.equals(FILES) || category.equals(DOWNLOADS);
    }


    public boolean isFabExpanded() {
        return fabCreateMenu.isExpanded();
    }

    public void collapseFab() {
        fabCreateMenu.collapse();
    }


    /**
     * Called from {@link #onBackPressed()} . Does the following:
     * 1. If homescreen enabled, returns to home screen
     * 2. If homescreen disabled, exits the app
     */
    private void removeDualFileFragment() {

        Fragment dualFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

        backStackInfo.clearBackStack();
        Logger.log(TAG, "RemoveFragmentFromBackStack--dualFragment=" + dualFragment);

        if (dualFragment != null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim
                    .enter_from_right, R.anim
                                           .exit_to_left);
            ft.remove(dualFragment);
            ft.commitAllowingStateLoss();
        }
    }

    private void resumeAds() {
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    private void pauseAds() {
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabCreateFile:
                Analytics.getLogger().operationClicked(Analytics.Logger.EV_FAB);
                showCreateFileDialog();
                fabCreateMenu.collapse();
                break;
            case R.id.fabCreateFolder:
                Analytics.getLogger().operationClicked(Analytics.Logger.EV_FAB);
                showCreateDirDialog();
                fabCreateMenu.collapse();
                break;

        }
    }


    private void toggleDragData(FileInfo fileInfo) {
        if (draggedData.contains(fileInfo)) {
            draggedData.remove(fileInfo);
        } else {
            draggedData.add(fileInfo);
        }
    }

    private void itemClickActionMode(int position, boolean isLongPress) {
        fileListAdapter.toggleSelection(position, isLongPress);

        boolean hasCheckedItems = fileListAdapter.getSelectedCount() > 0;
        if (hasCheckedItems && !isActionModeActive) {
            // there are some selected items, start the actionMode
            startActionMode();
        } else if (!hasCheckedItems && isActionModeActive) {
            // there no selected items, finish the actionMode
            menuControls.endActionMode();
        }
        if (isActionModeActive) {
            FileInfo fileInfo = fileInfoList.get(position);
            toggleDragData(fileInfo);
            SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
            setSelectedItemPos(checkedItemPos);
            menuControls.setToolbarText(String.valueOf(fileListAdapter
                                                               .getSelectedCount()));
        }
    }

    private void calculateScroll(String currentDir) {
        View vi = fileList.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int position;
        if (viewMode == ViewMode.LIST) {
            position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else {
            position = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        Bundle bundle = new Bundle();
        bundle.putInt(FileConstants.KEY_POSITION, position);
        bundle.putInt(FileConstants.KEY_OFFSET, top);

        putScrolledPosition(currentDir, bundle);
    }

    private void setSelectedItemPos(SparseBooleanArray selectedItemPos) {
        mSelectedItemPositions = selectedItemPos;
        menuControls.setupMenuVisibility(selectedItemPos);
    }

    private void toggleSelectAll(boolean selectAll) {
        fileListAdapter.clearSelection();
        for (int i = 0; i < fileListAdapter.getItemCount(); i++) {
            fileListAdapter.toggleSelectAll(i, selectAll);
        }
        SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
        setSelectedItemPos(checkedItemPos);
        int count = fileListAdapter.getSelectedCount();
        if (count == 0) {
            menuControls.endActionMode();
        } else {
            menuControls.setToolbarText(String.valueOf(fileListAdapter.getSelectedCount()) + " " +
                                                getResources().getString(R.string.selected));
            fileListAdapter.notifyDataSetChanged();
        }
    }

    void clearSelection() {
        fileListAdapter.removeSelection();
    }


    public void reloadList(String path, Category category) {
        currentDir = path;
        this.category = category;
        if (shouldShowPathNavigation()) {
            navigationInfo.setInitialDir(path);
        }
        if (checkIfLibraryCategory(category)) {
            hideFab();
        } else {
            showFab();
        }
        menuControls.setCategory(category);
        menuControls.setCurrentDir(currentDir);
        menuControls.setupSortVisibility();
        if (shouldShowPathNavigation()) {
            navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, category);
        } else if (showLibSpecificNavigation(category)) {
            navigationInfo.addLibSpecificNavButtons(isHomeScreenEnabled, category, bucketName);
        } else {
            navigationInfo.addHomeNavButton(isHomeScreenEnabled, category);
        }
        backStackInfo.addToBackStack(path, category);
        if (isActionModeActive() && (checkIfLibraryCategory(category) ||
                !menuControls.isPasteOp())) {
            menuControls.endActionMode();
        }
        refreshList();
    }


    private void stopAnimation() {
        if (!fileListAdapter.mStopAnimation) {
            for (int i = 0; i < fileList.getChildCount(); i++) {
                View view = fileList.getChildAt(i);
                if (view != null) {
                    view.clearAnimation();
                }
            }
        }
        fileListAdapter.mStopAnimation = true;
    }


    private boolean isZipMode() {
        return isZipViewer;
    }

    void onDataLoaded(ArrayList<FileInfo> data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (data != null) {

            shouldStopAnimation = true;
            fileInfoList = data;
            fileListAdapter.setCategory(category);
            fileList.setAdapter(fileListAdapter);
            fileListAdapter.updateAdapter(fileInfoList);

            addItemDecoration();

            if (!data.isEmpty()) {
                getScrolledPosition();
                fileList.stopScroll();
                mTextEmpty.setVisibility(View.GONE);
            } else {
                mTextEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    List<FileInfo> getFileList() {
        return fileInfoList;
    }

    private void getScrolledPosition() {
        if (currentDir != null && scrollPosition.containsKey(currentDir)) {
            Bundle b = scrollPosition.get(currentDir);
            if (viewMode == ViewMode.LIST) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(b.getInt
                        (FileConstants
                                 .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
            } else {
                ((GridLayoutManager) layoutManager).scrollToPositionWithOffset(b.getInt
                        (FileConstants
                                 .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
            }
        }
    }

    private void putScrolledPosition(String path, Bundle position) {
        scrollPosition.put(path, position);
    }

    private void removeScrolledPos(String path) {
        if (path == null) {
            return;
        }
        scrollPosition.remove(path);
    }


    int getViewMode() {
        return viewMode;
    }

    void passViewMode() {
        if (mIsDualModeEnabled) {
            ((AceActivity) getActivity()).switchView(viewMode, !(fragment instanceof DualPaneList));
        }
    }

    void switchView() {
        if (viewMode == ViewMode.LIST) {
            viewMode = ViewMode.GRID;
        } else {
            viewMode = ViewMode.LIST;
        }
        bridge.saveSettingsOnExit(gridCols, viewMode);

        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            fileList.setLayoutManager(layoutManager);

        } else {
            refreshSpan(((AceActivity) getActivity()).getConfiguration());
        }

        shouldStopAnimation = true;
        fileListAdapter.setViewMode(viewMode);

        fileListAdapter.setSearchCallback(this);
        fileList.setAdapter(fileListAdapter);
        fileListAdapter.notifyDataSetChanged();
        if (viewMode == ViewMode.LIST) {
            if (mGridItemDecoration != null) {
                fileList.removeItemDecoration(mGridItemDecoration);
            }
            if (dividerItemDecoration == null) {
                dividerItemDecoration = new DividerItemDecoration(getActivity(), currentTheme);
            }
            dividerItemDecoration.setOrientation();
            fileList.addItemDecoration(dividerItemDecoration);
        } else {
            if (dividerItemDecoration != null) {
                fileList.removeItemDecoration(dividerItemDecoration);
            }
            addItemDecoration();
        }

        initializeListeners();

    }


    SparseBooleanArray getSelectedItems() {
        return mSelectedItemPositions;
    }


    private boolean isRooted() {
        return isRootMode;
    }

    void showCreateDirDialog() {
        String title = getContext().getString(R.string.new_folder);
        String texts[] = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string
                                     .create), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.FOLDER_CREATION, null,
                                     dialogListener);
    }

    private void showCreateFileDialog() {
        String title = getContext().getString(R.string.new_file);
        String texts[] = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string.create), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.FILE_CREATION, null,
                                     dialogListener);
    }

    void showRenameDialog(FileInfo fileInfo, String text) {
        this.fileInfo = fileInfo;
        String title = getContext().getString(R.string.action_rename);
        String texts[] = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string.action_rename), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.RENAME, text, dialogListener);
    }

    public void onPasteAction(boolean isMove, ArrayList<FileInfo> filesToPaste, String
            destinationDir) {
        menuControls.endActionMode();
        Logger.log(TAG, "onPasteAction: isScanning:" + isMediaScanning + "mediascanpath:" + mediaScanningPaths.size() + "destinationDir:" + destinationDir);
        if (isMediaScanning && isMediaScanning(destinationDir)) {
            onOperationFailed(Operations.COPY);
            return;
        }
        bridge.startPasteOperation(destinationDir, isMove, isRooted(), filesToPaste);
    }

    private boolean isMediaScanning(String path) {

        for (String scannerPath : mediaScanningPaths) {
            if (path.equals(scannerPath)) {
                return true;
            }
        }
        return false;
    }


    private synchronized void addToMediaScanning(String path) {
        Log.d(TAG, "addToMediaScanning: path:" + path);
        isMediaScanning = true;
        mediaScanningPaths.add(path);
    }

    private synchronized void removeFromMediaScanning(String path) {
        Log.d(TAG, "removeFromMediaScanning: path:" + path);
        mediaScanningPaths.remove(path);
        if (mediaScanningPaths.size() == 0) {
            isMediaScanning = false;
        }
    }

    private Intent  operationIntent;
    private boolean isSAFShown;

    public void showSAFDialog(String path, Intent intent) {
        dismissDialog();
        Analytics.getLogger().SAFShown();
        operationIntent = intent;
        isSAFShown = true;
        String title = getContext().getString(R.string.needsaccess);
        String texts[] = new String[]{title, getContext().getString(R.string
                                                                            .needs_access_summary, path),
                getContext().getString(R.string.open), getContext().getString(R.string
                                                                                      .dialog_cancel)};
        DialogHelper.showSAFDialog(getContext(), R.layout.dialog_saf,
                                   texts, alertDialogListener);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (getActivity().getPackageManager().resolveActivity(intent, 0) != null) {
            fragment.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                                            SAF_REQUEST);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.msg_error_not_supported)
                    , Toast.LENGTH_LONG).show();
        }
    }

    public void onFileExists(Operations operation) {
        switch (operation) {
            case FOLDER_CREATION:
            case FILE_CREATION:
            case EXTRACT:
            case RENAME:
                final EditText editText = dialog.findViewById(R.id.editFileName);
                editText.setError(getContext().getString(R.string.msg_file_exists));
                break;
            case HIDE:
                Toast.makeText(getContext(), getContext().getString(R.string.msg_file_exists),
                               Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void showConflictDialog(final List<FileInfo> conflictFiles,
                                   List<FileInfo> destFiles, final String destinationDir, final
                                   boolean isMove,
                                   final DialogHelper.PasteConflictListener pasteConflictListener) {
        Analytics.getLogger().conflictDialogShown();
        DialogHelper.showConflictDialog(getContext(), conflictFiles, destFiles, destinationDir,
                                        isMove, pasteConflictListener);

    }

    public void showPasteProgressDialog(String destinationDir, List<FileInfo> files,
                                        boolean isMove) {
        new OperationProgress().showPasteProgress(getContext(), destinationDir, files,
                                                  isMove);
    }

    public void deleteFiles(ArrayList<FileInfo> filesToDelete) {
        if (isMediaScanning && isMediaScanning(filesToDelete.get(0).getFilePath())) {
            onOperationFailed(Operations.DELETE);
            return;
        }
        bridge.deleteFiles(filesToDelete);
    }

    public void sortFiles(int position) {
        bridge.persistSortMode(position);
        refreshList();
        if (mIsDualModeEnabled) {
            ((AceActivity) getActivity()).refreshList(!(fragment instanceof DualPaneList)); //Intentional negation to make the other pane reflect changes
        }
    }

    public void getPermissions(String filePath, boolean directory) {
        bridge.getFilePermissions(filePath, directory);
    }

    public void hideUnHideFiles(ArrayList<FileInfo> infoList, ArrayList<Integer> pos) {
        bridge.hideUnHideFiles(infoList, pos);
    }

    public void onExtractPositiveClick(Dialog dialog, String currentFilePath, String newFileName,
                                       boolean isChecked) {

        this.dialog = dialog;
        if (mSelectedPath == null) {
            mSelectedPath = currentDir;
        }
        bridge.onExtractPositiveClick(currentFilePath, newFileName, isChecked, mSelectedPath);
    }


    public void showSelectPathDialog(Button buttonPathSelect) {
        this.buttonPathSelect = buttonPathSelect;
        DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
        dialogFragment.setTargetFragment(fragment, DIALOG_FRAGMENT);
        dialogFragment.setStyle(DialogBrowseFragment.STYLE_NORMAL, getThemeStyle());
        dialogFragment.show(fragment.getFragmentManager(), "Browse Fragment");
    }


    private Dialog dialog;

    public void onInvalidName(Operations operation) {
        switch (operation) {
            case EXTRACT:
            case FILE_CREATION:
            case FOLDER_CREATION:
                final EditText editText = dialog.findViewById(R.id.editFileName);
                editText.setError(getContext().getString(R.string.msg_error_invalid_name));
                break;
        }
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void onPermissionsFetched(ArrayList<Boolean[]> permissionList) {
        menuControls.onPermissionsFetched(permissionList);

    }

    public int getSortMode() {
        return bridge.getSortMode();
    }

    public void onCompressPosClick(Dialog dialog, String newFileName,
                                   String extension, ArrayList<FileInfo> paths) {
        this.dialog = dialog;
        String newFilePath = currentDir + File.separator + newFileName + extension;
        bridge.onCompressPosClick(newFilePath, paths);
    }

    public void onPermissionSetError() {
        Toast.makeText(getContext(), getContext().getString(R.string.error), Toast.LENGTH_SHORT)
                .show();
    }

    public void onPermissionsSet() {
        refreshList();
    }

    public void setPermissions(String path, boolean isDir, String permissions) {
        bridge.setPermissions(path, isDir, permissions);
    }

    public void removeHomeFromNavPath() {
        isHomeScreenEnabled = false;
        navigationInfo.removeHomeFromNavPath();
    }

    public void openDrawer() {
        drawerListener.onDrawerIconClicked();
    }

    public void setDrawerListener(DrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }

    public void performVoiceSearch(String query) {
        Analytics.getLogger().searchClicked(true);
        menuControls.performVoiceSearch(query);
    }

    public void onQueryTextChange(String query) {
        fileListAdapter.filter(query);
    }

    public int onDragLocationEvent(DragEvent event, int oldPos) {
        View onTopOf = fileList.findChildViewUnder(event.getX(), event.getY());
        int newPos = fileList.getChildAdapterPosition(onTopOf);
//        Log.d(TAG, "onDragLocationEvent: pos:"+newPos);

        if (oldPos != newPos && newPos != RecyclerView.NO_POSITION) {
            // For scroll up
            if (oldPos != RecyclerView.NO_POSITION && newPos < oldPos) {
                int changedPos = newPos - 2;
                Logger.log(TAG, "drag Location old pos=" + oldPos + "new pos=" +
                        newPos +
                        "changed pos=" + changedPos);
                if (changedPos >= 0) {
                    fileList.smoothScrollToPosition(changedPos);
                }
            } else {
                int changedPos = newPos + 2;
                // For scroll down
                if (changedPos < fileInfoList.size()) {
                    fileList.smoothScrollToPosition(newPos + 2);
                }
                Logger.log(TAG, "drag Location old pos=" + oldPos + "new pos=" +
                        newPos +
                        "changed pos=" + changedPos);

            }
            oldPos = newPos;
            fileListAdapter.setDraggedPos(newPos);
        }
        return oldPos;
    }

    public void onDragDropEvent(DragEvent event) {
        if (!category.equals(FILES)) {
            Toast.makeText(getContext(), getResources().getString(R.string.error_unsupported),
                           Toast.LENGTH_SHORT).show();
            return;
        }
        View top = fileList.findChildViewUnder(event.getX(), event.getY());
        int position = fileList.getChildAdapterPosition(top);
        Logger.log(TAG, "DROP new pos=" + position + " this:" + StoragesUiView.this + "fileListSize:" + fileList.getAdapter().getItemCount());
        @SuppressWarnings("unchecked")
        ArrayList<FileInfo> draggedFiles = (ArrayList<FileInfo>) event.getLocalState();
        ArrayList<String> paths = new ArrayList<>();

                  /*  ArrayList<FileInfo> paths = dragData.getParcelableArrayListExtra(FileConstants
                            .KEY_PATH);*/

        String destinationDir;
        if (position != -1) {
            destinationDir = fileInfoList.get(position).getFilePath();
        } else {
            destinationDir = currentDir;
        }

        for (FileInfo info : draggedFiles) {
            paths.add(info.getFilePath());
        }

        String sourceParent = new File(draggedFiles.get(0).getFilePath()).getParent();
        if (!new File(destinationDir).isDirectory()) {
            destinationDir = new File(destinationDir).getParent();
        }

        boolean value = destinationDir.equals(sourceParent);
        Logger.log(TAG, "Source parent=" + sourceParent + " " + value);


        if (!paths.contains(destinationDir)) {
            if (!destinationDir.equals(sourceParent)) {
                Logger.log(TAG, "Source parent=" + sourceParent + " Dest=" +
                        destinationDir + "draggedFiles:" + draggedFiles.size());
                dragHelper.showDragDialog(draggedFiles, destinationDir);
            } else {
                ArrayList<FileInfo> info = new ArrayList<>();
                info.addAll(draggedFiles);
                Logger.log(TAG, "Source=" + draggedFiles.get(0) + "Dest=" +
                        destinationDir);
                onPasteAction(false, info, destinationDir);
            }
        }

//        draggedData = new ArrayList<>();
    }

    public void onDragExit() {
//        fileListAdapter.clearDragPos();
//        draggedData = new ArrayList<>();
    }

    public void onDragEnded(View view, DragEvent event) {

        View top1 = fileList.findChildViewUnder(event.getX(), event.getY());
        int position1 = fileList.getChildAdapterPosition(top1);
        Logger.log(TAG, "onDragEnded: " + category + " result:" + event.getResult() + " position:" +
                position1 + " this:" + StoragesUiView.this);

        if (!event.getResult() && position1 == RecyclerView.NO_POSITION) {

            ClipData clipData = event.getClipData();
            if (clipData == null) {
                menuControls.endActionMode();
                return;
            }
            ClipData.Item item = clipData.getItemAt(0);
            Intent intent = item.getIntent();
            if (intent == null) {
                menuControls.endActionMode();
                return;
            }
            int dragCategory = intent.getIntExtra(KEY_CATEGORY, 0);
            Logger.log(TAG, "onDragEnded: category:" + category + " dropcat:" + dragCategory);

            if (dragCategory == FILES.getValue() && !category.equals(FILES)) {
                Toast.makeText(getContext(), "Not supported", Toast.LENGTH_SHORT).show();
                return;
            }
            ViewParent parent1 = view.getParent().getParent();

            if (((View) parent1).getId() == R.id.frame_container_dual) {
                Logger.log(TAG, "DRAG END parent dual =" + true);
            } else {
                Logger.log(TAG, "DRAG END parent dual =" + false);
                BaseFileList singlePaneFragment = (BaseFileList)
                        fragment.getFragmentManager()
                                .findFragmentById(R.id.main_container);
                Logger.log(TAG, "DRAG END single dir=" + mLastSinglePaneDir);
                @SuppressWarnings("unchecked")
                ArrayList<FileInfo> dragPaths = (ArrayList<FileInfo>) event.getLocalState();
                if (mLastSinglePaneDir != null) {
                    String[] files = new File(mLastSinglePaneDir).list();
                    if (singlePaneFragment != null && files != null && files.length == 0 &&
                            dragPaths.size() != 0) {
                        dragHelper.showDragDialog(dragPaths, mLastSinglePaneDir);
                    }
                }
            }

        }
        view.post(new Runnable() {
            @Override
            public void run() {
                menuControls.endActionMode();
            }
        });
//        draggedData = new ArrayList<>();
    }

    public void syncDrawer() {
        if (drawerListener != null) {
            drawerListener.syncDrawer();
        }
    }

    public void showZipProgressDialog(ArrayList<FileInfo> files, String destinationPath) {
        new OperationProgress().showZipProgressDialog(getContext(), files, destinationPath);
    }

    @SuppressWarnings("unused")
    public void onOperationFailed(Operations operation) {
        Toast.makeText(getContext(), R.string.msg_operation_failed, Toast
                .LENGTH_SHORT).show();
    }

    public void showExtractDialog(Intent intent) {
        new OperationProgress().showExtractProgressDialog(getContext(), intent);
    }

    public void onSelectAllClicked() {
        if (mSelectedItemPositions != null) {
            if (mSelectedItemPositions.size() < fileListAdapter.getItemCount()) {
                toggleSelectAll(true);
            } else {
                toggleSelectAll(false);
            }
        }
    }


    private void startActionMode() {
        isActionModeActive = true;
        clearSelectedPos();
        hideFab();
        draggedData.clear();
        menuControls.startActionMode();
    }

    public void endActionMode() {
        isDragStarted = false;
        isActionModeActive = false;
        fileListAdapter.clearDragPos();
        fileListAdapter.removeSelection();
        mSwipeRefreshLayout.setEnabled(true);
        // FAB should be visible only for Files Category
        if (isFilesCategory()) {
            showFab();
        }
    }

    boolean isActionModeActive() {
        return isActionModeActive;
    }

    void setDualPaneState() {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);
    }


    @Override
    public void onHomeClicked() {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);

        isHomeClicked = true;
        menuControls.endActionMode();
        fileListAdapter.stopAutoPlayVid();
        //removeDualFileFragment();
        getActivity().onBackPressed();
    }


    @Override
    public void onNavButtonClicked(String dir) {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);

        if (isActionModeActive() && !menuControls.isPasteOp()) {
            menuControls.endActionMode();
        }
        if (isZipMode()) {
            menuControls.setCurrentDir(dir);
            zipViewer.onBackPressed(dir);
        } else {
            currentDir = dir;
            menuControls.setCurrentDir(currentDir);
            int position = 0;
            ArrayList<BackStackModel> backStack = backStackInfo.getBackStack();
            for (int i = 0; i < backStack.size(); i++) {
                if (currentDir.equals(backStack.get(i).getFilePath())) {
                    position = i;
                    break;
                }
            }
            for (int j = backStack.size() - 1; j > position; j--) {
                backStackInfo.removeEntryAtIndex(j);
            }
            refreshList();
            navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, FILES);
        }
    }

    @Override
    public void onNavButtonClicked(Category category, String bucketName) {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);

        if (isActionModeActive() && !menuControls.isPasteOp()) {
            menuControls.endActionMode();
        }
        this.category = category;
        menuControls.setCategory(category);
        menuControls.setupSortVisibility();

        int position = 0;
        ArrayList<BackStackModel> backStack = backStackInfo.getBackStack();
        for (int i = 0; i < backStack.size(); i++) {
            if (category.equals(backStack.get(i).getCategory())) {
                position = i;
                break;
            }
        }
        for (int j = backStack.size() - 1; j > position; j--) {
            backStackInfo.removeEntryAtIndex(j);
        }

        refreshList();
        navigationInfo.addLibSpecificNavButtons(isHomeScreenEnabled, category, bucketName);

    }

    private int getThemeStyle() {
        switch (currentTheme) {
            case DARK:
                return R.style.BaseDarkTheme;
            case LIGHT:
                return R.style.BaseLightTheme;
        }
        return R.style.BaseDarkTheme;
    }


    void updateFavouritesGroup(ArrayList<FileInfo> fileInfoList) {
        ArrayList<FavInfo> favInfoArrayList = new ArrayList<>();
        for (int i = 0; i < fileInfoList.size(); i++) {
            FileInfo info = fileInfoList.get(i);
            String name = info.getFileName();
            String path = info.getFilePath();
            FavInfo favInfo = new FavInfo();
            favInfo.setFileName(name);
            favInfo.setFilePath(path);
            favInfoArrayList.add(favInfo);
        }
        bridge.updateFavorites(favInfoArrayList);
        favoriteListener.updateFavorites(favInfoArrayList);
    }

    void removeFavorite(List<FileInfo> fileInfoList) {
        ArrayList<FavInfo> favInfoArrayList = new ArrayList<>();
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        for (int i = 0; i < fileInfoList.size(); i++) {
            FileInfo info = fileInfoList.get(i);
            String name = info.getFileName();
            String path = info.getFilePath();
            FavInfo favInfo = new FavInfo();
            favInfo.setFileName(name);
            favInfo.setFilePath(path);
            if (sharedPreferenceWrapper.removeFavorite(getActivity(), favInfo)) {
                favInfoArrayList.add(favInfo);
            }
        }
        if (favInfoArrayList.size() > 0) {
            if (category.equals(FAVORITES)) {
                refreshList();
            }
            favoriteListener.removeFavorites(favInfoArrayList);
        }
    }


    private StoragesUiView.FavoriteOperation favoriteListener;

    void changeGridCols() {
        refreshSpan(((AceActivity) getActivity()).getConfiguration());
    }

    private void refreshSpan(Configuration configuration) {
        if (viewMode == ViewMode.GRID) {
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT || !mIsDualModeEnabled) {
                gridCols = ConfigurationHelper.getStorageGridCols(configuration);
            } else {
                gridCols = ConfigurationHelper.getStorageDualGridCols(configuration);
            }
            layoutManager = new CustomGridLayoutManager(getActivity(), gridCols);
            fileList.setLayoutManager(layoutManager);
        }
    }


    private boolean isFilesCategory() {
        return category.equals(FILES);
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.log(TAG, "onConfigChanged " + newConfig.orientation);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            refreshSpan(newConfig);
        }
    }


    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (!mInstanceStateExists) {
            Logger.log(TAG, "onDestroy UNREGISTER");
            getActivity().unregisterReceiver(mReloadListReceiver);
        }
    }

    public void onViewDestroyed() {
        fileList.stopScroll();
        if (!mInstanceStateExists) {
            bridge.saveSettingsOnExit(gridCols, viewMode);
        }

        if (fileListAdapter != null) {
            fileListAdapter.onDetach();
        }
    }

    void clearSelectedPos() {
        mSelectedItemPositions = new SparseBooleanArray();
    }


    public void endDrag() {
        isDragStarted = false;
    }

    public void setHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public void onFavAdded(int count) {
        FileUtils.showMessage(getContext(), String.format(getContext().getString(R.string.msg_added_fav),
                                                          count));
    }

    public void onFavExists() {
        FileUtils.showMessage(getContext(), getContext().getString(R.string.fav_exists));
    }

    public void hideDualPane() {
        mIsDualModeEnabled = false;
    }


    public void setFavListener(StoragesUiView.FavoriteOperation favoriteListener) {
        this.favoriteListener = favoriteListener;
    }

    private DialogHelper.DialogCallback dialogListener = new DialogHelper.DialogCallback() {


        @Override
        public void onPositiveButtonClick(Dialog dialog, Operations operation, String name) {
            StoragesUiView.this.dialog = dialog;
            switch (operation) {
                case FOLDER_CREATION:
                    bridge.createDir(currentDir, name, isRooted());
                    break;
                case FILE_CREATION:
                    bridge.createFile(currentDir, name, isRooted());
                    break;
                case RENAME:
                    String filePath = fileInfo.getFilePath();
                    bridge.renameFile(filePath, new File(filePath).getParent(), name, isRooted());
                    break;
            }
        }

        @Override
        public void onNegativeButtonClick(Operations operations) {

        }
    };

    // Dialog for SAF and APK dialog
    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper
            .AlertDialogListener()
    {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPositiveButtonClick(View view) {
            if (isSAFShown) {
                triggerStorageAccessFramework();
            } else {
                Uri uri = createContentUri(getContext(), filePath);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_INSTALL_PACKAGE);

                String mimeType = getSingleton().getMimeTypeFromExtension(extension);
                intent.setData(uri);
                extension = null;

                if (mimeType != null) {
                    grantUriPermission(getContext(), intent, uri);
                } else {
                    openWith(uri, getContext());
                }
            }
        }

        @Override
        public void onNegativeButtonClick(View view) {
            if (isSAFShown) {
                Toast.makeText(getContext(), getContext().getString(R.string.error), Toast
                        .LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNeutralButtonClick(View view) {
            openZipViewer(filePath);
        }
    };

    public void addHomeNavPath() {
        isHomeScreenEnabled = true;
        if (checkIfFileCategory(category)) {
            if (shouldShowPathNavigation()) {
                navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled,
                                               category);
            } else {
                navigationInfo.addHomeNavButton(isHomeScreenEnabled, category);
            }
        } else if (showLibSpecificNavigation(category)) {
            navigationInfo.addLibSpecificNavButtons(isHomeScreenEnabled, category, bucketName);
        } else {
            navigationInfo.addHomeNavButton(isHomeScreenEnabled, category);
        }
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
        menuControls.updateMenuTitle(viewMode == ViewMode.LIST ? ViewMode.GRID : ViewMode.LIST);
        switchView();
    }


    public interface FavoriteOperation {
        void updateFavorites(ArrayList<FavInfo> favList);
        void removeFavorites(ArrayList<FavInfo> favList);
    }

    private ZipCommunicator zipCommunicator = new ZipCommunicator() {

        @Override
        public void removeZipScrollPos(String newPath) {
            if (newPath == null) {
                return;
            }
            scrollPosition.remove(newPath);
        }

        @Override
        public void endZipMode(String dir) {
            if (dir != null && dir.length() != 0) {
                currentDir = dir;
            }
            isZipViewer = false;
            zipViewer = null;
            ArrayList<BackStackModel> backStack = backStackInfo.getBackStack();
            int backStackSize = backStack.size();
            // If home clicked, backstack will be cleared already
            if (backStackSize == 0) {
                return;
            }
            int position = 0;
            if (currentDir == null) {
                position = backStackSize - 2;
            } else {
                for (int i = 0; i < backStackSize; i++) {
                    if (currentDir.equals(backStack.get(i).getFilePath())) {
                        position = i;
                        break;
                    }
                }
            }
            for (int j = backStackSize - 1; j > position; j--) {
                backStackInfo.removeEntryAtIndex(j);
            }

            refreshList();
            if (shouldShowPathNavigation()) {
                navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, category);
            } else {
                navigationInfo.addHomeNavButton(isHomeScreenEnabled, category);
            }
        }

        @Override
        public void calculateZipScroll(String dir) {
            calculateScroll(dir);
        }

        @Override
        public void onZipContentsLoaded(ArrayList<FileInfo> data) {
            onDataLoaded(data);
        }

        @Override
        public void openZipViewer(String currentDir) {
            StoragesUiView.this.openZipViewer(currentDir);
        }

        @Override
        public void setNavDirectory(String path, boolean isHomeScreenEnabled, Category category) {
            navigationInfo.setNavDirectory(path, isHomeScreenEnabled, category);
        }

        @Override
        public void addToBackStack(String path, Category category) {
            backStackInfo.addToBackStack(path, category);
        }

        @Override
        public void removeFromBackStack() {
            backStackInfo.removeEntryAtIndex(backStackInfo.getBackStack().size() - 1);
        }

        @Override
        public void setInitialDir(String path) {
            navigationInfo.setInitialDir(path);
        }
    };

}
