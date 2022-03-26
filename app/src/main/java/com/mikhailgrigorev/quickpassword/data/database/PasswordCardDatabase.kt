package com.mikhailgrigorev.quickpassword.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mikhailgrigorev.quickpassword.data.dao.PasswordCardDao
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard

const val PASSWORD_CARD_DB_NAME = "password_card"

@Database(entities = [PasswordCard::class], version = 1, exportSchema = false)
abstract class PasswordCardDatabase : RoomDatabase() {
    abstract fun PasswordCardDao(): PasswordCardDao

    companion object {

        private var INSTANCE: PasswordCardDatabase? = null

        fun setInstance(context: Context): PasswordCardDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            PasswordCardDatabase::class.java,
                            PASSWORD_CARD_DB_NAME
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