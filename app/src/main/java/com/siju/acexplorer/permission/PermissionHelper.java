package com.siju.acexplorer.permission;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;

import static com.siju.acexplorer.BaseActivity.PERMISSIONS_REQUEST;
import static com.siju.acexplorer.BaseActivity.SETTINGS_REQUEST;

/**
 * Created by SJ on 19-01-2017.
 */

public class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    private final Activity context;
    private Dialog permissionDialog;
    private String permissions[] = new String[]{Manifest.permission
            .WRITE_EXTERNAL_STORAGE};
    private PermissionResultCallback permissionCallback;


    private PermissionHelper(@NonNull Activity context) {
        this.context = context;
        if (context instanceof PermissionResultCallback) {
            this.permissionCallback = (PermissionResultCallback) context;
        } else {
            throw new IllegalArgumentException("Activity must implement (OnPermissionCallback)");
        }
    }

    public static PermissionHelper getInstance(Activity context) {
        return new PermissionHelper(context);
    }

    /**
     * Called for the 1st time when app is launched to check permissions
     */
    public void checkPermissions() {
        if (!PermissionUtils.hasRequiredPermissions()) {
            requestPermission();
        } else {
            Logger.log(TAG, "checkPermissions ELSE");
        }

    }

    private void requestPermission() {
        Logger.log(TAG, "requestPermission");
        ActivityCompat.requestPermissions(context, permissions, PERMISSIONS_REQUEST);
    }

    public void onPermissionResult() {
        if (PermissionUtils.hasRequiredPermissions()) {
            Logger.log(TAG, "Permission granted");
            if (!inappShortcutMode) {
                permissionCallback.onPermissionGranted(permissions);
//                setPermissionGranted();
            } else {
                openCategoryItem(null, mCategory);
                inappShortcutMode = false;
            }
            dismissRationaleDialog();
        } else {
            showRationale();
        }
    }

    private void showRationale() {
        Log.d(TAG, "showRationale");

        final boolean showSettings;
        Button buttonGrant;
        TextView textViewPermissionHint;

        showSettings = !ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission
                .WRITE_EXTERNAL_STORAGE);
        if (permissionDialog == null) {
            permissionDialog = new Dialog(context, R.style.PermissionDialog);
            permissionDialog.setContentView(R.layout.dialog_runtime_permissions);

        }
        permissionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "Rationale dismiss");
                if (!PermissionUtils.hasRequiredPermissions()) {
                    permissionDialog.dismiss();
                    context.finish();
                }
            }
        });
        buttonGrant = (Button) permissionDialog.findViewById(R.id.buttonGrant);
        textViewPermissionHint = (TextView) permissionDialog.findViewById(R.id.textPermissionHint);
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
                    context.startActivityForResult(intent, SETTINGS_REQUEST);
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
