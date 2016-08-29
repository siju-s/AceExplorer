package com.siju.acexplorer.filesystem;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
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
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.model.BackStackModel;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipModel;
import com.siju.acexplorer.filesystem.task.PasteConflictChecker;
import com.siju.acexplorer.filesystem.ui.CustomGridLayoutManager;
import com.siju.acexplorer.filesystem.ui.CustomLayoutManager;
import com.siju.acexplorer.filesystem.ui.DialogBrowseFragment;
import com.siju.acexplorer.filesystem.ui.DividerItemDecoration;
import com.siju.acexplorer.filesystem.ui.EnhancedMenuInflater;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.utils.DialogUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.siju.acexplorer.R.id.textEmpty;


/**
 * Created by Siju on 13-06-2016.
 */

public class FileListFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<ArrayList<FileInfo>>,
        SearchView.OnQueryTextListener,
        Toolbar.OnMenuItemClickListener,
        DialogBrowseFragment.SelectedPathListener {

    private final String TAG = this.getClass().getSimpleName();
    //    private ListView fileList;
    private RecyclerView recyclerViewFileList;
    private View root;
    private final int LOADER_ID = 1000;
    private final int LOADER_ID_DUAL = 2000;

    private FileListAdapter fileListAdapter;
    public ArrayList<FileInfo> fileInfoList;
    private boolean mIsDualMode;
    private String mFilePath;
    private String mFilePathOther;

    private int mCategory;
    private int mViewMode = FileConstants.KEY_LISTVIEW;
    private String mPath;
    private boolean mIsZip;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private TextView mTextEmpty;
    private boolean mIsDualActionModeActive;
    private boolean mIsLandscapeMode;
    private boolean mIsDualModeEnabledSettings;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private boolean mStartDrag;
    private GestureDetectorCompat gestureDetector;
    private long mLongPressedTime;
    private boolean mIsDragInProgress;
    private View mItemView;
    private int mDragInitialPos = -1;
    private ArrayList<FileInfo> mDragPaths = new ArrayList<>();
    //    private PasteUtils mPasteUtils;
    //    private RecyclerView.LayoutManager llm;
    private RecyclerView.LayoutManager llm;
    private String mLastDualPaneDir;
    private String mLastSinglePaneDir;
    private boolean mDualPaneInFocus;
    private View viewDummy;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mZipParentPath;
    private int mZipLevelDual;
    private BaseActivity mBaseActivity;
    private boolean isDualPaneInFocus;
    private Toolbar mBottomToolbar;
    private ActionMode mActionMode;
    private SparseBooleanArray mSelectedItemPositions = new SparseBooleanArray();
    MenuItem mPasteItem, mRenameItem, mInfoItem, mArchiveItem, mFavItem, mExtractItem, mHideItem, mPermissionItem;
    private static final int PASTE_OPERATION = 1;
    private static final int DELETE_OPERATION = 2;
    private static final int ARCHIVE_OPERATION = 3;
    private static final int DECRYPT_OPERATION = 4;

    private boolean mIsMoveOperation = false;
    private HashMap<String, Integer> mPathActionMap = new HashMap<>();
    private int mPasteAction = FileUtils.ACTION_NONE;
    private boolean isPasteConflictDialogShown;
    private String mSourceFilePath = null;
    private ArrayList<String> tempSourceFile = new ArrayList<>();
    private int tempConflictCounter = 0;
    private Dialog mPasteConflictDialog;
    private String mCurrentZipDir;
    public ArrayList<ZipModel> totalZipList = new ArrayList<>();
    public ArrayList<ZipModel> zipChildren = new ArrayList<>();
    public Archive mArchive;
    public ArrayList<FileHeader> totalRarList = new ArrayList<>();
    public ArrayList<FileHeader> rarChildren = new ArrayList<>();
    private boolean mInParentZip = true;
    private int mParentZipCategory;
    private boolean mIsPasteItemVisible;
    private boolean mIsFavGroup;
    private String mSelectedPath;
    private Button buttonPathSelect;
    private HashMap<String, Bundle> scrollPosition = new HashMap<>();
    private HashMap<String, Bundle> scrollPositionDualPane = new HashMap<>();
    private boolean mIsDataRefreshed;
    private int mGridColumns;
    private int mGridItemWidth;
    private SharedPreferences mPreferences;
    private DisplayMetrics displayMetrics;
    private int mOldWidth;
    private int mCurrentOrientation;
    private ArrayList<FileInfo> mCopiedData = new ArrayList<>();
    private ArrayList<FileInfo> mFiles = new ArrayList<>();

    private String mOldFilePath, mNewFilePath;
    private boolean mIsRootMode = true;
    private int mRenamedPosition = -1;
    private boolean mIsSwipeRefreshed;
    private FileUtils mFileUtils;
    private boolean mStopAnim = true;
    private boolean mShowHidden;
    private int mSortMode;
    private boolean mIsBackPressed;


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
        mCurrentOrientation = getResources().getConfiguration().orientation;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mIsLandscapeMode = getResources().getConfiguration().orientation == Configuration
                .ORIENTATION_LANDSCAPE;
        mIsDualModeEnabledSettings = mPreferences
                .getBoolean(FileConstants.PREFS_DUAL_PANE, false);
        mGridColumns = mPreferences.getInt(FileConstants.KEY_GRID_COLUMNS, 0);
        mShowHidden = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        mSortMode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);

        Bundle args = new Bundle();
        final String fileName;
        ArrayList<FileInfo> list = new ArrayList<>();

        if (getArguments() != null) {
            if (getArguments().getString(FileConstants.KEY_PATH) != null) {
                mFilePath = getArguments().getString(FileConstants.KEY_PATH);
                mFilePathOther = getArguments().getString(FileConstants.KEY_PATH_OTHER);

            }
            mCategory = getArguments().getInt(FileConstants.KEY_CATEGORY, FileConstants.CATEGORY.FILES.getValue());
            if (mCategory != FileConstants.CATEGORY.FILES.getValue()) {
                list = getArguments().getParcelableArrayList(FileConstants
                        .KEY_LIB_SORTLIST);
                if (list != null)
                    Log.d(TAG, "Lib list =" + list.size());
                mBaseActivity.toggleNavBarFab(true);

            } else {
                mBaseActivity.toggleNavBarFab(false);
            }

            mIsZip = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
            mIsDualMode = getArguments().getBoolean(FileConstants.KEY_DUAL_MODE, false);
            mDualPaneInFocus = getArguments().getBoolean(FileConstants.KEY_FOCUS_DUAL, false);
            if (mDualPaneInFocus) {
                mLastDualPaneDir = mFilePath;
                mLastSinglePaneDir = mFilePathOther;
                Log.d(TAG, "on onActivityCreated dual focus Yes--singledir" + mLastSinglePaneDir + "dualDir=" + mLastDualPaneDir);

            } else {
                mLastSinglePaneDir = mFilePath;
                mLastDualPaneDir = mFilePathOther;
                Log.d(TAG, "on onActivityCreated dual focus No--singledir" + mLastSinglePaneDir + "dualDir=" + mLastDualPaneDir);
            }

        }
        mViewMode = sharedPreferenceWrapper.getViewMode(getActivity());

        Log.d(TAG, "on onActivityCreated--Fragment" + mFilePath);
        Log.d(TAG, "View mode=" + mViewMode);
        args.putString(FileConstants.KEY_PATH, mFilePath);
        recyclerViewFileList.setHasFixedSize(true);

        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            llm = new CustomLayoutManager(getActivity());
        } else {
            llm = new CustomGridLayoutManager(getActivity(), getResources().getInteger(R
                    .integer.grid_columns));

        }
