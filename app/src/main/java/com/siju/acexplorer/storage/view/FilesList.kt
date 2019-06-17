package com.siju.acexplorer.storage.view

import android.content.res.Configuration
import android.view.View
import android.widget.TextView
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.view.custom.CustomGridLayoutManager
import com.siju.acexplorer.storage.view.custom.CustomLayoutManager
import com.siju.acexplorer.storage.view.custom.recyclerview.FastScrollRecyclerView
import com.siju.acexplorer.utils.ConfigurationHelper

class FilesList(val fragment: BaseFileListFragment, val view: View, val viewMode: Int) {

    private lateinit var fileList: FastScrollRecyclerView
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
        fileList.setHasFixedSize(true)
        setLayoutManager(fileList, viewMode)
        adapter = FileListAdapter {

        }
    }

    private fun setLayoutManager(fileList: FastScrollRecyclerView, viewMode: Int) {
        when (viewMode) {
            ViewMode.LIST -> fileList.layoutManager = CustomLayoutManager(view.context)
            ViewMode.GRID -> fileList.layoutManager = CustomGridLayoutManager(view.context,
                                                                              getGridColumns(
                                                                                      view.resources.configuration))
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
        adapter.submitList(data)
    }


}