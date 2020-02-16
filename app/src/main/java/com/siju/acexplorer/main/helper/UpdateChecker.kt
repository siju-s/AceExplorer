package com.siju.acexplorer.main.helper

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.google.android.play.core.tasks.Task
import com.siju.acexplorer.R
import com.siju.acexplorer.main.AceActivity

const val REQUEST_CODE_UPDATE = 300

class UpdateChecker(val context: Context, val activity: AceActivity, private var updateCallback: UpdateCallback) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private var isUpdateAvailable = false
    private var updateStatus = InstallStatus.UNKNOWN

    private val installStateUpdatedListener = InstallStateUpdatedListener {
        updateStatus = it.installStatus()
        Log.d(this.javaClass.simpleName, "installStateUpdatedListener:updateStatus:$updateStatus")
        isUpdateAvailable = when {
            it.installStatus() == InstallStatus.DOWNLOADED -> {
                updateCallback.onUpdateDownloaded(appUpdateManager)
                true
            }
            it.installStatus() == InstallStatus.INSTALLED -> {
                updateCallback.onUpdateInstalled()
                false
            }
            it.installStatus() == InstallStatus.DOWNLOADING -> {
                updateCallback.onUpdateDownloading()
                true
            }
            else -> {
                true
            }
        }
    }

    init {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        startUpdateTask(appUpdateInfoTask)
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    fun setUpdateCallback(updateCallback: UpdateCallback) {
        this.updateCallback = updateCallback
    }

    private fun startUpdateTask(appUpdateInfoTask: Task<AppUpdateInfo>, userInitiated : Boolean = false) {
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                requestUpdate(appUpdateInfo, userInitiated)
            }
        }
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                updateStatus = InstallStatus.DOWNLOADED
                isUpdateAvailable = true
                updateCallback.onUpdateDownloaded(appUpdateManager)
            }
        }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo, userInitiated : Boolean = false) {
        val viewModel = activity.getViewModel()
        isUpdateAvailable = true
        updateStatus = appUpdateInfo.installStatus()
        Log.d(this.javaClass.simpleName, "requestUpdate:updateStatus:$updateStatus")
        if (!userInitiated && viewModel.hasUserCancelledUpdate()) {
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
        Log.d(this.javaClass.simpleName, "startUpdate:updateStatus:$updateStatus")
        when (updateStatus) {
            InstallStatus.DOWNLOADED -> appUpdateManager.completeUpdate()
            InstallStatus.PENDING, InstallStatus.CANCELED, InstallStatus.UNKNOWN -> {
               startUpdateTask(appUpdateManager.appUpdateInfo, true)
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

    fun showUpdateSnackbar(view : View?) {
        view?.let {
            val context = view.context
            val snackbar = Snackbar.make(view, context.getString(R.string.update_available), Snackbar.LENGTH_INDEFINITE)
                    .apply {
                        setAction(R.string.restart) {
                            startUpdate()
                        }
                    }
            snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    onUpdateSnackbarDismissed()
                }
            })
            snackbar.show()
        }
    }

    interface UpdateCallback {
        fun onUpdateDownloaded(appUpdateManager: AppUpdateManager)
        fun onUpdateInstalled()
        fun onUpdateCancelledByUser()
        fun onUpdateSnackbarDismissed()
        fun onUpdateDownloading()
    }
}