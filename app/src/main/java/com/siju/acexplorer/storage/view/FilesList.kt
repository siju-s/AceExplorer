package com.siju.acexplorer.storage.view

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
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager
import com.siju.acexplorer.utils.ConfigurationHelper
import com.siju.acexplorer.utils.ScrollInfo

private const val TAG = "FilesList"
private const val DELAY_SCROLL_UPDATE_MS = 100L

class FilesList(val fragment: BaseFileListFragment, val view: View, var viewMode: ViewMode) :
        View.OnTouchListener {

    private var itemView: View? = null
    private lateinit var fileList: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: FileListAdapter
    private val dragHelper = DragHelper(view.context, this)

    init {
        initializeViews()
        setupList()
    }

    private fun initializeViews() {
        fileList = view.findViewById(R.id.recyclerViewFileList)
        emptyText = view.findViewById(R.id.textEmpty)
    }

    private fun setupList() {
        setLayoutManager(fileList, viewMode)
        adapter = FileListAdapter(
                viewMode,
                {
                    fragment.handleItemClick(it.first, it.second)
                },
                { fileInfo, pos, view ->
                    fragment.handleLongItemClick(fileInfo, pos)
                    val hasSelectedItems = adapter.getMultiSelectionHelper()?.hasSelectedItems()
                    if (hasSelectedItems == true) {
                        this.itemView = view
                    }
                }
        )
        fileList.adapter = adapter
        fileList.setOnDragListener(dragHelper.dragListener)
        fileList.setOnTouchListener(this)

    }

    private fun setLayoutManager(fileList: RecyclerView, viewMode: ViewMode) {
        fileList.layoutManager = when (viewMode) {
            ViewMode.LIST -> LinearLayoutManager(view.context)
            ViewMode.GRID -> CustomGridLayoutManager(view.context,
                                                     getGridColumns(view.resources.configuration))
        }
    }

    private fun getGridColumns(configuration: Configuration): Int {
        return if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ConfigurationHelper.getStorageGridCols(configuration)
        }
        else {
            ConfigurationHelper.getStorageDualGridCols(configuration)
        }
    }

    fun onDataLoaded(data: ArrayList<FileInfo>) {
        Log.e(TAG, "onDataLoaded:${data.size}")
        if (data.isEmpty()) {
            emptyText.visibility = View.VISIBLE
        }
        else {
            emptyText.visibility = View.GONE
        }
        adapter.submitList(data)
    }

    fun onViewModeChanged(viewMode: ViewMode) {
        Log.e(TAG, "onViewModeChanged:$viewMode")
        setLayoutManager(fileList, viewMode)
        this.viewMode = viewMode
        adapter.viewMode = viewMode
        fileList.adapter = adapter
    }

    fun refresh() {
        adapter.notifyDataSetChanged()
    }

    fun setMultiSelectionHelper(multiSelectionHelper: MultiSelectionHelper) {
        adapter.setMultiSelectionHelper(multiSelectionHelper)
    }

    fun getScrollInfo(): ScrollInfo {
        val view = fileList.getChildAt(0)
        val offset = view?.top ?: 0
        val position = when (viewMode) {
            ViewMode.LIST -> {
                val layoutManager = fileList.layoutManager as LinearLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
            ViewMode.GRID -> {
                val layoutManager = fileList.layoutManager as GridLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
        }
        return ScrollInfo(position, offset)
    }

    //TODO Find way to get right delay time (probably after list drawn)
    fun scrollToPosition(scrollInfo: ScrollInfo) {
        fileList.postDelayed({
                                 Log.e(TAG,
                                       "scrollToPosition:${scrollInfo.position}, offset:${scrollInfo.offset}")
                                 when (viewMode) {
                                     ViewMode.LIST -> {
                                         val layoutManager = fileList.layoutManager as LinearLayoutManager
                                         scrollListView(scrollInfo, layoutManager)
                                     }
                                     ViewMode.GRID -> {
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
        }
        else {
            layoutManager.scrollToPositionWithOffset(scrollInfo.position,
                                                     scrollInfo.offset)
        }
    }

    private fun scrollGridView(scrollInfo: ScrollInfo,
                               layoutManager: GridLayoutManager) {
        if (shouldScrollToTop(scrollInfo)) {
            scrollGridToTop(layoutManager)
        }
        else {
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

        if (fragment.isDragNotStarted()) {
            return false
        }

        when(touchEvent) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                fragment.onUpEvent()
            }

            MotionEvent.ACTION_MOVE -> {
                fragment.onMoveEvent()
            }
        }

        return false
    }

    fun startDrag(category: Category, selectedCount: Int, draggedData: ArrayList<FileInfo>) {
        itemView?.let {view ->
            val intent = Intent()
            intent.putParcelableArrayListExtra(FileConstants.KEY_PATH, draggedData)
            intent.putExtra(KEY_CATEGORY, category.value)
            val data = ClipData.newIntent("", intent)
            val shadowBuilder = dragHelper.getDragShadowBuilder(view, selectedCount)

            if (SdkHelper.isAtleastNougat) {
                this.view.startDragAndDrop(data, shadowBuilder, draggedData, 0)
            }
            else {
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
            }
            else {
                scrollDownToPos(newPos)
            }
            adapter.setDraggedPosition(newPos)
        }
        return newPos
    }

    fun onDragEnd(view: View, event: DragEvent) {
        val top = fileList.findChildViewUnder(event.x, event.y) ?: return
        val pos = fileList.getChildAdapterPosition(top)
        if (!event.result && pos == RecyclerView.NO_POSITION) {
            val clipData = event.clipData
            if (clipData == null) {
                fragment.endActionMode()
                return
            }
            val item = clipData.getItemAt(0)
            val intent = item.intent
            if (intent == null) {
                fragment.endActionMode()
                return
            }
            val dragCategory = intent.getIntExtra(KEY_CATEGORY, 0)

            if (dragCategory == Category.FILES.value && fragment.getCategory() != Category.FILES) {
                view.context.showToast("Not supported")
                return
            }
        }

        view.post {
            fragment.endActionMode()
        }
    }

    fun onDragDropEvent(event: DragEvent) {
        if (fragment.getCategory() != Category.FILES) {
            view.context.showToast("Not supported")
            return
        }
        val top = fileList.findChildViewUnder(event.x, event.y) ?: return
        val pos = fileList.getChildAdapterPosition(top)
        val data = event.localState
        fragment.onDragDropEvent(pos, data as ArrayList<FileInfo>)
    }

    private fun scrollUpTOPos(newPos: Int) {
        val changedPos = newPos - 2
        if (changedPos >= 0) {
            fileList.smoothScrollToPosition(changedPos)
        }
    }

    private fun scrollDownToPos(newPos: Int) {
        val changedPos = newPos + 2
        // For scroll down
        if (changedPos < adapter.itemCount) {
            fileList.smoothScrollToPosition(changedPos)
        }
    }

    fun showDragDialog(destinationDir: String?,
                       draggedData: ArrayList<FileInfo>,
                       dragDialogListener: DialogHelper.DragDialogListener) {
        dragHelper.showDragDialog(draggedData, destinationDir, dragDialogListener)
    }

    fun onEndActionMode() {
        adapter.clearDragPosition()
    }
}