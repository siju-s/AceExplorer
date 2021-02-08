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

package com.siju.acexplorer.storage.modules.picker.view

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.inflateLayout
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.modules.picker.model.PickerAction
import com.siju.acexplorer.storage.modules.picker.model.PickerResultAction
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.storage.modules.picker.viewmodel.PickerViewModel
import com.siju.acexplorer.storage.view.FileListAdapter
import com.siju.acexplorer.utils.ScrollInfo
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val TAG = "PickerFragment"
private const val PERMISSIONS_REQUEST = 1
const val KEY_PICKER_TYPE = "picker_type"
const val RINGTONE_TYPE = "ringtone_type"
private const val DELAY_SCROLL_UPDATE_MS = 100L

@AndroidEntryPoint
class PickerFragment : DialogFragment() {

    private val viewModel: PickerViewModel by viewModels()

    private lateinit var fileList: FastScrollRecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var currentPathText: TextView
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var emptyText: TextView
    private lateinit var adapter: FileListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        initBackPressListener(dialog)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflateLayout(R.layout.dialog_browse, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(false)
        setupViewModels()
        view?.let {
            setupUI(it)
        }
        initObservers()
        getArgs()
        loadData()
    }

    private fun initBackPressListener(dialog: Dialog) {
        dialog.setOnKeyListener { _, keyCode, event ->
            return@setOnKeyListener if ((keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP)) {
                viewModel.onBackPressed()
                true
            } else false
        }
    }

    private fun setupViewModels() {
        activity?.let {
            viewModel.setPermissionHelper(
                    PermissionHelper(it as AppCompatActivity, AceApplication.appContext))
        }
    }

    private fun setupUI(view: View) {
        setupToolbar(view)
        initializeViews(view)
        setupList()
        initListeners()
    }

    private fun setupToolbar(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
    }

    private fun initializeViews(view: View) {
        fileList = view.findViewById(R.id.recyclerViewFileList)
        emptyText = view.findViewById(R.id.textEmpty)
        backButton = view.findViewById(R.id.imageButtonBack)
        currentPathText = view.findViewById(R.id.textPath)
        okButton = view.findViewById(R.id.buttonPositive)
        cancelButton = view.findViewById(R.id.buttonNegative)
        okButton.text = getString(R.string.msg_ok)
        cancelButton.text = getString(R.string.dialog_cancel)
    }


    private fun setupList() {
        fileList.layoutManager = LinearLayoutManager(context)
        adapter = FileListAdapter(
                ViewMode.LIST,
                {
                    viewModel.handleItemClick(it.first)
                },
                { _, _, _ ->

                },
                null
        )
        adapter.setMainCategory(Category.FILES)
        fileList.adapter = adapter
    }

    private fun initListeners() {
        okButton.setOnClickListener {
            viewModel.okButtonClicked()
        }

        cancelButton.setOnClickListener { viewModel.onCancelButtonClicked() }


        backButton.setOnClickListener {
            viewModel.onNavigationBackButtonPressed()
        }

        dialog?.setOnCancelListener {
            viewModel.onCancelButtonClicked()
        }
    }

    private fun initObservers() {
        viewModel.permissionStatus.observe(this, { permissionStatus ->
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Required  -> viewModel.requestPermissions()
                is PermissionHelper.PermissionState.Rationale -> viewModel.showPermissionRationale()
                is PermissionHelper.PermissionState.Granted   -> {
                    onCurrentPathChanged(viewModel.currentPath.value)
                }
                else                                          -> {
                }
            }
        })

        viewModel.fileData.observe(viewLifecycleOwner, {
            onDataLoaded(it)
        })

        viewModel.storage.observe(viewLifecycleOwner, {
            it?.apply {
                val pickerType = viewModel.pickerInfo.value?.first
                if (pickerType == PickerType.COPY || pickerType == PickerType.CUT) {
                    onDataLoaded(it)
                }
            }
        })

        viewModel.pickerInfo.observe(viewLifecycleOwner, {
            it?.apply {
                when (it.first) {
                    PickerType.RINGTONE -> {
                        setupRingtonePicker()
                    }
                    PickerType.FILE -> {
                        setupFilePicker()
                    }
                    PickerType.GET_CONTENT -> {
                        setupContentPicker()
                    }
                    PickerType.NONE -> {
                        setTitle(getString(R.string.dialog_title_browse))
                    }
                    PickerType.COPY -> {
                        okButton.text = getString(R.string.action_copy)
                        toolbar.title = getString(R.string.dialog_title_browse)
                        onStorageListScreen()
                    }
                    PickerType.CUT -> {
                        okButton.text = getString(R.string.action_cut)
                        toolbar.title = getString(R.string.dialog_title_browse)
                        onStorageListScreen()
                    }
                }
            }
        })

        viewModel.currentPath.observe(viewLifecycleOwner, {
            it?.apply {
                onCurrentPathChanged(it)
            }
        })

        viewModel.result.observe(viewLifecycleOwner, {
            it?.apply {
                when (it.pickerAction) {
                    PickerAction.RINGTONE_PICK -> {
                        onRingtonePickerResult(it)
                    }
                    PickerAction.FILE_PICK     -> {
                        onFilePickerResult(it)
                    }

                    PickerAction.OK            -> {
                        onOkButtonResult(it)
                    }

                    PickerAction.CANCEL        -> {
                        onCancelButtonResult(it)
                    }
                }
            }
        })

