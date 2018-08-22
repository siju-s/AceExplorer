package com.siju.acexplorer.appmanager.model;

public interface AppModel {

    Object getPackageInfo(String packageName);
    String getInstallerSource(String packageName);
}
