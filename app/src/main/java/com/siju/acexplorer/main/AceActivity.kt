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

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kobakei.ratethisapp.RateThisApp
import com.siju.acexplorer.R
import com.siju.acexplorer.base.view.BaseActivity
import com.siju.acexplorer.main.view.FragmentsFactory
import com.siju.acexplorer.main.view.MainUi
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class AceActivity : BaseActivity() {

    private lateinit var mainUi: MainUi
    private lateinit var mainViewModel: MainViewModel

    private var configuration: Configuration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel.setPermissionHelper(PermissionHelper( this, applicationContext))

        initObservers()
        initListeners()
        bottom_navigation.selectedItemId = R.id.navigation_home
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


    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onNewIntent(intent: Intent) {
        mainUi.onIntentReceived(intent)
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (!mainUi.handleActivityResult(requestCode, resultCode, intent)) {
            super.onActivityResult(requestCode, resultCode, intent)
        }
    }


    override fun onBackPressed() {
        if (mainUi.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mainUi.onExit()
        super.onDestroy()
    }


    override fun onRestart() {
        super.onRestart()
        mainUi.checkForPreferenceChanges()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        mainUi.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        mainUi.onContextItemSelected(item)
        return super.onContextItemSelected(item)
    }

    fun showDualFrame() {
        mainUi.showDualFrame()
    }

    fun setDualPaneFocusState(isDualPaneInFocus: Boolean) {
        mainUi.setDualPaneFocusState(isDualPaneInFocus)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configuration = newConfig
        mainUi.onConfigChanged(newConfig)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        mainUi.onMultiWindowChanged(isInMultiWindowMode, newConfig)
    }

    fun getConfiguration(): Configuration {
        return configuration ?: resources.configuration
    }

    fun switchView(viewMode: Int, isDual: Boolean) {
        configuration = getConfiguration()
        if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainUi.switchView(viewMode, isDual)
        }
    }

    fun refreshList(isDual: Boolean) {
        configuration = getConfiguration()
        if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainUi.refreshList(isDual)
        }
    }

    fun onSearchClicked() {
        mainUi.onSearchClicked()
    }


}
