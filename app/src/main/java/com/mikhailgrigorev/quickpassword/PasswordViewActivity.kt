package com.mikhailgrigorev.quickpassword

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.*
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.databinding.ActivityPasswordViewBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.dbhelpers.PasswordsDataBaseHelper
import java.io.File


class PasswordViewActivity : AppCompatActivity() {

    private val _keyTheme = "themePreference"
    private val _keyUsername = "prefUserNameKey"
    private val _preferenceFile = "quickPassPreference"
    private val _keyAutoCopy = "prefAutoCopyKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var from: String
    private var condition = true
    private lateinit var binding: ActivityPasswordViewBinding

    @SuppressLint("Recycle", "SetTextI18n", "UseCompatLoadingForDrawables")
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
            handler.postDelayed(r, time * 6L)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = ActivityPasswordViewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val cardRadius = sharedPref.getString("cardRadius", "none")
        if(cardRadius != null)
            if(cardRadius != "none") {
                binding.warnCard.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
                binding.cardView3.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
            }


        val args: Bundle? = intent.extras
        login= args?.get("login").toString()

        from= args?.get("openedFrom").toString()

        if (from == "shortcut"){
            intent.putExtra("login", login)
            intent.putExtra("passName", args?.get("passName").toString())
            condition=false
            val intent = Intent(this, ReLoginActivity::class.java)
            startActivityForResult(intent, 1)
        }

