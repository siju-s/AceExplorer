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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.inflateLayout
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.modules.picker.model.PickerAction
import com.siju.acexplorer.storage.modules.picker.model.PickerModelImpl
import com.siju.acexplorer.storage.modules.picker.model.PickerResultAction
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.storage.modules.picker.viewmodel.PickerViewModel
import com.siju.acexplorer.storage.modules.picker.viewmodel.PickerViewModelFactory
import com.siju.acexplorer.storage.view.FileListAdapter
import com.siju.acexplorer.utils.ScrollInfo
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.*

private const val TAG = "PickerFragment"
private const val PERMISSIONS_REQUEST = 1
const val KEY_PICKER_TYPE = "picker_type"
const val RINGTONE_TYPE = "ringtone_type"
private const val DELAY_SCROLL_UPDATE_MS = 100L

@Suppress("UNNECESSARY_SAFE_CALL")
class PickerFragment private constructor(private val activity: AppCompatActivity) : DialogFragment() {

    private lateinit var fileList: FastScrollRecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var currentPathText: TextView
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var emptyText: TextView
    private lateinit var adapter: FileListAdapter
    private lateinit var viewModel: PickerViewModel

    //TODO DO this while adding scroll to File List
    private val scrollPosition = HashMap<String, Bundle>()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity, theme) {
            override fun onBackPressed() {
                viewModel.onBackPressed()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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

    private fun setupViewModels() {
        val viewModelFactory = PickerViewModelFactory(PickerModelImpl())
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(PickerViewModel::class.java)
        viewModel.setPermissionHelper(
                PermissionHelper(activity, AceApplication.appContext))
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
                { _,_,_ ->

                },
                null
        )
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
        viewModel.permissionStatus.observe(this, Observer { permissionStatus ->
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

        viewModel.fileData.observe(viewLifecycleOwner, Observer {
            onDataLoaded(it)
        })

        viewModel.storage.observe(viewLifecycleOwner, Observer {
            it?.apply {

            }
        })

        viewModel.pickerInfo.observe(viewLifecycleOwner, Observer {
            it?.apply {
                when (it.first) {
                    PickerType.RINGTONE -> {
                        setupRingtonePicker()
                    }
                    PickerType.FILE     -> {
                        setupFilePicker()
                    }
                    PickerType.NONE     -> {
                        setTitle(getString(R.string.dialog_title_browse))
                    }
                }
            }
        })

        viewModel.currentPath.observe(viewLifecycleOwner, Observer {
            it?.apply {
                onCurrentPathChanged(it)
            }
        })

        viewModel.result.observe(viewLifecycleOwner, Observer {
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

        viewModel.showEmptyText.observe(viewLifecycleOwner, Observer {
            if (it.second) {
                showEmptyText(it.first)
            }
            else {
                hideEmptyText()
            }
        })

        viewModel.directoryClicked.observe(viewLifecycleOwner, Observer {
            it?.apply {
                viewModel.saveScrollInfo(getScrollInfo())
            }
        })

        viewModel.scrollInfo.observe(viewLifecycleOwner, Observer {
            it?.apply {
                scrollToPosition(it)
            }
        })
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
                activity.setResult(RESULT_OK, pickerResultAction.data)
                activity.finish()
            }
            false -> {
                activity.setResult(RESULT_CANCELED, null)
                activity.finish()
            }
        }
    }

    private fun onFilePickerResult(pickerResultAction: PickerResultAction) {
        when (pickerResultAction.result) {
            true -> {
                activity.setResult(RESULT_OK, pickerResultAction.data)
                activity.finish()
            }
        }
    }

    private fun onOkButtonResult(pickerResultAction: PickerResultAction) {
        targetFragment?.onActivityResult(targetRequestCode, RESULT_OK, pickerResultAction.data)
        dialog?.dismiss()
    }

    private fun onCancelButtonResult(pickerResultAction: PickerResultAction) {
        activity.setResult(RESULT_CANCELED, null)
        when (pickerResultAction.data?.getSerializableExtra(KEY_PICKER_TYPE)) {
            PickerType.RINGTONE -> {
                activity.finish()
            }
            else                -> {
                dialog?.dismiss()
            }
        }
    }

    private fun setupRingtonePicker() {
        Analytics.getLogger().pickerShown(true)
        setTitle(getString(R.string.dialog_title_picker))
        okButton.visibility = View.GONE
    }

    private fun setupFilePicker() {
        Analytics.getLogger().pickerShown(false)
        setTitle(getString(R.string.dialog_title_browse))
    }


    private fun setTitle(title: String) {
        val actionBar = activity.supportActionBar
        actionBar?.title = title
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST -> viewModel.onPermissionResult(requestCode,
                                                                permissions,
                                                                grantResults)
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

    fun getScrollInfo(): ScrollInfo {
        val view = fileList.getChildAt(0)
        val offset = view?.top ?: 0

        val layoutManager = fileList.layoutManager as LinearLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()

        return ScrollInfo(position, offset)
    }

    fun scrollToPosition(scrollInfo: ScrollInfo) {
        fileList.postDelayed({
                                 val layoutManager = fileList.layoutManager as LinearLayoutManager
                                 layoutManager.scrollToPositionWithOffset(scrollInfo.position,
                                                                          scrollInfo.offset)
                             }
                             , DELAY_SCROLL_UPDATE_MS)
    }

//    private fun computeScroll() {
//        val vi = fileList.getChildAt(0)
//        val top = vi?.top ?: 0
//        val index = layoutManager!!.findFirstVisibleItemPosition()
//
//        val b = Bundle()
//        b.putInt(INDEX, index)
//        b.putInt(TOP, top)
//        scrollPosition[currentPath] = b
//    }


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

        fun newInstance(activity: AppCompatActivity, theme: Int, pickerType: PickerType,
                        ringtoneType: Int = -1): PickerFragment {
            val dialogFragment = PickerFragment(activity )
            dialogFragment.setStyle(STYLE_NORMAL, theme)
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
