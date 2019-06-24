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
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.ads.AdsView
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.main.model.helper.ViewHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.storage.model.SortMode
import com.siju.acexplorer.storage.model.StorageModelImpl
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import com.siju.acexplorer.storage.viewmodel.FileListViewModelFactory
import com.siju.acexplorer.utils.InstallHelper
import kotlinx.android.synthetic.main.main_list.*
import kotlinx.android.synthetic.main.toolbar.*

const val KEY_PATH = "path"
const val KEY_CATEGORY = "category"
private const val TAG = "BaseFileListFragment"

open class BaseFileListFragment : Fragment() {

    private var hiddenMenuItem: MenuItem? = null
    private var path: String? = null
    private var category = Category.FILES
    private lateinit var filesList: FilesList
    private lateinit var floatingView: FloatingView
    private lateinit var navigationView: NavigationView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var fileListViewModel: FileListViewModel
    private lateinit var adView: AdsView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        getArgs()
        adView = AdsView(main_content)
        setupToolbar()
        setupViewModels()

        val view = view
        view?.let {
            filesList = FilesList(this, view, fileListViewModel.getViewMode())
            floatingView = FloatingView(view)
            navigationView = NavigationView(view, fileListViewModel.navigationCallback)
        }

        setupNavigationView()
        initObservers()
        Log.e(TAG, "onAct created:$hiddenMenuItem")
        setHiddenCheckedState(fileListViewModel.shouldShowHiddenFiles())
    }

    private fun setupToolbar() {
        toolbar.title = resources.getString(R.string.app_name)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun setupNavigationView() {
        fileListViewModel.setNavigationView(navigationView)
        navigationView.showNavigationView()
    }

    private fun getArgs() {
        val args = arguments
        args?.let {
            path = it.getString(KEY_PATH)
            category = it.getSerializable(KEY_CATEGORY) as Category
        }
    }

    private fun setupViewModels() {
        val activity = requireNotNull(activity)
        mainViewModel = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        val viewModelFactory = FileListViewModelFactory(StorageModelImpl(AceApplication.appContext))
        fileListViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(FileListViewModel::class.java)
    }

    private fun initObservers() {
        mainViewModel.permissionStatus.observe(viewLifecycleOwner, Observer { permissionStatus ->
            Log.e(TAG, "permissionStatus state:$permissionStatus")
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Granted -> fileListViewModel.loadData(path,
                                                                                          category)
                else                                        -> {
                }
            }
        })

        mainViewModel.premiumLiveData.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "Premium state:$it")
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
            floatingView.setTheme(it)
        })

        fileListViewModel.fileData.observe(viewLifecycleOwner, Observer {
            if (::filesList.isInitialized) {
                filesList.onDataLoaded(it)
            }
        })

        fileListViewModel.showFab.observe(viewLifecycleOwner, Observer { showFab ->
            if (showFab) {
                floatingView.showFab()
            }
            else {
                floatingView.hideFab()
            }
        })

        fileListViewModel.viewFileEvent.observe(viewLifecycleOwner, Observer {
            viewFile(it.first, it.second)
        })

        fileListViewModel.viewMode.observe(viewLifecycleOwner, Observer {
            if (::filesList.isInitialized) {
                filesList.onViewModeChanged(it)
            }
        })

        fileListViewModel.sortEvent.observe(viewLifecycleOwner, Observer { sortMode ->
            DialogHelper.showSortDialog(context, sortMode, sortDialogListener)
        })

        fileListViewModel.installAppEvent.observe(viewLifecycleOwner, Observer {
            val canInstall = it.first
            if (canInstall) {
                openInstallScreen(it.second)
            }
            else {
                InstallHelper.requestUnknownAppsInstallPermission(this)
            }
        })
    }

    private fun openInstallScreen(path: String?) {
        val context = context
        context?.let {
            InstallHelper.openInstallAppScreen(context,
                                               UriHelper.createContentUri(context.applicationContext,
                                                                          path))
        }
    }

    private fun showAds() {
        adView.showAds()
    }

    private fun hideAds() {
        adView.hideAds()
    }

    fun handleItemClick(fileInfo: FileInfo) {
        fileListViewModel.handleItemClick(fileInfo)
    }

    fun onBackPressed() = fileListViewModel.onBackPress()

    private fun viewFile(path: String, extension: String?) {
        Log.e(TAG, "Viewfile:path:$path, extension:$extension")
        val context = context
        context?.let {
            when (extension?.toLowerCase()) {
                null               -> {
                    val uri = UriHelper.createContentUri(context, path)
                    uri?.let {
                        DialogHelper.openWith(it, context)
                    }
                }
                ViewHelper.EXT_APK -> ViewHelper.viewApkFile(context, path,
                                                             fileListViewModel.apkDialogListener)
                else               -> ViewHelper.viewFile(context, path, extension)
            }
        }
    }


    //    fun onBackPressed(): Boolean {
