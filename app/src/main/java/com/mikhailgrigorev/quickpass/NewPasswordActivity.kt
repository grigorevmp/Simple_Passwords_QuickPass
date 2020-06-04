package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.*
import android.content.res.ColorStateList
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_edit_pass.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_new_password.*
import kotlinx.android.synthetic.main.activity_new_password.authToogle
import kotlinx.android.synthetic.main.activity_new_password.cardPass
import kotlinx.android.synthetic.main.activity_new_password.genPasswordId
import kotlinx.android.synthetic.main.activity_new_password.genPasswordIdField
import kotlinx.android.synthetic.main.activity_new_password.generatePassword
import kotlinx.android.synthetic.main.activity_new_password.lengthToggle
import kotlinx.android.synthetic.main.activity_new_password.lettersToggle
import kotlinx.android.synthetic.main.activity_new_password.newName
import kotlinx.android.synthetic.main.activity_new_password.newNameField
import kotlinx.android.synthetic.main.activity_new_password.noteField
import kotlinx.android.synthetic.main.activity_new_password.numbersToggle
import kotlinx.android.synthetic.main.activity_new_password.passQuality
import kotlinx.android.synthetic.main.activity_new_password.passSettings
import kotlinx.android.synthetic.main.activity_new_password.savePass
import kotlinx.android.synthetic.main.activity_new_password.seekBar
import kotlinx.android.synthetic.main.activity_new_password.symToggles
import kotlinx.android.synthetic.main.activity_new_password.timeLimit
import kotlinx.android.synthetic.main.activity_new_password.upperCaseToggle
import kotlinx.android.synthetic.main.activity_new_password.userAvatar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class NewPasswordActivity : AppCompatActivity() {

    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private var length = 20
    private var useSyms = false
    private var useUC = false
    private var useLetters = false
    private var useNums = false

    @SuppressLint("Recycle", "ClickableViewAccessibility", "ResourceAsColor", "RestrictedApi",
        "SetTextI18n"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_new_password)

        val args: Bundle? = intent.extras
        val login: String? = args?.get("login").toString()

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
                val ex_infoImgText = cursor.getString(imageIndex).toString()
                val id = resources.getIdentifier(
                    ex_infoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }

        val list = mutableListOf<String>()
        val pass: String? = args?.get("pass").toString()
        genPasswordIdField.setText(pass)
        if(pass!="") {
            val myPasswordManager = PasswordManager()
            val evaluation: Float =
                myPasswordManager.evaluatePassword(genPasswordIdField.text.toString())
            passQuality.text = evaluation.toString()
        }
        useLetters = args?.get("useLetters") as Boolean
        if(useLetters){
            lettersToggle.isChecked = true
            list.add(lettersToggle.text.toString())
        }
        useUC = args.get("useUC") as Boolean
        if(useUC){
            upperCaseToggle.isChecked = true
            list.add(upperCaseToggle.text.toString())
        }
        useNums = args.get("useNums") as Boolean
        if(useNums){
            numbersToggle.isChecked = true
            list.add(numbersToggle.text.toString())
        }
        useSyms = args.get("useSyms") as Boolean
        if(useSyms){
            symToggles.isChecked = true
            list.add(symToggles.text.toString())
        }
        length = args.get("length") as Int
        lengthToggle.text = getString(R.string.length)  + ": " +  length

        getInfo.setOnClickListener {
            if(info_card.visibility ==  View.GONE){
                info_card.visibility =  View.VISIBLE
            }
            else{
                info_card.visibility =  View.GONE
            }
        }

        lengthToggle.setOnClickListener {
            if(seekBar.visibility ==  View.GONE){
                seekBar.visibility =  View.VISIBLE
            }
            else{
                seekBar.visibility =  View.GONE
            }
            /*
            val txt = EditText(this)
            txt.hint = "$length"
            AlertDialog.Builder(this)
                .setTitle("Length of the password")
                .setMessage("Input length of your password")
                .setView(txt,  20, 0, 20, 0)
                .setPositiveButton(
                    "Set"
                ) { _, _ ->
                    length = txt.text.toString().toInt()
                    lengthToggle.text = getString(R.string.length)  + ": " +  length
                }
                .setNegativeButton(
                    "Cancel"
                ) { _, _ -> }
                .show()*/
        }

        // Set a SeekBar change listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                length = i
                lengthToggle.text = getString(R.string.length)  + ": " + length
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        // Loop through the chips
        for (index in 0 until passSettings.childCount) {
            val chip: Chip = passSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener{view, isChecked ->
                if (isChecked){
                    if (view.id == R.id.lettersToggle)
                        useLetters = true
                    if (view.id == R.id.symToggles)
                        useSyms = true
                    if (view.id == R.id.numbersToggle)
                        useNums = true
                    if (view.id == R.id.upperCaseToggle)
                        useUC = true
                    list.add(view.text.toString())
                }else{
                    if (view.id == R.id.lettersToggle)
                        useLetters = false
                    if (view.id == R.id.symToggles)
                        useSyms = false
                    if (view.id == R.id.numbersToggle)
                        useNums = false
                    if (view.id == R.id.upperCaseToggle)
                        useUC = false
                    list.remove(view.text.toString())
                }
                //if (list.isNotEmpty()){
                //    // SHow the selection
                //    toast("Selected $list")
                //}
            }
        }

        genPasswordIdField.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val myPasswordManager = PasswordManager()
                val evaluation: Float = myPasswordManager.evaluatePassword(genPasswordIdField.text.toString())
                lettersToggle.isChecked = myPasswordManager.isLetters(genPasswordIdField.text.toString())
                upperCaseToggle.isChecked = myPasswordManager.isUpperCase(genPasswordIdField.text.toString())
                numbersToggle.isChecked = myPasswordManager.isNumbers(genPasswordIdField.text.toString())
                symToggles.isChecked = myPasswordManager.isSymbols(genPasswordIdField.text.toString())
                passQuality.text = evaluation.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        generatePassword.setOnClickListener {
            val myPasswordManager = PasswordManager()
            //Create a password with letters, uppercase letters, numbers but not special chars with 17 chars
            if(list.size == 0){
                genPasswordId.error = getString(R.string.noRules)
            }
            else {
                genPasswordId.error = null
                val newPassword: String =
                    myPasswordManager.generatePassword(useLetters, useUC, useNums, useSyms, length)
                genPasswordIdField.setText(newPassword)

                val evaluation: Float = myPasswordManager.evaluatePassword(genPasswordIdField.text.toString())
                passQuality.text = evaluation.toString()
            }
        }
        generatePassword.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    cardPass.elevation = 50F
                    generatePassword.background = ContextCompat.getDrawable(this, R.color.grey)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    generatePassword.background = ContextCompat.getDrawable(this, R.color.white)
                    cardPass.elevation = 10F
                    v.invalidate()
                }
            }
            false
        }

        genPasswordId.setOnClickListener {
            if(genPasswordIdField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", genPasswordIdField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        genPasswordIdField.setOnClickListener {
            if(genPasswordIdField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", genPasswordIdField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        savePass.setOnClickListener {
            val pdbHelper = PasswordsDataBaseHelper(this, login.toString())
            val passDataBase = pdbHelper.writableDatabase
            val contentValues = ContentValues()

            val newCursor: Cursor = passDataBase.query(
                pdbHelper.TABLE_USERS, arrayOf(pdbHelper.KEY_NAME),
                "NAME = ?", arrayOf(login),
                null, null, null
            )

            if (newCursor.moveToFirst()) {
                newName.error = getString(R.string.exists)
            }
            else if (login != null) {
                if (login.isEmpty() || login.length < 3) {
                    inputLoginId.error = getString(R.string.errNumOfText)
                } else {
                    contentValues.put(pdbHelper.KEY_ID, Random.nextInt(0, 100))
                    contentValues.put(pdbHelper.KEY_NAME, newNameField.text.toString())
                    contentValues.put(pdbHelper.KEY_PASS, genPasswordIdField.text.toString())
                    var keyFA = "0"
                    if(authToogle.isChecked)
                        keyFA = "1"
                    var keytimeLimit = "0"
                    if(timeLimit.isChecked)
                        keytimeLimit = "1"
                    contentValues.put(pdbHelper.KEY_2FA, keyFA)
                    contentValues.put(pdbHelper.KEY_USE_TIME, keytimeLimit)
                    contentValues.put(pdbHelper.KEY_TIME, getDateTime())
                    contentValues.put(pdbHelper.KEY_DESC, noteField.text.toString())
                    passDataBase.insert(pdbHelper.TABLE_USERS, null, contentValues)
                }
            }

            val intent = Intent(this, PassGenActivity::class.java)
            intent.putExtra("login", login)
            startActivity(intent)
            finish()
        }
    }

    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }


    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
}