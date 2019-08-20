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

package com.siju.acexplorer.main

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kobakei.ratethisapp.RateThisApp
import com.siju.acexplorer.R
import com.siju.acexplorer.base.view.BaseActivity
import com.siju.acexplorer.extensions.isLandscape
import com.siju.acexplorer.helper.ToolbarHelper
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.view.FragmentsFactory
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.search.view.SearchFragment
import com.siju.acexplorer.storage.view.BaseFileListFragment
import com.siju.acexplorer.storage.view.DualPaneFragment
import com.siju.acexplorer.storage.view.FileListFragment
import com.siju.billingsecure.BillingKey
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "AceActivity"

class AceActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel.setPermissionHelper(PermissionHelper(this, applicationContext))

        initObservers()
        initListeners()
        bottom_navigation.selectedItemId = R.id.navigation_home
        Log.e(TAG, "billing key:${BillingKey.getBillingKey()}")
    }

    private fun initListeners() {
        bottom_navigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener)
        bottom_navigation.setOnNavigationItemReselectedListener(navigationItemReselectedListener)
    }

    private fun initObservers() {
        mainViewModel.permissionStatus.observe(this, Observer { permissionStatus ->
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Required -> mainViewModel.requestPermissions()
                is PermissionHelper.PermissionState.Rationale -> mainViewModel.showPermissionRationale()
                else -> {
                }
            }
        })

        mainViewModel.dualMode.observe(this, Observer {
            it?.apply {
                if (it) {
                    onDualModeEnabled(resources.configuration)
                } else {
                    disableDualPane()
                }
            }
        })

        mainViewModel.storageScreenReady.observe(this, Observer {
            it?.apply {
                if (it) {
                    onDualModeEnabled(resources.configuration)
                }
            }
        })

        mainViewModel.homeClicked.observe(this, Observer {
            it?.apply {
                if (it) {
                    disableDualPane()
                    mainViewModel.setHomeClickedFalse()
                }
            }
        })

        mainViewModel.navigateToSearch.observe(this, Observer {
            it?.apply {
                if (it) {
                    disableDualPane()
                    mainViewModel.setNavigatedToSearch()
                    openFragment(SearchFragment.newInstance(), true)
                }
            }
        })
    }

    private fun onDualModeEnabled(configuration: Configuration?) {
        if (canEnableDualPane(configuration)) {
            enableDualPane()
            mainViewModel.refreshData()
        }
        mainViewModel.setStorageNotReady()
    }

    private fun enableDualPane() {
        frame_container_dual.visibility = View.VISIBLE
        viewSeparator.visibility = View.VISIBLE
        createDualFragment()
    }

    private fun canEnableDualPane(configuration: Configuration?) = isCurrentScreenStorage() && configuration.isLandscape()

    private fun disableDualPane() {
        frame_container_dual.visibility = View.GONE
        viewSeparator.visibility = View.GONE
        if (isCurrentScreenStorage()) {
            mainViewModel.refreshData()
        }
    }

    private fun isCurrentScreenStorage(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.main_container)
        if (fragment is BaseFileListFragment) {
            return true
        }
        return false
    }

    private val navigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        val fragment = FragmentsFactory.createFragment(menuItem.itemId)
        openFragment(fragment)
        true
    }

    private val navigationItemReselectedListener = BottomNavigationView.OnNavigationItemReselectedListener { menuItem ->
        val fragment = FragmentsFactory.createFragment(menuItem.itemId)
        openFragment(fragment)
    }


    private fun openFragment(fragment: Fragment, addToBackStack : Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    private fun createDualFragment() {
        val fragment = DualPaneFragment.newInstance(StorageUtils.internalStorage, Category.FILES)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container_dual, fragment)
        transaction.commit()
    }

//    override fun onNewIntent(intent: Intent) {
////        mainUi.onIntentReceived(intent)
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        mainViewModel.onPermissionResult(requestCode, permissions, grantResults)
    }


    override fun onStart() {
        super.onStart()
        // Monitor launch times and interval from installation
        RateThisApp.onCreate(this)
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.onResume()
    }

    //    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
