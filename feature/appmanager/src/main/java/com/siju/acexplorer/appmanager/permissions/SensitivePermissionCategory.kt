package com.siju.acexplorer.appmanager.permissions

import android.Manifest
import androidx.annotation.StringRes
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.types.AppInfo

enum class SensitivePermissionCategory(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val permissions: Set<String>
) {
    CAMERA(R.string.permission_camera, R.string.permission_camera_description, setOf(Manifest.permission.CAMERA)),
    MICROPHONE(R.string.permission_microphone, R.string.permission_microphone_description, setOf(Manifest.permission.RECORD_AUDIO)),
    LOCATION(
        R.string.permission_location,
        R.string.permission_location_description,
        setOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    ),
    SMS(
        R.string.permission_sms,
        R.string.permission_sms_description,
        setOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )
    ),
    CONTACTS(
        R.string.permission_contacts,
        R.string.permission_contacts_description,
        setOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    );

    fun isGrantedBy(app: AppInfo): Boolean = app.grantedPermissions.any(permissions::contains)
}

data class PermissionCategoryCount(
    val category: SensitivePermissionCategory,
    val appCount: Int
)
