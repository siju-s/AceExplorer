package com.siju.acexplorer.storage.view

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.CategoryHelper.checkIfAnyMusicCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.shouldShowSort
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.root.RootUtils
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.theme.Theme


private const val TAG = "MenuControls"

class MenuControls(val fragment: BaseFileListFragment, val view: View, categoryFragmentView: View,
                   val category: Category, var viewMode: ViewMode) :
        Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener {

    private val bottomToolbar: Toolbar = view.findViewById(R.id.toolbar_bottom)
    private val toolbar: Toolbar = categoryFragmentView.findViewById(R.id.toolbar)
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
    private lateinit var deleteItem: MenuItem

    private var searchView: SearchView? = null
    private var hiddenMenuItem: MenuItem? = null
    private var isSearchActive = false
    private var theme: Theme? = null

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
            CategoryHelper.checkIfFileCategory(category) || category == Category.SCREENSHOT || category == Category.APP_MANAGER ||
                    category == Category.APPS || category == Category.PDF

    fun onStartActionMode() {
        Log.d(TAG, "onStartActionMode")
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
                searchView = searchItem.actionView as SearchView
            }
            sortItem = menu.findItem(R.id.action_sort)
            hiddenMenuItem = menu.findItem(R.id.action_hidden)
            setupMenuItemVisibility()
            setupSearchView()
            setHiddenCheckedState(fragment.shouldShowHiddenFiles())
            toggleViewModeMenuItemState(viewMode, menu)
        }
    }

    private fun toggleViewModeMenuItemState(viewMode: ViewMode, menu: Menu) {
        Log.d(TAG, "toggleViewModeMenuItemState:$viewMode")
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
        Log.d(TAG, "setupMenuItemVisibility:$category")
        searchItem.isVisible = true
        sortItem.isVisible = shouldShowSort(category)
        if (Category.APP_MANAGER == category || CategoryHelper.checkIfLibraryCategory(category)) {
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
            category == Category.APP_MANAGER -> {
                toggleAppManagerMenuVisibility()
            }

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
        fragment.onMenuItemClick(item)
        return false
    }

    fun onSelectedCountChanged(count: Int,
                               fileInfo: FileInfo?,
                               externalSdList: ArrayList<String>) {
        Log.d(TAG, "onSelectedCountChanged:$count")
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

    fun setTheme(theme: Theme) {
        this.theme = theme
        Log.e(TAG, "setTheme:$theme")
        val darkColoredTheme = Theme.isDarkColoredTheme(fragment.resources, theme)
        if (darkColoredTheme) {
            toolbar.popupTheme = R.style.Dark_AppTheme_PopupOverlay
            bottomToolbar.popupTheme = R.style.Dark_AppTheme_PopupOverlay
            bottomToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.tab_bg_color))
        } else {
            toolbar.popupTheme = R.style.AppTheme_PopupOverlay
            bottomToolbar.popupTheme = R.style.AppTheme_PopupOverlay
            bottomToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }
    }

}