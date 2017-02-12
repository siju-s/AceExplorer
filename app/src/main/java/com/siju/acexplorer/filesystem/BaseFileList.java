package com.siju.acexplorer.filesystem;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.siju.acexplorer.AceActivity;
import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.billing.BillingHelper;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.backstack.BackStackInfo;
import com.siju.acexplorer.filesystem.backstack.NavigationCallback;
import com.siju.acexplorer.filesystem.backstack.NavigationInfo;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.helper.FileOpsHelper;
import com.siju.acexplorer.filesystem.helper.ShareHelper;
import com.siju.acexplorer.filesystem.model.BackStackModel;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.modes.ViewMode;
import com.siju.acexplorer.filesystem.operations.OperationProgress;
import com.siju.acexplorer.filesystem.operations.Operations;
import com.siju.acexplorer.filesystem.root.RootUtils;
import com.siju.acexplorer.filesystem.task.CopyService;
import com.siju.acexplorer.filesystem.task.DeleteTask;
import com.siju.acexplorer.filesystem.task.MoveFiles;
import com.siju.acexplorer.filesystem.task.PasteConflictChecker;
import com.siju.acexplorer.filesystem.task.SearchTask;
import com.siju.acexplorer.filesystem.theme.Themes;
import com.siju.acexplorer.filesystem.ui.CustomGridLayoutManager;
import com.siju.acexplorer.filesystem.ui.CustomLayoutManager;
import com.siju.acexplorer.filesystem.ui.DialogBrowseFragment;
import com.siju.acexplorer.filesystem.ui.DividerItemDecoration;
import com.siju.acexplorer.filesystem.ui.EnhancedMenuInflater;
import com.siju.acexplorer.filesystem.ui.GridItemDecoration;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.views.FastScrollRecyclerView;
import com.siju.acexplorer.filesystem.zip.ZipViewer;
import com.siju.acexplorer.helper.RootHelper;
import com.siju.acexplorer.helper.root.RootTools;
import com.siju.acexplorer.helper.root.rootshell.execution.Command;
import com.siju.acexplorer.utils.Dialogs;
import com.siju.acexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.siju.acexplorer.R.string.hide;
import static com.siju.acexplorer.filesystem.FileConstants.ADS;
import static com.siju.acexplorer.filesystem.app.AppUtils.getAppIcon;
import static com.siju.acexplorer.filesystem.app.AppUtils.getAppIconForFolder;
import static com.siju.acexplorer.filesystem.groups.Category.AUDIO;
import static com.siju.acexplorer.filesystem.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.filesystem.groups.Category.FAVORITES;
import static com.siju.acexplorer.filesystem.groups.Category.FILES;
import static com.siju.acexplorer.filesystem.groups.Category.IMAGE;
import static com.siju.acexplorer.filesystem.groups.Category.VIDEO;
import static com.siju.acexplorer.filesystem.groups.Category.checkIfFileCategory;
import static com.siju.acexplorer.filesystem.helper.MediaStoreHelper.removeMedia;
import static com.siju.acexplorer.filesystem.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.filesystem.helper.PermissionsHelper.parse;
import static com.siju.acexplorer.filesystem.helper.UriHelper.getUriForCategory;
import static com.siju.acexplorer.filesystem.helper.ViewHelper.viewFile;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.ACTION_RELOAD_LIST;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_CONFLICT_DATA;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_POSITION;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_RESULT;


public class BaseFileList extends Fragment implements LoaderManager
        .LoaderCallbacks<ArrayList<FileInfo>>,
        SearchView.OnQueryTextListener,
        Toolbar.OnMenuItemClickListener, SearchTask.SearchHelper,
        View.OnClickListener, NavigationCallback, com.siju.acexplorer.common.SearchView.Listener, PopupMenu.OnMenuItemClickListener {

    private final String TAG = this.getClass().getSimpleName();
    private CoordinatorLayout mMainLayout;
    private FastScrollRecyclerView recyclerViewFileList;
    private View root;
    private final int LOADER_ID = 1000;
    private final int INVALID_POS = -1;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private String currentDir;
    private Category category;
    private int viewMode = ViewMode.LIST;
    private boolean isZipViewer;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private TextView mTextEmpty;
    private boolean mIsDualModeEnabled;
    private MenuItem mViewItem;
    private boolean isDragStarted;
    private long mLongPressedTime;
    private View mItemView;
    private ArrayList<FileInfo> draggedData = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private String mLastSinglePaneDir;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AceActivity aceActivity;
    private Toolbar mBottomToolbar;
    private ActionMode actionMode;
    private SparseBooleanArray mSelectedItemPositions = new SparseBooleanArray();
    private MenuItem mPasteItem;
    private MenuItem cancelItem;
    private MenuItem createItem;
    private MenuItem mRenameItem;
    private MenuItem mInfoItem;
    private MenuItem mArchiveItem;
    private MenuItem mFavItem;
    private MenuItem mExtractItem;
    private MenuItem mHideItem;
    private MenuItem mPermissionItem;
    private boolean mIsMoveOperation = false;
    private String mSelectedPath;
    private Button buttonPathSelect;
    private final HashMap<String, Bundle> scrollPosition = new HashMap<>();
    private int gridCols;
    private SharedPreferences preferences;
    private int mCurrentOrientation;
    private final ArrayList<FileInfo> mCopiedData = new ArrayList<>();
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
    private LinearLayout navDirectory;
    private HorizontalScrollView scrollNavigation;
    private Toolbar toolbar;
    private boolean isHomeScreenEnabled;
    private NavigationInfo navigationInfo;
    private BackStackInfo backStackInfo;
    private Themes currentTheme;
    private FileOpsHelper fileOpHelper;
    private ImageButton imgNavigationIcon;
    private TextView toolbarTitle;
    private com.siju.acexplorer.common.SearchView searchView;
    private ImageButton imgOverflow;
    private boolean isPasteVisible;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        aceActivity = (AceActivity) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.main_list, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        initializeViews();
        setToolbar(savedInstanceState);
        setViewTheme();
        setupAds();
        registerReceivers();
        fileOpHelper = new FileOpsHelper(this);
        dialogs = new Dialogs();
        navigationInfo = new NavigationInfo(this);
        backStackInfo = new BackStackInfo();

        if (savedInstanceState == null) {
            mCurrentOrientation = getResources().getConfiguration().orientation;
            checkPreferences();
            getArgs();
            viewMode = sharedPreferenceWrapper.getViewMode(getActivity());
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
        } else {
            mInstanceStateExists = true;
        }
    }

    private void initializeViews() {
        mMainLayout = (CoordinatorLayout) root.findViewById(R.id.main_content);
        recyclerViewFileList = (FastScrollRecyclerView) root.findViewById(R.id.recyclerViewFileList);
        mTextEmpty = (TextView) root.findViewById(R.id.textEmpty);
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        recyclerViewFileList.setOnDragListener(new myDragEventListener());
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        int colorResIds[] = {R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorPrimaryDark};
        mSwipeRefreshLayout.setColorSchemeResources(colorResIds);
        mSwipeRefreshLayout.setDistanceToTriggerSync(500);
        mBottomToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_bottom);
        toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        frameLayoutFab = (FrameLayout) root.findViewById(R.id.frameLayoutFab);
        aceActivity.syncDrawerState();
        toolbar.setTitle(R.string.app_name);
        fabCreateMenu = (FloatingActionsMenu) getActivity().findViewById(R.id.fabCreate);
        fabCreateFolder = (FloatingActionButton) getActivity().findViewById(R.id.fabCreateFolder);
        fabCreateFile = (FloatingActionButton) getActivity().findViewById(R.id.fabCreateFile);
        fabOperation = (FloatingActionButton) getActivity().findViewById(R.id.fabOperation);

        navDirectory = (LinearLayout) root.findViewById(R.id.navButtons);
        scrollNavigation = (HorizontalScrollView) root.findViewById(R.id.scrollNavigation);
        frameLayoutFab.getBackground().setAlpha(0);
    }

    private void setToolbar(Bundle savedInstanceState) {
        View actionBar = getLayoutInflater(savedInstanceState).inflate(R.layout.abc_custom, null);
        toolbar.addView(actionBar);
        searchView = (com.siju.acexplorer.common.SearchView) actionBar.findViewById(R.id.search_view);
        searchView.setListener(this);
        imgNavigationIcon = (ImageButton) actionBar.findViewById(R.id.imgNavigationIcon);
        toolbarTitle = (TextView) actionBar.findViewById(R.id.toolbarTitle);
        imgNavigationIcon.setOnClickListener(this);
        imgOverflow = (ImageButton) actionBar.findViewById(R.id.imgButtonOverflow);
        imgOverflow.setOnClickListener(this);
    }


    private void setViewTheme() {
        currentTheme = ((BaseActivity) getActivity()).getCurrentTheme();
        switch (currentTheme) {
            case DARK:
                mMainLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_colorPrimary));
                scrollNavigation.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                mBottomToolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                toolbar.setPopupTheme(R.style.Dark_AppTheme_PopupOverlay);
                mBottomToolbar.setPopupTheme(R.style.Dark_AppTheme_PopupOverlay);

