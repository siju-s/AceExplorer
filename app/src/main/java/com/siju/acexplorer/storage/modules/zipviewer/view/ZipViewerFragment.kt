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

package com.siju.acexplorer.storage.modules.zipviewer.view


import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.main.model.helper.ViewHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper.openWith
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback
import com.siju.acexplorer.storage.modules.zipviewer.model.ZipViewerModelImpl
import com.siju.acexplorer.storage.modules.zipviewer.viewmodel.ZipViewerViewModel
import com.siju.acexplorer.storage.modules.zipviewer.viewmodel.ZipViewerViewModelFactory
import com.siju.acexplorer.utils.InstallHelper
import java.util.*

private const val TAG = "ZipViewerFragment"

class ZipViewerFragment(
        private val fragment: Fragment,
        private val parentZipPath: String,
        private val zipViewerCallback: ZipViewerCallback) : ZipViewer
{

    private lateinit var viewModel: ZipViewerViewModel

    init {
        setupViewModel()
        initObservers()
        viewModel.populateTotalZipList(parentZipPath)
    }

    private fun setupViewModel() {
        val viewModelFactory = ZipViewerViewModelFactory(
                ZipViewerModelImpl(AceApplication.appContext), zipViewerCallback)
        viewModel = ViewModelProviders.of(fragment, viewModelFactory)
                .get(ZipViewerViewModel::class.java)
    }

    override fun loadData() {
        viewModel.loadData(null, parentZipPath)
    }

    private fun initObservers() {
        viewModel.viewFileEvent.observe(fragment.viewLifecycleOwner, Observer {
            viewFile(it.first, it.second)
        })

        viewModel.installAppEvent.observe(fragment.viewLifecycleOwner, Observer {
            val canInstall = it.first
            if (canInstall) {
                openInstallScreen(it.second)
            }
            else {
                InstallHelper.requestUnknownAppsInstallPermission(fragment)
            }
        })
    }

    private fun openInstallScreen(path: String?) {
        val context = fragment.context
        context?.let {
            InstallHelper.openInstallAppScreen(context,
                                               UriHelper.createContentUri(
                                                       context.applicationContext,
                                                       path))
        }
    }

    override fun onDirectoryClicked(position: Int) {
        viewModel.onDirectoryClicked(position)
    }

    override fun onFileClicked(position: Int) {
        viewModel.onFileClicked(position)
    }

    override fun onBackPress() {
        viewModel.onBackPressed()
    }

    override fun endZipMode(dir: String?) {
        viewModel.endZipMode(dir)
    }

    //TODO 28-Jul-2019 Handle this when navigation button click implemented
    fun navigateTo(dir: String?) {
        viewModel.checkZipMode(dir)
    }

    private fun viewFile(path: String, extension: String?) {
        Log.e(TAG, "Viewfile:path:$path, extension:$extension")
        val context = fragment.context
        context?.let {
            when (extension?.toLowerCase(Locale.ROOT)) {
                null               -> {
                    val uri = UriHelper.createContentUri(context, path)
                    uri?.let {
                        openWith(it, context)
                    }
                }
                ViewHelper.EXT_APK -> ViewHelper.viewApkFile(context, path,
                                                             viewModel.apkDialogListener)
                else               -> ViewHelper.viewFile(context, path, extension)
            }
        }
    }
}
