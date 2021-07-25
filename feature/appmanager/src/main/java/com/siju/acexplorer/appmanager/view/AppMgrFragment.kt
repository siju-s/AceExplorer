package com.siju.acexplorer.appmanager.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.databinding.AppsListContainerBinding
import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.appmanager.filter.AppType
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.appmanager.viewmodel.AppMgrViewModel
import com.siju.acexplorer.common.ActionModeState
import com.siju.acexplorer.common.SortDialog
import com.siju.acexplorer.common.SortMode
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.utils.ConfigurationHelper
import com.siju.acexplorer.common.view.custom.CustomGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppMgrFragment : Fragment(), Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener {

    private var _binding: AppsListContainerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AppMgrAdapter
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AppsListContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        initObservers()
        fetchData(AppType.USER_APP)
        registerPackageReceiver()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun setupUi() {
        val viewMode = viewModel.getViewMode()
        adapter = AppMgrAdapter(
            viewModel,
            viewMode, { appInfo, pos ->
                onItemClick(appInfo, pos)
            },
            { _, pos, _ ->
                onItemLongClicked(pos)
            })
        setupToolbar(binding.appBarContainer.toolbarContainer.toolbar, viewMode)
        this.bottomToolbar = binding.appsListContainer.bottomToolbar
        val appsList = binding.appsListContainer.appsList
        setLayoutManager(appsList, viewMode)
        appsList.adapter = adapter
    }

    private fun onItemLongClicked(pos: Int) {
        viewModel.onItemLongClicked(pos)
    }

    private fun onItemClick(appInfo: AppInfo, pos: Int) {
        viewModel.onItemClicked(appInfo, pos)
    }

    private fun setupToolbar(toolbar: Toolbar, viewMode: ViewMode) {
        this.toolbar = toolbar
        toolbar.title = getString(R.string.app_manager)
        toolbar.inflateMenu(R.menu.app_manager)
        toolbar.setOnMenuItemClickListener(this)
        setupMenuItems(toolbar.menu, viewMode)
    }

    private fun setupActionModeToolbar() {
        toolbar.menu.clear()
        toolbar.setNavigationIcon(R.drawable.ic_back_white)
        toolbar.inflateMenu(R.menu.action_mode)
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
        viewModel.appsList.observe(viewLifecycleOwner, {
            it?.let {
                Log.d("AppFrag", "initObservers: ${it.size}")
                hideLoadingIndicator()
                binding.appsListContainer.appsList.layoutManager?.scrollToPosition(0)
                setToolbarSubtitle(it.size)
                adapter.onDataLoaded(it)
            }
        })
        viewModel.updateList.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let {
                if (it) {
                    adapter.notifyDataSetChanged()
                }
            }
        })
        viewModel.appsSourceFilteredList.observe(viewLifecycleOwner, {
            it?.let {
                setToolbarSubtitle(it.size)
                adapter.onDataLoaded(it)
            }
        })
        viewModel.actionModeState.observe(viewLifecycleOwner, {
            it?.let {
                onActionModeStateChanged(it)
            }
        })
        viewModel.selectedItemCount.observe(viewLifecycleOwner, {
            it?.let {
                if (it > 0) {
                    toolbar.title = it.toString()
                }
            }
        })
        viewModel.selectedItemChanged.observe(viewLifecycleOwner, {
            it?.let {
                adapter.notifyItemChanged(it)
            }
        })
        viewModel.multiOperationData.observe(viewLifecycleOwner, {
            it?.let {
                for (item in it) {
                    AppHelper.uninstallApp(activity as? AppCompatActivity, item.packageName)
                }
            }
        })
        viewModel.navigateToAppDetail.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let {
                val directions = AppMgrFragmentDirections.actionAppMgrFragmentToAppDetailActivity(it.first.packageName)
                findNavController().navigate(directions)
            }
        })
        viewModel.refreshList.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let {
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.backPressed.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let {
                backPressedCallback.isEnabled = false
                activity?.onBackPressed()
            }
        })
        viewModel.closeSearch.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let {
                if (it) {
                    searchView?.isIconified = true
                }
            }
        })
    }

    private fun showLoadingIndicator() {
        binding.appsListContainer.swipeRefresh.isRefreshing = true
    }

    private fun hideLoadingIndicator() {
        binding.appsListContainer.swipeRefresh.isRefreshing = false
    }

    private fun setToolbarSubtitle(count : Int?) {
        if (count == 0 || count == null) {
            toolbar.subtitle = ""
        }
        else {
            toolbar.subtitle = context?.resources?.getQuantityString(R.plurals.number_of_apps, count, count)
        }
    }

    private fun onActionModeStateChanged(actionModeState: ActionModeState) {
        if (actionModeState == ActionModeState.STARTED) {
            adapter.setSelectionMode(true)
            onActionModeStarted()
        }
        else {
            adapter.setSelectionMode(false)
            onActionModeEnd()
        }
        adapter.notifyDataSetChanged()
    }

    private fun onActionModeStarted() {
        setToolbarSubtitle(0)
        setupActionModeToolbar()
        setupBottomToolbar()
        showBottomToolbar()
    }

    private fun onActionModeEnd() {
        clearActionModeToolbar()
        setupToolbar(toolbar, viewModel.getViewMode())
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
        searchItem = menu.findItem(R.id.action_search)
        setupSearchView()
        menu.findItem(R.id.action_apps_user).isChecked = true
        menu.findItem(R.id.action_source_all).isChecked = true
        installSourceItem = menu.findItem(R.id.action_installed_source)
        allSourceItem = menu.findItem(R.id.action_source_all)
        userSourceItem = menu.findItem(R.id.action_apps_user)
        toggleViewModeMenuItemState(viewMode, menu)
    }

    private fun setupSearchView() {
        searchView = searchItem.actionView as SearchView
        searchView?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchView?.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView?.setOnQueryTextListener(this)
        searchView?.queryHint = searchView?.context?.getString(R.string.search_name_or_package)
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
            ViewMode.LIST    -> menu.findItem(R.id.action_view_list).isChecked = true
            ViewMode.GRID    -> menu.findItem(R.id.action_view_grid).isChecked = true
            ViewMode.GALLERY -> menu.findItem(R.id.action_view_gallery).isChecked = true
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_apps_system  -> {
                installSourceItem.isEnabled = false
                allSourceItem.isChecked = true
                fetchData(AppType.SYSTEM_APP)
                applyBadgeToMenuItem(R.id.action_filter)
            }
            R.id.action_apps_user    -> {
                allSourceItem.isChecked = true
                installSourceItem.isEnabled = true
                fetchData(AppType.USER_APP)
                clearBadgeMenuItem(R.id.action_filter)
            }
            R.id.action_apps_all     -> {
                allSourceItem.isChecked = true
                installSourceItem.isEnabled = true
                fetchData(AppType.ALL_APPS)
                applyBadgeToMenuItem(R.id.action_filter)
            }
            R.id.action_playstore    -> onAppSourceClicked(AppSource.PLAYSTORE)
            R.id.action_amazon_store -> onAppSourceClicked(AppSource.AMAZON_APPSTORE)
            R.id.action_unknown      -> onAppSourceClicked(AppSource.UNKNOWN)
            R.id.action_source_all   -> {
                if (userSourceItem.isChecked) {
                    clearBadgeMenuItem(R.id.action_installed_source)
                }
                onAppSourceClicked(AppSource.ALL)
            }
            R.id.action_view_list    -> {
                onViewModeChanged(ViewMode.LIST)
            }
            R.id.action_view_grid    -> {
                onViewModeChanged(ViewMode.GRID)
            }
            R.id.action_view_gallery -> {
                onViewModeChanged(ViewMode.GALLERY)
            }
            R.id.action_sort         -> {
                context?.let {
                    SortDialog.show(it, viewModel.getSortMode(), sortListener, true)
                }
            }
            R.id.action_select_all   -> {
                viewModel.onSelectAllClicked()
            }
            R.id.action_delete       -> {
                viewModel.onDeleteClicked()
            }

        }
        if (item?.isCheckable == true) {
            item.isChecked = !item.isChecked
        }
        return true
    }

    private fun onViewModeChanged(viewMode: ViewMode) {
        if (adapter.getViewMode() != viewMode) {
            val fileList = binding.appsListContainer.appsList
            setLayoutManager(fileList, viewMode)
            adapter.setViewMode(viewMode)
            fileList.adapter = adapter
            viewModel.saveViewMode(viewMode)
        }
    }

    private fun setLayoutManager(fileList: RecyclerView, viewMode: ViewMode) {
        Log.d("AppMgr", "setLayoutManager viewMode: $viewMode")
        val context = fileList.context
        fileList.layoutManager = when (viewMode) {
            ViewMode.LIST                   -> LinearLayoutManager(context)
            ViewMode.GRID, ViewMode.GALLERY -> {
                val gridColumns = getGridColumns(resources.configuration, viewMode)
                val gridLayoutManager = CustomGridLayoutManager(
                    context,
                    gridColumns
                )
                gridLayoutManager
            }
        }
    }

    private fun getGridColumns(configuration: Configuration, viewMode: ViewMode): Int {
        return ConfigurationHelper.getStorageGridCols(configuration, viewMode)
    }

    private fun onAppSourceClicked(source: AppSource) {
        applyBadgeToMenuItem(R.id.action_filter)
        viewModel.filterAppBySource(source)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun applyBadgeToMenuItem(itemId: Int) {
        val context = context
        if (menuItemBadge == null) {
            context?.let {
                menuItemBadge = BadgeDrawable.create(it)
                BadgeUtils.attachBadgeDrawable(
                    menuItemBadge!!,
                    binding.appBarContainer.toolbarContainer.toolbar,
                    itemId
                )
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun clearBadgeMenuItem(itemId: Int) {
        val context = context
        context ?: return
        menuItemBadge?.let {
            BadgeUtils.detachBadgeDrawable(it, binding.appBarContainer.toolbarContainer.toolbar, itemId)
        }
        menuItemBadge = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        adapter.filter(newText)
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