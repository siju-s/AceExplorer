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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper.getCategoryName
import java.util.*

private const val TAG = "NavigationView"

@SuppressLint("InflateParams")
class NavigationView(view: View, private val navigationCallback: NavigationCallback) {

    private val context = view.context
    private val navDirectory: LinearLayout = view.findViewById(R.id.navButtonsContainer)
    private val scrollNavigation: HorizontalScrollView = view.findViewById(R.id.scrollNavigation)

    private val homeButton = LayoutInflater.from(context).inflate(R.layout.navigation_button,
                                                                  null) as ImageButton


    //    private void checkIfFavIsRootDir() {
    //
    //        if (!currentDir.contains(getInternalStorage()) && !externalSDPaths.contains
    //                (currentDir)) {
    //            isCurrentDirRoot = true;
    //            initialDir = File.separator;
    //        }
    //    }


    fun addHomeButton() {
        clearNavigation()
        setupHomeButton()
        addViewToNavigation(homeButton)
        addViewToNavigation(createNavigationArrow())
    }

    private fun setupHomeButton() {
        homeButton.setImageResource(R.drawable.ic_home_white_48)
        homeButton.setOnClickListener {
            Analytics.logger.navBarClicked(true)
            navigationCallback.onHomeClicked()
        }
    }

    fun addGenericTitle(category: Category) {
        addLibSpecificTitle(category, null)
    }

    fun addLibraryTitle(category: Category) {
        val button = createLibraryTitleButton(category)
        addViewToNavigation(button)
        button.setOnClickListener {
            navigationCallback.onNavButtonClicked(category, getCategoryName(context,
                                                                            category).uppercase(
                    Locale.getDefault()))
        }
    }

    private fun createLibraryTitleButton(
            category: Category): MaterialButton {
        val title = getCategoryName(context, category).uppercase(Locale.getDefault())
        val button = LayoutInflater.from(context).inflate(R.layout.material_button,
                                                          null) as MaterialButton
        button.text = title
        return button
    }

    private fun addLibSpecificTitle(category: Category, bucketName: String?) {
        val button = LayoutInflater.from(context).inflate(R.layout.material_button,
                                                          null) as MaterialButton
        val title = bucketName?.uppercase(Locale.getDefault()) ?: getCategoryName(context,
                                                                                    category).uppercase(
                Locale.getDefault())
        button.text = title

        addViewToNavigation(button)
        button.setOnClickListener {
            Logger.log(TAG, "nav button onclick--bucket=$bucketName category:$category")
            navigationCallback.onNavButtonClicked(category, bucketName)
        }
        scrollNavigation()
    }

    private fun navButtonOnClick(dir: String?) {
        Logger.log(TAG, "Dir=$dir currentDir=$dir")
        navigationCallback.onNavButtonClicked(dir)
    }

    private fun addViewToNavigation(view: View) {
        navDirectory.addView(view)
    }

    private fun createNavigationArrow() =
        LayoutInflater.from(context).inflate(R.layout.navigation_arrow,
                                             null) as MaterialButton


    private fun createNavigationButton() =
        LayoutInflater.from(context).inflate(R.layout.navigation_button,
                                                    null) as ImageButton

    private fun createNavigationTextButton() =
            LayoutInflater.from(context).inflate(R.layout.material_button,
                    null) as MaterialButton

    private fun clearNavigation() {
        navDirectory.removeAllViews()
    }

    fun showNavigationView() {
        scrollNavigation.visibility = View.VISIBLE
    }

    fun hideNavigationView() {
        scrollNavigation.visibility = View.GONE
    }

    private fun scrollNavigation() {
        scrollNavigation.postDelayed({
                                         scrollNavigation.fullScroll(
                                                 HorizontalScrollView.FOCUS_RIGHT)
                                         scrollNavigation.smoothScrollBy(100, 0)
                                     }, 100L)
    }

    fun createRootStorageButton(path: String) {
        val button = createNavigationButton()
        button.setImageResource(R.drawable.ic_root_white_48)
        setupCommonStorageAction(button, path)
    }

    fun createInternalStorageButton(path: String) {
        val button = createNavigationButton()
        button.setImageResource(R.drawable.ic_storage_white_48)
        setupCommonStorageAction(button, path)
    }

    fun createExternalStorageButton(path: String) {
        val button = createNavigationButton()
        button.setImageResource(R.drawable.ic_ext_sd_white_48)
        setupCommonStorageAction(button, path)
    }

    private fun setupCommonStorageAction(button: View, path: String) {
        button.setOnClickListener {
            navButtonOnClick(path)
        }
        addViewToNavigation(button)
    }

    fun createNavButtonStorageParts(path: String, dirName: String) {
        addViewToNavigation(createNavigationArrow())
        val button = createNavigationTextButton()
        button.text = dirName
        setupCommonStorageAction(button, path)
        scrollNavigation()
    }

    fun createLibraryTitleNavigation(category: Category, bucketName: String?) {
        addViewToNavigation(createNavigationArrow())
        addLibSpecificTitle(category, bucketName)
    }

}
