package com.siju.acexplorer.storage.view

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper.checkIfAnyMusicCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isRecentCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isRecentGenericCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isSortOrActionModeUnSupported
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.root.RootUtils

private const val TAG = "MenuControls"

class MenuControls(val fragment: BaseFileListFragment, val view: View, val category: Category) :
        Toolbar.OnMenuItemClickListener {

    private var bottomToolbar: Toolbar = view.findViewById(R.id.toolbar_bottom)
    private var toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val context = view.context
    private lateinit var searchItem: MenuItem
    private lateinit var sortItem: MenuItem
    private lateinit var renameItem: MenuItem
    private lateinit var infoItem: MenuItem
    private lateinit var hideItem: MenuItem
    private lateinit var shareItem: MenuItem
    private lateinit var archiveItem: MenuItem
    private lateinit var favItem: MenuItem
    private lateinit var extractItem: MenuItem
    private lateinit var permissionItem: MenuItem
    private lateinit var deleteFavItem : MenuItem

    fun onStartActionMode() {
        Log.e(TAG, "onStartActionMode")
        setupActionModeToolbar()
        setupActionModeMenu()
        showBottomToolbar()
    }

    private fun setupActionModeMenu() {
        bottomToolbar.menu.clear()
        EnhancedMenuInflater.inflate(fragment.activity?.menuInflater, bottomToolbar.menu,
                                     category)
        val menu = bottomToolbar.menu
        setupActionModeMenuItems(menu)
        if (category != Category.FILES) {
            archiveItem.isVisible = false
            extractItem.isVisible = false
            favItem.isVisible = false
            hideItem.isVisible = false
        }
        bottomToolbar.setOnMenuItemClickListener(this)
    }

    private fun setupActionModeMenuItems(menu: Menu) {
        renameItem = menu.findItem(R.id.action_edit)
        infoItem = menu.findItem(R.id.action_info)
        hideItem = menu.findItem(R.id.action_hide)
        archiveItem = menu.findItem(R.id.action_archive)
        favItem = menu.findItem(R.id.action_fav)
        extractItem = menu.findItem(R.id.action_extract)
        shareItem = menu.findItem(R.id.action_share)
        permissionItem = menu.findItem(R.id.action_permissions)
        deleteFavItem = menu.findItem(R.id.action_delete_fav)
    }

    fun onEndActionMode() {
        Log.e(TAG, "onEndActionMode")
        hideBottomToolbar()
        clearActionModeToolbar()
        setupBaseMenu()
        setToolbarText(context.getString(R.string.app_name))
    }

    private fun setupActionModeToolbar() {
        toolbar.menu.clear()
        toolbar.setNavigationIcon(R.drawable.ic_back_white)
        toolbar.inflateMenu(R.menu.action_mode)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener {
            fragment.onBackPressed()
        }
    }

    private fun clearActionModeToolbar() {
        toolbar.menu.clear()
        toolbar.navigationIcon = null
    }

    private fun setupBaseMenu() {
        toolbar.inflateMenu(R.menu.filelist_base)
        setupMenuItems(toolbar.menu)
    }

    private fun setToolbarText(text: String) {
        toolbar.title = text
    }

    private fun showBottomToolbar() {
        bottomToolbar.visibility = View.VISIBLE
    }

    private fun hideBottomToolbar() {
        bottomToolbar.visibility = View.GONE
    }

    private fun setupMenuItems(menu: Menu) {
        searchItem = menu.findItem(R.id.action_search)
        sortItem = menu.findItem(R.id.action_sort)
        setupSortVisibility()
    }

    private fun setupSortVisibility() {
        if (isSortOrActionModeUnSupported(category) || isRecentGenericCategory(
                        category) || isRecentCategory(category)) {
            searchItem.isVisible = false
            sortItem.isVisible = false
        }
        else if (Category.LARGE_FILES == category) {
            searchItem.isVisible = true
            sortItem.isVisible = false
        }
        else {
            searchItem.isVisible = true
            sortItem.isVisible = true
        }
    }

    private fun toggleMenuVisibility(count: Int,
                                     fileInfo: FileInfo?,
                                     externalSdList: ArrayList<String>) {
        when {
            count == 1 -> onSingleItemSelected(fileInfo, externalSdList)
            count > 1  -> {
                renameItem.isVisible = false
                infoItem.isVisible = false
                extractItem.isVisible = false
                permissionItem.isVisible = false
                hideItem.isVisible = false
            }
            category == Category.FAVORITES -> {
                deleteFavItem.isVisible = true
            }
        }
    }

    private fun onSingleItemSelected(fileInfo: FileInfo?,
                                     externalSdList: ArrayList<String>) {
        renameItem.isVisible = true
        infoItem.isVisible = true
        hideItem.isVisible = true

        val isDir = fileInfo?.isDirectory
        val filePath = fileInfo?.filePath

        val isRoot = RootUtils.isRootDir(filePath, externalSdList)
        toggleCompressedMenuVisibility(filePath)
        toggleRootMenuVisibility(isRoot)
        if (isDir == false) {
            favItem.isVisible = false
        }

        when {
            category == Category.APP_MANAGER  -> {
                toggleAppManagerMenuVisibility()
            }

            checkIfAnyMusicCategory(category) -> {
                permissionItem.isVisible = false
                extractItem.isVisible = false
                archiveItem.isVisible = false
            }

        }
        //TODO Check directory, theme etc
    }

    private fun toggleAppManagerMenuVisibility() {
        renameItem.isVisible = false
        permissionItem.isVisible = false
        extractItem.isVisible = false
        archiveItem.isVisible = false
        shareItem.isVisible = false
        hideItem.isVisible = false
        favItem.isVisible = false
        deleteFavItem.isVisible = false
    }

    private fun toggleRootMenuVisibility(isRoot: Boolean) {
        if (isRoot) {
            permissionItem.isVisible = true
            extractItem.isVisible = false
            archiveItem.isVisible = false
        }
    }

    private fun toggleCompressedMenuVisibility(filePath: String?) {
        if (FileUtils.isFileCompressed(filePath)) {
            extractItem.isVisible = true
            archiveItem.isVisible = false
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        fragment.onMenuItemClick(item)
        return false
    }

    fun onSelectedCountChanged(count: Int,
                               fileInfo: FileInfo?,
                               externalSdList: ArrayList<String>) {
        setToolbarText(count.toString())
        toggleMenuVisibility(count, fileInfo, externalSdList)
    }

    fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    fun onPasteEnabled() {
        bottomToolbar.visibility = View.VISIBLE
        bottomToolbar.menu.clear()
        bottomToolbar.inflateMenu(R.menu.action_mode_paste)
    }

}