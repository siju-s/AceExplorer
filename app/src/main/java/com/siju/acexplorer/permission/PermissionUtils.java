package com.siju.acexplorer.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.siju.acexplorer.Factory;

import java.util.Hashtable;

public class PermissionUtils {
    private static final String[] sRequiredPermissions = new String[] {
            // Required to read existing SMS threads
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final Hashtable<String, Integer> sPermissions = new Hashtable<>();

    /** Does the app have the minimum set of permissions required to operate. */
    public static boolean hasRequiredPermissions() {
        return hasPermissions();
    }

    /** Does the app have all the specified permissions */
    private static boolean hasPermissions() {
        for (final String permission : PermissionUtils.sRequiredPermissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasPermission(final String permission) {
        if (PermissionUtils.isAtLeastM()) {
            // It is safe to cache the PERMISSION_GRANTED result as the process gets killed if the
            // user revokes the permission setting. However, PERMISSION_DENIED should not be
            // cached as the process does not get killed if the user enables the permission setting.
            if (!sPermissions.containsKey(permission)
                    || sPermissions.get(permission) == PackageManager.PERMISSION_DENIED) {
                final Context context = Factory.get();
                final int permissionState = context.checkSelfPermission(permission);
                sPermissions.put(permission, permissionState);
            }
            return sPermissions.get(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }



    /**
     * @return True if the version of Android that we're running on is at least M
     *  (API level 23).
     */
    public static boolean isAtLeastM() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }
}
