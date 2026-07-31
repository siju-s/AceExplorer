package com.siju.acexplorer.storage.model.trash

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

private const val DATABASE_NAME = "trash.db"

@Database(entities = [TrashEntry::class], version = 1, exportSchema = false)
abstract class TrashDatabase : RoomDatabase() {

    abstract fun trashDao(): TrashDao

    companion object {

        @Volatile
        private var instance: TrashDatabase? = null

        fun getInstance(context: Context): TrashDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context).also { database -> instance = database }
            }
        }

        private fun build(context: Context): TrashDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TrashDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}
