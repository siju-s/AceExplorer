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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.badge.BadgeDrawable
import com.siju.acexplorer.appmanager.AppInfoProvider
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.databinding.AppsListContainerBinding
import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.appmanager.filter.AppType
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.appmanager.view.compose.custom.menu.AppMgrActionModeItems
import com.siju.acexplorer.appmanager.view.compose.GridItem
import com.siju.acexplorer.appmanager.view.compose.ListItem
import com.siju.acexplorer.appmanager.view.compose.custom.menu.AppMgrOverflowMenuItems
import com.siju.acexplorer.appmanager.view.compose.data.AppMgrMenuItem
import com.siju.acexplorer.appmanager.viewmodel.AppMgrViewModel
import com.siju.acexplorer.common.ActionModeState
import com.siju.acexplorer.common.R.string.msg_operation_failed
import com.siju.acexplorer.common.SortDialog
import com.siju.acexplorer.common.SortMode
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.compose.data.IconSource
import com.siju.acexplorer.common.compose.ui.ThemePreviews
import com.siju.acexplorer.common.compose.ui.TopAppBarWithSearch
import com.siju.acexplorer.common.compose.ui.custom.EnumDropdownMenu
import com.siju.acexplorer.common.compose.ui.menu.DropdownMenuTrailingIcon
import com.siju.acexplorer.common.compose.ui.menu.ExpandableMenu
import com.siju.acexplorer.common.theme.MyApplicationTheme
import com.siju.acexplorer.common.theme.Theme
import com.siju.acexplorer.common.utils.ConfigurationHelper
import com.siju.acexplorer.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.siju.acexplorer.common.R as RC


@AndroidEntryPoint
class AppMgrFragment : Fragment(), Toolbar.OnMenuItemClickListener {

    private var _binding: AppsListContainerBinding? = null

    private val viewModel: AppMgrViewModel by viewModels()
    private lateinit var userSourceItem: MenuItem

    private var menuItemBadge: BadgeDrawable? = null
    private lateinit var toolbar: Toolbar
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
        viewModel.fetchPackages(AppType.USER_APP)
        registerPackageReceiver()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    @Composable
    private fun AppContent(viewModel: AppMgrViewModel) {
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            topBar = {
                TopAppBar(viewModel, searchQuery) { query ->
                    searchQuery = query
                }
            })
        { innerPadding ->
            MainContent(viewModel, innerPadding, searchQuery.text)
        }
    }

    @Composable
    private fun TopAppBar(
        viewModel: AppMgrViewModel,
        searchQuery: TextFieldValue,
        onSearchQueryChanged: (TextFieldValue) -> Unit
    ) {
        var isSearchVisible by remember { mutableStateOf(false) }
        val actionModeState by viewModel.actionModeState.observeAsState()
        val viewMode by viewModel.viewMode.collectAsState()
        var showBadge by remember { mutableStateOf(false) }

        TopAppBarWithSearch(title = getString(R.string.app_manager),
            actionModeEnabled = actionModeState == ActionModeState.STARTED,
            searchQuery = searchQuery,
            searchPlaceholderText = R.string.search_name_or_package,
            isSearchVisible = isSearchVisible,
            onSearchQueryChange = { onSearchQueryChanged(it) },
            onSearchToggle = { isSearchVisible = !isSearchVisible },
            onClearSearchQuery = { onSearchQueryChanged(TextFieldValue("")) },
            menuItems = {
                ExpandableMenu(
                    IconSource.Painter(com.siju.acexplorer.common.R.drawable.ic_filter),
                    R.string.filter,
                    showBadge = showBadge
                ) { dismissMenu ->
                    AppsTypeMenuItem(
                        viewModel.getAppType(),
                        viewModel.getAppSource(),
                        dismissMenu
                    ) { menuItem ->
                        if (menuItem is AppMgrMenuItem.AppType) {
                            showBadge = menuItem.appType != AppType.USER_APP
                            viewModel.fetchPackages(menuItem.appType)
                        } else if (menuItem is AppMgrMenuItem.AppSource) {
                            showBadge = menuItem.source != AppSource.ALL
                            viewModel.filterAppBySource(menuItem.source)
                        }
                    }
                }
            },
            actionModeContent = {
                if (actionModeState == ActionModeState.STARTED) {
                    AppMgrActionModeItems(doneClicked = {
                        viewModel.onDeleteClicked()
                    },
                        selectAllClicked = {
                            viewModel.onSelectAllClicked()
                        })
                }
            },
            onNavigationClick = {
                viewModel.endActionMode()
            },
            overflowMenuItems = { dismissMenu ->
                AppMgrOverflowMenuItems(viewMode, dismissMenu = dismissMenu) { menuItem ->
                    if (menuItem is AppMgrMenuItem.ViewMode) {
                        viewModel.setSelectedViewMode(menuItem.viewMode)
                    } else if (menuItem is AppMgrMenuItem.Sort) {
                        viewModel.onSortClicked()
                    }
                }
            }
        )
    }

    @Composable
    private fun MainContent(
        viewModel: AppMgrViewModel,
        innerPadding: PaddingValues,
        searchText: String
    ) {
        val apps by viewModel.filteredAppsList.observeAsState(initial = emptyList())
        val selectedItems by viewModel.getSelectedItems().collectAsState()
        val viewMode by viewModel.viewMode.collectAsState()

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
            }, key = { _, item -> item.packageName }) { index, item ->
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
        val viewMode by viewModel.viewMode.collectAsState()
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

    @Composable
    fun AppsTypeMenuItem(
        selectedAppType: AppType,
        selectedAppSource: AppSource,
        dismissMenu: () -> Unit,
        onMenuItemClicked: (AppMgrMenuItem) -> Unit
    ) {
        var showSubMenu by remember { mutableStateOf(false) }

        if (!showSubMenu) {
            EnumDropdownMenu(selectedItem = selectedAppType,
                enumEntries = AppType.entries.toTypedArray(),
                resourceIdProvider = { it.resourceId },
                dismissMenu = dismissMenu,
                onMenuItemClicked = { appType ->
                    onMenuItemClicked(AppMgrMenuItem.AppType(appType))
                }
            )
            DropdownMenuTrailingIcon(R.string.source_from) {
                showSubMenu = true
            }
        } else {
            EnumDropdownMenu(selectedItem = selectedAppSource,
                enumEntries = AppSource.entries.filter { it != AppSource.SYSTEM }.toTypedArray(),
                resourceIdProvider = { it.resourceId },
                dismissMenu = dismissMenu,
                onMenuItemClicked = { appType ->
                    onMenuItemClicked(AppMgrMenuItem.AppSource(appType))
                }
            )
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

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sortClicked.collect { sortMode ->
                    context?.let {
                        SortDialog.show(it, sortMode, object : SortDialog.Listener {
                            override fun onPositiveButtonClick(sortMode: SortMode) {
                                viewModel.onSortClicked(sortMode)
                            }
                        })
                    }
                }
            }
        }

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
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            RC.id.action_apps_system -> {
                applyBadgeToMenuItem(RC.id.action_filter)
            }

            RC.id.action_apps_user -> {
                clearBadgeMenuItem(RC.id.action_filter)
            }

            RC.id.action_apps_all -> {
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
//        val context = context
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