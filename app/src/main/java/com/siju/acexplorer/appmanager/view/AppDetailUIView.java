package com.siju.acexplorer.appmanager.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.appmanager.helper.AppHelper;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.model.helper.SdkHelper;

public class AppDetailUIView extends CoordinatorLayout implements AppDetailUi, View.OnClickListener {
    public static final String               URL_STORE = "https://play.google" +
                                                         ".com/store/apps/details?id=";
    private             AppCompatActivity    activity;
    private             Toolbar              toolbar;
    private             Button               settingsButton;
    private             Button               uninstallButton;
    private             TextView             packageNameText;
    private             TextView             versionNameText;
    private             TextView             appNameText;
    private             TextView             sourceText;
    private             TextView             minSdkText;
    private             TextView             minSdkTextHolder;
    private             TextView             targetSdkText;
    private             TextView             versionCodeText;
    private             TextView             updatedTimeText;
    private             TextView             installedTimeText;
    private             TextView             permissionText;
    private             TextView             permissionHolderText;
    private             TextView             enabledText;
    private             ImageView            imageIcon;
    private             FloatingActionButton fabStore;
    private             Listener             listener;

    private String packageName;


    public AppDetailUIView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void inflateView() {
        LayoutInflater.from(getContext()).inflate(R.layout.app_detail_ui, this, true);
        getIntentExtras();
        setupUI();
        setupData();
    }

    private void getIntentExtras() {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return;
        }
        packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
    }

    @SuppressLint("DefaultLocale")
    private void setupUI() {
        setupToolbar();
        findViewsById();
        initListeners();
    }

    private void setupData() {
        Object packageInfo = listener.getPackageInfo(packageName);
        if (packageInfo != null) {
            String source = listener.getInstallerSource(packageName);
            setupData((PackageInfo) packageInfo, source);
        }
    }

    public AppCompatActivity getActivity() {
        return activity;
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getContext().getString(R.string.app_detail));
        getActivity().setSupportActionBar(toolbar);
        getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        versionCodeText = findViewById(R.id.textVersionCode);
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
        if (SdkHelper.INSTANCE.isAtleastPie()) {
            versionCodeText.setText(String.valueOf(packageInfo.getLongVersionCode()));
        }
        else {
            versionCodeText.setText(String.valueOf(packageInfo.versionCode));
        }
        sourceText.setText(source);
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
        enabledText.setText(enabled ? getContext().getString(R.string.yes) : getContext().getString(R.string.no));
        String appName = applicationInfo.loadLabel(getActivity().getPackageManager()).toString();
        appNameText.setText(appName);
        toolbar.setTitle(appName);
        imageIcon.setContentDescription(appName);

        targetSdkText.setText(String.valueOf(applicationInfo.targetSdkVersion));
        if (SdkHelper.INSTANCE.isAtleastNougat()) {
            minSdkText.setText(String.valueOf(applicationInfo.minSdkVersion));
        } else {
            minSdkText.setVisibility(View.GONE);
            minSdkTextHolder.setVisibility(View.GONE);
        }
        setupAppIcon();
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
        } else if (SdkHelper.INSTANCE.isAtleastOreo() && drawable instanceof AdaptiveIconDrawable) {
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
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    private void updateBackground(FloatingActionButton fab, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(R.color.colorPrimary));
        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.colorAccent));

        fab.setRippleColor(lightVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
        settingsButton.setBackgroundColor(vibrantColor);
        uninstallButton.setBackgroundColor(vibrantColor);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onResume() {
        if (AppHelper.isPackageNotExisting(AceApplication.Companion.getAppContext(), packageName)) {
            getActivity().finish();
        }
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_UNINSTALL && resultCode == Activity.RESULT_OK) {
            getActivity().finish();
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settingsButton:
                AppHelper.openAppSettings(getContext(), packageName);
                break;
            case R.id.uninstallButton:
                AppHelper.uninstallApp(getActivity(), packageName);
                break;
            case R.id.fabStore:
                try {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                                                           Uri.parse("market://details?id=" + this.packageName)));
                }
                catch (android.content.ActivityNotFoundException anfe) {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            URL_STORE + this.packageName)));
                }
                break;

        }
    }
}
