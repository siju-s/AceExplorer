package com.siju.acexplorer.appmanager.model;

import android.content.Context;
import android.content.pm.PackageManager;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;

public class AppModelImpl implements AppModel {

    private static final String  PACKAGE_NAME_PLAYSTORE       = "com.android.vending";
    private static final String  PACKAGE_NAME_AMAZON_APPSTORE = "com.amazon.venezia";
    private              Context context;

    public AppModelImpl() {
        context = AceApplication.Companion.getAppContext();
    }

    @Override
    public Object getPackageInfo(String packageName)  {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        }
        catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public String getInstallerSource(String packageName) {
        return getInstallerSourceName(context.getPackageManager().getInstallerPackageName(packageName));
    }

    private String getInstallerSourceName(String packageName) {
        if (packageName == null) {
            return context.getString(R.string.unknown);
        } else if (PACKAGE_NAME_PLAYSTORE.equals(packageName)) {
            return context.getString(R.string.play_store);
        } else if (PACKAGE_NAME_AMAZON_APPSTORE.equals(packageName)) {
            return context.getString(R.string.amazon_play_store);
        }
        return context.getString(R.string.unknown);
    }
}
