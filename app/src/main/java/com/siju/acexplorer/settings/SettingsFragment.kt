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

package com.siju.acexplorer.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.siju.acexplorer.R
import com.siju.acexplorer.extensions.inflateLayout
import kotlinx.android.synthetic.main.toolbar_settings.*


class SettingsFragment private constructor(): Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflateLayout(R.layout.pref_holder, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        PreferenceManager.setDefaultValues(context, R.xml.pref_settings, false)
        setupActionBar()

        getSupportFragmentManager()?.beginTransaction()
                ?.replace(R.id.frameSettings, SettingsPreferenceFragment())
                ?.commit()

        getSupportFragmentManager()?.addOnBackStackChangedListener {
            if (activity?.supportFragmentManager?.backStackEntryCount == 0) {
                removeToolbarAsUp()
                setToolbarTitle(getString(R.string.action_settings))
            }
        }
    }

    private fun setupActionBar() {
        toolbar.title = resources.getString(R.string.action_settings)
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
    }

    private fun setToolbarTitle(title: String) {
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.title = title
    }

    private fun removeToolbarAsUp() {
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.setHomeButtonEnabled(false)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


//    override fun onSupportNavigateUp(): Boolean {
//        return if (supportFragmentManager.popBackStackImmediate()) {
//            true
//        }
//        else super.onSupportNavigateUp()
//    }


    private fun getSupportFragmentManager() = activity?.supportFragmentManager

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            getSupportFragmentManager()?.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
