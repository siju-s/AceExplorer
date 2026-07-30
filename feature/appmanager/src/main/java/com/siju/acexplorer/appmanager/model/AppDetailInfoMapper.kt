package com.siju.acexplorer.appmanager.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ComponentInfo
import android.content.pm.PackageInfo
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.extensions.getInstallerPackage
import com.siju.acexplorer.appmanager.extensions.getInstallingPackage
import com.siju.acexplorer.appmanager.helper.InstallTimes
import com.siju.acexplorer.common.utils.DateUtils
import com.siju.acexplorer.common.utils.SdkHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Turns the raw [PackageInfo] the platform gives us into the grouped detail model the screen
 * renders. Kept apart from the model that owns the LiveData so that fetching and mapping can
 * change independently.
 */
class AppDetailInfoMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signatureReader: AppSignatureReader,
    private val appSizeCalculator: AppSizeCalculator,
    private val installSourceResolver: InstallSourceResolver
) {

    fun map(packageInfo: PackageInfo): AppDetailInfo {
        val applicationInfo = packageInfo.applicationInfo ?: return NULL_APP_DETAIL_INFO

        return AppDetailInfo(
            identity = createIdentity(packageInfo, applicationInfo),
            version = createVersionInfo(packageInfo),
            installDetails = createInstallDetails(packageInfo),
            buildDetails = createBuildDetails(applicationInfo),
            securityDetails = createSecurityDetails(packageInfo, applicationInfo),
            componentCounts = createComponentCounts(packageInfo),
            apkSize = appSizeCalculator.formattedApkSize(applicationInfo)
        )
    }

    private fun createVersionInfo(packageInfo: PackageInfo): AppVersionInfo {
        return AppVersionInfo(packageInfo.versionName, versionCodeOf(packageInfo))
    }

    private fun createIdentity(packageInfo: PackageInfo, applicationInfo: ApplicationInfo): AppIdentity {
        return AppIdentity(
            packageName = packageInfo.packageName,
            appName = applicationInfo.loadLabel(context.packageManager).toString(),
            enabled = applicationInfo.enabled,
            uid = applicationInfo.uid,
            processName = applicationInfo.processName.orEmpty(),
            appOrigin = originOf(applicationInfo)
        )
    }

    private fun createInstallDetails(packageInfo: PackageInfo): AppInstallDetails {
        val packageName = packageInfo.packageName
        val initiatingPackage = context.packageManager.getInstallerPackage(packageName)
        val installingPackage = context.packageManager.getInstallingPackage(packageName)

        return AppInstallDetails(
            sourceName = installSourceResolver.resolveName(initiatingPackage),
            initiatingPackage = initiatingPackage,
            installingSourceName = installingSourceName(initiatingPackage, installingPackage),
            installTime = formattedDateOrEmpty(packageInfo.firstInstallTime),
            updatedTime = formattedDateOrEmpty(packageInfo.lastUpdateTime)
        )
    }

    /** Times the platform cannot really know are left empty so the row disappears. */
    private fun formattedDateOrEmpty(timeInMillis: Long): String {
        if (!InstallTimes.isRealInstallTime(timeInMillis)) {
            return ""
        }
        return DateUtils.convertDate(timeInMillis)
    }

    /**
     * Only worth showing when the app was handed over to a different installer, otherwise it just
     * repeats the source row.
     */
    private fun installingSourceName(initiatingPackage: String?, installingPackage: String?): String? {
        if (installingPackage == null || installingPackage == initiatingPackage) {
            return null
        }
        return installSourceResolver.resolveName(installingPackage)
    }

    private fun createBuildDetails(applicationInfo: ApplicationInfo): AppBuildDetails {
        return AppBuildDetails(
            minSdk = minSdkOf(applicationInfo),
            targetSdk = applicationInfo.targetSdkVersion,
            architecture = architectureLabel(applicationInfo),
            splitCount = applicationInfo.splitSourceDirs.orEmpty().size,
            apkPath = applicationInfo.sourceDir.orEmpty(),
            dataPath = applicationInfo.dataDir.orEmpty()
        )
    }

    private fun architectureLabel(applicationInfo: ApplicationInfo): String? {
        val architecture = AppArchitecture.of(applicationInfo) ?: return null
        return context.getString(
            R.string.architecture_format,
            context.getString(architecture.bitnessRes),
            architecture.abiName
        )
    }

    @Suppress("DEPRECATION")
    private fun createSecurityDetails(
        packageInfo: PackageInfo,
        applicationInfo: ApplicationInfo
    ): AppSecurityDetails {
        val signature = signatureReader.read(packageInfo)

        return AppSecurityDetails(
            signatureDigest = signature.digest,
            signerCount = signature.signerCount,
            usesKeyRotation = signature.usesKeyRotation,
            debuggable = applicationInfo.hasFlag(ApplicationInfo.FLAG_DEBUGGABLE),
            backupAllowed = applicationInfo.hasFlag(ApplicationInfo.FLAG_ALLOW_BACKUP),
            cleartextTrafficAllowed = applicationInfo.hasFlag(ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC)
        )
    }

    private fun createComponentCounts(packageInfo: PackageInfo): AppComponentCounts {
        val activities = packageInfo.activities.orEmpty()
        val services = packageInfo.services.orEmpty()
        val receivers = packageInfo.receivers.orEmpty()
        val providers = packageInfo.providers.orEmpty()

        val allComponents = buildList<ComponentInfo> {
            addAll(activities)
            addAll(services)
            addAll(receivers)
            addAll(providers)
        }

        return AppComponentCounts(
            activities = activities.size,
            services = services.size,
            receivers = receivers.size,
            providers = providers.size,
            exported = allComponents.count { component -> component.exported }
        )
    }

    private fun originOf(applicationInfo: ApplicationInfo): AppOrigin {
        return when {
            applicationInfo.hasFlag(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) -> AppOrigin.UPDATED_SYSTEM
            applicationInfo.hasFlag(ApplicationInfo.FLAG_SYSTEM) -> AppOrigin.SYSTEM
            else -> AppOrigin.USER
        }
    }

    private fun minSdkOf(applicationInfo: ApplicationInfo): Int {
        return if (SdkHelper.isAtleastNougat) {
            applicationInfo.minSdkVersion
        }
        else {
            0
        }
    }

    @Suppress("DEPRECATION")
    private fun versionCodeOf(packageInfo: PackageInfo): Long {
        return if (SdkHelper.isAtleastPie) {
            packageInfo.longVersionCode
        }
        else {
            packageInfo.versionCode.toLong()
        }
    }

    private fun ApplicationInfo.hasFlag(flag: Int): Boolean = flags and flag != 0
}
