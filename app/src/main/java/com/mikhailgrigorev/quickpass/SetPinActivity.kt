package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_set_pin.*

class SetPinActivity : AppCompatActivity() {
    private val KEY_THEME = "themePreference"
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USEPIN = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String

    @SuppressLint("SetTextI18n", "Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        when(pref.getString(KEY_THEME, "none")){
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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
        setContentView(R.layout.activity_set_pin)

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String? = getString(R.string.hi) + " " + login
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

        num0.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "0")
        }
        num1.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "1")
        }
        num2.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "2")
        }
        num3.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "3")
        }
        num4.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "4")
        }
        num5.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "5")
        }
        num6.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "6")
        }
        num7.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "7")
        }
        num8.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "8")
        }
        num9.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "9")
        }
        erase.setOnClickListener {
            if(inputPinIdField.text.toString().isNotEmpty())
                inputPinIdField.setText(inputPinIdField.text.toString().substring(0, inputPinIdField.text.toString().length - 1))
        }

        inputPinIdField.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(inputPinIdField.text.toString().length == 4){
                    savePin.alpha = 1F
                }
                else{
                    savePin.alpha = 0F
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        savePin.setOnClickListener {
            val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString(KEY_USEPIN, inputPinIdField.text.toString())
                commit()
            }
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity", account)
            startActivity(intent)
            finish()
        }


    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                this.overridePendingTransition(R.anim.right_in,
                        R.anim.right_out)
                finish()
            }
        }
        return false
    }

}