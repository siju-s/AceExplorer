package com.siju.acexplorer.storage.view

import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager
import com.siju.acexplorer.utils.ConfigurationHelper
import com.siju.acexplorer.utils.ScrollInfo

private const val TAG = "FilesList"
private const val DELAY_SCROLL_UPDATE_MS = 100L
class FilesList(val fragment: BaseFileListFragment, val view: View, var viewMode: ViewMode) {

    private lateinit var fileList: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: FileListAdapter

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
                {
                    fragment.handleLongItemClick(it.first, it.second)
                }
        )
        fileList.adapter = adapter
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
        val position = when(viewMode) {
            ViewMode.LIST ->  {
                val layoutManager = fileList.layoutManager as LinearLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
            ViewMode.GRID ->  {
                val layoutManager = fileList.layoutManager as GridLayoutManager
                layoutManager.findFirstVisibleItemPosition()
            }
        }
        return ScrollInfo(position, offset)
    }

    fun scrollToPosition(scrollInfo: ScrollInfo) {
        fileList.postDelayed({
            Log.e(TAG, "scrollToPosition:${scrollInfo.position}, offset:${scrollInfo.offset}")
            when (viewMode) {
                ViewMode.LIST -> {
                    val layoutManager = fileList.layoutManager as LinearLayoutManager
                    layoutManager.scrollToPositionWithOffset(scrollInfo.position, scrollInfo.offset)
                }
                ViewMode.GRID -> {
                    val layoutManager = fileList.layoutManager as GridLayoutManager
                    layoutManager.scrollToPositionWithOffset(scrollInfo.position, scrollInfo.offset)
                }
            }
        }, DELAY_SCROLL_UPDATE_MS)
    }
}