        viewModel.showEmptyText.observe(viewLifecycleOwner, {
            if (it.second) {
                showEmptyText(it.first)
            } else {
                hideEmptyText()
            }
        })

        viewModel.directoryClicked.observe(viewLifecycleOwner, {
            it?.apply {
                viewModel.saveScrollInfo(getScrollInfo())
            }
        })

        viewModel.scrollInfo.observe(viewLifecycleOwner, {
            it?.apply {
                scrollToPosition(it)
            }
        })

        viewModel.isRootStorageList.observe(viewLifecycleOwner, {
            it?.apply {
                if (it) {
                    onStorageListScreen()
                }
                else {
                    onItemClicked()
                }
            }
        })
    }

    private fun disableButton(view : View) {
        view.isEnabled = false
        view.alpha = 0.5f
    }

    private fun enableButton(view : View) {
        view.isEnabled = true
        view.alpha = 1f
    }

    private fun onItemClicked() {
        enableButton(okButton)
        enableButton(backButton)
    }

    private fun onStorageListScreen() {
        disableButton(okButton)
        disableButton(backButton)
    }

    private fun onCurrentPathChanged(path: String?) {
        Log.d(TAG, "onCurrentPathChanged:$path")
        path?.let {
            currentPathText.text = path
            refreshList(path)
        }
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            viewModel.setArgs(bundle as Any)
        }
    }

    private fun loadData() {
        viewModel.fetchStorageList()
    }

    private fun onRingtonePickerResult(pickerResultAction: PickerResultAction) {
        when (pickerResultAction.result) {
            true  -> {
                activity?.setResult(RESULT_OK, pickerResultAction.data)
                activity?.finish()
            }
            false -> {
                activity?.setResult(RESULT_CANCELED, null)
                activity?.finish()
            }
        }
    }

    private fun onFilePickerResult(pickerResultAction: PickerResultAction) {
        when (pickerResultAction.result) {
            true -> {
                activity?.setResult(RESULT_OK, pickerResultAction.data)
                activity?.finish()
            }
        }
    }

    private fun onOkButtonResult(pickerResultAction: PickerResultAction) {
        targetFragment?.onActivityResult(targetRequestCode, RESULT_OK, pickerResultAction.data)
        dialog?.dismiss()
    }

    private fun onCancelButtonResult(pickerResultAction: PickerResultAction) {
        activity?.setResult(RESULT_CANCELED, null)
        when (pickerResultAction.data?.getSerializableExtra(KEY_PICKER_TYPE)) {
            PickerType.RINGTONE -> {
                activity?.finish()
            }
            else                -> {
                dialog?.dismiss()
            }
        }
    }

    private fun setupRingtonePicker() {
        Analytics.logger.pickerShown(true)
        setTitle(getString(R.string.dialog_title_picker))
        okButton.visibility = View.GONE
    }

    private fun setupFilePicker() {
        Analytics.logger.pickerShown(false)
        setTitle(getString(R.string.dialog_title_browse))
    }

    private fun setupContentPicker() {
        Analytics.logger.pickerShown(false)
        setTitle(getString(R.string.dialog_title_browse))
        okButton.visibility = View.GONE
        cancelButton.visibility = View.GONE
    }

    private fun setTitle(title: String) {
        val actionBar = activity?.actionBar
        actionBar?.title = title
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST -> viewModel.onPermissionResult()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun refreshList(path: String?) {
        Log.d(TAG, "refreshList: ")
        viewModel.loadData(path)
    }

    private fun getScrollInfo(): ScrollInfo {
        val view = fileList.getChildAt(0)
        val offset = view?.top ?: 0

        val layoutManager = fileList.layoutManager as LinearLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()

        return ScrollInfo(position, offset)
    }

    private fun scrollToPosition(scrollInfo: ScrollInfo) {
        fileList.postDelayed({
            val layoutManager = fileList.layoutManager as LinearLayoutManager
            layoutManager.scrollToPositionWithOffset(scrollInfo.position,
                    scrollInfo.offset)
        }
                , DELAY_SCROLL_UPDATE_MS)
    }

    override fun onDestroyView() {
        activity?.setResult(AppCompatActivity.RESULT_OK, null)
        super.onDestroyView()
    }

    private fun onDataLoaded(data: ArrayList<FileInfo>) {
        adapter.submitList(data)
    }

    private fun showEmptyText(pickerType: PickerType) {
        if (pickerType == PickerType.RINGTONE) {
            emptyText.text = getString(R.string.no_music)
        }
        else {
            emptyText.text = getString(R.string.no_files)
        }
        emptyText.visibility = View.VISIBLE
    }

    private fun hideEmptyText() {
        emptyText.visibility = View.GONE
    }

    companion object {

        fun newInstance(pickerType: PickerType, ringtoneType: Int = -1): PickerFragment {
            val dialogFragment = PickerFragment()
            dialogFragment.setStyle(STYLE_NORMAL, R.style.BaseDeviceTheme)
            val args = Bundle()
            with(args) {
                putSerializable(KEY_PICKER_TYPE, pickerType)
                putInt(RINGTONE_TYPE, ringtoneType)
            }
            dialogFragment.arguments = args
            return dialogFragment
        }
    }
}
