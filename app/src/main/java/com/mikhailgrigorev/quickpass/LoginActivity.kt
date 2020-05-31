package com.mikhailgrigorev.quickpass

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {

    private val TAG = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Start animation
        loginFab.show()


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
        signUpChipGroup.setOnCheckedChangeListener{ group, checkedId ->
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
        if (password.isEmpty() || password.length < 4) {
            inputPasswordId.error = getString(R.string.errPass)
            valid = false;
        } else {
            inputPasswordId.error = null
        }
        return valid
    }

    private fun signUp (login: String, password:String) {

        Log.d(TAG, "SignUp");

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val contentValues = ContentValues()

        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        var dbLogin = "null"

        if (cursor.moveToFirst()) {
            inputLoginId.error = getString(R.string.exists)
            return
        } else {
            contentValues.put(dbHelper.KEY_ID, Random.nextInt(0, 100))
            contentValues.put(dbHelper.KEY_NAME, login)
            contentValues.put(dbHelper.KEY_PASS, password)
            contentValues.put(dbHelper.KEY_IMAGE, "ic_useravatar")
            database.insert(dbHelper.TABLE_USERS, null, contentValues);
        }

        signIn(login, password)
    }

    private fun signIn (login: String, password:String){

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
            inputLoginId.error = getString(R.string.wrong_name)
            return
        }

        cursor.close()

        // создание объекта Intent для запуска SecondActivity

        val intent = Intent(this, PassGenActivity::class.java)
        intent.putExtra("login", dbLogin)
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}