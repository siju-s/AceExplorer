package com.siju.acexplorer.storage.model.trash

/**
 * Outcome of moving files to the trash.
 *
 * [tooLargeToTrash] holds files that could not be parked because the move would have crossed a
 * storage volume and there was not enough free space to copy them. Those files are left untouched,
 * so the caller can offer to delete them permanently instead of silently doing nothing.
 */
data class TrashResult(
    val trashedIds: List<Long>,
    val trashedCount: Int,
    val failedPaths: List<String>,
    val tooLargeToTrash: List<String>
) {

    val hasFailures: Boolean
        get() = failedPaths.isNotEmpty() || tooLargeToTrash.isNotEmpty()

    companion object {
        val EMPTY = TrashResult(emptyList(), 0, emptyList(), emptyList())
    }
}

/**
 * Outcome of restoring files.
 *
 * [renamedOnRestore] holds the names of items whose original path was already occupied and which
 * were therefore restored alongside under a new name rather than overwriting anything.
 */
data class RestoreResult(
    val restoredCount: Int,
    val failedNames: List<String>,
    val renamedOnRestore: List<String>
) {

    companion object {
        val EMPTY = RestoreResult(0, emptyList(), emptyList())
    }
}
