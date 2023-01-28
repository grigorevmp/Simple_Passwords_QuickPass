package com.mikhailgrigorev.simple_password.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mikhailgrigorev.simple_password.data.dao.PasswordCardDao
import com.mikhailgrigorev.simple_password.data.dbo.CustomFieldConverters
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard


const val PASSWORD_CARD_DB_NAME = "password_card"

/** Versions **/

// Release version
const val firstVersion = 1

// ChangeLog: add list of custom fields
const val secondVersion = 2

/** Database creation **/

@Database(entities = [PasswordCard::class], version = secondVersion, exportSchema = false)
@TypeConverters(CustomFieldConverters::class)
abstract class PasswordCardDatabase : RoomDatabase() {
    abstract fun PasswordCardDao(): PasswordCardDao

    companion object {

        private var INSTANCE: PasswordCardDatabase? = null

        /** Instance creation **/

        fun setInstance(context: Context): PasswordCardDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            PasswordCardDatabase::class.java,
                            PASSWORD_CARD_DB_NAME
                    )
                            .addMigrations(MIGRATION_ADD_CUSTOM_FIELDS)
                            .build()
                    INSTANCE = instance
                    return instance
                }
            } else {
                return INSTANCE!!
            }
        }

        @Synchronized
        fun getInstance(): PasswordCardDatabase {
            return INSTANCE!!
        }

        /** Migration **/

        /** Migration from [firstVersion] to [secondVersion] **/
        private val MIGRATION_ADD_CUSTOM_FIELDS: Migration =
                object : Migration(firstVersion, secondVersion) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE $PASSWORD_CARD_DB_NAME ADD COLUMN custom_field TEXT NOT NULL DEFAULT '[]'")
                    }
                }
    }
}