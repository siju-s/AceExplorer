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
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.siju.acexplorer.R;
import com.siju.acexplorer.base.view.BaseActivity;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.SharedPreferenceWrapper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.storage.model.BackStackModel;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.storage.model.backstack.BackStackInfo;
import com.siju.acexplorer.storage.model.backstack.NavigationCallback;
import com.siju.acexplorer.storage.model.backstack.NavigationInfo;
import com.siju.acexplorer.storage.model.operations.OperationProgress;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.model.zip.ZipViewer;
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager;
import com.siju.acexplorer.storage.view.custom.CustomLayoutManager;
import com.siju.acexplorer.storage.view.custom.DividerItemDecoration;
import com.siju.acexplorer.storage.view.custom.GridItemDecoration;
import com.siju.acexplorer.storage.view.custom.recyclerview.FastScrollRecyclerView;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.Dialogs;
import com.siju.acexplorer.utils.Utils;
import com.siju.acexplorer.view.DrawerListener;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.model.FileConstants.ADS;
import static com.siju.acexplorer.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.checkIfFileCategory;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.removeMedia;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.model.helper.SdkHelper.isAtleastNougat;
import static com.siju.acexplorer.model.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.model.helper.UriHelper.getUriForCategory;
import static com.siju.acexplorer.model.helper.UriHelper.grantUriPermission;
import static com.siju.acexplorer.model.helper.ViewHelper.openWith;
import static com.siju.acexplorer.model.helper.ViewHelper.viewFile;
import static com.siju.acexplorer.model.helper.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.model.helper.helper.ViewHelper.viewFile;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_RELOAD_LIST;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_POSITION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT;

/**
 * Created by Siju on 02 September,2017
 */
public class StoragesUiView extends CoordinatorLayout implements View.OnClickListener,
        NavigationCallback {

    private final String TAG = this.getClass().getSimpleName();
    private Theme theme;
    private Fragment fragment;
    private StorageBridge bridge;
    private CoordinatorLayout mMainLayout;
    private FastScrollRecyclerView fileList;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private String currentDir;
    private Category category;
    private int viewMode = ViewMode.LIST;
    private boolean isZipViewer;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private TextView mTextEmpty;
    private boolean mIsDualModeEnabled;
    private boolean isDragStarted;
    private long mLongPressedTime;
    private View mItemView;
    private ArrayList<FileInfo> draggedData = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private String mLastSinglePaneDir;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ActionMode actionMode;
    private SparseBooleanArray mSelectedItemPositions = new SparseBooleanArray();

    private String mSelectedPath;
    private Button buttonPathSelect;
    private final HashMap<String, Bundle> scrollPosition = new HashMap<>();
    private int gridCols;
    private SharedPreferences preferences;
    private int mCurrentOrientation;
    private final boolean mIsRootMode = true;
    private Dialogs dialogs;
    private boolean shouldStopAnimation = true;
    private DividerItemDecoration mDividerItemDecoration;
    private GridItemDecoration mGridItemDecoration;
    private boolean mInstanceStateExists;
    private final int DIALOG_FRAGMENT = 5000;
    public static final int SAF_REQUEST = 2000;
    private boolean isPremium = true;
    private AdView mAdView;
    private boolean isInSelectionMode;
    private FloatingActionsMenu fabCreateMenu;
    private FloatingActionButton fabCreateFolder;
    private FloatingActionButton fabCreateFile;
    private FloatingActionButton fabOperation;
    private FrameLayout frameLayoutFab;
    private boolean isHomeScreenEnabled;
    private NavigationInfo navigationInfo;
    private BackStackInfo backStackInfo;
    private Theme currentTheme;
    private DrawerListener drawerListener;
    private MenuControls menuControls;
    private DragHelper dragHelper;


    public StoragesUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public static StoragesUiView inflate(ViewGroup parent) {
        return (StoragesUiView) LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list,
                parent,
                false);
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

//        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        frameLayoutFab = findViewById(R.id.frameLayoutFab);
//        aceActivity.syncDrawerState();
        fabCreateMenu = findViewById(R.id.fabCreate);
        fabCreateFolder = findViewById(R.id.fabCreateFolder);
        fabCreateFile = findViewById(R.id.fabCreateFile);
        fabOperation = findViewById(R.id.fabOperation);
        frameLayoutFab.getBackground().setAlpha(0);
    }


    private void setTheme() {
        currentTheme = ((BaseActivity) getActivity()).getCurrentTheme();
        switch (currentTheme) {
            case DARK:
                mMainLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color
                        .dark_background));

