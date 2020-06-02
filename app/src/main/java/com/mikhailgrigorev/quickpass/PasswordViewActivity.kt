package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_password_view.*

class PasswordViewActivity : AppCompatActivity() {

    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private var length = 20
    private var useSyms = false
    private var useUC = false
    private var useLetters = false
    private var useNums = false
    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0

    @SuppressLint("Recycle", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_view)

        val args: Bundle? = intent.extras
        val login: String? = args?.get("login").toString()
        val passName: String? = args?.get("passName").toString()

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
                val id = resources.getIdentifier(
                    ex_infoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }


        var dbLogin: String = ""
        var dbPassword: String

        val pdbHelper = PasswordsDataBaseHelper(this, login.toString())
        val pdatabase = pdbHelper.writableDatabase
        try {
            val pcursor: Cursor = pdatabase.query(
                pdbHelper.TABLE_USERS, arrayOf(pdbHelper.KEY_NAME, pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA, pdbHelper.KEY_USE_TIME, pdbHelper.KEY_TIME, pdbHelper.KEY_DESC),
                "NAME = ?", arrayOf(passName),
                null, null, null
            )


            if (pcursor.moveToFirst()) {
                val nameIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_PASS)
                val _2FAIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_2FA)
                val uTIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_USE_TIME)
                val timeIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_TIME)
                val descIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_DESC)
                do {
                    dbLogin = pcursor.getString(nameIndex).toString()
                    dbPassword = pcursor.getString(passIndex).toString()
                    val db2FAIndex = pcursor.getString(_2FAIndex).toString()
                    val dbUTIndex = pcursor.getString(uTIndex).toString()
                    val dbTimeIndex = pcursor.getString(timeIndex).toString()
                    val dbDescIndex = pcursor.getString(descIndex).toString()
                    passwordName.text = dbLogin
                    passwordContent.text = "Password: $dbPassword"
                    password2FA.text = "Use 2FA: $db2FAIndex"
                    passwordUTL.text = "Use limits of time: $dbUTIndex"
                    passwordTime.text = "Time limits: $dbTimeIndex"
                    passwordDesc.text = "Password: $dbDescIndex"
                } while (pcursor.moveToNext())
            } else {
                passwordName.text = "no passwords"
            }

        } catch (e: SQLException) {
            passwordName.text = "no passwords"
        }

        deletePassword.setOnClickListener {
            pdatabase.delete(pdbHelper.TABLE_USERS,
                "NAME = ?",
                arrayOf(dbLogin))
            toast("You password has been deleted")
            val intent = Intent(this, PassGenActivity::class.java)
            intent.putExtra("login", login)
            startActivity(intent)
            finish()
        }

    }
    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
}