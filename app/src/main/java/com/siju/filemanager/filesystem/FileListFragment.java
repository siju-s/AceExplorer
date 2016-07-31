package com.siju.filemanager.filesystem;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.common.SharedPreferenceWrapper;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.model.ZipModel;
import com.siju.filemanager.filesystem.ui.CustomGridLayoutManager;
import com.siju.filemanager.filesystem.ui.CustomLayoutManager;
import com.siju.filemanager.filesystem.ui.DividerItemDecoration;
import com.siju.filemanager.filesystem.ui.EnhancedMenuInflater;
import com.siju.filemanager.filesystem.utils.FileUtils;
import com.siju.filemanager.filesystem.utils.PasteUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.siju.filemanager.BaseActivity.ACTION_VIEW_MODE;
import static com.siju.filemanager.R.id.textEmpty;


/**
 * Created by Siju on 13-06-2016.
 */

public class FileListFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<ArrayList<FileInfo>>,
        SearchView.OnQueryTextListener,
        Toolbar.OnMenuItemClickListener {

    //    private ListView fileList;
    private RecyclerView recyclerViewFileList;
    private View root;
    private final int LOADER_ID = 1000;
    private final int LOADER_ID_DUAL = 2000;

    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private boolean mIsDualMode;
    private String mFilePath;
    private String mFilePathOther;

    private int mCategory;
    private int mViewMode = FileConstants.KEY_LISTVIEW;
    private String mPath;
    private boolean mIsZip;
    private SharedPreferenceWrapper preference;
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
    private ArrayList<String> mDragPaths = new ArrayList<>();
    private PasteUtils mPasteUtils;
    //    private RecyclerView.LayoutManager llm;
    private RecyclerView.LayoutManager llm;
    private String mLastDualPaneDir;
    private String mLastSinglePaneDir;
    private boolean mDualPaneInFocus;
    private View viewDummy;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private StoragesFragment.ActionModeCommunicator mActionModeCallback;
    private String mZipParentPath;
    private int mZipLevelDual;
    private BaseActivity mBaseActivity;
    private boolean isDualPaneInFocus;
    private Toolbar mBottomToolbar;
    private ActionMode mActionMode;
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
    private String mCurrentZipDir;
    public ArrayList<ZipModel> totalZipList = new ArrayList<>();
    public ArrayList<ZipModel> zipChildren = new ArrayList<>();
    private boolean mInParentZip = true;


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
//        mActionModeCallback = (StoragesFragment.ActionModeCommunicator) getActivity();
        setHasOptionsMenu(true);
        initializeViews();
        mIsLandscapeMode = getResources().getConfiguration().orientation == Configuration
                .ORIENTATION_LANDSCAPE;
        mIsDualModeEnabledSettings = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(FileConstants.PREFS_DUAL_PANE, false);

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
                    Log.d("TAG", "Lib list =" + list.size());
                mBaseActivity.toggleViews(true);

            } else {
                mBaseActivity.toggleViews(false);
            }

            mIsZip = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
            mIsDualMode = getArguments().getBoolean(FileConstants.KEY_DUAL_MODE, false);
            mDualPaneInFocus = getArguments().getBoolean(FileConstants.KEY_FOCUS_DUAL, false);
            if (mDualPaneInFocus) {
                mLastDualPaneDir = mFilePath;
                mLastSinglePaneDir = mFilePathOther;
                Log.d("TAG", "on onActivityCreated dual focus Yes--singledir" + mLastSinglePaneDir + "dualDir=" + mLastDualPaneDir);

            } else {
                mLastSinglePaneDir = mFilePath;
                mLastDualPaneDir = mFilePathOther;
                Log.d("TAG", "on onActivityCreated dual focus No--singledir" + mLastSinglePaneDir + "dualDir=" + mLastDualPaneDir);
            }
   /*         if (mIsDualMode) {
                mLastDualPaneDir = mFilePath;

            } else {
                mLastSinglePaneDir = mFilePath;

            }*/
        }
        mViewMode = preference.getViewMode(getActivity());

        Log.d("TAG", "on onActivityCreated--Fragment" + mFilePath);
        Log.d("TAG", "View mode=" + mViewMode);


        args.putString(FileConstants.KEY_PATH, mFilePath);

        if (list == null || list.size() == 0) {
            if (FileListFragment.this instanceof FileListDualFragment) {
                isDualPaneInFocus = true;
            } else {
                isDualPaneInFocus = false;
            }
            if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                mBaseActivity.setNavDirectory(mFilePath, isDualPaneInFocus);
            }
            getLoaderManager().initLoader(LOADER_ID, args, this);
        } else {
            fileInfoList = new ArrayList<>();
            fileInfoList.addAll(list);
            recyclerViewFileList.setHasFixedSize(true);

            if (mViewMode == FileConstants.KEY_LISTVIEW) {
                llm = new CustomLayoutManager(getActivity());
            } else {
                llm = new CustomGridLayoutManager(getActivity(), getResources().getInteger(R
                        .integer.grid_columns));

            }
            llm.setAutoMeasureEnabled(false);
            recyclerViewFileList.setLayoutManager(llm);
            recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
            recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                    .VERTICAL));
        }

        fileListAdapter = new FileListAdapter(FileListFragment.this, getContext(), fileInfoList,
                mCategory, mViewMode);
        recyclerViewFileList.setAdapter(fileListAdapter);


        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (getActionMode() != null) {
                    if (mIsDualActionModeActive) {
                        if (FileListFragment.this instanceof FileListDualFragment) {
                            itemClickActionMode(position, false);
                        } else {
                            handleCategoryItemClick(position);
                        }
                    } else {
                        if (FileListFragment.this instanceof FileListDualFragment) {
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
                Logger.log("TAG", "On long click" + mStartDrag);
                itemClickActionMode(position, true);
                mLongPressedTime = System.currentTimeMillis();

                if (getActionMode() != null && fileListAdapter
                        .getSelectedCount() >= 1) {
//                    mDragPaths = new ArrayList<String>();
                    String path = fileInfoList.get(position).getFilePath();

                    if (!mDragPaths.contains(path)) {
                        mDragPaths.add(path);
                    }
                    mItemView = view;

                    mStartDrag = true;

                    mDragInitialPos = position;
                } else {
                    Logger.log("TAG", "On long click ELSE");

                }
            }
        });

        recyclerViewFileList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int event = motionEvent.getActionMasked();

                if (mStartDrag && event == MotionEvent.ACTION_UP) {
                    mStartDrag = false;
                } else if (mStartDrag && event == MotionEvent.ACTION_MOVE && mLongPressedTime != 0) {
                    long timeElapsed = System.currentTimeMillis() - mLongPressedTime;
//                    Logger.log("TAG", "On item touch time Elapsed" + timeElapsed);

                    if (timeElapsed > 1000) {
                        mLongPressedTime = 0;
                        mStartDrag = false;

//                        View top = recyclerViewFileList.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                        int position = mDragInitialPos; //recyclerViewFileList
//                                .getChildAdapterPosition(top);
                        mDragInitialPos = -1;
                        Intent intent = new Intent();
                        Logger.log("TAG", "On touch drag path size=" + mDragPaths.size());

                        intent.putStringArrayListExtra(FileConstants.KEY_PATH, mDragPaths);
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


    private void initializeViews() {
        recyclerViewFileList = (RecyclerView) root.findViewById(R.id.recyclerViewFileList);
        mTextEmpty = (TextView) root.findViewById(textEmpty);
        preference = new SharedPreferenceWrapper();
        //ViewParent viewParent = recyclerViewFileList.getParent().getParent();
        recyclerViewFileList.setOnDragListener(new myDragEventListener());
        viewDummy = root.findViewById(R.id.viewDummy);
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        int colorResIds[] = {R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorPrimaryDark};
        mSwipeRefreshLayout.setColorSchemeResources(colorResIds);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        mBottomToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_bottom);
    }

    private void handleCategoryItemClick(int position) {
        switch (mCategory) {
            case 0:
                // For file, open external apps based on Mime Type
                if (!fileInfoList.get(position).isDirectory()) {
                    String extension = fileInfoList.get(position).getExtension();
                    if (extension.equalsIgnoreCase("zip")) {
//                        showZipFileOptions(fileInfoList.get(position).getFilePath(),mFilePath);
                        String path = fileInfoList.get(position).getFilePath();
                        Bundle bundle = new Bundle();
                        bundle.putString(FileConstants.KEY_PATH, path);
                        bundle.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                        bundle.putBoolean(FileConstants.KEY_ZIP, true);
                        Intent intent = new Intent(getActivity(), BaseActivity.class);
       /*                 if (FileListFragment.this instanceof FileListDualFragment) {
                            intent.setAction(BaseActivity.ACTION_DUAL_VIEW_FOLDER_LIST);
                            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, true);
                        } else {
                            intent.setAction(BaseActivity.ACTION_VIEW_FOLDER_LIST);
                            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, false);
                        }*/
                        mIsZip = true;
                        mInParentZip = true;
                        mCurrentZipDir = null;
                        mZipParentPath = path;
                        reloadList(true, path);
/*
                        intent.putExtras(bundle);
                        startActivity(intent);*/


                    } else {
                        FileUtils.viewFile(getActivity(), fileInfoList.get(position).getFilePath(), fileInfoList.get
                                (position).getExtension());
                    }

                } else {
                    if (mIsZip) {
                        String name = zipChildren.get(position).getName();
                        mInParentZip = false;
                        mCurrentZipDir = name.substring(0, name.length() - 1);
                        reloadList(true, mZipParentPath);
                    } else {
                        Bundle bundle = new Bundle();
                        String path = fileInfoList.get(position).getFilePath();
                        bundle.putString(FileConstants.KEY_PATH, path);
                        bundle.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);

                        if (FileListFragment.this instanceof FileListDualFragment) {
                            isDualPaneInFocus = true;
                        } else {
                            isDualPaneInFocus = false;
                        }
                        mBaseActivity.setCurrentDir(path, isDualPaneInFocus);
                        mBaseActivity.setNavDirectory(path, isDualPaneInFocus);
                        getLoaderManager().restartLoader(LOADER_ID, bundle, this);
                    }


                /*    Intent intent = new Intent(getActivity(), BaseActivity.class);
                    if (FileListFragment.this instanceof FileListDualFragment) {
                        intent.setAction(BaseActivity.ACTION_DUAL_VIEW_FOLDER_LIST);
                        intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, true);
                    } else {
                        intent.setAction(BaseActivity.ACTION_VIEW_FOLDER_LIST);
                        intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, false);
                    }


                    intent.putExtras(bundle);
                    startActivity(intent);*/
                }
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                FileUtils.viewFile(getActivity(), fileInfoList.get(position).getFilePath(), fileInfoList.get(position)
                        .getExtension());

                break;

        }
    }


    private void itemClickActionMode(int position, boolean isLongPress) {
        fileListAdapter.toggleSelection(position, isLongPress);
        boolean hasCheckedItems = fileListAdapter.getSelectedCount() > 0;
        ActionMode actionMode = getActionMode();
        if (hasCheckedItems && actionMode == null) {
            // there are some selected items, start the actionMode
            startActionMode();

            if (FileListFragment.this instanceof FileListDualFragment) {
                mIsDualActionModeActive = true;
            } else {
                mIsDualActionModeActive = false;
            }
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
        ) + " selected");
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
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, mFilePath);
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    public void setCategory(int category) {
        mCategory = category;
    }


    public void reloadList(boolean isDualPaneClicked, String path) {
//        if (!mIsZip) {
        mFilePath = path;
//        }
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, mFilePath);
        args.putBoolean(FileConstants.KEY_ZIP, mIsZip);
     /*   if (isDualPaneClicked) {
            getLoaderManager().restartLoader(LOADER_ID_DUAL, args, this);
        } else {
            getLoaderManager().restartLoader(LOADER_ID, args, this);

        }*/
        getLoaderManager().restartLoader(LOADER_ID, args, this);

    }

    public boolean getIsZipMode() {
        return mIsZip;
    }

    public boolean checkZipMode() {
        if (mCurrentZipDir == null || mCurrentZipDir.length() == 0) {
            mIsZip = false;
            mZipParentPath = null;
            mInParentZip = true;
            return true;
        } else {
            mCurrentZipDir = new File(mCurrentZipDir).getParent();
            reloadList(true, mZipParentPath);
            return false;
        }
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        fileInfoList = new ArrayList<>();
        String path = args.getString(FileConstants.KEY_PATH);
        mSwipeRefreshLayout.setRefreshing(true);
        if (mIsZip) {

            return new FileListLoader(this, path, FileConstants.CATEGORY.ZIP_VIEWER.getValue(),
                    mCurrentZipDir, isDualPaneInFocus, mInParentZip);
        } else {
            return new FileListLoader(getContext(), path, mCategory);

        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
//        Log.d("TAG", "on onLoadFinished--" + data.size());
        if (data != null) {

            Log.d("TAG", "on onLoadFinished--" + data.size());
            // Stop refresh animation
            mSwipeRefreshLayout.setRefreshing(false);
            fileInfoList = data;
            fileListAdapter.setCategory(mCategory);
            fileListAdapter.updateAdapter(fileInfoList);

            if (!data.isEmpty()) {
                recyclerViewFileList.setHasFixedSize(true);

                if (mViewMode == FileConstants.KEY_LISTVIEW) {
                    llm = new CustomLayoutManager(getActivity());
                } else {
                    llm = new CustomGridLayoutManager(getActivity(), getResources().getInteger(R
                            .integer.grid_columns));

                }
                llm.setAutoMeasureEnabled(false);
                recyclerViewFileList.setLayoutManager(llm);
                recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
                recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                        .VERTICAL));
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

    public void startActionMode() {

//        fabCreateMenu.setVisibility(View.GONE);
        toggleDummyView(true);
        mBaseActivity.toggleFab(true);
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
//                    showMessage(mSelectedItemPositions.size() + " " + getString(R.string.msg_cut_copy));
                    mIsMoveOperation = true;
//                    togglePasteVisibility(true);
                    getActivity().supportInvalidateOptionsMenu();
                    mActionMode.finish();

                }
                break;
            case R.id.action_copy:
                if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                    mIsMoveOperation = false;
                /*    showMessage(mSelectedItemPositions.size() + " " + getString(R.string.msg_cut_copy));
                    togglePasteVisibility(true);*/
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
//                    showDialog(filesToDelete);
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
                    FileListFragment singlePaneFragment = null;
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
                }
            }

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rename:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        final String filePath = mFileList.get(mSelectedItemPositions.keyAt(0)).getFilePath();
//                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                        String fileName = mFileList.get(mSelectedItemPositions.keyAt(0)).getFileName();

                        final long id = mFileList.get(mSelectedItemPositions.keyAt(0)).getId();
