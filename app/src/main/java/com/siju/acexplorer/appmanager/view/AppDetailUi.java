package com.siju.acexplorer.appmanager.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public interface AppDetailUi {

    String EXTRA_PACKAGE_NAME     = "package";
    int    REQUEST_CODE_UNINSTALL = 1;

    void inflateView();

    void setActivity(AppCompatActivity activity);

    void onResume();

    void handleActivityResult(int requestCode, int resultCode, Intent data);

    void setListener(Listener listener);

    interface Listener {
        Object getPackageInfo(String packageName);

        String getInstallerSource(String packageName);
    }
}
