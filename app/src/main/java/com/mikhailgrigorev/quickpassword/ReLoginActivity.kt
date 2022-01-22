package com.mikhailgrigorev.quickpassword

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.mikhailgrigorev.quickpassword.databinding.ActivityLoginBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.sender.GMailSender
import kotlin.random.Random

class ReLoginActivity : AppCompatActivity() {

    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
    private val _keyBio = "prefUserBioKey"
    private val _keyUsePin = "prefUsePinKey"
    private val _tag = "SignUpActivity"
    private val DEFAULT_ROTATION = 45F
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)

        with(pref.edit()) {
            putInt(
                    "__BS",
                    com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
            )
            apply()
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false)
                }
                else{
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start animation
        binding.loginFab.show()

        // Checking prefs
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val username = sharedPref.getString(_keyUsername, "none")

        val usePin = sharedPref.getString(_keyUsePin, "none")
        if(usePin != "none"){
            val intent = Intent(this, RePinActivity::class.java)
            intent.putExtra("login", username)
            startActivityForResult(intent, 1)
            finish()
        }
        else if(username != "none"){
            val intent = Intent(this, ReSignActivity::class.java)
            intent.putExtra("login", username)
            startActivity(intent)
            finish()
        }


        // Fab handler
        binding.loginFab.setOnClickListener {
            if (binding.signUpChip.isChecked){
                if (validate(
                            binding.inputLoginIdField.text.toString(),
                            binding.inputPasswordIdField.text.toString()
                    ))
                    signUp(binding.inputLoginIdField.text.toString(), binding.inputPasswordIdField.text.toString())
            }
            else{
                if (validate(
                            binding.inputLoginIdField.text.toString(),
                            binding.inputPasswordIdField.text.toString()
                    ))
                    signIn(binding.inputLoginIdField.text.toString(), binding.inputPasswordIdField.text.toString())
            }
        }

        // Chip handler
        binding.signUpChipGroup.setOnCheckedChangeListener{ _, _ ->
            // Get the checked chip instance from chip group
            binding.signUpChip.let {
                if (binding.signUpChip.isChecked){
                    binding.loginFab.hide()
                    binding.loginFab.text = getString(R.string.sign_up)
                    binding.loginFab.show()
                } else{
                    binding.loginFab.hide()
                    binding.loginFab.text = getString(R.string.sign_in)
                    binding.loginFab.show()
                }
            }
        }

        // Generate random password
        val pm = PasswordManager()
        binding.generatePassword.setOnClickListener {
            binding.inputPasswordId.error = null
            val newPassword: String =
                    pm.generatePassword(
                            isWithLetters = true,
                            isWithUppercase = true,
                            isWithNumbers = true,
                            isWithSpecial = false,
                            length = 12
                    )
            binding.inputPasswordIdField.setText(newPassword)

            binding.generatePassword.animate().rotation( (binding.generatePassword.rotation + DEFAULT_ROTATION)%360F).interpolator = AccelerateDecelerateInterpolator()
        }

        binding.sendMail.setOnClickListener {
            val login = binding.inputLoginIdField.text.toString()


            val dbHelper = DataBaseHelper(this)
            val database = dbHelper.writableDatabase
            val cursor: Cursor = database.query(
                    dbHelper.TABLE_USERS, arrayOf(
                    dbHelper.KEY_NAME,
                    dbHelper.KEY_MAIL,
                    dbHelper.KEY_PASS
            ),
                    "NAME = ?", arrayOf(login),
                    null, null, null
            )

            if (cursor.moveToFirst()) {
                val mailIndex: Int = cursor.getColumnIndex(dbHelper.KEY_MAIL)
                val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
                do {
                    val dbMail = cursor.getString(mailIndex).toString()
                    val dbPass = cursor.getString(passIndex).toString()
                    val sender = GMailSender(
                            hidden_email, //USE YOUR OWN
                            hidden_password
                    )
                    if (dbMail != "none") {
                        Thread {
                            try {
                                sender.sendMail(
                                        "QuickPass- Password restoring",
                                        "Hello! Seems like you forgot your password and decided to restore it.\n\n" +
                                                "Your password: $dbPass \n\n" +
                                                "Have a good day, \n" +
                                                "QuickPass =)",
                                        "grigorevmp@gmail.com",
                                        dbMail
                                )

                            } catch (e: Exception) {
                                Log.e("SendMail", e.message, e)
                            }
                        }.start()
                        Toast.makeText(
                                this,
                                getString(R.string.sendMail2) + "\n ($dbMail)",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        Toast.makeText(this, getString(R.string.sendMail3), Toast.LENGTH_SHORT)
                                .show()
                    }
                } while (cursor.moveToNext())
            } else {
                Toast.makeText(this, getString(R.string.sendMail1), Toast.LENGTH_SHORT).show()
            }
        }
        
        
    }

    private fun validate(login: String, password: String): Boolean {
        var valid = false
        if (login.isEmpty() || login.length < 3) {
            binding.inputLoginId.error = getString(R.string.errNumOfText)
        } else {
            binding.inputLoginId.error = null
            valid = true
        }
        if (password.isEmpty() || password.length < 4 || password.length > 20) {
            binding.inputPasswordId.error = getString(R.string.errPass)
            valid = false
        } else {
            binding.inputPasswordId.error = null
        }
        return valid
    }

    @SuppressLint("Recycle")
    private fun signUp(login: String, password: String) {

        Log.d(_tag, "SignUp")

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val contentValues = ContentValues()

        val cursor: Cursor = database.query(
                dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS),
                "NAME = ?", arrayOf(login),
                null, null, null
        )

        if (cursor.moveToFirst()) {
            binding.inputLoginId.error = getString(R.string.exists)
            return
        } else {
            contentValues.put(dbHelper.KEY_ID, Random.nextInt(0, 100))
            contentValues.put(dbHelper.KEY_NAME, login)
            contentValues.put(dbHelper.KEY_PASS, password)
            contentValues.put(dbHelper.KEY_IMAGE, "ic_account")
            contentValues.put(dbHelper.KEY_MAIL, "none")
            database.insert(dbHelper.TABLE_USERS, null, contentValues)
        }

        signIn(login, password)
    }

    private fun signIn(login: String, password: String){

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
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            do {
                dbPassword = cursor.getString(passIndex).toString()
                if(dbPassword != password){
                    binding.inputPasswordId.error = getString(R.string.wrong_pass)
                    return
                }
            } while (cursor.moveToNext())
        } else {
            binding.inputLoginId.error = getString(R.string.wrong_name)
            return
        }

        cursor.close()

        // создание объекта Intent для запуска SecondActivity

        if(isAvailable(this)){
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.bio_usage))
            builder.setMessage(getString(R.string.fingerUnlock))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                // Checking prefs
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(_keyBio, "using")
                    commit()
                }
                val intent = intent
                setResult(2, intent)
                finish()
            }

            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(_keyBio, "none")
                    commit()
                }
                val intent = intent
                setResult(2, intent)
                finish()
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(_keyBio, "none")
                    commit()
                }
                val intent = intent
                setResult(2, intent)
                finish()
            }
            builder.setCancelable(false)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        else{
            val intent = intent
            setResult(2, intent)
            finish()
        }
    }
    private fun isAvailable(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 1) {
                val intent = intent
                setResult(2, intent)
                finish()
            }
        }
    }
}