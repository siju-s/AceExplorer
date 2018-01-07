package com.siju.acexplorer.analytics;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.siju.acexplorer.BuildConfig;

import java.util.List;
import java.util.Locale;


public class GoogleAnalytics implements Analytics.Logger {

    private FirebaseAnalytics firebaseAnalytics;
    private boolean sendAnalytics = true;


    @Override
    public void register(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void reportDeviceName() {
        String name = Build.MANUFACTURER + " : " + Build.PRODUCT + "(" + Build.MODEL + ")";
        Bundle params = new Bundle();
        params.putString("name", name);
        params.putInt("version", Build.VERSION.SDK_INT);
        params.putString("language", Locale.getDefault().getDisplayLanguage());
        params.putString("country", Locale.getDefault().getDisplayCountry());
        logEvent(EV_DEVICE_NAME, params);
    }

    @Override
    public void addLibClicked() {
        logEvent(EV_HOME_ADD);
    }

    @Override
    public void homeStorageDisplayed(int count, List<String> paths) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            stringBuilder.append(paths.get(i));
            if (i != paths.size() - 1) {
                stringBuilder.append(",");
            }
        }
        Bundle params = new Bundle();
        params.putInt("storage_count", count);
        params.putString("paths", stringBuilder.toString());
        logEvent(EV_HOMESCREEN, params);
    }

    @Override
    public void homeLibsDisplayed(int count, List<String> names) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            stringBuilder.append(names.get(i));
            if (i != names.size() - 1) {
                stringBuilder.append(",");
            }
        }
        Bundle params = new Bundle();
        params.putInt("lib_count", count);
        params.putString("names", stringBuilder.toString());
        logEvent(EV_HOMESCREEN, params);
    }

    @Override
    public void storageDisplayed() {
        logEvent(EV_STORAGE);
    }

    @Override
    public void extStorageDisplayed() {
        logEvent(EV_EXT_STORAGE);
    }

    @Override
    public void aboutDisplayed() {
        logEvent(EV_ABOUT);
    }

    @Override
    public void settingsDisplayed() {
        logEvent(EV_SETTINGS);
    }

    @Override
    public void rateUsClicked() {
        logEvent(EV_RATE_US);
    }

    @Override
    public void unlockFullClicked() {
        logEvent(EV_UNLOCK_FULL);
    }

    @Override
    public void searchClicked(boolean isVoiceSearch) {
        Bundle params = new Bundle();
        params.putString("voice", isVoiceSearch ? "yes" : "no");
        logEvent(EV_SEARCH, params);
    }


    @Override
    public void setInAppStatus(int status) {
        Bundle params = new Bundle();
        String text;
        if (status == -1) {
            text = "Unsupported";
        } else if (status == 0) {
            text = "Free";
        } else {
            text = "Premium";
        }
        params.putString("status", text);
        logEvent(EV_BILLING_STATUS, params);
    }

    @Override
    public void dualPaneState(boolean state) {
        Bundle params = new Bundle();
        params.putString("state", state ? "yes" : "no");
        logEvent(EV_DUAL_PANE, params);
    }

    @Override
    public void userTheme(String theme) {
        Bundle params = new Bundle();
        params.putString("state", theme);
        logEvent(EV_THEME, params);
    }

    @Override
    public void switchView(boolean isList) {
        Bundle params = new Bundle();
        params.putString("view", isList ? "LIST" : "GRID");
        logEvent(EV_VIEW, params);
    }

    @Override
    public void SAFShown() {
        logEvent(EV_SAF);

    }

    @Override
    public void SAFResult(boolean success) {
        Bundle params = new Bundle();
        params.putString("success", success ? "yes" : "no");
        logEvent(EV_SAF_RESULT, params);
    }


    @Override
    public void libDisplayed(String name) {
        Bundle params = new Bundle();
        params.putString("lib_name", name);
        logEvent(EV_LIB_DISPLAYED, params);
    }

    @Override
    public void appInviteClicked() {
        logEvent(EV_APP_INVITE);
    }

    @Override
    public void appInviteResult(boolean success) {
        Bundle params = new Bundle();
        params.putString("success", success ? "yes" : "no");
        logEvent(EV_APP_RESULT, params);
    }


    @Override
    public void drawerItemClicked() {
        logEvent(EV_DRAWER);
    }

    @Override
    public void operationClicked(String operation) {
        Bundle params = new Bundle();
        params.putString("operation", operation);
        logEvent(EV_OPERATION, params);
    }

    @Override
    public void pathCopied() {
        logEvent(EV_COPY_PATH);
    }

    @Override
    public void dragDialogShown() {
        logEvent(EV_DRAG);
    }

    @Override
    public void conflictDialogShown() {
        logEvent(EV_CONFLICT);
    }

    @Override
    public void zipViewer(String extension) {
        Bundle params = new Bundle();
        params.putString("extension", extension);
        logEvent(EV_ZIP_VIEW, params);
    }

    @Override
    public void navBarClicked(boolean isHome) {
        Bundle params = new Bundle();
        params.putString("isHome", isHome ? "yes" : "no");
        logEvent(EV_NAV_BAR, params);
    }

    @Override
    public void openFile() {
        logEvent(EV_OPEN_FILE);
    }

    @Override
    public void openAsDialogShown() {
        logEvent(EV_OPEN_AS);
    }

    @Override
    public void pickerShown(boolean isRingtonePicker) {
        Bundle params = new Bundle();
        params.putString("isRingTonePicker", isRingtonePicker ? "yes" : "no");
        logEvent(EV_PICKER, params);
    }


    @Override
    public void enterPeekMode() {
        logEvent(EV_PEEK);
    }

    @Override
    public void sendAnalytics(boolean isSent) {
        sendAnalytics = isSent;
    }


    @Override
    public void logEvent(String event, Bundle params) {
        if (sendAnalytics && !BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("prod")) {
            firebaseAnalytics.logEvent(event, params);
        }
    }


    @Override
    public void logEvent(String event) {
        if (sendAnalytics && !BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("prod")) {
            firebaseAnalytics.logEvent(event, null);
        }
    }
}