//                frameLayoutFab.setBackgroundColor(ContextCompat.getColor(getContext(), R.color
// .dark_overlay));
                break;
        }
    }


    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    private AppCompatActivity getActivity() {
        return (AppCompatActivity) fragment.getActivity();
    }

    public void setBridgeRef(StorageBridge bridge) {
        this.bridge = bridge;
    }

    void initialize() {
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        dragHelper = new DragHelper(getContext());
        setTheme();
        fileList.setOnDragListener(dragHelper.getDragEventListener());
        menuControls = new MenuControls(getActivity(), this, theme);
        menuControls.setCategory(category);
        menuControls.setCurrentDir(currentDir);
        checkBillingStatus();
        registerReceivers();

        dialogs = new Dialogs();
        navigationInfo = new NavigationInfo(this, this);
        backStackInfo = new BackStackInfo();

        mCurrentOrientation = getResources().getConfiguration().orientation;
        getPreferences();
        getArgs();
        setupList();
        if (shouldShowPathNavigation()) {
            navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, category);
        }
        else {
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
        Log.d(TAG, "checkBillingStatus: " + billingStatus);
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

    public void onFreeVersion() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        showAds();
    }

    public void onPremiumVersion() {
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
                }
                else {
                    onFreeVersion();
                }
            }
        }
    };


    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter(ADS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(adsReceiver, intentFilter);
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
            mAdView = new AdView(getActivity().getApplicationContext());
            mAdView.setAdSize(AdSize.BANNER);
            mAdView.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));
            // DYNAMICALLY CREATE AD END
            AdRequest adRequest = new AdRequest.Builder().build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
            // Add the AdView to the view hierarchy. The view will have no size until the ad is
            // loaded.
            adviewLayout.addView(mAdView);
        }
        else {
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
            public void onItemClick(int position) {
                if (actionMode != null && !menuControls.isPasteOp()) {
                    itemClickActionMode(position, false);
                }
                else {
                    handleItemClick(position);
                }
            }
        });
        fileListAdapter.setOnItemLongClickListener(new FileListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Logger.log(TAG, "On long click" + isDragStarted);
                if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION) {
                    return;
                }

                if (!isZipViewer && !menuControls.isPasteOp()) {
                    itemClickActionMode(position, true);
                    mLongPressedTime = System.currentTimeMillis();

                    if (actionMode != null && fileListAdapter.getSelectedCount() >= 1) {
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

        fileList.setOnTouchListener(touchListener);
    }

    private void setupFab() {
        fabCreateFile.setOnClickListener(this);
        fabCreateFolder.setOnClickListener(this);
        fabOperation.setOnClickListener(this);

        fabCreateMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener() {

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

    private View.OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int event = motionEvent.getActionMasked();

            if (shouldStopAnimation) {
                stopAnimation();
                shouldStopAnimation = false;
            }

            if (isDragStarted && event == MotionEvent.ACTION_UP) {
                isDragStarted = false;
            }
            else if (isDragStarted && event == MotionEvent.ACTION_MOVE && mLongPressedTime !=
                    0) {
                long timeElapsed = System.currentTimeMillis() - mLongPressedTime;
//                    Logger.log(TAG, "On item touch time Elapsed" + timeElapsed);

                if (timeElapsed > 1000) {
                    mLongPressedTime = 0;
                    isDragStarted = false;
                    Logger.log(TAG, "On touch drag path size=" + draggedData.size());
                    if (draggedData.size() > 0) {
                        Intent intent = new Intent();
                        intent.putParcelableArrayListExtra(FileConstants.KEY_PATH, draggedData);
                        ClipData data = ClipData.newIntent("", intent);
                        int count = fileListAdapter.getSelectedCount();
                        View.DragShadowBuilder shadowBuilder = dragHelper.getDragShadowBuilder
                                (mItemView, count);
                        if (isAtleastNougat()) {
                            view.startDragAndDrop(data, shadowBuilder, draggedData, 0);
                        }
                        else {
                            view.startDrag(data, shadowBuilder, draggedData, 0);
                        }
                    }
                }
            }
            return false;
        }

    };


    private void handleItemClick(int position) {
        if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION) {
            return;
        }

        switch (category) {
            case AUDIO:
            case VIDEO:
            case IMAGE:
            case DOCS:
                viewFile(getContext(), fileInfoList.get(position).getFilePath(),
                        fileInfoList.get(position).getExtension(), alertDialogListener);
                break;
            case FILES:
            case DOWNLOADS:
            case COMPRESSED:
            case FAVORITES:
            case PDF:
            case APPS:
            case LARGE_FILES:
                genericFileItemClick(position);
                break;
        }
    }

    private void genericFileItemClick(int position) {
        if (fileInfoList.get(position).isDirectory()) {
            onDirectoryClicked(position);
        }
        else {
            onFileClicked(position);
        }
    }

    private void onDirectoryClicked(int position) {
        calculateScroll();
        if (isZipMode()) {
            zipViewer.onDirectoryClicked(position);
        }
        else {
            String path = fileInfoList.get(position).getFilePath();
            category = FILES;
            reloadList(path, category);
        }
    }

    private void onFileClicked(int position) {
        String filePath = fileInfoList.get(position).getFilePath();
        String extension = fileInfoList.get(position).getExtension();

        if (!isZipMode() && isZipViewable(filePath)) {
            openZipViewer(filePath);
        }
        else {
            if (isZipMode()) {
                zipViewer.onFileClicked(position);
            }
            else {
                viewFile(getContext(), filePath, extension, alertDialogListener);
            }
        }
    }


    private boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith(".zip") ||
                filePath.toLowerCase().endsWith(".jar") ||
                filePath.toLowerCase().endsWith(".rar");
    }


    public void setPremium() {
        isPremium = true;
        hideAds();
    }


    public void onPause() {
        pauseAds();
        Logger.log(TAG, "OnPause");
        if (!mInstanceStateExists) {
            getActivity().unregisterReceiver(mReloadListReceiver);
        }
    }

    public void onResume() {
        if (!mInstanceStateExists) {
            IntentFilter intentFilter = new IntentFilter(ACTION_RELOAD_LIST);
            intentFilter.addAction(ACTION_OP_REFRESH);
            getActivity().registerReceiver(mReloadListReceiver, intentFilter);
        }
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

                    Uri treeUri = intent.getData();
                    Log.d(TAG, "tree uri=" + treeUri + " old uri=" + oldUri);
                    bridge.handleSAFResult(operationIntent, treeUri, isRooted(), intent.getFlags());

                }
                // If not confirmed SAF, or if still not writable, then revert settings.
                else {
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
                if (mDividerItemDecoration == null) {
                    mDividerItemDecoration = new DividerItemDecoration(getActivity(), currentTheme);
                }
                else {
                    fileList.removeItemDecoration(mDividerItemDecoration);
                }
                fileList.addItemDecoration(mDividerItemDecoration);
                break;
            case ViewMode.GRID:
                if (mGridItemDecoration == null) {
                    mGridItemDecoration = new GridItemDecoration(getContext(), currentTheme,
                            gridCols);
                }
                else {
                    fileList.removeItemDecoration(mGridItemDecoration);
                }
                fileList.addItemDecoration(mGridItemDecoration);
                break;
        }
    }


    private ZipViewer zipViewer;

    public void openZipViewer(String path) {
        calculateScroll();
        isZipViewer = true;
        zipViewer = new ZipViewer(this, path);
        refreshList();
    }

    public void endZipMode() {
        isZipViewer = false;
        zipViewer = null;
    }


    private final BroadcastReceiver mReloadListReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(ACTION_RELOAD_LIST)) {
                calculateScroll();
                String path = intent.getStringExtra(KEY_FILEPATH);
                Logger.log(TAG, "New zip PAth=" + path);
                if (path != null) {
                    scanFile(getActivity().getApplicationContext(), path);
                }
                refreshList();
            }
            else if (action.equals(ACTION_OP_REFRESH)) {
                Bundle bundle = intent.getExtras();
                Operations operation = (Operations) bundle.getSerializable(KEY_OPERATION);
                onOperationResult(intent, operation);
            }
        }
    };

    void onOperationResult(Intent intent, Operations operation) {

        switch (operation) {
            case DELETE:

                ArrayList<FileInfo> deletedFilesList = intent.getParcelableArrayListExtra
                        (KEY_FILES);

                for (FileInfo info : deletedFilesList) {
                    scanFile(getActivity().getApplicationContext(), info.getFilePath());
                }

                Uri uri = getUriForCategory(category);
                getContext().getContentResolver().notifyChange(uri, null);
                fileInfoList.removeAll(deletedFilesList);
                fileListAdapter.setStopAnimation(true);
                fileListAdapter.updateAdapter(fileInfoList);

                break;

            case RENAME:

                final int position = intent.getIntExtra(KEY_POSITION, -1);
                String oldFile = intent.getStringExtra(KEY_FILEPATH);
                String newFile = intent.getStringExtra(KEY_FILEPATH2);
                int type = fileInfoList.get(position).getType();
                removeMedia(getActivity(), new File(oldFile), type);
                scanFile(getActivity().getApplicationContext(), newFile);
                fileInfoList.get(position).setFilePath(newFile);
                fileInfoList.get(position).setFileName(new File(newFile).getName());
                fileListAdapter.setStopAnimation(true);
                Logger.log(TAG, "Position changed=" + position);
                scanFile(getActivity().getApplicationContext(), newFile);
                fileListAdapter.notifyItemChanged(position);
                break;

            case CUT:
            case COPY:
                ArrayList<String> copiedFiles = intent.getStringArrayListExtra(KEY_FILES);

                if (copiedFiles != null) {
                    for (String path : copiedFiles) {
                        scanFile(getActivity().getApplicationContext(), path);
                    }
                }
            case FOLDER_CREATION:
            case FILE_CREATION:
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

    public boolean onBackPressed() {

        if (menuControls.isSearch()) {
            return true;
        }
        else if (isZipMode()) {
            zipViewer.onBackPressed();
        }
        else {
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
                    .getBackStack().size() - 1);
            if (checkIfFileCategory(currentCategory)) {
//                navigationInfo.setInitialDir();
                category = currentCategory;
                if (shouldShowPathNavigation()) {
                    navigationInfo.setInitialDir(currentDir);
                    navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled,
                            currentCategory);
                }
                else {
                    navigationInfo.addHomeNavButton(isHomeScreenEnabled, currentCategory);
                }
            }
            else {
                hideFab();
            }
            this.currentDir = currentDir;
            refreshList();
            menuControls.setTitleForCategory(currentCategory);
            if (currentCategory.equals(FILES)) {
                showFab();
            }

            return false;
        }
        else {
            removeFileFragment();
  /*          if (!isHomeScreenEnabled) {
                getActivity().finish();
            }*/
            return true;
        }
    }

    private boolean checkIfBackStackExists() {
        int backStackSize = backStackInfo.getBackStack().size();
        Logger.log(TAG, "checkIfBackStackExists --size=" + backStackSize);


        if (backStackSize == 1) {
            backStackInfo.clearBackStack();
            return false;
        }
        else if (backStackSize > 1) {
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
    private void removeFileFragment() {

        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id
                .main_container);
        Fragment dualFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id
                .frame_container_dual);

        backStackInfo.clearBackStack();
        Logger.log(TAG, "RemoveFragmentFromBackStack--frag=" + fragment);

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
        Log.d(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.fabCreateFile:
                showCreateFileDialog();
                fabCreateMenu.collapse();
                break;
            case R.id.fabCreateFolder:
                showCreateDirDialog();
                fabCreateMenu.collapse();
                break;

        }
    }


    private void toggleDragData(FileInfo fileInfo) {
        if (draggedData.contains(fileInfo)) {
            draggedData.remove(fileInfo);
        }
        else {
            draggedData.add(fileInfo);
        }
    }

    private void itemClickActionMode(int position, boolean isLongPress) {
        fileListAdapter.toggleSelection(position, isLongPress);

        boolean hasCheckedItems = fileListAdapter.getSelectedCount() > 0;
        ActionMode actionMode = getActionMode();
        if (hasCheckedItems && actionMode == null) {
            // there are some selected items, start the actionMode
//            aceActivity.updateDrawerIcon(true);

            startActionMode();
        }
        else if (!hasCheckedItems && actionMode != null) {
            // there no selected items, finish the actionMode
//            mActionModeCallback.endActionMode();
            actionMode.finish();
        }
        if (getActionMode() != null) {
            FileInfo fileInfo = fileInfoList.get(position);
            toggleDragData(fileInfo);
            SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
            setSelectedItemPos(checkedItemPos);
            this.actionMode.setTitle(String.valueOf(fileListAdapter
                    .getSelectedCount()) + " selected");
        }
    }

    private void calculateScroll() {
        View vi = fileList.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int position;
        if (viewMode == ViewMode.LIST) {
            position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        else {
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


    // 1 Extra for Footer since {#getItemCount has footer
    // TODO Remove this 1 when if footer removed in future
    private void toggleSelectAll(boolean selectAll) {
        fileListAdapter.clearSelection();
        for (int i = 0; i < fileListAdapter.getItemCount() - 1; i++) {
            fileListAdapter.toggleSelectAll(i, selectAll);
        }
        SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
        setSelectedItemPos(checkedItemPos);
        actionMode.setTitle(String.valueOf(fileListAdapter.getSelectedCount()) + " " +
                getResources().getString
                        (R.string.selected));
        fileListAdapter.notifyDataSetChanged();
    }

    private void clearSelection() {
        fileListAdapter.removeSelection();
    }


    public void reloadList(String path, Category category) {
        currentDir = path;
        this.category = category;
        if (shouldShowPathNavigation()) {
            navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, category);
        }
        else {
            navigationInfo.addHomeNavButton(isHomeScreenEnabled, category);
        }
        backStackInfo.addToBackStack(path, category);
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


    public boolean isZipMode() {
        return isZipViewer;
    }

    public void resetZipMode() {
        isZipViewer = false;
    }


    public void onReset() {
        // Clear the data in the adapter.
        Log.d(TAG, "onLoaderReset: ");
        fileListAdapter.updateAdapter(null);
    }

    void onDataLoaded(ArrayList<FileInfo> data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (data != null) {

            Log.d(TAG, "on onLoadFinished--" + data.size());

            shouldStopAnimation = true;
            fileInfoList = data;
            fileListAdapter.setCategory(category);
            fileList.setAdapter(fileListAdapter);
            fileListAdapter.updateAdapter(fileInfoList);

            addItemDecoration();

            if (!data.isEmpty()) {

                Log.d("TEST", "on onLoadFinished scrollpos--" + scrollPosition.entrySet());
                getScrolledPosition();
                fileList.stopScroll();
                mTextEmpty.setVisibility(View.GONE);
            }
            else {
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
            }
            else {
                ((GridLayoutManager) layoutManager).scrollToPositionWithOffset(b.getInt
                        (FileConstants
                                .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
            }
        }
    }

    private void putScrolledPosition(String path, Bundle position) {
        Log.d(TAG, "putScrolledPosition: " + path);
        scrollPosition.put(path, position);
    }

    private void removeScrolledPos(String path) {
        if (path == null) {
            return;
        }
        Log.d(TAG, "removeScrolledPos: " + path);
        scrollPosition.remove(path);
    }

    public void onZipContentsLoaded(ArrayList<FileInfo> fileInfoList) {
        onDataLoaded(fileInfoList);
    }

    int getViewMode() {
        return viewMode;
    }

    int switchView() {
        if (viewMode == ViewMode.LIST) {
            viewMode = ViewMode.GRID;
        } else {
            viewMode = ViewMode.LIST;
        }
        fileListAdapter = null;
        fileList.setHasFixedSize(true);

        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            fileList.setLayoutManager(layoutManager);

        }
        else {
            refreshSpan();
        }

        shouldStopAnimation = true;

        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, category, viewMode);
        fileList.setAdapter(fileListAdapter);
        if (viewMode == ViewMode.LIST) {
            if (mGridItemDecoration != null) {
                fileList.removeItemDecoration(mGridItemDecoration);
            }
            if (mDividerItemDecoration == null) {
                mDividerItemDecoration = new DividerItemDecoration(getActivity(), currentTheme);
            }
            mDividerItemDecoration.setOrientation();
            fileList.addItemDecoration(mDividerItemDecoration);
        }
        else {
            if (mDividerItemDecoration != null) {
                fileList.removeItemDecoration(mDividerItemDecoration);
            }
            addItemDecoration();
        }

        initializeListeners();

    }

    /**
     * Show dual pane in Landscape mode
     */
    public void showDualPane() {

        // For Files category only, show dual pane
        mIsDualModeEnabled = true;
        refreshSpan();
    }

    private void getPreferences() {
        Bundle bundle = bridge.getUserPrefs();
        gridCols = bundle.getInt(FileConstants.KEY_GRID_COLUMNS, 0);
        isHomeScreenEnabled = bundle.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        viewMode = bundle.getInt(FileConstants.PREFS_VIEW_MODE, ViewMode.LIST);
    }

    private void getArgs() {
        if (getArguments() != null) {
            currentDir = getArguments().getString(FileConstants.KEY_PATH);
            category = (Category) getArguments().getSerializable(FileConstants.KEY_CATEGORY);
            isZipViewer = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
            mIsDualModeEnabled = getArguments().getBoolean(FileConstants.KEY_DUAL_ENABLED, false);

            if (checkIfLibraryCategory(category)) {
                hideFab();
            }
            else {
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
//            aceActivity.toggleDualPaneVisibility(true);
            showDualPane();
//            aceActivity.createDualFragment();
        }
    }

    private boolean checkIfLibraryCategory(Category category) {
        return !category.equals(FILES);
    }

    public void showFab() {
        frameLayoutFab.setVisibility(View.VISIBLE);
    }

    public void hideFab() {
        frameLayoutFab.setVisibility(View.GONE);
    }


    private void setupList() {
        fileList.setHasFixedSize(true);
        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            fileList.setLayoutManager(layoutManager);
        }
        else {
            refreshSpan();
        }
        fileList.setItemAnimator(new DefaultItemAnimator());
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList,
                category, viewMode);
    }

    public void refreshList() {
        Log.d(TAG, "onCreateLoader: ");
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        mSwipeRefreshLayout.setRefreshing(true);
        if (isZipMode()) {
            zipViewer.onCreateLoader(null);
        }
        else {
            bridge.loadData(currentDir, category, false);
        }
    }


    @Override
    public void onPreExecute() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onPostExecute() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onProgressUpdate(FileInfo val) {
        addSearchResult(val);
    }

    @Override
    public void onCancelled() {
        mSwipeRefreshLayout.setRefreshing(false);

    }

    ActionModeCallback getActionModeCallback() {
        return new ActionModeCallback();
    }

    SparseBooleanArray getSelectedItems() {
        return mSelectedItemPositions;
    }


    public boolean isRooted() {
        return mIsRootMode;
    }

    private void showCreateDirDialog() {
        String title = getContext().getString(R.string.new_folder);
        String texts[] = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string
                .create), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.FOLDER_CREATION, ,
                dialogListener);
    }

    private void showCreateFileDialog() {
        String title = getContext().getString(R.string.new_file);
        String texts[] = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string.create), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.FILE_CREATION, , dialogListener);
    }

    void showRenameDialog(String text) {
        String title = getContext().getString(R.string.action_rename);
        String texts[] = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string.create), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.RENAME, text, dialogListener);
    }

    public void onPasteAction(boolean isMove, ArrayList<FileInfo> info) {
        bridge.startPasteOperation(currentDir, isMove, isRooted(), info);
    }

    private Intent operationIntent;
    private boolean isSAFShown;

    public void showSAFDialog(String path, Intent intent) {
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
            getActivity().startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                    SAF_REQUEST);
        }
        else {
            Toast.makeText(getContext(), getContext().getString(R.string.msg_error_not_supported)
                    , Toast.LENGTH_LONG).show();
        }
    }

    public void onFileExists(Operations operation, String msg) {
        FileUtils.showMessage(getContext(), msg);
        switch (operation) {
            case FOLDER_CREATION:
                showCreateDirDialog();
                break;

            case FILE_CREATION:
                showCreateFileDialog();
                break;

            case EXTRACT:
                final EditText editText = dialog.findViewById(R.id.editFileName);
                editText.setError(getContext().getString(R.string.file_exists));
                break;

        }

    }

    public void showConflictDialog(final List<FileInfo> conflictFiles,
                                   final String destinationDir, final boolean isMove,
                                   final DialogHelper.PasteConflictListener pasteConflictListener) {
        DialogHelper.showConflictDialog(getContext(), conflictFiles, destinationDir, isMove, pasteConflictListener);
    }

    public void showPasteProgressDialog(String destinationDir, List<FileInfo> files,
                                        List<CopyData> copyData, boolean isMove) {
        new OperationProgress().showPasteProgress(getContext(), destinationDir, files, copyData, isMove);
    }

    public void deleteFiles(ArrayList<FileInfo> filesToDelete) {
        bridge.deleteFiles(filesToDelete);
    }

    public void sortFiles(int position) {
        bridge.persistSortMode(position);
        refreshList();
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
        bridge.onExtractPositiveClick(currentFilePath, newFileName, isChecked, mSelectedPath);
    }



    public void showSelectPathDialog() {
        DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
        dialogFragment.setTargetFragment(fragment, DIALOG_FRAGMENT);
        dialogFragment.setStyle(DialogBrowseFragment.STYLE_NORMAL, getThemeStyle());
        dialogFragment.show(fragment.getFragmentManager(), "Browse Fragment");
    }


    private Dialog dialog;

    public void onInvalidName(Operations operation) {
        switch (operation) {
            case EXTRACT:
                final EditText editText = dialog.findViewById(R.id.editFileName);
                editText.setError(getContext().getString(R.string.msg_error_invalid_name));
                break;
        }
    }

    public void dismissDialog(Operations operation) {
        dialog.dismiss();
    }

    public void onPermissionsFetched(ArrayList<Boolean[]> permissionList) {
        menuControls.onPermissionsFetched(permissionList);

    }

    public int getSortMode() {
        return bridge.getSortMode();
    }

    public void onCompressPosClick(Dialog dialog, Operations operation, String newFileName, String extension, ArrayList<FileInfo> paths) {
        String newFilePath = currentDir + File.separator + newFileName + extension;
        bridge.onCompressPosClick(newFilePath, paths);
    }


    /**
     * Triggered on long press click on item
     */
    private final class ActionModeCallback implements ActionMode.Callback {


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menuControls.onStartActionMode();
            actionMode = mode;
            isInSelectionMode = true;
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.d(TAG, "onPrepareActionMode: ");
            return false;
        }


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_select_all:
                    if (mSelectedItemPositions != null) {
                        if (mSelectedItemPositions.size() < fileListAdapter.getItemCount() - 1) {
                            toggleSelectAll(true);
                        }
                        else {
                            toggleSelectAll(false);
                        }
                    }
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isInSelectionMode = false;
            clearSelection();
            actionMode = null;
            menuControls.onActionModeEnd();
            mSelectedItemPositions = new SparseBooleanArray();
            mSwipeRefreshLayout.setEnabled(true);
            draggedData.clear();
            // FAB should be visible only for Files Category
            if (isFilesCategory()) {
                showFab();
            }
        }
    }

    private boolean isInitialSearch;

    private void addSearchResult(FileInfo fileInfo) {
        // initially clearing the array for new result set
        if (!isInitialSearch) {
            fileInfoList.clear();
            fileListAdapter.clear();

        }
        isInitialSearch = true;
        fileInfoList.add(fileInfo);
        fileListAdapter.updateSearchResult(fileInfo);
        stopAnimation();
//        aceActivity.addToBackStack(currentDir, mCategory);

    }

    private void startActionMode() {
        hideFab();
        menuControls.startActionMode();
    }

    private ActionMode getActionMode() {
        return actionMode;
    }

    void finishActionMode() {
        actionMode.finish();
    }


    @Override
    public void onHomeClicked() {
        endActionMode();
        removeFileFragment();
        getActivity().onBackPressed();
    }


    @Override
    public void onNavButtonClicked(String dir) {

        if (isZipMode()) {
            zipViewer.onBackPressed();
        }
        else {
            currentDir = dir;
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


    private int getThemeStyle() {
        switch (currentTheme) {
            case DARK:
                return R.style.BaseDarkTheme_Dark;
            case LIGHT:
                return R.style.BaseLightTheme_Light;
        }
        return R.style.BaseDarkTheme_Dark;

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
            SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
            sharedPreferenceWrapper.addFavorite(getActivity(), favInfo);
            favInfoArrayList.add(favInfo);
        }

//        aceActivity.updateFavourites(favInfoArrayList);
    }

    void removeFavorite(ArrayList<FileInfo> fileInfoList) {
        ArrayList<FavInfo> favInfoArrayList = new ArrayList<>();
        for (int i = 0; i < fileInfoList.size(); i++) {
            FileInfo info = fileInfoList.get(i);
            String name = info.getFileName();
            String path = info.getFilePath();
            FavInfo favInfo = new FavInfo();
            favInfo.setFileName(name);
            favInfo.setFilePath(path);
            SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
            sharedPreferenceWrapper.removeFavorite(getActivity(), favInfo);
            favInfoArrayList.add(favInfo);
        }
        refreshList();
//        aceActivity.removeFavourites(favInfoArrayList);
    }






/*    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(this.getClass().getSimpleName(), "onCreateOptionsMenu" + "Fragment=");
*//*
//        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_base, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mPasteItem = menu.findItem(R.id.action_paste);
        mPasteItem.setVisible(mIsPasteItemVisible);
        viewMode = sharedPreferenceWrapper.getViewMode(getActivity());
        mViewItem = menu.findItem(R.id.action_view);
        updateMenuTitle();
        setupSearchView();*//*
    }*/


    public void refreshSpan() {
        if (viewMode == ViewMode.GRID) {
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT || !mIsDualModeEnabled ||
                    checkIfLibraryCategory(category)) {
                gridCols = getResources().getInteger(R.integer.grid_columns);
            }
            else {
                gridCols = getResources().getInteger(R.integer.grid_columns_dual);
            }
            Log.d(TAG, "Refresh span--columns=" + gridCols + "category=" + category + " dual " +
                    "mode=" +
                    mIsDualModeEnabled);

            layoutManager = new CustomGridLayoutManager(getActivity(), gridCols);
            fileList.setLayoutManager(layoutManager);
        }
    }


    private boolean isFilesCategory() {
        return category.equals(FILES);
    }

    public void endActionMode() {
        isInSelectionMode = false;
        clearSelectedPos();
        if (actionMode != null) {
            actionMode.finish();
        }
        actionMode = null;
        menuControls.hideBottomToolbar();
        mSwipeRefreshLayout.setEnabled(true);
        draggedData.clear();
    }

    public boolean isInSelectionMode() {
        return isInSelectionMode;
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            refreshSpan();
        }
        Logger.log(TAG, "onConfigurationChanged " + newConfig.orientation);

    }


    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    public void onViewDestroyed() {
        fileList.stopScroll();
        if (!mInstanceStateExists) {
            preferences.edit().putInt(FileConstants.KEY_GRID_COLUMNS, gridCols).apply();
            sharedPreferenceWrapper.savePrefs(getActivity(), viewMode);
        }
        menuControls.removeSearchTask();

        if (fileListAdapter != null) {
            fileListAdapter.onDetach();
        }
    }

    private void clearSelectedPos() {
        mSelectedItemPositions = new SparseBooleanArray();
    }


    public void setDrawerListener(DrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }

    private DialogHelper.DialogCallback dialogListener = new DialogHelper.DialogCallback() {

        @Override
        public void onError(int error) {

        }

        @Override
        public void onPositiveButtonClick(Dialog dialog, Operations operation, String name) {
            bridge.createDir(currentDir, name, isRooted());
        }

        @Override
        public void onNegativeButtonClick(Operations operations) {

        }
    };

    // Dialog for SAF
    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper
            .AlertDialogListener() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPositiveButtonClick(View view) {
            if (isSAFShown) {
                triggerStorageAccessFramework();
            } else {
                Uri uri = createContentUri(getContext(), currentDir);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_INSTALL_PACKAGE);

                String mimeType = getSingleton().getMimeTypeFromExtension(extension);
                intent.setData(uri);

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
            } else {
                openZipViewer(currentDir);
            }
        }

        @Override
        public void onNeutralButtonClick(View view) {

        }
    };

}
