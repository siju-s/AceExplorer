package com.siju.acexplorer.appmanager.view.detail

import android.content.Context
import androidx.annotation.StringRes
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.helper.AndroidVersions
import com.siju.acexplorer.appmanager.model.AppBuildDetails
import com.siju.acexplorer.appmanager.model.AppComponentCounts
import com.siju.acexplorer.appmanager.model.AppDetailInfo
import com.siju.acexplorer.appmanager.model.AppIdentity
import com.siju.acexplorer.appmanager.model.AppInstallDetails
import com.siju.acexplorer.appmanager.model.AppOrigin
import com.siju.acexplorer.appmanager.model.AppSecurityDetails
import com.siju.acexplorer.appmanager.model.AppVersionInfo

private const val NO_SDK_VERSION = 0
private const val SINGLE_SIGNER = 1

/**
 * Builds the rows shown in the app details card.
 *
 * Rows without a value are dropped and empty sections are never emitted, so a package that hides
 * some metadata simply shows fewer rows instead of blanks.
 */
class AppDetailSectionFactory(private val context: Context) {

    fun create(appDetailInfo: AppDetailInfo): List<AppDetailSection> {
        return listOfNotNull(
            identitySection(appDetailInfo.identity),
            installSection(appDetailInfo.installDetails),
            buildSection(appDetailInfo.buildDetails, appDetailInfo.version),
            securitySection(appDetailInfo.securityDetails),
            componentSection(appDetailInfo.componentCounts)
        )
    }

    private fun identitySection(identity: AppIdentity): AppDetailSection? {
        val rows = listOfNotNull(
            rowOf(R.string.package_name, identity.packageName, copyable = true),
            rowOf(R.string.app_uid, identity.uid.toString()),
            rowOf(R.string.app_origin, context.getString(originLabelRes(identity.appOrigin))),
            rowOf(R.string.process_name, processNameOrNull(identity))
        )
        return sectionOf(R.string.app_detail_section_identity, rows)
    }

    private fun installSection(installDetails: AppInstallDetails): AppDetailSection? {
        val rows = listOfNotNull(
            rowOf(R.string.installed_source, installDetails.sourceName),
            rowOf(R.string.installed_by, installDetails.installingSourceName),
            rowOf(R.string.installed, installDetails.installTime),
            rowOf(R.string.updated, installDetails.updatedTime)
        )
        return sectionOf(R.string.app_detail_section_install, rows)
    }

    private fun buildSection(buildDetails: AppBuildDetails, version: AppVersionInfo): AppDetailSection? {
        val rows = listOfNotNull(
            rowOf(com.siju.acexplorer.common.R.string.version_code, version.versionCode.toString()),
            rowOf(R.string.min_sdk, sdkOrNull(buildDetails.minSdk)),
            rowOf(R.string.target_sdk, sdkOrNull(buildDetails.targetSdk)),
            rowOf(R.string.architecture, buildDetails.architecture),
            rowOf(R.string.split_apks, splitCountOrNull(buildDetails.splitCount)),
            rowOf(R.string.apk_path, buildDetails.apkPath, copyable = true),
            rowOf(R.string.data_folder, buildDetails.dataPath, copyable = true)
        )
        return sectionOf(R.string.app_detail_section_build, rows)
    }

    private fun securitySection(securityDetails: AppSecurityDetails): AppDetailSection? {
        val rows = listOfNotNull(
            rowOf(R.string.signing_certificate, securityDetails.signatureDigest, copyable = true),
            rowOf(R.string.signer_count, signerCountOrNull(securityDetails.signerCount)),
            rowOf(R.string.key_rotation, yesOrNo(securityDetails.usesKeyRotation)),
            rowOf(R.string.debuggable, yesOrNo(securityDetails.debuggable)),
            rowOf(R.string.backup_allowed, yesOrNo(securityDetails.backupAllowed)),
            rowOf(R.string.cleartext_traffic, yesOrNo(securityDetails.cleartextTrafficAllowed))
        )
        return sectionOf(R.string.app_detail_section_security, rows)
    }

    /**
     * Component counts are best effort, so the whole section is dropped when nothing was read.
     */
    private fun componentSection(componentCounts: AppComponentCounts): AppDetailSection? {
        if (!hasAnyComponent(componentCounts)) {
            return null
        }
        val rows = listOfNotNull(
            rowOf(R.string.component_activities, componentCounts.activities.toString()),
            rowOf(R.string.component_services, componentCounts.services.toString()),
            rowOf(R.string.component_receivers, componentCounts.receivers.toString()),
            rowOf(R.string.component_providers, componentCounts.providers.toString()),
            rowOf(R.string.component_exported, componentCounts.exported.toString())
        )
        return sectionOf(R.string.app_detail_section_components, rows)
    }

    private fun hasAnyComponent(componentCounts: AppComponentCounts): Boolean {
        return componentCounts.activities > 0 ||
                componentCounts.services > 0 ||
                componentCounts.receivers > 0 ||
                componentCounts.providers > 0
    }

    private fun processNameOrNull(identity: AppIdentity): String? {
        val processName = identity.processName
        val runsInOwnProcess = processName.isEmpty() || processName == identity.packageName
        return if (runsInOwnProcess) null else processName
    }

    private fun sdkOrNull(sdkVersion: Int): String? {
        if (sdkVersion == NO_SDK_VERSION) {
            return null
        }
        return AndroidVersions.describeSdk(context, sdkVersion)
    }

    private fun splitCountOrNull(splitCount: Int): String? {
        if (splitCount == 0) {
            return null
        }
        return splitCount.toString()
    }

    private fun signerCountOrNull(signerCount: Int): String? {
        if (signerCount <= SINGLE_SIGNER) {
            return null
        }
        return signerCount.toString()
    }

    private fun yesOrNo(value: Boolean): String {
        return context.getString(if (value) R.string.yes else R.string.no)
    }

    @StringRes
    private fun originLabelRes(appOrigin: AppOrigin): Int {
        return when (appOrigin) {
            AppOrigin.USER -> R.string.app_origin_user
            AppOrigin.SYSTEM -> R.string.app_origin_system
            AppOrigin.UPDATED_SYSTEM -> R.string.app_origin_updated_system
        }
    }

    private fun rowOf(@StringRes labelRes: Int, value: String?, copyable: Boolean = false): AppDetailRow? {
        if (value.isNullOrBlank()) {
            return null
        }
        return AppDetailRow(labelRes, value, copyable)
    }

    private fun sectionOf(@StringRes titleRes: Int, rows: List<AppDetailRow>): AppDetailSection? {
        if (rows.isEmpty()) {
            return null
        }
        return AppDetailSection(titleRes, rows)
    }
}
