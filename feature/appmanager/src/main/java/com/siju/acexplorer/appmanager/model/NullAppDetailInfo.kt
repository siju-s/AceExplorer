package com.siju.acexplorer.appmanager.model

/**
 * Null object used when a package exists but its [android.content.pm.ApplicationInfo] is missing,
 * so the screen renders empty rows instead of crashing on a null detail.
 */
val NULL_APP_DETAIL_INFO = AppDetailInfo(
    identity = AppIdentity(
        packageName = "",
        appName = "",
        enabled = false,
        uid = 0,
        processName = "",
        appOrigin = AppOrigin.USER
    ),
    version = AppVersionInfo(versionName = null, versionCode = 0),
    installDetails = AppInstallDetails(
        sourceName = "",
        initiatingPackage = null,
        installingSourceName = null,
        installTime = "",
        updatedTime = ""
    ),
    buildDetails = AppBuildDetails(
        minSdk = 0,
        targetSdk = 0,
        architecture = null,
        splitCount = 0,
        apkPath = "",
        dataPath = ""
    ),
    securityDetails = AppSecurityDetails(
        signatureDigest = null,
        signerCount = 0,
        usesKeyRotation = false,
        debuggable = false,
        backupAllowed = false,
        cleartextTrafficAllowed = false
    ),
    componentCounts = AppComponentCounts(
        activities = 0,
        services = 0,
        receivers = 0,
        providers = 0,
        exported = 0
    ),
    apkSize = ""
)
