package com.siju.acexplorer.storage.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.showToast
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.main.model.helper.ShareHelper
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.helper.RecentDataConverter
import com.siju.acexplorer.storage.model.RecentTimeData
import com.siju.acexplorer.storage.model.RecentTimeHelper.isRecentTimeLineCategory
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager
import com.siju.acexplorer.ui.peekandpop.PeekPopUiView
import com.siju.acexplorer.ui.peekandpop.PeekPopView
import com.siju.acexplorer.utils.ConfigurationHelper
import com.siju.acexplorer.utils.ScrollInfo
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

private const val TAG = "FilesList"
private const val DELAY_SCROLL_UPDATE_MS = 100L

class FilesList(private val fileListHelper: FileListHelper,
                val view: View,
                private var viewMode: ViewMode,
                var category: Category,
                var sortMode: Int) : View.OnTouchListener
{
    private val dragHelper = DragHelper(view.context, this)

    private lateinit var fileList: FastScrollRecyclerView
    private lateinit var emptyText: TextView

    private var recentData: ArrayList<RecentTimeData.RecentDataItem>? = null
    private var itemView: View? = null
    private var adapter: FileListAdapter? = null
    private var recentAdapter: RecentAdapter? = null
    private var multiSelectionHelper: MultiSelectionHelper? = null
    private var peekAndPop: PeekPopView? = null

    init {
        initializeViews()
        setupList()
    }

    private fun initializeViews() {
        fileList = view.findViewById(R.id.recyclerViewFileList)
        emptyText = view.findViewById(R.id.textEmpty)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupList(zipMode: Boolean = false) {
        Log.d(TAG, "setupList:category:$category, viewMode:$viewMode")
        setLayoutManager(fileList, viewMode, category)
        setupPeekPop()
        setAdapter(zipMode)
        fileList.setOnTouchListener(this)
    }

    private fun setAdapter(isZipMode: Boolean = false) {
        if (!isZipMode && isRecentTimeLineCategory(category)) {
            setRecentAdapter()
            return
        }
        adapter = FileListAdapter(
                viewMode,
                {
                    fileListHelper.handleItemClick(it.first, it.second)
                },
                { fileInfo, pos, view ->
                    fileListHelper.handleLongItemClick(fileInfo, pos)
                    val hasSelectedItems = adapter?.getMultiSelectionHelper()?.hasSelectedItems()
                    if (hasSelectedItems == true) {
                        this.itemView = view
                    }
                },
                peekAndPop
        )
        adapter?.setMainCategory(category)
        fileList.adapter = adapter
        fileList.setOnDragListener(dragHelper.dragListener)
    }

    private fun setRecentAdapter() {
        recentAdapter = RecentAdapter(
                viewMode,
                {
                    fileListHelper.handleItemClick(it.first, it.second)
                },
                { fileInfo, pos, view ->
                    fileListHelper.handleLongItemClick(fileInfo, pos)
                    val hasSelectedItems = adapter?.getMultiSelectionHelper()?.hasSelectedItems()
                    if (hasSelectedItems == true) {
                        this.itemView = view
                    }
                },
                { pos, checked ->
                    onRecentHeaderClicked(pos, checked, recentData)
                },
                peekAndPop
        )
        fileList.adapter = recentAdapter
    }

    private fun onRecentHeaderClicked(pos: Int, checked: Boolean, recentData: ArrayList<RecentTimeData.RecentDataItem>?) {
        if (recentData == null) {
            return
        }
        for (index in 0 until recentData.size) {
            if (index <= pos) {
                continue
            }
            val dataItem = recentData[index]
            if (dataItem is RecentTimeData.RecentDataItem.Item) {
                if (checked && multiSelectionHelper?.isSelected(index) == false) {
                    fileListHelper.handleItemClick(dataItem.fileInfo, index)
                }
                else if (!checked) {
                    fileListHelper.handleItemClick(dataItem.fileInfo, index)
                }
            }
            else {
                break
            }
        }
    }

    private fun setLayoutManager(fileList: RecyclerView, viewMode: ViewMode, category: Category) {
        Log.d(TAG, "setLayoutManager viewMode: $viewMode")
        fileList.layoutManager = when (viewMode) {
            ViewMode.LIST -> LinearLayoutManager(view.context)
            ViewMode.GRID, ViewMode.GALLERY -> {
                val gridColumns = getGridColumns(view.resources.configuration, viewMode)
                val gridLayoutManager = CustomGridLayoutManager(view.context,
                        gridColumns)
                setSpanSize(gridLayoutManager, gridColumns, category)
                gridLayoutManager
            }
        }
    }

    private fun setupPeekPop() {
        peekAndPop = PeekPopUiView(fileListHelper.getActivityInstance(), fileList)
        peekAndPop?.setPeekPopCallback(object : PeekPopView.PeekPopCallback {
            override fun onItemClick(view: View, fileInfo: FileInfo, pos: Int) {
                when (view.id) {
                    R.id.imagePeekView, R.id.autoPlayView, R.id.imageIcon -> {
                        fileListHelper.handleItemClick(fileInfo, pos)
                    }
                    R.id.imageButtonShare -> {
                        peekAndPop?.endPeekMode()
                        ShareHelper.shareMedia(view.context, fileInfo.category, null, fileInfo.filePath)
                    }
                    R.id.imageButtonInfo -> {
                        peekAndPop?.endPeekMode()
                        fileListHelper.openPeekPopInfo(fileInfo, UriHelper.createContentUri(view.context, fileInfo.filePath))
                    }
                    R.id.buttonNext -> peekAndPop?.loadPeekView(PeekPopView.PeekButton.NEXT, pos + 1)
                    R.id.buttonPrev -> peekAndPop?.loadPeekView(PeekPopView.PeekButton.PREVIOUS, pos - 1)
                }
            }

            override fun canShowPeek(): Boolean {
                return !fileListHelper.isActionModeActive() && viewMode != ViewMode.GALLERY
            }
        })
    }

    private fun setSpanSize(gridLayoutManager: CustomGridLayoutManager, spanSize: Int, category: Category) {
        if (isRecentTimeLineCategory(category)) {
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (recentAdapter?.getItemViewType(position)) {
                        RecentAdapter.ITEM_VIEW_TYPE_HEADER -> spanSize
                        else -> 1
                    }
                }
            }
        }
    }

    private fun getGridColumns(configuration: Configuration, viewMode: ViewMode): Int {
        return if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT || !fileListHelper.isDualModeEnabled()) {
            ConfigurationHelper.getStorageGridCols(configuration, viewMode)
        } else {
            ConfigurationHelper.getStorageDualGridCols(configuration, viewMode)
        }
    }

    fun refreshGridColumns(viewMode: ViewMode) {
        Log.d(TAG, "refreshGridColumns:$viewMode")
        if (this.viewMode == viewMode) {
            setLayoutManager(fileList, this.viewMode, category)
        }
        else {
            onViewModeChanged(viewMode)
        }
    }


    fun onDataLoaded(data: ArrayList<FileInfo>, category: Category, isZipMode : Boolean = false) {
        Log.d(TAG, "onDataLoaded:${data.size}")
        this.category = category
        if (data.isEmpty()) {
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.GONE
        }
        if (adapter == null) {
            setupList(isZipMode)
            peekAndPop?.setFileList(data)
            multiSelectionHelper?.let {
                if (isZipMode) {
                    adapter?.setMultiSelectionHelper(it)
                }
                else {
                    getAdapter()?.setMultiSelectionHelper(it)
                }
            }
            recentAdapter = null
        }
        else {
            peekAndPop?.setFileList(data)
        }
        adapter?.onDataLoaded(data)
    }

    fun onRecentDataLoaded(category: Category, data: ArrayList<RecentTimeData.RecentDataItem>) {
        Log.d(TAG, "onRecentDataLoaded:${data.size}, recentAdapter:$recentAdapter")
        this.recentData = data
        this.category = category
        peekAndPop?.setFileList(RecentDataConverter.getRecentItemList(data))
        if (data.isEmpty()) {
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.GONE
        }
        if (recentAdapter == null) {
            setLayoutManager(fileList, viewMode, category)
            setRecentAdapter()
            multiSelectionHelper?.let { getAdapter()?.setMultiSelectionHelper(it) }
            adapter = null
        }
        recentAdapter?.submitList(data)
    }

    fun onViewModeChanged(viewMode: ViewMode) {
        if (this.viewMode == viewMode) {
            return
        }
        Log.d(TAG, "onViewModeChanged:$viewMode, category:$category")
        setLayoutManager(fileList, viewMode, category)
        this.viewMode = viewMode
        if (isRecentTimeLineCategory(category)) {
            recentAdapter?.viewMode = viewMode
            fileList.adapter = recentAdapter
        } else {
            adapter?.viewMode = viewMode
            fileList.adapter = adapter
        }
    }

    fun onSortModeChanged(sortMode: Int) {
        Log.d(TAG, "onSortModeChanged:$sortMode, category:$category")
        if (this.sortMode == sortMode) {
            return
        }
        this.sortMode = sortMode
        fileListHelper.refreshList()
    }

    fun refresh() {
        Log.d(TAG, "refresh:$category, this:$this")
        getAdapter()?.refresh()
    }

    fun setMultiSelectionHelper(multiSelectionHelper: MultiSelectionHelper) {
        this.multiSelectionHelper = multiSelectionHelper
        Log.d(TAG, "setMultiSelectionHelper:$category, this:$this")
        getAdapter()?.setMultiSelectionHelper(multiSelectionHelper)
    }

    private fun getAdapter(): BaseListAdapter? {
        return if (isRecentTimeLineCategory(category)) {
            recentAdapter
        } else {
            adapter
        }
    }

    fun isPeekMode() : Boolean {
        peekAndPop ?: return false
        return peekAndPop?.isPeekMode() == true
    }

    fun endPeekMode() {
        peekAndPop?.endPeekMode()
    }

    fun getScrollInfo(): ScrollInfo {
        val view = fileList.getChildAt(0)
        val offset = view?.top ?: 0
        val position = when (viewMode) {
            ViewMode.LIST -> {
                val layoutManager = fileList.layoutManager as LinearLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
            ViewMode.GRID, ViewMode.GALLERY -> {
                val layoutManager = fileList.layoutManager as GridLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
        }
        return ScrollInfo(position, offset)
    }

    //TODO Find way to get right delay time (probably after list drawn)
    fun scrollToPosition(scrollInfo: ScrollInfo) {
        fileList.postDelayed({
            Log.d(TAG,
                    "scrollToPosition:${scrollInfo.position}, offset:${scrollInfo.offset}")
            when (viewMode) {
                ViewMode.LIST -> {
                    val layoutManager = fileList.layoutManager as LinearLayoutManager
                    scrollListView(scrollInfo, layoutManager)
                }
                ViewMode.GRID, ViewMode.GALLERY -> {
                    val layoutManager = fileList.layoutManager as GridLayoutManager
                    scrollGridView(scrollInfo, layoutManager)
                }
            }
        }, DELAY_SCROLL_UPDATE_MS)
    }

    private fun scrollListView(scrollInfo: ScrollInfo,
                               layoutManager: LinearLayoutManager) {
        if (shouldScrollToTop(scrollInfo)) {
            scrollListToTop(layoutManager)
        } else {
            layoutManager.scrollToPositionWithOffset(scrollInfo.position,
                    scrollInfo.offset)
        }
    }

    private fun scrollGridView(scrollInfo: ScrollInfo,
                               layoutManager: GridLayoutManager) {
        if (shouldScrollToTop(scrollInfo)) {
            scrollGridToTop(layoutManager)
        } else {
            layoutManager.scrollToPositionWithOffset(scrollInfo.position,
                    scrollInfo.offset)
        }
    }

    private fun shouldScrollToTop(
            scrollInfo: ScrollInfo) = scrollInfo.position == 0 && scrollInfo.offset == 0

    private fun scrollListToTop(layoutManager: LinearLayoutManager) {
        layoutManager.smoothScrollToPosition(fileList, null, 0)
    }

    private fun scrollGridToTop(layoutManager: GridLayoutManager) {
        layoutManager.smoothScrollToPosition(fileList, null, 0)
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        val touchEvent = event.actionMasked

        if (fileListHelper.isDragNotStarted() || isDragUnsupported(category)) {
            return false
        }
        Log.d(TAG, "onTouch:$touchEvent")

        when (touchEvent) {
            MotionEvent.ACTION_MOVE -> {
                fileListHelper.onMoveEvent()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                fileListHelper.onUpEvent()
                view?.performClick()
            }

        }

        return false
    }

    @Suppress("DEPRECATION")
    fun startDrag(category: Category, selectedCount: Int, draggedData: ArrayList<FileInfo>) {
        itemView?.let { view ->
            val intent = Intent()
            intent.putParcelableArrayListExtra(FileConstants.KEY_PATH, draggedData)
            intent.putExtra(KEY_CATEGORY, category.value)
            val data = ClipData.newIntent("", intent)
            val shadowBuilder = dragHelper.getDragShadowBuilder(view, selectedCount)

            if (SdkHelper.isAtleastNougat) {
                this.view.startDragAndDrop(data, shadowBuilder, draggedData, 0)
            } else {
                this.view.startDrag(data, shadowBuilder, draggedData, 0)
            }
        }
    }

    fun onDragLocationEvent(event: DragEvent, oldPos: Int): Int {
        val top = fileList.findChildViewUnder(event.x, event.y) ?: return oldPos

        val newPos = fileList.getChildAdapterPosition(top)
        if (oldPos != newPos && newPos != RecyclerView.NO_POSITION) {
            if (oldPos != RecyclerView.NO_POSITION && newPos < oldPos) {
                scrollUpTOPos(newPos)
            } else {
                scrollDownToPos(newPos)
            }
            adapter?.setDraggedPosition(newPos)
        }
        return newPos
    }

    fun onDragEnd(view: View, event: DragEvent) {
        val top = fileList.findChildViewUnder(event.x, event.y) ?: return
        val pos = fileList.getChildAdapterPosition(top)
        if (!event.result && pos == RecyclerView.NO_POSITION) {
            val clipData = event.clipData
            if (clipData == null) {
                fileListHelper.endActionMode()
                return
            }
            val item = clipData.getItemAt(0)
            val intent = item.intent
            if (intent == null) {
                fileListHelper.endActionMode()
                return
            }
            val dragCategory = intent.getIntExtra(KEY_CATEGORY, 0)
            Log.d(TAG, "dragCategory:$dragCategory")
            if (isDragEndUnsupported(dragCategory)) {
                showError(view)
                return
            }
        }

        view.post {
            fileListHelper.endActionMode()
        }
    }

    private fun showError(view: View) {
        view.context.showToast(view.context.getString(R.string.error_unsupported))
    }

    private fun isDragEndUnsupported(dragCategory: Int) =
            dragCategory == Category.FILES.value && fileListHelper.getCategory() != Category.FILES

    private fun isDragUnsupported(category: Category) = category != Category.FILES

    @Suppress("UNCHECKED_CAST")
    fun onDragDropEvent(event: DragEvent) {
        if (fileListHelper.getCategory() != Category.FILES) {
            showError(view)
            return
        }
        val top = fileList.findChildViewUnder(event.x, event.y) ?: return
        val pos = fileList.getChildAdapterPosition(top)
        val data = event.localState
        fileListHelper.onDragDropEvent(pos, data as ArrayList<FileInfo>)
    }

    private fun scrollUpTOPos(newPos: Int) {
        val changedPos = newPos - 2
        if (changedPos >= 0) {
            fileList.smoothScrollToPosition(changedPos)
        }
    }

    private fun scrollDownToPos(newPos: Int) {
        adapter?.let {
            val changedPos = newPos + 2
            // For scroll down
            if (changedPos < it.itemCount) {
                fileList.smoothScrollToPosition(changedPos)
            }
        }
    }

    fun showDragDialog(destinationDir: String?,
                       draggedData: ArrayList<FileInfo>,
                       dragDialogListener: DialogHelper.DragDialogListener) {
        dragHelper.showDragDialog(draggedData, destinationDir, dragDialogListener)
    }

    fun onEndActionMode() {
        adapter?.clearDragPosition()
    }

    fun onQueryChanged(query: String?) {
        if (query != null ) {
            peekAndPop?.let { adapter?.filter(query) }
        }
    }
}