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
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
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
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.model.helper.SdkHelper;


@SuppressWarnings("ConstantConditions")
public class AppInfoActivity extends BaseActivity implements View.OnClickListener {

    public static final  int                  REQUEST_CODE_UNINSTALL = 1;
    public static final  String               URL_STORE              = "https://play.google.com/store/apps/details?id=";
    private static final String               PACKAGE_NAME           = "package";
    public static final String PACKAGE_NAME_PLAYSTORE = "com.android.vending";
    public static final String PACKAGE_NAME_AMAZON_APPSTORE = "com.amazon.venezia";
    private              Toolbar              toolbar;
    private              Button               settingsButton;
    private              Button               uninstallButton;
    private              TextView             packageNameText;
    private              TextView             versionNameText;
    private              TextView             appNameText;
    private              TextView             sourceText;
    private              TextView             minSdkText;
    private              TextView             minSdkTextHolder;
    private              TextView             targetSdkText;
    private              TextView             updatedTimeText;
    private              TextView             installedTimeText;
    private              TextView             permissionText;
    private              TextView             permissionHolderText;
    private              TextView             enabledText;
    private              ImageView            imageIcon;
    private              FloatingActionButton fabStore;

    private String packageName;

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
        setupData();
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
        setupToolbar();
        findViewsById();
        initListeners();
    }

    private void setupData() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String source = getPackageManager().getInstallerPackageName(packageName);
            setupData(packageInfo, source);
        }
        catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        packageNameText = findViewById(R.id.textPackage);
        versionNameText = findViewById(R.id.textVersionName);
        appNameText = findViewById(R.id.textAppName);
        sourceText = findViewById(R.id.textSource);
        minSdkText = findViewById(R.id.textMinSdk);
        minSdkTextHolder = findViewById(R.id.textMinSdkPlaceHolder);
        targetSdkText = findViewById(R.id.textTargetSdk);
        updatedTimeText = findViewById(R.id.textUpdated);
        installedTimeText = findViewById(R.id.textInstalled);
        permissionText = findViewById(R.id.textPermissions);
        permissionHolderText = findViewById(R.id.textPermissionPlaceholder);
        enabledText = findViewById(R.id.textEnabled);
        imageIcon = findViewById(R.id.imageAppIcon);
        settingsButton = findViewById(R.id.settingsButton);
        uninstallButton = findViewById(R.id.uninstallButton);
        fabStore = findViewById(R.id.fabStore);
    }

    private void initListeners() {
        fabStore.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        uninstallButton.setOnClickListener(this);
    }

    private void setupData(PackageInfo packageInfo, String source) {
        setPackageInfo(packageInfo, source);
        setupPermissionData(packageInfo);
        setupAppProperties(packageInfo);
    }

    private void setPackageInfo(PackageInfo packageInfo, String source) {
        packageNameText.setText(packageName);
        versionNameText.setText(packageInfo.versionName);
        sourceText.setText(getInstallerSource(source));
        updatedTimeText.setText(FileUtils.convertDate(packageInfo.lastUpdateTime));
        installedTimeText.setText(FileUtils.convertDate(packageInfo.firstInstallTime));
    }

    private void setupPermissionData(PackageInfo packageInfo) {
        String[] permissions = packageInfo.requestedPermissions;
        if (permissions == null) {
            permissionText.setVisibility(View.GONE);
            permissionHolderText.setVisibility(View.GONE);
        } else {
            StringBuilder permissionList = new StringBuilder();

            for (String permission : permissions) {
                permissionList.append(permission);
                permissionList.append('\n');
            }
            permissionText.setText(permissionList.toString());
        }
    }

    private void setupAppProperties(PackageInfo packageInfo) {
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        boolean enabled = applicationInfo.enabled;
        enabledText.setText(enabled ? getString(R.string.yes) : getString(R.string.no));
        String appName = applicationInfo.loadLabel(getPackageManager()).toString();
        appNameText.setText(appName);
        toolbar.setTitle(appName);
        imageIcon.setContentDescription(appName);

        targetSdkText.setText(String.valueOf(applicationInfo.targetSdkVersion));
        if (SdkHelper.isAtleastNougat()) {
            minSdkText.setText(String.valueOf(applicationInfo.minSdkVersion));
        } else {
            minSdkText.setVisibility(View.GONE);
            minSdkTextHolder.setVisibility(View.GONE);
        }
        setupAppIcon();
    }

    private String getInstallerSource(String packageName) {
        if (packageName == null) {
            return getString(R.string.unknown);
        } else if (packageName.equals(PACKAGE_NAME_PLAYSTORE)) {
            return getString(R.string.play_store);
        } else if (packageName.equals(PACKAGE_NAME_AMAZON_APPSTORE)) {
            return getString(R.string.amazon_play_store);
        }
        return getString(R.string.unknown);
    }

    private void setupAppIcon() {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_apk_green)
                .diskCacheStrategy(DiskCacheStrategy.NONE); // cannot disk cache

        Glide.with(this)
             .as(Drawable.class)
             .apply(options.dontAnimate().dontTransform().priority(Priority.LOW))
             .load(packageName)
             .into(new SimpleTarget<Drawable>() {
                 @Override
                 public void onResourceReady(@NonNull Drawable drawable,
                                             @Nullable Transition<? super Drawable> transition)
                 {
                     imageIcon.setImageDrawable(drawable);
                     Bitmap bitmap = getBitmapFromDrawable(drawable);
                     if (bitmap != null) {
                         Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                             public void onGenerated(Palette palette) {
                                 applyPalette(palette);
                             }
                         });
                     }
                 }
             });
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (SdkHelper.isAtleastOreo() && drawable instanceof AdaptiveIconDrawable) {
            bitmap = Bitmap
                    .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }

    private void applyPalette(Palette palette) {
        if (palette != null) {
            updateBackground(fabStore, palette);
            supportStartPostponedEnterTransition();
        }
    }

    private void updateBackground(FloatingActionButton fab, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(R.color.colorPrimary));
        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.colorAccent));

        fab.setRippleColor(lightVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
        settingsButton.setBackgroundColor(vibrantColor);
        uninstallButton.setBackgroundColor(vibrantColor);
        if (SdkHelper.isAtleastLollipop()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UNINSTALL && resultCode == Activity.RESULT_OK) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppHelper.isPackageNotExisting(getApplicationContext(), packageName)) {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settingsButton:
                AppHelper.openAppSettings(this, packageName);
                break;
            case R.id.uninstallButton:
                AppHelper.uninstallApp(this, packageName);
                break;
            case R.id.fabStore:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.packageName)));
                }
                catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            URL_STORE + this.packageName)));
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
