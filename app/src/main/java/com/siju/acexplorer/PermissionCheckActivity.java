package com.siju.acexplorer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.utils.DialogUtils;
import com.siju.acexplorer.utils.PermissionUtils;

/**
 * Created by Siju on 11-09-2016.
 */
public class PermissionCheckActivity extends AppCompatActivity {
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;
    private static final long AUTOMATED_RESULT_THRESHOLD_MILLLIS = 250;
    private static final String PACKAGE_URI_PREFIX = "package:";
    private long mRequestTimeMillis;
    private TextView mNextView;
    private TextView mSettingsView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (redirectIfNeeded()) {
            return;
        }
        setContentView(R.layout.permission_rationale);

        mNextView  = (TextView)findViewById(R.id.ok);
        mSettingsView  = (TextView)findViewById(R.id.settings);


        mNextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryRequestPermission();
            }
        });

        mSettingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse(PACKAGE_URI_PREFIX + getPackageName()));
                startActivity(intent);
            }
        });

        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                finish();
            }
        });



//        tryRequestPermission();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (redirectIfNeeded()) {
            return;
        }
    }


    private void tryRequestPermission() {
        final String[] missingPermissions = PermissionUtils.getRequiredPermissions();
        if (missingPermissions.length == 0) {
            redirect();
            return;
        }

        mRequestTimeMillis = SystemClock.elapsedRealtime();
        ActivityCompat.requestPermissions(this, missingPermissions, REQUIRED_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            // We do not use grantResults as some of the granted permissions might have been
            // revoked while the permissions dialog box was being shown for the missing permissions.
            if (PermissionUtils.hasRequiredPermissions()) {
                redirect();
            } else {
                final long currentTimeMillis = SystemClock.elapsedRealtime();
                // If the permission request completes very quickly, it must be because the system
                // automatically denied. This can happen if the user had previously denied it
                // and checked the "Never ask again" check box.
                if ((currentTimeMillis - mRequestTimeMillis) < AUTOMATED_RESULT_THRESHOLD_MILLLIS) {
                    mNextView.setVisibility(View.GONE);

                    mSettingsView.setVisibility(View.VISIBLE);
                    findViewById(R.id.textViewPermissionHint).setVisibility(View.VISIBLE);

//                    showRationaleDialog(true);
                }
//                else {
//                    showRationaleDialog(false);
//                }
            }
        }
    }

    private void showRationaleDialog(boolean showSettings) {

        String texts[] = new String[]{getString(R.string.need_permission), getString(R.string.msg_ok),
                "", getString(R.string.exit)};

        final MaterialDialog materialDialog = new DialogUtils().showCustomDialog(this,
                R.layout.permission_rationale, texts);
        TextView textOk = (TextView)materialDialog.findViewById(R.id.ok);
        TextView textSettings = (TextView)materialDialog.findViewById(R.id.settings);


        if (showSettings){
            textOk.setVisibility(View.GONE);
            textSettings.setVisibility(View.VISIBLE);
            TextView textViewPermissionHint = (TextView)materialDialog.findViewById(R.id.textViewPermissionHint);
            textViewPermissionHint.setVisibility(View.VISIBLE);

        }

        textOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                tryRequestPermission();
            }
        });

        textSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse(PACKAGE_URI_PREFIX + getPackageName()));
                startActivity(intent);
            }
        });

        materialDialog.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                materialDialog.dismiss();
                finish();
            }
        });

        materialDialog.show();



    }

    /**
     * Returns true if the redirecting was performed
     */
    private boolean redirectIfNeeded() {
        if (!PermissionUtils.hasRequiredPermissions()) {
            return false;
        }

        redirect();
        return true;
    }

    private void redirect() {
        Intent intent = new Intent(this, BaseActivity.class);
        startActivity(intent);
        finish();
    }
}
