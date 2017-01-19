package com.siju.acexplorer.permission;

/**
 * Created by SJ on 19-01-2017.
 */

public interface PermissionResultCallback {

    void onPermissionGranted(String[] permissionName);

    void onPermissionDeclined(String[] permissionName);
}
