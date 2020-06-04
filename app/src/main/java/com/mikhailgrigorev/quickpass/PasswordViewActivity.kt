package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_password_view.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

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
    lateinit var login: String
    lateinit var passName: String

    @SuppressLint("Recycle", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_password_view)

        val args: Bundle? = intent.extras
        login= args?.get("login").toString()
        passName = args?.get("passName").toString()

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
                    helloTextId.text = dbLogin
                    dbPassword = pcursor.getString(passIndex).toString()
                    passViewField.setText(dbPassword)
                    val db2FAIndex = pcursor.getString(_2FAIndex).toString()
                    if (db2FAIndex == "1"){
                        authToogle.isChecked = true
                    }
                    val dbUTIndex = pcursor.getString(uTIndex).toString()
                    if (dbUTIndex == "1"){
                        timeLimit.isChecked = true
                    }
                    val dbTimeIndex = pcursor.getString(timeIndex).toString()
                    passwordTime.text = getString(R.string.time_lim) + " " + dbTimeIndex
                    val dbDescIndex = pcursor.getString(descIndex).toString()
                    noteViewField.setText(dbDescIndex)
                } while (pcursor.moveToNext())
            } else {
                helloTextId.text = getString(R.string.no_text)
            }

        } catch (e: SQLException) {
            helloTextId.text = getString(R.string.no_text)
        }

        deletePassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete this password")
            builder.setMessage("Are you really want to this password?")

            builder.setPositiveButton("YES"){ _, _ ->
                pdatabase.delete(
                    pdbHelper.TABLE_USERS,
                    "NAME = ?",
                    arrayOf(dbLogin)
                )
                toast("You password has been deleted")
                val intent = Intent(this, PassGenActivity::class.java)
                intent.putExtra("login", login)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton("NO"){ _, _ ->
            }

            builder.setNeutralButton("Cancel"){_,_ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        userAvatar.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            startActivity(intent)
        }

        passView.setOnClickListener {
            if(passViewField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", passViewField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        passViewField.setOnClickListener {
            if(passViewField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", passViewField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        editButton.setOnClickListener {
            val intent = Intent(this, EditPassActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            startActivity(intent)
            finish()
        }

    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent(this, PassGenActivity::class.java)
                intent.putExtra("login", login)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                this.overridePendingTransition(R.anim.right_in,
                    R.anim.right_out);
                finish()
            }
        }
        return false
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }

}