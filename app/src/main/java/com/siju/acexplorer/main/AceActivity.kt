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

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.ActivityResult
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.base.view.BaseActivity
import com.siju.acexplorer.databinding.ActivityMainBinding
import com.siju.acexplorer.extensions.isLandscape
import com.siju.acexplorer.home.view.CategoryFragment
import com.siju.acexplorer.home.view.HomeScreenFragment
import com.siju.acexplorer.home.view.HomeScreenFragmentDirections
import com.siju.acexplorer.main.helper.REQUEST_CODE_UPDATE
import com.siju.acexplorer.main.helper.UpdateChecker
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.main.viewmodel.Pane
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.search.view.SearchFragment
import com.siju.acexplorer.settings.SettingsPreferenceFragment
import com.siju.acexplorer.storage.view.BaseFileListFragment
import com.siju.acexplorer.storage.view.DualPaneFragment
import com.siju.acexplorer.storage.view.FileListFragment
import com.siju.acexplorer.tools.ToolsFragment
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AceActivity"
private const val ACTION_IMAGES = "android.intent.action.SHORTCUT_IMAGES"
private const val ACTION_MUSIC = "android.intent.action.SHORTCUT_MUSIC"
private const val ACTION_VIDEOS = "android.intent.action.SHORTCUT_VIDEOS"
private const val ACTION_RECENT = "android.intent.action.SHORTCUT_RECENT"


