package com.siju.acexplorer.storage.model.trash

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val TRASH_TABLE = "trash_entry"

/**
 * A file or folder the user deleted, recorded so it can be put back where it came from.
 *
 * The file itself lives in a trash directory under [trashDirPath] as [trashedName]; this row is the
 * only thing that remembers where it belongs.
 */
@Entity(tableName = TRASH_TABLE)
data class TrashEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    /** Unique name of the item inside the trash directory. */
    @ColumnInfo(name = "trashed_name")
    val trashedName: String,

    /** Trash directory holding the item. Varies by storage volume. */
    @ColumnInfo(name = "trash_dir_path")
    val trashDirPath: String,

    /** Absolute path the item is restored to. */
    @ColumnInfo(name = "original_path")
    val originalPath: String,

    /** Name shown to the user, which is the original file name. */
    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "size")
    val size: Long,

    @ColumnInfo(name = "is_directory")
    val isDirectory: Boolean,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long
) {

    /** Folder the item will be restored into, for display. */
    val originalParentPath: String
        get() = originalPath.substringBeforeLast('/', "")
}
