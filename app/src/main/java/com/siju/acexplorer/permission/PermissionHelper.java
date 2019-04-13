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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static com.siju.acexplorer.main.view.MainUiView.PERMISSIONS_REQUEST;


public class PermissionHelper {
    private static final String     TAG                    = "PermissionHelper";
    private static final        int PHONE_SETTINGS_REQUEST = 2000;

    private final AppCompatActivity context;
    private       PermissionResultCallback permissionCallback;
    private       Dialog                   permissionDialog;

    private String REQD_PERMISSIONS[] = new String[]{Manifest.permission
            .WRITE_EXTERNAL_STORAGE};


    public PermissionHelper(@NonNull AppCompatActivity context, PermissionResultCallback permissionCallback) {
        this.context = context;
        this.permissionCallback = permissionCallback;
    }


    public void onResume() {
         /*
          This handles the scenario when snackbar is shown and user presses home and grants
          access to app and
          returns to app. In that case,setupInitialData the data and dismiss the snackbar.
         */

        if (permissionDialog != null && permissionDialog.isShowing()) {
            if (PermissionUtils.hasRequiredPermissions()) {
                permissionDialog.dismiss();
                permissionCallback.onPermissionGranted(REQD_PERMISSIONS);
            }
        }
    }

    public void checkPermissions() {
        if (!PermissionUtils.hasRequiredPermissions()) {
            requestPermission();
        } else {
            permissionCallback.onPermissionGranted(REQD_PERMISSIONS);
        }
    }

    private void requestPermission() {
        Logger.log(TAG, "requestPermission");
        ActivityCompat.requestPermissions(context, REQD_PERMISSIONS, PERMISSIONS_REQUEST);
    }

    public void onPermissionResult() {
        if (PermissionUtils.hasRequiredPermissions()) {
            Logger.log(TAG, "Permission granted");
            permissionCallback.onPermissionGranted(REQD_PERMISSIONS);
            dismissRationaleDialog();
        } else {
            showRationale();
        }
    }

    private void showRationale() {
        final boolean showSettings;
        Button buttonGrant;
        TextView textViewPermissionHint;

        showSettings = !ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest
                .permission
                .WRITE_EXTERNAL_STORAGE);
        if (permissionDialog == null) {
            permissionDialog = new Dialog(context, R.style.PermissionDialog);
            permissionDialog.setContentView(R.layout.dialog_runtime_permissions);

        }
        permissionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Logger.log(TAG, "Rationale dismiss");
                if (!PermissionUtils.hasRequiredPermissions()) {
                    permissionDialog.dismiss();
                    context.finish();
                }
            }
        });
        buttonGrant = permissionDialog.findViewById(R.id.buttonGrant);
        textViewPermissionHint = permissionDialog.findViewById(R.id.textPermissionHint);
        if (showSettings) {
            buttonGrant.setText(R.string.action_settings);
            textViewPermissionHint.setVisibility(View.VISIBLE);
        }
        buttonGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showSettings) {
                    requestPermission();
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    context.startActivityForResult(intent, PHONE_SETTINGS_REQUEST);
                }
            }
        });

        permissionDialog.show();
    }

    private void dismissRationaleDialog() {
        if (permissionDialog != null && permissionDialog.isShowing()) {
            permissionDialog.dismiss();
        }
    }
}
