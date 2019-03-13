package com.siju.acexplorer.storage.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.appmanager.AppDetailActivity;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.view.AceActivity;
import com.siju.acexplorer.main.view.dialog.DialogHelper;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager;
import com.siju.acexplorer.storage.view.custom.CustomLayoutManager;
import com.siju.acexplorer.storage.view.custom.DividerItemDecoration;
import com.siju.acexplorer.storage.view.custom.GridItemDecoration;
import com.siju.acexplorer.storage.view.custom.recyclerview.FastScrollRecyclerView;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.ui.peekandpop.PeekAndPop;
import com.siju.acexplorer.utils.ConfigurationHelper;
import com.siju.acexplorer.utils.InstallHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.main.model.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.main.model.groups.Category.ALBUM_DETAIL;
import static com.siju.acexplorer.main.model.groups.Category.ARTIST_DETAIL;
import static com.siju.acexplorer.main.model.groups.Category.FILES;
import static com.siju.acexplorer.main.model.groups.Category.FOLDER_IMAGES;
import static com.siju.acexplorer.main.model.groups.Category.FOLDER_VIDEOS;
import static com.siju.acexplorer.main.model.groups.Category.GENRE_DETAIL;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isSortOrActionModeUnSupported;
import static com.siju.acexplorer.main.model.helper.MediaStoreHelper.removeMedia;
import static com.siju.acexplorer.main.model.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.main.model.helper.MediaStoreHelper.updateMedia;
import static com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastNougat;
import static com.siju.acexplorer.main.model.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.main.model.helper.ViewHelper.viewFile;
import static com.siju.acexplorer.main.view.dialog.DialogHelper.openWith;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_RELOAD_LIST;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OLD_FILES;
import static com.siju.acexplorer.storage.model.operations.Operations.HIDE;

