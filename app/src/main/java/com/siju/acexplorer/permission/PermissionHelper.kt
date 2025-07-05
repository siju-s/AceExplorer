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

package com.siju.acexplorer.permission

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.R
import com.siju.acexplorer.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.siju.acexplorer.common.R as RC

private const val TAG = "PermissionHelper"
private const val SCHEMA_PACKAGE = "package"

class PermissionHelper @Inject constructor(
    private val activity: FragmentActivity,
    @ApplicationContext val context: Context
) {
    private var permissionRationaleDialog: Dialog? = null

    val permissionStatus: MutableLiveData<PermissionState> = MutableLiveData()

    fun checkPermissions() {
        if (hasAllFilesPermissions()) {
            permissionStatus.value = PermissionState.Granted
        } else {
            permissionStatus.value = PermissionState.Required
        }
        Log.d(TAG, "checkPermissions: permissionstatus:${permissionStatus.value}")
    }

    fun onForeground() {
        if (Environment.isExternalStorageManager()) {
            dismissRationaleDialog()
            permissionStatus.value = PermissionState.Granted
        } else if (permissionRationaleDialog?.isShowing == true && hasAllFilesPermissions()
        ) {
            dismissRationaleDialog()
            permissionStatus.value = PermissionState.Granted
        }
    }

    private fun hasAllFilesPermissions(): Boolean {
        return Environment.isExternalStorageManager()
    }

    fun requestPermission() {
        showRationale()
    }

    fun onPermissionResult() {
        Log.d(TAG, "onPermissionResult")
        if (hasAllFilesPermissions()) {
            Logger.log(TAG, "Permission granted")
            permissionStatus.value = PermissionState.Granted
            dismissRationaleDialog()
        } else {
            permissionStatus.value = PermissionState.Rationale
        }
    }

    fun showRationale() {
        if (permissionRationaleDialog == null) {
            createRationaleDialog()
        }
        val showSettings = false

        val buttonGrant: Button? = permissionRationaleDialog?.findViewById(R.id.buttonGrant)
        val textViewPermissionHint: TextView? = permissionRationaleDialog?.findViewById(
            R.id.textPermissionHint
        )

        permissionRationaleDialog?.setOnDismissListener {
            onRationaleDialogDismissed()
        }

        if (showSettings) {
            buttonGrant?.setText(RC.string.action_settings)
            textViewPermissionHint?.visibility = View.VISIBLE
        }

        buttonGrant?.setOnClickListener {
            when {
                showSettings -> {
                    openSettings()
                }

                !hasAllFilesPermissions() -> {
                    requestAllFilesPermission()
                }
            }
        }

        permissionRationaleDialog?.show()
    }

    private fun requestAllFilesPermission() {
        val uri = Uri.fromParts(SCHEMA_PACKAGE, context.packageName, null)
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = uri
        activity.startActivity(intent)
    }

    private fun onRationaleDialogDismissed() {
        if (!hasAllFilesPermissions()) {
            activity.finish()
        }
    }

    private fun createRationaleDialog() {
        permissionRationaleDialog = Dialog(activity, R.style.PermissionDialog)
        permissionRationaleDialog?.setContentView(R.layout.dialog_runtime_permissions)
    }

    private fun openSettings() {
        val uri = Uri.fromParts(SCHEMA_PACKAGE, context.packageName, null)
        val intent = Intent()
        intent.apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = uri
        }
        activity.startActivity(intent)
    }

    private fun dismissRationaleDialog() {
        permissionRationaleDialog?.dismiss()
    }

    sealed class PermissionState {
        data object Granted : PermissionState()
        data object Required : PermissionState()
        data object Rationale : PermissionState()
    }
}