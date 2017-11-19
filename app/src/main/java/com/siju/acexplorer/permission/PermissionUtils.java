/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.siju.acexplorer.AceApplication;

import java.util.Hashtable;

import static com.siju.acexplorer.model.helper.SdkHelper.isAtleastMarsh;

public class PermissionUtils {
    private static final String[]                   REQD_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final Hashtable<String, Integer> permissions      = new Hashtable<>();

    public static boolean hasRequiredPermissions() {
        return hasPermissions();
    }

    public static boolean hasStoragePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    private static boolean hasPermissions() {
        for (final String permission : PermissionUtils.REQD_PERMISSIONS) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasPermission(final String permission) {
        if (isAtleastMarsh()) {
            // It is safe to cache the PERMISSION_GRANTED result as the process gets killed if the
            // user revokes the permission setting. However, PERMISSION_DENIED should not be
            // cached as the process does not get killed if the user enables the permission setting.
            if (!permissions.containsKey(permission)
                    || permissions.get(permission) == PackageManager.PERMISSION_DENIED) {
                final Context context = AceApplication.getAppContext();
                final int permissionState = ContextCompat.checkSelfPermission(context, permission);
                permissions.put(permission, permissionState);
            }
            return permissions.get(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }


}
