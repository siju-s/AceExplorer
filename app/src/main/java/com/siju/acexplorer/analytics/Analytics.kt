package com.siju.acexplorer.analytics

import android.content.Context
import android.os.Bundle

object Analytics {
    @JvmStatic
    val logger: Logger = GoogleAnalytics()

    interface Logger {
        fun register(context: Context?)
        fun reportDeviceName()
        fun addLibClicked()
        fun homeStorageDisplayed(count: Int, paths: List<String?>?)
        fun homeLibsDisplayed(count: Int, names: List<String?>?)
        fun storageDisplayed()
        fun extStorageDisplayed()
        fun aboutDisplayed()
        fun settingsDisplayed()
        fun rateUsClicked()
        fun policyOpened()
        fun unlockFullClicked()
        fun searchClicked(isVoiceSearch: Boolean)
        fun setInAppStatus(status: Int)
        fun dualPaneState(state: Boolean)
        fun userTheme(theme: String?)
        fun switchView(isList: Boolean)
        fun safShown()
        fun safResult(success: Boolean)
        fun libDisplayed(name: String?)
        fun appInviteClicked()
        fun appInviteResult(success: Boolean)
        fun operationClicked(operation: String?)
        fun pathCopied()
        fun dragDialogShown()
        fun conflictDialogShown()
        fun zipViewer(extension: String?)
        fun navBarClicked(isHome: Boolean)
        fun openFile()
        fun openAsDialogShown()
        fun pickerShown(isRingtonePicker: Boolean)
        fun sendAnalytics(isSent: Boolean)
        fun logEvent(event: String?, params: Bundle?)
        fun logEvent(event: String?)
        fun enterPeekMode()

        companion object {
            const val EV_DEVICE_NAME = "Device_Name"
            const val EV_HOME_ADD = "Library_add_clicked"
            const val EV_HOMESCREEN = "Home_Screen"
            const val EV_STORAGE = "Storage_Screen"
            const val EV_EXT_STORAGE = "External_Storage_screen"
            const val EV_DUAL_PANE = "Dual_Pane"
            const val EV_THEME = "Theme"
            const val EV_VIEW = "View"
            const val EV_ABOUT = "About_Clicked"
            const val EV_SETTINGS = "Settings_Clicked"
            const val EV_RATE_US = "Rate_us"
            const val EV_POLICY = "Policy"
            const val EV_UNLOCK_FULL = "Unlock_full_version"
            const val EV_BILLING_STATUS = "Inapp_status"
            const val EV_SEARCH = "Search_clicked"
            const val EV_LIB_DISPLAYED = "Library_displayed"
            const val EV_APP_INVITE = "App_invite_clicked"
            const val EV_APP_RESULT = "App_invite_result"
            const val EV_SAF = "SAF"
            const val EV_SAF_RESULT = "SAF_Result"
            const val EV_OPERATION = "Operation"
            const val EV_FAB = "FAB"
            const val EV_SHARE = "Share_clicked"
            const val EV_CUT = "Cut_clicked"
            const val EV_COPY = "Copy_clicked"
            const val EV_PASTE = "Paste_clicked"
            const val EV_RENAME = "Rename_clicked"
            const val EV_DELETE = "Delete_clicked"
            const val EV_PROPERTIES = "Info_clicked"
            const val EV_EXTRACT = "Extract_clicked"
            const val EV_ARCHIVE = "Archive_clicked"
            const val EV_HIDE = "Hide_clicked"
            const val EV_ADD_FAV = "Add_favorite_clicked"
            const val EV_DELETE_FAV = "Delete_favorite_clicked"
            const val EV_COPY_PATH = "Copy_path"
            const val EV_DRAG = "Drag_dialog_shown"
            const val EV_CONFLICT = "Conflict_dialog_shown"
            const val EV_ZIP_VIEW = "Zip_viewer"
            const val EV_OPEN_AS = "Open_as_dialog_shown"
            const val EV_OPEN_FILE = "Open_file"
            const val EV_NAV_BAR = "Navbar_clicked"
            const val EV_PERMISSIONS = "Permissions_clicked"
            const val EV_PICKER = "Picker"
            const val EV_PEEK = "Peek"
        }
    }
}