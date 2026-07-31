package com.siju.acexplorer.storage.model.trash

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrashDao {

    @Insert
    suspend fun insert(entries: List<TrashEntry>): List<Long>

    @Query("SELECT * FROM $TRASH_TABLE ORDER BY deleted_at DESC")
    fun observeAll(): LiveData<List<TrashEntry>>

    @Query("SELECT * FROM $TRASH_TABLE ORDER BY deleted_at DESC")
    suspend fun getAll(): List<TrashEntry>

    @Query("SELECT * FROM $TRASH_TABLE WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<TrashEntry>

    @Query("SELECT * FROM $TRASH_TABLE WHERE deleted_at < :cutoff")
    suspend fun getDeletedBefore(cutoff: Long): List<TrashEntry>

    @Query("DELETE FROM $TRASH_TABLE WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM $TRASH_TABLE")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(size), 0) FROM $TRASH_TABLE")
    fun observeTotalSize(): LiveData<Long>

    @Query("SELECT COALESCE(SUM(size), 0) FROM $TRASH_TABLE")
    suspend fun totalSize(): Long
}
