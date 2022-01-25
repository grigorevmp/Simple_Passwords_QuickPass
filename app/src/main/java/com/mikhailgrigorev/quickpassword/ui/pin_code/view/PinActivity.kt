package com.mikhailgrigorev.quickpassword.ui.pin_code.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.databinding.ActivityPinBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainActivity
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import java.util.concurrent.Executor

class PinActivity : AppCompatActivity() {

    private val _keyUsername = "prefUserNameKey"
    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsePin = "prefUsePinKey"
    private val _keyBio = "prefUserBioKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityPinBinding

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
            else -> setTheme(R.style.Theme_QP)
        }
        super.onCreate(savedInstanceState)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false)
                }
                else{
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
        }
        binding = ActivityPinBinding.inflate(layoutInflater)
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
        binding.tvUsernameText.text = name



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
                    "ic_account" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                    "ic_account_Pink" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink
                    )
                    "ic_account_Red" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red
                    )
                    "ic_account_Purple" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple
                    )
                    "ic_account_Violet" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet
                    )
                    "ic_account_Dark_Violet" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet
                    )
                    "ic_account_Blue" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue
                    )
                    "ic_account_Cyan" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan
                    )
                    "ic_account_Teal" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal
                    )
                    "ic_account_Green" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green
                    )
                    "ic_account_lightGreen" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen
                    )
                    else -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                }
                binding.tvAvatarSymbol.text = login[0].toString()
            } while (cursor.moveToNext())
        }

        // Checking prefs

        val useBio = sharedPref.getString(_keyBio, "none")
        val usePin = sharedPref.getString(_keyUsePin, "none")


        val hasBiometricFeature :Boolean = this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)

        if(hasBiometricFeature) {
            if (useBio != "none") {
                binding.finger.visibility = View.VISIBLE
                binding.finger.isClickable = true
                val intent = Intent(this, MainActivity::class.java)
                executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor,
                        object : BiometricPrompt.AuthenticationCallback() {

                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult
                            ) {
                                super.onAuthenticationSucceeded(result)
                                intent.putExtra("login", login)
                                startActivity(intent)
                                finish()
                            }

                        })

                promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.biometricLogin))
                        .setSubtitle(getString(R.string.logWithBio))
                        .setNegativeButtonText(getString(R.string.usePass))
                        .build()

                // Prompt appears when user clicks "Log in".
                // Consider integrating with the keystore to unlock cryptographic operations,
                // if needed by your app.
                biometricPrompt.authenticate(promptInfo)

            }

            binding.finger.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
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

        binding.exit.setOnClickListener {
            exit(sharedPref)
        }

        val intent = Intent(this, MainActivity::class.java)
        binding.inputPinIdField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(binding.inputPinIdField.text.toString().length == 4){
                    if(binding.inputPinIdField.text.toString() == usePin){
                        intent.putExtra("login", login)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        binding.inputPinId.error = getString(R.string.incorrectPin)
                    }
                }
                else{
                    binding.inputPinId.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

    }
    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(_keyUsername).apply()
        sharedPref.edit().remove(_keyUsePin).apply()
        sharedPref.edit().remove(_keyBio).apply()
        val intent = Intent(this, LoginAfterSplashActivity::class.java)
        startActivity(intent)
        finish()
    }

}