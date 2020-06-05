package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_pass_gen.*


class PassGenActivity : AppCompatActivity() {

    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private var length = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0
    private val passwords: ArrayList<Pair<String, String>> = ArrayList()
    private val quality: ArrayList<String> = ArrayList()
    private lateinit var login: String

    @SuppressLint("Recycle", "ClickableViewAccessibility", "ResourceAsColor", "RestrictedApi",
        "SetTextI18n"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_pass_gen)


        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name

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
                val exInfoImgText = cursor.getString(imageIndex).toString()
                val id = resources.getIdentifier(
                    exInfoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }


        var dbLogin: String

        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        try {
            val pCursor: Cursor = pDatabase.query(
                pdbHelper.TABLE_USERS, arrayOf(pdbHelper.KEY_NAME, pdbHelper.KEY_PASS, pdbHelper.KEY_2FA),
                null, null,
                null, null, null
            )


            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val myPasswordManager = PasswordManager()
                    val evaluation: Float =
                        myPasswordManager.evaluatePassword(pass)
                    val qualityNum = when {
                        evaluation < 0.33 -> "2"
                        evaluation < 0.66 -> "3"
                        else -> "1"
                    }
                    dbLogin = pCursor.getString(nameIndex).toString()
                    val fa = pCursor.getString(aIndex).toString()
                    passwords.add(Pair(dbLogin, fa))
                    quality.add(qualityNum)
                    when (qualityNum) {
                        "1" -> safePass += 1
                        "2" -> unsafePass += 1
                        "3" -> fixPass += 1
                    }
                } while (pCursor.moveToNext())
            }
        } catch (e: SQLException) {
        }

        if(passwords.size == 0){
            allPassword.visibility = View.GONE
            noPasswords.visibility = View.VISIBLE
        }

        correctPasswords.text = safePass.toString() + " " + getString(R.string.correct_passwords)
        negativePasswords.text = unsafePass.toString() + " " + getString(R.string.incorrect_password)
        fixPasswords.text = getString(R.string.need_fix) + " " + fixPass.toString() + " " + getString(R.string.passwords)

        passwordRecycler.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.VERTICAL, false)

        passwordRecycler.setHasFixedSize(true)

        passwordRecycler.adapter = PasswordAdapter(passwords, quality, this, clickListener = {
            passClickListener(it)
        })

        search.setOnClickListener {
            if(searchPass.visibility ==  View.GONE){
                searchPass.visibility =  View.VISIBLE
                passwordRecycler.adapter = null
                imageView.visibility =  View.VISIBLE
                imageView.visibility=  View.VISIBLE
                newPass.visibility =  View.GONE
            }
            else{
                searchPass.visibility =  View.GONE
                newPass.visibility =  View.VISIBLE
                imageView.visibility =  View.GONE
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, this, clickListener = {
                    passClickListener(it)
                })
            }
        }

        searchPassField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                for ((index, pair) in passwords.withIndex()) {
                    if (pair.first.contains(s.toString())){
                        passwords2.add(pair)
                        quality2.add(quality[index])
                    }
                }
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putString(KEY_USERNAME, login)
            commit()
        }

        userAvatar.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("activity","menu");
            startActivity(intent)
            finish()
        }

        val list = mutableListOf<String>()
        // Loop through the chips
        for (index in 0 until passSettings.childCount) {
            val chip: Chip = passSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener{view, isChecked ->
                if (isChecked){
                    if (view.id == R.id.lettersToggle)
                        useLetters = true
                    if (view.id == R.id.symToggles)
                        useSymbols = true
                    if (view.id == R.id.numbersToggle)
                        useNumbers = true
                    if (view.id == R.id.upperCaseToggle)
                        useUC = true
                    list.add(view.text.toString())
                }else{
                    if (view.id == R.id.lettersToggle)
                        useLetters = false
                    if (view.id == R.id.symToggles)
                        useSymbols = false
                    if (view.id == R.id.numbersToggle)
                        useNumbers = false
                    if (view.id == R.id.upperCaseToggle)
                        useUC = false
                    list.remove(view.text.toString())
                }
            }
        }

        lengthToggle.text = getString(R.string.length)  + ": " +  length
        lengthToggle.setOnClickListener {
            if(seekBar.visibility ==  View.GONE){
                seekBar.visibility =  View.VISIBLE
            }
            else{
                seekBar.visibility =  View.GONE
            }
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

        generatePassword.setOnClickListener {
            val myPasswordManager = PasswordManager()
            //Create a password with letters, uppercase letters, numbers but not special chars with 17 chars
            if(list.size == 0){
                genPasswordId.error = getString(R.string.noRules)
            }
            else {
                genPasswordId.error = null
                val newPassword: String =
                    myPasswordManager.generatePassword(useLetters, useUC, useNumbers, useSymbols, length)
                genPasswordIdField.setText(newPassword)
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

        newPass.setOnClickListener {
            val intent = Intent(this, NewPasswordActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("pass", genPasswordIdField.text.toString())
            intent.putExtra("useLetters", useLetters)
            intent.putExtra("useUC", useUC)
            intent.putExtra("useNumbers", useNumbers)
            intent.putExtra("useSymbols", useSymbols)
            intent.putExtra("length", length)
            startActivity(intent)
        }
    }

    private fun passClickListener(position: Int) {
        // You got the position of ArrayList
        val intent = Intent(this, PasswordViewActivity::class.java)
        intent.putExtra("login", login)
        intent.putExtra("passName", passwords[position].first)
        startActivity(intent)
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

}