package com.mikhailgrigorev.quickpassword.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mikhailgrigorev.quickpassword.data.dao.PasswordCardDao
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

private const val PASSWORD_CARD_DB_NAME = "password_card"

@Database(entities = [PasswordCard::class], version = 1, exportSchema = false)
abstract class PasswordCardDatabase : RoomDatabase() {
    /*
       Main password database class
     */

    abstract fun PasswordCardDao(): PasswordCardDao

    companion object {

        private var INSTANCE: PasswordCardDatabase? = null

        fun setInstance(context: Context): PasswordCardDatabase? {
            if (INSTANCE == null) {
                CoroutineScope(IO).launch {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.applicationContext,
                                PasswordCardDatabase::class.java, PASSWORD_CARD_DB_NAME
                        ).addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                populateDatabase(INSTANCE!!)
                            }
                        })
                                .build()
                    }
                }
            }
            return INSTANCE
        }

        @Synchronized
        fun getInstance(): PasswordCardDatabase {
            return INSTANCE!!
        }

        private fun populateDatabase(db: PasswordCardDatabase) {
            // val passwordCardDao = db.PasswordCardDao()
            // CoroutineScope(IO).launch {
            //     passwordCardDao.insert()
            // }
        }
    }
}