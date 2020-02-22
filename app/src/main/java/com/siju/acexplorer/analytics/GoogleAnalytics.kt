package com.siju.acexplorer.analytics

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.siju.acexplorer.BuildConfig
import java.util.*

class GoogleAnalytics : Analytics.Logger {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var sendAnalytics = true

    override fun register(context: Context?) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
    }

    override fun reportDeviceName() {
        val name = Build.MANUFACTURER + " : " + Build.PRODUCT + "(" + Build.MODEL + ")"
        val params = Bundle()
        params.putString("name", name)
        params.putInt("version", Build.VERSION.SDK_INT)
        params.putString("language", Locale.getDefault().displayLanguage)
        params.putString("country", Locale.getDefault().displayCountry)
        logEvent(Analytics.Logger.EV_DEVICE_NAME, params)
    }

    override fun addLibClicked() {
        logEvent(Analytics.Logger.EV_HOME_ADD)
    }

    override fun homeStorageDisplayed(count: Int, paths: List<String?>?) {
        val stringBuilder = StringBuilder()
        for (i in paths!!.indices) {
            stringBuilder.append(paths[i])
            if (i != paths.size - 1) {
                stringBuilder.append(",")
            }
        }
        val params = Bundle()
        params.putInt("storage_count", count)
        params.putString("paths", stringBuilder.toString())
        logEvent(Analytics.Logger.EV_HOMESCREEN, params)
    }

    override fun homeLibsDisplayed(count: Int, names: List<String?>?) {
        val stringBuilder = StringBuilder()
        for (i in names!!.indices) {
            stringBuilder.append(names[i])
            if (i != names.size - 1) {
                stringBuilder.append(",")
            }
        }
        val params = Bundle()
        params.putInt("lib_count", count)
        params.putString("names", stringBuilder.toString())
        logEvent(Analytics.Logger.EV_HOMESCREEN, params)
    }

    override fun storageDisplayed() {
        logEvent(Analytics.Logger.EV_STORAGE)
    }

    override fun extStorageDisplayed() {
        logEvent(Analytics.Logger.EV_EXT_STORAGE)
    }

    override fun aboutDisplayed() {
        logEvent(Analytics.Logger.EV_ABOUT)
    }

    override fun settingsDisplayed() {
        logEvent(Analytics.Logger.EV_SETTINGS)
    }

    override fun rateUsClicked() {
        logEvent(Analytics.Logger.EV_RATE_US)
    }

    override fun policyOpened() {
        logEvent(Analytics.Logger.EV_POLICY)
    }

    override fun unlockFullClicked() {
        logEvent(Analytics.Logger.EV_UNLOCK_FULL)
    }

    override fun searchClicked(isVoiceSearch: Boolean) {
        val params = Bundle()
        params.putString("voice", if (isVoiceSearch) "yes" else "no")
        logEvent(Analytics.Logger.EV_SEARCH, params)
    }

    override fun setInAppStatus(status: Int) {
        val params = Bundle()
        val text = when (status) {
            -1 -> {
                "Unsupported"
            }
            0 -> {
                "Free"
            }
            else -> {
                "Premium"
            }
        }
        params.putString("status", text)
        logEvent(Analytics.Logger.EV_BILLING_STATUS, params)
    }

    override fun dualPaneState(state: Boolean) {
        val params = Bundle()
        params.putString("state", if (state) "yes" else "no")
        logEvent(Analytics.Logger.EV_DUAL_PANE, params)
    }

    override fun userTheme(theme: String?) {
        val params = Bundle()
        params.putString("state", theme)
        logEvent(Analytics.Logger.EV_THEME, params)
    }

    override fun switchView(isList: Boolean) {
        val params = Bundle()
        params.putString("view", if (isList) "LIST" else "GRID")
        logEvent(Analytics.Logger.EV_VIEW, params)
    }

    override fun safShown() {
        logEvent(Analytics.Logger.EV_SAF)
    }

    override fun safResult(success: Boolean) {
        val params = Bundle()
        params.putString("success", if (success) "yes" else "no")
        logEvent(Analytics.Logger.EV_SAF_RESULT, params)
    }

    override fun libDisplayed(name: String?) {
        val params = Bundle()
        params.putString("lib_name", name)
        logEvent(Analytics.Logger.EV_LIB_DISPLAYED, params)
    }

    override fun appInviteClicked() {
        logEvent(Analytics.Logger.EV_APP_INVITE)
    }

    override fun appInviteResult(success: Boolean) {
        val params = Bundle()
        params.putString("success", if (success) "yes" else "no")
        logEvent(Analytics.Logger.EV_APP_RESULT, params)
    }

    override fun operationClicked(operation: String?) {
        val params = Bundle()
        params.putString("operation", operation)
        logEvent(Analytics.Logger.EV_OPERATION, params)
    }

    override fun pathCopied() {
        logEvent(Analytics.Logger.EV_COPY_PATH)
    }

    override fun dragDialogShown() {
        logEvent(Analytics.Logger.EV_DRAG)
    }

    override fun conflictDialogShown() {
        logEvent(Analytics.Logger.EV_CONFLICT)
    }

    override fun zipViewer(extension: String?) {
        val params = Bundle()
        params.putString("extension", extension)
        logEvent(Analytics.Logger.EV_ZIP_VIEW, params)
    }

    override fun navBarClicked(isHome: Boolean) {
        val params = Bundle()
        params.putString("isHome", if (isHome) "yes" else "no")
        logEvent(Analytics.Logger.EV_NAV_BAR, params)
    }

    override fun openFile() {
        logEvent(Analytics.Logger.EV_OPEN_FILE)
    }

    override fun openAsDialogShown() {
        logEvent(Analytics.Logger.EV_OPEN_AS)
    }

    override fun pickerShown(isRingtonePicker: Boolean) {
        val params = Bundle()
        params.putString("isRingTonePicker", if (isRingtonePicker) "yes" else "no")
        logEvent(Analytics.Logger.EV_PICKER, params)
    }

    override fun enterPeekMode() {
        logEvent(Analytics.Logger.EV_PEEK)
    }

    override fun sendAnalytics(isSent: Boolean) {
        sendAnalytics = isSent
    }

    override fun logEvent(event: String?, params: Bundle?) {
        if (sendAnalytics && !BuildConfig.DEBUG && !BuildConfig.IS_DEV_VERSION) {
            firebaseAnalytics!!.logEvent(event!!, params)
        }
    }

    override fun logEvent(event: String?) {
        if (sendAnalytics && !BuildConfig.DEBUG && !BuildConfig.IS_DEV_VERSION) {
            firebaseAnalytics!!.logEvent(event!!, null)
        }
    }
}