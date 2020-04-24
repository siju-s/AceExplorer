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

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.R
import com.siju.acexplorer.logging.Logger


private const val TAG = "PermissionHelper"
private const val PHONE_SETTINGS_REQUEST = 2000
private const val SCHEMA_PACKAGE = "package"
private const val PERMISSIONS_REQUEST = 1000
private const val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

class PermissionHelper(private val activity: AppCompatActivity, private val context: Context) {
    private var permissionRationaleDialog: Dialog? = null

    val permissionStatus: MutableLiveData<PermissionState> = MutableLiveData()
    private val permissionsNeeded = ArrayList<String>()

    fun checkPermissions() {
        if (hasPermissions(context)) {
            permissionStatus.value = PermissionState.Granted
        }
        else {
            permissionStatus.value = PermissionState.Required
        }
    }

    fun onForeground() {
        if (permissionRationaleDialog?.isShowing == true) {
            Log.d(TAG, "onForeground")
            if (hasPermissions(context)) {
                dismissRationaleDialog()
                permissionStatus.value = PermissionState.Granted
            }
        }
    }

    private fun hasPermissions(context: Context): Boolean {
        val packageInfo: PackageInfo
        try {
            packageInfo = context.packageManager.getPackageInfo(context.packageName,
                                                                PackageManager.GET_PERMISSIONS)
        }
        catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        }
        val dangerousPermissions = getDangerousPermissions(context, packageInfo)

        permissionsNeeded.clear()
        for (permission in dangerousPermissions) {
            if (isPermissionDenied(context, permission)) {
                permissionsNeeded.add(permission)
            }
        }
        return permissionsNeeded.isEmpty()
    }

    private fun isPermissionDenied(context: Context, permission: String) =
            ContextCompat.checkSelfPermission(context,
                                              permission) == PackageManager.PERMISSION_DENIED


    private fun getDangerousPermissions(context: Context,
                                        packageInfo: PackageInfo): ArrayList<String> {
        val dangerousPermissions = arrayListOf<String>()

        for (permission in packageInfo.requestedPermissions) {
            val permissionInfo: PermissionInfo
            try {
                permissionInfo = context.packageManager.getPermissionInfo(permission, 0)
            }
            catch (exception: PackageManager.NameNotFoundException) {
                continue
            }
            when (getPermissionProtectionLevel(permissionInfo)) {
                PermissionInfo.PROTECTION_DANGEROUS ->
                    dangerousPermissions.add(permission)

            }
        }
        return dangerousPermissions
    }

    @Suppress("DEPRECATION")
    private fun getPermissionProtectionLevel(permissionInfo: PermissionInfo): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissionInfo.protection
        }
        else {
            return permissionInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE
        }
    }


    fun requestPermission() {
        Logger.log(TAG, "requestPermission")
        ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray(),
                                          PERMISSIONS_REQUEST)
    }

    fun onPermissionResult() {
        Log.d(TAG, "onPermissionResult")
        if (hasPermissions(context)) {
            Logger.log(TAG, "Permission granted")
            permissionStatus.value = PermissionState.Granted
            dismissRationaleDialog()
        }
        else {
            permissionStatus.value = PermissionState.Rationale
        }
    }

    fun showRationale() {
        if (permissionRationaleDialog == null) {
            createRationaleDialog()
        }
        val showSettings = !ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                                                                storagePermission)
        val buttonGrant: Button? = permissionRationaleDialog?.findViewById(R.id.buttonGrant)
        val textViewPermissionHint: TextView? = permissionRationaleDialog?.findViewById(
                R.id.textPermissionHint)

        permissionRationaleDialog?.setOnDismissListener {
            onRationaleDialogDismissed()
        }

        if (showSettings) {
            buttonGrant?.setText(R.string.action_settings)
            textViewPermissionHint?.visibility = View.VISIBLE
        }
        buttonGrant?.setOnClickListener {
            if (showSettings) {
                openSettings()
            }
            else {
                requestPermission()
            }
        }

        permissionRationaleDialog?.show()
    }

    private fun onRationaleDialogDismissed() {
        if (!hasPermissions(context)) {
            activity.finish()
        }
    }

    private fun createRationaleDialog() {
        permissionRationaleDialog = Dialog(activity, R.style.PermissionDialog)
        permissionRationaleDialog?.setContentView(R.layout.dialog_runtime_permissions)
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(SCHEMA_PACKAGE, context.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, PHONE_SETTINGS_REQUEST)
    }

    private fun dismissRationaleDialog() {
        Log.d(TAG, "dismissRationaleDialog")
        permissionRationaleDialog?.dismiss()
    }

    sealed class PermissionState {
        object Granted : PermissionState()
        object Required : PermissionState()
        object Rationale : PermissionState()
    }

}
