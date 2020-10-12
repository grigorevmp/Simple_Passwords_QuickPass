package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_edit_account.*


class EditAccountActivity : AppCompatActivity() {

    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String
    private lateinit var imageName: String

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        when(pref.getString(_keyTheme, "none")){
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        super.onCreate(savedInstanceState)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_edit_account)

        back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity", account)
            setResult(1, intent)
            finish()
        }

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name
        nameViewField.setText(login)

        // Checking prefs
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString(_keyUsername, login)
            commit()
        }

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS,
            arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS, dbHelper.KEY_ID, dbHelper.KEY_IMAGE),
            "NAME = ?",
            arrayOf(login),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val exInfoPassText = cursor.getString(passIndex).toString()
                val exInfoImgText = cursor.getString(imageIndex).toString()
                imageName = exInfoImgText
                passViewField.setText(exInfoPassText)
                when(cursor.getString(imageIndex).toString()){
                    "ic_account" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                    "ic_account_Pink" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink)
                    "ic_account_Red" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red)
                    "ic_account_Purple" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple)
                    "ic_account_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet)
                    "ic_account_Dark_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet)
                    "ic_account_Blue" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue)
                    "ic_account_Cyan" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan)
                    "ic_account_Teal" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal)
                    "ic_account_Green" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green)
                    "ic_account_lightGreen" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen)
                    else -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                }
                accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }


        savePass.setOnClickListener {
            nameView.error = null
            passView.error = null
            if (nameViewField.text.toString()
                        .isEmpty() || nameViewField.text.toString().length < 3
            ) {
                nameView.error = getString(R.string.errNumOfText)
            } else if (passViewField.text.toString()
                        .isEmpty() || passViewField.text.toString().length < 4 || passViewField.text.toString().length > 20
            ) {
                passView.error = getString(R.string.errPass)
            } else {
                val contentValues = ContentValues()
                contentValues.put(dbHelper.KEY_NAME, nameViewField.text.toString())
                contentValues.put(dbHelper.KEY_PASS, passViewField.text.toString())
                contentValues.put(dbHelper.KEY_IMAGE, imageName)
                database.update(
                    dbHelper.TABLE_USERS, contentValues,
                    "NAME = ?",
                    arrayOf(login)
                )
                // Checking prefs
                with(sharedPref.edit()) {
                    putString(_keyUsername, nameViewField.text.toString())
                    commit()
                }

                val pdbHelper = PasswordsDataBaseHelper(this, login)
                val pDatabase = pdbHelper.writableDatabase
                if (login != nameViewField.text.toString()) {
                    database.execSQL("DROP TABLE IF EXISTS " + nameViewField.text.toString())
                    pDatabase.execSQL("DROP TABLE IF EXISTS " + nameViewField.text.toString())
                    pDatabase.execSQL("ALTER TABLE " + login + " RENAME TO " + nameViewField.text.toString())
                }
                val intent = Intent()
                // Checking prefs

                with(sharedPref.edit()) {
                    putString(_keyUsername, nameViewField.text.toString())
                    commit()
                }

                intent.putExtra("login", nameViewField.text.toString())
                intent.putExtra("passName", passViewField.text.toString())
                intent.putExtra("activity", account)
                setResult(1, intent)
                finish()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                setResult(1, intent)
                finish()
            }
        }
        return false
    }
}
