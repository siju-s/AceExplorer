package com.siju.acexplorer.filesystem;

import android.annotation.SuppressLint;
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
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.style.ImageSpan;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.model.BackStackModel;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipModel;
import com.siju.acexplorer.filesystem.task.ExtractZipEntry;
import com.siju.acexplorer.filesystem.task.PasteConflictChecker;
import com.siju.acexplorer.filesystem.ui.CustomGridLayoutManager;
import com.siju.acexplorer.filesystem.ui.CustomLayoutManager;
import com.siju.acexplorer.filesystem.ui.DialogBrowseFragment;
import com.siju.acexplorer.filesystem.ui.DividerItemDecoration;
import com.siju.acexplorer.filesystem.ui.EnhancedMenuInflater;
import com.siju.acexplorer.filesystem.ui.GridItemDecoration;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;
import com.siju.acexplorer.filesystem.views.FastScrollRecyclerView;
import com.siju.acexplorer.utils.DialogUtils;
import com.siju.acexplorer.utils.Utils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FileListFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<ArrayList<FileInfo>>,
        SearchView.OnQueryTextListener,
        Toolbar.OnMenuItemClickListener {

    private final String TAG = this.getClass().getSimpleName();
    private FastScrollRecyclerView recyclerViewFileList;
    private View root;
    private final int LOADER_ID = 1000;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private String mFilePath;
    private String mFilePathOther;
    private int mCategory;
    private int mViewMode = FileConstants.KEY_LISTVIEW;
    private boolean mIsZip;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private TextView mTextEmpty;
    private boolean mIsDualActionModeActive;
    private boolean mIsDualModeEnabled;
    private MenuItem mViewItem;
    private SearchView mSearchView;
    private boolean isDragStarted;
    private long mLongPressedTime;
    private View mItemView;
    private ArrayList<FileInfo> mDragPaths = new ArrayList<>();
    private RecyclerView.LayoutManager llm;
    private String mLastDualPaneDir;
    private String mLastSinglePaneDir;
    private View viewDummy;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mZipParentPath;
    private String mChildZipPath;
    private boolean inChildZip;
    private BaseActivity mBaseActivity;
    private boolean isDualPaneInFocus;
    private Toolbar mBottomToolbar;
    private ActionMode mActionMode;
    private SparseBooleanArray mSelectedItemPositions = new SparseBooleanArray();
    private MenuItem mPasteItem;
    private MenuItem mRenameItem;
    private MenuItem mInfoItem;
    private MenuItem mArchiveItem;
    private MenuItem mFavItem;
    private MenuItem mExtractItem;
    private MenuItem mHideItem;
    private MenuItem mPermissionItem;
    private boolean mIsMoveOperation = false;
    private String mCurrentZipDir;
    public ArrayList<ZipModel> totalZipList = new ArrayList<>();
    public ArrayList<ZipModel> zipChildren = new ArrayList<>();
    public Archive mArchive;
    public final ArrayList<FileHeader> totalRarList = new ArrayList<>();
    private final ArrayList<FileHeader> rarChildren = new ArrayList<>();
    private boolean mInParentZip = true;
    private int mParentZipCategory;
    private boolean mIsPasteItemVisible;
    private String mSelectedPath;
    private Button buttonPathSelect;
    private final HashMap<String, Bundle> scrollPosition = new HashMap<>();
    private final HashMap<String, Bundle> scrollPositionDualPane = new HashMap<>();
    private int mGridColumns;
    private SharedPreferences mPreferences;
    private int mCurrentOrientation;
    private final ArrayList<FileInfo> mCopiedData = new ArrayList<>();
    private final boolean mIsRootMode = true;
    private boolean mIsSwipeRefreshed;
    private FileUtils mFileUtils;
    private boolean mStopAnim = true;
    private boolean mIsBackPressed;
    private DividerItemDecoration mDividerItemDecoration;
    private GridItemDecoration mGridItemDecoration;
    private boolean mIsDarkTheme;
    private boolean mInstanceStateExists;
    private final int DIALOG_FRAGMENT = 5000;
    private boolean clearCache;
    private ZipEntry zipEntry = null;
    private String zipEntryFileName;
    private boolean setRefreshSpan;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBaseActivity = (BaseActivity) context;
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.file_list, container, false);
        return root;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        initializeViews();

        if (savedInstanceState == null) {
            mCurrentOrientation = getResources().getConfiguration().orientation;
            mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mIsDualModeEnabled = mPreferences
                    .getBoolean(FileConstants.PREFS_DUAL_PANE, false);
            mGridColumns = mPreferences.getInt(FileConstants.KEY_GRID_COLUMNS, 0);
            mIsDarkTheme = ThemeUtils.isDarkTheme(getActivity());

            if (getArguments() != null) {
                if (getArguments().getString(FileConstants.KEY_PATH) != null) {
                    mFilePath = getArguments().getString(FileConstants.KEY_PATH);
                    mFilePathOther = getArguments().getString(FileConstants.KEY_PATH_OTHER);
                }
                mCategory = getArguments().getInt(FileConstants.KEY_CATEGORY, FileConstants.CATEGORY.FILES.getValue());
                if (FileUtils.checkIfLibraryCategory(mCategory)) {
                    mBaseActivity.hideFab();
                    mBaseActivity.addHomeNavButton(false);
                } else {
                    mBaseActivity.showFab();
                }
                mBaseActivity.toggleNavigationVisibility(true);

                mIsZip = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
                boolean isDualPaneInFocus = getArguments().getBoolean(FileConstants.KEY_FOCUS_DUAL, false);
                if (isDualPaneInFocus) {
                    mLastDualPaneDir = mFilePath;
                    mLastSinglePaneDir = mFilePathOther;
                    Log.d(TAG, "on onActivityCreated dual focus Yes--singledir" + mLastSinglePaneDir + "dualDir=" +
                            mLastDualPaneDir);

                } else {
                    mLastSinglePaneDir = mFilePath;
                    mLastDualPaneDir = mFilePathOther;
                    Log.d(TAG, "on onActivityCreated dual focus No--singledir" + mLastSinglePaneDir + "dualDir=" +
                            mLastDualPaneDir);
                }

            }
            mViewMode = sharedPreferenceWrapper.getViewMode(getActivity());

            Log.d(TAG, "on onActivityCreated--Path" + mFilePath);
            Log.d(TAG, "View mode=" + mViewMode);
            Bundle args = new Bundle();
            args.putString(FileConstants.KEY_PATH, mFilePath);
            recyclerViewFileList.setHasFixedSize(true);

            if (mViewMode == FileConstants.KEY_LISTVIEW) {
                llm = new CustomLayoutManager(getActivity());
                recyclerViewFileList.setLayoutManager(llm);
            } else {
                refreshSpan();
            }
            recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
            fileListAdapter = new FileListAdapter(getContext(), fileInfoList,
                    mCategory, mViewMode);
            isDualPaneInFocus = checkIfDualFragment();
            if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                mBaseActivity.setNavDirectory(mFilePath, isDualPaneInFocus);
            }
            getLoaderManager().initLoader(LOADER_ID, args, this);
            initializeListeners();
        } else {
            mInstanceStateExists = true;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mInstanceStateExists) {
            IntentFilter intentFilter = new IntentFilter(FileConstants.RELOAD_LIST);
            intentFilter.addAction(FileConstants.REFRESH);
            getActivity().registerReceiver(mReloadListReceiver, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.log(TAG, "OnPause");
        if (!mInstanceStateExists) {
            getActivity().unregisterReceiver(mReloadListReceiver);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    mSelectedPath = data.getStringExtra("PATH");
                    if (buttonPathSelect != null) {
                        buttonPathSelect.setText(mSelectedPath);
                    }
                }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addItemDecoration() {

        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            if (mDividerItemDecoration == null) {
                mDividerItemDecoration = new DividerItemDecoration(getActivity(), mIsDarkTheme);
            } else {
                recyclerViewFileList.removeItemDecoration(mDividerItemDecoration);
            }
            recyclerViewFileList.addItemDecoration(mDividerItemDecoration);
        } else {
            Drawable divider;
            if (mIsDarkTheme) {
                divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider_line_dark);
            } else {
                divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider_line);
            }
            if (mGridItemDecoration == null) {
                mGridItemDecoration = new GridItemDecoration(divider, divider, mGridColumns);
            } else {
                recyclerViewFileList.removeItemDecoration(mGridItemDecoration);
            }
            recyclerViewFileList.addItemDecoration(mGridItemDecoration);
        }
    }

    private void initializeViews() {
        recyclerViewFileList = (FastScrollRecyclerView) root.findViewById(R.id.recyclerViewFileList);
        mTextEmpty = (TextView) root.findViewById(R.id.textEmpty);
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        recyclerViewFileList.setOnDragListener(new myDragEventListener());
        viewDummy = root.findViewById(R.id.viewDummy);
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        int colorResIds[] = {R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorPrimaryDark};
        mSwipeRefreshLayout.setColorSchemeResources(colorResIds);
        mSwipeRefreshLayout.setDistanceToTriggerSync(500);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mIsSwipeRefreshed = true;
                refreshList();
            }
        });
        mBottomToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_bottom);
        mFileUtils = new FileUtils();
    }

    private void initializeListeners() {
        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (getActionMode() != null) {
                    if (mIsDualActionModeActive) {
                        if (checkIfDualFragment()) {
                            itemClickActionMode(position, false);
                        } else {
                            handleCategoryItemClick(position);
                        }
                    } else {
                        if (checkIfDualFragment()) {
                            handleCategoryItemClick(position);
                        } else {
                            itemClickActionMode(position, false);
                        }
                    }
                } else {
                    handleCategoryItemClick(position);
                }
            }
        });
        fileListAdapter.setOnItemLongClickListener(new FileListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Logger.log(TAG, "On long click" + isDragStarted);
                if (!mIsZip) {
                    itemClickActionMode(position, true);
                    mLongPressedTime = System.currentTimeMillis();

                    if (getActionMode() != null && fileListAdapter
                            .getSelectedCount() >= 1) {
                        mSwipeRefreshLayout.setEnabled(false);
                        FileInfo fileInfo = fileInfoList.get(position);

                        if (!mDragPaths.contains(fileInfo)) {
                            mDragPaths.add(fileInfo);
                        }
                        mItemView = view;
                        isDragStarted = true;
                    }
                }
            }
        });


        recyclerViewFileList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    if (fileListAdapter != null && mStopAnim) {
                        stopAnimation();
                        mStopAnim = false;
                    }
                }
            }
        });

        recyclerViewFileList.setOnTouchListener(new View.OnTouchListener() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                Logger.log(TAG, "On touch listener" + mStopAnim);

                int event = motionEvent.getActionMasked();

                if (fileListAdapter != null && mStopAnim) {
                    stopAnimation();
                    mStopAnim = false;
                }

                if (isDragStarted && event == MotionEvent.ACTION_UP) {
                    isDragStarted = false;
                } else if (isDragStarted && event == MotionEvent.ACTION_MOVE && mLongPressedTime != 0) {
                    long timeElapsed = System.currentTimeMillis() - mLongPressedTime;
//                    Logger.log(TAG, "On item touch time Elapsed" + timeElapsed);

                    if (timeElapsed > 1000) {
                        mLongPressedTime = 0;
                        isDragStarted = false;
                        Logger.log(TAG, "On touch drag path size=" + mDragPaths.size());
                        if (mDragPaths.size() > 0) {
                            Intent intent = new Intent();
                            intent.putParcelableArrayListExtra(FileConstants.KEY_PATH, mDragPaths);
                            ClipData data = ClipData.newIntent("", intent);
                            int count = fileListAdapter
                                    .getSelectedCount();
                            View.DragShadowBuilder shadowBuilder = new MyDragShadowBuilder(mItemView,
                                    count);
                            if (Utils.isAtleastNougat()) {
                                view.startDragAndDrop(data, shadowBuilder, mDragPaths, 0);
                            } else {
                                view.startDrag(data, shadowBuilder, mDragPaths, 0);
                            }
                        }
                    }
                }
                return false;
            }
        });
    }


    public void openCompressedFile(String path) {
        computeScroll();

        mIsZip = true;
        mInParentZip = true;
        mCurrentZipDir = null;
        mZipParentPath = path;
        zipEntry = null;
        reloadList(false, path);
        mParentZipCategory = mCategory;
        isDualPaneInFocus = checkIfDualFragment();
        Logger.log(TAG, "Opencompressedfile--mCategory" + mCategory);
        if (mCategory == FileConstants.CATEGORY.COMPRESSED.getValue() ||
                mCategory == FileConstants.CATEGORY.APPS.getValue()) {
            mBaseActivity.showFab();
            mBaseActivity.setCurrentDir(path, isDualPaneInFocus);
            mBaseActivity.setCurrentCategory(mCategory);
            mBaseActivity.initializeStartingDirectory();
        }
        mBaseActivity.setNavDirectory(path, isDualPaneInFocus);
        mBaseActivity.addToBackStack(path, mCategory);

    }


    private void handleCategoryItemClick(int position) {
        if (position >= fileInfoList.size()) return;

        switch (mCategory) {
            case 0:
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                Logger.log(TAG, "on handleCategoryItemClick--");
                if (fileInfoList.get(position).isDirectory()) {
                    computeScroll();
                    if (mIsZip) {
//                        zipEntry = null;
//                        inChildZip = false;
                        String name = zipChildren.get(position).getName();
                        if (name.startsWith("/")) name = name.substring(1, name.length());
                        String name1 = name.substring(0, name.length() - 1); // 2 so that / doesnt come
                        zipEntry = zipChildren.get(position).getEntry();
                        zipEntryFileName = name1;
                        Logger.log(TAG, "handleCategoryItemClick--entry=" + zipEntry + " dir=" + zipEntry.isDirectory()
                                + "name=" + zipEntryFileName);
                        viewZipContents(position);
                    } else {
                        String path = mFilePath = fileInfoList.get(position).getFilePath();

                        isDualPaneInFocus = checkIfDualFragment();
                        mBaseActivity.setCurrentDir(path, isDualPaneInFocus);

                        // This is opCompleted when any homescreen item is clicked like Fav . Then Fav->FavList . So on
                        // clicking fav list item, category has to be set to files
                        if (mCategory != FileConstants.CATEGORY.FILES.getValue() && FileUtils.checkIfFileCategory
                                (mCategory)) {
                            mCategory = FileConstants.CATEGORY.FILES.getValue();
                            mBaseActivity.showFab();
                            mBaseActivity.setCurrentCategory(mCategory);
                            mBaseActivity.initializeStartingDirectory();
                        }
                        reloadList(false, mFilePath);
                        mBaseActivity.setNavDirectory(path, isDualPaneInFocus);
                        mBaseActivity.addToBackStack(path, mCategory);
                    }


                } else {
                    String filePath = fileInfoList.get(position).getFilePath();
                    String extension = fileInfoList.get(position).getExtension();

                    if (!mIsZip && isZipViewable(filePath)) {
                        openCompressedFile(filePath);
                    } else {
                        if (mIsZip) {
                            clearCache = true;
                            if (mZipParentPath.endsWith(".zip")) {
                                String name = zipChildren.get(position).getName().substring(zipChildren.get(position)
                                        .getName().lastIndexOf("/") + 1);

                                ZipEntry zipEntry = zipChildren.get(position).getEntry();
                                ZipEntry zipEntry1 = new ZipEntry(zipEntry);
                                String cacheDirPath = createCacheDirExtract();
                                Logger.log(TAG, "Zip entry NEW:" + zipEntry1 + " zip entry=" + zipEntry);

                                if (cacheDirPath != null) {
                                    if (name.endsWith(".zip")) {
                                        this.zipEntry = zipEntry1;
                                        zipEntryFileName = name;
                                        inChildZip = true;
                                        viewZipContents(position);
                                        mChildZipPath = cacheDirPath + "/" + name;
                                        return;
                                    }


                                    try {
                                        ZipFile zipFile;
                                        if (inChildZip) {
                                            zipFile = new ZipFile(mChildZipPath);
                                        } else {
                                            zipFile = new ZipFile(mZipParentPath);
                                        }
                                        zipEntry1 = zipEntry;
                                        new ExtractZipEntry(zipFile, cacheDirPath,
                                                FileListFragment.this, name, zipEntry1)
                                                .execute();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if (mZipParentPath.endsWith(".rar")) {
                                String name = rarChildren.get(position).getFileNameString();
                                FileHeader fileHeader = rarChildren.get(position);
                                String cacheDirPath = createCacheDirExtract();

                                if (cacheDirPath != null) {

                                    try {
                                        Archive rarFile = new Archive(new File(mZipParentPath));
                                        new ExtractZipEntry(rarFile, cacheDirPath,
                                                FileListFragment.this, name, fileHeader)
                                                .execute();

                                    } catch (IOException | RarException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }

                        } else {
                            FileUtils.viewFile(FileListFragment.this, filePath, extension);
                        }
                    }
                }
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                FileUtils.viewFile(FileListFragment.this, fileInfoList.get(position).getFilePath(),
                        fileInfoList.get
                                (position).getExtension());
                break;

        }
    }

    private boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith("zip") ||
                filePath.toLowerCase().endsWith("jar") ||
                filePath.toLowerCase().endsWith("rar");
    }

    private void viewZipContents(int position) {
        if (mZipParentPath.endsWith("rar")) {
            String name = rarChildren.get(position).getFileNameString();
            mCurrentZipDir = name.substring(0, name.length() - 1);
        } else {
            mCurrentZipDir = zipChildren.get(position).getName();
        }

        mInParentZip = false;
        reloadList(false, mZipParentPath);
        isDualPaneInFocus = checkIfDualFragment();
        String newPath;
        if (mCurrentZipDir.startsWith(File.separator)) {
            newPath = mZipParentPath + mCurrentZipDir;
        } else {
            newPath = mZipParentPath + File.separator + mCurrentZipDir;
        }
        mBaseActivity.setCurrentDir(newPath, isDualPaneInFocus);
        mBaseActivity.setNavDirectory(newPath, isDualPaneInFocus);
    }

    private String createCacheDirExtract() {
        String cacheTempDir = ".tmp";
        File file = new File(getActivity().getExternalCacheDir().getParent(), cacheTempDir);

        if (!file.exists()) {
            boolean result = file.mkdir();
            if (result) {
                String nomedia = ".nomedia";
                File noMedia = new File(file + File.separator + nomedia);
                try {
                    noMedia.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file.getAbsolutePath();
            }
        } else {
            return file.getAbsolutePath();
        }
        return null;
    }


    private final BroadcastReceiver mReloadListReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FileConstants.RELOAD_LIST)) {
                computeScroll();
                String path = intent.getStringExtra(FileConstants.KEY_PATH);
                Logger.log(TAG, "New zip PAth=" + path);
                if (path != null) {
                    FileUtils.scanFile(getActivity(), path);
                }
                reloadList(true, mFilePath);
            } else if (action.equals("refresh")) {

                int operation = intent.getIntExtra(FileConstants.OPERATION, -1);

                switch (operation) {
                    case FileConstants.DELETE:

                        ArrayList<FileInfo> deletedFilesList = intent.getParcelableArrayListExtra("deleted_files");
//                        if (!FileUtils.checkIfFileCategory(mCategory)) {

                        String[] pathArray = new String[deletedFilesList.size()];
                        ArrayList<String> paths = new ArrayList<>();
                        for (FileInfo info : deletedFilesList) {
                            paths.add(info.getFilePath());
                        }
                        paths.toArray(pathArray);

                        MediaScannerConnection.scanFile(getActivity(), pathArray, null, new OnScanCompletedListener() {

                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.d(TAG, "Scan completed=" + path + "uri=" + uri);
                            }
                        });

                        mIsBackPressed = true;
                        Uri uri = FileUtils.getUriForCategory(mCategory);
                        getContext().getContentResolver().notifyChange(uri, null);
                        for (int i = 0; i < deletedFilesList.size(); i++) {
                            fileInfoList.remove(deletedFilesList.get(i));
                        }
                        fileListAdapter.setStopAnimation(true);
                        fileListAdapter.updateAdapter(fileInfoList);

                        break;

                    case FileConstants.RENAME:

                        final int position = intent.getIntExtra("position", -1);
                        String oldFile = intent.getStringExtra("old_file");
                        String newFile = intent.getStringExtra("new_file");

                        int type = fileInfoList.get(position).getType();
                        FileUtils.removeMedia(getActivity(), new File(oldFile), type);
                        FileUtils.scanFile(getActivity().getApplicationContext(), newFile);
                        fileInfoList.get(position).setFilePath(newFile);
                        fileInfoList.get(position).setFileName(new File(newFile).getName());
                        fileListAdapter.setStopAnimation(true);
                        Logger.log(TAG, "Position changed=" + position);
                        MediaScannerConnection.scanFile(getActivity().getApplicationContext(), new String[]{newFile},
                                null, new
                                        OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(String path, Uri uri) {
                                                Log.d(TAG, "Scan completed=" + path + "uri=" + uri);
                                                fileListAdapter.notifyItemChanged(position);
                                            }
                                        });
                        break;

                    case FileConstants.MOVE:
                    case FileConstants.FOLDER_CREATE:
                    case FileConstants.FILE_CREATE:
                    case FileConstants.COPY:
                        boolean isSuccess = intent.getBooleanExtra(FileConstants.IS_OPERATION_SUCCESS, true);
                        ArrayList<String> copiedFiles = intent.getStringArrayListExtra(FileConstants.KEY_PATH);

                        if (copiedFiles != null) {
                            for (String path : copiedFiles) {
                                FileUtils.scanFile(getActivity().getApplicationContext(), path);
                            }
                        }

                        if (!isSuccess) {
                            Toast.makeText(getActivity(), getString(R.string.msg_operation_failed), Toast
                                    .LENGTH_LONG).show();
                        } else {
                            computeScroll();
                            reloadList(true, mFilePath);
                        }
                        break;

                }
            }
        }
    };


    private void itemClickActionMode(int position, boolean isLongPress) {
        fileListAdapter.toggleSelection(position, isLongPress);
        boolean hasCheckedItems = fileListAdapter.getSelectedCount() > 0;
        ActionMode actionMode = getActionMode();
        if (hasCheckedItems && actionMode == null) {
            // there are some selected items, start the actionMode
//            mBaseActivity.updateDrawerIcon(true);

            startActionMode();


            mIsDualActionModeActive = checkIfDualFragment();
//            ((BaseActivity) getActivity()).setFileList(fileInfoList);
        } else if (!hasCheckedItems && actionMode != null) {
            // there no selected items, finish the actionMode

//            mActionModeCallback.endActionMode();
            actionMode.finish();
        }
        if (getActionMode() != null) {
            SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
            setSelectedItemPos(checkedItemPos);
            mActionMode.setTitle(String.valueOf(fileListAdapter
                    .getSelectedCount()) + " selected");
        }
    }

    private void computeScroll() {
        View vi = recyclerViewFileList.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int position;
        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            position = ((LinearLayoutManager) llm).findFirstVisibleItemPosition();
        } else {
            position = ((GridLayoutManager) llm).findFirstVisibleItemPosition();
        }
        Bundle bundle = new Bundle();
        bundle.putInt(FileConstants.KEY_POSITION, position);
        bundle.putInt(FileConstants.KEY_OFFSET, top);
        if (checkIfDualFragment()) {
            scrollPositionDualPane.put(mFilePath, bundle);
        } else {
            scrollPosition.put(mFilePath, bundle);
        }
    }

    private void setSelectedItemPos(SparseBooleanArray selectedItemPos) {
        mSelectedItemPositions = selectedItemPos;
        if (selectedItemPos.size() > 1) {
            mRenameItem.setVisible(false);
            mInfoItem.setVisible(false);
        } else {
            mRenameItem.setVisible(true);
            mInfoItem.setVisible(true);
        }
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
        mActionMode.setTitle(String.valueOf(fileListAdapter.getSelectedCount()) + " " + getString(R.string.selected));
        fileListAdapter.notifyDataSetChanged();
    }

    private void clearSelection() {
        fileListAdapter.removeSelection();
    }

    private void toggleDummyView(boolean isVisible) {
        if (isVisible)
            viewDummy.setVisibility(View.VISIBLE);
        else
            viewDummy.setVisibility(View.GONE);
    }

    public void refreshList() {
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, mFilePath);
        args.putBoolean(FileConstants.KEY_ZIP, mIsZip);
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    public void setCategory(int category) {
        mCategory = category;
        setRefreshSpan = true;

    }


    public void reloadList(boolean isBackPressed, String path) {
        mFilePath = path;
        mIsBackPressed = isBackPressed;
        refreshList();
    }