//        if (!mainUi.handleActivityResult(requestCode, resultCode, intent)) {
//            super.onActivityResult(requestCode, resultCode, intent)
//        }
//    }
//
//
    override fun onBackPressed() {
        when (val fragment = supportFragmentManager.findFragmentById(R.id.main_container)) {
            is BaseFileListFragment -> when (val focusedFragment = getCurrentFocusFragment(fragment)) {
                is DualPaneFragment -> {
                    onDualPaneBackPress(focusedFragment)
                }
                is FileListFragment -> {
                    onSinglePaneBackPress(focusedFragment)
                }
            }
            is SearchFragment -> onSearchBackPress(fragment)
            else -> super.onBackPressed()
        }
    }

    private fun onDualPaneBackPress(focusedFragment: DualPaneFragment) {
        val backPressNotHandled = focusedFragment.onBackPressed()
        if (backPressNotHandled) {
            super.onBackPressed()
            disableDualPane()
        }
    }

    private fun onSinglePaneBackPress(focusedFragment: FileListFragment) {
        val backPressNotHandled = focusedFragment.onBackPressed()
        if (backPressNotHandled) {
            super.onBackPressed()
            disableDualPane()
        }
    }

    private fun onSearchBackPress(fragment: SearchFragment) {
        val backPressNotHandled = fragment.onBackPressed()
        if (backPressNotHandled) {
            super.onBackPressed()
        }
    }

    private fun getCurrentFocusFragment(fragment: BaseFileListFragment): Fragment? {
        if (mainViewModel.isDualPaneInFocus) {
            return supportFragmentManager.findFragmentById(R.id.frame_container_dual)
        } else {
            return fragment
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat,
                                           pref: Preference): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                ClassLoader.getSystemClassLoader(),
                pref.fragment)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        replaceFragment(supportFragmentManager, fragment)
        ToolbarHelper.setToolbarTitle(this, pref.title.toString())
        ToolbarHelper.showToolbarAsUp(this)
        return true
    }

    private fun replaceFragment(fragmentManager: FragmentManager,
                                fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mainViewModel.dualMode.value == true && newConfig.isLandscape()) {
            onDualModeEnabled(newConfig)
        } else {
            disableDualPane()
        }
    }
//
//    override fun onDestroy() {
//        mainUi.onExit()
//        super.onDestroy()
//    }
//
//
//    override fun onRestart() {
//        super.onRestart()
//        mainUi.checkForPreferenceChanges()
//    }
//
//    override fun onCreateContextMenu(menu: ContextMenu, v: View,
//                                     menuInfo: ContextMenu.ContextMenuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo)
//        mainUi.onCreateContextMenu(menu, v, menuInfo)
//    }
//
//    override fun onContextItemSelected(item: MenuItem): Boolean {
//        mainUi.onContextItemSelected(item)
//        return super.onContextItemSelected(item)
//    }
//
//    fun showDualFrame() {
//        mainUi.showDualFrame()
//    }
//
//    fun setDualPaneFocusState(isDualPaneInFocus: Boolean) {
//        mainUi.setDualPaneFocusState(isDualPaneInFocus)
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        configuration = newConfig
//        mainUi.onConfigChanged(newConfig)
//    }
//
//    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
//        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
//        mainUi.onMultiWindowChanged(isInMultiWindowMode, newConfig)
//    }
//
//    fun getConfiguration(): Configuration {
//        return configuration ?: resources.configuration
//    }
//
//    fun switchView(viewMode: Int, isDual: Boolean) {
//        configuration = getConfiguration()
//        if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mainUi.switchView(viewMode, isDual)
//        }
//    }
//
//    fun refreshList(isDual: Boolean) {
//        configuration = getConfiguration()
//        if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mainUi.refreshList(isDual)
//        }
//    }
//
//    fun onSearchClicked() {
//        mainUi.onSearchClicked()
//    }


}
