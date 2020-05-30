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

package com.siju.acexplorer.storage.view

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.ads.AdsView
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.appmanager.view.AppDetailActivity
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.showToast
import com.siju.acexplorer.home.view.CategoryMenuHelper
import com.siju.acexplorer.main.helper.UpdateChecker
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.CategoryHelper.isAppManager
import com.siju.acexplorer.main.model.helper.PermissionsHelper
import com.siju.acexplorer.main.model.helper.ShareHelper
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.main.model.helper.ViewHelper
import com.siju.acexplorer.main.view.InfoFragment
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper.PermissionDialogListener
import com.siju.acexplorer.main.viewmodel.InfoSharedViewModel
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.main.viewmodel.Pane
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.storage.model.PasteOpData
import com.siju.acexplorer.storage.model.SortMode
import com.siju.acexplorer.storage.model.StorageModelImpl
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.model.operations.*
import com.siju.acexplorer.storage.modules.picker.model.PickerModelImpl
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.storage.modules.picker.view.PickerFragment
import com.siju.acexplorer.storage.modules.zipviewer.view.ZipViewerFragment
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import com.siju.acexplorer.storage.viewmodel.FileListViewModelFactory
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.utils.InstallHelper
import com.siju.acexplorer.utils.InstallHelper.openInstallScreen
import kotlinx.android.synthetic.main.main_list.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

const val KEY_PATH = "path"
const val KEY_CATEGORY = "category"
const val KEY_SHOW_NAVIGATION = "show_navigation"
private const val TAG = "BaseFileListFragment"
private const val SAF_REQUEST = 2000
private const val EXTRACT_PATH_REQUEST = 5000
private const val COPY_PATH_REQUEST = 6000
private const val CUT_PATH_REQUEST = 7000

abstract class BaseFileListFragment : Fragment(), FileListHelper {

    private var packageReceiverRegistered = false
    private lateinit var filesList: FilesList
    private lateinit var floatingView: FloatingView
    private lateinit var navigationView: NavigationView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var fileListViewModel: FileListViewModel
    private lateinit var menuControls: MenuControls
    private lateinit var infoSharedViewModel: InfoSharedViewModel

    private var adView: AdsView? = null
    private var categoryMenuHelper: CategoryMenuHelper? = null
    private var path: String? = null
    private var category = Category.FILES
    private var showNavigation = true

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getArgs()
        Log.d(TAG, "onActivityCreated:$this, category:$category, path:$path, dest:${findNavController().currentDestination}")
        val view = view
        val container = view?.findViewById<CoordinatorLayout>(R.id.main_content)
        container?.let {
            adView = AdsView(it)
        }
        setupToolbar()
        setupViewModels()

