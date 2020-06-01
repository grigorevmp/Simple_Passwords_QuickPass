package com.mikhailgrigorev.quickpass

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_sign.*

class SignActivity : AppCompatActivity() {

    private val TAG = "SignupActivity"
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        val args: Bundle? = intent.extras
        val login: String? = args?.get("login").toString()
        val name: String? = "Hi, $login"
        helloTextId.text = name

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_IMAGE),
            "NAME = ?", arrayOf(login),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val ex_infoImgText = cursor.getString(imageIndex).toString()
                val infoImgText = "Avatar src: $ex_infoImgText"
                val id = getResources().getIdentifier(
                    ex_infoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }

        // Start animation
        loginFab.show()
        logOutFab.show()

        // Fab handler
        loginFab.setOnClickListener {
            if (validate(inputPasswordIdField.text.toString()))
                    signIn(login.toString(), inputPasswordIdField.text.toString())

        }

        logOutFab.setOnClickListener {
            val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            exit(sharedPref)
        }
    }
    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(KEY_USERNAME).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validate(password:String): Boolean {
        var valid = true
        if (password.isEmpty() || password.length < 4 || password.length > 20) {
            inputPasswordId.error = getString(R.string.errPass)
            valid = false;
        } else {
            inputPasswordId.error = null
        }
        return valid
    }

    private fun signIn (login:String, password:String){

        Log.d(TAG, "SignIn");

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        var dbLogin = "null"
        var dbPassword = "null"

        if (cursor.moveToFirst()) {
            val nameIndex: Int = cursor.getColumnIndex(dbHelper.KEY_NAME)
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            do {
                dbLogin = cursor.getString(nameIndex).toString()
                dbPassword = cursor.getString(passIndex).toString()
                if(dbPassword != password){
                    inputPasswordId.error = getString(R.string.wrong_pass)
                    return
                }
            } while (cursor.moveToNext())
        } else {
            inputPasswordId.error = getString(R.string.wrong_name)
            return
        }

        cursor.close()

        // создание объекта Intent для запуска SecondActivity

        val intent = Intent(this, PassGenActivity::class.java)
        intent.putExtra("login", dbLogin)
        startActivity(intent)
        finish()
    }
}