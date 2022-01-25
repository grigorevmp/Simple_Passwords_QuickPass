package com.mikhailgrigorev.quickpassword.ui.settings

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.billingclient.BuildConfig
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.databinding.ActivitySettingsBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import com.mikhailgrigorev.quickpassword.ui.pin_code.set.SetPinActivity
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
    private var condition = true
    private lateinit var binding: ActivitySettingsBinding

    @SuppressLint("SetTextI18n", "Recycle", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        when (pref.getString(_keyTheme, "none")) {
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        when (pref.getString("themeAccentPreference", "none")) {
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
        // Finish app after some time
        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            if (condition) {
                condition = false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val time: Long = 100000
        val sharedPref2 = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val lockTime2 = sharedPref2.getString("appLockTime", "6")
        if (lockTime2 != null) {
            if (lockTime2 != "0")
                handler.postDelayed(r, time * lockTime2.toLong())
        } else
            handler.postDelayed(r, time * 6L)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false)
                }
                else{
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
        }
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.defaultSystem.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.light.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.dark.isChecked = true
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> binding.autoBattery.isChecked = true
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> binding.defaultSystem.isChecked = true
            else -> binding.defaultSystem.isChecked = true
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

        binding.light.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "no")
                commit()
            }
            binding.light.isChecked = true
            recreate()
        }
        binding.dark.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "yes")
                commit()
            }
            binding.dark.isChecked = true
            recreate()
        }
        binding.autoBattery.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "battery")
                commit()
            }
            binding.autoBattery.isChecked = true
            recreate()
        }
        binding.defaultSystem.setOnClickListener {
            with(sharedPref.edit()) {
                putString(_keyTheme, "default")
                commit()
            }
            binding.defaultSystem.isChecked = true
            recreate()
        }

        val useBio = sharedPref.getString(_keyBio, "none")
        val useAuto = sharedPref.getString(_keyAutoCopy, "none")
        val usePin = sharedPref.getString(_keyUsePin, "none")

        val useAnalyze = sharedPref.getString("useAnalyze", "none")

        val lockTime = sharedPref.getString("appLockTime", "none")

        val cardRadius = sharedPref.getString("cardRadius", "none")
        if (cardRadius != null)
            if (cardRadius != "none") {
                binding.infoCard.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
                binding.themeSettings.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
                binding.cvWarningRulesCard.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
                binding.exportCrad.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
            }


        binding.appLockTime.text = "6m"
        if (lockTime != null)
            if (lockTime != "none") {
                binding.appLockBar.progress = lockTime.toInt()
                binding.appLockTime.text = lockTime.toInt().toString() + "m"
                if (lockTime.toInt() == 0) {
                    binding.appLockTime.text = getString(R.string.dontlock)
                }
            }

        binding.cardRadiusVal.text = "10"
        if (cardRadius != null)
            if (cardRadius != "none") {
                binding.cardRadiusBar.progress = cardRadius.toInt()
                binding.cardRadiusVal.text = cardRadius.toInt().toString()
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

        if (dbMail != "none") {
            binding.userMailSwitch.isChecked = true
            mailSet = true
        }

        val hasBiometricFeature :Boolean = this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        if (!hasBiometricFeature){
            binding.biometricSwitch.visibility = View.GONE
            binding.biometricText.visibility = View.GONE
        }

        if (useBio == "using") {
            binding.biometricSwitch.isChecked = true
        }

        if (useAuto == "dis") {
            binding.autoCopySwitch.isChecked = false
        }

        if (usePin != "none") {
            binding.setPinSwitch.isChecked = true
        }

        if (useAnalyze != "none") {
            binding.userAnalyzerSwitch.isChecked = true
        }

        binding.userAnalyzerSwitch.setOnCheckedChangeListener { _, _ ->
            if (!binding.userAnalyzerSwitch.isChecked) {
                with(sharedPref.edit()) {
                    putString("useAnalyze", "none")
                    commit()
                }
            } else {
                with(sharedPref.edit()) {
                    putString("useAnalyze", "yes")
                    commit()
                }
            }
        }

        binding.userAnalyzer.setOnClickListener {
            if (binding.userAnalyzerSwitch.isChecked) {
                binding.userAnalyzerSwitch.isChecked = false
                with(sharedPref.edit()) {
                    putString("useAnalyze", "none")
                    commit()
                }
            } else {
                binding.userAnalyzerSwitch.isChecked = true
                with(sharedPref.edit()) {
                    putString("useAnalyze", "yes")
                    commit()
                }
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.autoFillSettings.visibility = View.GONE

        }

        binding.checkAutoFillSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                testAutoFill(this)
            }
        }

        binding.userMail.setOnClickListener {
            if (binding.userMailSwitch.isChecked) {
                binding.userMailSwitch.isChecked = false
                mailSet = false
                val newMail = "none"
                val contentValues = ContentValues()
                contentValues.put(dbHelper.KEY_MAIL, newMail)
                database.update(
                        dbHelper.TABLE_USERS, contentValues,
                        "NAME = ?",
                        arrayOf(login)
                )
            } else {
                var newMail: String
                val inputEditTextField = EditText(this)
                inputEditTextField.setSingleLine()
                val dialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setTitle(getString(R.string.newMail))
                        .setMessage(getString(R.string.mail_description))
                        .setPositiveButton(getString(R.string.saveButton)) { _, _ ->
                            newMail = inputEditTextField.text.toString()
                            val contentValues = ContentValues()
                            contentValues.put(dbHelper.KEY_MAIL, newMail)
                            database.update(
                                    dbHelper.TABLE_USERS, contentValues,
                                    "NAME = ?",
                                    arrayOf(login)
                            )
                            mailSet = true
                            binding.userMailSwitch.isChecked = true
                        }
                        .setNegativeButton(getString(R.string.closeButton)) { _, _ ->
                            binding.userMailSwitch.isChecked = false
                        }
                        .setCancelable(false)
                        .create()
                dialog.setView(inputEditTextField, 50, 50, 50, 50)
                dialog.show()
            }
        }


        binding.userMailSwitch.setOnCheckedChangeListener { _, _ ->
            if (!binding.userMailSwitch.isChecked and mailSet) {
                mailSet = false
                val newMail = "none"
                val contentValues = ContentValues()
                contentValues.put(dbHelper.KEY_MAIL, newMail)
                database.update(
                        dbHelper.TABLE_USERS, contentValues,
                        "NAME = ?",
                        arrayOf(login)
                )
            } else if (!mailSet) {
                binding.userMailSwitch.isChecked = false
                var newMail: String
                val inputEditTextField = EditText(this)
                inputEditTextField.setSingleLine()
                val dialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setTitle(getString(R.string.newMail))
                        .setMessage(getString(R.string.mail_description))
                        //.setView(inputEditTextField, 100, 100, 100, 100)
                        .setPositiveButton(getString(R.string.saveButton)) { _, _ ->
                            newMail = inputEditTextField.text.toString()
                            val contentValues = ContentValues()
                            contentValues.put(dbHelper.KEY_MAIL, newMail)
                            database.update(
                                    dbHelper.TABLE_USERS, contentValues,
                                    "NAME = ?",
                                    arrayOf(login)
                            )
                            mailSet = true
                            binding.userMailSwitch.isChecked = true
                        }
                        .setNegativeButton(getString(R.string.closeButton)) { _, _ ->
                            binding.userMailSwitch.isChecked = false
                        }
                        .create()
                dialog.setView(inputEditTextField, 50, 50, 50, 50)
                dialog.show()

            }
        }

        binding.autoCopySwitch.setOnCheckedChangeListener { _, _ ->
            if (!binding.autoCopySwitch.isChecked) {
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "dis")
                    commit()
                }
            } else {
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "none")
                    commit()
                }
            }
        }


        binding.autoCopy.setOnClickListener {
            if (binding.autoCopySwitch.isChecked) {
                binding.autoCopySwitch.isChecked = false
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "dis")
                    commit()
                }
            } else {
                binding.autoCopySwitch.isChecked = true
                with(sharedPref.edit()) {
                    putString(_keyAutoCopy, "none")
                    commit()
                }
            }
        }

        binding.setPinSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.setPinSwitch.isChecked) {
                condition = false
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                startActivity(intent)
                finish()
            } else {
                condition = false
                with(sharedPref.edit()) {
                    putString(_keyUsePin, "none")
                    commit()
                }
            }
        }

        binding.setPin.setOnClickListener {
            if (binding.setPinSwitch.isChecked) {
                binding.setPinSwitch.isChecked = false
                with(sharedPref.edit()) {
                    putString(_keyUsePin, "none")
                    commit()
                }
            } else {
                condition = false
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                startActivity(intent)
                finish()
            }
        }

        binding.appLockBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                binding.appLockTime.text = i.toString() + "m"
                if (i == 0) {
                    binding.appLockTime.text = getString(R.string.dontlock)
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

        binding.cardRadiusBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                binding.cardRadiusVal.text = i.toString()
                binding.infoCard.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        i.toFloat(),
                        resources.displayMetrics
                )
                binding.themeSettings.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        i.toFloat(),
                        resources.displayMetrics
                )
                binding.cvWarningRulesCard.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        i.toFloat(),
                        resources.displayMetrics
                )
                binding.exportCrad.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        i.toFloat(),
                        resources.displayMetrics
                )
                with(sharedPref.edit()) {
                    putString("cardRadius", i.toString())
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


        binding.biometricSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.biometricSwitch.isChecked) {
                with(sharedPref.edit()) {
                    putString(_keyBio, "using")
                    commit()
                }
            } else {
                with(sharedPref.edit()) {
                    putString(_keyBio, "none")
                    commit()
                }
            }
        }

        binding.biometricText.setOnClickListener {
            if (!binding.biometricSwitch.isChecked) {
                binding.biometricSwitch.isChecked = true
                with(sharedPref.edit()) {
                    putString(_keyBio, "using")
                    commit()
                }
            } else {
                binding.biometricSwitch.isChecked = false
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
                when (cursor.getString(imageIndex).toString()) {
                    "ic_account" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account
                            )
                    "ic_account_Pink" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Pink
                            )
                    "ic_account_Red" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Red
                            )
                    "ic_account_Purple" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Purple
                            )
                    "ic_account_Violet" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Violet
                            )
                    "ic_account_Dark_Violet" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Dark_Violet
                            )
                    "ic_account_Blue" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Blue
                            )
                    "ic_account_Cyan" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Cyan
                            )
                    "ic_account_Teal" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Teal
                            )
                    "ic_account_Green" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Green
                            )
                    "ic_account_lightGreen" -> binding.cvAccountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
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
        updateChooser(imageName)


        binding.redColor.setOnClickListener {
            imageName = "ic_account_Red"
            updateChooser(imageName)
        }

        binding.pinkColor.setOnClickListener {
            imageName = "ic_account_Pink"
            updateChooser(imageName)
        }

        binding.purpleColor.setOnClickListener {
            imageName = "ic_account_Purple"
            updateChooser(imageName)
        }

        binding.violetColor.setOnClickListener {
            imageName = "ic_account_Violet"
            updateChooser(imageName)
        }

        binding.darkVioletColor.setOnClickListener {
            imageName = "ic_account_Dark_Violet"
            updateChooser(imageName)
        }

        binding.blueColor.setOnClickListener {
            imageName = "ic_account_Blue"
            updateChooser(imageName)
        }

        binding.cyanColor.setOnClickListener {
            imageName = "ic_account_Cyan"
            updateChooser(imageName)
        }

        binding.tealColor.setOnClickListener {
            imageName = "ic_account_Teal"
            updateChooser(imageName)
        }
        binding.greenColor.setOnClickListener {
            imageName = "ic_account_Green"
            updateChooser(imageName)
        }
        binding.lightGreenColor.setOnClickListener {
            imageName = "ic_account_lightGreen"
            updateChooser(imageName)
        }

        binding.iconTheme.setOnClickListener {
            when (imageName) {
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

    private fun turnOffAllIcons() {
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


    private fun updateChooser(imageName: String) {
        when (imageName) {
            "ic_account_Red" -> {
                clearCE()
                setAlpha()
                binding.redColor.alpha = 1F
                binding.redColor.cardElevation = 20F
            }
            "ic_account_Pink" -> {
                clearCE()
                setAlpha()
                binding.pinkColor.alpha = 1F
                binding.pinkColor.cardElevation = 20F
            }
            "ic_account_Purple" -> {
                clearCE()
                setAlpha()
                binding.purpleColor.alpha = 1F
                binding.purpleColor.cardElevation = 20F
            }
            "ic_account_Violet" -> {
                clearCE()
                setAlpha()
                binding.violetColor.alpha = 1F
                binding.violetColor.cardElevation = 20F
            }
            "ic_account_Dark_Violet" -> {
                clearCE()
                setAlpha()
                binding.darkVioletColor.alpha = 1F
                binding.darkVioletColor.cardElevation = 20F
            }
            "ic_account_Blue" -> {
                clearCE()
                setAlpha()
                binding.blueColor.alpha = 1F
                binding.blueColor.cardElevation = 20F
            }
            "ic_account_Cyan" -> {
                clearCE()
                setAlpha()
                binding.cyanColor.alpha = 1F
                binding.cyanColor.cardElevation = 20F
            }
            "ic_account_Teal" -> {
                clearCE()
                setAlpha()
                binding.tealColor.alpha = 1F
                binding.tealColor.cardElevation = 20F
            }
            "ic_account_Green" -> {
                clearCE()
                setAlpha()
                binding.greenColor.alpha = 1F
                binding.greenColor.cardElevation = 20F
            }
            "ic_account_lightGreen" -> {
                clearCE()
                setAlpha()
                binding.lightGreenColor.alpha = 1F
                binding.lightGreenColor.cardElevation = 20F
            }
            else -> {
                clearCE()
                setAlpha()
                binding.purpleColor.alpha = 1F
                binding.purpleColor.cardElevation = 20F
            }
        }
        updateAvatar(imageName)
    }

    private fun clearCE() {
        binding.redColor.cardElevation = 0F
        binding.pinkColor.cardElevation = 0F
        binding.purpleColor.cardElevation = 0F
        binding.violetColor.cardElevation = 0F
        binding.darkVioletColor.cardElevation = 0F
        binding.blueColor.cardElevation = 0F
        binding.cyanColor.cardElevation = 0F
        binding.tealColor.cardElevation = 0F
        binding.greenColor.cardElevation = 0F
        binding.lightGreenColor.cardElevation = 0F
    }

    private fun setAlpha() {
        binding.redColor.alpha = 0.7F
        binding.pinkColor.alpha = 0.7F
        binding.purpleColor.alpha = 0.7F
        binding.violetColor.alpha = 0.7F
        binding.darkVioletColor.alpha = 0.7F
        binding.blueColor.alpha = 0.7F
        binding.cyanColor.alpha = 0.7F
        binding.tealColor.alpha = 0.7F
        binding.greenColor.alpha = 0.7F
        binding.lightGreenColor.alpha = 0.7F
    }

    @SuppressLint("Recycle")
    private fun updateAvatar(imageName: String) {

        when (imageName) {
            "ic_account" -> {
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
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
                binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account
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
        }

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(dbHelper.KEY_IMAGE, imageName)
        database.update(
                dbHelper.TABLE_USERS, contentValues,
                "NAME = ?",
                arrayOf(login)
        )

        val pdbHelper = PasswordsDataBaseHelper(this, login)
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

        binding.importDB.setOnClickListener {
            try {
                if (ContextCompat.checkSelfPermission(
                            this@SettingsActivity,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PackageManager.PERMISSION_GRANTED
                    )
                }
                val intent = Intent()
                        .setAction(Intent.ACTION_GET_CONTENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("text/*")

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
            } catch (ex: Exception) {
            }
        }

        binding.back.setOnClickListener {
            condition = false
            val intent = Intent()
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            setResult(1, intent)
            finish()
        }

        binding.export.setOnClickListener {
            try {
                if (ContextCompat.checkSelfPermission(
                            this@SettingsActivity,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PackageManager.PERMISSION_GRANTED
                    )
                }
                val intent = Intent()
                        .setAction(Intent.ACTION_CREATE_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("text/csv")

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 222)
            } catch (ex: Exception) {
            }


        }
    }

    private fun Context.toast(message: String) =
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
                condition = false
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                setResult(1, intent)
                finish()
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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
            pCursor.close()
        } catch (e: SQLException) {
        }


        pdbHelper = PasswordsDataBaseHelper(this, login)
        val passDataBase = pdbHelper.writableDatabase
        contentValues = ContentValues()
        if (requestCode == 111 && resultCode == RESULT_OK) {
            try {
                data?.data?.let {
                    contentResolver.openInputStream(it)
                }?.let {
                    val buffer = BufferedReader(InputStreamReader(it))
                    var line: String?

                    var count = 0

                    var strTemp: Array<String>? = null
                    while (buffer.readLine().also { it2 -> line = it2 } != null) {
                        var str = line!!.split(",".toRegex()).toTypedArray()

                        if (strTemp != null) {
                            str = strTemp + str.drop(1)
                        }

                        if (str.size < 10) {
                            strTemp = str
                            continue
                        } else {
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


                    val mediaStorageDir = File(
                            applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
                    )
                    if (!mediaStorageDir.exists()) {
                        mediaStorageDir.mkdirs()
                        Toast.makeText(
                                applicationContext,
                                "Directory Created",
                                Toast.LENGTH_LONG
                        ).show()
                    }

                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("App", "failed to create directory")
                        }
                    }




                    val to = getAbsoluteDir(this)
                    copyFolder(to, mediaStorageDir)

                }
            } catch (e: Exception) { // If the app failed to attempt to retrieve the error file, throw an error alert
                Toast.makeText(
                        this,
                        "Sorry, but there was an error reading in the file",
                        Toast.LENGTH_SHORT
                ).show()
            }
            toast(getString(R.string.imported))
        }
        if (requestCode == 222 && resultCode == RESULT_OK) {
            try {

                data?.data?.let {
                    contentResolver.openOutputStream(it)
                }?.let {
                    val c = passDataBase.rawQuery("select * from $login", null)

                    val bw = BufferedWriter(OutputStreamWriter(it))

                    //val bw = BufferedWriter(fw)
                    val rowcount: Int = c.count
                    val colcount: Int = c.columnCount
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
                        c.close()

                        val mediaStorageDir = File(
                                applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
                        )
                        if (!mediaStorageDir.exists()) {
                            mediaStorageDir.mkdirs()
                            Toast.makeText(
                                    applicationContext,
                                    "Directory Created",
                                    Toast.LENGTH_LONG
                            ).show()
                        }

                        if (!mediaStorageDir.exists()) {
                            if (!mediaStorageDir.mkdirs()) {
                                Log.d("App", "failed to create directory")
                            }
                        }




                        val to = getAbsoluteDir(this)
                        copyFolder(mediaStorageDir, to)

                    }
                }
            } catch (e: Exception) { // If the app failed to attempt to retrieve the error file, throw an error alert
                Toast.makeText(
                        this,
                        "Sorry, but there was an error writing in the file",
                        Toast.LENGTH_SHORT
                ).show()
            }
            toast(getString(R.string.exported))
        }


    }


    private fun copyFolder(source: File, destination: File) {
        if (source.isDirectory) {
            if (!destination.exists()) {
                destination.mkdirs()
            }
            val files = source.list()
            if (files != null)
                for (file in files) {
                    val srcFile = File(source, file)
                    val destFile = File(destination, file)
                    copyFolder(srcFile, destFile)
                }
        } else {
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                `in` = FileInputStream(source)
                out = FileOutputStream(destination)
                val buffer = ByteArray(1024)
                var length: Int
                while (`in`.read(buffer).also { length = it } > 0) {
                    out.write(buffer, 0, length)
                }
            } catch (e: java.lang.Exception) {
                try {
                    `in`!!.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
                try {
                    out!!.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testAutoFill(context: Context){
        val autoFillManager: AutofillManager = context.getSystemService(AutofillManager::class.java)
        if (!autoFillManager.hasEnabledAutofillServices()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:com.mikhailgrigorev.quickpass")
            startActivityForResult(intent, 0)
        }
        else{
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:none")
            startActivityForResult(intent, 0)
        }

    }

    private fun getAbsoluteDir(ctx: Context): File {
        val optionalPath = "QuickPass"
        var rootPath: String = if (optionalPath != "") {
            ctx.getExternalFilesDir(optionalPath)!!.absolutePath
        } else {
            ctx.getExternalFilesDir(null)!!.absolutePath
        }
        // extraPortion is extra part of file path
        val extraPortion = ("Android/data/" + BuildConfig.APPLICATION_ID
                + File.separator + "files" + File.separator)
        // Remove extraPortion
        rootPath = rootPath.replace(extraPortion, "")
        return File(rootPath)
    }

}
