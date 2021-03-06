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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.main.model.helper.ViewHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper.openWith
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback
import com.siju.acexplorer.storage.modules.zipviewer.model.ZipViewerModelImpl
import com.siju.acexplorer.storage.modules.zipviewer.viewmodel.ZipViewerViewModel
import com.siju.acexplorer.storage.modules.zipviewer.viewmodel.ZipViewerViewModelFactory
import com.siju.acexplorer.utils.InstallHelper

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
        viewModel = ViewModelProvider(fragment, viewModelFactory)
                .get(ZipViewerViewModel::class.java)
    }

    override fun loadData() {
        viewModel.loadData(null, parentZipPath)
    }

    private fun initObservers() {
        viewModel.viewFileEvent.observe(fragment.viewLifecycleOwner, {
            it?.apply {
                viewFile(it.first, it.second)
                viewModel.endViewFileEvent()
            }
        })

        viewModel.installAppEvent.observe(fragment.viewLifecycleOwner, {
            val canInstall = it.first
            if (canInstall) {
                openInstallScreen(it.second)
            }
        })
        viewModel.zipFailEvent.observe(fragment.viewLifecycleOwner, {
            if (it) {
                val context = fragment.context
                context?.let {
                    Toast.makeText(context, context.getString(R.string.zip_open_error), Toast.LENGTH_SHORT).show()
                    viewModel.setZipFailEvent(false)
                }
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

    override fun navigateTo(dir: String?) {
        viewModel.checkZipMode(dir)
    }

    private fun viewFile(path: String, extension: String?) {
        Log.d(TAG, "Viewfile:path:$path, extension:$extension")
        val context = fragment.context
        context?.let {
            when (extension?.lowercase()) {
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