        view?.let {
            val viewMode = fileListViewModel.getViewMode(category)
            filesList = FilesList(this, view, viewMode, category, mainViewModel.getSortMode())
            floatingView = FloatingView(view, this)
            navigationView = NavigationView(view, fileListViewModel.navigationCallback)
            val appbarView = getAppBarView(it) ?: return
            menuControls = MenuControls(this, view, appbarView, category, viewMode)
            setupMultiSelection()
            setupNavigationView()
            initObservers()
            setupDualScreenMode()
        }
    }

    private fun getAppBarView(it: View): View? {
        return if (showNavigation) {
            it
        }
        else {
            categoryMenuHelper?.getCategoryView()
        }
    }

    private fun setupDualScreenMode() {
        Log.d(TAG, "setupDualScreenMode:dualMode:${mainViewModel.isDualPaneEnabled()}")
        if (this is FileListFragment && mainViewModel.isDualPaneEnabled()) {
            mainViewModel.setStorageReady()
        }
    }

    private fun setupMultiSelection() {
        filesList.setMultiSelectionHelper(fileListViewModel.multiSelectionHelper)
    }

    private fun setupToolbar() {
        if (!showNavigation) {
            appbar.visibility = View.GONE
        }
        else {
            setToolbarTitle(toolbar)
        }
    }

    private fun setToolbarTitle(toolbar: Toolbar) {
        when {
            isAppManager(category) -> {
                toolbar.title = resources.getString(R.string.app_manager)
            }
            category == Category.FILES -> {
                toolbar.title = resources.getString(R.string.app_name)
            }
            else -> {
                toolbar.title = CategoryHelper.getCategoryName(context, category).toUpperCase(Locale.getDefault())
            }
        }
    }

    private fun setupNavigationView() {
        fileListViewModel.setNavigationView(navigationView)
        if (isAppManager(category) || !showNavigation) {
            navigationView.hideNavigationView()
        }
        else {
            navigationView.showNavigationView()
        }
    }

    private fun getArgs() {
        val args = arguments
        args?.let {
            if (this is FileListFragment) {
                val bundle = FileListFragmentArgs.fromBundle(args)
                path = bundle.path
                category =  bundle.category
                showNavigation = bundle.showNavigation
            }
            else {
                val bundle = DualPaneFragmentArgs.fromBundle(args)
                path = bundle.path
                category =  bundle.category
                showNavigation = bundle.showNavigation
            }
        }
    }

    private fun setupViewModels() {
        Log.d(TAG, "setupViewModels:$this")
        val activity = requireNotNull(activity)
        mainViewModel = ViewModelProvider(activity).get(MainViewModel::class.java)
        val viewModelFactory = FileListViewModelFactory(StorageModelImpl(AceApplication.appContext, category))
        fileListViewModel = ViewModelProvider(this, viewModelFactory)
                .get(FileListViewModel::class.java)
        infoSharedViewModel = ViewModelProvider(activity).get(InfoSharedViewModel::class.java)
        categoryMenuHelper = mainViewModel.getCategoryMenuHelper()
    }

    fun showUpdateSnackbar(updateChecker: UpdateChecker?) {
        updateChecker?.showUpdateSnackbar(view?.findViewById(R.id.main_content))
    }

    @Suppress("ObjectLiteralToLambda")
    private fun initObservers() {
        mainViewModel.permissionStatus.observe(viewLifecycleOwner, Observer { permissionStatus ->
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Granted -> {
                    if (isAppManager(category)) {
                        registerPackageReceiver(AceApplication.appContext)
                    }
                    fileListViewModel.loadData(path,
                                               category)
                }
                else -> {
                }
            }
        })

        mainViewModel.premiumLiveData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (it.entitled) {
                    hideAds()
                }
                else {
                    showAds()
                }
            }
        })

        mainViewModel.theme.observe(viewLifecycleOwner, Observer {
            if (::menuControls.isInitialized) {
                floatingView.setTheme(it)
                menuControls.setTheme(it)
            }
        })

        fileListViewModel.fileData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (::filesList.isInitialized) {
                    filesList.onDataLoaded(it, fileListViewModel.category, fileListViewModel.isZipMode())
                }
            }
        })

        fileListViewModel.recentFileData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (::filesList.isInitialized) {
                    filesList.onRecentDataLoaded(it.first, it.second)
                }
            }
        })

        //TODO 23 Feb 2020 Empty observer since we need atleast 1 observer for sort mode to reflect changes and we don't need lambda that's why suppressed lint warning.
        mainViewModel.sortMode.observe(viewLifecycleOwner, object : Observer<Int> {
            override fun onChanged(t: Int?) {
                Log.d(TAG, "Sort mode:$t")
            }
        })

        mainViewModel.refreshGridCols.observe(viewLifecycleOwner, Observer {
            it?.apply {
                Log.d(TAG, "refreshGridCols pane:${it.first}, reload:${it.second}, this:${this@BaseFileListFragment is FileListFragment}")
                if (::filesList.isInitialized && shouldRefreshPane(it.first, it.second)) {
                    filesList.refreshGridColumns(fileListViewModel.getViewMode(category))
                    mainViewModel.setRefreshDone(it.first)
                }
            }
        })

        mainViewModel.reloadPane.observe(viewLifecycleOwner, Observer {
            it?.apply {
                val pane = it.first
                val reload = it.second
                Log.d(TAG, "Reload pane:$pane, reload:$reload, this:${this@BaseFileListFragment is FileListFragment}")
                if (shouldRefreshPane(pane, reload)) {
                    fileListViewModel.refreshList()
                    mainViewModel.setReloadPane(pane, false)
                }
            }
        })

        fileListViewModel.showFab.observe(viewLifecycleOwner, Observer { showFab ->
            Log.d(TAG, "Showfab:$showFab")
            if (showFab) {
                floatingView.showFab()
            }
            else {
                floatingView.hideFab()
            }
        })

        fileListViewModel.viewFileEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                viewFile(it.first, it.second)
            }
        })

        fileListViewModel.viewImageFileEvent.observe(viewLifecycleOwner, Observer {
            ViewHelper.openImage(context, it.first, it.second)
        })

        fileListViewModel.viewMode.observe(viewLifecycleOwner, Observer {
            if (::filesList.isInitialized) {
                filesList.onViewModeChanged(it)
                refreshGridCols()
            }
            if (::menuControls.isInitialized) {
                menuControls.onViewModeChanged(it)
            }
        })

        fileListViewModel.sortEvent.observe(viewLifecycleOwner, Observer { sortMode ->
            context?.let { DialogHelper.showSortDialog(it, sortMode, sortDialogListener) }
        })

        fileListViewModel.installAppEvent.observe(viewLifecycleOwner, Observer {
            val canInstall = it.first
            if (canInstall) {
                openInstallScreen(context, it.second)
            }
            else {
                InstallHelper.requestUnknownAppsInstallPermission(this)
            }
        })

        fileListViewModel.actionModeState.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "actionModeState:$it")
            it?.apply {
                when (it) {
                    ActionModeState.STARTED -> {
                        onActionModeStarted()
                    }
                    ActionModeState.ENDED -> {
                        onActionModeEnded()
                    }
                }
            }
        })

        fileListViewModel.selectedFileInfo.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (fileListViewModel.actionModeState.value != ActionModeState.ENDED) {
                    menuControls.onSelectedCountChanged(it.first, it.second,
                                                        mainViewModel.getExternalSdList())
                }
            }
        })

        fileListViewModel.refreshEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (it) {
                    filesList.refresh()
                }
            }
        })

        fileListViewModel.singleOpData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                handleSingleItemOperation(it)
            }
        })

        fileListViewModel.homeClicked.observe(viewLifecycleOwner, Observer {
            it?.apply {
                mainViewModel.onHomeClicked()
                activity?.onBackPressed()
            }
        })

        fileListViewModel.operationResult.observe(viewLifecycleOwner, Observer {
            it?.apply {
                handleOperationResult(it)
            }
        })

        fileListViewModel.noOpData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                when (it.first) {
                    Operations.FOLDER_CREATION -> showCreateFolderDialog(context)
                    Operations.FILE_CREATION -> showCreateFileDialog(context)
                    else -> {}
                }
            }
        })

        fileListViewModel.multiSelectionOpData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                handleMultiItemOperation(it)
            }
        })

        fileListViewModel.pasteOpData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                handlePasteOperation(it)
            }
        })

        fileListViewModel.pasteConflictCheckData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                handlePasteOperation(it)
            }
        })

        fileListViewModel.showPasteDialog.observe(viewLifecycleOwner, Observer {
            it?.apply {
                context?.let { context ->
                    OperationProgress().showPasteDialog(context, it.second, it.third, it.first)
                }
            }
        })

        fileListViewModel.showZipDialog.observe(viewLifecycleOwner, Observer {
            it?.apply {
                context?.let { context ->
                    dismissDialog()
                    OperationProgress().showExtractProgressDialog(context, it.second, it.third)
                }
            }
        })

        fileListViewModel.showCompressDialog.observe(viewLifecycleOwner, Observer {
            it?.apply {
                context?.let { context ->
                    dismissDialog()
                    OperationProgress().showZipProgressDialog(context, it.second, it.third)
                }
            }
        })

        fileListViewModel.directoryClicked.observe(viewLifecycleOwner, Observer {
            it?.apply {
                fileListViewModel.saveScrollInfo(filesList.getScrollInfo())
                mainViewModel.setPaneFocus(this@BaseFileListFragment is DualPaneFragment)
            }
        })

        fileListViewModel.scrollInfo.observe(viewLifecycleOwner, Observer {
            it?.apply {
                filesList.scrollToPosition(it)
            }
        })

        fileListViewModel.openZipViewerEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                fileListViewModel.saveScrollInfo(filesList.getScrollInfo())
                val zipViewer = ZipViewerFragment(this@BaseFileListFragment, it.first,
                                                  it.second)
                floatingView.hideFab()
                fileListViewModel.setZipViewer(zipViewer)
            }
        })

        fileListViewModel.dragEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                filesList.startDrag(it.first, it.second, it.third)
            }
        })

        fileListViewModel.showDragDialog.observe(viewLifecycleOwner, Observer {
            it?.apply {
                filesList.showDragDialog(it.first, it.second, it.third)
            }
        })

        fileListViewModel.navigationClicked.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (it) {
                    mainViewModel.setPaneFocus(this@BaseFileListFragment is DualPaneFragment)
                }
            }
        })

        fileListViewModel.refreshData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (it) {
                    fileListViewModel.refreshList()
                    fileListViewModel.setRefreshStateFalse()
                }
            }
        })

        mainViewModel.onMenuItemClicked.observe(viewLifecycleOwner, Observer {
            it?.let {
                val item = it.getMenuItem()
                item?.let {
                    onMenuItemClick(item)
                }
            }
        })

        mainViewModel.refreshData.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    refreshDataOnTabSelected()
                }
            }
        })
    }

    override fun openPeekPopInfo(fileInfo: FileInfo, uri: Uri?) {
        infoSharedViewModel.apply {
            setFileInfo(fileInfo)
            setUri(uri)
        }
        InfoFragment.newInstance(activity?.supportFragmentManager)
    }

    private fun onActionModeStarted() {
        floatingView.hideFab()
        hideAds()
        menuControls.onStartActionMode()
        categoryMenuHelper?.disableTab()
    }

    private fun onActionModeEnded() {
        if (Category.FILES == category) {
            floatingView.showFab()
        }
        menuControls.onEndActionMode()
        filesList.onEndActionMode()
        if (mainViewModel.isFreeVersion()) {
            showAds()
        }
        if (categoryMenuHelper == null) {
            setToolbarTitle(toolbar)
        }
        else {
            categoryMenuHelper?.enableTab()
            categoryMenuHelper?.setToolbarTitle()
        }
    }

    private fun shouldRefreshPane(pane : Pane, reload : Boolean) : Boolean {
        Log.d(TAG, "Reload pane:$pane, reload:$reload, this:${this@BaseFileListFragment is FileListFragment}")
        return (reload && ((this@BaseFileListFragment is FileListFragment && pane == Pane.SINGLE) ||
                        this@BaseFileListFragment is DualPaneFragment && pane == Pane.DUAL ))
    }

    private fun reloadPane() {
        if (fileListViewModel.isDualModeEnabled()) {
            val paneToReload = getPane()
            mainViewModel.setReloadPane(paneToReload, true)
        }
    }

    private fun refreshGridCols() {
        if (fileListViewModel.isDualModeEnabled()) {
            val paneToRefresh = getPane()
            mainViewModel.refreshLayout(paneToRefresh)
        }
    }

    private fun getPane(): Pane {
        return if (this is DualPaneFragment) {
            Pane.SINGLE
        } else {
            Pane.DUAL
        }
    }

    override fun onPause() {
        super.onPause()
        fileListViewModel.onPause()
    }

    override fun onResume() {
        super.onResume()
        fileListViewModel.onResume()
    }

    private fun registerPackageReceiver(context: Context) {
        packageReceiverRegistered = true
        val filter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addDataScheme(AppHelper.SCHEME_PACKAGE)
        context.registerReceiver(packageChangeReceiver, filter)
    }

    private fun unregisterPackageReceiver(context: Context) {
        if (packageReceiverRegistered) {
            context.unregisterReceiver(packageChangeReceiver)
            packageReceiverRegistered = false
        }
    }

    private fun handlePasteOperation(pasteConflictCheckData: PasteConflictCheckData) {
        context?.let { DialogHelper.showConflictDialog(it, pasteConflictCheckData, fileListViewModel.pasteConflictListener) }
    }

    private val packageChangeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            fileListViewModel.loadData(null, Category.APP_MANAGER)
        }
    }

    private fun handleOperationResult(operationResult: Pair<Operations, OperationAction>) {
        val action = operationResult.second
        val operation = operationResult.first
        val context = context
        Log.d(TAG, "handleOperationResult: $operation, result:${action.operationResult.resultCode}")
        context?.let {
            when (action.operationResult.resultCode) {
                OperationResultCode.SUCCESS -> {
                    onOperationSuccess(operation, action)
                }
                OperationResultCode.SAF -> {
                    dismissDialog()
                    showSAFDialog(context, action.operationData.arg1)
                }
                OperationResultCode.INVALID_FILE -> {
                    onOperationError(operation, context.getString(
                            R.string.msg_error_invalid_name))
                }
                OperationResultCode.FILE_EXISTS -> {
                    onOperationError(operation, context.getString(
                            R.string.msg_file_exists))
                }
                OperationResultCode.FAVORITE_EXISTS -> {
                    onOperationError(operation, context.getString(R.string.fav_exists))
                }
                OperationResultCode.FAIL -> {
                    onOperationFailed(context)
                }
            }
        }
    }

    private fun onOperationFailed(context: Context) {
        dismissDialog()
        context.showToast(getString(R.string.msg_operation_failed))
    }

    private fun onOperationSuccess(operation: Operations, operationAction: OperationAction) {
        when (operation) {
            Operations.DELETE -> {
                val count = operationAction.operationResult.count
                context?.showToast(resources.getQuantityString(R.plurals.number_of_files, count,
                                                               count) +
                                           " " + resources.getString(R.string.msg_delete_success))
            }

            Operations.COPY -> {
                val count = operationAction.operationResult.count
                context?.showToast(String.format(
                        Locale.getDefault(), resources.getString(R.string.copied), count))
            }

            Operations.CUT -> {
                val count = operationAction.operationResult.count
                context?.showToast(String.format(
                        Locale.getDefault(), resources.getString(R.string.moved), count))
            }

            Operations.FAVORITE -> {
                val count = operationAction.operationResult.count
                context?.showToast(String.format(
                        Locale.getDefault(), resources.getString(R.string.msg_added_to_fav), count))
            }
            else -> {}
        }
        dismissDialog()
        if (operation != Operations.FAVORITE) {
            fileListViewModel.loadData(fileListViewModel.currentDir,
                                       fileListViewModel.category)
            reloadPane()
        }
    }


    private fun handleSingleItemOperation(operationData: Pair<Operations, FileInfo>) {
        Log.d(TAG, "handleSingleItemOperation: ${operationData.second.permissions}")
        when (operationData.first) {
            Operations.RENAME -> {
                context?.let { context -> showRenameDialog(context, operationData.second) }
            }
            Operations.INFO -> {
                context?.let { context ->
                    val fileInfo = operationData.second
                    infoSharedViewModel.setFileInfo(fileInfo)
                    infoSharedViewModel.setUri(UriHelper.createContentUri(context, fileInfo.filePath))
                    InfoFragment.newInstance(activity?.supportFragmentManager)
                }
            }

            Operations.EXTRACT -> {
                context?.let { context ->
                    DialogHelper.showExtractDialog(context, operationData.second.filePath,
                                                   fileListViewModel.currentDir,
                                                   extractDialogListener)
                }
            }
            Operations.PERMISSIONS -> {
                context?.let { context ->
                    val fileInfo = operationData.second
                    val permissions = fileInfo.permissions
                    val permissionList = PermissionsHelper.parse(permissions)
                    DialogHelper.showPermissionsDialog(context, fileInfo.filePath, fileInfo.isDirectory, permissionList, permissionDialogListener)
                }
            }
            else -> {
            }
        }
    }

    private fun handleMultiItemOperation(operationData: Pair<Operations, ArrayList<FileInfo>>) {
        when (operationData.first) {
            Operations.DELETE -> {
                context?.let { context ->
                    if (isAppManager(category)) {
                        for (fileInfo in operationData.second) {
                            AppHelper.uninstallApp(activity as AppCompatActivity, fileInfo.filePath)
                        }
                    }
                    else {
                        showDeleteDialog(context, operationData.second)
                    }
                }
            }
            Operations.SHARE -> {
                context?.let { ShareHelper.shareFiles(it, operationData.second, category) }
            }

            Operations.COPY -> {
                showCopyToDialog()
            }

            Operations.CUT -> {
                showCutToDialog()
            }

            Operations.COMPRESS -> {
                context?.let {
                    showCompressDialog(it, operationData.second)
                }
            }

            Operations.FAVORITE -> {
                fileListViewModel.addToFavorite(operationData.second)
            }

            Operations.DELETE_FAVORITE -> {
                fileListViewModel.removeFavorite(operationData.second)
            }

            else -> {
            }
        }
    }

    private fun handlePasteOperation(pasteOpData: PasteOpData) {
        pasteOpData.destinationDir?.let {
            fileListViewModel.onPaste(it, Pair(pasteOpData.operations, pasteOpData.filesToPaste))
        }
    }

    fun onCreateDirClicked() {
        fileListViewModel.onFABClicked(Operations.FOLDER_CREATION, fileListViewModel.currentDir)
    }

    fun onCreateFileClicked() {
        fileListViewModel.onFABClicked(Operations.FILE_CREATION, fileListViewModel.currentDir)
    }

    private fun showCreateFolderDialog(context: Context?) {
        context?.let {
            val title = context.getString(R.string.new_folder)
            val texts = arrayOf(title, context.getString(R.string.enter_name),
                                context.getString(R.string.create),
                                context.getString(R.string.dialog_cancel))

            DialogHelper.showInputDialog(context, texts, Operations.FOLDER_CREATION, null,
                                         alertDialogListener)
        }
    }

    private fun showCreateFileDialog(context: Context?) {
        context?.let {
            val title = context.getString(R.string.new_file)
            val texts = arrayOf(title, context.getString(R.string.enter_name),
                                context.getString(R.string.create),
                                context.getString(R.string.dialog_cancel))

            DialogHelper.showInputDialog(context, texts, Operations.FILE_CREATION, null,
                                         alertDialogListener)
        }
    }

    private fun showDeleteDialog(context: Context, files: ArrayList<FileInfo>) {
        DialogHelper.showDeleteDialog(context, files, false, deleteDialogListener)
    }

    private fun showCompressDialog(context: Context, files: ArrayList<FileInfo>) {
        DialogHelper.showCompressDialog(context, files, compressDialogListener)
    }

    private fun onOperationError(operation: Operations, message: String) {
        when (operation) {
            Operations.FOLDER_CREATION, Operations.FILE_CREATION, Operations.EXTRACT, Operations.RENAME, Operations.COMPRESS -> {
                val editText = dialog?.findViewById<EditText>(R.id.editFileName)
                editText?.requestFocus()
                editText?.error = message
            }
            Operations.HIDE, Operations.FAVORITE -> Toast.makeText(
                    context, message, Toast.LENGTH_SHORT).show()
            else -> {
            }
        }
    }


    private fun showRenameDialog(context: Context, fileInfo: FileInfo) {
        val title = context.getString(R.string.action_rename)
        val texts = arrayOf(title, context.getString(R.string.enter_name),
                            context.getString(R.string.action_rename),
                            context.getString(R.string.dialog_cancel))
        DialogHelper.showInputDialog(context, texts, Operations.RENAME, fileInfo.filePath,
                                     alertDialogListener)
    }

    private fun showSAFDialog(context: Context, path: String) {
        DialogHelper.showSAFDialog(context, path, safDialogListener)
    }

    private fun dismissDialog() {
        dialog?.dismiss()
    }

    private var dialog: Dialog? = null

    private val permissionDialogListener = object : PermissionDialogListener {

        override fun onPositiveButtonClick(path: String?, isDir: Boolean, permissions: String?) {
            if (path != null && permissions != null) {
                fileListViewModel.setPermissions(path, permissions, isDir)
            }
        }
    }

    private val alertDialogListener = object : DialogHelper.DialogCallback {

        override fun onPositiveButtonClick(dialog: Dialog?, operation: Operations?,
                                           name: String?) {
            this@BaseFileListFragment.dialog = dialog
            fileListViewModel.onOperation(operation, name)
        }

        override fun onNegativeButtonClick(operations: Operations?) {
        }
    }

    private val extractDialogListener = object : DialogHelper.ExtractDialogListener {
        override fun onPositiveButtonClick(dialog: Dialog?,
                operation: Operations,
                sourceFilePath: String,
                newFileName: String, destinationDir: String) {
            this@BaseFileListFragment.dialog = dialog
            fileListViewModel.onExtractOperation(newFileName, destinationDir)
        }

        override fun onSelectButtonClicked(dialog: Dialog) {
            val theme = mainViewModel.theme.value
            this@BaseFileListFragment.dialog = dialog
            if (theme == null) {
                return
            }
            activity?.let {
                val dialogFragment = PickerFragment.newInstance(it as AppCompatActivity, getThemeStyle(theme),
                        PickerType.FILE)
                dialogFragment.setTargetFragment(this@BaseFileListFragment, EXTRACT_PATH_REQUEST)
                this@BaseFileListFragment.parentFragmentManager.let { fragmentManager ->
                    dialogFragment.show(fragmentManager, "Browse Fragment")
                }
            }
        }
    }

    private fun showCopyToDialog() {
        val theme = mainViewModel.theme.value ?: return
        activity?.let {
            val dialogFragment = PickerFragment.newInstance(it as AppCompatActivity, getThemeStyle(theme),
                    PickerType.COPY)
            dialogFragment.setTargetFragment(this@BaseFileListFragment, COPY_PATH_REQUEST)
            this@BaseFileListFragment.parentFragmentManager.let { fragmentManager ->
                dialogFragment.show(fragmentManager, "Browse Fragment")
            }
        }
    }

    private fun showCutToDialog() {
        val theme = mainViewModel.theme.value ?: return
        activity?.let {
            val dialogFragment = PickerFragment.newInstance(it as AppCompatActivity, getThemeStyle(theme),
                    PickerType.CUT)
            dialogFragment.setTargetFragment(this@BaseFileListFragment, CUT_PATH_REQUEST)
            this@BaseFileListFragment.parentFragmentManager.let { fragmentManager ->
                dialogFragment.show(fragmentManager, "Browse Fragment")
            }
        }
    }

    private val compressDialogListener = object : DialogHelper.CompressDialogListener {
        override fun onPositiveButtonClick(dialog: Dialog?, operation: Operations,
                                           newFileName: String,
                                           paths: ArrayList<FileInfo>) {
            this@BaseFileListFragment.dialog = dialog
            fileListViewModel.onOperation(operation, newFileName)
        }

        override fun onNegativeButtonClick(operation: Operations?) {
        }
    }

    private fun getThemeStyle(theme: Theme): Int {
        return when (theme) {
            Theme.DARK -> R.style.BaseDarkTheme
            Theme.LIGHT, Theme.DEVICE -> R.style.BaseLightTheme
        }
    }

    private val deleteDialogListener = object : DialogHelper.DeleteDialogListener {
        override fun onPositiveButtonClick(view: View, isTrashEnabled: Boolean, filesToDelete: ArrayList<FileInfo>) {
            fileListViewModel.deleteFiles(filesToDelete)
        }

        override fun onPositiveButtonClick(view: View?, isTrashEnabled: Boolean, filesToDelete: Uri) {

        }
    }

    private fun triggerStorageAccessFramework() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                                   SAF_REQUEST)
        }
        else {
            Toast.makeText(context, context?.getString(R.string.msg_error_not_supported),
                           Toast.LENGTH_LONG).show()
        }
    }


    private val safDialogListener = object : DialogHelper.AlertDialogListener {

        override fun onPositiveButtonClick(view: View) {
            triggerStorageAccessFramework()
        }

        override fun onNeutralButtonClick(view: View) {

        }

        override fun onNegativeButtonClick(view: View) {
            Toast.makeText(context, context?.getString(R.string.error), Toast
                    .LENGTH_SHORT).show()
        }
    }

    private fun showAds() {
        adView?.showAds()
    }

    private fun hideAds() {
        adView?.hideAds()
    }

    override fun handleItemClick(fileInfo: FileInfo, position: Int) {
        if (isAppManager(category) && !isActionModeActive()) {
            context?.let {
                menuControls.endSearch()
                AppDetailActivity.openAppInfo(it, fileInfo.filePath)
            }
        }
        else {
            fileListViewModel.handleItemClick(fileInfo, position)
        }
    }

    override fun handleLongItemClick(fileInfo: FileInfo, second: Int) {
        fileListViewModel.handleLongClick(fileInfo, second)
    }

    override fun isActionModeActive(): Boolean {
        return fileListViewModel.isActionModeActive()
    }

    override fun getActivityInstance() : AppCompatActivity = this.activity as AppCompatActivity

    fun onBackPressed() : Boolean {
        Log.d(TAG, "onBackPressed:$this")
        val isPeekMode = filesList.isPeekMode()
        return when {
            isPeekMode -> {
                filesList.endPeekMode()
                false
            }
            floatingView.isFabExpanded -> {
                floatingView.collapseFab()
                false
            }
            menuControls.isSearchActive() -> {
                menuControls.endSearch()
                false
            }
            else -> {
                fileListViewModel.onBackPress()
            }
        }
    }

        private fun viewFile(path: String, extension: String?) {
            Log.d(TAG, "viewFile:path:$path, extension:$extension")
            val context = context
            context?.let {
                when (extension?.toLowerCase(Locale.ROOT)) {
                    null -> {
                        val uri = UriHelper.createContentUri(context, path)
                        uri?.let {
                            DialogHelper.openWith(it, context)
                        }
                    }
                    ViewHelper.EXT_APK -> ViewHelper.viewApkFile(context, path,
                            fileListViewModel.apkDialogListener)
                    else -> ViewHelper.viewFile(context, path, extension)
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
            when (requestCode) {
                InstallHelper.UNKNOWN_APPS_INSTALL_REQUEST -> {
                    if (resultCode == RESULT_OK) {
                        openInstallScreen(context, fileListViewModel.apkPath)
                        fileListViewModel.apkPath = null
                    }
                }

                SAF_REQUEST -> {
                    if (resultCode == RESULT_OK) {
                        val uri = intent?.data
                        if (uri == null) {
                            Toast.makeText(context, resources.getString(R.string
                                    .access_denied_external),
                                    Toast.LENGTH_LONG).show()
                        }
                        else {
                            fileListViewModel.handleSafResult(uri, intent.flags)
                        }

                    }
                    else {
                        Analytics.logger.safResult(false)
                        Toast.makeText(context, resources.getString(R.string
                                .access_denied_external),
                                Toast.LENGTH_LONG).show()
                    }
                }

                EXTRACT_PATH_REQUEST -> {
                    if (resultCode == RESULT_OK) {
                        val destDir = intent?.getStringExtra(PickerModelImpl.KEY_PICKER_SELECTED_PATH)
                        val pathButton = dialog?.findViewById<Button>(R.id.buttonPathSelect)
                        pathButton?.text = destDir
                    }
                }

                COPY_PATH_REQUEST -> {
                    if (resultCode == RESULT_OK) {
                        val destDir = intent?.getStringExtra(PickerModelImpl.KEY_PICKER_SELECTED_PATH)
                        fileListViewModel.copyTo(destDir)
                    }
                }

                CUT_PATH_REQUEST -> {
                    if (resultCode == RESULT_OK) {
                        val destDir = intent?.getStringExtra(PickerModelImpl.KEY_PICKER_SELECTED_PATH)
                        fileListViewModel.cutTo(destDir)
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, intent)
        }


        override fun onDestroy() {
            super.onDestroy()
            if (isAppManager(category)) {
                unregisterPackageReceiver(AceApplication.appContext)
            }
        }

        private fun navigateToSearchScreen() {
            Log.d(TAG, "navigateToSearchScreen:$this")
            mainViewModel.navigateToSearch.value = true
        }

        fun onMenuItemClick(item: MenuItem) {
            when (item.itemId) {
                R.id.action_view_list -> {
                    item.isChecked = true
                    fileListViewModel.switchView(ViewMode.LIST)
                }
                R.id.action_view_grid -> {
                    item.isChecked = true
                    fileListViewModel.switchView(ViewMode.GRID)
                }
                R.id.action_view_gallery -> {
                    item.isChecked = true
                    fileListViewModel.switchView(ViewMode.GALLERY)
                }
                R.id.action_hidden -> {
                    item.isChecked = !item.isChecked
                    fileListViewModel.onHiddenFileSettingChanged(item.isChecked)
                }
                R.id.action_sort -> {
                    fileListViewModel.onSortClicked()
                }
                R.id.action_search -> {
                    if (category != Category.APP_MANAGER) {
                        navigateToSearchScreen()
                    }
                }
                else -> {
                    if (item.itemId != R.id.action_view) {
                        fileListViewModel.onMenuItemClick(item.itemId)
                    }
                }
            }
        }

        override fun onUpEvent() {
            fileListViewModel.onUpTouchEvent()
        }

        override fun onMoveEvent() {
            fileListViewModel.onMoveTouchEvent()
        }

        override fun isDragNotStarted() = fileListViewModel.isDragNotStarted()

        override fun endActionMode() {
            fileListViewModel.endActionMode()
        }

        override fun getCategory() = fileListViewModel.category

        override fun onDragDropEvent(pos: Int, data: ArrayList<FileInfo>) {
            fileListViewModel.onDragDropEvent(pos)
        }

        override fun isDualModeEnabled() = fileListViewModel.isDualModeEnabled()

        override fun refreshList() {
            fileListViewModel.refreshList()
        }

        private val sortDialogListener = object : DialogHelper.SortDialogListener {
            override fun onPositiveButtonClick(sortMode: SortMode) {
                fileListViewModel.onSort(sortMode)
                reloadPane()
            }
        }

        private fun refreshDataOnTabSelected() {
            if (::filesList.isInitialized) {
                val viewMode = fileListViewModel.getViewMode(category)
                filesList.onViewModeChanged(viewMode)
                filesList.onSortModeChanged(mainViewModel.getSortMode())
            }
        }

        fun shouldShowHiddenFiles(): Boolean {
            if (::fileListViewModel.isInitialized) {
                return fileListViewModel.shouldShowHiddenFiles()
            }
            return false
        }

    fun onQueryTextChange(query: String?) {
        filesList.onQueryChanged(query)
    }

    fun createDualFragment() {
        val activity = activity as AppCompatActivity?
        activity?.let {
            val action = FileListFragmentDirections.actionFileListFragmentDualToDualPaneFragment2(StorageUtils.internalStorage, Category.FILES, true)
            val navController = activity.findNavController(R.id.nav_host_dual)
            val currentDestination = navController.currentDestination
            Log.d(TAG, "createDualFragment: navController:$navController, currentDest:$currentDestination")
            if (currentDestination?.id == R.id.fileListFragmentDual) {
                navController.navigate(action)
            }
        }
    }
}
