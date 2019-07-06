package com.siju.acexplorer.storage.view

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper.*

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
        toolbar.inflateMenu(R.menu.action_mode)
    }

    private fun clearActionModeToolbar() {
        toolbar.menu.clear()
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

    private fun toggleMenuVisibility(count: Int) {
        when {
            count == 1 -> onSingleItemSelected()
            count > 1  -> {
                renameItem.isVisible = false
                infoItem.isVisible = false
                extractItem.isVisible = false
                permissionItem.isVisible = false
                hideItem.isVisible = false
            }
        }
    }

    private fun onSingleItemSelected() {
        renameItem.isVisible = true
        infoItem.isVisible = true
        hideItem.isVisible = true
        when {
            category == Category.APP_MANAGER -> {
                renameItem.isVisible = false
                permissionItem.isVisible = false
                extractItem.isVisible = false
                archiveItem.isVisible = false
                shareItem.isVisible = false
                hideItem.isVisible = false
            }

            checkIfAnyMusicCategory(category) -> {
                permissionItem.isVisible = false
                extractItem.isVisible = false
                archiveItem.isVisible = false
            }
        }
        //TODO Check directory, theme etc
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        fragment.onMenuItemClick(item)
        return false
    }

    fun onSelectedCountChanged(count: Int) {
        setToolbarText(count.toString())
        toggleMenuVisibility(count)
    }

}