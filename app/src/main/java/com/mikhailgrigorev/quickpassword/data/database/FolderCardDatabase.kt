package com.mikhailgrigorev.quickpassword.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mikhailgrigorev.quickpassword.data.dao.FolderDao
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

private const val PASSWORD_CARD_DB_NAME = "folder_card"

@Database(entities = [FolderCard::class], version = 1, exportSchema = false)
abstract class FolderCardDatabase : RoomDatabase() {
    /*
       Main password database class
     */

    abstract fun FolderDao(): FolderDao

    companion object {

        private var INSTANCE: FolderCardDatabase? = null

        fun setInstance(context: Context): FolderCardDatabase? {
            if (INSTANCE == null) {
                CoroutineScope(IO).launch {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.applicationContext,
                                FolderCardDatabase::class.java, PASSWORD_CARD_DB_NAME
                        ).addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                populateDatabase(INSTANCE!!)
                            }
                        }).build()
                    }
                }
            }
            return INSTANCE
        }

        @Synchronized
        fun getInstance(): FolderCardDatabase {
            return INSTANCE!!
        }

        private fun populateDatabase(db: FolderCardDatabase) {
            /* val passwordCardDao = db.FolderDao()
            CoroutineScope(IO).launch {
                passwordCardDao.insert(
                        FolderCard(
                                _id = 0,
                                colorTag = 0,
                                description = "null",
                                imageSrc = "null",
                                name = "Test folder"
                        )
                )
            } */
        }
    }
}