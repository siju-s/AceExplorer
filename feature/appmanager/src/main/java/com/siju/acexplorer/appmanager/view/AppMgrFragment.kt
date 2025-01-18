package com.siju.acexplorer.appmanager.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.siju.acexplorer.appmanager.AppInfoProvider
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.databinding.AppsListContainerBinding
import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.appmanager.filter.AppType
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.appmanager.view.compose.AppMgrActionModeItems
import com.siju.acexplorer.appmanager.view.compose.GridItem
import com.siju.acexplorer.appmanager.view.compose.ListItem
import com.siju.acexplorer.appmanager.viewmodel.AppMgrViewModel
import com.siju.acexplorer.common.ActionModeState
import com.siju.acexplorer.common.R.string.*
import com.siju.acexplorer.common.SortDialog
import com.siju.acexplorer.common.SortMode
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.compose.ui.ThemePreviews
import com.siju.acexplorer.common.compose.ui.TopAppBarWithSearch
import com.siju.acexplorer.common.theme.MyApplicationTheme
import com.siju.acexplorer.common.theme.Theme
import com.siju.acexplorer.common.utils.ConfigurationHelper
import com.siju.acexplorer.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import com.siju.acexplorer.common.R as RC


@AndroidEntryPoint
class AppMgrFragment : Fragment(), Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener {

    private var _binding: AppsListContainerBinding? = null

    private val viewModel: AppMgrViewModel by viewModels()
    private lateinit var installSourceItem: MenuItem
    private lateinit var allSourceItem: MenuItem
    private lateinit var userSourceItem: MenuItem

    private var menuItemBadge: BadgeDrawable? = null
    private var searchView: SearchView? = null
    private lateinit var searchItem: MenuItem
    private lateinit var toolbar: Toolbar
    private lateinit var bottomToolbar: Toolbar
    private var packageReceiverRegistered = false

    private val uninstallResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                context.showToast(getString(msg_operation_failed))
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyApplicationTheme(appTheme = Theme.getTheme(requireContext())) {
                    AppContent(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        fetchData(AppType.USER_APP)
        registerPackageReceiver()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }


    @Composable
    private fun AppContent(viewModel: AppMgrViewModel) {
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var isSearchVisible by remember { mutableStateOf(false) }
        val actionModeState by viewModel.actionModeState.observeAsState()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            topBar = {
                TopAppBarWithSearch(title = getString(R.string.app_manager),
                    actionModeEnabled = actionModeState == ActionModeState.STARTED,
                    searchQuery = searchQuery,
                    isSearchVisible = isSearchVisible,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchToggle = { isSearchVisible = !isSearchVisible },
                    onClearSearchQuery = { searchQuery = TextFieldValue("") },
                    onViewModeSelected = {
                        viewModel.setSelectedViewMode(it)
                    },
                    actionModeContent = {
                        if (actionModeState == ActionModeState.STARTED) {
                            AppMgrActionModeItems(doneClicked =
                            {

                            },
                                selectAllClicked = {
                                    viewModel.onSelectAllClicked()
                                })
                        }
                    },
                    onNavigationClick = {
                        viewModel.endActionMode()
                    }
                )
            }) { innerPadding ->
            MainContent(viewModel, innerPadding, searchQuery.text)
        }
    }

    @Composable
    private fun MainContent(
        viewModel: AppMgrViewModel,
        innerPadding: PaddingValues,
        searchText: String
    ) {
        val apps by viewModel.filteredAppsList.observeAsState(initial = emptyList())
        val selectedItems by viewModel.getSelectedItems().collectAsState()
        val viewMode by viewModel.viewMode.observeAsState()

        println("Selected items ${selectedItems.size}")

        if (viewMode == ViewMode.LIST) {
            AppsList(apps, innerPadding, searchText, selectedItems)
        } else {
            AppsGrid(apps, innerPadding, searchText, selectedItems)
        }
    }


