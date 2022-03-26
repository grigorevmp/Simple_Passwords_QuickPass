package com.mikhailgrigorev.quickpassword.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mikhailgrigorev.quickpassword.data.dao.FolderDao
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard

const val FOLDER_CARD_DB_NAME = "folder_card"

@Database(entities = [FolderCard::class], version = 1, exportSchema = false)
abstract class FolderCardDatabase : RoomDatabase() {
    abstract fun FolderDao(): FolderDao

    companion object {

        private var INSTANCE: FolderCardDatabase? = null

        fun setInstance(context: Context): FolderCardDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            FolderCardDatabase::class.java,
                            FOLDER_CARD_DB_NAME
                    ).build()
                    INSTANCE = instance
                    return instance
                }
            }
            else {
                return INSTANCE!!
            }
        }

        @Synchronized
        fun getInstance(): FolderCardDatabase {
            return INSTANCE!!
        }

    }
}