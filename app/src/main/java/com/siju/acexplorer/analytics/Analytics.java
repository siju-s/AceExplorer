package com.siju.acexplorer.analytics;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

/**
 * Created by sj on 04/11/17.
 */

public class Analytics {


    private static final Analytics.Logger logger = new GoogleAnalytics();

    public static Analytics.Logger getLogger() {
        return logger;
    }

    public interface Logger {

        String EV_DEVICE_NAME = "Device_Name";

        String EV_HOME_ADD = "Library_add_clicked";

        String EV_HOMESCREEN = "Home_Screen";

        String EV_STORAGE = "Storage_Screen";

        String EV_EXT_STORAGE = "External_Storage_screen";

        String EV_DUAL_PANE = "Dual_Pane";

        String EV_THEME = "Theme";

        String EV_VIEW = "View";

        String EV_ABOUT = "About_Clicked";

        String EV_SETTINGS = "Settings_Clicked";


        String EV_RATE_US = "Rate_us";

        String EV_UNLOCK_FULL = "Unlock_full_version";

        String EV_BILLING_STATUS = "Inapp_status";

        String EV_SEARCH = "Search_clicked";

        String EV_LIB_DISPLAYED = "Library_displayed";

        String EV_APP_INVITE = "App_invite_clicked";

        String EV_APP_RESULT = "App_invite_result";

        String EV_SAF = "SAF";

        String EV_SAF_RESULT = "SAF_Result";

        String EV_OPERATION = "Operation";

        String EV_FAB = "FAB";

        String EV_SHARE = "Share_clicked";

        String EV_CUT = "Cut_clicked";

        String EV_COPY = "Copy_clicked";

        String EV_PASTE = "Paste_clicked";

        String EV_RENAME = "Rename_clicked";

        String EV_DELETE = "Delete_clicked";

        String EV_PROPERTIES = "Info_clicked";

        String EV_EXTRACT = "Extract_clicked";

        String EV_ARCHIVE = "Archive_clicked";

        String EV_HIDE = "Hide_clicked";

        String EV_ADD_FAV = "Add_favorite_clicked";

        String EV_DELETE_FAV = "Delete_favorite_clicked";

        String EV_COPY_PATH = "Copy_path";

        String EV_DRAG = "Drag_dialog_shown";

        String EV_CONFLICT = "Conflict_dialog_shown";

        String EV_ZIP_VIEW = "Zip_viewer";

        String EV_OPEN_AS = "Open_as_dialog_shown";

        String EV_OPEN_FILE = "Open_file";

        String EV_NAV_BAR = "Navbar_clicked";

        String EV_DRAWER = "Drawer_item_clicked";

        String EV_PERMISSIONS = "Permissions_clicked";

        String EV_PICKER = "Picker";


        void register(Context context);

        void reportDeviceName();

        void addLibClicked();

        void homeStorageDisplayed(int count, List<String> paths);

        void homeLibsDisplayed(int count, List<String> names);

        void storageDisplayed();

        void extStorageDisplayed();

        void aboutDisplayed();

        void settingsDisplayed();

        void rateUsClicked();

        void unlockFullClicked();

        void searchClicked(boolean isVoiceSearch);

        void setInAppStatus(int status);

        void dualPaneState(boolean state);

        void userTheme(String theme);

        void switchView(boolean isList);

        void SAFShown();

        void SAFResult(boolean success);

        void libDisplayed(String name);

        void appInviteClicked();

        void appInviteResult(boolean success);

        void drawerItemClicked();

        void operationClicked(String operation);

        void pathCopied();

        void dragDialogShown();

        void conflictDialogShown();

        void zipViewer(String extension);

        void navBarClicked(boolean isHome);

        void openFile();

        void openAsDialogShown();

        void pickerShown(boolean isRingtonePicker);

        void sendAnalytics(boolean isSent);

        void logEvent(String event, Bundle params);

        void logEvent(String event);
    }
}
