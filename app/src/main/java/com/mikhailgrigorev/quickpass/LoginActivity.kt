package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {

    private val TAG = "SignUpActivity"
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private val KEY_BIO = "prefUserBioKey"
    private val KEY_USEPIN = "prefUsePinKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_login)

        // Start animation
        loginFab.show()

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val username = sharedPref.getString(KEY_USERNAME, "none")

        val usePin = sharedPref.getString(KEY_USEPIN, "none")
        if(usePin != "none"){
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("login", username)
            startActivity(intent)
            finish()
        }
        else if(username != "none"){
            val intent = Intent(this, SignActivity::class.java)
            intent.putExtra("login", username)
            startActivity(intent)
            finish()
        }


        // Fab handler
        loginFab.setOnClickListener {
            if (signUpChip.isChecked){
                if (validate(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString()))
                    signUp(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString())
            }
            else{
                if (validate(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString()))
                    signIn(inputLoginIdField.text.toString(), inputPasswordIdField.text.toString())
            }
        }

        // Chip handler
        signUpChipGroup.setOnCheckedChangeListener{ _, _ ->
            // Get the checked chip instance from chip group
            signUpChip?.let {
                if (signUpChip.isChecked){
                    loginFab.hide()
                    loginFab.text = getString (R.string.sign_up)
                    loginFab.show()
                }
                else{
                    loginFab.hide()
                    loginFab.text = getString (R.string.sign_in)
                    loginFab.show()
                }
            }
        }
    }

    private fun validate(login: String, password:String): Boolean {
        var valid = false
        if (login.isEmpty() || login.length < 3) {
            inputLoginId.error = getString(R.string.errNumOfText)
        } else {
            inputLoginId.error = null
            valid = true
        }
        if (password.isEmpty() || password.length < 4 || password.length > 20) {
            inputPasswordId.error = getString(R.string.errPass)
            valid = false
        } else {
            inputPasswordId.error = null
        }
        return valid
    }

    @SuppressLint("Recycle")
    private fun signUp (login: String, password:String) {

        Log.d(TAG, "SignUp")

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val contentValues = ContentValues()

        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            inputLoginId.error = getString(R.string.exists)
            return
        } else {
            contentValues.put(dbHelper.KEY_ID, Random.nextInt(0, 100))
            contentValues.put(dbHelper.KEY_NAME, login)
            contentValues.put(dbHelper.KEY_PASS, password)
            contentValues.put(dbHelper.KEY_IMAGE, "ic_account")
            database.insert(dbHelper.TABLE_USERS, null, contentValues)
        }

        signIn(login, password)
    }

    private fun signIn (login: String, password:String){

        Log.d(TAG, "SignIn")

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        var dbLogin: String
        var dbPassword: String

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
            inputLoginId.error = getString(R.string.wrong_name)
            return
        }

        cursor.close()

        // создание объекта Intent для запуска SecondActivity

        if(isAvailable(this)){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.bio_usage))
            builder.setMessage(getString(R.string.fingUnlock))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                // Checking prefs
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "using")
                    commit()
                }
                val intent = Intent(this, PassGenActivity::class.java)
                intent.putExtra("login", dbLogin)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "none")
                    commit()
                }
                val intent = Intent(this, PassGenActivity::class.java)
                intent.putExtra("login", dbLogin)
                startActivity(intent)
                finish()
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "none")
                    commit()
                }
                val intent = Intent(this, PassGenActivity::class.java)
                intent.putExtra("login", dbLogin)
                startActivity(intent)
                finish()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        else{
            val intent = Intent(this, PassGenActivity::class.java)
            intent.putExtra("login", dbLogin)
            startActivity(intent)
            finish()
        }
    }
    private fun isAvailable(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }
}