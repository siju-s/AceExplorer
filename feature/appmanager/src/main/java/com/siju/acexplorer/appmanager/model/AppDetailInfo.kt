package com.siju.acexplorer.appmanager.model

/**
 * Everything the app detail screen shows about a single installed package.
 *
 * The details are grouped the same way they are rendered, so a new row only touches the group it
 * belongs to instead of a flat constructor that keeps growing.
 */
data class AppDetailInfo(
    val identity: AppIdentity,
    val version: AppVersionInfo,
    val installDetails: AppInstallDetails,
    val buildDetails: AppBuildDetails,
    val securityDetails: AppSecurityDetails,
    val componentCounts: AppComponentCounts,
    val apkSize: String
)

/** Who the app is: name, package and the identity the system runs it under. */
data class AppIdentity(
    val packageName: String,
    val appName: String,
    val enabled: Boolean,
    val uid: Int,
    val processName: String,
    val appOrigin: AppOrigin
)

/** How the app got onto the device and when. */
data class AppInstallDetails(
    val sourceName: String,
    val initiatingPackage: String?,
    val installingSourceName: String?,
    val installTime: String,
    val updatedTime: String
)

/** How the app was built and packaged. */
data class AppBuildDetails(
    val minSdk: Int,
    val targetSdk: Int,
    val architecture: String?,
    val splitCount: Int,
    val apkPath: String,
    val dataPath: String
)

/** Signing identity and the manifest flags worth knowing about. */
data class AppSecurityDetails(
    val signatureDigest: String?,
    val signerCount: Int,
    val usesKeyRotation: Boolean,
    val debuggable: Boolean,
    val backupAllowed: Boolean,
    val cleartextTrafficAllowed: Boolean
)

/** Declared component counts, plus how many of them any other app can reach. */
data class AppComponentCounts(
    val activities: Int,
    val services: Int,
    val receivers: Int,
    val providers: Int,
    val exported: Int
)

enum class AppOrigin {
    USER,
    SYSTEM,
    UPDATED_SYSTEM
}
