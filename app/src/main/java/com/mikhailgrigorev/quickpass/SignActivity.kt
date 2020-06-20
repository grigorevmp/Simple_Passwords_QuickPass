package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_sign.*
import java.util.concurrent.Executor

class SignActivity : AppCompatActivity() {

    private val TAG = "SignUpActivity"
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private val KEY_BIO = "prefUserBioKey"
    private val KEY_USEPIN = "prefUsePinKey"
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @SuppressLint("Recycle", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when ((resources.configuration.uiMode + Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_sign)

        val args: Bundle? = intent.extras
        val login: String? = args?.get("login").toString()
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
                accountAvatarText.text = login?.get(0).toString()
            } while (cursor.moveToNext())
        }

        // Start animation
        loginFab.show()

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val username = sharedPref.getString(KEY_BIO, "none")
        if(username != "none"){
            finger.visibility = View.VISIBLE
            finger.isClickable = true
            val intent = Intent(this, PassGenActivity::class.java)
            executor = ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
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

        finger.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }


        // Fab handler
        loginFab.setOnClickListener {
            if (validate(inputPasswordIdField.text.toString()))
                    signIn(login.toString(), inputPasswordIdField.text.toString())

        }

        logOutFab.setOnClickListener {
            exit(sharedPref)
        }
    }
    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(KEY_USERNAME).apply()
        sharedPref.edit().remove(KEY_USEPIN).apply()
        sharedPref.edit().remove(KEY_BIO).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validate(password:String): Boolean {
        var valid = true
        if (password.isEmpty() || password.length < 4 || password.length > 20) {
            inputPasswordId.error = getString(R.string.errPass)
            valid = false
        } else {
            inputPasswordId.error = null
        }
        return valid
    }

    private fun signIn (login:String, password:String){

        Log.d(TAG, "SignIn")

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        var dbLogin: String
        var dbPassword: String

        if (cursor.moveToFirst()) {
            val nameIndex: Int = cursor.getColumnIndex(dbHelper.KEY_NAME)
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            do {
                dbLogin = cursor.getString(nameIndex).toString()
                dbPassword = cursor.getString(passIndex).toString()
                if(dbPassword != password){
                    inputPasswordId.error = getString(R.string.wrong_pass)
                    return
                }
            } while (cursor.moveToNext())
        } else {
            inputPasswordId.error = getString(R.string.wrong_name)
            return
        }

        cursor.close()

        val intent = Intent(this, PassGenActivity::class.java)
        intent.putExtra("login", dbLogin)
        startActivity(intent)
        finish()

    }

}