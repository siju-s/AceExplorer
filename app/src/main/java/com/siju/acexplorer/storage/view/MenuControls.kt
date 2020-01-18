package com.siju.acexplorer.storage.view

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.CategoryHelper.checkIfAnyMusicCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isSortOrActionModeUnSupported
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.root.RootUtils
import com.siju.acexplorer.storage.model.ViewMode

private const val TAG = "MenuControls"

class MenuControls(val fragment: BaseFileListFragment, val view: View, categoryFragmentView: View,
                   val category: Category, var viewMode: ViewMode) :
        Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener {

    private var bottomToolbar: Toolbar = view.findViewById(R.id.toolbar_bottom)
    private var toolbar: Toolbar = categoryFragmentView.findViewById(R.id.toolbar)
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
    private lateinit var deleteFavItem: MenuItem

    private var searchView: SearchView? = null
    private var hiddenMenuItem: MenuItem? = null
    private var isSearchActive = false

    init {
        // When Categoryfragment with viewpager is not shown, the BaseFileListFragment toolbar inflates the menu
        // else the CategoryFragment is responsible for inflating menu items so that duplicate menu items are not created by BaseFileListFragment
        if (shouldInflateBaseMenu()) {
            setupBaseMenu()
        }
        else {
            setupMenuItems(toolbar.menu)
        }
    }

    private fun shouldInflateBaseMenu() =
            CategoryHelper.checkIfFileCategory(category) || category == Category.SCREENSHOT || category == Category.APP_MANAGER ||
                    category == Category.PDF || category == Category.APPS

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
        setToolbarTitle(context.getString(R.string.app_name))
        if (isSearchActive) {
            setSearchActive(false)
        }
    }

    fun setSearchActive(value: Boolean) {
        this.isSearchActive = value
        Log.e(TAG, "setSearchActive:$isSearchActive")
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
        toolbar.menu.clear()
        if (CategoryHelper.isAppManager(category)) {
            toolbar.inflateMenu(R.menu.search)
        }
        else {
            toolbar.inflateMenu(R.menu.filelist_base)
        }
        toolbar.setOnMenuItemClickListener(this)
        setupMenuItems(toolbar.menu)
    }

    private fun showBottomToolbar() {
        Log.e(TAG, "showBottomToolbar")
        bottomToolbar.visibility = View.VISIBLE
    }

    private fun hideBottomToolbar() {
        Log.e(TAG, "hideBottomToolbar")
        bottomToolbar.visibility = View.GONE
    }

    private fun setupMenuItems(menu: Menu?) {
        menu?.let {
            searchItem = menu.findItem(R.id.action_search)
            if (CategoryHelper.isAppManager(category)) {
                searchView = searchItem.actionView as SearchView
            }
            sortItem = menu.findItem(R.id.action_sort)
            setupMenuItemVisibility()
            setupSearchView()
            hiddenMenuItem = menu.findItem(R.id.action_hidden)
            setHiddenCheckedState(fragment.shouldShowHiddenFiles())
            toggleViewModeMenuItemState(viewMode, menu)
        }
    }

    private fun toggleViewModeMenuItemState(viewMode: ViewMode, menu: Menu) {
        Log.e(TAG, "toggleViewModeMenuItemState:$viewMode")
        when (viewMode) {
            ViewMode.LIST -> menu.findItem(R.id.action_view_list).isChecked = true
            ViewMode.GRID -> menu.findItem(R.id.action_view_grid).isChecked = true
            ViewMode.GALLERY -> menu.findItem(R.id.action_view_gallery).isChecked = true
        }
    }

    private fun setHiddenCheckedState(state: Boolean) {
        hiddenMenuItem?.isChecked = state
    }

    private fun setupMenuItemVisibility() {
        Log.e(TAG, "setupSortVisibility:$category")
        if (isSortOrActionModeUnSupported(category)) {
            searchItem.isVisible = true
            sortItem.isVisible = false
        } else if (Category.LARGE_FILES == category) {
            searchItem.isVisible = true
            sortItem.isVisible = false
        } else if (Category.APP_MANAGER == category) {
            searchItem.isVisible = true
            sortItem.isVisible = true
            hiddenMenuItem?.isVisible = false
        } else {
            searchItem.isVisible = true
            sortItem.isVisible = true
        }
    }


    private fun setupSearchView() {
        if (!CategoryHelper.isAppManager(category)) {
            return
        }
        searchView?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchView?.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView?.setOnQueryTextListener(this)
        searchView?.maxWidth = Int.MAX_VALUE
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                Analytics.getLogger().searchClicked(false)
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
        Log.e(TAG, "endSearch:$isSearchActive")
        searchView?.setQuery("", false)
        hideSearchView()
        searchView?.isIconified = true
    }

    fun isSearchActive(): Boolean {
        Log.e(TAG, "isSearchActive:$isSearchActive")
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
            category == Category.APP_MANAGER -> {
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
        Log.e(TAG, "onSelectedCountChanged:$count")
        setToolbarTitle(count.toString())
        toggleActionModeMenuVisibility(count, fileInfo, externalSdList)
    }

    fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    fun onPasteEnabled() {
        bottomToolbar.visibility = View.VISIBLE
        bottomToolbar.menu.clear()
        bottomToolbar.inflateMenu(R.menu.action_mode_paste)
    }

    fun onViewModeChanged(viewMode: ViewMode) {
        this.viewMode = viewMode
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.e(TAG, "onQueryTextChange:$query")
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        Log.e(TAG, "onQueryTextChange:$query")
        if (fragment.isActionModeActive()) {
            return true
        }
        fragment.onQueryTextChange(query)
        return true
    }

}