package com.siju.acexplorer.storage.view

import android.annotation.SuppressLint
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.CategoryHelper.checkIfAnyMusicCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.shouldShowSort
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.root.RootUtils
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.common.R as RC

private const val TAG = "MenuControls"

class MenuControls(val fragment: BaseFileListFragment, val view: View, categoryFragmentView: View,
                   val category: Category, var viewMode: ViewMode, private val mainViewModel: MainViewModel) :
        Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener {

    private val bottomToolbar: Toolbar = view.findViewById(R.id.toolbar_bottom)
    private val toolbar: Toolbar = categoryFragmentView as Toolbar
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
    private lateinit var deleteFavItem: MenuItem
    private lateinit var deleteItem: MenuItem
    private lateinit var installSourceItem : MenuItem
    private lateinit var allSourceItem: MenuItem
    private lateinit var userSourceItem: MenuItem

    private var menuItemBadge : BadgeDrawable?= null
    private var searchView: SearchView? = null
    private var hiddenMenuItem: MenuItem? = null
    private var isSearchActive = false

    init {
        // When Categoryfragment with viewpager is not shown, the BaseFileListFragment toolbar inflates the menu
        // else the CategoryFragment is responsible for inflating menu items so that duplicate menu items are not created by BaseFileListFragment
        if (shouldInflateBaseMenu()) {
            setupBaseMenu()
        } else {
            setupMenuItems(toolbar.menu)
        }
    }

    private fun shouldInflateBaseMenu() =
            CategoryHelper.checkIfFileCategory(category) || category == Category.SCREENSHOT  ||
                    category == Category.APPS || category == Category.PDF

    fun onStartActionMode() {
        Log.d(TAG, "onStartActionMode")
        setupActionModeToolbar()
        setupActionModeMenu()
        if (!mainViewModel.isFilePicker()) {
            showBottomToolbar()
        }
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
        deleteItem = menu.findItem(R.id.action_delete)
    }

    fun onEndActionMode() {
        Log.d(TAG, "onEndActionMode")
        hideBottomToolbar()
        clearActionModeToolbar()
        setupBaseMenu()
        if (isSearchActive) {
            setSearchActive(false)
        }
    }

    fun setSearchActive(value: Boolean) {
        this.isSearchActive = value
        Log.d(TAG, "setSearchActive:$isSearchActive")
    }

    private fun setupActionModeToolbar() {
        toolbar.menu.clear()
        toolbar.setNavigationIcon(RC.drawable.ic_back_white)
        toolbar.inflateMenu(RC.menu.action_mode)
        toolbar.setOnMenuItemClickListener(this)
        if (mainViewModel.isPickerMultiSelection()) {
            toolbar.menu.findItem(RC.id.action_done).isVisible = true
        }
        toolbar.setNavigationOnClickListener {
            fragment.onBackPressed()
        }
    }

    private fun clearActionModeToolbar() {
        toolbar.menu.clear()
        toolbar.navigationIcon = null
    }

    private fun setupBaseMenu() {
        toolbar.menu.clear()
        if (CategoryHelper.isAppManager(category)) {
            toolbar.inflateMenu(RC.menu.app_manager)
        } else {
            toolbar.inflateMenu(R.menu.filelist_base)
        }
        toolbar.setOnMenuItemClickListener(this)
        setupMenuItems(toolbar.menu)
    }

    private fun showBottomToolbar() {
        Log.d(TAG, "showBottomToolbar")
        bottomToolbar.visibility = View.VISIBLE
    }

    private fun hideBottomToolbar() {
        Log.d(TAG, "hideBottomToolbar")
        bottomToolbar.visibility = View.GONE
    }

    private fun setupMenuItems(menu: Menu?) {
        menu?.let {
            searchItem = menu.findItem(R.id.action_search)
            if (CategoryHelper.isAppManager(category)) {
                setupAppManagerMenuItems(menu)
            }
            sortItem = menu.findItem(R.id.action_sort)
            hiddenMenuItem = menu.findItem(R.id.action_hidden)
            setupMenuItemVisibility()
            setupSearchView()
            setHiddenCheckedState(fragment.shouldShowHiddenFiles())
            toggleViewModeMenuItemState(viewMode, menu)
        }
    }

    private fun setupAppManagerMenuItems(menu: Menu) {
        searchView = searchItem.actionView as SearchView
        menu.findItem(RC.id.action_apps_user).isChecked = true
        menu.findItem(RC.id.action_source_all).isChecked = true
        installSourceItem = menu.findItem(RC.id.action_installed_source)
        allSourceItem = menu.findItem(RC.id.action_source_all)
        userSourceItem = menu.findItem(RC.id.action_apps_user)
    }

    private fun toggleViewModeMenuItemState(viewMode: ViewMode, menu: Menu) {
        Log.d(TAG, "toggleViewModeMenuItemState:$viewMode")
        when (viewMode) {
            ViewMode.LIST    -> menu.findItem(R.id.action_view_list).isChecked = true
            ViewMode.GRID    -> menu.findItem(R.id.action_view_grid).isChecked = true
            ViewMode.GALLERY -> menu.findItem(R.id.action_view_gallery).isChecked = true
        }
    }

    private fun setHiddenCheckedState(state: Boolean) {
        hiddenMenuItem?.isChecked = state
    }

    private fun setupMenuItemVisibility() {
        Log.d(TAG, "setupMenuItemVisibility:$category")
        searchItem.isVisible = !mainViewModel.isFilePicker()
        sortItem.isVisible = shouldShowSort(category)
        if (CategoryHelper.checkIfLibraryCategory(category)) {
            hiddenMenuItem?.isVisible = false
        }
    }

    private fun setupSearchView() {
        if (!CategoryHelper.isAppManager(category)) {
            return
        }
        searchView?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchView?.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView?.setOnQueryTextListener(this)
        searchView?.queryHint = searchView?.context?.getString(com.siju.acexplorer.common.R.string.action_search)
        searchView?.maxWidth = Int.MAX_VALUE
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                Analytics.logger.searchClicked(false)
                setSearchActive(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                setSearchActive(false)
                return true
            }
        })
        searchView?.setOnSearchClickListener {
            setSearchActive(true)
        }
        searchView?.setOnCloseListener {
            setSearchActive(false)
            false
        }
    }

    private fun hideSearchView() {
        searchItem.collapseActionView()
    }

    fun endSearch() {
        Log.d(TAG, "endSearch:$isSearchActive")
        searchView?.setQuery("", false)
        hideSearchView()
        searchView?.isIconified = true
    }

    fun isSearchActive(): Boolean {
        Log.d(TAG, "isSearchActive:$isSearchActive")
        return isSearchActive
    }

    private fun toggleActionModeMenuVisibility(count: Int,
                                               fileInfo: FileInfo?,
                                               externalSdList: ArrayList<String>) {
        when {
            count == 1 -> onSingleItemSelected(fileInfo, externalSdList)
            count > 1 -> {
                renameItem.isVisible = false
                infoItem.isVisible = false
                extractItem.isVisible = false
                permissionItem.isVisible = false
                hideItem.isVisible = false
                if (category == Category.FAVORITES) {
                    deleteFavItem.isVisible = true
                    deleteItem.isVisible = false
                }
            }
        }
    }

    private fun onSingleItemSelected(fileInfo: FileInfo?,
                                     externalSdList: ArrayList<String>) {
        renameItem.isVisible = true
        infoItem.isVisible = true
        hideItem.isVisible = CategoryHelper.isFileBasedCategory(category)
        setHideItemProperties(fileInfo?.fileName)

        val isDir = fileInfo?.isDirectory
        val filePath = fileInfo?.filePath

        val isRoot = RootUtils.isRootDir(filePath, externalSdList)
        toggleCompressedMenuVisibility(filePath)
        toggleRootMenuVisibility(isRoot)
        if (isDir == false) {
            favItem.isVisible = false
        }

        when {
            category == Category.FAVORITES -> {
                deleteFavItem.isVisible = true
                deleteItem.isVisible = false
                renameItem.isVisible = false
            }

            checkIfAnyMusicCategory(category) -> {
                permissionItem.isVisible = false
                extractItem.isVisible = false
                archiveItem.isVisible = false
            }

        }
    }

    private fun setHideItemProperties(fileName: String?) {
        if (fileName == null) {
            return
        }
        if (fileName.startsWith(".")) {
            hideItem.setIcon(R.drawable.ic_unhide)
            hideItem.setTitle(R.string.unhide)
        } else {
            hideItem.setIcon(R.drawable.ic_hide)
            hideItem.setTitle(R.string.hide)
        }
    }

    private fun toggleRootMenuVisibility(isRoot: Boolean) {
        if (isRoot) {
            permissionItem.isVisible = true
            extractItem.isVisible = false
            archiveItem.isVisible = false
        }
    }

    private fun toggleCompressedMenuVisibility(filePath: String?) {
        if (category == Category.COMPRESSED) {
            extractItem.isVisible = false
            archiveItem.isVisible = false
        }
        else if (FileUtils.isFileCompressed(filePath)) {
            extractItem.isVisible = true
            archiveItem.isVisible = false
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when(item.itemId) {
            RC.id.action_apps_system -> {
                installSourceItem.isEnabled = false
                allSourceItem.isChecked = true
                applyBadgeToMenuItem(RC.id.action_filter)
            }
            RC.id.action_apps_user -> {
                allSourceItem.isChecked = true
                installSourceItem.isEnabled = true
                clearBadgeMenuItem(RC.id.action_filter)
            }
            RC.id.action_apps_all -> {
                allSourceItem.isChecked = true
                installSourceItem.isEnabled = true
                applyBadgeToMenuItem(com.siju.acexplorer.common.R.id.action_filter)
            }
            RC.id.action_playstore, RC.id.action_amazon_store, RC.id.action_unknown -> {
                applyBadgeToMenuItem(RC.id.action_filter)
            }
            RC.id.action_source_all -> {
                if (userSourceItem.isChecked) {
                    clearBadgeMenuItem(RC.id.action_installed_source)
                }
            }
        }
        fragment.onMenuItemClick(item)
        return false
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun applyBadgeToMenuItem(itemId : Int) {
        val context = fragment.context
        if (menuItemBadge == null) {
            context?.let {
                menuItemBadge = BadgeDrawable.create(it)
                BadgeUtils.attachBadgeDrawable(menuItemBadge!!, toolbar, itemId)
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun clearBadgeMenuItem(itemId : Int) {
        val context = fragment.context
        context ?: return
        menuItemBadge?.let {
            BadgeUtils.detachBadgeDrawable(it, toolbar, itemId)
        }
        menuItemBadge = null
    }

    fun onSelectedCountChanged(count: Int,
                               fileInfo: FileInfo?,
                               externalSdList: ArrayList<String>) {
        Log.d(TAG, "onSelectedCountChanged:$count")
        setToolbarTitle(count.toString())
        toggleActionModeMenuVisibility(count, fileInfo, externalSdList)
    }

    private fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    fun onViewModeChanged(viewMode: ViewMode) {
        this.viewMode = viewMode
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d(TAG, "onQueryTextChange:$query")
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        Log.d(TAG, "onQueryTextChange:$query")
        if (fragment.isActionModeActive()) {
            return true
        }
        fragment.onQueryTextChange(query)
        return true
    }
}