/*    public void loadZipEntry() {
        reloadList();
    }*/

    private void stopAnimation() {
        if ((!fileListAdapter.mStopAnimation)) {
            for (int i = 0; i < recyclerViewFileList.getChildCount(); i++) {
                View view = recyclerViewFileList.getChildAt(i);
                if (view != null) view.clearAnimation();
            }
        }
        fileListAdapter.mStopAnimation = true;
    }


    public boolean isZipMode() {
        return mIsZip;
    }

    public BackStackModel endZipMode() {

        mIsZip = false;
        inChildZip = false;
        mInParentZip = true;
        mCurrentZipDir = null;
        totalZipList.clear();
        zipChildren.clear();
        return new BackStackModel(mZipParentPath, mParentZipCategory);
    }


    public boolean checkZipMode() {
        if (mCurrentZipDir == null || mCurrentZipDir.length() == 0) {
            endZipMode();
            return true;
        } else {
            inChildZip = false;
            Logger.log(TAG, "checkZipMode--currentzipdir B4=" + mCurrentZipDir);
            mCurrentZipDir = new File(mCurrentZipDir).getParent();
            if (mCurrentZipDir.equals(File.separator)) {
                mCurrentZipDir = null;
            }
            Logger.log(TAG, "checkZipMode--currentzipdir AFT=" + mCurrentZipDir);
            reloadList(true, mZipParentPath);
            isDualPaneInFocus = checkIfDualFragment();
            String newPath;
            if (mCurrentZipDir == null || mCurrentZipDir.equals(File.separator)) {
                newPath = mZipParentPath;
            } else {
                if (mCurrentZipDir.startsWith(File.separator)) {
                    newPath = mZipParentPath + File.separator + mCurrentZipDir;
                } else {
                    newPath = mZipParentPath + mCurrentZipDir;
                }
            }
            mBaseActivity.setCurrentDir(newPath, isDualPaneInFocus);
            mBaseActivity.setNavDirectory(newPath, isDualPaneInFocus);
            return false;
        }
    }

    public boolean isInZipMode(String path) {
        if (mCurrentZipDir == null || mCurrentZipDir.length() == 0 || !path.contains(mZipParentPath)) {
            endZipMode();
            return true;
        } else if (path.equals(mZipParentPath)) {
            mInParentZip = true;
            mCurrentZipDir = null;
            reloadList(true, mZipParentPath);
            mBaseActivity.setCurrentDir(mZipParentPath, isDualPaneInFocus);
            mBaseActivity.setNavDirectory(mZipParentPath, isDualPaneInFocus);
            return false;
        } else {
            String newPath = path.substring(mZipParentPath.length() + 1, path.length());
            Logger.log(TAG, "New zip path=" + newPath);
            mCurrentZipDir = newPath;
            mInParentZip = false;
            reloadList(false, mZipParentPath);
            mBaseActivity.setCurrentDir(path, isDualPaneInFocus);
            mBaseActivity.setNavDirectory(path, isDualPaneInFocus);
            return false;
        }
    }

    public void setBackPressed() {
        mIsBackPressed = true;
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        String path = args.getString(FileConstants.KEY_PATH);
        mSwipeRefreshLayout.setRefreshing(true);
        Logger.log(TAG, "onCreateLoader---path=" + path + "category=" + mCategory + "zip entry=" + zipEntry);

        if (mIsZip) {
            if (inChildZip) {
                if (zipEntry.isDirectory()) {
                    return new FileListLoader(this, mChildZipPath, createCacheDirExtract(),
                            zipEntryFileName, zipEntry);
                } else
                    return new FileListLoader(this, mZipParentPath, createCacheDirExtract(),
                            zipEntryFileName, zipEntry);
            }
            return new FileListLoader(this, path, FileConstants.CATEGORY.ZIP_VIEWER.getValue(),
                    mCurrentZipDir, isDualPaneInFocus, mInParentZip);
        } else {
            return new FileListLoader(this, getContext(), path, mCategory);
        }
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        if (mIsSwipeRefreshed) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsSwipeRefreshed = false;
        }
        mSwipeRefreshLayout.setRefreshing(false);

        if (data != null) {

            Log.d(TAG, "on onLoadFinished--" + data.size());
/*            if (FileUtils.checkIfLibraryCategory(mCategory)) {
                mBaseActivity.addCountText(data.size());
            }*/
            mStopAnim = true;
            fileInfoList = data;
            fileListAdapter.setCategory(mCategory);
            fileListAdapter.updateAdapter(fileInfoList);
            recyclerViewFileList.setAdapter(fileListAdapter);
            addItemDecoration();

            if (!data.isEmpty()) {

                if (setRefreshSpan) {
                    refreshSpan();
                    setRefreshSpan = false;
                }

                if (mIsBackPressed) {
                    if (checkIfDualFragment()) {
                        if (scrollPositionDualPane.containsKey(mFilePath)) {
                            Bundle b = scrollPositionDualPane.get(mFilePath);
                            if (mViewMode == FileConstants.KEY_LISTVIEW)
                                ((LinearLayoutManager) llm).scrollToPositionWithOffset(b.getInt(FileConstants
                                        .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
                            else
                                ((GridLayoutManager) llm).scrollToPositionWithOffset(b.getInt(FileConstants
                                        .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
                            recyclerViewFileList.stopScroll();
                        }
                    } else {
                        Log.d("TEST", "on onLoadFinished scrollpos--" + scrollPosition.entrySet());

                        if (scrollPosition.containsKey(mFilePath)) {
                            Bundle b = scrollPosition.get(mFilePath);
                            if (mViewMode == FileConstants.KEY_LISTVIEW)
                                ((LinearLayoutManager) llm).scrollToPositionWithOffset(b.getInt(FileConstants
                                        .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
                            else
                                ((GridLayoutManager) llm).scrollToPositionWithOffset(b.getInt(FileConstants
                                        .KEY_POSITION), b.getInt(FileConstants.KEY_OFFSET));
                        }
                    }
                    mIsBackPressed = false;
                }
                recyclerViewFileList.stopScroll();
                mTextEmpty.setVisibility(View.GONE);
            } else {
                mTextEmpty.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }

    private boolean checkIfDualFragment() {
        return FileListFragment.this instanceof FileListDualFragment;
    }

    private void startActionMode() {

        mBaseActivity.toggleFab(true);
        toggleDummyView(true);
        mBottomToolbar.setVisibility(View.VISIBLE);
        mBottomToolbar.startActionMode(new ActionModeCallback());
        mBottomToolbar.inflateMenu(R.menu.action_mode_bottom);
        mBottomToolbar.getMenu().clear();
        EnhancedMenuInflater.inflate(getActivity().getMenuInflater(), mBottomToolbar.getMenu(),
                mCategory);
        mBottomToolbar.setOnMenuItemClickListener(this);

    }

    private ActionMode getActionMode() {
        return mActionMode;
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
                    mIsMoveOperation = true;
                    togglePasteVisibility(true);
                    getActivity().supportInvalidateOptionsMenu();
                    mActionMode.finish();
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
                    togglePasteVisibility(true);
                    getActivity().supportInvalidateOptionsMenu();
                    mActionMode.finish();
                }
                break;
            case R.id.action_delete:

                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    ArrayList<FileInfo> filesToDelete = new ArrayList<>();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                        filesToDelete.add(info);
                    }
                    if (mCategory == FileConstants.CATEGORY.FAVORITES.getValue()) {
                        removeFavorite(filesToDelete);
                        Toast.makeText(getContext(), getString(R.string.fav_removed), Toast.LENGTH_SHORT).show();
                    } else {
                        mFileUtils.showDeleteDialog(mBaseActivity, filesToDelete);
                    }
                    mActionMode.finish();
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
                    FileUtils.shareFiles(getActivity(), filesToShare, mCategory);
                    mActionMode.finish();
                }
                break;

            case R.id.action_select_all:
                if (mSelectedItemPositions != null) {
                    if (mSelectedItemPositions.size() < fileListAdapter.getItemCount() - 1) {
                        toggleSelectAll(true);
                    } else {
                        toggleSelectAll(false);

                    }
                }
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
            mActionMode = mode;
            MenuInflater inflater = mActionMode.getMenuInflater();
            inflater.inflate(R.menu.action_mode, menu);
            mRenameItem = menu.findItem(R.id.action_rename);
            mInfoItem = menu.findItem(R.id.action_info);
            mArchiveItem = menu.findItem(R.id.action_archive);
            mFavItem = menu.findItem(R.id.action_fav);
            mExtractItem = menu.findItem(R.id.action_extract);
            mHideItem = menu.findItem(R.id.action_hide);
            mPermissionItem = menu.findItem(R.id.action_permissions);
            setupMenu();
            return true;
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

            } else {
                mRenameItem.setVisible(true);
                mInfoItem.setVisible(true);
                if (mSelectedItemPositions.size() == 1) {


                    boolean isDirectory = fileInfoList.get(mSelectedItemPositions.keyAt(0))
                            .isDirectory();
                    String filePath = fileInfoList.get(mSelectedItemPositions.keyAt(0))
                            .getFilePath();

                    boolean isRoot = !filePath.startsWith("/sdcard") && !filePath.startsWith("/storage");
                    if (FileUtils.isFileCompressed(filePath)) {
                        mExtractItem.setVisible(true);
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
                    SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                            "" + getString(R.string
                            .unhide));
                    if (mIsDarkTheme) {
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_unhide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_unhide_black), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    mHideItem.setTitle(hideBuilder);
                } else {
                    SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                            "" + getString(R.string
                            .hide));

                    if (mIsDarkTheme) {
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_hide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_hide_black), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    mHideItem.setTitle(hideBuilder);
                }
            }

            return false;
        }

        private void setupMenu() {
//            MenuItem item = menu.findItem(R.id.action_archive);
            SpannableStringBuilder builder = new SpannableStringBuilder(" " + "  " + getString(R
                    .string
                    .action_archive));
            // replace "*" with icon
            if (mIsDarkTheme) {
                builder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_archive_white), 0, 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_archive_black), 0, 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            mArchiveItem.setTitle(builder);

            SpannableStringBuilder favBuilder = new SpannableStringBuilder(" " + "  " + getString
                    (R.string
                            .add_fav));
            // replace "*" with icon
            if (mIsDarkTheme) {
                favBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_favorite_white), 0, 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                favBuilder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_favorite_black), 0, 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mFavItem.setTitle(favBuilder);

            SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                    getString(R.string
                            .hide));
            mHideItem.setTitle(hideBuilder);
        }


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rename:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        final String oldFilePath = fileInfoList.get(mSelectedItemPositions.keyAt(0)
                        ).getFilePath();
                        int renamedPosition = mSelectedItemPositions.keyAt(0);
                        String newFilePath = new File(oldFilePath).getParent();
                        renameDialog(oldFilePath, newFilePath, renamedPosition);
                    }
                    return true;

                case R.id.action_info:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo fileInfo = fileInfoList.get(mSelectedItemPositions.keyAt(0));
                        showInfoDialog(fileInfo);
                    }
                    mActionMode.finish();
                    return true;
                case R.id.action_archive:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        ArrayList<FileInfo> paths = new ArrayList<>();
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                            paths.add(info);
                        }
                        setBackPressed();
                        mFileUtils.showCompressDialog(mBaseActivity, mFilePath, paths);
                    }
                    mActionMode.finish();
                    return true;
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
                    }
                    mActionMode.finish();
                    return true;

                case R.id.action_extract:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo fileInfo = fileInfoList.get(mSelectedItemPositions.keyAt(0));
                        String currentFile = fileInfo.getFilePath();
                        showExtractOptions(currentFile, mFilePath);
                    }

                    mActionMode.finish();
                    return true;

                case R.id.action_hide:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        ArrayList<FileInfo> infoList = new ArrayList<>();
                        ArrayList<Integer> pos = new ArrayList<>();
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            infoList.add(fileInfoList.get(mSelectedItemPositions.keyAt(i)));
                            pos.add(mSelectedItemPositions.keyAt(i));

                        }
                        hideUnHideFiles(infoList, pos);
                    }
                    mActionMode.finish();
                    return true;

                case R.id.action_permissions:

                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(0));
                        showPermissionsDialog(info);
                    }
                    mActionMode.finish();
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            clearSelection();
            toggleDummyView(false);

            mActionMode = null;
            mBottomToolbar.setVisibility(View.GONE);
            mSelectedItemPositions = new SparseBooleanArray();
            mSwipeRefreshLayout.setEnabled(true);
            mDragPaths.clear();
            // FAB should be visible only for Files Category
            if (mCategory == 0) {
                mBaseActivity.toggleFab(false);
            }
        }
    }

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
        final MaterialDialog materialDialog = new DialogUtils().showEditDialog(getActivity(), texts);

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
                mBaseActivity.mFileOpsHelper.renameFile(mIsRootMode, oldFile, newFile,
                        position);
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
        mActionMode.finish();
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
            mBaseActivity.mFileOpsHelper.renameFile(mIsRootMode, oldFile, newFile,
                    pos.get(i));
        }
    }


    private void showPermissionsDialog(final FileInfo fileInfo) {

        String texts[] = new String[]{getString(R.string.permissions), getString(R.string.msg_ok),
                "", getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showCustomDialog(getActivity(),
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
        String perm = fileInfo.getPermissions();

        ArrayList<Boolean[]> arrayList = FileUtils.parse(perm);
        Boolean[] read = arrayList.get(0);
        Boolean[] write = arrayList.get(1);
        Boolean[] exe = arrayList.get(2);
        readown.setChecked(read[0]);
        readgroup.setChecked(read[1]);
        readother.setChecked(read[2]);
        writeown.setChecked(write[0]);
        writegroup.setChecked(write[1]);
        writeother.setChecked(write[2]);
        exeown.setChecked(exe[0]);
        exegroup.setChecked(exe[1]);
        exeother.setChecked(exe[2]);

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
                try {//
                    RootTools.remount(fileInfo.getFilePath(), "RW");
                    RootTools.getShell(true).add(com);
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


    private void showExtractOptions(final String currentFilePath, final String currentDir) {

        mSelectedPath = null;
        final String currentFileName = currentFilePath.substring(currentFilePath.lastIndexOf("/")
                + 1, currentFilePath.lastIndexOf("."));
        String texts[] = new String[]{getString(R.string.extract), getString(R.string.extract),
                "", getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showCustomDialog(getActivity(),
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
                dialogFragment.setTargetFragment(FileListFragment.this, DIALOG_FRAGMENT);
                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, checkTheme());
                dialogFragment.show(getFragmentManager(), "Browse Fragment");
            }
        });

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = editFileName.getText().toString();
                if (!FileUtils.validateFileName(fileName)) {
                    editFileName.setError(getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }
                if (radioButtonSpecify.isChecked()) {
                    File newFile = new File(mSelectedPath + "/" + currentFileName);
                    File currentFile = new File(currentFilePath);
                    mBaseActivity.mFileOpsHelper.extractFile(currentFile, newFile);
                } else {
                    File newFile = new File(currentDir + "/" + currentFileName);
                    File currentFile = new File(currentFilePath);
                    mBaseActivity.mFileOpsHelper.extractFile(currentFile, newFile);
                }
                setBackPressed();
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

    private int checkTheme() {
        int theme = ThemeUtils.getTheme(getActivity());

        if (theme == FileConstants.THEME_DARK) {
            return R.style.Dark_AppTheme_NoActionBar;
        } else {
            return R.style.AppTheme_NoActionBar;
        }
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

        mBaseActivity.updateFavourites(favInfoArrayList);
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
        mBaseActivity.removeFavourites(favInfoArrayList);
    }


    private void togglePasteVisibility(boolean isVisible) {
        mPasteItem.setVisible(isVisible);
        mIsPasteItemVisible = isVisible;
    }

    private void showInfoDialog(FileInfo fileInfo) {
        String title = getString(R.string.properties);
        String texts[] = new String[]{title, getString(R.string.msg_ok), "", null};
        final MaterialDialog materialDialog = new DialogUtils().showCustomDialog(getActivity(),
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
        if (FileUtils.isDateNotInMs(mCategory)) {
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

            if (fileInfo.getType() == FileConstants.CATEGORY.VIDEO.getValue()) {
                Uri videoUri = Uri.fromFile(new File(path));
                Glide.with(getActivity()).load(videoUri).centerCrop()
                        .placeholder(R.drawable.ic_movie)
                        .crossFade(2)
                        .into(imageFileIcon);
            } else if (fileInfo.getType() == FileConstants.CATEGORY.IMAGE.getValue()) {
                Uri imageUri = Uri.fromFile(new File(path));
                Glide.with(getActivity()).load(imageUri).centerCrop()
                        .crossFade(2)
                        .placeholder(R.drawable.ic_image_default)
                        .into(imageFileIcon);
            } else if (fileInfo.getType() == FileConstants.CATEGORY.AUDIO.getValue()) {
                imageFileIcon.setImageResource(R.drawable.ic_music_default);
            } else if (fileInfo.getExtension().equals(FileConstants.APK_EXTENSION)) {
                Drawable apkIcon = FileUtils.getAppIcon(getActivity(), path);
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

        int color = new DialogUtils().getCurrentThemePrimary(getActivity());
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

                PasteConflictChecker conflictChecker = new PasteConflictChecker(mBaseActivity, destinationDir,
                        mIsRootMode, isMoveOperation);
                ArrayList<FileInfo> info = new ArrayList<>();
                info.addAll(sourcePaths);
                conflictChecker.execute(info);
                clearSelectedPos();
                mActionMode.finish();
                return true;
            }
        });

        final MaterialDialog materialDialog = builder.build();

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActionMode != null)
                    mActionMode.finish();
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    class myDragEventListener implements View.OnDragListener {

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
/*                        int visiblePos = ((LinearLayoutManager) llm).findLastVisibleItemPosition();
                        if (newPos + 2 >= visiblePos) {
                            ((LinearLayoutManager) llm).scrollToPosition(newPos + 1);
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
                    mDragPaths = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DROP:
//                    Log.d(TAG,"DRag drop"+pos);

                    View top = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position = recyclerViewFileList.getChildAdapterPosition(top);
                    Logger.log(TAG, "DROP new pos=" + position);
                    fileListAdapter.clearDragPos();
                    ArrayList<FileInfo> paths = (ArrayList<FileInfo>) event.getLocalState();

                  /*  ArrayList<FileInfo> paths = dragData.getParcelableArrayListExtra(FileConstants
                            .KEY_PATH);*/

                    String destinationDir;
                    if (position != -1) {
                        destinationDir = fileInfoList.get(position).getFilePath();
                    } else {
                        destinationDir = mFilePath;
                    }
                    String sourceParent = new File(paths.get(0).getFilePath()).getParent();
                    if (!new File(destinationDir).isDirectory()) {
                        destinationDir = new File(destinationDir).getParent();
                    }

                    boolean value = destinationDir.equals(sourceParent);
                    Logger.log(TAG, "Source parent=" + sourceParent + " " + value);


                    if (!destinationDir.equals(sourceParent)) {
                        Logger.log(TAG, "Source parent=" + sourceParent + " Dest=" +
                                destinationDir);
                        showDragDialog(paths, destinationDir);
                    } else {
                        final boolean isMoveOperation = false;
                        PasteConflictChecker conflictChecker = new PasteConflictChecker(mBaseActivity, destinationDir,
                                mIsRootMode, isMoveOperation);
                        ArrayList<FileInfo> info = new ArrayList<>();
                        info.addAll(paths);
                        conflictChecker.execute(info);
                        clearSelectedPos();
                        Logger.log(TAG, "Source=" + paths.get(0) + "Dest=" + destinationDir);
                        mActionMode.finish();
                    }

                    mDragPaths = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    View top1 = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position1 = recyclerViewFileList.getChildAdapterPosition(top1);
                    ArrayList<FileInfo> dragPaths = (ArrayList<FileInfo>) event.getLocalState();


                    Logger.log(TAG, "DRAG END new pos=" + position1);
                    Logger.log(TAG, "DRAG END Local state=" + dragPaths);
                    Logger.log(TAG, "DRAG END result=" + event.getResult());
                    Logger.log(TAG, "DRAG END mCurrentDirSingle=" + mLastSinglePaneDir);
                    Logger.log(TAG, "DRAG END mCurrentDirDual=" + mLastDualPaneDir);
                    Log.d(TAG, "DRag end");
                    fileListAdapter.clearDragPos();
                    if (!event.getResult() && position1 == RecyclerView.NO_POSITION) {
                        ViewParent parent1 = v.getParent().getParent();

                        if (((View) parent1).getId() == R.id.frame_container_dual) {
                            Logger.log(TAG, "DRAG END parent dual =" + true);
                            FileListDualFragment dualPaneFragment = (FileListDualFragment)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.frame_container_dual);
                            Logger.log(TAG, "DRAG END Dual dir=" + mLastDualPaneDir);

//                            Logger.log(TAG, "Source=" + mDragPaths.get(0) + "Dest=" + mLastDualPaneDir);
                            if (dualPaneFragment != null && new File(mLastDualPaneDir).list().length == 0 &&
                                    dragPaths.size() != 0) {
//                                if (!destinationDir.equals(paths.get(0))) {
                                showDragDialog(dragPaths, mLastDualPaneDir);
//                                }
                            }
                        } else {
                            Logger.log(TAG, "DRAG END parent dual =" + false);
                            FileListFragment singlePaneFragment = (FileListFragment)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.main_container);
                            Logger.log(TAG, "DRAG END single dir=" + mLastSinglePaneDir);

//                            Logger.log(TAG, "Source=" + mDragPaths.get(0) + "Dest=" + mLastDualPaneDir);
                            if (singlePaneFragment != null && new File(mLastSinglePaneDir).list().length == 0 &&
                                    dragPaths.size() != 0) {
//                                if (!destinationDir.equals(paths.get(0))) {
                                showDragDialog(dragPaths, mLastSinglePaneDir);
//                                }
                            }
                        }

                    }
                    mDragPaths = new ArrayList<>();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(FileListFragment.this.getClass().getSimpleName(), "On Create options " +
                "Fragment=");

        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_base, menu);
        MenuItem mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mPasteItem = menu.findItem(R.id.action_paste);
        mPasteItem.setVisible(mIsPasteItemVisible);
        mViewMode = sharedPreferenceWrapper.getViewMode(getActivity());
        mViewItem = menu.findItem(R.id.action_view);
        updateMenuTitle();
        setupSearchView();

    }

    private void updateMenuTitle() {
        mViewItem.setTitle(mViewMode == 0 ? R.string.action_view_grid : R.string.action_view_list);
    }


    private void setupSearchView() {
        // Disable full screen keyboard in landscape
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        fileListAdapter.filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    private void clearSelectedPos() {
            mSelectedItemPositions = new SparseBooleanArray();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_paste:
                if (mCopiedData.size() > 0) {

                    PasteConflictChecker conflictChecker = new PasteConflictChecker(mBaseActivity, mFilePath,
                            mIsRootMode, mIsMoveOperation);

                    ArrayList<FileInfo> info = new ArrayList<>();
                    info.addAll(mCopiedData);
                    conflictChecker.execute(info);
                    clearSelectedPos();
                    mCopiedData.clear();
                    togglePasteVisibility(false);
                }
                break;

            case R.id.action_view:
                if (mViewMode == FileConstants.KEY_LISTVIEW) {
                    mViewMode = FileConstants.KEY_GRIDVIEW;
                } else {
                    mViewMode = FileConstants.KEY_LISTVIEW;
                }
                sharedPreferenceWrapper.savePrefs(getActivity(), mViewMode);
                switchView();
                updateMenuTitle();
                break;

            case R.id.action_sort:
                showSortDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        int color = new DialogUtils().getCurrentThemePrimary(getActivity());

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
        mPreferences.edit().putInt(FileConstants.KEY_SORT_MODE, sortMode).apply();
    }

    private int getSortMode() {
        return mPreferences.getInt(FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }

    private void switchView() {
        fileListAdapter = null;
        recyclerViewFileList.setHasFixedSize(true);

        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            llm = new CustomLayoutManager(getActivity());
            recyclerViewFileList.setLayoutManager(llm);

        } else {
            refreshSpan();
        }

        mStopAnim = true;

        fileListAdapter = new FileListAdapter(getContext(), fileInfoList,
                mCategory, mViewMode);

        recyclerViewFileList.setAdapter(fileListAdapter);
        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            if (mGridItemDecoration != null) {
                recyclerViewFileList.removeItemDecoration(mGridItemDecoration);
            }
            if (mDividerItemDecoration == null) {
                mDividerItemDecoration = new DividerItemDecoration(getActivity(), mIsDarkTheme);
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


    public void refreshSpan() {
        if (mViewMode == FileConstants.KEY_GRIDVIEW) {
            mIsDualModeEnabled = mPreferences
                    .getBoolean(FileConstants.PREFS_DUAL_PANE, false);
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT || !mIsDualModeEnabled || FileUtils
                    .checkIfLibraryCategory(mCategory)) {
                mGridColumns = getResources().getInteger(R.integer.grid_columns);
            } else {
                mGridColumns = getResources().getInteger(R.integer.grid_columns_dual);
            }
            Log.d(TAG, "Refresh span--columns=" + mGridColumns + "category=" + mCategory + " dual mode=" +
                    mIsDualModeEnabled);

            llm = new CustomGridLayoutManager(getActivity(), mGridColumns);
            recyclerViewFileList.setLayoutManager(llm);
        }
    }

    @Override
    public void onDestroyView() {
        if (!mInstanceStateExists) {
            mPreferences.edit().putInt(FileConstants.KEY_GRID_COLUMNS, mGridColumns).apply();
            sharedPreferenceWrapper.savePrefs(getActivity(), mViewMode);
        }
        if (clearCache) {
            clearCache();
            clearCache = false;
        }
        super.onDestroyView();
    }

    private void clearCache() {
        String path = createCacheDirExtract();
        if (path != null) {
            File[] files = new File(path).listFiles();

            if (files != null) {
                for (File file : files)
                    file.delete();
            }
        }
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
