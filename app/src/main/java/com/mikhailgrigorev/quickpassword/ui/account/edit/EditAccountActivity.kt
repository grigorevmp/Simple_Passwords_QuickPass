package com.mikhailgrigorev.quickpassword.ui.account.edit

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.databinding.ActivityEditAccountBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import com.mikhailgrigorev.quickpassword.ui.donut.condition


class EditAccountActivity : AppCompatActivity() {

    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var imageName: String
    private lateinit var binding: ActivityEditAccountBinding

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        when(pref.getString(_keyTheme, "none")){
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
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

        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            if(condition) {
                condition=false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val time: Long =  100000
        val sharedPref2 = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val lockTime = sharedPref2.getString("appLockTime", "6")
        if(lockTime != null) {
            if (lockTime != "0")
                handler.postDelayed(r, time * lockTime.toLong())
        }
        else {
            handler.postDelayed(r, time * 6L)
        }

        binding = ActivityEditAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            setResult(1, intent)
            finish()
        }

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        val name: String = getString(R.string.hi) + " " + login
        binding.tvUsernameText.text = name
        binding.nameViewField.setText(login)

        // Checking prefs
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString(_keyUsername, login)
            commit()
        }

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS,
            arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS, dbHelper.KEY_ID, dbHelper.KEY_IMAGE),
            "NAME = ?",
            arrayOf(login),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val exInfoPassText = cursor.getString(passIndex).toString()
                val exInfoImgText = cursor.getString(imageIndex).toString()
                imageName = exInfoImgText
                binding.passViewField.setText(exInfoPassText)
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


        // Generate random password
        val pm = PasswordManager()
        binding.generatePassword.setOnClickListener {
            binding.tilPassword.error = null
            val newPassword: String =
                    pm.generatePassword(
                            isWithLetters = true,
                            isWithUppercase = true,
                            isWithNumbers = true,
                            isWithSpecial = false,
                            length = 12
                    )
            binding.passViewField.setText(newPassword)

            binding.generatePassword.animate().rotation( (binding.generatePassword.rotation + 45F)%360F).interpolator = AccelerateDecelerateInterpolator()
        }

        binding.savePass.setOnClickListener {
            binding.nameView.error = null
            binding.tilPassword.error = null
            if (binding.nameViewField.text.toString()
                        .isEmpty() || binding.nameViewField.text.toString().length < 3
            ) {
                binding.nameView.error = getString(R.string.errNumOfText)
            } else if (binding.passViewField.text.toString()
                        .isEmpty() || binding.passViewField.text.toString().length < 4 || binding.passViewField.text.toString().length > 20
            ) {
                binding.tilPassword.error = getString(R.string.errPass)
            } else {
                val contentValues = ContentValues()
                contentValues.put(dbHelper.KEY_NAME, binding.nameViewField.text.toString())
                contentValues.put(dbHelper.KEY_PASS, binding.passViewField.text.toString())
                contentValues.put(dbHelper.KEY_IMAGE, imageName)
                database.update(
                    dbHelper.TABLE_USERS, contentValues,
                    "NAME = ?",
                    arrayOf(login)
                )
                // Checking prefs
                with(sharedPref.edit()) {
                    putString(_keyUsername, binding.nameViewField.text.toString())
                    commit()
                }

                val pdbHelper = PasswordsDataBaseHelper(this, login)
                val pDatabase = pdbHelper.writableDatabase
                if (login != binding.nameViewField.text.toString()) {
                    database.execSQL("DROP TABLE IF EXISTS " + binding.nameViewField.text.toString())
                    pDatabase.execSQL("DROP TABLE IF EXISTS " + binding.nameViewField.text.toString())
                    pDatabase.execSQL("ALTER TABLE " + login + " RENAME TO " + binding.nameViewField.text.toString())
                }
                val intent = Intent()
                // Checking prefs

                with(sharedPref.edit()) {
                    putString(_keyUsername, binding.nameViewField.text.toString())
                    commit()
                }

                intent.putExtra("login", binding.nameViewField.text.toString())
                intent.putExtra("passName", binding.passViewField.text.toString())
                setResult(1, intent)
                finish()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                setResult(1, intent)
                finish()
            }
        }
        return false
    }
}