    @Composable
    private fun AppsList(
        apps: List<AppInfo>,
        innerPadding: PaddingValues,
        searchText: String,
        selectedItems: Set<Int>
    ) {
        LazyColumn(Modifier.padding(innerPadding)) {
            itemsIndexed(apps.filter {
                it.packageName.contains(searchText, ignoreCase = true) ||
                        it.name.contains(searchText, ignoreCase = true)
            }, key = { _, item -> item.packageName } ) { index, item ->
                val selected = selectedItems.contains(index)

                println("ITEM :${item.packageName} selected:$selected")
                ListItem(item,
                    selected,
                    onItemClick = {
                        onItemClick(it, index)
                    }, onItemLongClick = {
                        onItemLongClicked(index)
                    }
                )
            }
        }
    }

    @Composable
    private fun AppsGrid(
        apps: List<AppInfo>,
        innerPadding: PaddingValues,
        searchText: String,
        selectedItems: Set<Int>
    ) {
        val viewMode = viewModel.getViewMode()
        val gridColumns = getGridColumns(resources.configuration, viewMode)

        LazyVerticalGrid(
            modifier = Modifier.padding(innerPadding),
            columns = GridCells.Fixed(gridColumns)
        ) {
            itemsIndexed(apps.filter {
                it.packageName.contains(searchText, ignoreCase = true) ||
                        it.name.contains(searchText, ignoreCase = true)
            }) { index, item ->
                val selected = selectedItems.contains(index)
                GridItem(item,
                    selected,
                    onItemClick = {
                        onItemClick(it, index)
                    }, onItemLongClick = {
                        onItemLongClicked(index)
                    },
                    viewMode = viewMode
                )
            }
        }
    }

    @ThemePreviews
    @Composable
    fun ListItemPreview(@PreviewParameter(AppInfoProvider::class) data: AppInfo) {
        ListItem(
            data = data,
            selected = false,
            onItemClick = { },
            onItemLongClick = { }
        )
    }

    @ThemePreviews
    @Composable
    fun GridItemPreview(@PreviewParameter(AppInfoProvider::class) data: AppInfo) {
        GridItem(
            data = data,
            selected = false,
            onItemClick = { },
            onItemLongClick = { },
            viewMode = ViewMode.GALLERY
        )
    }

    private fun onItemLongClicked(pos: Int) {
        viewModel.onItemLongClicked(pos)
    }

    private fun onItemClick(appInfo: AppInfo, pos: Int) {
        viewModel.onItemClicked(appInfo, pos)
    }