//                        String extension = null;
                        String extension = mFileList.get(mSelectedItemPositions.keyAt(0)).getExtension();
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
                                    rename.setError(getResources().getString(R.string.msg_error_valid_name));
                                    return;
                                }
/*                                FileListFragment singlePaneFragment = (FileListFragment) getSupportFragmentManager()
                                        .findFragmentById(R
                                                .id.frame_container);
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
                                }*/
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
              /*      if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo fileInfo = mFileList.get(mSelectedItemPositions.keyAt(0));
                        showDialog(fileInfo);

                    }*/
                    mActionMode.finish();
                    return true;
                case R.id.action_archive:
                 /*   if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
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
                    }*/
                    mActionMode.finish();
                    return true;
                case R.id.action_fav:
       /*             if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            FileInfo info = mFileList.get(mSelectedItemPositions.keyAt(i));
                            updateFavouritesGroup(info);
                        }
                        showMessage(getString(R.string.msg_added_to_fav));
                    }*/
                    mActionMode.finish();
                    return true;

                case R.id.action_extract:
              /*      if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        FileInfo fileInfo = mFileList.get(mSelectedItemPositions.keyAt(0));
                        String currentFile = fileInfo.getFilePath();
                        showExtractOptions(currentFile, mCurrentDir);
                    }*/

                    mActionMode.finish();
                    return true;

                case R.id.action_hide:
                    if (mSelectedItemPositions != null && mSelectedItemPositions.size() > 0) {
                        ArrayList<FileInfo> infoList = new ArrayList<>();
                        for (int i = 0; i < mSelectedItemPositions.size(); i++) {
                            infoList.add(mFileList.get(mSelectedItemPositions.keyAt(i)));
                        }
//                        hideFiles(infoList);
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
            // FAB should be visible only for Files Category
            if (mCategory == 0) {
                mBaseActivity.toggleFab(false);
//                fabCreateMenu.setVisibility(View.VISIBLE);
            }
        }
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
//            shadow = v
//            shadow = new TextDrawable(getActivity(),"ABCDDDDDDDDDDDDDDDDDD");