//            llm.setAutoMeasureEnabled(false);
        recyclerViewFileList.setLayoutManager(llm);
        recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                .VERTICAL));


        if (list == null || list.size() == 0) {
            isDualPaneInFocus = checkIfDualFragment();
            if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                mBaseActivity.setNavDirectory(mFilePath, isDualPaneInFocus);
            }
            getLoaderManager().initLoader(LOADER_ID, args, this);
        } else {
            fileInfoList = new ArrayList<>();
            fileInfoList.addAll(list);
           /* recyclerViewFileList.setHasFixedSize(true);

            if (mViewMode == FileConstants.KEY_LISTVIEW) {
                llm = new CustomLayoutManager(getActivity());
            } else {
                llm = new CustomGridLayoutManager(getActivity(), getResources().getInteger(R
                        .integer.grid_columns));

            }
//            llm.setAutoMeasureEnabled(false);
            recyclerViewFileList.setLayoutManager(llm);
            recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
            recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                    .VERTICAL));
*/
        }

        fileListAdapter = new FileListAdapter(FileListFragment.this, getContext(), fileInfoList,
                mCategory, mViewMode);
        recyclerViewFileList.setAdapter(fileListAdapter);
        initializeListeners();

        recyclerViewFileList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int event = motionEvent.getActionMasked();

                if (fileListAdapter != null && mStopAnim) {
                    stopAnimation();
                    mStopAnim = false;
                }

                if (mStartDrag && event == MotionEvent.ACTION_UP) {
                    mStartDrag = false;
                } else if (mStartDrag && event == MotionEvent.ACTION_MOVE && mLongPressedTime != 0) {
                    long timeElapsed = System.currentTimeMillis() - mLongPressedTime;
//                    Logger.log(TAG, "On item touch time Elapsed" + timeElapsed);

                    if (timeElapsed > 1000) {
                        mLongPressedTime = 0;
                        mStartDrag = false;
                        mDragInitialPos = -1;
                        Intent intent = new Intent();
                        Logger.log(TAG, "On touch drag path size=" + mDragPaths.size());

                        intent.putParcelableArrayListExtra(FileConstants.KEY_PATH, mDragPaths);
                        ClipData data = ClipData.newIntent("", intent);
                        int count = fileListAdapter
                                .getSelectedCount();
                        View.DragShadowBuilder shadowBuilder = new MyDragShadowBuilder(mItemView,
                                count);
                        view.startDrag(data, shadowBuilder, mDragPaths, 0);
                    }
                }
                return false;
            }
        });


    }

    @Override
    public void onResume() {

        super.onResume();
        IntentFilter intentFilter = new IntentFilter("reload_list");
        intentFilter.addAction("refresh");
        getActivity().registerReceiver(mReloadListReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReloadListReceiver);
    }

    private void initializeViews() {
        recyclerViewFileList = (RecyclerView) root.findViewById(R.id.recyclerViewFileList);
        mTextEmpty = (TextView) root.findViewById(textEmpty);
        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        recyclerViewFileList.setOnDragListener(new myDragEventListener());
        viewDummy = root.findViewById(R.id.viewDummy);
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        int colorResIds[] = {R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorPrimaryDark};
        mSwipeRefreshLayout.setColorSchemeResources(colorResIds);
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
                Logger.log(TAG, "On item click");
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
                Logger.log(TAG, "On long click" + mStartDrag);
                if (!mIsZip) {
                    itemClickActionMode(position, true);
                    mLongPressedTime = System.currentTimeMillis();

                    if (getActionMode() != null && fileListAdapter
                            .getSelectedCount() >= 1) {
//                    mDragPaths = new ArrayList<String>();
                        FileInfo fileInfo = fileInfoList.get(position);

                        if (!mDragPaths.contains(fileInfo)) {
                            mDragPaths.add(fileInfo);
                        }
                        mItemView = view;

                        mStartDrag = true;

                        mDragInitialPos = position;
                    } else {
                        Logger.log(TAG, "On long click ELSE");

                    }
                }
            }
        });
    }

    public void openCompressedFile(String path) {

        mIsZip = true;
        mInParentZip = true;
        mCurrentZipDir = null;
        mZipParentPath = path;
        reloadList(false, path);
        mParentZipCategory = mCategory;
        isDualPaneInFocus = checkIfDualFragment();
        Logger.log(TAG, "Opencompressedfile--mCategory" + mCategory);
        if (mCategory == FileConstants.CATEGORY.COMPRESSED.getValue()) {
//                            mCategory = FileConstants.CATEGORY.FILES.getValue();
            mBaseActivity.toggleNavBarFab(false);
            mBaseActivity.setCurrentDir(path, isDualPaneInFocus);
            mBaseActivity.setCurrentCategory(mCategory);
            mBaseActivity.initializeStartingDirectory();
        }
        mBaseActivity.setNavDirectory(path, isDualPaneInFocus);
        mBaseActivity.addToBackStack(path, mCategory);

    }


    private void handleCategoryItemClick(int position) {
        switch (mCategory) {
            case 0:
            case 5:
            case 7:
            case 8:
            case 11:
                Log.d("TEST", "on handleCategoryItemClick--");

                // For file, open external apps based on Mime Type
                if (!fileInfoList.get(position).isDirectory()) {
                    String filePath = fileInfoList.get(position).getFilePath();
                    String extension = fileInfoList.get(position).getExtension();
                    if (isFileZipViewable(filePath)) {
                        openCompressedFile(filePath);
                    } else {
                        FileUtils.viewFile(FileListFragment.this, filePath, extension);
                    }

                } else {
                    computeScroll();
                    if (mIsZip) {
                        if (mZipParentPath.endsWith("zip")) {
                            mCurrentZipDir = zipChildren.get(position).getName();
                        }
                        else {
                            String name = rarChildren.get(position).getFileNameString();
                            mCurrentZipDir = name.substring(0, name.length() - 1);

                        }

                        mInParentZip = false;
                        reloadList(false, mZipParentPath);
                        isDualPaneInFocus = checkIfDualFragment();
                        String newPath = mZipParentPath + "/" + mCurrentZipDir;
                        mBaseActivity.setCurrentDir(newPath, isDualPaneInFocus);
                        mBaseActivity.setNavDirectory(newPath, isDualPaneInFocus);

                    } else {

                        String path = mFilePath = fileInfoList.get(position).getFilePath();
                        reloadList(false, mFilePath);
                        isDualPaneInFocus = checkIfDualFragment();
                        mBaseActivity.setCurrentDir(path, isDualPaneInFocus);

                        // This is done when any homescreen item is clicked like Fav . Then Fav->FavList . So on
                        // clicking fav list item, category has to be set to files
                        if (mCategory != FileConstants.CATEGORY.FILES.getValue() && FileUtils.checkIfFileCategory
                                (mCategory)) {
                            mCategory = FileConstants.CATEGORY.FILES.getValue();
                            mBaseActivity.toggleNavBarFab(false);
                            mBaseActivity.setCurrentCategory(mCategory);
                            mBaseActivity.initializeStartingDirectory();

//                            mBaseActivity.setNavDirectory(mFilePath,isDualPaneInFocus);
                        }
                        mBaseActivity.setNavDirectory(path, isDualPaneInFocus);
                        mBaseActivity.addToBackStack(path, mCategory);
                    }


                }
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                FileUtils.viewFile(FileListFragment.this, fileInfoList.get(position).getFilePath(), fileInfoList.get
                        (position)
                        .getExtension());

                break;

        }
    }

    public boolean isFileZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith("zip") ||
                filePath.toLowerCase().endsWith("jar") ||
                filePath.toLowerCase().endsWith("rar");
    }


    private BroadcastReceiver mReloadListReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("reload_list")) {
                computeScroll();
                String path = intent.getStringExtra(FileConstants.KEY_PATH);
                Logger.log(TAG, "New zip PAth=" + path);
                FileUtils.scanFile(getActivity(), path);
                reloadList(true, mFilePath);
            } else if (action.equals("refresh")) {

                int operation = intent.getIntExtra(FileConstants.OPERATION, -1);

                switch (operation) {
                    case FileConstants.DELETE:
                        ArrayList<FileInfo> deletedFilesList = intent.getParcelableArrayListExtra("deleted_files");
                        if (!FileUtils.checkIfFileCategory(mCategory)) {

                            String[] pathArray = new String[deletedFilesList.size()];
                            ArrayList<String> paths = new ArrayList<>();
                            for (FileInfo info : deletedFilesList) {
                                paths.add(info.getFilePath());
                            }
                            paths.toArray(pathArray);

                            /*String[] mimeArray = new String[mimeTypes.size()];
                            mimeTypes.toArray(mimeArray);
*/
                            MediaScannerConnection.scanFile(getActivity(), pathArray, null, new MediaScannerConnection
                                    .OnScanCompletedListener() {

                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.d(TAG, "Scan completed=" + path + "uri=" + uri);
                                }
                            });

//                        refreshMediaStore(paths.get(i));
                        }
                        for (int i = 0; i < deletedFilesList.size(); i++) {
                            fileInfoList.remove(deletedFilesList.get(i));
                        }
                        fileListAdapter.updateAdapter(fileInfoList);

                        break;

                    case FileConstants.RENAME:
                        int position = intent.getIntExtra("position", -1);
                        String oldFile = intent.getStringExtra("old_file");
                        String newFile = intent.getStringExtra("new_file");

                        if (!FileUtils.checkIfFileCategory(mCategory)) {
                            FileUtils.removeMedia(getActivity(), new File(oldFile), mCategory);
                            FileUtils.scanFile(getActivity(), newFile);
                        }
                        fileInfoList.get(position).setFilePath(newFile);
                        fileInfoList.get(position).setFileName(new File(newFile).getName());
                        fileListAdapter.updateAdapter(fileInfoList);
                        break;

                    case FileConstants.MOVE:
                    case FileConstants.FOLDER_CREATE:
                    case FileConstants.FILE_CREATE:
                        refreshList();
                        break;

                }


            }
        }
    };


    public void update(ArrayList<FileInfo> list) {
        fileInfoList.clear();
        mSwipeRefreshLayout.setRefreshing(false);
        fileListAdapter.setCategory(mCategory);
        fileInfoList.addAll(list);
        fileListAdapter.updateAdapter(list);
    }


    private void itemClickActionMode(int position, boolean isLongPress) {
        fileListAdapter.toggleSelection(position, isLongPress);
        boolean hasCheckedItems = fileListAdapter.getSelectedCount() > 0;
        ActionMode actionMode = getActionMode();
        if (hasCheckedItems && actionMode == null) {
            // there are some selected items, start the actionMode
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

    public void computeScroll() {
        View vi = recyclerViewFileList.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int index;
        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            index = ((LinearLayoutManager) llm).findFirstVisibleItemPosition();
        } else {
            index = ((GridLayoutManager) llm).findFirstVisibleItemPosition();
        }
        Bundle b = new Bundle();
        b.putInt("index", index);
        b.putInt("top", top);
        if (checkIfDualFragment()) {
            scrollPositionDualPane.put(mFilePath, b);
        } else {
            scrollPosition.put(mFilePath, b);
        }
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


    public void toggleSelectAll(boolean selectAll) {
        fileListAdapter.clearSelection();
        for (int i = 0; i < fileListAdapter.getItemCount(); i++) {
            fileListAdapter.toggleSelectAll(i, selectAll);
        }
        SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
        setSelectedItemPos(checkedItemPos);

        mActionMode.setTitle(String.valueOf(fileListAdapter.getSelectedCount()
        ) + " " + getString(R.string.selected));
        fileListAdapter.notifyDataSetChanged();

    }

    public void clearSelection() {
        fileListAdapter.removeSelection();

    }

    public void toggleDummyView(boolean isVisible) {
        if (isVisible)
            viewDummy.setVisibility(View.VISIBLE);
        else
            viewDummy.setVisibility(View.GONE);

    }

    public void refreshList() {
        computeScroll();
        mIsDataRefreshed = true;
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, mFilePath);
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    public void setCategory(int category) {
        mCategory = category;
    }


    public void reloadList(boolean isBackPressed, String path) {
//        if (!mIsZip) {
        mFilePath = path;
        mIsBackPressed = isBackPressed;
//        }
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, mFilePath);
        args.putBoolean(FileConstants.KEY_ZIP, mIsZip);
        mIsDataRefreshed = true;
        getLoaderManager().restartLoader(LOADER_ID, args, this);

    }

    public void stopAnimation() {
        if ((!fileListAdapter.mStopAnimation)) {
            for (int j = 0; j < recyclerViewFileList.getChildCount(); j++) {
                View v = recyclerViewFileList.getChildAt(j);
                if (v != null) v.clearAnimation();
            }
        }
        fileListAdapter.mStopAnimation = true;
    }


    public boolean isZipMode() {
        return mIsZip;
    }

    public BackStackModel clearZipMode() {

        mIsZip = false;
//        mZipParentPath = null;
        mInParentZip = true;
        mCurrentZipDir = null;
        totalZipList.clear();
        zipChildren.clear();
        BackStackModel backStackModel = new BackStackModel(mZipParentPath, mParentZipCategory);
        return backStackModel;
    }


    public boolean checkZipMode() {
        if (mCurrentZipDir == null || mCurrentZipDir.length() == 0) {
            clearZipMode();
            return true;
        } else {
            mCurrentZipDir = new File(mCurrentZipDir).getParent();
            reloadList(true, mZipParentPath);
            isDualPaneInFocus = checkIfDualFragment();
            String newPath;
            if (mCurrentZipDir == null) {
                newPath = mZipParentPath;
            } else {
                newPath = mZipParentPath + "/" + mCurrentZipDir;
            }
            mBaseActivity.setCurrentDir(newPath, isDualPaneInFocus);
            mBaseActivity.setNavDirectory(newPath, isDualPaneInFocus);
            return false;
        }
    }

    public boolean navButtonBack(String path) {
        if (mCurrentZipDir == null || mCurrentZipDir.length() == 0 || !path.contains(mZipParentPath)) {
            clearZipMode();
            return true;
        } else if (path.equals(mZipParentPath)) {
            mInParentZip = true;
            mCurrentZipDir = null;
            reloadList(true, null);
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


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        Logger.log("TEST", "onCreateLoader");
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        String path = args.getString(FileConstants.KEY_PATH);
//        mSwipeRefreshLayout.setRefreshing(true);
        if (mIsZip) {
            return new FileListLoader(this, path, FileConstants.CATEGORY.ZIP_VIEWER.getValue(),
                    mCurrentZipDir, isDualPaneInFocus, mInParentZip);
        } else {
            return new FileListLoader(getContext(), path, mCategory, mShowHidden, mSortMode);
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        if (mIsSwipeRefreshed) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsSwipeRefreshed = false;
        }

//        Log.d(TAG, "on onLoadFinished--" + data.size());
        if (data != null) {

            Log.d("TEST", "on onLoadFinished--" + data.size());
            // Stop refresh animation
//            mSwipeRefreshLayout.setRefreshing(false);
            mStopAnim = true;
            fileInfoList = data;
            fileListAdapter.setCategory(mCategory);
            fileListAdapter.updateAdapter(fileInfoList);
            recyclerViewFileList.setAdapter(fileListAdapter);


            if (!data.isEmpty()) {
                recyclerViewFileList.setHasFixedSize(true);


                if (!mIsDataRefreshed) {
                    /*if (mViewMode == FileConstants.KEY_LISTVIEW) {
                        llm = new CustomLayoutManager(getActivity());
                    } else {
                        findNoOfGridColumns();
                        mGridItemWidth = dpToPx(100);
                        llm = new CustomGridLayoutManager(getActivity(), mGridItemWidth);
                    }
                    llm.setAutoMeasureEnabled(false);
                    recyclerViewFileList.setLayoutManager(llm);
                    recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
                    recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                            .VERTICAL));*/
                } else {
                    if (mIsBackPressed) {
                        if (checkIfDualFragment()) {
                            if (scrollPositionDualPane.containsKey(mFilePath)) {
                                Bundle b = scrollPositionDualPane.get(mFilePath);
                                if (mViewMode == FileConstants.KEY_LISTVIEW)
                                    ((LinearLayoutManager) llm).scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
                                else
                                    ((GridLayoutManager) llm).scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
                                recyclerViewFileList.stopScroll();
                            }
                        } else {
                            Log.d("TEST", "on onLoadFinished scrollpos--" + scrollPosition.entrySet());

                            if (scrollPosition.containsKey(mFilePath)) {
                                Bundle b = scrollPosition.get(mFilePath);
                                if (mViewMode == FileConstants.KEY_LISTVIEW)
                                    ((LinearLayoutManager) llm).scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
                                else
                                    ((GridLayoutManager) llm).scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));

                            }
                        }
                        mIsDataRefreshed = false;
                        mIsBackPressed = false;
                    }
                    recyclerViewFileList.stopScroll();
                }

                if (mTextEmpty.getVisibility() == View.VISIBLE) {
                    mTextEmpty.setVisibility(View.GONE);
                }
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

    public void startActionMode() {

//        fabCreateMenu.setVisibility(View.GONE);

        mBaseActivity.toggleFab(true);
        toggleDummyView(true);
        mBottomToolbar.setVisibility(View.VISIBLE);

        mBottomToolbar.startActionMode(new ActionModeCallback());
        mBottomToolbar.inflateMenu(R.menu.action_mode_bottom);
        mBottomToolbar.getMenu().clear();
        EnhancedMenuInflater.inflate(getActivity().getMenuInflater(), mBottomToolbar.getMenu(),
                true,
                mCategory);
        mBottomToolbar.setOnMenuItemClickListener(this);

    }

    public ActionMode getActionMode() {
        return mActionMode;
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
                    FileUtils.showMessage(getActivity(), mSelectedItemPositions.size() + " " +
                            getString(R.string.msg_cut_copy));
                    mCopiedData.clear();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        mCopiedData.add(fileInfoList.get(mSelectedItemPositions.keyAt(i)));
                    }
//                    mCopiedData.addAll(fileInfoList);
                    mIsMoveOperation = true;
                    togglePasteVisibility(true);
                    getActivity().supportInvalidateOptionsMenu();
                    mActionMode.finish();

                }
                break;
            case R.id.action_copy:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    mIsMoveOperation = false;
                    FileUtils.showMessage(getActivity(), mSelectedItemPositions.size() + " " + getString(R.string.msg_cut_copy));
                    mCopiedData.clear();
                    for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                        mCopiedData.add(fileInfoList.get(mSelectedItemPositions.keyAt(i)));
                    }
//                    mCopiedData.addAll(fileInfoList);
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
                    mFileUtils.showDeleteDialog(mBaseActivity, filesToDelete);
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
                    FileListFragment singlePaneFragment = null;
                    if (mSelectedItemPositions.size() < fileListAdapter.getItemCount()) {
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
                mFavItem.setVisible(false);

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
                    String fileName = fileInfoList.get(mSelectedItemPositions.keyAt(0)).getFileName();
                    if (fileName.startsWith(".")) {
                        SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                                "" + getString(R.string
                                .unhide));
                        // replace "*" with icon
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable
                                        .ic_unhide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mHideItem.setTitle(hideBuilder);
                    } else {
                        SpannableStringBuilder hideBuilder = new SpannableStringBuilder(" " + "  " +
                                "" + getString(R.string
                                .hide));
                        // replace "*" with icon
                        hideBuilder.setSpan(new ImageSpan(getActivity(), R.drawable
                                        .ic_hide_white), 0, 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mHideItem.setTitle(hideBuilder);
                    }
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


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rename:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        final String oldFilePath = fileInfoList.get(mSelectedItemPositions.keyAt(0)
                        ).getFilePath();
                        int renamedPosition = mSelectedItemPositions.keyAt(0);
                        String newFilePath = new File(oldFilePath).getParent();
                        renameDialog(oldFilePath,
                                newFilePath, renamedPosition);
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
//                        FileInfo fileInfo = fileInfoList.get(mSelectedItemPositions.keyAt(0));
                        ArrayList<FileInfo> paths = new ArrayList<>();
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                            paths.add(info);
                        }
                        mFileUtils.showCompressDialog(mBaseActivity, mFilePath, paths);
                    }
                    mActionMode.finish();
                    return true;
                case R.id.action_fav:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        int count = 0;
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            FileInfo info = fileInfoList.get(mSelectedItemPositions.keyAt(i));
                            // Fav option meant only for directories
                            if (info.isDirectory()) {
                                updateFavouritesGroup(info);
                                count++;
                            }

                        }
                        if (count > 0)
                            FileUtils.showMessage(getActivity(), getString(R.string.msg_added_to_fav));


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
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            infoList.add(fileInfoList.get(mSelectedItemPositions.keyAt(i)));
                        }
                        hideUnHideFiles(infoList);
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
            mSelectedItemPositions.clear();
            // FAB should be visible only for Files Category
            if (mCategory == 0) {
                mBaseActivity.toggleFab(false);
//                fabCreateMenu.setVisibility(View.VISIBLE);
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
                File oldFile = new File(oldFilePath);
                mBaseActivity.mFileOpsHelper.renameFile(mIsRootMode, oldFile, newFile,
                        position);
//                renameFile(oldFile, newFile, position);
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
        refreshList();
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
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG);
                    }

                    @Override
                    public void commandTerminated(int i, String s) {
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG);
                    }

                    @Override
                    public void commandCompleted(int i, int i2) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.completed), Toast.LENGTH_LONG);
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

        final File currentFile = new File(currentFilePath);
        mSelectedPath = null;
        final String currentFileName = currentFilePath.substring(currentFilePath.lastIndexOf("/")
                + 1, currentFilePath.lastIndexOf("."));
       /* String texts[] = new String[]{getString(R.string.enter_file_name), currentFileName,
                getString(R.string.extract), getString(R.string.extract),
                getString(R.string.dialog_cancel)};*/
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
        materialDialog.show();
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
                   /* new ExtractManager(FileListFragment.this)
                            .extract(currentFile, mSelectedPath, currentFileName);*/
                } else {
                    File newFile = new File(currentDir + "/" + currentFileName);
                    File currentFile = new File(currentFilePath);
                    mBaseActivity.mFileOpsHelper.extractFile(currentFile, newFile);
                   /* new ExtractManager(FileListFragment.this)
                            .extract(currentFile, currentDir, currentFileName);*/
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

    @Override
    public void getSelectedPath(String path) {
        mSelectedPath = path;
        if (buttonPathSelect != null) {
            buttonPathSelect.setText(mSelectedPath);
        }
    }


    private void updateFavouritesGroup(FileInfo info) {

        String name = info.getFileName();
        String path = info.getFilePath();
        FavInfo favInfo = new FavInfo();
        favInfo.setFileName(name);
        favInfo.setFilePath(path);
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        sharedPreferenceWrapper.addFavorite(getActivity(), favInfo);
        mBaseActivity.updateFavourites(name, path);
    }


    public void togglePasteVisibility(boolean isVisible) {
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

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();

    }


    public BitmapDrawable writeOnDrawable(String text) {

//        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
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
        private Drawable shadow;
        private Point mScaleFactor;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v, int count) {

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
            // Sets size parameter to member that will be used for scaling shadow image.
            mScaleFactor = size;

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

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        String items[] = new String[]{getString(R.string.action_copy), getString(R.string.move)};
        builder.title(getString(R.string.drag));
        builder.content(getString(R.string.dialog_to_placeholder, destinationDir));
        builder.positiveText(getString(R.string.msg_ok));
        builder.positiveColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        builder.items(items);
        builder.negativeText(getString(R.string.dialog_cancel));
        builder.negativeColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        builder.itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {

                final boolean isMoveOperation = position == 1;

                PasteConflictChecker conflictChecker = new PasteConflictChecker(mBaseActivity, FileListFragment
                        .this, destinationDir,
                        false, mIsRootMode, isMoveOperation, checkIfDualFragment());
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
                mActionMode.finish();
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    protected class myDragEventListener implements View.OnDragListener {

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
                    mIsDragInProgress = true;


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
                    mIsDragInProgress = false;
                    fileListAdapter.clearDragPos();
                    mDragPaths = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DROP:
//                    Log.d(TAG,"DRag drop"+pos);

                    View top = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position = recyclerViewFileList.getChildAdapterPosition(top);
                    Logger.log(TAG, "DROP new pos=" + position);
                    fileListAdapter.clearDragPos();


                    // Gets the item containing the dragged data
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    // Gets the text data from the item.
                    Intent dragData = item.getIntent();
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
//                    String sourceParent = "/storage/emulated/0";
//                    destinationDir = "/storage/emulated/0";

                    if (!new File(destinationDir).isDirectory()) {
                        destinationDir = new File(destinationDir).getParent();
                    }

                    boolean value = destinationDir.equals(sourceParent);
                    Logger.log(TAG, "Source parent=" + sourceParent + " " + value);


                    if (!destinationDir.equals(sourceParent)) {
                        /*System.out.println("Source=" + paths.get(0) + "Dest=" +
                                destinationDir);*/
                        Logger.log(TAG, "Source parent=" + sourceParent + " Dest=" +
                                destinationDir);
                        showDragDialog(paths, destinationDir);
                    } else {
                        final boolean isMoveOperation = false;
                        PasteConflictChecker conflictChecker = new PasteConflictChecker(mBaseActivity, FileListFragment
                                .this, destinationDir,
                                false, mIsRootMode, isMoveOperation, checkIfDualFragment());
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
//                    Object localState = event.getLocalState();
                    ArrayList<FileInfo> dragPaths = (ArrayList<FileInfo>) event.getLocalState();


                    Logger.log(TAG, "DRAG END new pos=" + position1);
//                    Logger.log(TAG, "DRAG END Local state=" + localState);
                    Logger.log(TAG, "DRAG END Local state=" + dragPaths);

                    Logger.log(TAG, "DRAG END result=" + event.getResult());
                    Logger.log(TAG, "DRAG END mCurrentDirSingle=" + mLastSinglePaneDir);
                    Logger.log(TAG, "DRAG END mCurrentDirDual=" + mLastDualPaneDir);


                    Log.d(TAG, "DRag end");
                    mIsDragInProgress = false;
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
                            if (dualPaneFragment != null && new File(mLastDualPaneDir).list().length == 0 && dragPaths.size() != 0) {
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
                            if (singlePaneFragment != null && new File(mLastSinglePaneDir).list().length == 0 && dragPaths.size() != 0) {
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

    ;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(FileListFragment.this.getClass().getSimpleName(), "On Create options " +
                "Fragment=");

        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_base, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mPasteItem = menu.findItem(R.id.action_paste);
        mPasteItem.setVisible(mIsPasteItemVisible);
        setupSearchView();

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
        // Here is where we are going to implement our filter logic
/*        final List<FileInfo> filteredModelList = filter(fileInfoList, query);
        fileListAdapter.animateTo(filteredModelList);
        recyclerViewFileList.scrollToPosition(0);*/
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
//        fileListAdapter.filter(query);
/*        mSearchView.clearFocus();
        MenuItemCompat.collapseActionView(mSearchItem);*/
        return false;
    }


    public void clearSelectedPos() {
        if (mSelectedItemPositions != null && mSelectedItemPositions.size() != 0) {
            mSelectedItemPositions.clear();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sortMode = 0;
        switch (item.getItemId()) {

            case R.id.action_paste:
                if (mCopiedData != null && mCopiedData.size() > 0) {

                    PasteConflictChecker conflictChecker = new PasteConflictChecker(mBaseActivity, FileListFragment
                            .this, mFilePath,
                            false, mIsRootMode, mIsMoveOperation, checkIfDualFragment());

                    ArrayList<FileInfo> info = new ArrayList<>();
                    info.addAll(mCopiedData);
                    conflictChecker.execute(info);
                    clearSelectedPos();
                    mCopiedData.clear();
                    togglePasteVisibility(false);


                }
                break;
            case R.id.action_view_list:
                if (mViewMode != FileConstants.KEY_LISTVIEW) {
                    mViewMode = FileConstants.KEY_LISTVIEW;
                    sharedPreferenceWrapper.savePrefs(getActivity(), mViewMode);
                    switchView();
                }
                break;
            case R.id.action_view_grid:
                if (mViewMode != FileConstants.KEY_GRIDVIEW) {
                    mViewMode = FileConstants.KEY_GRIDVIEW;
                    sharedPreferenceWrapper.savePrefs(getActivity(), mViewMode);
                    switchView();
                }
                break;

            case R.id.action_sort:
                showSortDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        String items[] = new String[]{getString(R.string.sort_name), getString(R.string.sort_name_desc),
                getString(R.string.sort_type), getString(R.string.sort_type_desc),
                getString(R.string.sort_size), getString(R.string.sort_size_desc),
                getString(R.string.sort_date), getString(R.string.sort_date_desc)};
        builder.title(getString(R.string.action_sort));
        builder.positiveText(getString(R.string.dialog_cancel));
        builder.positiveColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        builder.items(items);

        builder.alwaysCallSingleChoiceCallback();
        builder.itemsCallbackSingleChoice(getSortMode(), new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                fileInfoList = FileUtils.sortFiles(fileInfoList, position);
                persistSortMode(position);
                fileListAdapter.notifyDataSetChanged();
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
        } else {
            mGridItemWidth = dpToPx(100);
        /*    if (mGridColumns == 0 || mGridColumns == -1) {
                mGridColumns = getResources().getInteger(R
                        .integer.grid_columns);
            }*/
//            llm = new CustomGridLayoutManager(getActivity(), mGridColumns);
            llm = new CustomGridLayoutManager(getActivity(), mGridItemWidth);

        }

        llm.setAutoMeasureEnabled(false);
        recyclerViewFileList.setLayoutManager(llm);
        fileListAdapter = new FileListAdapter(FileListFragment.this, getContext(), fileInfoList,
                mCategory, mViewMode);
        recyclerViewFileList.setAdapter(fileListAdapter);
        initializeListeners();

    }

    public int dpToPx(int dp) {
        if (displayMetrics == null) displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void findNoOfGridColumns() {
        int width = recyclerViewFileList.getMeasuredWidth();
        Logger.log(TAG, "findNoOfGridColumns--width=" + width);
        mGridItemWidth = dpToPx(100);


//        mGridColumns = width / dptopx;
        Logger.log(TAG, "findNoOfGridColumns--Grid cols=" + mGridColumns);
        if (mIsLandscapeMode && mIsDualModeEnabledSettings && !mIsDataRefreshed) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_container);
            ((FileListFragment) fragment).refreshSpan();

        }
    }

    public void refreshSpan() {
        if (mViewMode == FileConstants.KEY_GRIDVIEW) {
            if (mGridItemWidth == 0) {
                mGridItemWidth = dpToPx(100);
            }
//            findNoOfGridColumns();
            Logger.log(TAG, "refreshSpan--llm=" + llm);
            if (llm != null) {
//                ((CustomGridLayoutManager) llm).setSpanCount(mGridColumns);
//                recyclerViewFileList.setHasFixedSize(true);
                llm = new CustomGridLayoutManager(getActivity(), mGridItemWidth);
                recyclerViewFileList.setLayoutManager(llm);
            }
        }
    }


    @Override
    public void onDestroy() {
//        Log.d(TAG, "on onDestroy--Fragment");
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        mPreferences.edit().putInt(FileConstants.KEY_GRID_COLUMNS, mGridColumns).apply();
        sharedPreferenceWrapper.savePrefs(getActivity(), mViewMode);
        super.onDestroyView();
    }

    private int refreshMediaStore(String path) {
        int count = 0;
        Uri deletedUri = FileUtils.getContentUriForDelete(getActivity(), path, mCategory);
        if (deletedUri != null) {
            count = getActivity().getContentResolver().delete(deletedUri, null, null);
        }
        return count;
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
