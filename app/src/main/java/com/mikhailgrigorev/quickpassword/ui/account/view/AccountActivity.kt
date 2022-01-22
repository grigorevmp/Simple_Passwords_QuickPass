package com.mikhailgrigorev.quickpassword.ui.account.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.databinding.ActivityAccountBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.about.AboutActivity
import com.mikhailgrigorev.quickpassword.ui.account.edit.EditAccountActivity
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import com.mikhailgrigorev.quickpassword.ui.donut.DonutActivity
import com.mikhailgrigorev.quickpassword.ui.settings.SettingsActivity

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

    private var condition = true
    private lateinit var binding: ActivityAccountBinding

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false)
                }
                else{
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
        }

        // Finish app after some time
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
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)

        val lockTime = sharedPref.getString("appLockTime", "6")
        if(lockTime != null) {
            if (lockTime != "0")
                handler.postDelayed(r, time * lockTime.toLong())
        }
        else
            handler.postDelayed(r, time*6L)

        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val cardRadius = sharedPref.getString("cardRadius", "none")
        if(cardRadius != null)
            if(cardRadius != "none") {
                binding.cardCup.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.crypted.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.warnCard.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.settingsCard.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.specialInfo.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.correctScan.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
                binding.cardView.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
            }


        val useAnalyze = sharedPref.getString("useAnalyze", "none")
        if (useAnalyze != null)
            if (useAnalyze != "none"){
                binding.totalPoints.visibility = View.GONE
                binding.realPoints.visibility = View.GONE
                binding.correctScan.visibility = View.GONE
                binding.cardView.visibility = View.GONE
                binding.specialInfo.visibility = View.GONE
                binding.crypted.visibility = View.GONE
            }


        // Get Extras
        val args: Bundle? = intent.extras
        login = args?.get("login").toString()

        // Set login
        val newLogin = sharedPref.getString(_keyUsername, login)
        if(newLogin != login)
            login = newLogin.toString()

        //Set pass
        passName = args?.get("passName").toString()

        // Set greeting
        val name: String = getString(R.string.hi) + " " + login
        binding.helloTextId.text = name

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
                binding.passViewField.setText(exInfoPassText)
                when(cursor.getString(imageIndex).toString()){
                    "ic_account" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account
                            )
                    "ic_account_Pink" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Pink
                            )
                    "ic_account_Red" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Red
                            )
                    "ic_account_Purple" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Purple
                            )
                    "ic_account_Violet" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Violet
                            )
                    "ic_account_Dark_Violet" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Dark_Violet
                            )
                    "ic_account_Blue" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Blue
                            )
                    "ic_account_Cyan" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Cyan
                            )
                    "ic_account_Teal" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Teal
                            )
                    "ic_account_Green" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Green
                            )
                    "ic_account_lightGreen" -> binding.accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_lightGreen
                            )
                    else -> binding.accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                }
                binding.accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }
        binding.aboutApp.setOnClickListener {
            condition=false
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
        binding.cardCup.setOnClickListener {
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

            binding.correctPasswords.text = resources.getQuantityString(
                    R.plurals.correct_passwords,
                    correctNum,
                    correctNum
            )
            binding.negativePasswords.text = resources.getQuantityString(
                    R.plurals.incorrect_password,
                    inCorrectNum,
                    inCorrectNum
            )
            binding.notSafePasswords.text = resources.getQuantityString(
                    R.plurals.need_fix,
                    midCorrectNum,
                    midCorrectNum
            )

            binding.afText.text = faNum.toString()
            binding.tlText.text = tlNum.toString()
            binding.crText.text = crNum.toString()
            binding.pinText.text = pinNum.toString()
            binding.allPass.text = (correctNum+ inCorrectNum + midCorrectNum).toString()

            if(binding.allPass.text.toString() != "0") {
                binding.realPoints.text =
                        ((correctNum.toFloat() + midCorrectNum.toFloat() / 2 + inCorrectNum.toFloat() * 0 + tlNum.toFloat() + faNum.toFloat())
                                / (7 / 3 * (correctNum.toFloat() + inCorrectNum.toFloat() + midCorrectNum.toFloat())))
                                .toString()
            }
            else{
                binding.realPoints.text = "0"
            }
            pCursor.close()
        } catch (e: SQLException) {
        }

        // Settings button animation
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotation.fillAfter = true
        binding.settings.startAnimation(rotation)




        // Log out button
        binding.logOut.setOnClickListener {
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
        binding.editAccount.setOnClickListener {
            condition=false
            val intent = Intent(this, EditAccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            startActivityForResult(intent, 1)
        }

        // Settings button
        binding.settings.setOnClickListener {
            condition=false
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            startActivityForResult(intent, 1)
        }

        // Delete button
        binding.deleteAccount.setOnClickListener {
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
        binding.back.setOnClickListener {
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
        condition = false
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

    private fun Context.toast(message: String)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}