//            shadow = new ColorDrawable(Color.LTGRAY);
            shadow = writeOnDrawable("" + count);
            //ColorDrawable(Color.RED);

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
            Log.d("TAG", "width=" + width);

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() / 2;
//            height = 100;

            Log.d("TAG", "height=" + height);


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

    private void showDragDialog(final ArrayList<String> sourcePaths, final String destinationDir) {
        final Dialog dialog = new Dialog(
                getActivity());
        dialog.setContentView(R.layout.dialog_drag);
        dialog.setCancelable(true);


        final RadioButton radioButtonCopy = (RadioButton) dialog.findViewById(R.id
                .radioButtonCopy);
        final RadioButton radioButtonMove = (RadioButton) dialog.findViewById(R.id
                .radioButtonMove);
        TextView textPath = (TextView) dialog.findViewById(R.id.textPath);
        Button buttonOk = (Button) dialog.findViewById(R.id.buttonOk);
        Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);


        dialog.show();
        final boolean isMoveOperation = radioButtonMove.isChecked();
        textPath.setText(destinationDir);

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPasteUtils = new PasteUtils(getActivity(), FileListFragment.this, destinationDir,
                        true);
                mPasteUtils.setMoveOperation(isMoveOperation);

                boolean isPasteConflict = false;
                for (int i = 0; i < sourcePaths.size(); i++) {
                    isPasteConflict = mPasteUtils.checkIfFileExists(sourcePaths.get(i), new File
                            (destinationDir));
                }

                if (!isPasteConflict) {
                    mPasteUtils.callAsyncTask();
                } else {
                    mPasteUtils.showDialog(sourcePaths.get(0));
//                        isPasteConflictDialogShown = false;

                }
                mActionMode.finish();

