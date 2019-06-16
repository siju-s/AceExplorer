/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.home.view

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.ImageButton

import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.view.custom.helper.SimpleItemTouchHelperCallback
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.utils.ConfigurationHelper

import java.util.ArrayList

import com.siju.acexplorer.main.model.groups.Category.DOWNLOADS
import com.siju.acexplorer.main.model.groups.Category.GENERIC_IMAGES
import com.siju.acexplorer.main.model.groups.Category.GENERIC_MUSIC
import com.siju.acexplorer.main.model.groups.Category.GENERIC_VIDEOS

/**
 * Created by Siju on 03 September,2017
 */
class HomeLibrary(private val context: Context) {

    private val TAG = this.javaClass.simpleName
    private var libraryList: RecyclerView? = null
    private var layoutLibrary: CardView? = null
    private var homeLibAdapter: HomeLibAdapter? = null
    private var homeLibraryInfoArrayList: MutableList<HomeLibraryInfo> = ArrayList()
    private var currentOrientation: Int = 0
    private var isActionModeActive: Boolean = false
    private var deleteButton: ImageButton? = null

    var libraries: MutableList<HomeLibraryInfo>
        get() = homeLibraryInfoArrayList
        set(libraries) {
            this.homeLibraryInfoArrayList = libraries
            inflateLibraryItem()
        }

    init {
        init()
    }

    private fun setTheme(theme: Theme) {
        when (theme) {
            Theme.DARK -> layoutLibrary!!.setCardBackgroundColor(ContextCompat.getColor(context, R.color
                    .dark_home_card_bg))
            Theme.LIGHT -> layoutLibrary!!.setCardBackgroundColor(ContextCompat.getColor(context, R.color
                    .light_home_card_bg))
        }
    }

    private fun init() {
        libraryList = homeUiView.findViewById(R.id.categoryList)
        layoutLibrary = homeUiView.findViewById(R.id.cardViewLibrary)
        setTheme(theme)
        deleteButton = homeUiView.findViewById(R.id.deleteButton)
        if (theme == Theme.LIGHT) {
            deleteButton!!.setImageResource(R.drawable.ic_delete_black)
        } else {
            deleteButton!!.setImageResource(R.drawable.ic_delete_white)
        }
        currentOrientation = homeUiView.configuration.orientation
        initList()
        setListeners()
        homeUiView.getLibraries()
    }

    private fun initList() {
        libraryList!!.itemAnimator = DefaultItemAnimator()
        libraryList!!.setHasFixedSize(true)
        libraryList!!.isNestedScrollingEnabled = false
        homeLibAdapter = HomeLibAdapter(context)
        homeLibAdapter!!.setHasStableIds(false)
        libraryList!!.itemAnimator!!.changeDuration = 0
        setGridColumns(homeUiView.configuration)

        val callback = SimpleItemTouchHelperCallback(homeLibAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(libraryList)
        libraryList!!.adapter = homeLibAdapter
    }

    private fun setListeners() {
        homeLibAdapter!!.setOnItemClickListener(object : HomeLibAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isActionModeActive) {
                    itemClickActionMode(position, false)
                } else {
                    handleItemClick(position)
                }

            }
        })

        homeLibAdapter!!.setOnItemLongClickListener(object : HomeLibAdapter.OnItemLongClickListener {
            override fun onItemLongClick(view: View, position: Int) {
                itemClickActionMode(position, true)
            }
        })

        deleteButton!!.setOnClickListener {
            val sparseBooleanArray = homeLibAdapter!!.getSelectedItemPositions()
            val fileInfoList = ArrayList<HomeLibraryInfo>()
            for (i in 0 until sparseBooleanArray.size()) {
                fileInfoList.add(homeLibraryInfoArrayList[sparseBooleanArray.keyAt(i)])
            }
            homeLibraryInfoArrayList.removeAll(fileInfoList)
            homeLibAdapter!!.updateAdapter(homeLibraryInfoArrayList)
            endActionMode()
            reloadAftDelete()
        }
    }

    private fun reloadAftDelete() {
        homeUiView.reloadLibs(formLibSortList())
    }


    private fun handleItemClick(position: Int) {
        var category = homeLibraryInfoArrayList[position].category
        var path: String? = null
        when (category) {
            DOWNLOADS -> path = StorageUtils.downloadsDirectory
            Category.AUDIO -> category = GENERIC_MUSIC
            Category.IMAGE -> category = GENERIC_IMAGES
            Category.VIDEO -> category = GENERIC_VIDEOS
        }
        homeUiView.loadFileList(path, category)

    }

    private fun itemClickActionMode(position: Int, isLongPress: Boolean) {
        if (position == homeLibraryInfoArrayList.size - 1) {
            return
        }
        homeLibAdapter!!.toggleSelection(position, isLongPress)

        val hasCheckedItems = homeLibAdapter!!.getSelectedCount() > 0
        if (hasCheckedItems && !isActionModeActive) {
            startActionMode()
        } else if (!hasCheckedItems && isActionModeActive) {
            endActionMode()
        }
    }

    private fun startActionMode() {
        isActionModeActive = true
        deleteButton!!.visibility = View.VISIBLE
    }

    private fun endActionMode() {
        deleteButton!!.visibility = View.GONE
        isActionModeActive = false
        homeLibAdapter!!.clearSelection()
        saveLibs()
    }


    private fun setGridColumns(configuration: Configuration) {
        val gridColumns = ConfigurationHelper.getHomeGridCols(configuration)//context.getResources().getInteger(R.integer.homescreen_columns);
        val gridLayoutManager = GridLayoutManager(context, gridColumns)
        libraryList!!.layoutManager = gridLayoutManager
    }

    private fun inflateLibraryItem() {
        // NPE here in dual mode when orientation change from LAND->PORT
        val libNames = ArrayList<String>()
        for (libraryInfo in homeLibraryInfoArrayList) {
            libNames.add(libraryInfo.categoryName)
        }

        homeLibAdapter!!.updateAdapter(homeLibraryInfoArrayList)
        Analytics.getLogger().homeLibsDisplayed(homeLibraryInfoArrayList.size, libNames)
    }


    fun onOrientationChanged(configuration: Configuration) {
        val orientation = configuration.orientation
        if (currentOrientation != orientation) {
            currentOrientation = orientation
            setGridColumns(configuration)
            inflateLibraryItem()
        }
    }

    companion object {
        val LIBSORT_REQUEST_CODE = 1000
    }

}