        val newLogin = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE).getString(
                _keyUsername,
                login
        )
        if(newLogin != login)
            login = newLogin.toString()
        passName = args?.get("passName").toString()
        val newPass = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE).getString(
                "__PASSNAME",
                passName
        )
        if(newPass != passName && from != "shortcut")
            passName = newPass.toString()



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


        var dbLogin = ""
        var dbPassword: String
        var dbGroup = "null"

        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        try {
            val pCursor: Cursor = pDatabase.query(
                    pdbHelper.TABLE_USERS, arrayOf(
                    pdbHelper.KEY_NAME,
                    pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA,
                    pdbHelper.KEY_USE_TIME,
                    pdbHelper.KEY_TIME,
                    pdbHelper.KEY_DESC,
                    pdbHelper.KEY_TAGS,
                    pdbHelper.KEY_GROUPS,
                    pdbHelper.KEY_LOGIN,
                    pdbHelper.KEY_CIPHER
            ),
                    "NAME = ?", arrayOf(passName),
                    null, null, null
            )


            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
                val uTIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_USE_TIME)
                val timeIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TIME)
                val descIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_DESC)
                val tagsIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TAGS)
                val groupIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_GROUPS)
                val loginIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_LOGIN)
                val cryptIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_CIPHER)
                do {
                    dbLogin = pCursor.getString(nameIndex).toString()
                    binding.helloTextId.text = dbLogin


                    val dbCryptIndex = pCursor.getString(cryptIndex).toString()

                    binding.crypt.visibility = View.GONE

                    dbPassword = pCursor.getString(passIndex).toString()

                    val pm = PasswordManager()

                    binding.addSettings.visibility = View.GONE
                    if (dbCryptIndex == "crypted"){
                        binding.crypt.isChecked = true
                        binding.crypt.visibility = View.VISIBLE
                        binding.addSettings.visibility = View.VISIBLE
                        binding.cypheredWarn.visibility = View.VISIBLE
                        binding.cypheredWarnImg.visibility = View.VISIBLE
                        dbPassword = pm.decrypt(dbPassword).toString()
                    }


                    binding.passViewFieldView.setText(dbPassword)
                    val myPasswordManager = PasswordManager()
                    var evaluation: String = myPasswordManager.evaluatePasswordString(dbPassword)


                    val dbTimeIndex = pCursor.getString(timeIndex).toString()
                    binding.passwordTime.text = getString(R.string.time_lim) + " " + dbTimeIndex

                    dbGroup = if(pCursor.getString(groupIndex) == null)
                        "none"
                    else
                        pCursor.getString(groupIndex).toString()

                    if((myPasswordManager.evaluateDate(dbTimeIndex)) && (dbPassword.length!= 4)){
                        binding.warnCard.visibility = View.VISIBLE
                        evaluation = "low"
                    }

                    if (pm.popularPasswords(dbPassword) or ((dbPassword.length == 4) and pm.popularPin(
                                dbPassword
                        ))){
                        binding.tooEasy.visibility = View.VISIBLE
                        binding.tooEasyImg.visibility = View.VISIBLE
                        evaluation = "low"
                    }

                    when (evaluation) {
                        "low" -> binding.passQuality.text = getString(R.string.low)
                        "high" -> binding.passQuality.text = getString(R.string.high)
                        else -> binding.passQuality.text = getString(R.string.medium)
                    }
                    when (evaluation) {
                        "low" -> binding.passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.negative
                                )
                        )
                        "high" -> binding.passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.positive
                                )
                        )
                        else -> binding.passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.fixable
                                )
                        )
                    }

                    if (evaluation == "high")
                        binding.warning.visibility = View.GONE
                    else
                        binding.warning2.visibility = View.GONE



                    if((dbPassword.length == 4) and (evaluation == "high")){
                        binding.passQualityText.text = getString(R.string.showPin)
                        binding.passQuality.visibility = View.GONE
                        binding.warning.visibility = View.GONE
                        binding.warning2.visibility = View.VISIBLE
                        binding.warning2.setImageDrawable(getDrawable(R.drawable.credit_card))
                    }

                    val db2FAIndex = pCursor.getString(aIndex).toString()

                    binding.authToggle.visibility = View.GONE
                    binding.timeLimit.visibility = View.GONE
                    if (db2FAIndex == "1"){
                        binding.authToggle.isChecked = true
                        binding.authToggle.visibility = View.VISIBLE
                        binding.addSettings.visibility = View.VISIBLE
                    }
                    val dbUTIndex = pCursor.getString(uTIndex).toString()
                    if (dbUTIndex == "1"){
                        binding.timeLimit.isChecked = true
                        binding.timeLimit.visibility = View.VISIBLE
                        binding.addSettings.visibility = View.VISIBLE
                    }




                    val dbDescIndex = pCursor.getString(descIndex).toString()
                    if (dbDescIndex != "")
                        binding.noteViewField.setText(dbDescIndex)
                    else
                        binding.noteView.visibility = View.GONE


                    val dbEmailIndex = pCursor.getString(loginIndex).toString()
                    if (dbEmailIndex != "")
                        binding.emailViewField.setText(dbEmailIndex)
                    else
                        binding.emailView.visibility = View.GONE

                    val dbTagsIndex = pCursor.getString(tagsIndex).toString()
                    if(dbTagsIndex != "") {
                        dbTagsIndex.split("\\s".toRegex()).forEach { item ->
                            val chip = Chip(binding.group.context)
                            chip.text= item
                            chip.isClickable = false
                            binding.group.addView(chip)
                        }
                    }
                    else{
                        binding.kwInfo.visibility = View.GONE
                    }

                } while (pCursor.moveToNext())
            } else {
                binding.helloTextId.text = getString(R.string.no_text)
            }

        } catch (e: SQLException) {
            binding.helloTextId.text = getString(R.string.no_text)
        }

        if((args?.get("sameWith") != null) and (args?.get("sameWith").toString() != "none")){
            binding.warning0.visibility = View.VISIBLE
            binding.sameParts.visibility = View.VISIBLE
            binding.sameParts.text = args?.get("sameWith").toString()
            binding.passQuality.setTextColor(ContextCompat.getColor(applicationContext, R.color.negative))
            binding.passQuality.text = getString(R.string.low)
        }

        val autoCopy = sharedPref.getString(_keyAutoCopy, "none")

        if(autoCopy == "none" && binding.passViewFieldView.text.toString() != ""){
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", binding.passViewFieldView.text.toString())
            clipboard.setPrimaryClip(clip)
            toast(getString(R.string.passCopied))
        }

        binding.deletePassword.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.deletePassword))
            builder.setMessage(getString(R.string.passwordDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)){ _, _ ->
                pDatabase.delete(
                        pdbHelper.TABLE_USERS,
                        "NAME = ?",
                        arrayOf(dbLogin)
                )
                toast(getString(R.string.passwordDeleted))
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("login", login)
                condition=false
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton(getString(R.string.no)){ _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)){ _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.helloTextId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Name", binding.helloTextId.text.toString())
            clipboard.setPrimaryClip(clip)
            toast(getString(R.string.nameCopied))
        }

        binding.accountAvatar.setOnClickListener {
            condition=false
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("activity", "menu")
            startActivityForResult(intent, 1)
        }

        binding.emailView.setOnClickListener {
            if(binding.emailViewField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Login", binding.emailViewField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.loginCopied))
            }
        }

        binding.emailViewField.setOnClickListener {
            if(binding.emailViewField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Login", binding.emailViewField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.loginCopied))
            }
        }


        binding.passView.setOnClickListener {
            if(binding.passViewFieldView.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", binding.passViewFieldView.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        binding.passViewFieldView.setOnClickListener {
            if(binding.passViewFieldView.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", binding.passViewFieldView.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }


        binding.back.setOnClickListener {
            if(from != "short") {
                condition=false
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                setResult(1, intent)
                finish()
            }
            else{
                condition=false
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                startActivity(intent)
                finish()
            }
        }

        binding.editButton.setOnClickListener {
            condition=false
            val intent = Intent(this, EditPassActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            startActivityForResult(intent, 1)
        }

        if(dbGroup == "#favorite"){
            binding.favButton.visibility = View.GONE
            binding.favButton2.visibility = View.VISIBLE
        }


        binding.favButton.setOnClickListener {
            binding.favButton.visibility = View.GONE
            binding.favButton2.visibility = View.VISIBLE
            val contentValues = ContentValues()
            contentValues.put(pdbHelper.KEY_GROUPS, "#favorite")
            pDatabase.update(
                    pdbHelper.TABLE_USERS, contentValues,
                    "NAME = ?",
                    arrayOf(passName)
            )
        }

        binding.favButton2.setOnClickListener {
            binding.favButton.visibility = View.VISIBLE
            binding.favButton2.visibility = View.GONE

            val contentValues = ContentValues()
            contentValues.put(pdbHelper.KEY_GROUPS, "none")
            pDatabase.update(
                    pdbHelper.TABLE_USERS, contentValues,
                    "NAME = ?",
                    arrayOf(passName)
            )
        }


        val useAnalyze = sharedPref.getString("useAnalyze", "none")
        if (useAnalyze != null)
            if (useAnalyze != "none"){
                binding.passQualityText.visibility = View.GONE
                binding.warning.visibility = View.GONE
                binding.passQualityText.visibility = View.GONE
                binding.passQuality.visibility = View.GONE
                binding.warning2.visibility = View.GONE
                binding.sameParts.visibility = View.GONE
                binding.warning0.visibility = View.GONE
            }
        val mediaStorageDir = File(
                applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
        )
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
            Toast.makeText(applicationContext, "Directory Created", Toast.LENGTH_LONG).show()
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory")
            }
        }

        val layoutTransition = binding.mainLinearLayout.layoutTransition
        layoutTransition.setDuration(5000) // Change duration
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        val file = File(mediaStorageDir, "$passName.jpg")
        if (file.exists()){
            val uri = Uri.fromFile(file)
            binding.attachedImage.setImageURI(uri)
            binding.attachedImageText.visibility = View.VISIBLE
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val widthMax: Int = size.x
            val width = (widthMax/2.4).toInt()
            val height = binding.attachedImage.drawable.minimumHeight * width /  binding.attachedImage.drawable.minimumWidth
            binding.attachedImage.layoutParams.height = height
            binding.attachedImage.layoutParams.width = width

            binding.attachedImage.setOnClickListener {
                val uriForOpen = FileProvider.getUriForFile(
                        this,
                        this.applicationContext.packageName.toString() + ".provider",
                        file
                )
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(uriForOpen, "image/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            }
        }
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (from != "short") {
                    condition = false
                    val intent = Intent()
                    intent.putExtra("login", login)
                    intent.putExtra("passName", passName)
                    setResult(1, intent)
                    finish()
                } else {
                    condition = false
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("login", login)
                    intent.putExtra("passName", passName)
                    startActivity(intent)
                    finish()
                }
            }
        }
        return false
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 1) {
                condition=false
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }
    private fun Context.toast(message: String)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}