/*
                FileUtils.copyToDirectory(getActivity(), sourcePath, destinationDir,
                        isMoveOperation, action, null);*/
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

    protected class myDragEventListener implements View.OnDragListener {

        int oldPos = -1;

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View v, DragEvent event) {

            ViewParent parent = v.getParent().getParent();
//            Log.d("TAG", "parent" + parent);


            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();

            // Handles each of the expected events
            switch (action) {

                case DragEvent.ACTION_DRAG_STARTED:

                    Log.d("TAG", "DRag started" + v);
                    mIsDragInProgress = true;


                    // Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {

                        // As an example of what your application might do,
                        // applies a blue color tint to the View to indicate that it can accept
                        // data.
                        ColorFilter filter1 = new PorterDuffColorFilter(
                                Color.BLUE, PorterDuff.Mode.MULTIPLY);

//                        v.getBackground().setColorFilter(filter1);
  /*                      int color = ContextCompat.getColor(getActivity(), R.color.actionModeItemSelected);

                        v.setBackgroundColor(color);

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate();*/

                        // returns true to indicate that the View can accept the dragged data.
                        return true;

                    }

                    // Returns false. During the current drag and drop operation, this View will
                    // not receive events again until ACTION_DRAG_ENDED is sent.
                    return false;

                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d("TAG", "DRag entered");
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    View onTopOf = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int newPos = recyclerViewFileList.getChildAdapterPosition(onTopOf);
                    Log.d("TAG", "DRag location --pos=" + newPos);

                    if (oldPos != newPos && newPos != RecyclerView.NO_POSITION) {
/*                        int visiblePos = ((LinearLayoutManager) llm).findLastVisibleItemPosition();
                        if (newPos + 2 >= visiblePos) {
                            ((LinearLayoutManager) llm).scrollToPosition(newPos + 1);
                        }
//                        recyclerViewFileList.smoothScrollToPosition(newPos+2);
                        Logger.log("TAG", "drag old pos=" + oldPos + "new pos=" + newPos+"Last " +
                                "visible="+visiblePos);*/
                        // For scroll up
                        if (oldPos != RecyclerView.NO_POSITION && newPos < oldPos) {
                            int changedPos = newPos - 2;
                            Logger.log("TAG", "drag Location old pos=" + oldPos + "new pos=" + newPos +
                                    "changed pos=" + changedPos);
                            if (changedPos >= 0)
                                recyclerViewFileList.smoothScrollToPosition(changedPos);
                        } else {
                            int changedPos = newPos + 2;
                            // For scroll down
                            if (changedPos < fileInfoList.size())
                                recyclerViewFileList.smoothScrollToPosition(newPos + 2);
                            Logger.log("TAG", "drag Location old pos=" + oldPos + "new pos=" + newPos +
                                    "changed pos=" + changedPos);

                        }
                        oldPos = newPos;
                        fileListAdapter.setDraggedPos(newPos);
                    }
                    // Ignore the event
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("TAG", "DRag exit");
                    mIsDragInProgress = false;
                    fileListAdapter.clearDragPos();
                    mDragPaths = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DROP:
//                    Log.d("TAG","DRag drop"+pos);

                    View top = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position = recyclerViewFileList.getChildAdapterPosition(top);
                    Logger.log("TAG", "DROP new pos=" + position);
                    fileListAdapter.clearDragPos();


                    // Gets the item containing the dragged data
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    // Gets the text data from the item.
                    Intent dragData = item.getIntent();
                    ArrayList<String> paths = dragData.getStringArrayListExtra(FileConstants
                            .KEY_PATH);
       /*             String destinationDir = fileInfoList.get(position).getFilePath();
//                    Logger.log("TAG", "Source=" + path + "Dest=" + destinationDir);
                    if (!destinationDir.equals(paths.get(0))) {

                        showDragDialog(paths, destinationDir);
                    }*/

                    String destinationDir;
                    if (position != -1) {
                        destinationDir = fileInfoList.get(position).getFilePath();
                    } else {
                        destinationDir = mFilePath;
                    }

                    if (!destinationDir.equals(paths.get(0))) {
                        if (!new File(destinationDir).isDirectory()) {
                            destinationDir = new File(destinationDir).getParent();
                        }
                        System.out.println("Source=" + paths.get(0) + "Dest=" +
                                destinationDir);
                        Logger.log("TAG", "Source=" + paths.get(0) + "Dest=" + destinationDir);
                        showDragDialog(paths, destinationDir);
                    }

                    mDragPaths = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    View top1 = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position1 = recyclerViewFileList.getChildAdapterPosition(top1);
//                    Object localState = event.getLocalState();
                    ArrayList<String> dragPaths = (ArrayList<String>) event.getLocalState();


                    Logger.log("TAG", "DRAG END new pos=" + position1);
//                    Logger.log("TAG", "DRAG END Local state=" + localState);
                    Logger.log("TAG", "DRAG END Local state=" + dragPaths);

                    Logger.log("TAG", "DRAG END result=" + event.getResult());
                    Logger.log("TAG", "DRAG END mCurrentDirSingle=" + mLastSinglePaneDir);
                    Logger.log("TAG", "DRAG END mCurrentDirDual=" + mLastDualPaneDir);


                    Log.d("TAG", "DRag end");
                    mIsDragInProgress = false;
                    fileListAdapter.clearDragPos();
                    if (!event.getResult() && position1 == RecyclerView.NO_POSITION) {
                        ViewParent parent1 = v.getParent().getParent();

                        if (((View) parent1).getId() == R.id.frame_container_dual) {
                            Logger.log("TAG", "DRAG END parent dual =" + true);
                            FileListDualFragment dualPaneFragment = (FileListDualFragment)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.frame_container_dual);
                            Logger.log("TAG", "DRAG END Dual dir=" + mLastDualPaneDir);

//                            Logger.log("TAG", "Source=" + mDragPaths.get(0) + "Dest=" + mLastDualPaneDir);
                            if (dualPaneFragment != null && new File(mLastDualPaneDir).list().length == 0 && dragPaths.size() != 0) {
//                                if (!destinationDir.equals(paths.get(0))) {
                                showDragDialog(dragPaths, mLastDualPaneDir);
//                                }
                            }
                        } else {
                            Logger.log("TAG", "DRAG END parent dual =" + false);
                            FileListFragment singlePaneFragment = (FileListFragment)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.main_container);
                            Logger.log("TAG", "DRAG END single dir=" + mLastSinglePaneDir);

//                            Logger.log("TAG", "Source=" + mDragPaths.get(0) + "Dest=" + mLastDualPaneDir);
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
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
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
        setupSearchView();

    }

    private void setupSearchView() {
//        mSearchView.setIconifiedByDefault(true);
        // Disable full screen keyboard in landscape
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSearchView.setOnQueryTextListener(this);
//        mSearchView.setSubmitButtonEnabled(true);
//        mSearchView.setQueryHint("Search Here");
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

/*    private List<FileInfo> filter(List<FileInfo> models, String query) {
        query = query.toLowerCase();

        final List<FileInfo> filteredModelList = new ArrayList<>();
        for (FileInfo model : models) {
            final String text = model.getFileName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_paste:
/*                    pasteOperationCleanUp();
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


                    }*/
                break;


            case R.id.action_view_list:
                if (mViewMode != FileConstants.KEY_LISTVIEW) {
                    mViewMode = FileConstants.KEY_LISTVIEW;
                    preference.savePrefs(getActivity(), mViewMode);
                    switchView();
                }
                break;
            case R.id.action_view_grid:
                if (mViewMode != FileConstants.KEY_GRIDVIEW) {
                    mViewMode = FileConstants.KEY_GRIDVIEW;
                    preference.savePrefs(getActivity(), mViewMode);
                    switchView();
                }
                break;

            case R.id.action_sort_name_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_NAME);
                fileListAdapter.notifyDataSetChanged();
                break;
            case R.id.action_sort_name_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_NAME_DESC);
                fileListAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_type_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_TYPE);
                fileListAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_type_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_TYPE_DESC);
                fileListAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_size_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_SIZE);
                fileListAdapter.notifyDataSetChanged();

                break;

            case R.id.action_sort_size_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_SIZE_DESC);
                fileListAdapter.notifyDataSetChanged();

                break;
            case R.id.action_sort_date_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_DATE);
                fileListAdapter.notifyDataSetChanged();
                break;
            case R.id.action_sort_date_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_DATE_DESC);
                fileListAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchView() {
        Bundle bundle = new Bundle();
        bundle.putString(FileConstants.KEY_PATH, mFilePath);
        Intent intent = new Intent(getActivity(), BaseActivity.class);
        if (FileListFragment.this instanceof FileListDualFragment) {
            intent.setAction(BaseActivity.ACTION_DUAL_VIEW_FOLDER_LIST);
            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, true);

        } else {
            intent.setAction(BaseActivity.ACTION_VIEW_FOLDER_LIST);
            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, false);
            intent.putExtra(ACTION_VIEW_MODE, mViewMode);
            intent.putExtra(FileConstants.KEY_CATEGORY, mCategory);
        }

        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void sortFiles(ArrayList<FileInfo> files, int sortMode) {

        switch (sortMode) {
            case 0:
                Collections.sort(files, FileUtils.comparatorByName);
                break;
            case 1:
                Collections.sort(files, FileUtils.comparatorByNameDesc);
                break;
            case 2:
                Collections.sort(files, FileUtils.comparatorByType);
                break;
            case 3:
                Collections.sort(files, FileUtils.comparatorByTypeDesc);
                break;
            case 4:
                Collections.sort(files, FileUtils.comparatorBySize);
                break;
            case 5:
                Collections.sort(files, FileUtils.comparatorBySizeDesc);
                break;
            case 6:
                Collections.sort(files, FileUtils.comparatorByDate);
                break;
            case 7:
                Collections.sort(files, FileUtils.comparatorByDateDesc);
                break;

        }
    }

    @Override
    public void onDestroy() {
//        Log.d("TAG", "on onDestroy--Fragment");
        super.onDestroy();

    }


}