@AndroidEntryPoint
class AceActivity : BaseActivity(), MainCommunicator, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var permissionHelper: PermissionHelper
    private lateinit var navController: NavController
    private lateinit var binding : ActivityMainBinding
    private lateinit var reviewManager : ReviewManager

    private var updateChecker: UpdateChecker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        reviewManager = ReviewManager(this)

        setContentView(binding.root)

        setupNavController()
        setupPermission()
        handleIntent(intent)
        initObservers()
        initListeners()
        updateChecker = UpdateChecker(applicationContext, this, updateCallback)
        checkIfInAppShortcut(intent)
    }

    private fun setupPermission() {
        permissionHelper = PermissionHelper(this, applicationContext)
        permissionHelper.checkPermissions()
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        if (intent.action == Intent.ACTION_GET_CONTENT) {
            hideTabs()
            Analytics.logger.filePickerShown()
            mainViewModel.onFilePicker(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
        }
    }

    private fun hideTabs() {
        binding.bottomNavigation.visibility = View.GONE
    }

    private fun setupNavController() {
        navController = findNavController(R.id.nav_host)
        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, dest, _ ->
            Log.d(TAG, "setupNavController: destAdded:$dest")
        }
    }

    override fun getUpdateChecker(): UpdateChecker? {
        return updateChecker
    }

    fun getViewModel() = mainViewModel

    private fun initListeners() {
        binding.bottomNavigation.setOnItemSelectedListener(navigationItemSelectedListener)
        binding.bottomNavigation.setOnItemReselectedListener(navigationItemReselectedListener)
    }

    private fun initObservers() {
        permissionHelper.permissionStatus.observe(this, { permissionStatus ->
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Required -> permissionHelper.requestPermission()
                is PermissionHelper.PermissionState.Rationale -> permissionHelper.showRationale()
                is PermissionHelper.PermissionState.Granted -> mainViewModel.onPermissionsGranted()
                else -> {}
            }
        })

        mainViewModel.dualMode.observe(this, {
            Log.d(TAG, "Dual mode value:$it")
            it?.apply {
                if (it) {
                    onDualModeEnabled(resources.configuration)
                } else {
                    disableDualPane()
                }
            }
        })

        mainViewModel.storageScreenReady.observe(this, {
            it?.apply {
                if (it) {
                    onDualModeEnabled(resources.configuration)
                }
            }
        })

        mainViewModel.homeClicked.observe(this, {
            it?.apply {
                if (it) {
                    disableDualPane()
                    mainViewModel.setHomeClickedFalse()
                }
            }
        })

        mainViewModel.navigateToSearch.observe(this, {
            it?.getContentIfNotHandled()?.apply {
                if (this) {
                    disableDualPane()
                    navController.navigate(R.id.searchFragment)
                }
            }
        })
    }

    private fun checkIfInAppShortcut(intent: Intent?) {
        Log.d(TAG, "checkIfInAppShortcut() called with: intent = ${intent?.action}")
        if (intent == null || intent.action == null || intent.action == Intent.ACTION_GET_CONTENT) {
            return
        }
        val category = when (intent.action) {
            ACTION_IMAGES -> Category.GENERIC_IMAGES
            ACTION_MUSIC ->  Category.GENERIC_MUSIC
            ACTION_VIDEOS -> Category.GENERIC_VIDEOS
            ACTION_RECENT -> Category.RECENT
            else          -> Category.GENERIC_IMAGES
        }
        if (mainViewModel.permissionStatus.value is PermissionHelper.PermissionState.Granted) {
            val action = HomeScreenFragmentDirections.actionNavigationHomeToCategoryFragment(null, category)
            navController.navigate(action)
        }
    }

    private fun onDualModeEnabled(configuration: Configuration?) {
        if (canEnableDualPane(configuration)) {
            enableDualPane()
            mainViewModel.refreshLayout(Pane.SINGLE)
        }
        mainViewModel.setStorageNotReady()
    }

    private fun enableDualPane() {
        Log.d(TAG, "enableDualPane")
        binding.contentMain.navHostDual.visibility = View.VISIBLE
        binding.contentMain.viewSeparator.visibility = View.VISIBLE
        createDualFragment()
    }

    private fun canEnableDualPane(configuration: Configuration?) = isCurrentScreenStorage() && configuration.isLandscape()

    private fun disableDualPane() {
        Log.d(TAG, "disableDualPane")
        //TODO 29 Feb 2020 Find why dual mode preference firing even  though value unchanged
        // Happens when opening a file and returning back
        if (binding.contentMain.navHostDual.visibility == View.GONE) {
            return
        }
        binding.contentMain.navHostDual.visibility = View.GONE
        binding.contentMain.viewSeparator.visibility = View.GONE
        if (isCurrentScreenStorage()) {
            mainViewModel.refreshLayout(Pane.SINGLE)
        }
    }

    private fun isCurrentScreenStorage(): Boolean {
        val fragment = getCurrentFragment()
        if (fragment is BaseFileListFragment) {
            return true
        }
        return false
    }

    private val navigationItemSelectedListener = NavigationBarView.OnItemSelectedListener { menuItem ->
        clearBackStack(menuItem.itemId)
        disableDualPane()
        Log.d(TAG, "navItemSelected: $menuItem")
        navController.navigate(menuItem.itemId)
        true
    }

    private val navigationItemReselectedListener = NavigationBarView.OnItemReselectedListener { menuItem ->
        clearBackStack(menuItem.itemId)
        disableDualPane()
        navController.navigate(menuItem.itemId)
    }

    private fun clearBackStack(id: Int, inclusive: Boolean = true) {
        navController.popBackStack(id, inclusive)
    }

    private fun createDualFragment() {
        Log.d(TAG, "createDualFragment: ")
        val fragment = getCurrentFragment()
        if (fragment is FileListFragment) {
            fragment.createDualFragment()
        }
    }

    override fun onNewIntent(intent: Intent) {
        Log.d(TAG, "onNewIntent() called with: intent = ${intent.action}")
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            val fragment = getCurrentFragment()
            if (fragment is SearchFragment) {
                fragment.performVoiceSearch(query)
            }
        }
        else if (intent.action == AppHelper.getUninstallAction()) {
            val extras = intent.extras
            val status = extras?.getInt(PackageInstaller.EXTRA_STATUS, 0)
            if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                val confirmIntent = extras.get(Intent.EXTRA_INTENT) as Intent
                startActivity(confirmIntent)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.onPermissionResult()
    }

    override fun onStart() {
        super.onStart()
        reviewManager.checkCanShowReviewDialog()
    }

    override fun onResume() {
        super.onResume()
        permissionHelper.onForeground()
        updateChecker?.onResume()
    }

    override fun onBackPressed() {
        val fragment = getCurrentFragment()
        Log.d(TAG, "onBackPressed: getCurrentFrag:$fragment")
        when (fragment) {
            is BaseFileListFragment -> when (val focusedFragment = getCurrentFocusFragment(fragment)) {
                is DualPaneFragment -> {
                    onDualPaneBackPress(focusedFragment)
                }
                is FileListFragment -> {
                    onSinglePaneBackPress(focusedFragment)
                }
            }
            is CategoryFragment -> {
                val backPressed = fragment.onBackPressed()
                if (backPressed) {
                    super.onBackPressed()
                }
            }
            is SearchFragment -> onSearchBackPress(fragment)
            is ToolsFragment -> {
                clearBackStack(R.id.navigation_tools)
                switchToHomeScreen()
            }
            is SettingsPreferenceFragment -> {
                clearBackStack(R.id.navigation_settings)
                switchToHomeScreen()
            }
            else -> super.onBackPressed()
        }
    }

    private fun switchToHomeScreen() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
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
        return if (mainViewModel.isDualPaneInFocus) {
            getDualFragment()
        } else {
            fragment
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat,
                                           pref: Preference): Boolean {
        navController.navigate(R.id.action_navigation_settings_to_aboutFragment)
        return true
    }

    private val updateCallback = object : UpdateChecker.UpdateCallback {
        override fun onUpdateDownloaded(appUpdateManager: AppUpdateManager) {
            showUpdateDownloadedSnackbar()
        }

        override fun onUpdateInstalled() {
            removeUpdateBadge()
            mainViewModel.onUpdateInstalled()
        }

        override fun onUpdateCancelledByUser() {
            showUpdateBadge()
        }

        override fun onUpdateSnackbarDismissed() {
            showUpdateBadge()
        }

        override fun onUpdateDownloading() {
            Toast.makeText(this@AceActivity, getString(R.string.update_downloading), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE) {
            when (resultCode) {
                Activity.RESULT_CANCELED -> {
                    mainViewModel.userCancelledUpdate()
                    showUpdateBadge()
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    showUpdateBadge()
                }
            }
        }
    }

    private fun showUpdateDownloadedSnackbar() {
        val fragment = getCurrentFragment()
        if (fragment is HomeScreenFragment) {
            fragment.showUpdateSnackbar(updateChecker)
        } else if (fragment is BaseFileListFragment) {
            fragment.showUpdateSnackbar(updateChecker)
        }
    }

    private fun showUpdateBadge() {
        binding.bottomNavigation.getOrCreateBadge(R.id.navigation_settings)
    }

    private fun removeUpdateBadge() {
        binding.bottomNavigation.removeBadge(R.id.navigation_settings)
    }

    private fun getCurrentFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    private fun getDualFragment() : Fragment? {
        val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.nav_host_dual) as NavHostFragment?
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged:${newConfig.isLandscape()}, dualMode:${mainViewModel.dualMode.value}")
        if (mainViewModel.dualMode.value == true && newConfig.isLandscape()) {
            onDualModeEnabled(newConfig)
        } else {
            disableDualPane()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateChecker?.onDestroy()
    }

}
