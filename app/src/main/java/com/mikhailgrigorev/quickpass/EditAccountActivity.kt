package com.mikhailgrigorev.quickpass

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_account.*


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
                val id = resources.getIdentifier(
                    exInfoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }


        loadAcc.setOnClickListener {
            avatars.visibility = View.VISIBLE
        }

        // Checking prefs
        when (imageName) {
            "ic_account" -> {
                basicAcc.rotation = 20F
            }
            "ic_custom" -> {
                customAcc.rotation = 20F
            }
            "ic_m" -> {
                mAcc.rotation = 20F
            }
            "ic_e" -> {
                eAcc.rotation = 20F
            }
        }


        basicAcc.setOnClickListener {
            basicAcc.rotation = 20F
            customAcc.rotation = 0F
            mAcc.rotation = 0F
            eAcc.rotation = 0F
            imageName = "ic_account"
        }

        customAcc.setOnClickListener {
            basicAcc.rotation = 0F
            customAcc.rotation = 20F
            mAcc.rotation = 0F
            eAcc.rotation = 0F
            imageName = "ic_custom"
        }

        mAcc.setOnClickListener {
            basicAcc.rotation = 0F
            customAcc.rotation = 0F
            mAcc.rotation = 20F
            eAcc.rotation = 0F
            imageName = "ic_m"
        }

        eAcc.setOnClickListener {
            basicAcc.rotation = 0F
            customAcc.rotation = 0F
            mAcc.rotation = 0F
            eAcc.rotation = 20F
            imageName = "ic_e"
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
