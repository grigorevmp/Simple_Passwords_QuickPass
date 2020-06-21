package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_password_view.*
import java.util.*

class PasswordViewActivity : AppCompatActivity() {

    private val KEY_THEME = "themePreference"
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private lateinit var login: String
    private lateinit var passName: String
    private val KEY_AUTOCOPY = "prefAutoCopyKey"

    @SuppressLint("Recycle", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        when(pref.getString(KEY_THEME, "none")){
            "none", "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        when(pref.getString("themeAccentPreference", "none")){
            "Red" -> setTheme(R.style.AppThemeRed)
            "Pink" -> setTheme(R.style.AppThemePink)
            "Purple" -> setTheme(R.style.AppThemePurple)
            "Violet" -> setTheme(R.style.AppThemeViolet)
            "DViolet" -> setTheme(R.style.AppThemeDarkViolet)
            "Blue" -> setTheme(R.style.AppThemeBlue)
            "Cyan" -> setTheme(R.style.AppThemeCyan)
            "Teal" -> setTheme(R.style.AppThemeTeal)
            "Green" -> setTheme(R.style.AppThemeGreen)
            "LGreen" -> setTheme(R.style.AppThemeLightGreen)
            else -> setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        when ((resources.configuration.uiMode + Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
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


        var dbLogin = ""
        var dbPassword: String

        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        try {
            val pCursor: Cursor = pDatabase.query(
                pdbHelper.TABLE_USERS, arrayOf(pdbHelper.KEY_NAME, pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA, pdbHelper.KEY_USE_TIME, pdbHelper.KEY_TIME,
                    pdbHelper.KEY_DESC, pdbHelper.KEY_TAGS),
                "NAME = ?", arrayOf(passName),
                null, null, null
            )


            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
                val uTIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_USE_TIME)
                val timeIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TIME)
                val descIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_DESC)
                val tagsIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TAGS)
                do {
                    dbLogin = pCursor.getString(nameIndex).toString()
                    helloTextId.text = dbLogin
                    dbPassword = pCursor.getString(passIndex).toString()
                    passViewField.setText(dbPassword)
                    val myPasswordManager = PasswordManager()
                    val evaluation: String = myPasswordManager.evaluatePasswordString(dbPassword)
                    when (evaluation) {
                        "low" -> passQuality.text = getString(R.string.low)
                        "high" -> passQuality.text = getString(R.string.high)
                        else -> passQuality.text = getString(R.string.medium)
                    }
                    when (evaluation) {
                        "low" -> passQuality.setTextColor(ContextCompat.getColor(applicationContext, R.color.negative))
                        "high" -> passQuality.setTextColor(ContextCompat.getColor(applicationContext, R.color.positive))
                        else -> passQuality.setTextColor(ContextCompat.getColor(applicationContext, R.color.fixable))
                    }
                    val db2FAIndex = pCursor.getString(aIndex).toString()
                    if (db2FAIndex == "1"){
                        authToggle.isChecked = true
                    }
                    val dbUTIndex = pCursor.getString(uTIndex).toString()
                    if (dbUTIndex == "1"){
                        timeLimit.isChecked = true
                    }
                    val dbTimeIndex = pCursor.getString(timeIndex).toString()
                    passwordTime.text = getString(R.string.time_lim) + " " + dbTimeIndex

                    //val year = dbTimeIndex.substring(0, 3).toInt()
                    val month = dbTimeIndex.substring(5, 7).toInt()
                    //val day = dbTimeIndex.substring(8, 9).toInt()
                    //val hour = dbTimeIndex.substring(11, 12).toInt()
                    //val minute = dbTimeIndex.substring(14, 15).toInt()
                    //val second = dbTimeIndex.substring(17, 18).toInt()

                    val c = Calendar.getInstance()
                    val month2 = c.get(Calendar.MONTH)
                    if(month2 + 1 - month >= 4){
                        warnCard.visibility = View.VISIBLE
                    }


                    val dbDescIndex = pCursor.getString(descIndex).toString()
                    noteViewField.setText(dbDescIndex)

                    val dbTagsIndex = pCursor.getString(tagsIndex).toString()
                    if(dbTagsIndex != "")
                        keywords.text = dbTagsIndex
                } while (pCursor.moveToNext())
            } else {
                helloTextId.text = getString(R.string.no_text)
            }

        } catch (e: SQLException) {
            helloTextId.text = getString(R.string.no_text)
        }


        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val autoCopy = sharedPref.getString(KEY_AUTOCOPY, "none")

        if(autoCopy == "none" && passViewField.text.toString() != ""){
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", passViewField.text.toString())
            clipboard.setPrimaryClip(clip)
            toast(getString(R.string.passCopied))
        }

        deletePassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.deletePassword))
            builder.setMessage(getString(R.string.passwordDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                pDatabase.delete(
                    pdbHelper.TABLE_USERS,
                    "NAME = ?",
                    arrayOf(dbLogin)
                )
                toast(getString(R.string.passwordDeleted))
                val intent = Intent(this, PassGenActivity::class.java)
                intent.putExtra("login", login)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        accountAvatar.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity","viewPass")
            startActivity(intent)
            finish()
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
                    R.anim.right_out)
                finish()
            }
        }
        return false
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

}