public class FilesView extends RecyclerView.OnScrollListener
        implements FileListAdapter.OnItemLongClickListener, View.OnTouchListener,
                   FileListAdapter.SearchCallback {

    private static final String TAG = FilesView.class.getSimpleName();

    private AppCompatActivity activity;
    private Context           context;

    private FastScrollRecyclerView  fileList;
    private FileListAdapter         fileListAdapter;
    private StoragesUiView          storagesUiView;
    private DragHelper              dragHelper;
    private PeekAndPop              peekAndPop;
    private MenuControls            menuControls;
    private OperationResultReceiver operationResultReceiver;
    private Category                category;
    private FileInfo                fileInfo;

    private TextView                   emptyText;
    private SwipeRefreshLayout         swipeRefreshLayout;
    private RecyclerView.LayoutManager layoutManager;
    private DividerItemDecoration      dividerItemDecoration;
    private GridItemDecoration         mGridItemDecoration;
    private View                       mItemView;

    private       ArrayList<FileInfo>     fileInfoList;
    private final HashMap<String, Bundle> scrollPosition         = new HashMap<>();
    private       ArrayList<FileInfo>     draggedData            = new ArrayList<>();
    private       SparseBooleanArray      mSelectedItemPositions = new SparseBooleanArray();

    private String  currentDir;
    private String  bucketName;
    private String  extension;
    private String  filePath;
    private long    mLongPressedTime;
    private long    id;
    private int     viewMode;
    private int     gridCols;
    private boolean isActionModeActive;
    private boolean shouldStopAnimation = true;
    private boolean isDragStarted;


    FilesView(AppCompatActivity activity, StoragesUiView storagesUiView, int viewMode) {
        this.activity = activity;
        this.context = storagesUiView.getContext();
        this.storagesUiView = storagesUiView;
        this.viewMode = viewMode;
        operationResultReceiver = new OperationResultReceiver(getContext(), this);
        setupUI();
    }

    private void setupUI() {
        initializeViews();
        dragHelper = new DragHelper(getContext(), this);
        setupList();
        initializeListeners();
        registerReceiver();
    }

    private Context getContext() {
        return context;
    }

    public AppCompatActivity getActivity() {
        return activity;
    }

    void setMenuControls(MenuControls menuControls) {
        this.menuControls = menuControls;
    }

    private void initializeViews() {
        fileList = storagesUiView.findViewById(R.id.recyclerViewFileList);
        emptyText = storagesUiView.findViewById(R.id.textEmpty);
        setupSwipeRefresh();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initializeListeners() {
        fileList.setOnDragListener(dragHelper.getDragEventListener());
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
        fileListAdapter.setOnItemClickListener(onItemClickListener);
        fileListAdapter.setOnItemLongClickListener(this);
        fileList.addOnScrollListener(this);
        fileList.setOnTouchListener(this);
    }

    private void setupList() {
        fileList.setHasFixedSize(true);
        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            fileList.setLayoutManager(layoutManager);
        }
        else {
            refreshSpan(((AceActivity) getActivity()).getConfiguration());
        }
        fileList.setItemAnimator(new DefaultItemAnimator());
        peekAndPop = new PeekAndPop.Builder(getActivity()).peekLayout(R.layout.peek_pop).
                parentViewGroupToDisallowTouchEvents(fileList).build();
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList,
                category, viewMode, peekAndPop);
        fileListAdapter.setSearchCallback(this);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_RELOAD_LIST);
        filter.addAction(ACTION_OP_REFRESH);
        getActivity().registerReceiver(operationResultReceiver, filter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout = storagesUiView.findViewById(R.id.swipeRefreshLayout);
        int[] colorResIds = {R.color.colorPrimaryDark, R.color.colorPrimary, R.color
                .colorPrimaryDark};
        swipeRefreshLayout.setColorSchemeResources(colorResIds);
        swipeRefreshLayout.setDistanceToTriggerSync(500);
    }


    private FileListAdapter.OnItemClickListener onItemClickListener = new FileListAdapter.OnItemClickListener() {
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
                case R.id.buttonNext:
                    fileListAdapter.loadPeekView(position + 1, false);
                    break;
                case R.id.buttonPrev:
                    fileListAdapter.loadPeekView(position - 1, false);
                    break;
                default:
                    if (isActionModeActive() && !menuControls.isPasteOp()) {
                        itemClickActionMode(position, false);
                    }
                    else {
                        handleItemClick(position);
                    }
                    break;
            }
        }

        @Override
        public boolean canShowPeek() {
            return !isActionModeActive();
        }
    };

    boolean isActionModeActive() {
        return isActionModeActive;
    }

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
            case GIF:
            case RECENT_AUDIO:
            case RECENT_DOCS:
            case RECENT_IMAGES:
            case RECENT_VIDEOS:
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
            case RECENT_APPS:
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

            case RECENT:
                category = fileInfoList.get(position).getCategory();
                reloadList(null, category);
                break;

            case APP_MANAGER:
                AppDetailActivity.openAppInfo(getContext(), fileInfoList.get(position).getFilePath());
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
        boolean isDualPaneInFocus = storagesUiView.getFragment() instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);
        Log.d(TAG, "onDirectoryClicked() called with: isDualPaneInFocus = [" + isDualPaneInFocus + "]");

        if (storagesUiView.isZipMode()) {
            storagesUiView.getZipViewer().onDirectoryClicked(position);
        }
        else {
            calculateScroll(currentDir);
            String path = fileInfoList.get(position).getFilePath();
            category = FILES;
            reloadList(path, category);
        }
    }

    void pauseAutoPlayVid() {
        if (isPeekMode()) {
            fileListAdapter.stopAutoPlayVid();
        }
    }

    boolean isPeekMode() {
        return peekAndPop.getPeekView().isShown();
    }

    void endPeekMode() {
        fileListAdapter.stopAutoPlayVid();
    }


    private void onFileClicked(int position) {
        String filePath = fileInfoList.get(position).getFilePath();
        extension = fileInfoList.get(position).getExtension().toLowerCase();

        if (storagesUiView.isZipFile(filePath)) {
            storagesUiView.openZipViewer(filePath);
        }
        else {
            if (storagesUiView.isZipMode()) {
                storagesUiView.onZipFileClicked(position);
            }
            else {
                this.filePath = filePath;
                viewFile(getContext(), filePath, extension, alertDialogListener);
            }
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Logger.log(TAG, "On long click" + isDragStarted);
        if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION ||
                isSortOrActionModeUnSupported(category)) {
            return;
        }

        if (canExecuteLongPress()) {
            itemClickActionMode(position, true);
            mLongPressedTime = System.currentTimeMillis();

            if (isActionModeActive && fileListAdapter.getSelectedCount() >= 1) {
                swipeRefreshLayout.setEnabled(false);
                mItemView = view;
                isDragStarted = true;
            }
        }
    }

    private boolean canExecuteLongPress() {
        return !storagesUiView.isZipMode() && !menuControls.isPasteOp();
    }


    private void itemClickActionMode(int position, boolean isLongPress) {
        fileListAdapter.toggleSelection(position, isLongPress);

        boolean hasCheckedItems = fileListAdapter.getSelectedCount() > 0;
        if (hasCheckedItems && !isActionModeActive) {
            // there are some selected items, start the actionMode
            startActionMode();
        }
        else if (!hasCheckedItems && isActionModeActive) {
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

    void calculateScroll(String currentDir) {
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

    void calculateScroll() {
        calculateScroll(currentDir);
    }


    private void putScrolledPosition(String path, Bundle position) {
        scrollPosition.put(path, position);
    }


    void removeScrolledPos(String path) {
        if (path == null) {
            return;
        }
        scrollPosition.remove(path);
    }

    private boolean search;

    @Override
    public void updateList(ArrayList<FileInfo> fileInfoArrayList) {
        this.fileInfoList = fileInfoArrayList;
        if (isSearch() && fileInfoList.isEmpty()) {
            emptyText.setText(getContext().getString(R.string.no_search_results));
            emptyText.setVisibility(View.VISIBLE);
        }
        else {
            emptyText.setVisibility(View.GONE);
        }
    }


    void clearSelection() {
        fileListAdapter.removeSelection();
    }

    void reloadList(String path, Category category) {
        Log.d(TAG, "reloadList() called with: path = [" + path + "], category = [" + category + "]");
        currentDir = path;
        this.category = category;
        boolean isDualPaneInFocus = storagesUiView.getFragment() instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);
        storagesUiView.setCurrentDir(currentDir);
        storagesUiView.setCategory(category);
        storagesUiView.onReloadList(path);
        menuControls.setCategory(category);
        menuControls.setCurrentDir(currentDir);
        menuControls.setupSortVisibility();
        if (isActionModeActive() && (storagesUiView.checkIfLibraryCategory(category) || !menuControls.isPasteOp())) {
            menuControls.endActionMode();
        }
        storagesUiView.refreshList();
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

    private boolean isSearch() {
        return search;
    }

    void onDataLoaded(ArrayList<FileInfo> data) {
        swipeRefreshLayout.setRefreshing(false);
        Log.d(TAG, "onDataLoaded: search:" + search);
        if (isSearch()) {
            return;
        }
        if (data != null) {
            Log.d(TAG, "onDataLoaded: " + data.size());

            shouldStopAnimation = true;
            fileInfoList = data;
            fileListAdapter.setCategory(category);
            fileList.setAdapter(fileListAdapter);
            fileListAdapter.updateAdapter(fileInfoList);

            addItemDecoration();

            if (data.isEmpty()) {
                showEmptyPlaceholder();
            }
            else {
                getScrolledPosition();
                fileList.stopScroll();
                emptyText.setVisibility(View.GONE);
            }
        }
    }

    private void showEmptyPlaceholder() {
        emptyText.setText(getContext().getString(R.string.no_files));
        emptyText.setVisibility(View.VISIBLE);
    }

    private void addItemDecoration() {

        switch (viewMode) {
            case ViewMode.LIST:
                if (dividerItemDecoration == null) {
                    dividerItemDecoration = new DividerItemDecoration(getContext(), currentTheme);
                }
                else {
                    fileList.removeItemDecoration(dividerItemDecoration);
                }
                fileList.addItemDecoration(dividerItemDecoration);
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

    private void getScrolledPosition() {
        if (currentDir != null && scrollPosition.containsKey(currentDir)) {
            Bundle bundle = scrollPosition.get(currentDir);
            if (bundle == null) {
                return;
            }
            if (viewMode == ViewMode.LIST) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(bundle.getInt
                        (FileConstants
                                .KEY_POSITION), bundle.getInt(FileConstants.KEY_OFFSET));
            }
            else {
                ((GridLayoutManager) layoutManager).scrollToPositionWithOffset(bundle.getInt
                        (FileConstants
                                .KEY_POSITION), bundle.getInt(FileConstants.KEY_OFFSET));
            }
        }
    }

    void refreshSpan(Configuration configuration) {
        Log.d(TAG, "refreshSpan() called " + this + " dual:" + storagesUiView.isDualModeEnabled());
        if (viewMode == ViewMode.GRID) {
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT || !storagesUiView
                    .isDualModeEnabled()) {
                gridCols = ConfigurationHelper.getStorageGridCols(configuration);
            }
            else {
                gridCols = ConfigurationHelper.getStorageDualGridCols(configuration);
            }
            layoutManager = new CustomGridLayoutManager(getActivity(), gridCols);
            fileList.setLayoutManager(layoutManager);
        }
    }

    private void startActionMode() {
        isActionModeActive = true;
        clearSelectedPos();
        storagesUiView.onActionModeStarted();
        draggedData.clear();
        menuControls.startActionMode();
    }

    void endActionMode() {
        isDragStarted = false;
        isActionModeActive = false;
        fileListAdapter.clearDragPos();
        fileListAdapter.removeSelection();
        swipeRefreshLayout.setEnabled(true);
        storagesUiView.onActionModeEnded();
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
        }
        else {
            menuControls.setToolbarText(count + " " +
                    getContext().getResources().getString(R.string.selected));
            fileListAdapter.notifyDataSetChanged();
        }
    }

    private void setSelectedItemPos(SparseBooleanArray selectedItemPos) {
        mSelectedItemPositions = selectedItemPos;
        menuControls.setupMenuVisibility(selectedItemPos);
    }

    void onSelectAllClicked() {
        if (mSelectedItemPositions != null) {
            if (mSelectedItemPositions.size() < fileListAdapter.getItemCount()) {
                toggleSelectAll(true);
            }
            else {
                toggleSelectAll(false);
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
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
        }
        else if (event == MotionEvent.ACTION_MOVE && mLongPressedTime !=
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
                        storagesUiView.startDragAndDrop(data, shadowBuilder, draggedData, 0);
                    }
                    else {
                        storagesUiView.startDrag(data, shadowBuilder, draggedData, 0);
                    }
                }
            }
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction()
                == MotionEvent.ACTION_CANCEL) {
            storagesUiView.performClick();
        }
        return false;
    }

    int onDragLocationEvent(DragEvent event, int oldPos) {
        View onTopOf = fileList.findChildViewUnder(event.getX(), event.getY());
        if (onTopOf == null) {
            return oldPos;
        }
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
            }
            else {
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

    void onDragExit() {
//        fileListAdapter.clearDragPos();
//        draggedData = new ArrayList<>();
    }

    void onDragEnded(View view, DragEvent event) {

        View top1 = fileList.findChildViewUnder(event.getX(), event.getY());
        if (top1 == null) {
            return;
        }
        int position1 = fileList.getChildAdapterPosition(top1);

        Logger.log(TAG, "onDragEnded: " + category + " result:" + event.getResult() + " position:" +
                position1 + " this:" + FilesView.this);

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
            }

        }
        view.post(new Runnable() {
            @Override
            public void run() {
                menuControls.endActionMode();
            }
        });
    }

    void onDragDropEvent(DragEvent event) {
        if (!category.equals(FILES)) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_unsupported),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        View top = fileList.findChildViewUnder(event.getX(), event.getY());
        int position = fileList.getChildAdapterPosition(top);
        @SuppressWarnings("unchecked")
        ArrayList<FileInfo> draggedFiles = (ArrayList<FileInfo>) event.getLocalState();
        ArrayList<String> paths = new ArrayList<>();

                  /*  ArrayList<FileInfo> paths = dragData.getParcelableArrayListExtra(FileConstants
                            .KEY_PATH);*/

        String destinationDir;
        if (position != -1) {
            destinationDir = fileInfoList.get(position).getFilePath();
        }
        else {
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
            }
            else {
                ArrayList<FileInfo> info = new ArrayList<>(draggedFiles);
                Logger.log(TAG, "Source=" + draggedFiles.get(0) + "Dest=" +
                        destinationDir);
                onPasteAction(false, info, destinationDir);
            }
        }

