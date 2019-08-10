package com.siju.acexplorer.appmanager.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.appmanager.model.AppDetailDetailModelImpl
import com.siju.acexplorer.appmanager.model.AppInfo
import com.siju.acexplorer.appmanager.model.AppVersionInfo
import com.siju.acexplorer.appmanager.model.PermissionInfo
import com.siju.acexplorer.appmanager.viewmodel.AppDetailViewModel
import com.siju.acexplorer.appmanager.viewmodel.AppDetailViewModelFactory
import com.siju.acexplorer.base.view.BaseActivity
import com.siju.acexplorer.helper.ToolbarHelper
import com.siju.acexplorer.main.model.helper.SdkHelper


private const val URL_STORE = "https://play.google" + ".com/store/apps/details?id="
private const val EXTRA_PACKAGE_NAME = "package"

class AppDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var toolbar: Toolbar
    private lateinit var settingsButton: Button
    private lateinit var uninstallButton: Button
    private lateinit var packageNameText: TextView
    private lateinit var versionNameText: TextView
    private lateinit var appNameText: TextView
    private lateinit var sourceText: TextView
    private lateinit var minSdkText: TextView
    private lateinit var minSdkTextHolder: TextView
    private lateinit var targetSdkText: TextView
    private lateinit var versionCodeText: TextView
    private lateinit var updatedTimeText: TextView
    private lateinit var installedTimeText: TextView
    private lateinit var permissionText: TextView
    private lateinit var permissionHolderText: TextView
    private lateinit var enabledText: TextView
    private lateinit var imageIcon: ImageView
    private lateinit var fabStore: FloatingActionButton
    private lateinit var viewModel: AppDetailViewModel

    private lateinit var packageValue : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_detail_ui)

        setupUI()
        setupViewModels()
        initListeners()
        initObservers()
        setupData(getPackage())
    }


    private fun getPackage(): String? {
        if (intent == null || intent.getStringExtra(
                        EXTRA_PACKAGE_NAME) == null) {
            finish()
        }
        packageValue = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        return packageValue
    }

    private fun setupUI() {
        setupToolbar()
        findViewsById()
        initListeners()
    }

    private fun setupData(packageName: String?) {
        viewModel.fetchPackageInfo(packageName)
    }

    private fun setupViewModels() {
        val viewModelFactory = AppDetailViewModelFactory(
                AppDetailDetailModelImpl(AceApplication.appContext))
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(AppDetailViewModel::class.java)
    }

    private fun initObservers() {
        viewModel.versionInfo.observe(this, Observer {
            it?.apply {
                setupVersionInfo(it)
            }
        })

        viewModel.permissionInfo.observe(this, Observer {
            it?.apply {
                setupPermissionData(it)
            }
        })

        viewModel.appInfo.observe(this, Observer {
            it?.apply {
                setupAppProperties(it)
            }
        })
    }

    private fun setupVersionInfo(versionInfo: AppVersionInfo) {
        versionNameText.text = versionInfo.versionName
        versionCodeText.text = versionInfo.versionCode.toString()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        ToolbarHelper.setToolbarTitle(this, getString(R.string.app_detail))
        ToolbarHelper.showToolbarAsUp(this)
    }

    private fun findViewsById() {
        packageNameText = findViewById(R.id.textPackage)
        versionNameText = findViewById(R.id.textVersionName)
        appNameText = findViewById(R.id.textAppName)
        sourceText = findViewById(R.id.textSource)
        minSdkText = findViewById(R.id.textMinSdk)
        minSdkTextHolder = findViewById(R.id.textMinSdkPlaceHolder)
        targetSdkText = findViewById(R.id.textTargetSdk)
        updatedTimeText = findViewById(R.id.textUpdated)
        versionCodeText = findViewById(R.id.textVersionCode)
        installedTimeText = findViewById(R.id.textInstalled)
        permissionText = findViewById(R.id.textPermissions)
        permissionHolderText = findViewById(R.id.textPermissionPlaceholder)
        enabledText = findViewById(R.id.textEnabled)
        imageIcon = findViewById(R.id.imageAppIcon)
        settingsButton = findViewById(R.id.settingsButton)
        uninstallButton = findViewById(R.id.uninstallButton)
        fabStore = findViewById(R.id.fabStore)
    }

    private fun initListeners() {
        fabStore.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        uninstallButton.setOnClickListener(this)
    }

    private fun setupPermissionData(permissionInfo: PermissionInfo) {
        val permissions = permissionInfo.permissions
        if (permissions == null) {
            permissionText.visibility = View.GONE
            permissionHolderText.visibility = View.GONE
        }
        else {
            val permissionList = StringBuilder()
            for (permission in permissions) {
                permissionList.append(permission)
                permissionList.append('\n')
            }
            permissionText.text = permissionList.toString()
        }
    }

    private fun setupAppProperties(appInfo: AppInfo) {
        val enabled = appInfo.enabled
        enabledText.text = if (enabled) getString(R.string.yes)
        else getString(R.string.no)
        appNameText.text = appInfo.appName
        toolbar.title = appInfo.appName
        imageIcon.contentDescription = appInfo.appName

        targetSdkText.text = appInfo.targetSdk.toString()
        val minSdk = appInfo.minSdk
        if (minSdk == 0) {
            minSdkText.visibility = View.GONE
            minSdkTextHolder.visibility = View.GONE
        }
        else {
            minSdkText.text = appInfo.minSdk.toString()
        }
        setupAppIcon(appInfo.packageName)
        sourceText.text = appInfo.source
        updatedTimeText.text = appInfo.updatedTime
        installedTimeText.text = appInfo.installTime
    }

    private fun setupAppIcon(packageName: String) {
        val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_apk_green)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // cannot disk cache

        Glide.with(this)
                .`as`(Drawable::class.java)
                .apply(options.dontAnimate().dontTransform().priority(Priority.LOW))
                .load(packageName)
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(drawable: Drawable,
                                                 transition: Transition<in Drawable>?) {
                        imageIcon.setImageDrawable(drawable)
                        val bitmap = getBitmapFromDrawable(drawable)
                        if (bitmap != null) {
                            Palette.from(bitmap).generate { palette -> applyPalette(palette) }
                        }
                    }
                })
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            bitmap = drawable.bitmap
        }
        else if (SdkHelper.isAtleastOreo && drawable is AdaptiveIconDrawable) {
            bitmap = Bitmap
                    .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                                  Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }

    private fun applyPalette(palette: Palette?) {
        if (palette != null) {
            updateBackground(fabStore, palette)
            supportStartPostponedEnterTransition()
        }
    }

    private fun updateBackground(fab: FloatingActionButton, palette: Palette) {
        val lightVibrantColor = palette.getLightVibrantColor(
                ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        val vibrantColor = palette.getVibrantColor(
                ContextCompat.getColor(applicationContext, R.color.colorAccent))

        fab.rippleColor = lightVibrantColor
        fab.backgroundTintList = ColorStateList.valueOf(vibrantColor)
        settingsButton.setBackgroundColor(vibrantColor)
        uninstallButton.setBackgroundColor(vibrantColor)
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark)
    }

    public override fun onResume() {
        super.onResume()
        if (AppHelper.isPackageNotExisting(AceApplication.appContext, packageValue)) {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UNINSTALL && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.settingsButton -> AppHelper.openAppSettings(this, packageValue)
            R.id.uninstallButton -> AppHelper.uninstallApp(this, packageValue)
            R.id.fabStore -> try {
                startActivity(Intent(Intent.ACTION_VIEW,
                                     Uri.parse(
                                             "market://details?id=" + this.packageValue)))
            }
            catch (exception: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                        URL_STORE + this.packageValue)))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        const val REQUEST_CODE_UNINSTALL = 1

        fun openAppInfo(context: Context, packageName: String) {
            val intent = Intent(context, AppDetailActivity::class.java)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            context.startActivity(intent)
        }
    }
}