//                frameLayoutFab.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_overlay));
                break;
            case LIGHT:
                toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
                mBottomToolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
                break;
        }
    }

    private BroadcastReceiver adsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ADS)) {
                isPremium = intent.getBooleanExtra(FileConstants.KEY_PREMIUM, false);
                if (isPremium) {
                    hideAds();
                } else {
                    showAds();
                }
            }
        }
    };

    private void setupAds() {
        isPremium = BillingHelper.getInstance().getInAppBillingStatus().equals(BillingStatus.PREMIUM);
        if (isPremium) {
            hideAds();
        } else {
            if (getActivity() != null && !getActivity().isFinishing()) {
                showAds();
            }
        }
    }


    private void hideAds() {
        LinearLayout adviewLayout = (LinearLayout) root.findViewById(R.id.adviewLayout);
        if (adviewLayout.getChildCount() != 0) {
            adviewLayout.removeView(mAdView);
        }
    }

    private void showAds() {
        // DYNAMICALLY CREATE AD START
        LinearLayout adviewLayout = (LinearLayout) root.findViewById(R.id.adviewLayout);
        // Create an ad.
        if (mAdView == null) {
            mAdView = new AdView(getActivity().getApplicationContext());
            mAdView.setAdSize(AdSize.BANNER);
            mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
            // DYNAMICALLY CREATE AD END
            AdRequest adRequest = new AdRequest.Builder().build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
            // Add the AdView to the view hierarchy. The view will have no size until the ad is loaded.
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

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter(ADS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(adsReceiver, intentFilter);
    }

    private void checkPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gridCols = preferences.getInt(FileConstants.KEY_GRID_COLUMNS, 0);
        isHomeScreenEnabled = preferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
    }

    private void getArgs() {
        if (getArguments() != null) {
            currentDir = getArguments().getString(FileConstants.KEY_PATH);
            category = (Category) getArguments().getSerializable(FileConstants.KEY_CATEGORY);
            isZipViewer = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
            mIsDualModeEnabled = getArguments().getBoolean(FileConstants.KEY_DUAL_ENABLED, false);

            if (checkIfLibraryCategory(category)) {
                hideFab();
            } else {
                showFab();
            }
            toggleNavigationVisibility(true);
            mLastSinglePaneDir = currentDir;
        }
    }

    private void createDualFrag() {
        if (mIsDualModeEnabled && this instanceof FileList) {
            aceActivity.toggleDualPaneVisibility(true);
            showDualPane();
            aceActivity.createDualFragment();
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

    public void toggleNavigationVisibility(boolean isVisible) {
        scrollNavigation.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void setupList() {
        recyclerViewFileList.setHasFixedSize(true);
        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            recyclerViewFileList.setLayoutManager(layoutManager);
        } else {
            refreshSpan();
        }
        recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList,
                category, viewMode);
    }

    public void refreshList() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    private void initializeListeners() {

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aceActivity.openDrawer();
            }
        });

        fabCreateFile.setOnClickListener(this);
        fabCreateFolder.setOnClickListener(this);
        fabOperation.setOnClickListener(this);

        setupFab();


        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (actionMode != null) {
                    itemClickActionMode(position, false);
                } else {
                    handleItemClick(position);
                }
            }
        });
        fileListAdapter.setOnItemLongClickListener(new FileListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Logger.log(TAG, "On long click" + isDragStarted);
                if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION) return;

                if (!isZipViewer) {
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


        recyclerViewFileList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        recyclerViewFileList.setOnTouchListener(new View.OnTouchListener() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                Logger.log(TAG, "On touch listener" + shouldStopAnimation);

                int event = motionEvent.getActionMasked();

                if (shouldStopAnimation) {
                    stopAnimation();
                    shouldStopAnimation = false;
                }

                if (isDragStarted && event == MotionEvent.ACTION_UP) {
                    isDragStarted = false;
                } else if (isDragStarted && event == MotionEvent.ACTION_MOVE && mLongPressedTime != 0) {
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
                            View.DragShadowBuilder shadowBuilder = new MyDragShadowBuilder(mItemView,
                                    count);
                            if (Utils.isAtleastNougat()) {
                                view.startDragAndDrop(data, shadowBuilder, draggedData, 0);
                            } else {
                                view.startDrag(data, shadowBuilder, draggedData, 0);
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    private void setupFab() {
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


    private void handleItemClick(int position) {
        if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION) return;

        switch (category) {
            case AUDIO:
            case VIDEO:
            case IMAGE:
            case DOCS:
                viewFile(BaseFileList.this, fileInfoList.get(position).getFilePath(),
                        fileInfoList.get(position).getExtension());
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
        } else {
            onFileClicked(position);
        }
    }

    private void onDirectoryClicked(int position) {
        computeScroll();
        if (isZipMode()) {
            zipViewer.onDirectoryClicked(position);
        } else {
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
        } else {
            if (isZipMode()) {
                zipViewer.onFileClicked(position);
            } else {
                viewFile(BaseFileList.this, filePath, extension);
            }
        }
    }


    private boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith(".zip") ||
                filePath.toLowerCase().endsWith(".jar") ||
                filePath.toLowerCase().endsWith(".rar");
    }


    @Override
    public void scrollNavigation() {
        scrollNavigation.postDelayed(new Runnable() {
            public void run() {
                scrollNavigation.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);
    }

    public boolean onBackPressed() {
        if (isSearchVisible()) {
            hideSearchView();
            removeSearchTask();
        } else if (isZipMode()) {
            zipViewer.onBackPressed();
        } else {
            return backOperation();
        }
        return false;
    }


    private boolean checkIfBackStackExists() {
        int backStackSize = backStackInfo.getBackStack().size();
        Logger.log(TAG, "checkIfBackStackExists --size=" + backStackSize);


        if (backStackSize == 1) {
            backStackInfo.clearBackStack();
            return false;
        } else if (backStackSize > 1) {
            return true;
        }
        return false;
    }


    private boolean backOperation() {

        if (checkIfBackStackExists()) {
            removeScrolledPos(currentDir);
            backStackInfo.removeEntryAtIndex(backStackInfo.getBackStack().size() - 1);

            String currentDir = backStackInfo.getDirAtPosition(backStackInfo.getBackStack().size() - 1);
            Category currentCategory = backStackInfo.getCategoryAtPosition(backStackInfo.getBackStack().size() - 1);
            if (checkIfFileCategory(currentCategory)) {
//                navigationInfo.setInitialDir();
                category = currentCategory;
                if (shouldShowPathNavigation()) {
                    navigationInfo.setInitialDir(currentDir);
                    navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, currentCategory);
                } else {
                    navigationInfo.addHomeNavButton(isHomeScreenEnabled, currentCategory);
                }
            } else {
                hideFab();
            }
            this.currentDir = currentDir;
            refreshList();
            setTitleForCategory(currentCategory);
            if (currentCategory.equals(FILES)) {
                showFab();
            }

            return false;
        } else {
            removeFileFragment();
  /*          if (!isHomeScreenEnabled) {
                getActivity().finish();
            }*/
            return true;
        }
    }

    private boolean shouldShowPathNavigation() {
        return category.equals(FILES) || category.equals(DOWNLOADS);

    }

    public void onPermissionGranted() {
        refreshList();
    }


    private void setTitleForCategory(Category category) {
        switch (category) {
            case FILES:
                toolbar.setTitle(getString(R.string.app_name));
                break;
            case AUDIO:
                toolbar.setTitle(getString(R.string.nav_menu_music));
                break;
            case VIDEO:
                toolbar.setTitle(getString(R.string.nav_menu_video));
                break;
            case IMAGE:
                toolbar.setTitle(getString(R.string.nav_menu_image));
                break;
            case DOCS:
                toolbar.setTitle(getString(R.string.nav_menu_docs));
                break;
            default:
                toolbar.setTitle(getString(R.string.app_name));
        }
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

        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_container);
        Fragment dualFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

        backStackInfo.clearBackStack();
        Logger.log(TAG, "RemoveFragmentFromBackStack--frag=" + fragment);

        if (dualFragment != null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
                    .exit_to_left);
            ft.remove(dualFragment);
            ft.commitAllowingStateLoss();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!mInstanceStateExists) {
            IntentFilter intentFilter = new IntentFilter(ACTION_RELOAD_LIST);
            intentFilter.addAction(ACTION_OP_REFRESH);
            getActivity().registerReceiver(mReloadListReceiver, intentFilter);
        }
        resumeAds();
    }


    private void resumeAds() {
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
        pauseAds();
        super.onPause();
        Logger.log(TAG, "OnPause");
        if (!mInstanceStateExists) {
            getActivity().unregisterReceiver(mReloadListReceiver);
        }
    }

    private void pauseAds() {
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    mSelectedPath = intent.getStringExtra("PATH");
                    if (buttonPathSelect != null) {
                        buttonPathSelect.setText(mSelectedPath);
                    }
                }
            case SAF_REQUEST:
                String uriString = preferences.getString(FileConstants.SAF_URI, null);

                Uri oldUri = uriString != null ? Uri.parse(uriString) : null;

                if (resultCode == Activity.RESULT_OK) {
                    Uri treeUri = intent.getData();
                    Log.d(TAG, "tree uri=" + treeUri + " old uri=" + oldUri);
                    // Get Uri from Storage Access Framework.
                    // Persist URI - this is required for verification of writability.
                    if (treeUri != null) {
                        preferences.edit().putString(FileConstants.SAF_URI, treeUri.toString()).apply();
                        int takeFlags = intent.getFlags();
                        takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getActivity().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                        handleSAFOpResult(intent);
                    }
                }
                // If not confirmed SAF, or if still not writable, then revert settings.
                else {
                    if (oldUri != null) {
                        preferences.edit().putString(FileConstants.SAF_URI, oldUri.toString()).apply();
                    }

                    Toast.makeText(getContext(), getString(R.string.access_denied_external), Toast.LENGTH_LONG).show();
                    return;
                }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void handleSAFOpResult(Intent intent) {
        Operations operation = (Operations) intent.getSerializableExtra(KEY_OPERATION);
        switch (operation) {

            case DELETE:
                ArrayList<FileInfo> files = intent.getParcelableArrayListExtra(KEY_FILES);
                new DeleteTask(getContext(), mIsRootMode, files).execute();
                break;

            case COPY:

                Intent copyIntent = new Intent(getActivity(), CopyService.class);
                ArrayList<FileInfo> copiedFiles = intent.getParcelableArrayListExtra(KEY_FILES);
                ArrayList<CopyData> copyData = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA);
                String destinationPath = intent.getStringExtra(KEY_FILEPATH);
                copyIntent.putParcelableArrayListExtra(KEY_FILES, copiedFiles);
                copyIntent.putParcelableArrayListExtra(KEY_CONFLICT_DATA, copyData);
                copyIntent.putExtra(KEY_FILEPATH, destinationPath);
                new OperationProgress().showCopyProgressDialog(getActivity(), copyIntent);
                break;

            case CUT:
                ArrayList<FileInfo> movedFiles = intent.getParcelableArrayListExtra(KEY_FILES);
                ArrayList<CopyData> moveData = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA);
                String destinationMovePath = intent.getStringExtra(KEY_FILEPATH);
                new MoveFiles(getContext(), movedFiles, moveData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        destinationMovePath);
                break;

            case FOLDER_CREATION:
                String path = intent.getStringExtra(KEY_FILEPATH);
                fileOpHelper.mkDir(new File(path), mIsRootMode);
                break;

            case FILE_CREATION:
                String newFilePathCreate = intent.getStringExtra(KEY_FILEPATH);
                fileOpHelper.mkFile(new File(newFilePathCreate), mIsRootMode);
                break;

            case RENAME:
                String oldFilePath = intent.getStringExtra(KEY_FILEPATH);
                String newFilePath = intent.getStringExtra(KEY_FILEPATH2);
                int position = intent.getIntExtra(KEY_POSITION, INVALID_POS);
                fileOpHelper.renameFile(new File(oldFilePath), new File(newFilePath),
                        position, mIsRootMode);
                break;

            case EXTRACT:
                String oldFilePath1 = intent.getStringExtra(KEY_FILEPATH);
                String newFilePath1 = intent.getStringExtra(KEY_FILEPATH2);
                fileOpHelper.extractFile(new File(oldFilePath1), new File(newFilePath1));
                break;

            case COMPRESS:
                ArrayList<FileInfo> compressedFiles = intent.getParcelableArrayListExtra(KEY_FILES);
                String destinationCompressPath = intent.getStringExtra(KEY_FILEPATH);
                fileOpHelper.compressFile(new File(destinationCompressPath), compressedFiles);
                break;
        }
    }


    private void addItemDecoration() {

        switch (viewMode) {
            case ViewMode.LIST:
                if (mDividerItemDecoration == null) {
                    mDividerItemDecoration = new DividerItemDecoration(getActivity(), currentTheme);
                } else {
                    recyclerViewFileList.removeItemDecoration(mDividerItemDecoration);
                }
                recyclerViewFileList.addItemDecoration(mDividerItemDecoration);
                break;
            case ViewMode.GRID:
                if (mGridItemDecoration == null) {
                    mGridItemDecoration = new GridItemDecoration(getContext(), currentTheme, gridCols);
                } else {
                    recyclerViewFileList.removeItemDecoration(mGridItemDecoration);
                }
                recyclerViewFileList.addItemDecoration(mGridItemDecoration);
                break;
        }
    }


    private ZipViewer zipViewer;

    public void openZipViewer(String path) {
        computeScroll();
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
            if (action.equals(ACTION_RELOAD_LIST)) {
                computeScroll();
                String path = intent.getStringExtra(KEY_FILEPATH);
                Logger.log(TAG, "New zip PAth=" + path);
                if (path != null) {
                    scanFile(getActivity().getApplicationContext(), path);
                }
                refreshList();
            } else if (action.equals(ACTION_OP_REFRESH)) {
                Bundle bundle = intent.getExtras();
                Operations operation = (Operations) bundle.getSerializable(KEY_OPERATION);
                onOperationResult(intent, operation);
            }
        }
    };

    private void onOperationResult(Intent intent, Operations operation) {

        switch (operation) {
            case DELETE:

                ArrayList<FileInfo> deletedFilesList = intent.getParcelableArrayListExtra(KEY_FILES);

                for (FileInfo info : deletedFilesList) {
                    scanFile(getActivity().getApplicationContext(), info.getFilePath());
                }

                Uri uri = getUriForCategory(category);
                getContext().getContentResolver().notifyChange(uri, null);
                for (int i = 0; i < deletedFilesList.size(); i++) {
                    fileInfoList.remove(deletedFilesList.get(i));
                }
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
                    Toast.makeText(getActivity(), getString(R.string.msg_operation_failed), Toast
                            .LENGTH_LONG).show();
                } else {
                    computeScroll();
                    refreshList();
                }
                break;

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

    public void removeHomeFromNavPath() {
        Logger.log(TAG, "Nav directory count=" + navDirectory.getChildCount());

        for (int i = 0; i < Math.min(navDirectory.getChildCount(), 2); i++) {
            navDirectory.removeViewAt(0);
        }
    }

    private RefreshData refreshData;

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

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.fabCreateFile:
                new Dialogs().createFileDialog(this, mIsRootMode, currentDir);
                fabCreateMenu.collapse();
                break;
            case R.id.fabCreateFolder:
                new Dialogs().createDirDialog(this, mIsRootMode, currentDir);
                fabCreateMenu.collapse();
                break;
            case R.id.imgNavigationIcon:
                if (searchView.isExpanded()) {
                    searchView.enableSearch(false);
                } else {
                    aceActivity.openDrawer();
                }
                break;
            case R.id.imgButtonOverflow:
                showOptionsPopup(imgOverflow);
                break;
        }
    }

    private void showOptionsPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.file_base, popupMenu.getMenu());
        viewMode = sharedPreferenceWrapper.getViewMode(getActivity());
        mViewItem = popupMenu.getMenu().findItem(R.id.action_view);
        updateMenuTitle();
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    public FileOpsHelper getFileOpHelper() {
        return fileOpHelper;
    }

    @Override
    public void addViewToNavigation(View view) {
        navDirectory.addView(view);
    }

    @Override
    public void clearNavigation() {
        navDirectory.removeAllViews();
    }

    @Override
    public void onHomeClicked() {
        endActionMode();
        if (isSearchVisible()) {
            hideKeyboard();
            hideSearchView();
        }
        removeFileFragment();
        getActivity().onBackPressed();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
    }

    @Override
    public void onNavButtonClicked(String dir) {

        if (isZipMode()) {
            zipViewer.onBackPressed();
        } else {
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

    @Override
    public void onAnimationProgress(float f, boolean z) {

    }

    @Override
    public void onQueryChange(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            fileListAdapter.filter(charSequence.toString());
        }
    }

    @Override
    public void onQuerySubmit(CharSequence charSequence) {

    }

    @Override
    public void onSearchEnabled(boolean isExpanded) {
        Log.d(TAG, "onSearchEnabled: " + isExpanded);
        if (isExpanded) {
            toolbarTitle.setVisibility(View.GONE);
            searchView.setHint(getString(R.string.action_search));
            imgNavigationIcon.setImageResource(R.drawable.ic_up_arrow);
        } else {
            toolbarTitle.setVisibility(View.VISIBLE);
            imgNavigationIcon.setImageResource(R.drawable.ic_drawer);
        }
    }


    interface RefreshData {
        void refresh(Category category);
    }

    public void setRefreshData(RefreshData data) {
        refreshData = data;
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
        ActionMode actionMode = getActionMode();
        if (hasCheckedItems && actionMode == null) {
            // there are some selected items, start the actionMode
//            aceActivity.updateDrawerIcon(true);

            startActionMode();
        } else if (!hasCheckedItems && actionMode != null) {
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

    private void computeScroll() {
        View vi = recyclerViewFileList.getChildAt(0);
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
        setupMenuVisibility();
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
        actionMode.setTitle(String.valueOf(fileListAdapter.getSelectedCount()) + " " + getString(R.string.selected));
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
        } else {
            navigationInfo.addHomeNavButton(isHomeScreenEnabled, category);
        }
        backStackInfo.addToBackStack(path, category);
        refreshList();
    }


    private void stopAnimation() {
        if (!fileListAdapter.mStopAnimation) {
            for (int i = 0; i < recyclerViewFileList.getChildCount(); i++) {
                View view = recyclerViewFileList.getChildAt(i);
                if (view != null) view.clearAnimation();
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


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        mSwipeRefreshLayout.setRefreshing(true);
        if (isZipMode()) {
            return zipViewer.onCreateLoader(id, args);
        } else {
            return new FileListLoader(this, currentDir, category, false);
        }
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        onDataLoaded(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {
        // Clear the data in the adapter.
        Log.d(TAG, "onLoaderReset: ");
        fileListAdapter.updateAdapter(null);
    }

    private void onDataLoaded(ArrayList<FileInfo> data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (data != null) {

            Log.d(TAG, "on onLoadFinished--" + data.size());

            shouldStopAnimation = true;
            fileInfoList = data;
            fileListAdapter.setCategory(category);
            recyclerViewFileList.setAdapter(fileListAdapter);
            fileListAdapter.updateAdapter(fileInfoList);

            addItemDecoration();

            if (!data.isEmpty()) {

                Log.d("TEST", "on onLoadFinished scrollpos--" + scrollPosition.entrySet());
                getScrolledPosition();
                recyclerViewFileList.stopScroll();
                mTextEmpty.setVisibility(View.GONE);
            } else {
                mTextEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    private void getScrolledPosition() {
        if (currentDir != null && scrollPosition.containsKey(currentDir)) {
            Bundle b = scrollPosition.get(currentDir);
            if (viewMode == ViewMode.LIST)
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(b.getInt(FileConstants
                        .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
            else
                ((GridLayoutManager) layoutManager).scrollToPositionWithOffset(b.getInt(FileConstants
                        .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
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


    private void startActionMode() {

        hideFab();
//        toggleDummyView(true);
        mBottomToolbar.setVisibility(View.VISIBLE);
        mBottomToolbar.inflateMenu(R.menu.action_mode_bottom);
        mBottomToolbar.getMenu().clear();
        EnhancedMenuInflater.inflate(getActivity().getMenuInflater(), mBottomToolbar.getMenu(),
                category);
        setupMenu();
        mBottomToolbar.startActionMode(new ActionModeCallback());
        mBottomToolbar.setOnMenuItemClickListener(this);

    }

    private void setupMenu() {
        Menu menu = mBottomToolbar.getMenu();
        mRenameItem = menu.findItem(R.id.action_edit);
        mInfoItem = menu.findItem(R.id.action_info);
        mArchiveItem = menu.findItem(R.id.action_archive);
        mFavItem = menu.findItem(R.id.action_fav);
        mExtractItem = menu.findItem(R.id.action_extract);
        mHideItem = menu.findItem(R.id.action_hide);
        mPermissionItem = menu.findItem(R.id.action_permissions);
        // Dont show Fav and Archive option for Non file mode
        if (!category.equals(FILES)) {
            mArchiveItem.setVisible(false);
            mFavItem.setVisible(false);
            mHideItem.setVisible(false);
        }

        switch (currentTheme) {
            case LIGHT:
                mInfoItem.setIcon(R.drawable.ic_info_black);
                mArchiveItem.setIcon(R.drawable.ic_archive_black);
                mExtractItem.setIcon(R.drawable.ic_extract_black);
                mPermissionItem.setIcon(R.drawable.ic_permissions_black);
                mFavItem.setIcon(R.drawable.ic_favorite_black);
                break;
            case DARK:
                break;
        }

    }

    private void setupPasteMenu() {
        Menu menu = mBottomToolbar.getMenu();
        mPasteItem = menu.findItem(R.id.action_paste);
        createItem = menu.findItem(R.id.action_create);
        cancelItem = menu.findItem(R.id.action_cancel);

    }

    private void setupMenuVisibility() {
        Log.d(TAG, "setupMenuVisibility: " + mSelectedItemPositions.size());
        if (mSelectedItemPositions.size() > 1) {
            mRenameItem.setVisible(false);
            mInfoItem.setVisible(false);

        } else {
            mRenameItem.setVisible(true);
            mInfoItem.setVisible(true);
            if (mSelectedItemPositions.size() == 1) {


                boolean isDirectory = fileInfoList.get(mSelectedItemPositions.keyAt(0))
                        .isDirectory();
                String filePath = fileInfoList.get(mSelectedItemPositions.keyAt(0))
                        .getFilePath();

                boolean isRoot = fileInfoList.get(mSelectedItemPositions.keyAt(0)).isRootMode();
                if (FileUtils.isFileCompressed(filePath)) {
                    mExtractItem.setVisible(true);
                    mArchiveItem.setVisible(false);
                }
                if (isRoot) {
                    mPermissionItem.setVisible(true);
                }
                if (!isDirectory) {
                    mFavItem.setVisible(false);
                }

            }
            String fileName = fileInfoList.get(mSelectedItemPositions.keyAt(0)).getFileName();

            if (fileName.startsWith(".")) {
                mHideItem.setTitle(getString(R.string.unhide));
                if (currentTheme.equals(Themes.DARK)) {
                    mHideItem.setIcon(R.drawable.ic_unhide_white);
                } else {
                    mHideItem.setIcon(R.drawable.ic_unhide_black);

                }
            } else {
                mHideItem.setTitle(getString(hide));
                if (currentTheme.equals(Themes.DARK)) {
                    mHideItem.setIcon(R.drawable.ic_hide_white);
                } else {
                    mHideItem.setIcon(R.drawable.ic_hide_black);
                }
            }
        }
    }


    private ActionMode getActionMode() {
        return actionMode;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_cut:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    FileUtils.showMessage(getActivity(), mSelectedItemPositions.size() + " " +
                            getString(R.string.msg_cut_copy));
                    mCopiedData.clear();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        mCopiedData.add(fileInfoList.get(mSelectedItemPositions.keyAt(i)));
                    }
                    isPasteVisible = true;
                    mIsMoveOperation = true;
                    showPasteIcon();
                }
                break;
            case R.id.action_copy:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    mIsMoveOperation = false;
                    FileUtils.showMessage(getActivity(), mSelectedItemPositions.size() + " " + getString(R.string
                            .msg_cut_copy));
                    mCopiedData.clear();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        mCopiedData.add(fileInfoList.get(mSelectedItemPositions.keyAt(i)));
                    }
                    isPasteVisible = true;
                    showPasteIcon();
                }
                break;

            case R.id.action_paste:
                if (mCopiedData.size() > 0) {
                    ArrayList<FileInfo> info = new ArrayList<>();
                    info.addAll(mCopiedData);
                    PasteConflictChecker conflictChecker = new PasteConflictChecker(this, currentDir,
                            mIsRootMode, mIsMoveOperation, info);
                    conflictChecker.execute();
                    clearSelectedPos();
                    mCopiedData.clear();
                    endActionMode();
                }
                break;

            case R.id.action_create:
                new Dialogs().createDirDialog(this, mIsRootMode, currentDir);
                break;

            case R.id.action_delete:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    ArrayList<FileInfo> filesToDelete = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                        filesToDelete.add(info);
                    }
                    if (category.equals(FAVORITES)) {
                        removeFavorite(filesToDelete);
                        Toast.makeText(getContext(), getString(R.string.fav_removed), Toast.LENGTH_SHORT).show();
                    } else {
                        dialogs.showDeleteDialog(this, filesToDelete, RootUtils.isRooted(getContext()));
                    }
                    actionMode.finish();
                }
                break;
            case R.id.action_share:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    ArrayList<FileInfo> filesToShare = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                        if (!info.isDirectory()) {
                            filesToShare.add(info);
                        }
                    }
                    ShareHelper.shareFiles(getActivity(), filesToShare, category);
                    actionMode.finish();
                }
                break;

            case R.id.action_edit:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    final String oldFilePath = fileInfoList.get(mSelectedItemPositions.keyAt(0)).
                            getFilePath();
                    int renamedPosition = mSelectedItemPositions.keyAt(0);
                    String newFilePath = new File(oldFilePath).getParent();
                    renameDialog(oldFilePath, newFilePath, renamedPosition);
                    actionMode.finish();
                }
                break;

            case R.id.action_info:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    FileInfo fileInfo = fileInfoList.get(mSelectedItemPositions.keyAt(0));
                    showInfoDialog(fileInfo);
                    actionMode.finish();
                }
                break;
            case R.id.action_archive:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    ArrayList<FileInfo> paths = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                        paths.add(info);
                    }
                    dialogs.showCompressDialog(BaseFileList.this, currentDir, paths);
                    actionMode.finish();
                }
                break;
            case R.id.action_fav:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    int count = 0;
                    ArrayList<FileInfo> favList = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                        // Fav option meant only for directories
                        if (info.isDirectory()) {
                            favList.add(info);
//                                updateFavouritesGroup(info);
                            count++;
                        }
                    }


                    if (count > 0) {
                        FileUtils.showMessage(getActivity(), getString(R.string.msg_added_to_fav));
                        updateFavouritesGroup(favList);
                    }
                    actionMode.finish();
                }
                break;

            case R.id.action_extract:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    FileInfo fileInfo = fileInfoList.get(mSelectedItemPositions.keyAt(0));
                    String currentFile = fileInfo.getFilePath();
                    showExtractOptions(currentFile, currentDir);
                    actionMode.finish();
                }

                break;

            case R.id.action_hide:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    ArrayList<FileInfo> infoList = new ArrayList<>();
                    ArrayList<Integer> pos = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        infoList.add(fileInfoList.get(mSelectedItemPositions.keyAt(i)));
                        pos.add(mSelectedItemPositions.keyAt(i));

                    }
                    hideUnHideFiles(infoList, pos);
                    actionMode.finish();
                }
                break;

            case R.id.action_permissions:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(0));
                    showPermissionsDialog(info);
                    actionMode.finish();
                }
                break;


            case R.id.action_view:
                if (viewMode == ViewMode.LIST) {
                    viewMode = ViewMode.GRID;
                } else {
                    viewMode = ViewMode.LIST;
                }
                sharedPreferenceWrapper.savePrefs(getActivity(), viewMode);
                switchView();
                updateMenuTitle();
                break;

            case R.id.action_sort:
                showSortDialog();
                break;
        }
        return false;
    }

    /**
     * Triggered on long press click on item
     */
    private final class ActionModeCallback implements ActionMode.Callback {


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mBottomToolbar.setVisibility(View.VISIBLE);
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
                        } else {
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
            hidePasteIcon();
            mBottomToolbar.setVisibility(View.GONE);
            mSelectedItemPositions = new SparseBooleanArray();
            mSwipeRefreshLayout.setEnabled(true);
            draggedData.clear();
            // FAB should be visible only for Files Category
            if (isFilesCategory()) {
                showFab();
            }
        }
    }

    private boolean isFilesCategory() {
        return category.equals(FILES);
    }

    public void endActionMode() {
        isInSelectionMode = false;
        if (actionMode != null) {
            actionMode.finish();
        }
        actionMode = null;
        mBottomToolbar.setVisibility(View.GONE);
        mSelectedItemPositions = new SparseBooleanArray();
        mSwipeRefreshLayout.setEnabled(true);
        draggedData.clear();
    }

    public boolean isInSelectionMode() {
        return isInSelectionMode;
    }

    public boolean isSearchVisible() {
        return searchView.isExpanded();
    }

    @SuppressWarnings("ConstantConditions")
    private void renameDialog(final String oldFilePath, final String newFilePath, final int
            position) {
        String fileName = oldFilePath.substring(oldFilePath.lastIndexOf("/") + 1, oldFilePath.length());
        boolean file = false;
        String extension = null;
        if (new File(oldFilePath).isFile()) {
            String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
            fileName = tokens[0];
            extension = tokens[1];
            file = true;
        }
        final boolean isFile = file;
        final String ext = extension;

        String title = getString(R.string.action_rename);
        String texts[] = new String[]{"", fileName, title, title, "",
                getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new Dialogs().showEditDialog(getActivity(), texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }


                fileName = fileName.trim();
                String renamedName = fileName;
                if (isFile) {
                    renamedName = fileName + "." + ext;
                }

                File newFile = new File(newFilePath + "/" + renamedName);
                if (FileUtils.isFileExisting(newFilePath, newFile.getName())) {
                    materialDialog.getInputEditText().setError(getResources().getString(R.string
                            .dialog_title_paste_conflict));
                    return;
                }
                File oldFile = new File(oldFilePath);
                fileOpHelper.renameFile(oldFile, newFile, position, mIsRootMode);
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
        actionMode.finish();
    }


    private void hideUnHideFiles(ArrayList<FileInfo> fileInfo, ArrayList<Integer> pos) {
        for (int i = 0; i < fileInfo.size(); i++) {
            String fileName = fileInfo.get(i).getFileName();
            String renamedName;
            if (fileName.startsWith(".")) {
                renamedName = fileName.substring(1);
            } else {
                renamedName = "." + fileName;
            }
            String path = fileInfo.get(i).getFilePath();
            File oldFile = new File(path);
            String temp = path.substring(0, path.lastIndexOf(File.separator));

            File newFile = new File(temp + File.separator + renamedName);
            fileOpHelper.renameFile(oldFile, newFile, pos.get(i), mIsRootMode);
        }
    }


    private void showPermissionsDialog(final FileInfo fileInfo) {

        String texts[] = new String[]{getString(R.string.permissions), getString(R.string.msg_ok),
                "", getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new Dialogs().showCustomDialog(getActivity(),
                R.layout.dialog_permission, texts);
        final CheckBox readown = (CheckBox) materialDialog.findViewById(R.id.creadown);
        final CheckBox readgroup = (CheckBox) materialDialog.findViewById(R.id.creadgroup);
        final CheckBox readother = (CheckBox) materialDialog.findViewById(R.id.creadother);
        final CheckBox writeown = (CheckBox) materialDialog.findViewById(R.id.cwriteown);
        final CheckBox writegroup = (CheckBox) materialDialog.findViewById(R.id.cwritegroup);
        final CheckBox writeother = (CheckBox) materialDialog.findViewById(R.id.cwriteother);
        final CheckBox exeown = (CheckBox) materialDialog.findViewById(R.id.cexeown);
        final CheckBox exegroup = (CheckBox) materialDialog.findViewById(R.id.cexegroup);
        final CheckBox exeother = (CheckBox) materialDialog.findViewById(R.id.cexeother);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String perm = RootHelper.getPermissions(fileInfo.getFilePath(), fileInfo.isDirectory());
                ArrayList<Boolean[]> arrayList = parse(perm);
                final Boolean[] read = arrayList.get(0);
                final Boolean[] write = arrayList.get(1);
                final Boolean[] exe = arrayList.get(2);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readown.setChecked(read[0]);
                        readgroup.setChecked(read[1]);
                        readother.setChecked(read[2]);
                        writeown.setChecked(write[0]);
                        writegroup.setChecked(write[1]);
                        writeother.setChecked(write[2]);
                        exeown.setChecked(exe[0]);
                        exegroup.setChecked(exe[1]);
                        exeother.setChecked(exe[2]);
                    }
                });
            }
        };

        new Thread(runnable).start();

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                int a = 0, b = 0, c = 0;
                if (readown.isChecked()) a = 4;
                if (writeown.isChecked()) b = 2;
                if (exeown.isChecked()) c = 1;
                int owner = a + b + c;
                int d = 0, e = 0, f = 0;
                if (readgroup.isChecked()) d = 4;
                if (writegroup.isChecked()) e = 2;
                if (exegroup.isChecked()) f = 1;
                int group = d + e + f;
                int g = 0, h = 0, i = 0;
                if (readother.isChecked()) g = 4;
                if (writeother.isChecked()) h = 2;
                if (exeother.isChecked()) i = 1;
                int other = g + h + i;
                String finalValue = owner + "" + group + "" + other;

                String command = "chmod " + finalValue + " " + fileInfo.getFilePath();
                if (fileInfo.isDirectory())
                    command = "chmod -R " + finalValue + " \"" + fileInfo.getFilePath() + "\"";
                Command com = new Command(1, command) {
                    @Override
                    public void commandOutput(int i, String s) {
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void commandTerminated(int i, String s) {
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void commandCompleted(int i, int i2) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.completed), Toast
                                .LENGTH_LONG).show();
                    }
                };
                try {
                    RootUtils.mountRW(fileInfo.getFilePath());
                    RootTools.getShell(true).add(com);
                    RootUtils.mountRO(fileInfo.getFilePath());
                    refreshList();
                } catch (Exception e1) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error), Toast.LENGTH_LONG)
                            .show();
                    e1.printStackTrace();
                }

            }
        });
        materialDialog.show();

    }


    @SuppressWarnings("ConstantConditions")
    private void showExtractOptions(final String currentFilePath, final String currentDir) {

        mSelectedPath = null;
        final String currentFileName = currentFilePath.substring(currentFilePath.lastIndexOf("/")
                + 1, currentFilePath.lastIndexOf("."));
        String texts[] = new String[]{getString(R.string.extract), getString(R.string.extract),
                "", getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new Dialogs().showCustomDialog(getActivity(),
                R.layout.dialog_extract, texts);

        final RadioButton radioButtonSpecify = (RadioButton) materialDialog.findViewById(R.id
                .radioButtonSpecifyPath);
        buttonPathSelect = (Button) materialDialog.findViewById(R.id.buttonPathSelect);
        RadioGroup radioGroupPath = (RadioGroup) materialDialog.findViewById(R.id.radioGroupPath);
        final EditText editFileName = (EditText) materialDialog.findViewById(R.id.editFileName);
        editFileName.setText(currentFileName);
        radioGroupPath.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radioButtonCurrentPath) {
                    buttonPathSelect.setVisibility(View.GONE);
                } else {
                    buttonPathSelect.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonPathSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
                dialogFragment.setTargetFragment(BaseFileList.this, DIALOG_FRAGMENT);
                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, getThemeStyle());
                dialogFragment.show(getFragmentManager(), "Browse Fragment");
            }
        });

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = editFileName.getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    editFileName.setError(getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }
                if (radioButtonSpecify.isChecked()) {
                    File newFile = new File(mSelectedPath + "/" + currentFileName);
                    File currentFile = new File(currentFilePath);
                    if (FileUtils.isFileExisting(mSelectedPath, newFile.getName())) {
                        editFileName.setError(getResources().getString(R.string
                                .dialog_title_paste_conflict));
                        return;
                    }
                    fileOpHelper.extractFile(currentFile, newFile);
                } else {
                    File newFile = new File(currentDir + "/" + fileName);
                    File currentFile = new File(currentFilePath);
                    if (FileUtils.isFileExisting(currentDir, newFile.getName())) {
                        editFileName.setError(getResources().getString(R.string
                                .dialog_title_paste_conflict));
                        return;
                    }
                    fileOpHelper.extractFile(currentFile, newFile);
                }
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

    private int getThemeStyle() {
        switch (currentTheme) {
            case DARK:
                return R.style.DarkAppTheme_NoActionBar;
            case LIGHT:
                return R.style.AppTheme_NoActionBar;
        }
        return R.style.DarkAppTheme_NoActionBar;

    }


    private void updateFavouritesGroup(ArrayList<FileInfo> fileInfoList) {
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

        aceActivity.updateFavourites(favInfoArrayList);
    }

    private void removeFavorite(ArrayList<FileInfo> fileInfoList) {
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
        aceActivity.removeFavourites(favInfoArrayList);
    }


    private void showPasteIcon() {
        mBottomToolbar.setVisibility(View.VISIBLE);
        mBottomToolbar.getMenu().clear();
        mBottomToolbar.inflateMenu(R.menu.action_mode_paste);
/*        EnhancedMenuInflater.inflate(getActivity().getMenuInflater(), mBottomToolbar.getMenu(),
                category);*/
        setupPasteMenu();
/*        fabCreateMenu.setVisibility(View.GONE);
        fabOperation.setVisibility(View.VISIBLE);*/
    }

    private void hidePasteIcon() {
   /*     fabOperation.setVisibility(View.GONE);
        fabCreateMenu.setVisibility(View.VISIBLE);*/
    }


    @SuppressWarnings("ConstantConditions")
    private void showInfoDialog(FileInfo fileInfo) {
        String title = getString(R.string.properties);
        String texts[] = new String[]{title, getString(R.string.msg_ok), "", null};
        final MaterialDialog materialDialog = new Dialogs().showCustomDialog(getActivity(),
                R.layout.dialog_file_properties, texts);
        View view = materialDialog.getCustomView();
        ImageView imageFileIcon = (ImageView) view.findViewById(R.id.imageFileIcon);
        TextView textFileName = (TextView) view.findViewById(R.id.textFileName);
        TextView textPath = (TextView) view.findViewById(R.id.textPath);
        TextView textFileSize = (TextView) view.findViewById(R.id.textFileSize);
        TextView textDateModified = (TextView) view.findViewById(R.id.textDateModified);
        TextView textHidden = (TextView) view.findViewById(R.id.textHidden);
        TextView textReadable = (TextView) view.findViewById(R.id.textReadable);
        TextView textWriteable = (TextView) view.findViewById(R.id.textWriteable);
        TextView textHiddenPlaceHolder = (TextView) view.findViewById(R.id.textHiddenPlaceHolder);
        TextView textReadablePlaceHolder = (TextView) view.findViewById(R.id
                .textReadablePlaceHolder);
        TextView textWriteablePlaceHolder = (TextView) view.findViewById(R.id
                .textWriteablePlaceHolder);
        TextView textMD5 = (TextView) view.findViewById(R.id.textMD5);
        TextView textMD5Placeholder = (TextView) view.findViewById(R.id.textMD5PlaceHolder);

        String path = fileInfo.getFilePath();
        String fileName = fileInfo.getFileName();
        String fileDate;
        if (Category.checkIfFileCategory(category)) {
            fileDate = FileUtils.convertDate(fileInfo.getDate());
        } else {
            fileDate = FileUtils.convertDate(fileInfo.getDate() * 1000);
        }
        boolean isDirectory = fileInfo.isDirectory();
        String fileNoOrSize;
        if (isDirectory) {
            int childFileListSize = (int) fileInfo.getSize();
            if (childFileListSize == 0) {
                fileNoOrSize = getResources().getString(R.string.empty);
            } else if (childFileListSize == -1) {
                fileNoOrSize = "";
            } else {
                fileNoOrSize = getResources().getQuantityString(R.plurals.number_of_files,
                        childFileListSize, childFileListSize);
            }
        } else {
            long size = fileInfo.getSize();
            fileNoOrSize = Formatter.formatFileSize(getActivity(), size);
        }
        boolean isReadable = new File(path).canRead();
        boolean isWriteable = new File(path).canWrite();
        boolean isHidden = new File(path).isHidden();

        textFileName.setText(fileName);
        textPath.setText(path);
        textFileSize.setText(fileNoOrSize);
        textDateModified.setText(fileDate);

        if (!isFilesCategory()) {
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

        if (new File(path).isDirectory()) {
            textMD5.setVisibility(View.GONE);
            textMD5Placeholder.setVisibility(View.GONE);
            Drawable apkIcon = getAppIconForFolder(getActivity(), fileName);
            if (apkIcon != null) {
                imageFileIcon.setImageDrawable(apkIcon);
            } else {
                imageFileIcon.setImageResource(R.drawable.ic_folder);
            }
        } else {
            if (isFilesCategory()) {
                String md5 = FileUtils.getFastHash(path);
                textMD5.setText(md5);
            }

            if (fileInfo.getType() == VIDEO.getValue()) {
                Uri videoUri = Uri.fromFile(new File(path));
                Glide.with(getActivity()).load(videoUri).centerCrop()
                        .placeholder(R.drawable.ic_movie)
                        .crossFade(2)
                        .into(imageFileIcon);
            } else if (fileInfo.getType() == IMAGE.getValue()) {
                Uri imageUri = Uri.fromFile(new File(path));
                Glide.with(getActivity()).load(imageUri).centerCrop()
                        .crossFade(2)
                        .placeholder(R.drawable.ic_image_default)
                        .into(imageFileIcon);
            } else if (fileInfo.getType() == AUDIO.getValue()) {
                imageFileIcon.setImageResource(R.drawable.ic_music_default);
            } else if (fileInfo.getExtension().equals(FileConstants.APK_EXTENSION)) {
                Drawable apkIcon = getAppIcon(getActivity(), path);
                imageFileIcon.setImageDrawable(apkIcon);
            } else {
                imageFileIcon.setImageResource(R.drawable.ic_doc_white);
            }
        }

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();

    }


    private BitmapDrawable writeOnDrawable(String text) {

        Bitmap bm = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        bm.eraseColor(Color.DKGRAY);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        int countFont = getResources()
                .getDimensionPixelSize(R.dimen.drag_shadow_font);
        paint.setTextSize(countFont);

        Canvas canvas = new Canvas(bm);
        int strLength = (int) paint.measureText(text);
        int x = bm.getWidth() / 2 - strLength;

        // int y = s.titleOffset;
        int y = (bm.getHeight() - countFont) / 2;
//        drawText(canvas, x, y, title, labelWidth - s.leftMargin - x
//                - s.titleRightMargin, mTitlePaint);

        canvas.drawText(text, x, y - paint.getFontMetricsInt().ascent, paint);
//        canvas.drawText(text, bm.getWidth() / 2, bm.getHeight() / 2, paint);

        return new BitmapDrawable(getActivity().getResources(), bm);
    }


    private class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private final Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        MyDragShadowBuilder(View v, int count) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);
            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = writeOnDrawable("" + count);

        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth() / 6;
