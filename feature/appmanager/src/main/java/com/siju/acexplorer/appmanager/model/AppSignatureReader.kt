package com.siju.acexplorer.appmanager.model

import android.content.pm.PackageInfo
import android.content.pm.Signature
import android.content.pm.SigningInfo
import com.siju.acexplorer.common.utils.SdkHelper
import java.security.MessageDigest
import javax.inject.Inject

private const val DIGEST_ALGORITHM = "SHA-256"
private const val HEX_FORMAT = "%02X"
private const val HEX_SEPARATOR = ":"

/**
 * Reads the signing identity of a package.
 *
 * Android does not expose which APK signature scheme (v1/v2/v3) was used, so this reports only
 * what the platform actually tells us: the certificate digest, how many signers there are and
 * whether the signing key has been rotated.
 */
class AppSignatureReader @Inject constructor() {

    fun read(packageInfo: PackageInfo): SignatureReadResult {
        val signingInfo = signingInfoOrNull(packageInfo) ?: return SignatureReadResult.UNAVAILABLE
        val signers = signingInfo.apkContentsSigners.orEmpty()
        val primarySigner = signers.firstOrNull() ?: return SignatureReadResult.UNAVAILABLE

        return SignatureReadResult(
            digest = digestOf(primarySigner),
            signerCount = signers.size,
            usesKeyRotation = signingInfo.hasPastSigningCertificates()
        )
    }

    private fun signingInfoOrNull(packageInfo: PackageInfo): SigningInfo? {
        if (!SdkHelper.isAtleastPie) {
            return null
        }
        return packageInfo.signingInfo
    }

    private fun digestOf(signature: Signature): String {
        val digestBytes = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(signature.toByteArray())
        return digestBytes.joinToString(HEX_SEPARATOR) { byte -> HEX_FORMAT.format(byte) }
    }
}

data class SignatureReadResult(
    val digest: String?,
    val signerCount: Int,
    val usesKeyRotation: Boolean
) {

    companion object {
        val UNAVAILABLE = SignatureReadResult(digest = null, signerCount = 0, usesKeyRotation = false)
    }
}