//        draggedData = new ArrayList<>();
    }

    void onPasteAction(boolean isMove, ArrayList<FileInfo> filesToPaste, String
            destinationDir) {
        menuControls.endActionMode();
        if (isMediaScannerActive() && isMediaScanning(destinationDir)) {
            storagesUiView.onOperationFailed(Operations.COPY);
            return;
        }
        storagesUiView.onPasteAction(isMove, filesToPaste, destinationDir);
    }


    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

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


    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            removeScrolledPos(currentDir);
            storagesUiView.refreshList();
        }
    };

    public int getViewMode() {
        return viewMode;
    }

    int getNewViewMode() {
        if (viewMode == ViewMode.LIST) {
            viewMode = ViewMode.GRID;
        }
        else {
            viewMode = ViewMode.LIST;
        }
        Log.d(TAG, "getNewViewMode: " + viewMode);
        return viewMode;
    }

    void clearList() {
        Logger.log(TAG, "clearList");
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        swipeRefreshLayout.setRefreshing(true);
    }

    public void switchView() {
        if (viewMode == ViewMode.LIST) {
            layoutManager = new CustomLayoutManager(getActivity());
            fileList.setLayoutManager(layoutManager);

        }
        else {
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
                dividerItemDecoration = new DividerItemDecoration(getContext(), currentTheme);
            }
            dividerItemDecoration.setOrientation();
            fileList.addItemDecoration(dividerItemDecoration);
        }
        else {
            if (dividerItemDecoration != null) {
                fileList.removeItemDecoration(dividerItemDecoration);
            }
            addItemDecoration();
        }

        initializeListeners();
    }


    private void toggleDragData(FileInfo fileInfo) {
        if (draggedData.contains(fileInfo)) {
            draggedData.remove(fileInfo);
        }
        else {
            draggedData.add(fileInfo);
        }
    }

    void endDrag() {
        isDragStarted = false;
    }


    void onQueryChange(String query) {
        fileListAdapter.filter(query);
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(operationResultReceiver);
    }

    void onDestroyView() {
        fileList.stopScroll();
        if (fileListAdapter != null) {
            fileListAdapter.onDetach();
        }
    }

    int getGridCols() {
        return gridCols;
    }

    void onHomeClicked() {
        fileListAdapter.stopAutoPlayVid();
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
    }

    List<FileInfo> getFileList() {
        return fileInfoList;
    }


    void setGridCols(int anInt) {
        this.gridCols = anInt;
    }

    SparseBooleanArray getSelectedItems() {
        return mSelectedItemPositions;

    }

    void clearSelectedPos() {
        mSelectedItemPositions = new SparseBooleanArray();

    }

    void onFilesDeleted(List<FileInfo> deletedFilesList) {
        fileInfoList.removeAll(deletedFilesList);
        storagesUiView.removeFavorite(deletedFilesList);
        fileListAdapter.setStopAnimation(true);
        fileListAdapter.updateAdapter(fileInfoList);
    }


    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public void setCategory(Category category) {
        Log.d(TAG, "setCategory() called with: category = [" + category + "]");
        this.category = category;
    }

    // Dialog for SAF and APK dialog
    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper
            .AlertDialogListener() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPositiveButtonClick(View view) {
            String mimeType = getSingleton().getMimeTypeFromExtension(extension);
            Uri uri = createContentUri(getContext(), filePath);
            extension = null;
            if (mimeType == null) {
                openWith(uri, getContext());
                return;
            }
            boolean canInstallApp = InstallHelper.canInstallApp(getContext());
            if (canInstallApp) {
                InstallHelper.openInstallAppScreen(getContext(), uri);
            }
            else {
                InstallHelper.requestUnknownAppsInstallPermission(storagesUiView.getFragment());
            }
        }

        @Override
        public void onNegativeButtonClick(View view) {
        }

        @Override
        public void onNeutralButtonClick(View view) {
            storagesUiView.openZipViewer(filePath);
        }
    };

    private Theme currentTheme;

    public void setTheme(Theme currentTheme) {
        this.currentTheme = currentTheme;
    }

    String getBucketName() {
        return bucketName;
    }

    void setBucketName(String name) {
        this.bucketName = name;
    }

    public long getId() {
        return id;
    }

    void onRename(Intent intent, Operations operation) {
        storagesUiView.dismissDialog();
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
        if (!storagesUiView.showHidden() && new File(newFile).getName().startsWith(".")) {
            fileInfoList.remove(position);
            fileListAdapter.setList(fileInfoList);
            fileListAdapter.notifyItemRemoved(position);
        }
        else {
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

    }

    void showRenameDialog(FileInfo fileInfo, String text) {
        this.fileInfo = fileInfo;
        String title = getContext().getString(R.string.action_rename);
        String[] texts = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string.action_rename), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.RENAME, text, dialogListener);
    }

    private DialogHelper.DialogCallback dialogListener = new DialogHelper.DialogCallback() {


        @Override
        public void onPositiveButtonClick(Dialog dialog, Operations operation, String name) {
            storagesUiView.setDialog(dialog);
            if (operation == Operations.RENAME) {
                String filePath = fileInfo.getFilePath();
                storagesUiView.renameFile(filePath, new File(filePath).getParent(), name);
            }
        }

        @Override
        public void onNegativeButtonClick(Operations operations) {

        }
    };

    void openInstallScreen() {
        Uri uri = createContentUri(getContext(), filePath);
        InstallHelper.openInstallAppScreen(getContext(), uri);
    }

    boolean isMediaScannerActive() {
        return operationResultReceiver.isMediaScannerActive();
    }

    boolean isMediaScanning(String filePath) {
        return operationResultReceiver.isMediaScanningPath(filePath);
    }

    void onFileCreated() {
        calculateScroll();
        storagesUiView.refreshList();
    }

    void dismissFAB() {
        storagesUiView.dismissFAB();
    }

    void reloadList() {
        storagesUiView.refreshList();
    }

    @Override
    public void setSearch(boolean search) {
        this.search = search;
    }
}
