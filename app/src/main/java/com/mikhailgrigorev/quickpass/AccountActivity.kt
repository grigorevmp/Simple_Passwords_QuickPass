package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_account.*


class AccountActivity : AppCompatActivity() {

    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
    private val _keyBio = "prefUserBioKey"
    private val _keyUsePin = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String

    private val realPass: ArrayList<Pair<String, String>> = ArrayList()
    private val realQuality: ArrayList<String> = ArrayList()
    private val realMap: MutableMap<String, ArrayList<String>> = mutableMapOf()

    @SuppressLint("Recycle", "SetTextI18n")
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
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_account)

        val args: Bundle? = intent.extras
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        login = args?.get("login").toString()
        val newLogin = sharedPref.getString(_keyUsername, login)
        if(newLogin != login)
            login = newLogin.toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name

        // Checking prefs

        with(sharedPref.edit()) {
            putString(_keyUsername, login)
            commit()
        }

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
                dbHelper.TABLE_USERS, arrayOf(
                dbHelper.KEY_NAME,
                dbHelper.KEY_PASS,
                dbHelper.KEY_ID,
                dbHelper.KEY_IMAGE
        ),
                "NAME = ?", arrayOf(login),
                null, null, null
        )

        if (cursor.moveToFirst()) {
            val passIndex: Int = cursor.getColumnIndex(dbHelper.KEY_PASS)
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val exInfoPassText = cursor.getString(passIndex).toString()
                passViewField.setText(exInfoPassText)
                when(cursor.getString(imageIndex).toString()){
                    "ic_account" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account
                            )
                    "ic_account_Pink" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Pink
                            )
                    "ic_account_Red" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Red
                            )
                    "ic_account_Purple" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Purple
                            )
                    "ic_account_Violet" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Violet
                            )
                    "ic_account_Dark_Violet" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Dark_Violet
                            )
                    "ic_account_Blue" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Blue
                            )
                    "ic_account_Cyan" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Cyan
                            )
                    "ic_account_Teal" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Teal
                            )
                    "ic_account_Green" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Green
                            )
                    "ic_account_lightGreen" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_lightGreen
                            )
                    else -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                }
                accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }
        aboutApp.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        try {
            val pCursor: Cursor = pDatabase.query(
                    pdbHelper.TABLE_USERS, arrayOf(
                    pdbHelper.KEY_NAME,
                    pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA,
                    pdbHelper.KEY_TAGS,
                    pdbHelper.KEY_GROUPS,
                    pdbHelper.KEY_USE_TIME,
                    pdbHelper.KEY_TIME
            ),
                    null, null,
                    null, null, null
            )

            var correctNum = 0
            var inCorrectNum = 0
            var midCorrectNum = 0
            var faNum = 0
            var tlNum = 0

            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val login = pCursor.getString(nameIndex).toString()
                    realPass.add(Pair(login, pass))
                } while (pCursor.moveToNext())
            }

            analyzeDataBase()

            if (pCursor.moveToFirst()) {
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
                val tIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_USE_TIME)
                val timeIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TIME)
                var j = 0
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val myPasswordManager = PasswordManager()
                    var evaluation: Float =
                            myPasswordManager.evaluatePassword(pass)


                    val dbTimeIndex = pCursor.getString(timeIndex).toString()

                    if((myPasswordManager.evaluateDate(dbTimeIndex)) && (pass.length!= 4))
                        evaluation = 0F


                    if(realQuality[j] != "1")
                        evaluation = 0F
                    j++
                    when{
                        evaluation < 0.33 -> inCorrectNum += 1
                        evaluation < 0.66 -> midCorrectNum += 1
                        else -> correctNum += 1
                    }

                    val fa = pCursor.getString(aIndex).toString()
                    val tl= pCursor.getString(tIndex).toString()

                    if(fa == "1")
                        faNum += 1

                    if(tl == "1")
                        tlNum += 1

                } while (pCursor.moveToNext())
            }

            correctPasswords.text = resources.getQuantityString(
                    R.plurals.correct_passwords,
                    correctNum,
                    correctNum
            )
            negativePasswords.text = resources.getQuantityString(
                    R.plurals.incorrect_password,
                    inCorrectNum,
                    inCorrectNum
            )
            fixPasswords.text = resources.getQuantityString(
                    R.plurals.need_fix,
                    midCorrectNum,
                    midCorrectNum
            )

            afText.text = faNum.toString()
            tlText.text = tlNum.toString()

            allPass.text = (correctNum+ inCorrectNum + midCorrectNum).toString()

            realPoints.text = ((correctNum.toFloat() + midCorrectNum.toFloat()/2 + inCorrectNum.toFloat()*0 + tlNum.toFloat() + faNum.toFloat())
                    /(7/3*(correctNum.toFloat() + inCorrectNum.toFloat() + midCorrectNum.toFloat())))
                    .toString()

        } catch (e: SQLException) {
        }

        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotation.fillAfter = true
        settings.startAnimation(rotation)

        logOut.setOnClickListener {
            exit(sharedPref)
        }

        editAccount.setOnClickListener {
            val intent = Intent(this, EditAccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity", account)
            startActivityForResult(intent, 1)
        }

        settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity", account)
            startActivityForResult(intent, 1)
        }

        deleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.accountDelete))
            builder.setMessage(getString(R.string.accountDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                database.delete(
                        dbHelper.TABLE_USERS,
                        "NAME = ?",
                        arrayOf(login)
                )
                pDatabase.delete(
                        pdbHelper.TABLE_USERS,
                        null, null
                )
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
                this.overridePendingTransition(
                        R.anim.right_in,
                        R.anim.right_out
                )
                finish()
            }
        }
        return false
    }

    private fun analyzeDataBase() {
        var subContains: Boolean
        var gSubContains: Boolean
        for (pass in realPass){
            subContains = false
            gSubContains = false
            for (pass2 in realPass){
                if(pass.first != pass2.first){
                    for(i in 0..(pass.second.length - 4)){
                        if (pass2.second.contains(pass.second.subSequence(i, i + 3))){
                            subContains = true
                            gSubContains = true
                            break
                        }
                    }
                    if (subContains)
                        if (realMap.containsKey(pass.first))
                            realMap[pass.first]?.add(pass2.first)
                        else {
                            val c = arrayListOf(pass2.first)
                            realMap[pass.first] = c
                        }
                    subContains = false
                }
            }
            if (gSubContains) {
                realQuality.add("0")
            }
            else
                realQuality.add("1")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 1) {
                recreate()
            }
        }
    }
    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(_keyUsername).apply()
        sharedPref.edit().remove(_keyUsePin).apply()
        sharedPref.edit().remove(_keyBio).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message: String)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}