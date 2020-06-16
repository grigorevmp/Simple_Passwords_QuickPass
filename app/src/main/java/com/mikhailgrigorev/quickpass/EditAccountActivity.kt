package com.mikhailgrigorev.quickpass

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_edit_account.*
import kotlinx.android.synthetic.main.activity_edit_account.accountAvatar
import kotlinx.android.synthetic.main.activity_edit_account.accountAvatarText
import kotlinx.android.synthetic.main.activity_edit_account.helloTextId
import kotlinx.android.synthetic.main.activity_sign.*


class EditAccountActivity : AppCompatActivity() {
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String
    private lateinit var imageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_edit_account)


        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name
        nameViewField.setText(login)

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString(KEY_USERNAME, login)
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
                accountAvatarText.text = login?.get(0).toString()
            } while (cursor.moveToNext())
        }

        // Checking prefs
        when (imageName) {
            "ic_account" -> {
                basicColor.cardElevation = 20F
            }
            "ic_account_Red" -> {
                redColor.cardElevation = 20F
            }
            "ic_account_Pink" -> {
                pinkColor.cardElevation = 20F
            }
            "ic_account_Purple" -> {
                purpleColor.cardElevation = 20F
            }
            "ic_account_Violet" -> {
                violetColor.cardElevation = 20F
            }
            "ic_account_Dark_Violet" -> {
                darkVioletColor.cardElevation = 20F
            }
            "ic_account_Blue" -> {
                blueColor.cardElevation = 20F
            }
            "ic_account_Cyan" -> {
                cyanColor.cardElevation = 20F
            }
            "ic_account_Teal" -> {
                tealColor.cardElevation = 20F
            }
            "ic_account_Green" -> {
                greenColor.cardElevation = 20F
            }
            "ic_account_lightGreen" -> {
                lightGreenColor.cardElevation = 20F
            }
        }


        basicColor.setOnClickListener {
            clearCE()
            basicColor.cardElevation = 20F
            imageName = "ic_account"
            updateAvatar(imageName)
        }

        redColor.setOnClickListener {
            clearCE()
            redColor.cardElevation = 20F
            imageName = "ic_account_Red"
            updateAvatar(imageName)
        }

        pinkColor.setOnClickListener {
            clearCE()
            pinkColor.cardElevation = 20F
            imageName = "ic_account_Pink"
            updateAvatar(imageName)
        }

        purpleColor.setOnClickListener {
            clearCE()
            purpleColor.cardElevation = 20F
            imageName = "ic_account_Purple"
            updateAvatar(imageName)
        }

        violetColor.setOnClickListener {
            clearCE()
            violetColor.cardElevation = 20F
            imageName = "ic_account_Violet"
            updateAvatar(imageName)
        }

        darkVioletColor.setOnClickListener {
            clearCE()
            darkVioletColor.cardElevation = 20F
            imageName = "ic_account_Dark_Violet"
            updateAvatar(imageName)
        }

        blueColor.setOnClickListener {
            clearCE()
            blueColor.cardElevation = 20F
            imageName = "ic_account_Blue"
            updateAvatar(imageName)
        }

        cyanColor.setOnClickListener {
            clearCE()
            cyanColor.cardElevation = 20F
            lightGreenColor.cardElevation = 0F
            imageName = "ic_account_Cyan"
            updateAvatar(imageName)
        }

        tealColor.setOnClickListener {
            clearCE()
            tealColor.cardElevation = 20F
            imageName = "ic_account_Teal"
            updateAvatar(imageName)
        }
        greenColor.setOnClickListener {
            clearCE()
            greenColor.cardElevation = 20F
            imageName = "ic_account_Green"
            updateAvatar(imageName)
        }
        lightGreenColor.setOnClickListener {
            clearCE()
            lightGreenColor.cardElevation = 20F
            imageName = "ic_account_lightGreen"
            updateAvatar(imageName)
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
                val intent = Intent(this, AccountActivity::class.java)
                // Checking prefs
                with(sharedPref.edit()) {
                    putString(KEY_USERNAME, nameViewField.text.toString())
                    commit()
                }

                val pdbHelper = PasswordsDataBaseHelper(this, login)
                val pDatabase = pdbHelper.writableDatabase
                if (login != nameViewField.text.toString())
                    pDatabase.execSQL("ALTER TABLE " + login + " RENAME TO " + nameViewField.text.toString())
                intent.putExtra("login", nameViewField.text)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun clearCE() {
        basicColor.cardElevation = 0F
        redColor.cardElevation = 0F
        pinkColor.cardElevation = 0F
        purpleColor.cardElevation = 0F
        violetColor.cardElevation = 0F
        darkVioletColor.cardElevation = 0F
        blueColor.cardElevation = 0F
        cyanColor.cardElevation = 0F
        tealColor.cardElevation = 0F
        greenColor.cardElevation = 0F
        lightGreenColor.cardElevation = 0F
    }

    private fun updateAvatar(imageName: String) {
        when(imageName){
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

    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent(this, AccountActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                this.overridePendingTransition(
                    R.anim.right_in,
                    R.anim.right_out
                )
                finish()
            }
        }
        return false
    }
}
