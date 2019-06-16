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

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.Category

const val KEY_PATH = "path"
const val KEY_CATEGORY = "category"

open class BaseFileListFragment : Fragment() {


    val isFabExpanded: Boolean
        get() = storagesUi!!.isFabExpanded
    private var path : String? = null
    private var category: Category? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        getArgs()

    }

    private fun getArgs() {
        val args = arguments
        args?.let {
            path = it.getString(KEY_PATH)
            category = it.getSerializable(KEY_CATEGORY) as Category?
        }
    }

    fun onBackPressed(): Boolean {
        return storagesUi!!.onBackPress()
    }


    fun onPermissionGranted() {
        if (storagesUi != null) {
            storagesUi!!.refreshList()
        }
    }


    override fun onResume() {
        super.onResume()
        storagesUi!!.onResume()
    }


    override fun onPause() {
        storagesUi!!.onPause()
        super.onPause()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        storagesUi!!.handleActivityResult(requestCode, resultCode, intent)
        super.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        storagesUi!!.onConfigChanged(newConfig)
    }

    override fun onDestroyView() {
        storagesUi!!.onViewDestroyed()
        super.onDestroyView()
    }

    override fun onDestroy() {
        storagesUi!!.onExit()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    fun performVoiceSearch(query: String) {
        storagesUi!!.performVoiceSearch(query)
    }

    fun collapseFab() {
        storagesUi!!.collapseFab()
    }

    fun reloadList(directory: String, category: Category) {
        storagesUi!!.reloadList(directory, category)
    }

    fun refreshList() {
        storagesUi!!.refreshList()
    }

    fun removeHomeFromNavPath() {
        storagesUi!!.removeHomeFromNavPath()
    }

    fun addHomeNavPath() {
        storagesUi!!.addHomeNavPath()
    }

    fun refreshSpan() {
        storagesUi!!.refreshSpan()
    }

    fun showDualPane() {
        storagesUi!!.showDualPane()
    }

    fun hideDualPane() {
        storagesUi!!.hideDualPane()
        val fragment = activity!!.supportFragmentManager.findFragmentById(R.id.frame_container_dual)
        if (fragment != null) {
            val fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
            fragmentTransaction.remove(fragment).commitAllowingStateLoss()
        }
    }

    fun setPremium() {
        storagesUi!!.setPremium()
    }

    fun setHidden(showHidden: Boolean) {
        storagesUi!!.setHidden(showHidden)
    }

    fun switchView(viewMode: Int) {
        storagesUi!!.switchView(viewMode)
    }

    fun collapseSearchView() {
        storagesUi!!.collapseSearchView()
    }
}