//        return storagesUi!!.onBackPress()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        storagesUi!!.onResume()
//    }
//
//
//    override fun onPause() {
//        storagesUi!!.onPause()
//        super.onPause()
//    }
//
//
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            InstallHelper.UNKNOWN_APPS_INSTALL_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    openInstallScreen(fileListViewModel.apkPath)
                    fileListViewModel.apkPath = null
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }

    //
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filelist_base, menu)
        Log.e(TAG, "onCreateOptionsMenu")
        hiddenMenuItem = menu.findItem(R.id.action_hidden)
        if (::fileListViewModel.isInitialized) {
            setHiddenCheckedState(fileListViewModel.shouldShowHiddenFiles())
        }
    }

    private fun setHiddenCheckedState(state: Boolean) {
        hiddenMenuItem?.isChecked = state
    }

    //
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        storagesUi!!.onConfigChanged(newConfig)
//    }
//
//    override fun onDestroyView() {
//        storagesUi!!.onViewDestroyed()
//        super.onDestroyView()
//    }
//
//    override fun onDestroy() {
//        storagesUi!!.onExit()
//        super.onDestroy()
//    }
//
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_view_list -> {
                fileListViewModel.switchView(ViewMode.LIST)
                return true
            }
            R.id.action_view_grid -> {
                fileListViewModel.switchView(ViewMode.GRID)
                return true
            }
            R.id.action_hidden    -> {
                item.isChecked = !item.isChecked
                fileListViewModel.onHiddenFileSettingChanged(item.isChecked)
                return true
            }

            R.id.action_sort      -> {
                fileListViewModel.onSortClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val sortDialogListener = object : DialogHelper.SortDialogListener {
        override fun onPositiveButtonClick(sortMode: SortMode) {
            fileListViewModel.onSort(sortMode)
        }

        override fun onNegativeButtonClick(view: View?) {
        }

    }
//
//    fun performVoiceSearch(query: String) {
//        storagesUi!!.performVoiceSearch(query)
//    }
//
//    fun collapseFab() {
//        storagesUi!!.collapseFab()
//    }
//
//    fun reloadList(directory: String, category: Category) {
//        storagesUi!!.reloadList(directory, category)
//    }
//
//    fun refreshList() {
//        storagesUi!!.refreshList()
//    }
//
//    fun removeHomeFromNavPath() {
//        storagesUi!!.removeHomeFromNavPath()
//    }
//
//    fun addHomeNavPath() {
//        storagesUi!!.addHomeNavPath()
//    }
//
//    fun refreshSpan() {
//        storagesUi!!.refreshSpan()
//    }
//
//    fun showDualPane() {
//        storagesUi!!.showDualPane()
//    }
//
//    fun hideDualPane() {
//        storagesUi!!.hideDualPane()
//        val fragment = activity!!.supportFragmentManager.findFragmentById(R.id.frame_container_dual)
//        if (fragment != null) {
//            val fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
//            fragmentTransaction.remove(fragment).commitAllowingStateLoss()
//        }
//    }
//
//
//    fun setHidden(showHidden: Boolean) {
//        storagesUi!!.setHidden(showHidden)
//    }
//
//    fun switchView(viewMode: Int) {
//        storagesUi!!.switchView(viewMode)
//    }
//
//    fun collapseSearchView() {
//        storagesUi!!.collapseSearchView()
//    }
}
