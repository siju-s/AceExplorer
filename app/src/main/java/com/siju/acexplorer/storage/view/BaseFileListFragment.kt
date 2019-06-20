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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.ads.AdsView
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.storage.model.StorageModelImpl
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import com.siju.acexplorer.storage.viewmodel.FileListViewModelFactory
import kotlinx.android.synthetic.main.main_list.*

const val KEY_PATH = "path"
const val KEY_CATEGORY = "category"
private const val TAG = "BaseFileListFragment"

open class BaseFileListFragment : Fragment() {

    private var path: String? = null
    private var category: Category? = null
    private lateinit var filesList: FilesList
    private lateinit var floatingView: FloatingView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var fileListViewModel: FileListViewModel
    private lateinit var adView : AdsView

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

        setupViewModels()
        val view = view
        view?.let {
            filesList = FilesList(this, view, fileListViewModel.getViewMode())
            floatingView = FloatingView(view)
        }
        fileListViewModel.setCategory(category)
        initObservers()
    }

    private fun getArgs() {
        val args = arguments
        args?.let {
            path = it.getString(KEY_PATH)
            category = it.getSerializable(KEY_CATEGORY) as Category?
        }
    }

    private fun setupViewModels() {
        val activity = requireNotNull(activity)
        mainViewModel = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        val viewModelFactory = FileListViewModelFactory(StorageModelImpl(AceApplication.appContext))
        fileListViewModel = ViewModelProviders.of(this, viewModelFactory).get(FileListViewModel::class.java)
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

        fileListViewModel.category.observe(viewLifecycleOwner, Observer { category ->
            if (CategoryHelper.checkIfLibraryCategory(category)) {
                floatingView.hideFab()
            }
            else {
                floatingView.showFab()
            }
        })
    }

    private fun showAds() {
        adView.showAds()
    }

    private fun hideAds() {
        adView.hideAds()
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
//    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
//        storagesUi!!.handleActivityResult(requestCode, resultCode, intent)
//        super.onActivityResult(requestCode, resultCode, intent)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        menu.clear()
//        super.onCreateOptionsMenu(menu, inflater)
//    }
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
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return super.onOptionsItemSelected(item)
//    }
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
