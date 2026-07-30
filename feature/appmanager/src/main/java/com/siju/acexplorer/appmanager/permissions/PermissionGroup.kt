package com.siju.acexplorer.appmanager.permissions

import androidx.annotation.StringRes
import com.siju.acexplorer.appmanager.R

private const val KEYWORD_CAMERA = "CAMERA"
private const val KEYWORD_RECORD_AUDIO = "RECORD_AUDIO"
private const val KEYWORD_LOCATION = "LOCATION"
private const val KEYWORD_CONTACTS = "CONTACTS"
private const val KEYWORD_SMS = "SMS"
private const val KEYWORD_INTERNET = "INTERNET"
private const val KEYWORD_NETWORK = "NETWORK"
private const val KEYWORD_STORAGE = "STORAGE"
private const val KEYWORD_MEDIA = "MEDIA"
private const val KEYWORD_NOTIFICATION = "NOTIFICATION"
private const val KEYWORD_FOREGROUND_SERVICE = "FOREGROUND_SERVICE"

/**
 * Buckets a raw Android permission name into a group a user can understand.
 *
 * Declaration order is also match order, so the most specific groups are declared first and
 * [OTHER] stays last both when matching and when the groups are sorted for display.
 */
enum class PermissionGroup(@StringRes val labelRes: Int, private val permissionKeywords: List<String>) {

    CAMERA(R.string.permission_camera, listOf(KEYWORD_CAMERA)),
    MICROPHONE(R.string.permission_microphone, listOf(KEYWORD_RECORD_AUDIO)),
    LOCATION(R.string.permission_location, listOf(KEYWORD_LOCATION)),
    CONTACTS(R.string.permission_contacts, listOf(KEYWORD_CONTACTS)),
    SMS(R.string.permission_sms, listOf(KEYWORD_SMS)),
    NETWORK(R.string.permission_group_network, listOf(KEYWORD_INTERNET, KEYWORD_NETWORK)),
    FILES(R.string.permission_group_files, listOf(KEYWORD_STORAGE, KEYWORD_MEDIA)),
    NOTIFICATIONS(R.string.permission_group_notifications, listOf(KEYWORD_NOTIFICATION)),
    BACKGROUND(R.string.permission_group_background, listOf(KEYWORD_FOREGROUND_SERVICE)),
    OTHER(R.string.permission_group_other, emptyList());

    private fun matches(permission: String): Boolean {
        return permissionKeywords.any(permission::contains)
    }

    companion object {

        fun of(permission: String): PermissionGroup {
            return entries.firstOrNull { group -> group.matches(permission) } ?: OTHER
        }
    }
}
