package com.siju.acexplorer.main.view

import com.siju.acexplorer.R
import com.siju.acexplorer.home.view.HomeScreenFragment
import com.siju.acexplorer.settings.SettingsPreferenceFragment

object FragmentsFactory {

    fun createFragment(itemId: Int) =
            when (itemId) {
                R.id.navigation_home -> HomeScreenFragment()
//            R.id.navigation_tools ->
                R.id.navigation_settings -> SettingsPreferenceFragment()
                else -> HomeScreenFragment()
            }
}