//            width = 100;
            Log.d(TAG, "width=" + width);

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() / 2;
//            height = 100;

            Log.d(TAG, "height=" + height);


            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);
            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(2 * width, height * 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }

    private void showDragDialog(final ArrayList<FileInfo> sourcePaths, final String destinationDir) {

        int color = new Dialogs().getCurrentThemePrimary(getActivity());
        boolean canWrite = new File(destinationDir).canWrite();
        Logger.log(TAG, "Can write=" + canWrite);

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        CharSequence items[] = new String[]{getString(R.string.action_copy), getString(R.string.move)};
        builder.title(getString(R.string.drag));
        builder.content(getString(R.string.dialog_to_placeholder) + " " + destinationDir);
        builder.positiveText(getString(R.string.msg_ok));
        builder.positiveColor(color);
        builder.items(items);
        builder.negativeText(getString(R.string.dialog_cancel));
        builder.negativeColor(color);
        builder.itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {

                final boolean isMoveOperation = position == 1;
                ArrayList<FileInfo> info = new ArrayList<>();
                info.addAll(sourcePaths);
                PasteConflictChecker conflictChecker = new PasteConflictChecker(BaseFileList.this, destinationDir,
                        mIsRootMode, isMoveOperation, info);
                conflictChecker.execute();
                clearSelectedPos();
                if (actionMode != null) {
                    actionMode.finish();
                }
                return true;
            }
        });

        final MaterialDialog materialDialog = builder.build();

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionMode != null)
                    actionMode.finish();
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    private class myDragEventListener implements View.OnDragListener {

        int oldPos = -1;

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View v, DragEvent event) {

            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();

            // Handles each of the expected events
            switch (action) {

                case DragEvent.ACTION_DRAG_STARTED:

                    Log.d(TAG, "DRag started" + v);

                    // Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {

                        // returns true to indicate that the View can accept the dragged data.
                        return true;

                    }

                    // Returns false. During the current drag and drop operation, this View will
                    // not receive events again until ACTION_DRAG_ENDED is sent.
                    return false;

                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "DRag entered");
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    View onTopOf = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int newPos = recyclerViewFileList.getChildAdapterPosition(onTopOf);
//                    Log.d(TAG, "DRag location --pos=" + newPos);

                    if (oldPos != newPos && newPos != RecyclerView.NO_POSITION) {
/*                        int visiblePos = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                        if (newPos + 2 >= visiblePos) {
                            ((LinearLayoutManager) layoutManager).scrollToPosition(newPos + 1);
                        }
//                        recyclerViewFileList.smoothScrollToPosition(newPos+2);
                        Logger.log(TAG, "drag old pos=" + oldPos + "new pos=" + newPos+"Last " +
                                "visible="+visiblePos);*/
                        // For scroll up
                        if (oldPos != RecyclerView.NO_POSITION && newPos < oldPos) {
                            int changedPos = newPos - 2;
                            Logger.log(TAG, "drag Location old pos=" + oldPos + "new pos=" + newPos +
                                    "changed pos=" + changedPos);
                            if (changedPos >= 0)
                                recyclerViewFileList.smoothScrollToPosition(changedPos);
                        } else {
                            int changedPos = newPos + 2;
                            // For scroll down
                            if (changedPos < fileInfoList.size())
                                recyclerViewFileList.smoothScrollToPosition(newPos + 2);
                            Logger.log(TAG, "drag Location old pos=" + oldPos + "new pos=" + newPos +
                                    "changed pos=" + changedPos);

                        }
                        oldPos = newPos;
                        fileListAdapter.setDraggedPos(newPos);
                    }
                    // Ignore the event
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "DRag exit");
                    fileListAdapter.clearDragPos();
                    draggedData = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DROP:
