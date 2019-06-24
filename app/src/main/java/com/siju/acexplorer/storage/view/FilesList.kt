package com.siju.acexplorer.storage.view

import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager
import com.siju.acexplorer.utils.ConfigurationHelper

private const val TAG = "FilesList"
class FilesList(val fragment: BaseFileListFragment, val view: View, val viewMode: ViewMode) {

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
        adapter = FileListAdapter(viewMode) {
            fragment.handleItemClick(it)
        }
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
        adapter.viewMode = viewMode
        fileList.adapter = adapter
    }
}