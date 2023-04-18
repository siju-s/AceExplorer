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
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.R
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.helper.SdkHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.siju.acexplorer.common.R as RC


private const val TAG = "PermissionHelper"
private const val SCHEMA_PACKAGE = "package"
private const val PERMISSIONS_REQUEST = 1000
private const val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

class PermissionHelper @Inject constructor(private val activity: FragmentActivity, @ApplicationContext val context: Context) {
    private var permissionRationaleDialog: Dialog? = null

    val permissionStatus: MutableLiveData<PermissionState> = MutableLiveData()
    private val permissionsNeeded = ArrayList<String>()
    private var allFilesAccessNeeded = false

    fun checkPermissions() {
        if (hasPermissions(context) && !allFilesAccessNeeded) {
            permissionStatus.value = PermissionState.Granted
        }
        else {
            permissionStatus.value = PermissionState.Required
        }
        Log.d(TAG, "checkPermissions: permissionstatus:${permissionStatus.value}")
    }

    fun onForeground() {
        if (allFilesAccessNeeded && SdkHelper.isAtleastAndroid11 && Environment.isExternalStorageManager()) {
            allFilesAccessNeeded = false
            dismissRationaleDialog()
            permissionStatus.value = PermissionState.Granted
        }
        else if (permissionRationaleDialog?.isShowing == true && !allFilesAccessNeeded && hasPermissions(context)) {
                dismissRationaleDialog()
                permissionStatus.value = PermissionState.Granted
        }
    }

    private fun hasPermissions(context: Context): Boolean {
        val packageInfo: PackageInfo
        try {
            packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
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
        if (SdkHelper.isAtleastAndroid11) {
            allFilesAccessNeeded = !Environment.isExternalStorageManager()
            permissionsNeeded.remove(storagePermission)
            permissionsNeeded.remove(Manifest.permission.READ_EXTERNAL_STORAGE)
            //permissionsNeeded.remove("android.permission.POST_NOTIFICATIONS")
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
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray(),
                PERMISSIONS_REQUEST
            )
        }
        else if (allFilesAccessNeeded) {
            showRationale()
        }
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
        val showSettings = if (allFilesAccessNeeded) {
           false
        }
        else {
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                storagePermission
            )
        }
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
                allFilesAccessNeeded -> {
                    requestAllFilesPermission()
                }
                else -> {
                    requestPermission()
                }
            }
        }

        permissionRationaleDialog?.show()
    }

    private fun requestAllFilesPermission() {
        if (SdkHelper.isAtleastAndroid11) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts(SCHEMA_PACKAGE, context.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
    }

    private fun onRationaleDialogDismissed() {
        if (!hasPermissions(context) || allFilesAccessNeeded) {
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
        activity.startActivity(intent)
    }

    private fun dismissRationaleDialog() {
        permissionRationaleDialog?.dismiss()
    }

    sealed class PermissionState {
        object Granted : PermissionState()
        object Required : PermissionState()
        object Rationale : PermissionState()
    }

}
