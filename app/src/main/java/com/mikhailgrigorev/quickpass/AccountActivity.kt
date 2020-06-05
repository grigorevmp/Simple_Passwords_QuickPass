package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_account)

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putString(KEY_USERNAME, login)
            commit()
        }

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS, dbHelper.KEY_ID, dbHelper.KEY_IMAGE),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val exInfoPassText = cursor.getString(passIndex).toString()
                val exInfoImgText = cursor.getString(imageIndex).toString()
                passViewField.setText(exInfoPassText)
                val id = resources.getIdentifier(
                    exInfoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }

        logOut.setOnClickListener {
            exit(sharedPref)
        }

        editAccount.setOnClickListener {
            val intent = Intent(this, EditAccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity", account)
            startActivity(intent)
            finish()
        }

        deleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.accountDelete))
            builder.setMessage(getString(R.string.accountDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                database.delete(dbHelper.TABLE_USERS,
                    "NAME = ?",
                    arrayOf(login))
                val pdbHelper = PasswordsDataBaseHelper(this, login)
                val pDatabase = pdbHelper.writableDatabase
                pDatabase.delete(pdbHelper.TABLE_USERS,
                    "NAME = ?",
                    arrayOf(login))
                toast(getString(R.string.accountDeleted))
                exit(sharedPref)
            }

            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }


    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val activity = intent.getStringExtra("activity")
                if(activity == "menu"){
                    val intent = Intent(this, PassGenActivity::class.java)
                    intent.putExtra("login", login)
                    startActivity(intent)
                }
                else if(activity == "editPass"){
                    val intent = Intent(this, EditPassActivity::class.java)
                    intent.putExtra("login", login)
                    intent.putExtra("passName", passName)
                    startActivity(intent)
                }
                else if(activity == "viewPass"){
                    val intent = Intent(this, PasswordViewActivity::class.java)
                    intent.putExtra("login", login)
                    intent.putExtra("passName", passName)
                    startActivity(intent)
                }
                this.overridePendingTransition(R.anim.right_in,
                    R.anim.right_out)
                finish()
            }
        }
        return false
    }

    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(KEY_USERNAME).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

}