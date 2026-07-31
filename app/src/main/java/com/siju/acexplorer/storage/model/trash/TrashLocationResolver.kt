package com.siju.acexplorer.storage.model.trash

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

private const val TRASH_DIR_NAME = "trash"

/**
 * Decides which directory a deleted file should be parked in.
 *
 * Moving a file between storage volumes is a full byte copy, so trashing a large file off an SD
 * card into internal storage would be slow and could fail for lack of space. Where the platform
 * gives us an app-specific directory on the same volume as the file, we use that instead and the
 * move stays a rename.
 */
class TrashLocationResolver @Inject constructor(@ApplicationContext private val context: Context) {

    /** Every trash directory the app may have used, so the screen can find items on any volume. */
    fun allTrashDirs(): List<File> {
        val externalDirs = context.getExternalFilesDirs(null)
            .filterNotNull()
            .map { externalDir -> File(externalDir, TRASH_DIR_NAME) }
        return (listOf(internalTrashDir()) + externalDirs).distinctBy { dir -> dir.absolutePath }
    }

    /**
     * Trash directory on the same volume as [sourceFile], falling back to internal storage when the
     * file's volume has no app-specific directory.
     */
    fun trashDirFor(sourceFile: File): File {
        val sameVolumeDir = sameVolumeTrashDir(sourceFile) ?: internalTrashDir()
        sameVolumeDir.mkdirs()
        return sameVolumeDir
    }

    /** True when parking [sourceFile] in [trashDir] would copy bytes rather than rename. */
    fun isCrossVolume(sourceFile: File, trashDir: File): Boolean {
        return volumeRootOf(sourceFile.absolutePath) != volumeRootOf(trashDir.absolutePath)
    }

    private fun sameVolumeTrashDir(sourceFile: File): File? {
        val sourceVolumeRoot = volumeRootOf(sourceFile.absolutePath) ?: return null

        val matchingExternalDir = context.getExternalFilesDirs(null)
            .filterNotNull()
            .firstOrNull { externalDir -> volumeRootOf(externalDir.absolutePath) == sourceVolumeRoot }

        return matchingExternalDir?.let { externalDir -> File(externalDir, TRASH_DIR_NAME) }
    }

    private fun internalTrashDir(): File = File(context.filesDir, TRASH_DIR_NAME)

    /**
     * Storage volume a path belongs to, derived from the path itself because the app-specific
     * directories are nested inside the volume they live on.
     */
    private fun volumeRootOf(path: String): String? {
        val storageRoots = context.getExternalFilesDirs(null)
            .filterNotNull()
            .mapNotNull { externalDir -> externalDir.absolutePath.substringBefore("/Android/data", "").ifEmpty { null } }

        val matchingRoot = storageRoots.filter { root -> path.startsWith(root) }.maxByOrNull { root -> root.length }
        if (matchingRoot != null) {
            return matchingRoot
        }
        return if (path.startsWith(context.filesDir.absolutePath)) context.filesDir.absolutePath else null
    }
}
