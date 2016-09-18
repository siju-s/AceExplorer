package com.siju.acexplorer.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.siju.acexplorer.Factory;

import java.util.Hashtable;

/**
 * Created by Siju on 30-06-2016.
 */

public class PermissionUtils {
    private static String[] sRequiredPermissions = new String[] {
            // Required to read existing SMS threads
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static Hashtable<String, Integer> sPermissions = new Hashtable<String, Integer>();

    public static String[] getRequiredPermissions() {
        return sRequiredPermissions;
    }

    /** Does the app have the minimum set of permissions required to operate. */
    public static boolean hasRequiredPermissions() {
        return hasPermissions(sRequiredPermissions);
    }

    /** Does the app have all the specified permissions */
    public static boolean hasPermissions(final String[] permissions) {
        for (final String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermission(final String permission) {
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
