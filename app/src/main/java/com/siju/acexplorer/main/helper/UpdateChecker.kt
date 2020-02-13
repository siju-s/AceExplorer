package com.siju.acexplorer.main.helper

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE

const val REQUEST_CODE_UPDATE = 300

class UpdateChecker(val context: Context, val activity: AppCompatActivity, private val updateCallback: UpdateCallback) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    private val installStateUpdatedListener = InstallStateUpdatedListener {
        if (it.installStatus() == InstallStatus.DOWNLOADED) {
            updateCallback.onUpdateDownloaded(appUpdateManager)
        }
        else if (it.installStatus() == InstallStatus.INSTALLED) {
            updateCallback.onUpdateInstalled()
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
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                REQUEST_CODE_UPDATE)
    }


    fun startUpdate() {
        appUpdateManager.completeUpdate()
    }




    fun onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    interface UpdateCallback {
        fun onUpdateDownloaded(appUpdateManager: AppUpdateManager)
        fun onUpdateInstalled()
    }
}