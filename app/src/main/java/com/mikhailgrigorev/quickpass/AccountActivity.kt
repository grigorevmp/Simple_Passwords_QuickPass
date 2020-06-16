package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private val KEY_BIO = "prefUserBioKey"
    private val KEY_AUTOCOPY = "prefAutoCopyKey"
    private val KEY_USEPIN = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String

    @SuppressLint("Recycle", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_account)

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putString(KEY_USERNAME, login)
            commit()
        }


        val useBio = sharedPref.getString(KEY_BIO, "none")
        val useAuto = sharedPref.getString(KEY_AUTOCOPY, "none")
        val usePin = sharedPref.getString(KEY_USEPIN, "none")

        if(useBio == "using"){
            biometricSwitch.isChecked = true
        }

        if(useAuto == "dis"){
            autoCopySwitch.isChecked = false
        }

        if(usePin != "none"){
            setPinSwitch.isChecked = true
            setPinCurrent.alpha = 1F
            setPinCurrent.text = "($usePin)"
        }


        autoCopySwitch.setOnCheckedChangeListener { _, _ ->
            if(!autoCopySwitch.isChecked){
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "dis")
                    commit()
                }
            }
            else{
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "none")
                    commit()
                }
            }
        }

        autoCopy.setOnClickListener {
            if(autoCopySwitch.isChecked){
                autoCopySwitch.isChecked = false
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "dis")
                    commit()
                }
            }
            else{
                autoCopySwitch.isChecked = true
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "none")
                    commit()
                }
            }
        }

        setPinSwitch.setOnCheckedChangeListener { _, _ ->
            if(setPinSwitch.isChecked){
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                finish()
            }
            else{
                with (sharedPref.edit()) {
                    putString(KEY_USEPIN, "none")
                    commit()
                }
                setPinCurrent.alpha = 0F
            }
        }

        setPin.setOnClickListener {
            if(setPinSwitch.isChecked){
                setPinSwitch.isChecked = false
                with (sharedPref.edit()) {
                    putString(KEY_USEPIN, "none")
                    commit()
                }
                setPinCurrent.alpha = 0F
            }
            else{
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                finish()
            }
        }

        biometricSwitch.setOnCheckedChangeListener { _, _ ->
            if(biometricSwitch.isChecked){
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "using")
                    commit()
                }
            }
            else{
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "none")
                    commit()
                }
            }
        }

        biometricText.setOnClickListener {
            if(!biometricSwitch.isChecked){
                biometricSwitch.isChecked = true
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "using")
                    commit()
                }
            }
            else{
                biometricSwitch.isChecked = false
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "none")
                    commit()
                }
            }
        }



        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS, dbHelper.KEY_ID, dbHelper.KEY_IMAGE),
            "NAME = ?", arrayOf(login),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val exInfoPassText = cursor.getString(passIndex).toString()
                val exInfoImgText = cursor.getString(imageIndex).toString()
                passViewField.setText(exInfoPassText)
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
        aboutApp.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        logOut.setOnClickListener {
            exit(sharedPref)
        }

        editAccount.setOnClickListener {
            val intent = Intent(this, EditAccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity", account)
            startActivity(intent)
            finish()
        }

        deleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.accountDelete))
            builder.setMessage(getString(R.string.accountDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                database.delete(dbHelper.TABLE_USERS,
                    "NAME = ?",
                    arrayOf(login))
                val pdbHelper = PasswordsDataBaseHelper(this, login)
                val pDatabase = pdbHelper.writableDatabase
                pDatabase.delete(pdbHelper.TABLE_USERS,
                    null, null)
                toast(getString(R.string.accountDeleted))
                exit(sharedPref)
            }

            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }


    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                when (intent.getStringExtra("activity")) {
                    "menu" -> {
                        val intent = Intent(this, PassGenActivity::class.java)
                        intent.putExtra("login", login)
                        startActivity(intent)
                    }
                    "editPass" -> {
                        val intent = Intent(this, EditPassActivity::class.java)
                        intent.putExtra("login", login)
                        intent.putExtra("passName", passName)
                        startActivity(intent)
                    }
                    "viewPass" -> {
                        val intent = Intent(this, PasswordViewActivity::class.java)
                        intent.putExtra("login", login)
                        intent.putExtra("passName", passName)
                        startActivity(intent)
                    }
                }
                this.overridePendingTransition(R.anim.right_in,
                    R.anim.right_out)
                finish()
            }
        }
        return false
    }

    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(KEY_USERNAME).apply()
        sharedPref.edit().remove(KEY_USEPIN).apply()
        sharedPref.edit().remove(KEY_BIO).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

}