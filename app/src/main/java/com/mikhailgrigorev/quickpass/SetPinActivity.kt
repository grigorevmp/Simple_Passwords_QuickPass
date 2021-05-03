package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpass.databinding.ActivitySetPinBinding
import com.mikhailgrigorev.quickpass.dbhelpers.DataBaseHelper

class SetPinActivity : AppCompatActivity() {
    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsePin = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String
    private lateinit var binding: ActivitySetPinBinding

    @SuppressLint("SetTextI18n", "Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        when(pref.getString(_keyTheme, "none")){
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
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = ActivitySetPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val cardRadius = sharedPref.getString("cardRadius", "none")
        if(cardRadius != null)
            if(cardRadius != "none") {
                binding.cardNums.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
            }

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String = getString(R.string.hi) + " " + login
        binding.helloTextId.text = name


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
                    "ic_account" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                    "ic_account_Pink" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink)
                    "ic_account_Red" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red)
                    "ic_account_Purple" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple)
                    "ic_account_Violet" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet)
                    "ic_account_Dark_Violet" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet)
                    "ic_account_Blue" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue)
                    "ic_account_Cyan" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan)
                    "ic_account_Teal" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal)
                    "ic_account_Green" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green)
                    "ic_account_lightGreen" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen)
                    else -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                }
                binding.accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }


        binding.num0.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "0")
        }
        binding.num1.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "1")
        }
        binding.num2.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "2")
        }
        binding.num3.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "3")
        }
        binding.num4.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "4")
        }
        binding.num5.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "5")
        }
        binding.num6.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "6")
        }
        binding.num7.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "7")
        }
        binding.num8.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "8")
        }
        binding.num9.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "9")
        }
        binding.erase.setOnClickListener {
            if(binding.inputPinIdField.text.toString().isNotEmpty())
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString().substring(0, binding.inputPinIdField.text.toString().length - 1))
        }



        binding.inputPinIdField.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(binding.inputPinIdField.text.toString().length == 4){
                    binding.savePin.alpha = 1F
                }
                else{
                    binding.savePin.alpha = 0F
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.savePin.setOnClickListener {
            val sharedPref2 = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
            with (sharedPref2.edit()) {
                putString(_keyUsePin, binding.inputPinIdField.text.toString())
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