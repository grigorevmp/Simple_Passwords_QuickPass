package com.mikhailgrigorev.quickpass

//import com.anjlab.android.iab.v3.BillingProcessor
//import com.anjlab.android.iab.v3.TransactionDetails
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpass.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpass.dbhelpers.PasswordsDataBaseHelper
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
    private val _keyBio = "prefUserBioKey"
    private val _keyUsePin = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String

    private val realPass: ArrayList<Pair<String, String>> = ArrayList()
    private val realQuality: ArrayList<String> = ArrayList()
    private val realMap: MutableMap<String, ArrayList<String>> = mutableMapOf()

    var condition = true

    @SuppressLint("Recycle", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        // Set Theme

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

        // Finish app after some time
        val handler = Handler()
        val r = Runnable {
            if(condition) {
                condition=false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        handler.postDelayed(r, 600000)

        setContentView(R.layout.activity_account)

        // Get Extras
        val args: Bundle? = intent.extras
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        login = args?.get("login").toString()

        // Set login
        val newLogin = sharedPref.getString(_keyUsername, login)
        if(newLogin != login)
            login = newLogin.toString()

        //Set pass
        passName = args?.get("passName").toString()

        // Set greeting
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name

        // Checking prefs

        with(sharedPref.edit()) {
            putString(_keyUsername, login)
            commit()
        }

        // Open users database
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
            condition=false
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
        cardCup.setOnClickListener {
            condition=false
            val intent = Intent(this, DonutActivity::class.java)
            startActivity(intent)
        }
        cursor.close()

        // Open passwords database
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
                    pdbHelper.KEY_CIPHER,
                    pdbHelper.KEY_TIME
            ),
                    null, null,
                    null, null, null
            )

            var correctNum = 0
            var inCorrectNum = 0
            var midCorrectNum = 0
            var crNum = 0
            var faNum = 0
            var tlNum = 0
            var pinNum = 0

            // First scan to analyze same passes
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

            // Second scan to set quality
            if (pCursor.moveToFirst()) {
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
                val tIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_USE_TIME)
                val timeIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TIME)
                val cIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_CIPHER)
                var j = 0
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val myPasswordManager = PasswordManager()
                    val evaluation: Float = myPasswordManager.evaluatePassword(pass)
                    val dbCipherIndex = pCursor.getString(cIndex).toString()

                    var qualityNum = when {
                        evaluation < 0.33 -> "2"
                        evaluation < 0.66 -> "3"
                        else -> "1"
                    }


                    if (dbCipherIndex == "crypted" ) {
                        qualityNum = "6"
                        crNum += 1
                    }

                    val dbTimeIndex = pCursor.getString(timeIndex).toString()
                    if (myPasswordManager.evaluateDate(dbTimeIndex))
                        qualityNum = "2"

                    if (realQuality[j] != "1")
                        qualityNum = "2"

                    if (dbCipherIndex != "crypted" && pass.length == 4) {
                        qualityNum = "4"
                        pinNum += 1
                    }

                    j++

                    val fa = pCursor.getString(aIndex).toString()
                    val tl= pCursor.getString(tIndex).toString()

                    if(fa == "1")
                        faNum += 1

                    if(tl == "1")
                        tlNum += 1

                    when (qualityNum) {
                        "1" -> correctNum += 1
                        "2" -> inCorrectNum += 1
                        "3" -> midCorrectNum += 1
                        "4" -> correctNum += 1
                        "6" -> correctNum += 1
                    }

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
            crText.text = crNum.toString()
            pinText.text = pinNum.toString()
            allPass.text = (correctNum+ inCorrectNum + midCorrectNum).toString()

            realPoints.text = ((correctNum.toFloat() + midCorrectNum.toFloat()/2 + inCorrectNum.toFloat()*0 + tlNum.toFloat() + faNum.toFloat())
                    /(7/3*(correctNum.toFloat() + inCorrectNum.toFloat() + midCorrectNum.toFloat())))
                    .toString()
            pCursor.close()
        } catch (e: SQLException) {
        }

        // Settings button animation
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotation.fillAfter = true
        settings.startAnimation(rotation)




        // Log out button
        logOut.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.exit_account))
            builder.setMessage(getString(R.string.accountExitConfirm))
            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                exit(sharedPref)
            }
            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }


        // Edit button
        editAccount.setOnClickListener {
            condition=false
            val intent = Intent(this, EditAccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            startActivityForResult(intent, 1)
        }

        // Settings button
        settings.setOnClickListener {
            condition=false
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            startActivityForResult(intent, 1)
        }

        // Delete button
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
        back.setOnClickListener {
            //logo.visibility = View.VISIBLE
            //val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_splash)
            //rotation.fillAfter = true
            //logo.startAnimation(rotation)
            condition=false
            val intent = Intent()
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            setResult(1, intent)
            finish()
        }
    }



    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                //logo.visibility = View.VISIBLE
                //val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_splash)
                //rotation.fillAfter = true
                //logo.startAnimation(rotation)
                condition=false
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                setResult(1, intent)
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
        condition=false
        sharedPref.edit().remove(_keyUsername).apply()
        sharedPref.edit().remove(_keyUsePin).apply()
        sharedPref.edit().remove(_keyBio).apply()
        val intent = Intent(this, LoginAfterSplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message: String)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}