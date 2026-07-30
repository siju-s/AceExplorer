package com.siju.acexplorer.appmanager.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.helper.AppHelper
import com.siju.acexplorer.appmanager.model.AppDetailInfo
import com.siju.acexplorer.appmanager.model.AppOrigin
import com.siju.acexplorer.appmanager.model.AppVersionInfo
import com.siju.acexplorer.appmanager.model.PermissionInfo
import com.siju.acexplorer.appmanager.permissions.PermissionGroup
import com.siju.acexplorer.appmanager.store.AppStore
import com.siju.acexplorer.appmanager.store.AppStoreLauncher
import com.siju.acexplorer.appmanager.store.StoreLinkTarget
import com.siju.acexplorer.appmanager.view.detail.AppDetailSectionFactory
import com.siju.acexplorer.appmanager.view.detail.AppDetailSectionRenderer
import com.siju.acexplorer.appmanager.viewmodel.AppDetailViewModel
import com.siju.acexplorer.common.utils.ToolbarHelper
import com.siju.acexplorer.common.view.EdgeToEdgeActivity
import dagger.hilt.android.AndroidEntryPoint

private const val EXTRA_PACKAGE_NAME = "packageName"
private const val PERMISSION_GROUP_SEPARATOR = " • "

@AndroidEntryPoint
class AppDetailActivity : EdgeToEdgeActivity() {

    private val viewModel: AppDetailViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var settingsButton: Button
    private lateinit var uninstallButton: Button
    private lateinit var storeButton: Button
    private lateinit var versionNameText: TextView
    private lateinit var appNameText: TextView
    private lateinit var permissionText: TextView
    private lateinit var permissionSummaryText: TextView
    private lateinit var enabledText: TextView
    private lateinit var appSizeText: TextView
    private lateinit var imageIcon: ImageView
    private lateinit var technicalDetailsButton: Button
    private lateinit var technicalDetailsContainer: LinearLayout
    private lateinit var sectionRenderer: AppDetailSectionRenderer

    private val sectionFactory by lazy { AppDetailSectionFactory(this) }

    private var packageValue: String? = null
    private var appStore: AppStore = AppStore.PLAY_STORE
    private var storeLinkTarget: StoreLinkTarget = StoreLinkTarget.LISTING