    private fun setupActionModeToolbar() {
        toolbar.menu.clear()
        toolbar.setNavigationIcon(com.siju.acexplorer.common.R.drawable.ic_back_white)
        toolbar.inflateMenu(com.siju.acexplorer.common.R.menu.action_mode)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun clearActionModeToolbar() {
        toolbar.menu.clear()
        toolbar.navigationIcon = null
    }

    private fun initObservers() {
        viewModel.appsList.observe(viewLifecycleOwner) {
            it?.let {
                Log.d("AppFrag", "initObservers: ${it.size}")
                hideLoadingIndicator()
//                binding.appsListContainer.appsList.layoutManager?.scrollToPosition(0)
                setToolbarSubtitle(it.size)
//                adapter.onDataLoaded(it)
            }
        }
//        viewModel.updateList.observe(viewLifecycleOwner) {
//            it?.getContentIfNotHandled()?.let {
//                if (it) {
//                    adapter.notifyDataSetChanged()
//                }
//            }
//        }
        viewModel.appsSourceFilteredList.observe(viewLifecycleOwner) {
            it?.let {
                setToolbarSubtitle(it.size)
//                adapter.onDataLoaded(it)
            }
        }
//        viewModel.actionModeState.observe(viewLifecycleOwner) {
//            it?.let {
//                onActionModeStateChanged(it)
//            }
//        }
//        viewModel.selectedItemCount.observe(viewLifecycleOwner) {
//            it?.let {
//                setToolbarSubtitle(it)
//            }
//        }
        viewModel.multiOperationData.observe(viewLifecycleOwner) {
            it?.let {
                for (item in it) {
                    AppHelper.uninstallApp(
                        activity as? AppCompatActivity,
                        item.packageName,
                        uninstallResultLauncher
                    )
                }
            }
        }
        viewModel.navigateToAppDetail.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                val directions =
                    AppMgrFragmentDirections.actionAppMgrFragmentToAppDetailActivity(it.first.packageName)
                findNavController().navigate(directions)
            }
        }
        viewModel.refreshList.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
//                adapter.notifyDataSetChanged()
            }
        }
        viewModel.backPressed.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                backPressedCallback.isEnabled = false
                activity?.onBackPressed()
            }
        }
        viewModel.closeSearch.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                if (it) {
                    searchView?.isIconified = true
                }
            }
        }
    }

    private fun showLoadingIndicator() {
//        binding.appsListContainer.swipeRefresh.isRefreshing = true
    }

    private fun hideLoadingIndicator() {
//        binding.appsListContainer.swipeRefresh.isRefreshing = false
    }

    private fun setToolbarSubtitle(count: Int?) {
//        if (count == 0 || count == null) {
//            toolbar.subtitle = ""
//        } else {
//            toolbar.subtitle =
//                context?.resources?.getQuantityString(RC.plurals.number_of_apps, count, count)
//        }
    }

    private fun onActionModeStateChanged(actionModeState: ActionModeState) {
        if (actionModeState == ActionModeState.STARTED) {
            onActionModeStarted()
        } else if (actionModeState == ActionModeState.ENDED) {
            onActionModeEnd()
        }
    }

    private fun onActionModeStarted() {
        setToolbarSubtitle(0)
        setupActionModeToolbar()
        setupBottomToolbar()
        showBottomToolbar()
    }

    private fun onActionModeEnd() {
        clearActionModeToolbar()
//        setupToolbar(toolbar, viewModel.getViewMode())
        hideBottomToolbar()
        setToolbarSubtitle(viewModel.appsList.value?.size)
    }

    private fun setupBottomToolbar() {
        bottomToolbar.menu.clear()
        bottomToolbar.inflateMenu(R.menu.app_action_mode_bottom)
        bottomToolbar.setOnMenuItemClickListener(this)
    }

    private fun showBottomToolbar() {
        bottomToolbar.visibility = View.VISIBLE
    }

    private fun hideBottomToolbar() {
        bottomToolbar.visibility = View.GONE
    }

    private fun fetchData(appType: AppType) {
        showLoadingIndicator()
        viewModel.fetchPackages(appType)
    }

    private fun setupMenuItems(menu: Menu, viewMode: ViewMode) {
        searchItem = menu.findItem(RC.id.action_search)
        setupSearchView()
        menu.findItem(RC.id.action_apps_user).isChecked = true
        menu.findItem(RC.id.action_source_all).isChecked = true
        installSourceItem = menu.findItem(RC.id.action_installed_source)
        allSourceItem = menu.findItem(RC.id.action_source_all)
        userSourceItem = menu.findItem(RC.id.action_apps_user)
        toggleViewModeMenuItemState(viewMode, menu)
    }

    private fun setupSearchView() {
        searchView = searchItem.actionView as SearchView
        searchView?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchView?.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView?.setOnQueryTextListener(this)
        searchView?.queryHint = searchView?.context?.getString(RC.string.search_name_or_package)
        searchView?.maxWidth = Int.MAX_VALUE
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return true
            }
        })
        searchView?.setOnSearchClickListener {
            viewModel.onSearchActive()
        }
        searchView?.setOnCloseListener {
            viewModel.onSearchInactive()
            false
        }
    }

    private fun toggleViewModeMenuItemState(viewMode: ViewMode, menu: Menu) {
        when (viewMode) {
            ViewMode.LIST -> menu.findItem(RC.id.action_view_list).isChecked = true
            ViewMode.GRID -> menu.findItem(RC.id.action_view_grid).isChecked = true
            ViewMode.GALLERY -> menu.findItem(RC.id.action_view_gallery).isChecked = true
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            RC.id.action_apps_system -> {
                installSourceItem.isEnabled = false
                allSourceItem.isChecked = true
                fetchData(AppType.SYSTEM_APP)
                applyBadgeToMenuItem(RC.id.action_filter)
            }

            RC.id.action_apps_user -> {
                allSourceItem.isChecked = true
                installSourceItem.isEnabled = true
                fetchData(AppType.USER_APP)
                clearBadgeMenuItem(RC.id.action_filter)
            }

            RC.id.action_apps_all -> {
                allSourceItem.isChecked = true
                installSourceItem.isEnabled = true
                fetchData(AppType.ALL_APPS)
                applyBadgeToMenuItem(RC.id.action_filter)
            }

            RC.id.action_playstore -> onAppSourceClicked(AppSource.PLAYSTORE)
            RC.id.action_amazon_store -> onAppSourceClicked(AppSource.AMAZON_APPSTORE)
            RC.id.action_unknown -> onAppSourceClicked(AppSource.UNKNOWN)
            RC.id.action_source_all -> {
                if (userSourceItem.isChecked) {
                    clearBadgeMenuItem(RC.id.action_installed_source)
                }
                onAppSourceClicked(AppSource.ALL)
            }

            RC.id.action_view_list -> {
                viewModel.setSelectedViewMode(ViewMode.LIST)
            }

            RC.id.action_view_grid -> {
                viewModel.setSelectedViewMode(ViewMode.GRID)
            }

            RC.id.action_view_gallery -> {
                viewModel.setSelectedViewMode(ViewMode.GALLERY)
            }

            RC.id.action_sort -> {
                context?.let {
                    SortDialog.show(it, viewModel.getSortMode(), sortListener, true)
                }
            }

            com.siju.acexplorer.common.R.id.action_select_all -> {
                viewModel.onSelectAllClicked()
            }

            R.id.action_delete -> {
                viewModel.onDeleteClicked()
            }

        }
        if (item?.isCheckable == true) {
            item.isChecked = !item.isChecked
        }
        return true
    }

    private fun getGridColumns(configuration: Configuration, viewMode: ViewMode): Int {
        return ConfigurationHelper.getStorageGridCols(configuration, viewMode)
    }

    private fun onAppSourceClicked(source: AppSource) {
        applyBadgeToMenuItem(RC.id.action_filter)
        viewModel.filterAppBySource(source)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
    private fun applyBadgeToMenuItem(itemId: Int) {
        val context = context
//        if (menuItemBadge == null) {
//            context?.let {
//                menuItemBadge = BadgeDrawable.create(it)
//                BadgeUtils.attachBadgeDrawable(
//                    menuItemBadge!!,
//                    binding.appBarContainer.toolbarContainer.toolbar,
//                    itemId
//                )
//            }
//        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
    private fun clearBadgeMenuItem(itemId: Int) {
//        val context = context
//        context ?: return
//        menuItemBadge?.let {
//            BadgeUtils.detachBadgeDrawable(
//                it,
//                binding.appBarContainer.toolbarContainer.toolbar,
//                itemId
//            )
//        }
        menuItemBadge = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.updateSearchQuery(newText)
        return true
    }

    private val sortListener = object : SortDialog.Listener {
        override fun onPositiveButtonClick(sortMode: SortMode) {
            viewModel.onSortClicked(sortMode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterPackageReceiver()
        _binding = null
    }

    private fun registerPackageReceiver() {
        packageReceiverRegistered = true
        val filter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addDataScheme(AppHelper.SCHEME_PACKAGE)
        context?.applicationContext?.registerReceiver(packageChangeReceiver, filter)
    }

    private fun unregisterPackageReceiver() {
        if (packageReceiverRegistered) {
            context?.applicationContext?.unregisterReceiver(packageChangeReceiver)
            packageReceiverRegistered = false
        }
    }

    private val packageChangeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            viewModel.fetchPackagesCurrentType()
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.handleBackPress()
        }
    }
}