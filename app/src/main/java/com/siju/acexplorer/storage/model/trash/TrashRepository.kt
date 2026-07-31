package com.siju.acexplorer.storage.model.trash

import android.content.Context
import androidx.lifecycle.LiveData
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.storage.model.operations.DeleteOperation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val RESTORE_SUFFIX_LIMIT = 100

/**
 * The one place the rest of the app talks to about deleted files.
 *
 * Files are parked in an app-private directory and remembered in [TrashDao] so they can be put back
 * where they came from. Callers never touch the trash directory or the database directly.
 */
@Singleton
class TrashRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trashLocationResolver: TrashLocationResolver
) {

    private val trashDao: TrashDao
        get() = TrashDatabase.getInstance(context).trashDao()

    fun observeEntries(): LiveData<List<TrashEntry>> = trashDao.observeAll()

    fun observeTotalSize(): LiveData<Long> = trashDao.observeTotalSize()

    suspend fun totalSize(): Long = withContext(Dispatchers.IO) { trashDao.totalSize() }

    /**
     * Parks [paths] in the trash. Files that cannot be parked are left exactly where they are so
     * nothing is lost by a partial failure.
     */
    suspend fun moveToTrash(paths: List<String>): TrashResult = withContext(Dispatchers.IO) {
        val entries = mutableListOf<TrashEntry>()
        val failedPaths = mutableListOf<String>()
        val tooLargeToTrash = mutableListOf<String>()
        val pathsToRescan = mutableListOf<String>()

        for (path in paths) {
            val sourceFile = File(path)
            if (!sourceFile.exists()) {
                failedPaths.add(path)
                continue
            }

            val trashDir = trashLocationResolver.trashDirFor(sourceFile)
            val size = sizeOf(sourceFile)

            if (!hasRoomFor(sourceFile, trashDir, size)) {
                tooLargeToTrash.add(path)
                continue
            }

            val trashedName = UUID.randomUUID().toString()
            val destination = File(trashDir, trashedName)

            if (!moveFile(sourceFile, destination)) {
                failedPaths.add(path)
                continue
            }

            pathsToRescan.add(path)
            entries.add(
                TrashEntry(
                    trashedName = trashedName,
                    trashDirPath = trashDir.absolutePath,
                    originalPath = path,
                    displayName = sourceFile.name,
                    size = size,
                    isDirectory = destination.isDirectory,
                    deletedAt = System.currentTimeMillis()
                )
            )
        }

        val insertedIds = if (entries.isEmpty()) emptyList() else trashDao.insert(entries)
        rescan(pathsToRescan)

        TrashResult(
            trashedIds = insertedIds,
            trashedCount = entries.size,
            failedPaths = failedPaths,
            tooLargeToTrash = tooLargeToTrash
        )
    }

    /** Puts items back where they came from, never overwriting anything already at that path. */
    suspend fun restore(ids: List<Long>): RestoreResult = withContext(Dispatchers.IO) {
        val entries = trashDao.getByIds(ids)
        val failedNames = mutableListOf<String>()
        val renamedOnRestore = mutableListOf<String>()
        val restoredIds = mutableListOf<Long>()
        val pathsToRescan = mutableListOf<String>()

        for (entry in entries) {
            val trashedFile = File(entry.trashDirPath, entry.trashedName)
            if (!trashedFile.exists()) {
                failedNames.add(entry.displayName)
                continue
            }

            val destination = availableRestoreTarget(entry)
            if (destination == null) {
                failedNames.add(entry.displayName)
                continue
            }

            destination.parentFile?.mkdirs()
            if (!moveFile(trashedFile, destination)) {
                failedNames.add(entry.displayName)
                continue
            }

            if (destination.name != entry.displayName) {
                renamedOnRestore.add(destination.name)
            }
            restoredIds.add(entry.id)
            pathsToRescan.add(destination.absolutePath)
        }

        trashDao.deleteByIds(restoredIds)
        rescan(pathsToRescan)

        RestoreResult(restoredIds.size, failedNames, renamedOnRestore)
    }

    suspend fun deleteForever(ids: List<Long>) = withContext(Dispatchers.IO) {
        removeEntries(trashDao.getByIds(ids))
    }

    suspend fun emptyAll() = withContext(Dispatchers.IO) {
        removeEntries(trashDao.getAll())
    }

    /**
     * Drops items older than the retention window. Called when the trash screen opens rather than
     * from a scheduled job, which is accurate enough for a window measured in days.
     */
    suspend fun purgeOlderThan(retentionDays: Int) = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())
        removeEntries(trashDao.getDeletedBefore(cutoff))
    }

    private suspend fun removeEntries(entries: List<TrashEntry>) {
        for (entry in entries) {
            File(entry.trashDirPath, entry.trashedName).deleteRecursively()
        }
        trashDao.deleteByIds(entries.map { entry -> entry.id })
    }

    /**
     * Renames when both paths are on one volume, and copies then deletes when they are not. The
     * delete goes through [DeleteOperation] so the existing SAF and root fallbacks still apply to
     * files the app cannot remove directly.
     */
    private fun moveFile(source: File, destination: File): Boolean {
        if (source.renameTo(destination)) {
            return true
        }
        if (!source.copyRecursivelyQuietly(destination)) {
            destination.deleteRecursively()
            return false
        }
        if (!DeleteOperation().delete(source, mediaIndex = false)) {
            destination.deleteRecursively()
            return false
        }
        return true
    }

    private fun File.copyRecursivelyQuietly(destination: File): Boolean {
        return try {
            copyRecursively(destination, overwrite = true)
        }
        catch (e: Exception) {
            false
        }
    }

    /**
     * Finds a free path to restore into. Returns the original path when it is free, otherwise the
     * same name with a numeric suffix, so a restore can never overwrite a newer file.
     */
    private fun availableRestoreTarget(entry: TrashEntry): File? {
        val originalFile = File(entry.originalPath)
        if (!originalFile.exists()) {
            return originalFile
        }

        val parent = originalFile.parentFile ?: return null
        val baseName = originalFile.nameWithoutExtension
        val extension = originalFile.extension
        val suffixedExtension = if (extension.isEmpty()) "" else ".$extension"

        for (suffix in 1..RESTORE_SUFFIX_LIMIT) {
            val candidate = File(parent, "$baseName ($suffix)$suffixedExtension")
            if (!candidate.exists()) {
                return candidate
            }
        }
        return null
    }

    private fun hasRoomFor(sourceFile: File, trashDir: File, size: Long): Boolean {
        if (!trashLocationResolver.isCrossVolume(sourceFile, trashDir)) {
            return true
        }
        return trashDir.usableSpace > size
    }

    private fun sizeOf(file: File): Long {
        if (!file.isDirectory) {
            return file.length()
        }
        return file.walkBottomUp().filter { child -> child.isFile }.sumOf { child -> child.length() }
    }

    private fun rescan(paths: List<String>) {
        if (paths.isEmpty()) {
            return
        }
        MediaScannerHelper.scanFiles(context, paths.toTypedArray())
    }
}