    private val uninstallResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_detail_ui)
        handleWindowInsets()

        setupUI()
        initObservers()
        setupData(getPackage())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == AppHelper.getUninstallAction()) {
            val extras = intent.extras
            val status = extras?.getInt(PackageInstaller.EXTRA_STATUS, 0)
            if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                val confirmIntent = extras.get(Intent.EXTRA_INTENT) as Intent
                startActivity(confirmIntent)
            }
        }
    }

    private fun getPackage(): String? {
        if (intent == null || intent.getStringExtra(
                EXTRA_PACKAGE_NAME
            ) == null
        ) {
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

    private fun handleWindowInsets() {
        val root = findViewById<View>(R.id.appDetailRoot)
        val appBar = findViewById<View>(R.id.appDetailAppBar)
        val initialLeft = root.paddingLeft
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom
        val initialTop = appBar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(
                left = initialLeft + systemBars.left,
                right = initialRight + systemBars.right,
                bottom = initialBottom + systemBars.bottom
            )
            appBar.updatePadding(top = initialTop + systemBars.top)
            windowInsets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun setupData(packageName: String?) {
        viewModel.fetchPackageInfo(packageName)
    }

    private fun initObservers() {
        viewModel.permissionInfo.observe(this) { permissionInfo ->
            permissionInfo?.let(::setupPermissionData)
        }

        viewModel.appDetailInfo.observe(this) { appDetailInfo ->
            appDetailInfo?.let(::setupAppProperties)
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(com.siju.acexplorer.common.R.id.toolbar)
        setSupportActionBar(toolbar)
        ToolbarHelper.setToolbarTitle(this, getString(R.string.app_detail))
        ToolbarHelper.showToolbarAsUp(this)
    }

    private fun findViewsById() {
        versionNameText = findViewById(R.id.textVersionName)
        appNameText = findViewById(R.id.textAppName)
        permissionText = findViewById(R.id.textPermissions)
        permissionSummaryText = findViewById(R.id.textPermissionSummary)
        enabledText = findViewById(R.id.textEnabled)
        appSizeText = findViewById(R.id.textAppSize)
        imageIcon = findViewById(R.id.imageAppIcon)
        settingsButton = findViewById(R.id.settingsButton)
        uninstallButton = findViewById(R.id.uninstallButton)
        storeButton = findViewById(R.id.storeButton)
        technicalDetailsButton = findViewById(R.id.technicalDetailsButton)
        technicalDetailsContainer = findViewById(R.id.technicalDetailsContainer)
        sectionRenderer = AppDetailSectionRenderer(findViewById(R.id.appDetailSections))
    }

    private fun initListeners() {
        settingsButton.setOnClickListener { packageValue?.let { AppHelper.openAppSettings(this, it) } }
        uninstallButton.setOnClickListener {
            packageValue?.let { AppHelper.uninstallApp(this, it, uninstallResultLauncher) }
        }
        storeButton.setOnClickListener {
            packageValue?.let { AppStoreLauncher.openStoreListing(this, appStore, it, storeLinkTarget) }
        }
        technicalDetailsButton.setOnClickListener { toggleTechnicalDetails() }
    }

    private fun setupPermissionData(permissionInfo: PermissionInfo) {
        val permissions = permissionInfo.permissions
        if (permissions.isNullOrEmpty()) {
            permissionText.visibility = View.GONE
            permissionSummaryText.text = getString(R.string.no_permissions_requested)
        }
        else {
            permissionSummaryText.text = getString(R.string.permissions_requested, permissions.size)
            permissionText.text = permissions
                .map(PermissionGroup::of)
                .distinct()
                .sorted()
                .joinToString(PERMISSION_GROUP_SEPARATOR) { group -> getString(group.labelRes) }
        }
    }

    private fun setupAppProperties(appDetailInfo: AppDetailInfo) {
        val identity = appDetailInfo.identity

        enabledText.text = if (identity.enabled) getString(R.string.app_enabled)
        else getString(R.string.app_disabled)
        appNameText.text = identity.appName
        toolbar.title = identity.appName
        imageIcon.contentDescription = identity.appName

        setupVersionInfo(appDetailInfo.version)
        setupAppIcon(identity.packageName)
        setupStoreButton(appDetailInfo)
        appSizeText.text = appDetailInfo.apkSize
        sectionRenderer.render(sectionFactory.create(appDetailInfo))
    }

    private fun setupVersionInfo(versionInfo: AppVersionInfo) {
        val versionName = versionInfo.versionName
        versionNameText.text = if (versionName == null) {
            getString(R.string.version_unknown)
        }
        else {
            getString(R.string.app_version_with_code, versionName, versionInfo.versionCode)
        }
    }

    /**
     * Preinstalled apps have no store listing, so the button is only offered for installed apps.
     *
     * A known installer means the app really is on that store, so we open its listing directly.
     * A sideloaded app might not be listed anywhere, and a listing link for an unknown package
     * lands on an "item not found" page, so we search the Play Store instead and say "Find".
     */
    private fun setupStoreButton(appDetailInfo: AppDetailInfo) {
        if (appDetailInfo.identity.appOrigin == AppOrigin.SYSTEM) {
            storeButton.isVisible = false
            return
        }

        val installerPackage = appDetailInfo.installDetails.initiatingPackage
        val installedFromKnownStore = AppStore.isKnownInstaller(installerPackage)

        appStore = AppStore.forInstaller(installerPackage)
        storeLinkTarget = if (installedFromKnownStore) StoreLinkTarget.LISTING else StoreLinkTarget.SEARCH

        val labelRes = if (installedFromKnownStore) R.string.open_in_store else R.string.find_in_store
        storeButton.text = getString(labelRes, getString(appStore.labelRes))
        storeButton.isVisible = true
    }

    private fun setupAppIcon(packageName: String) {
        val options = RequestOptions()
            .centerCrop()
            .placeholder(com.siju.acexplorer.common.R.drawable.ic_apk_green)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // cannot disk cache

        Glide.with(this)
            .`as`(Drawable::class.java)
            .apply(options.dontAnimate().dontTransform().priority(Priority.LOW))
            .load(packageName)
            .into(object : CustomTarget<Drawable>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    imageIcon.setImageDrawable(placeholder)
                }

                override fun onResourceReady(
                    drawable: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    imageIcon.setImageDrawable(drawable)
                }
            })
    }

    public override fun onResume() {
        super.onResume()
        if (AppHelper.isPackageNotExisting(applicationContext, packageValue)) {
            finish()
        }
    }

    private fun toggleTechnicalDetails() {
        val showDetails = !technicalDetailsContainer.isVisible
        technicalDetailsContainer.isVisible = showDetails
        technicalDetailsButton.text = getString(
            if (showDetails) R.string.hide_app_details else R.string.show_app_details
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun openAppInfo(context: Context, packageName: String?) {
            packageName ?: return
            val intent = Intent(context, AppDetailActivity::class.java)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            context.startActivity(intent)
        }
    }
}
