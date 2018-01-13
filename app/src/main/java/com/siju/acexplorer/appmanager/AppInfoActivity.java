package com.siju.acexplorer.appmanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.siju.acexplorer.R;
import com.siju.acexplorer.base.view.BaseActivity;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.SdkHelper;


@SuppressWarnings("ConstantConditions")
public class AppInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final String PACKAGE_NAME           = "package";
    private static final String TAG                    = "AppInfoActivity";
    public static final  int    REQUEST_CODE_UNINSTALL = 1;
    private String  packageName;
    private Button  settingsButton;
    private Button  uninstallButton;
    private Toolbar toolbar;

    public static void openAppInfo(Context context, String packageName) {
        Intent intent = new Intent(context, AppInfoActivity.class);
        intent.putExtra(PACKAGE_NAME, packageName);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_detail);
        getIntentExtras();
        setupUI();
    }

    private void getIntentExtras() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        packageName = intent.getStringExtra(PACKAGE_NAME);

    }

    @SuppressLint("DefaultLocale")
    private void setupUI() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView packageNameText = findViewById(R.id.textPackage);
        TextView versionNameText = findViewById(R.id.textVersionName);
        TextView appNameText = findViewById(R.id.textAppName);
        TextView sourceText = findViewById(R.id.textSource);
        TextView minSdkText = findViewById(R.id.textMinSdk);
        TextView minSdkTextHolder = findViewById(R.id.textMinSdkPlaceHolder);
        TextView targetSdkText = findViewById(R.id.textTargetSdk);
        TextView updatedTimeText = findViewById(R.id.textUpdated);
        TextView installedTimeText = findViewById(R.id.textInstalled);
        TextView permissionText = findViewById(R.id.textPermissions);
        TextView enabledText = findViewById(R.id.textEnabled);
        final ImageView imageIcon = findViewById(R.id.imageAppIcon);

        settingsButton = findViewById(R.id.settingsButton);
        uninstallButton = findViewById(R.id.uninstallButton);
        FloatingActionButton fabStore = findViewById(R.id.fabStore);
        fabStore.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        uninstallButton.setOnClickListener(this);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_apk_green)
                .diskCacheStrategy(DiskCacheStrategy.NONE); // cannot disk cache
        // ApplicationInfo, nor Drawables


        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String source = getPackageManager().getInstallerPackageName(packageName);
            packageNameText.setText(packageName);
            versionNameText.setText(packageInfo.versionName);
            sourceText.setText(getInstallerSource(source));
            updatedTimeText.setText(FileUtils.convertDate(packageInfo.lastUpdateTime));
            installedTimeText.setText(FileUtils.convertDate(packageInfo.firstInstallTime));
            String[] permissions = packageInfo.requestedPermissions;
            StringBuilder permissionList = new StringBuilder();
            for (String permission : permissions) {
                permissionList.append(permission);
                permissionList.append('\n');
            }
            permissionText.setText(permissionList.toString());

            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            boolean enabled = applicationInfo.enabled;
            enabledText.setText(enabled ? getString(R.string.yes) : getString(R.string.no));
            String appName = applicationInfo.loadLabel(getPackageManager()).toString();
            appNameText.setText(appName);
            toolbar.setTitle(appName);


            targetSdkText.setText(String.valueOf(applicationInfo.targetSdkVersion));
            if (SdkHelper.isAtleastNougat()) {
                minSdkText.setText(String.valueOf(applicationInfo.minSdkVersion));
            } else {
                minSdkText.setVisibility(View.GONE);
                minSdkTextHolder.setVisibility(View.GONE);
            }
            Glide.with(this)
                    .as(Drawable.class)
                    .apply(options.dontAnimate().dontTransform().priority(Priority.LOW))
                    .load(applicationInfo)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                            imageIcon.setImageDrawable(drawable);
                            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                            Bitmap bitmap = bitmapDrawable.getBitmap();
                            if (bitmap != null) {
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    public void onGenerated(@NonNull Palette palette) {
                                        applyPalette(palette);
                                    }
                                });
                            }
                        }
                    });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getInstallerSource(String packageName) {
        if (packageName == null) {
            return getString(R.string.unknown);
        } else if (packageName.equals("com.android.vending")) {
            return getString(R.string.play_store);
        }
        return getString(R.string.unknown);
    }

    private void applyPalette(Palette palette) {

        updateBackground((FloatingActionButton) findViewById(R.id.fabStore), palette);
        supportStartPostponedEnterTransition();
    }

    private void updateBackground(FloatingActionButton fab, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.colorAccent));

        fab.setRippleColor(lightVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
        toolbar.setBackgroundColor(vibrantColor);
        settingsButton.setBackgroundColor(vibrantColor);
        uninstallButton.setBackgroundColor(vibrantColor);
        if (SdkHelper.isAtleastLollipop()) {
            getWindow().setStatusBarColor(darkenColor(vibrantColor));
        }

    }

    // Code to darken the color supplied (mostly color of toolbar)
    private static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: resultCode:" + resultCode);
        if (requestCode == REQUEST_CODE_UNINSTALL && resultCode == Activity.RESULT_OK) {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settingsButton:
                AppHelper.openAppSettings(getBaseContext(), packageName);
                break;
            case R.id.uninstallButton:
                AppHelper.uninstallApp(this, packageName);
                break;
            case R.id.fabStore:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.packageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + this.packageName)));
                }
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