//                    Log.d(TAG,"DRag drop"+pos);

                    View top = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position = recyclerViewFileList.getChildAdapterPosition(top);
                    Logger.log(TAG, "DROP new pos=" + position);
                    fileListAdapter.clearDragPos();
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
                                    destinationDir);
                            showDragDialog(draggedFiles, destinationDir);
                        } else {
                            final boolean isMoveOperation = false;
                            ArrayList<FileInfo> info = new ArrayList<>();
                            info.addAll(draggedFiles);
                            PasteConflictChecker conflictChecker = new PasteConflictChecker(BaseFileList.this,
                                    destinationDir, mIsRootMode, isMoveOperation, info);
                            conflictChecker.execute();
                            clearSelectedPos();
                            Logger.log(TAG, "Source=" + draggedFiles.get(0) + "Dest=" + destinationDir);
                            actionMode.finish();
                        }
                    }

                    draggedData = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    View top1 = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position1 = recyclerViewFileList.getChildAdapterPosition(top1);
                    @SuppressWarnings("unchecked")
                    ArrayList<FileInfo> dragPaths = (ArrayList<FileInfo>) event.getLocalState();


                    Logger.log(TAG, "DRAG END new pos=" + position1);
                    Logger.log(TAG, "DRAG END Local state=" + dragPaths);
                    Logger.log(TAG, "DRAG END result=" + event.getResult());
                    Logger.log(TAG, "DRAG END currentDirSingle=" + mLastSinglePaneDir);
                    Log.d(TAG, "DRag end");
                    fileListAdapter.clearDragPos();
                    if (!event.getResult() && position1 == RecyclerView.NO_POSITION) {
                        ViewParent parent1 = v.getParent().getParent();

                        if (((View) parent1).getId() == R.id.frame_container_dual) {
                            Logger.log(TAG, "DRAG END parent dual =" + true);
/*                            FileListDualFragment dualPaneFragment = (FileListDualFragment)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.frame_container_dual);
                            Logger.log(TAG, "DRAG END Dual dir=" + mLastDualPaneDir);

//                            Logger.log(TAG, "Source=" + draggedData.get(0) + "Dest=" + mLastDualPaneDir);
                            if (dualPaneFragment != null && new File(mLastDualPaneDir).list().length == 0 &&
                                    dragPaths.size() != 0) {
//                                if (!destinationDir.equals(paths.get(0))) {
                                showDragDialog(dragPaths, mLastDualPaneDir);
//                                }
                            }*/
                        } else {
                            Logger.log(TAG, "DRAG END parent dual =" + false);
                            BaseFileList singlePaneFragment = (BaseFileList)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.main_container);
                            Logger.log(TAG, "DRAG END single dir=" + mLastSinglePaneDir);

//                            Logger.log(TAG, "Source=" + draggedData.get(0) + "Dest=" + mLastDualPaneDir);
                            if (singlePaneFragment != null && new File(mLastSinglePaneDir).list().length == 0 &&
                                    dragPaths.size() != 0) {
//                                if (!destinationDir.equals(paths.get(0))) {
                                showDragDialog(dragPaths, mLastSinglePaneDir);
//                                }
                            }
                        }

                    }
                    draggedData = new ArrayList<>();
                    // returns true; the value is ignored.
                    return true;

                // An unknown action type was received.
                default:
                    Log.e(TAG, "Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
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


    private void updateMenuTitle() {
        mViewItem.setTitle(viewMode == ViewMode.LIST ? R.string.action_view_grid : R.string.action_view_list);
    }

    public void hideSearchView() {
        searchView.enableSearch(false);
    }

    public void performVoiceSearch(String query) {
//        searchView.setQuery(query, false);
    }

//    private  SearchTask searchTask;

    @Override
    public boolean onQueryTextChange(String query) {

        fileListAdapter.filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
/*        if (!query.isEmpty()) {
            if (searchTask == null) {
                searchTask = new SearchTask(this,query,currentDir);

            }
            else {
                searchTask.execute(query);
            }
        }
        hideSearchView();*/
        return false;
    }


    private void clearSelectedPos() {
        mSelectedItemPositions = new SparseBooleanArray();
    }


    private void showSortDialog() {
        int color = new Dialogs().getCurrentThemePrimary(getActivity());

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        CharSequence items[] = new String[]{getString(R.string.sort_name), getString(R.string.sort_name_desc),
                getString(R.string.sort_type), getString(R.string.sort_type_desc),
                getString(R.string.sort_size), getString(R.string.sort_size_desc),
                getString(R.string.sort_date), getString(R.string.sort_date_desc)};
        builder.title(getString(R.string.action_sort));
        builder.positiveText(getString(R.string.dialog_cancel));
        builder.positiveColor(color);
        builder.items(items);

        builder.alwaysCallSingleChoiceCallback();
        builder.itemsCallbackSingleChoice(getSortMode(), new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                persistSortMode(position);
                refreshList();
                dialog.dismiss();
                return true;
            }
        });

        final MaterialDialog materialDialog = builder.build();
        materialDialog.show();
    }

    private void persistSortMode(int sortMode) {
        preferences.edit().putInt(FileConstants.KEY_SORT_MODE, sortMode).apply();
    }

    private int getSortMode() {
        return preferences.getInt(FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }

    private void switchView() {
        fileListAdapter = null;
        recyclerViewFileList.setHasFixedSize(true);

        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            recyclerViewFileList.setLayoutManager(layoutManager);

        } else {
            refreshSpan();
        }

        shouldStopAnimation = true;

        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, category, viewMode);
        recyclerViewFileList.setAdapter(fileListAdapter);
        if (viewMode == ViewMode.LIST) {
            if (mGridItemDecoration != null) {
                recyclerViewFileList.removeItemDecoration(mGridItemDecoration);
            }
            if (mDividerItemDecoration == null) {
                mDividerItemDecoration = new DividerItemDecoration(getActivity(), currentTheme);
            }
            mDividerItemDecoration.setOrientation();
            recyclerViewFileList.addItemDecoration(mDividerItemDecoration);
        } else {
            if (mDividerItemDecoration != null) {
                recyclerViewFileList.removeItemDecoration(mDividerItemDecoration);
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


    public void refreshSpan() {
        if (viewMode == ViewMode.GRID) {
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT || !mIsDualModeEnabled ||
                    checkIfLibraryCategory(category)) {
                gridCols = getResources().getInteger(R.integer.grid_columns);
            } else {
                gridCols = getResources().getInteger(R.integer.grid_columns_dual);
            }
            Log.d(TAG, "Refresh span--columns=" + gridCols + "category=" + category + " dual mode=" +
                    mIsDualModeEnabled);

            layoutManager = new CustomGridLayoutManager(getActivity(), gridCols);
            recyclerViewFileList.setLayoutManager(layoutManager);
        }
    }

    @Override
    public void onDestroyView() {
        recyclerViewFileList.stopScroll();
        if (!mInstanceStateExists) {
            preferences.edit().putInt(FileConstants.KEY_GRID_COLUMNS, gridCols).apply();
            sharedPreferenceWrapper.savePrefs(getActivity(), viewMode);
        }
        removeSearchTask();

        refreshData = null;

        if (fileListAdapter != null) {
            fileListAdapter.onDetach();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @SuppressWarnings("EmptyMethod")
    public void removeSearchTask() {

     /*   if (searchTask != null) {
            searchTask.searchAsync.cancel(true);
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        aceActivity = null;
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        Logger.log(TAG, "onConfigurationChanged " + newConfig.orientation);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            refreshSpan();
        }
    }
}
