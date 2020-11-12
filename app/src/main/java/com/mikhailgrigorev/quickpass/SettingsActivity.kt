package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpass.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpass.dbhelpers.PasswordsDataBaseHelper
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class SettingsActivity : AppCompatActivity() {

    private val _keyTheme = "themePreference"
    private val _keyThemeAccent = "themeAccentPreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsername = "prefUserNameKey"
    private val _keyBio = "prefUserBioKey"
    private val _keyAutoCopy = "prefAutoCopyKey"
    private val _keyUsePin = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var imageName: String
    var condition = true
    @SuppressLint("SetTextI18n", "Recycle", "RestrictedApi")
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
        val time: Long =  100000
        val sharedPref2 = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val lockTime2 = sharedPref2.getString("appLockTime", "6")
        if(lockTime2 != null) {
            if (lockTime2 != "0")
                handler.postDelayed(r, time * lockTime2.toLong())
        }
        else
            handler.postDelayed(r, time*6L)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_settings)

        when(AppCompatDelegate.getDefaultNightMode()){
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> defaultSystem.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> light.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> dark.isChecked = true
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> autoBattery.isChecked = true
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> defaultSystem.isChecked = true
            else-> defaultSystem.isChecked = true
        }


        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()

        // Checking prefs
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString(_keyUsername, login)
            commit()
        }

        //THEME
        // Получаем экземпляр элемента Spinner

        light.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "no")
                commit()
            }
            light.isChecked = true
            recreate()
        }
        dark.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "yes")
                commit()
            }
            dark.isChecked = true
            recreate()
        }
        autoBattery.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "battery")
                commit()
            }
            autoBattery.isChecked = true
            recreate()
        }
        defaultSystem.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "default")
                commit()
            }
            defaultSystem.isChecked = true
            recreate()
        }


        val useBio = sharedPref.getString(_keyBio, "none")
        val useAuto = sharedPref.getString(_keyAutoCopy, "none")
        val usePin = sharedPref.getString(_keyUsePin, "none")

        val useAnalyze = sharedPref.getString("useAnalyze", "none")

        val lockTime = sharedPref.getString("appLockTime", "none")
        appLockTime.text = "6m"
        if(lockTime != null)
            if(lockTime != "none") {
            appLockBar.progress = lockTime.toInt()
            appLockTime.text = lockTime.toInt().toString() + "m"
            if (lockTime.toInt() == 0){
                appLockTime.text = getString(R.string.dontlock)
            }
        }



        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
                dbHelper.TABLE_USERS,
                arrayOf(
                        dbHelper.KEY_NAME,
                        dbHelper.KEY_PASS,
                        dbHelper.KEY_ID,
                        dbHelper.KEY_IMAGE,
                        dbHelper.KEY_MAIL
                ),
                "NAME = ?",
                arrayOf(login),
                null,
                null,
                null
        )


        var dbMail: String


        if (cursor.moveToFirst()) {
            val mailIndex: Int = cursor.getColumnIndex(dbHelper.KEY_MAIL)
            do {
                dbMail = cursor.getString(mailIndex).toString()
            } while (cursor.moveToNext())
        } else {
            return
        }


        var mailSet = false

        if(dbMail != "none"){
            userMailSwitch.isChecked = true
            mailSet = true
        }


        if(useBio == "using"){
            biometricSwitch.isChecked = true
        }

        if(useAuto == "dis"){
            autoCopySwitch.isChecked = false
        }

        if(usePin != "none"){
            setPinSwitch.isChecked = true
        }

        if(useAnalyze != "none"){
            userAnalyzerSwitch.isChecked = true
        }

        userAnalyzerSwitch.setOnCheckedChangeListener { _, _ ->
            if(!userAnalyzerSwitch.isChecked){
                with(sharedPref.edit()) {
                    putString("useAnalyze", "none")
                    commit()
                }
            }
            else{
                with(sharedPref.edit()) {
                    putString("useAnalyze", "yes")
                    commit()
                }
            }
        }


        userAnalyzer.setOnClickListener {
            if(userAnalyzerSwitch.isChecked){
                userAnalyzerSwitch.isChecked = false
                with(sharedPref.edit()) {
                    putString("useAnalyze", "none")
                    commit()
                }
            }
            else{
                userAnalyzerSwitch.isChecked = true
                with(sharedPref.edit()) {
                    putString("useAnalyze", "yes")
                    commit()
                }
            }
        }

        userMail.setOnClickListener {
            if(userMailSwitch.isChecked){
                userMailSwitch.isChecked = false
                mailSet = false
                val newMail = "none"
                val contentValues = ContentValues()
                contentValues.put(dbHelper.KEY_MAIL, newMail)
                database.update(
                        dbHelper.TABLE_USERS, contentValues,
                        "NAME = ?",
                        arrayOf(login)
                )
            }
            else{
                var newMail: String
                val inputEditTextField =  EditText(this)
                inputEditTextField.setSingleLine()
                val dialog =  AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setTitle(getString(R.string.newMail))
                        .setMessage(getString(R.string.mail_description))
                        .setView(inputEditTextField, 100, 100, 100, 100)
                        .setPositiveButton(getString(R.string.saveButton)){ _, _ ->
                            newMail = inputEditTextField.text.toString()
                            val contentValues = ContentValues()
                            contentValues.put(dbHelper.KEY_MAIL, newMail)
                            database.update(
                                    dbHelper.TABLE_USERS, contentValues,
                                    "NAME = ?",
                                    arrayOf(login)
                            )
                            mailSet = true
                            userMailSwitch.isChecked = true
                        }
                        .setNegativeButton(getString(R.string.closeButton), null)
                        .create()
                dialog.show()
            }
        }


        userMailSwitch.setOnCheckedChangeListener { _, _ ->
            if(!userMailSwitch.isChecked and mailSet){
                mailSet = false
                val newMail = "none"
                val contentValues = ContentValues()
                contentValues.put(dbHelper.KEY_MAIL, newMail)
                database.update(
                        dbHelper.TABLE_USERS, contentValues,
                        "NAME = ?",
                        arrayOf(login)
                )
            }
            else if (!mailSet){
                var newMail: String
                val inputEditTextField =  EditText(this)
                inputEditTextField.setSingleLine()



                val dialog =  AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setTitle(getString(R.string.newMail))
                        .setMessage(getString(R.string.mail_description))
                        .setView(inputEditTextField, 100, 100, 100, 100)
                        .setPositiveButton(getString(R.string.saveButton)){ _, _ ->
                            newMail = inputEditTextField.text.toString()
                            val contentValues = ContentValues()
                            contentValues.put(dbHelper.KEY_MAIL, newMail)
                            database.update(
                                    dbHelper.TABLE_USERS, contentValues,
                                    "NAME = ?",
                                    arrayOf(login)
                            )
                            mailSet = true
                            userMailSwitch.isChecked = true
                        }
                        .setNegativeButton(getString(R.string.closeButton), null)
                        .create()
                dialog.show()

            }
        }

        autoCopySwitch.setOnCheckedChangeListener { _, _ ->
            if(!autoCopySwitch.isChecked){
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "dis")
                    commit()
                }
            }
            else{
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "none")
                    commit()
                }
            }
        }


        autoCopy.setOnClickListener {
            if(autoCopySwitch.isChecked){
                autoCopySwitch.isChecked = false
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "dis")
                    commit()
                }
            }
            else{
                autoCopySwitch.isChecked = true
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "none")
                    commit()
                }
            }
        }

        setPinSwitch.setOnCheckedChangeListener { _, _ ->
            if(setPinSwitch.isChecked){
                condition=false
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                startActivity(intent)
                finish()
            }
            else{
                condition=false
                with(sharedPref.edit()) {
                    putString(_keyUsePin, "none")
                    commit()
                }
            }
        }

        setPin.setOnClickListener {
            if(setPinSwitch.isChecked){
                setPinSwitch.isChecked = false
                with(sharedPref.edit()) {
                    putString(_keyUsePin, "none")
                    commit()
                }
            }
            else{
                condition=false
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                startActivity(intent)
                finish()
            }
        }

        appLockBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                appLockTime.text = i.toString() + "m"
                if (i == 0){
                    appLockTime.text = getString(R.string.dontlock)
                }
                with(sharedPref.edit()) {
                    putString("appLockTime", i.toString())
                    commit()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        biometricSwitch.setOnCheckedChangeListener { _, _ ->
            if(biometricSwitch.isChecked){
                with(sharedPref.edit()) {
                    putString(_keyBio, "using")
                    commit()
                }
            }
            else{
                with(sharedPref.edit()) {
                    putString(_keyBio, "none")
                    commit()
                }
            }
        }

        biometricText.setOnClickListener {
            if(!biometricSwitch.isChecked){
                biometricSwitch.isChecked = true
                with(sharedPref.edit()) {
                    putString(_keyBio, "using")
                    commit()
                }
            }
            else{
                biometricSwitch.isChecked = false
                with(sharedPref.edit()) {
                    putString(_keyBio, "none")
                    commit()
                }
            }
        }



        if (cursor.moveToFirst()) {
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val exInfoImgText = cursor.getString(imageIndex).toString()
                imageName = exInfoImgText
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


// Checking prefs
        updateChooser(imageName)


        redColor.setOnClickListener {
            imageName = "ic_account_Red"
            updateChooser(imageName)
        }

        pinkColor.setOnClickListener {
            imageName = "ic_account_Pink"
            updateChooser(imageName)
        }

        purpleColor.setOnClickListener {
            imageName = "ic_account_Purple"
            updateChooser(imageName)
        }

        violetColor.setOnClickListener {
            imageName = "ic_account_Violet"
            updateChooser(imageName)
        }

        darkVioletColor.setOnClickListener {
            imageName = "ic_account_Dark_Violet"
            updateChooser(imageName)
        }

        blueColor.setOnClickListener {
            imageName = "ic_account_Blue"
            updateChooser(imageName)
        }

        cyanColor.setOnClickListener {
            imageName = "ic_account_Cyan"
            updateChooser(imageName)
        }

        tealColor.setOnClickListener {
            imageName = "ic_account_Teal"
            updateChooser(imageName)
        }
        greenColor.setOnClickListener {
            imageName = "ic_account_Green"
            updateChooser(imageName)
        }
        lightGreenColor.setOnClickListener {
            imageName = "ic_account_lightGreen"
            updateChooser(imageName)
        }

        iconTheme.setOnClickListener{
            when(imageName){
                "ic_account_Pink" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivityPink"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Red" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivityRed"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Purple" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivityPurple"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Violet" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivity"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Dark_Violet" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivitydViolet"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Blue" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivityBlue"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Cyan" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivityCyan"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Teal" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivityTeal"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_Green" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivityGreen"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
                "ic_account_lightGreen" -> {
                    turnOffAllIcons()
                    packageManager.setComponentEnabledSetting(
                            ComponentName(
                                    "com.mikhailgrigorev.quickpass",
                                    "com.mikhailgrigorev.quickpass.LoginActivitylGreen"
                            ),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    )
                }
            }
        }

    }

    private fun turnOffAllIcons(){
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivity"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivityGreen"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivityTeal"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivitylGreen"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivityCyan"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivityBlue"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivitydViolet"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivityPurple"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivityPink"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
                ComponentName(
                        "com.mikhailgrigorev.quickpass",
                        "com.mikhailgrigorev.quickpass.LoginActivityRed"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
    }


    private fun updateChooser(imageName: String){
        when (imageName) {
            "ic_account_Red" -> {
                clearCE()
                setAlpha()
                redColor.alpha = 1F
                redColor.cardElevation = 20F
            }
            "ic_account_Pink" -> {
                clearCE()
                setAlpha()
                pinkColor.alpha = 1F
                pinkColor.cardElevation = 20F
            }
            "ic_account_Purple" -> {
                clearCE()
                setAlpha()
                purpleColor.alpha = 1F
                purpleColor.cardElevation = 20F
            }
            "ic_account_Violet" -> {
                clearCE()
                setAlpha()
                violetColor.alpha = 1F
                violetColor.cardElevation = 20F
            }
            "ic_account_Dark_Violet" -> {
                clearCE()
                setAlpha()
                darkVioletColor.alpha = 1F
                darkVioletColor.cardElevation = 20F
            }
            "ic_account_Blue" -> {
                clearCE()
                setAlpha()
                blueColor.alpha = 1F
                blueColor.cardElevation = 20F
            }
            "ic_account_Cyan" -> {
                clearCE()
                setAlpha()
                cyanColor.alpha = 1F
                cyanColor.cardElevation = 20F
            }
            "ic_account_Teal" -> {
                clearCE()
                setAlpha()
                tealColor.alpha = 1F
                tealColor.cardElevation = 20F
            }
            "ic_account_Green" -> {
                clearCE()
                setAlpha()
                greenColor.alpha = 1F
                greenColor.cardElevation = 20F
            }
            "ic_account_lightGreen" -> {
                clearCE()
                setAlpha()
                lightGreenColor.alpha = 1F
                lightGreenColor.cardElevation = 20F
            }
            else ->  {
                clearCE()
                setAlpha()
                purpleColor.alpha = 1F
                purpleColor.cardElevation = 20F
            }
        }
        updateAvatar(imageName)
    }

    private fun clearCE() {
        redColor.cardElevation = 0F
        pinkColor.cardElevation = 0F
        purpleColor.cardElevation = 0F
        violetColor.cardElevation = 0F
        darkVioletColor.cardElevation = 0F
        blueColor.cardElevation = 0F
        cyanColor.cardElevation = 0F
        tealColor.cardElevation = 0F
        greenColor.cardElevation = 0F
        lightGreenColor.cardElevation = 0F
    }
    private fun setAlpha() {
        redColor.alpha = 0.7F
        pinkColor.alpha = 0.7F
        purpleColor.alpha = 0.7F
        violetColor.alpha = 0.7F
        darkVioletColor.alpha = 0.7F
        blueColor.alpha = 0.7F
        cyanColor.alpha = 0.7F
        tealColor.alpha = 0.7F
        greenColor.alpha = 0.7F
        lightGreenColor.alpha = 0.7F
    }

    @SuppressLint("Recycle")
    private fun updateAvatar(imageName: String) {

        when(imageName){
            "ic_account" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account
                )
                // Checking prefs
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Violet") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Violet")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Pink" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Pink
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Pink") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Pink")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Red" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Red
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Red") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Red")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Purple" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Purple
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Purple") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Purple")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Violet" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Violet
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Violet") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Violet")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Dark_Violet" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Dark_Violet
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "DViolet") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "DViolet")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Blue" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Blue
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Blue") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Blue")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Cyan" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Cyan
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Cyan") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Cyan")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Teal" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Teal
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Teal") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Teal")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Green" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Green
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "Green") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "Green")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_lightGreen" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_lightGreen
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                if (sharedPref.getString("themeAccentPreference", "none") != "LGreen") {
                    with(sharedPref.edit()) {
                        putString(_keyThemeAccent, "LGreen")
                        commit()
                    }
                    recreate()
                }
            }
            else -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account
                )
                val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "Violet") {
                with(sharedPref.edit()) {
                    putString(_keyThemeAccent, "Violet")
                    commit()
                }
                        recreate()
                    }
            }
        }

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase

        var contentValues = ContentValues()
        contentValues.put(dbHelper.KEY_IMAGE, imageName)
        database.update(
                dbHelper.TABLE_USERS, contentValues,
                "NAME = ?",
                arrayOf(login)
        )

        var pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase

        var names = ""

        try {
            val pCursor: Cursor = pDatabase.query(
                    pdbHelper.TABLE_USERS, arrayOf(
                    pdbHelper.KEY_NAME,
                    pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA,
                    pdbHelper.KEY_TAGS,
                    pdbHelper.KEY_GROUPS,
                    pdbHelper.KEY_USE_TIME
            ),
                    null, null,
                    null, null, null
            )

            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                do {
                    val login = pCursor.getString(nameIndex).toString()
                    names += login
                } while (pCursor.moveToNext())
            }

        } catch (e: SQLException) {
        }


        pdbHelper = PasswordsDataBaseHelper(this, login)
        val passDataBase = pdbHelper.writableDatabase
        contentValues = ContentValues()

        importDB.setOnClickListener {
            try {
                if (ContextCompat.checkSelfPermission(
                            this@SettingsActivity,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PackageManager.PERMISSION_GRANTED
                    )
                }
                val filename = "/MyBackUp.csv"
                val sdCardDir =
                        Environment.getExternalStorageDirectory()
                val saveFile = File(sdCardDir, filename)
                val file = FileReader(saveFile)
                val buffer = BufferedReader(file)
                var line: String?

                var count = 0

                var strTemp: Array<String>? = null
                while (buffer.readLine().also { line = it } != null) {
                    var str = line!!.split(",".toRegex()).toTypedArray()

                    if(strTemp != null) {
                        str = strTemp + str.drop(1)
                    }

                    if (str.size < 10) {
                        strTemp = str
                        continue
                    }
                    else{
                        strTemp = null
                    }


                    if (count == 0) {
                        count += 1
                        continue
                    }

                    if (!names.contains(str[1])) {
                        contentValues.put(pdbHelper.KEY_ID, Random.nextInt(0, 10000))

                        if (str[1] == "")
                            str[1] = "None"
                        contentValues.put(pdbHelper.KEY_NAME, str[1])

                        if (str[2] == "")
                            str[2] = "None"
                        contentValues.put(pdbHelper.KEY_PASS, str[2])

                        if (str[3] == "")
                            str[3] = "0"
                        contentValues.put(pdbHelper.KEY_2FA, str[3])

                        if (str[4] == "")
                            str[4] = "0"
                        contentValues.put(pdbHelper.KEY_USE_TIME, str[4])

                        contentValues.put(pdbHelper.KEY_TIME, getDateTime())

                        if (str[6] == "")
                            str[6] = ""
                        contentValues.put(pdbHelper.KEY_TAGS, str[6])

                        if (str[7] == "")
                            str[7] = ""
                        contentValues.put(pdbHelper.KEY_GROUPS, str[7])

                        if (str[8] == "")
                            str[8] = ""
                        contentValues.put(pdbHelper.KEY_LOGIN, str[8])

                        if (str[9] == "")
                            str[9] = ""
                        contentValues.put(pdbHelper.KEY_DESC, str[9])

                        if (str[10] == "")
                            str[10] = "none"
                        contentValues.put(pdbHelper.KEY_CIPHER, str[9])

                        passDataBase.insert(pdbHelper.TABLE_USERS, null, contentValues)
                    }


                }
                toast(getString(R.string.imported))
            }
            catch (ex: Exception) {
            }

        }

        back.setOnClickListener {
            condition=false
            val intent = Intent()
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            setResult(1, intent)
            finish()
        }

        export.setOnClickListener {
            try {
                if (ContextCompat.checkSelfPermission(
                            this@SettingsActivity,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PackageManager.PERMISSION_GRANTED
                    )
                }
                val c = passDataBase.rawQuery("select * from $login", null)
                val rowcount: Int
                val colcount: Int
                val sdCardDir =
                        Environment.getExternalStorageDirectory()
                val filename =  "/MyBackUp.csv"
                val saveFile = File(sdCardDir, filename)
                val bw = BufferedWriter(
                        OutputStreamWriter(
                                FileOutputStream(saveFile), "UTF-8"
                        )
                )

                //val bw = BufferedWriter(fw)
                rowcount = c.count
                colcount = c.columnCount
                if (rowcount > 0) {
                    c.moveToFirst()
                    for (i in 0 until colcount) {
                        if (i != colcount - 1) {
                            bw.write(c.getColumnName(i) + ",")
                        } else {
                            bw.write(c.getColumnName(i))
                        }
                    }
                    bw.newLine()
                    for (i in 0 until rowcount) {
                        c.moveToPosition(i)
                        for (j in 0 until colcount) {
                            if (j != colcount - 1) bw.write(c.getString(j) + ",") else bw.write(
                                    c.getString(j)
                            )
                        }
                        bw.newLine()
                    }
                    bw.flush()
                    toast(getString(R.string.exported))
                }
            } catch (ex: Exception) {
            }
        }
    }

    private fun Context.toast(message: String)=
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
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
}