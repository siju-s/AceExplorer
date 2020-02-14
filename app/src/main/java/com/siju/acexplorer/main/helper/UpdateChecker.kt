package com.siju.acexplorer.main.helper

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.siju.acexplorer.main.AceActivity

const val REQUEST_CODE_UPDATE = 300

class UpdateChecker(val context: Context, val activity: AceActivity, private val updateCallback: UpdateCallback) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private var isUpdateAvailable = false
    private var updateStatus = InstallStatus.UNKNOWN

    private val installStateUpdatedListener = InstallStateUpdatedListener {
        updateStatus = it.installStatus()
        isUpdateAvailable = if (it.installStatus() == InstallStatus.DOWNLOADED) {
            updateCallback.onUpdateDownloaded(appUpdateManager)
            true
        }
        else if (it.installStatus() == InstallStatus.INSTALLED || it.installStatus() == InstallStatus.DOWNLOADING) {
            updateCallback.onUpdateInstalled()
            false
        }
        else {
            true
        }
    }

    init {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                requestUpdate(appUpdateInfo)
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                updateCallback.onUpdateDownloaded(appUpdateManager)
            }
        }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo) {
        val viewModel = activity.getViewModel()
        if (viewModel.hasUserCancelledUpdate()) {
            updateCallback.onUpdateCancelledByUser()
        }
        else {
            startUpdateFlow(appUpdateInfo)
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                REQUEST_CODE_UPDATE)
    }

    fun startUpdate() {
        when (updateStatus) {
            InstallStatus.DOWNLOADED -> appUpdateManager.completeUpdate()
            InstallStatus.PENDING, InstallStatus.CANCELED -> {
               startUpdateFlow(appUpdateManager.appUpdateInfo.result)
            }
        }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    fun isUpdateAvailable()  = isUpdateAvailable

    fun isUpdateDownloaded() = updateStatus == InstallStatus.DOWNLOADED

    fun onUpdateSnackbarDismissed() {
        updateCallback.onUpdateSnackbarDismissed()
    }

    interface UpdateCallback {
        fun onUpdateDownloaded(appUpdateManager: AppUpdateManager)
        fun onUpdateInstalled()
        fun onUpdateCancelledByUser()
        fun onUpdateSnackbarDismissed()
    }
}