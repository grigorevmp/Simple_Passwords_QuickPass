package com.mikhailgrigorev.quickpassword.ui.auth.re.sign

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.databinding.ActivitySignBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import java.util.concurrent.Executor

class ReSignActivity : AppCompatActivity() {

    private val _tag = "SignUpActivity"
    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
    private val _keyBio = "prefUserBioKey"
    private val _keyUsePin = "prefUsePinKey"
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivitySignBinding

    @SuppressLint("Recycle", "ResourceAsColor")
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
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val args: Bundle? = intent.extras
        val login: String = args?.get("login").toString()
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
                            this, R.color.ic_account
                    )
                    "ic_account_Pink" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink
                    )
                    "ic_account_Red" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red
                    )
                    "ic_account_Purple" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple
                    )
                    "ic_account_Violet" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet
                    )
                    "ic_account_Dark_Violet" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet
                    )
                    "ic_account_Blue" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue
                    )
                    "ic_account_Cyan" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan
                    )
                    "ic_account_Teal" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal
                    )
                    "ic_account_Green" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green
                    )
                    "ic_account_lightGreen" -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen
                    )
                    else -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                }
                binding.accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }

        // Start animation
        binding.loginFab.show()

        val hasBiometricFeature :Boolean = this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)

        // Checking prefs
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val username = sharedPref.getString(_keyBio, "none")
        if(hasBiometricFeature) {
            if (username != "none") {
                binding.finger.visibility = View.VISIBLE
                binding.finger.isClickable = true
                executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor,
                        object : BiometricPrompt.AuthenticationCallback() {

                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult
                            ) {
                                super.onAuthenticationSucceeded(result)
                                val intent = intent
                                setResult(2, intent)
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


        // Fab handler
        binding.loginFab.setOnClickListener {
            if (validate(binding.inputPasswordIdField.text.toString()))
                    signIn(login, binding.inputPasswordIdField.text.toString())

        }

        binding.logOutFab.setOnClickListener {
            exit(sharedPref)
        }
    }
    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(_keyUsername).apply()
        sharedPref.edit().remove(_keyUsePin).apply()
        sharedPref.edit().remove(_keyBio).apply()


        val shortcutList = mutableListOf<ShortcutInfo>()

        val shortcutManager: ShortcutManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager =
                    getSystemService(ShortcutManager::class.java)!!

            shortcutManager.dynamicShortcuts = shortcutList
        }

        val intent = Intent(this, LoginAfterSplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validate(password:String): Boolean {
        var valid = true
        if (password.isEmpty() || password.length < 4 || password.length > 20) {
            binding.inputPasswordId.error = getString(R.string.errPass)
            valid = false
        } else {
            binding.inputPasswordId.error = null
        }
        return valid
    }

    private fun signIn (login:String, password:String){

        Log.d(_tag, "SignIn")

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        var dbPassword: String

        if (cursor.moveToFirst()) {
            cursor.getColumnIndex(dbHelper.KEY_NAME)
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            do {
                dbPassword = cursor.getString(passIndex).toString()
                if(dbPassword != password){
                    binding.inputPasswordId.error = getString(R.string.wrong_pass)
                    return
                }
            } while (cursor.moveToNext())
        } else {
            binding.inputPasswordId.error = getString(R.string.wrong_name)
            return
        }

        cursor.close()

        val intent = intent
        setResult(2, intent)
        finish()

    }

}