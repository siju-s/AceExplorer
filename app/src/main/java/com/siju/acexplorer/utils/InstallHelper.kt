package com.siju.acexplorer.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.helper.FileUtils.showMessage
import com.siju.acexplorer.main.model.helper.IntentResolver
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.main.model.helper.UriHelper

object InstallHelper {

    private const val PACKAGE = "package"

    fun canInstallApp(context: Context): Boolean {
        return if (SdkHelper.isAtleastOreo) {
            context.packageManager.canRequestPackageInstalls()
        }
        else true
    }

    fun requestUnknownAppsInstallPermission(context: Context?, launcher: ActivityResultLauncher<Intent>) {
        context ?: return
        if (SdkHelper.isAtleastOreo) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            val uri = Uri.fromParts(PACKAGE, context.packageName, null)
            intent.data = uri
            launcher.launch(intent)
        }
    }

    fun openInstallScreen(context: Context?, path: String?) {
        context?.let {
            openInstallAppScreen(context,
                    UriHelper.createContentUri(
                            context.applicationContext,
                            path))
        }
    }

    fun openInstallAppScreen(context: Context, uri: Uri?) {
        uri ?: return
        val intent = Intent()
        intent.action = Intent.ACTION_INSTALL_PACKAGE
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (IntentResolver.canHandleIntent(context, intent)) {
            context.startActivity(intent)
        }
        else {
            showMessage(context, context.getString(R.string.msg_error_not_supported))
        }
    }
}
