package com.mikhailgrigorev.quickpass

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.*
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
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
import com.mikhailgrigorev.quickpass.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpass.dbhelpers.PasswordsDataBaseHelper
import kotlinx.android.synthetic.main.activity_password_view.*
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
        setContentView(R.layout.activity_password_view)


        val cardRadius = sharedPref.getString("cardRadius", "none")
        if(cardRadius != null)
            if(cardRadius != "none") {
                warnCard.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
                cardView3.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        resources.displayMetrics
                )
            }


        val args: Bundle? = intent.extras
        login= args?.get("login").toString()
        from= args?.get("from").toString()

        if (from == "short"){
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
        if(newPass != passName && from != "short")
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
                    helloTextId.text = dbLogin


                    val dbCryptIndex = pCursor.getString(cryptIndex).toString()

                    crypt.visibility = View.GONE

                    dbPassword = pCursor.getString(passIndex).toString()

                    val pm = PasswordManager()

                    addSettings.visibility = View.GONE
                    if (dbCryptIndex == "crypted"){
                        crypt.isChecked = true
                        crypt.visibility = View.VISIBLE
                        addSettings.visibility = View.VISIBLE
                        cypheredWarn.visibility = View.VISIBLE
                        cypheredWarnImg.visibility = View.VISIBLE
                        dbPassword = pm.decrypt(dbPassword).toString()
                    }


                    passViewFieldView.setText(dbPassword)
                    val myPasswordManager = PasswordManager()
                    var evaluation: String = myPasswordManager.evaluatePasswordString(dbPassword)


                    val dbTimeIndex = pCursor.getString(timeIndex).toString()
                    passwordTime.text = getString(R.string.time_lim) + " " + dbTimeIndex

                    dbGroup = if(pCursor.getString(groupIndex) == null)
                        "none"
                    else
                        pCursor.getString(groupIndex).toString()

                    if((myPasswordManager.evaluateDate(dbTimeIndex)) && (dbPassword.length!= 4)){
                        warnCard.visibility = View.VISIBLE
                        evaluation = "low"
                    }

                    if (pm.popularPasswords(dbPassword) or ((dbPassword.length == 4) and pm.popularPin(
                                dbPassword
                        ))){
                        tooEasy.visibility = View.VISIBLE
                        tooEasyImg.visibility = View.VISIBLE
                        evaluation = "low"
                    }

                    when (evaluation) {
                        "low" -> passQuality.text = getString(R.string.low)
                        "high" -> passQuality.text = getString(R.string.high)
                        else -> passQuality.text = getString(R.string.medium)
                    }
                    when (evaluation) {
                        "low" -> passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.negative
                                )
                        )
                        "high" -> passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.positive
                                )
                        )
                        else -> passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.fixable
                                )
                        )
                    }

                    if (evaluation == "high")
                        warning.visibility = View.GONE
                    else
                        warning2.visibility = View.GONE



                    if((dbPassword.length == 4) and (evaluation == "high")){
                        passQualityText.text = getString(R.string.showPin)
                        passQuality.visibility = View.GONE
                        warning.visibility = View.GONE
                        warning2.visibility = View.VISIBLE
                        warning2.setImageDrawable(getDrawable(R.drawable.credit_card))
                    }

                    val db2FAIndex = pCursor.getString(aIndex).toString()

                    authToggle.visibility = View.GONE
                    timeLimit.visibility = View.GONE
                    if (db2FAIndex == "1"){
                        authToggle.isChecked = true
                        authToggle.visibility = View.VISIBLE
                        addSettings.visibility = View.VISIBLE
                    }
                    val dbUTIndex = pCursor.getString(uTIndex).toString()
                    if (dbUTIndex == "1"){
                        timeLimit.isChecked = true
                        timeLimit.visibility = View.VISIBLE
                        addSettings.visibility = View.VISIBLE
                    }




                    val dbDescIndex = pCursor.getString(descIndex).toString()
                    if (dbDescIndex != "")
                        noteViewField.setText(dbDescIndex)
                    else
                        noteView.visibility = View.GONE


                    val dbEmailIndex = pCursor.getString(loginIndex).toString()
                    if (dbEmailIndex != "")
                        emailViewField.setText(dbEmailIndex)
                    else
                        emailView.visibility = View.GONE

                    val dbTagsIndex = pCursor.getString(tagsIndex).toString()
                    if(dbTagsIndex != "") {
                        dbTagsIndex.split("\\s".toRegex()).forEach { item ->
                            val chip = Chip(group.context)
                            chip.text= item
                            chip.isClickable = false
                            group.addView(chip)
                        }
                    }
                    else{
                        kwInfo.visibility = View.GONE
                    }

                } while (pCursor.moveToNext())
            } else {
                helloTextId.text = getString(R.string.no_text)
            }

        } catch (e: SQLException) {
            helloTextId.text = getString(R.string.no_text)
        }

        if((args?.get("sameWith") != null) and (args?.get("sameWith").toString() != "none")){
            warning0.visibility = View.VISIBLE
            sameParts.visibility = View.VISIBLE
            sameParts.text = args?.get("sameWith").toString()
            passQuality.setTextColor(ContextCompat.getColor(applicationContext, R.color.negative))
            passQuality.text = getString(R.string.low)
        }

        val autoCopy = sharedPref.getString(_keyAutoCopy, "none")

        if(autoCopy == "none" && passViewFieldView.text.toString() != ""){
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", passViewFieldView.text.toString())
            clipboard.setPrimaryClip(clip)
            toast(getString(R.string.passCopied))
        }

        deletePassword.setOnClickListener {
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
                val intent = Intent(this, PassGenActivity::class.java)
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

        accountAvatar.setOnClickListener {
            condition=false
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("activity", "menu")
            startActivityForResult(intent, 1)
        }


        passView.setOnClickListener {
            if(passViewFieldView.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", passViewFieldView.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        passViewFieldView.setOnClickListener {
            if(passViewFieldView.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", passViewFieldView.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }


        back.setOnClickListener {
            //logo.visibility = View.VISIBLE
            //val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_splash)
            //rotation.fillAfter = true
            //logo.startAnimation(rotation)
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
                val intent = Intent(this, PassGenActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                startActivity(intent)
                finish()
            }
        }

        editButton.setOnClickListener {
            condition=false
            val intent = Intent(this, EditPassActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            startActivityForResult(intent, 1)
        }

        if(dbGroup == "#favorite"){
            favButton.visibility = View.GONE
            favButton2.visibility = View.VISIBLE
        }


        favButton.setOnClickListener {
            favButton.visibility = View.GONE
            favButton2.visibility = View.VISIBLE
            val contentValues = ContentValues()
            contentValues.put(pdbHelper.KEY_GROUPS, "#favorite")
            pDatabase.update(
                    pdbHelper.TABLE_USERS, contentValues,
                    "NAME = ?",
                    arrayOf(passName)
            )
        }

        favButton2.setOnClickListener {
            favButton.visibility = View.VISIBLE
            favButton2.visibility = View.GONE

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
                passQualityText.visibility = View.GONE
                warning.visibility = View.GONE
                passQualityText.visibility = View.GONE
                passQuality.visibility = View.GONE
                warning2.visibility = View.GONE
                sameParts.visibility = View.GONE
                warning0.visibility = View.GONE
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

        val layoutTransition = mainLinearLayout.layoutTransition
        layoutTransition.setDuration(5000) // Change duration
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)


        val file = File(mediaStorageDir, "$passName.jpg")
        if (file.exists()){
            val uri = Uri.fromFile(file)
            attachedImage.setImageURI(uri)
            attachedImageText.visibility = View.VISIBLE
            val width = attachedImage.drawable.minimumWidth/3
            val height = attachedImage.drawable.minimumHeight/3
            attachedImage.layoutParams.height = height
            attachedImage.layoutParams.width = width

            attachedImage.setOnClickListener {
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
                //logo.visibility = View.VISIBLE
                //val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_splash)
                //rotation.fillAfter = true
                //logo.startAnimation(rotation)
                if (from != "short") {
                    condition = false
                    val intent = Intent()
                    intent.putExtra("login", login)
                    intent.putExtra("passName", passName)
                    setResult(1, intent)
                    finish()
                } else {
                    condition = false
                    val intent = Intent(this, PassGenActivity::class.java)
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