package com.mikhailgrigorev.quickpassword.ui.settings

import android.annotation.SuppressLint
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
import android.view.KeyEvent
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.billingclient.BuildConfig
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivitySettingsBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginActivity
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
        super.onCreate(savedInstanceState)
        // Finish app after some time
        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            if (condition) {
                condition = false
                val intent = Intent(this, LoginActivity::class.java)
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

        val useBio = Utils.getBioMode()
        val usePin = Utils.getPinMode()
        val useAnalyze = Utils.useAnalyze()

        val useAuto = sharedPref.getString(_keyAutoCopy, "none")
        val lockTime = sharedPref.getString("appLockTime", "none")

        binding.appLockTime.text = "6m"
        if (lockTime != null)
            if (lockTime != "none") {
                binding.appLockBar.progress = lockTime.toInt()
                binding.appLockTime.text = lockTime.toInt().toString() + "m"
                if (lockTime.toInt() == 0) {
                    binding.appLockTime.text = getString(R.string.doNotLock)
                }
            }

        val hasBiometricFeature :Boolean = this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        if (!hasBiometricFeature){
            binding.biometricSwitch.visibility = View.GONE
            binding.biometricText.visibility = View.GONE
        }

        if (useBio) {
            binding.biometricSwitch.isChecked = true
        }

        if (useAuto == "dis") {
            binding.autoCopySwitch.isChecked = false
        }

        if (usePin) {
            binding.setPinSwitch.isChecked = true
        }

        if (useAnalyze) {
            binding.userAnalyzerSwitch.isChecked = true
        }

        binding.userAnalyzerSwitch.setOnCheckedChangeListener { _, _ ->
            Utils.setAnalyze(binding.userAnalyzerSwitch.isChecked)
        }

        binding.userAnalyzer.setOnClickListener {
            binding.userAnalyzerSwitch.isChecked = !binding.userAnalyzerSwitch.isChecked
            Utils.setAnalyze(binding.userAnalyzerSwitch.isChecked)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.autoFillSettings.visibility = View.GONE
        }

        binding.checkAutoFillSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                testAutoFill(this)
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
                startActivity(intent)
                finish()
            } else {
                condition = false
            }
            Utils.setPinMode(binding.setPinSwitch.isChecked)
        }

        binding.setPin.setOnClickListener {
            if (binding.setPinSwitch.isChecked) {
                binding.setPinSwitch.isChecked = false
                Utils.setPinMode(binding.setPinSwitch.isChecked)
            } else {
                condition = false
                Utils.setPinMode(binding.setPinSwitch.isChecked)
                val intent = Intent(this, SetPinActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.appLockBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                binding.appLockTime.text = i.toString() + "m"
                if (i == 0) {
                    binding.appLockTime.text = getString(R.string.doNotLock)
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

        binding.biometricSwitch.setOnCheckedChangeListener { _, _ ->
            Utils.setBioMode(binding.biometricSwitch.isChecked)
        }

        binding.biometricText.setOnClickListener {
            binding.biometricSwitch.isChecked = !binding.biometricSwitch.isChecked
            Utils.setBioMode(binding.biometricSwitch.isChecked)
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
            intent.data = Uri.parse("package:com.mikhailgrigorev.quickpassword")
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
