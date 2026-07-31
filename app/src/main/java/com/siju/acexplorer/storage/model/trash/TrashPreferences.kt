package com.siju.acexplorer.storage.model.trash

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

const val PREFS_TRASH_ENABLED = "prefsTrashEnabled"
const val PREFS_TRASH_RETENTION_DAYS = "prefsTrashRetentionDays"

private const val DEFAULT_TRASH_ENABLED = true
private const val DEFAULT_RETENTION_DAYS = "30"

/**
 * User choices about the recycle bin.
 */
class TrashPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val preferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    /** When off, deleting is permanent and the trash checkbox is not offered. */
    val isTrashEnabled: Boolean
        get() = preferences.getBoolean(PREFS_TRASH_ENABLED, DEFAULT_TRASH_ENABLED)

    val retentionDays: Int
        get() = preferences.getString(PREFS_TRASH_RETENTION_DAYS, DEFAULT_RETENTION_DAYS)
            ?.toIntOrNull() ?: DEFAULT_RETENTION_DAYS.toInt()
}
