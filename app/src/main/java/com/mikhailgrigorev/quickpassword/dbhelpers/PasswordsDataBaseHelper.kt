package com.mikhailgrigorev.quickpassword.dbhelpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PasswordsDataBaseHelper(context: Context?, tableName: String) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    val TABLE_USERS = tableName
    val KEY_ID = "_id"
    val KEY_NAME = "name"
    val KEY_PASS = "pass"
    val KEY_2FA = "_2fa"
    val KEY_USE_TIME = "utime"
    val KEY_TIME = "time"
    val KEY_DESC = "description"
    val KEY_TAGS = "tags"
    val KEY_GROUPS = "groups"
    val KEY_LOGIN = "login"
    val KEY_CIPHER = "cipher"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table " + TABLE_USERS + "("
                    + KEY_ID + " integer primary key,"
                    + KEY_NAME + " text," + KEY_PASS + " text,"
                    + KEY_2FA + " integer," + KEY_USE_TIME + " integer,"
                    + KEY_TIME + " date,"
                    + KEY_TAGS + " text," + KEY_GROUPS + " text,"
                    + KEY_LOGIN + " text,"
                    + KEY_DESC + " text,"
                    + KEY_CIPHER + " text"
                    + ")"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $KEY_CIPHER TEXT DEFAULT 'none'")
        }
    }

    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "PassDatabase"
    }
}