package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_pass_gen.*


class PassGenActivity : AppCompatActivity() {

    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private var length = 20
    private var useSyms = false
    private var useUC = false
    private var useLetters = false
    private var useNums = false
    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0
    val passwords: ArrayList<String> = ArrayList()
    lateinit var login: String

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

        correctPasswords.text = safePass.toString() + " " + getString(R.string.correct_passwords)
        negativePasswords.text = unsafePass.toString() + " " + getString(R.string.incorrect_password)
        fixPasswords.text = getString(R.string.need_fix) + " " + fixPass.toString() + " " + getString(R.string.passwords)

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


        var dbLogin: String
        var dbPassword: String

        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pdatabase = pdbHelper.writableDatabase
        try {
            val pcursor: Cursor = pdatabase.query(
                pdbHelper.TABLE_USERS, arrayOf(pdbHelper.KEY_NAME, pdbHelper.KEY_PASS),
                null, null,
                null, null, null
            )


            if (pcursor.moveToFirst()) {
                val nameIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pcursor.getColumnIndex(pdbHelper.KEY_PASS)
                do {
                    dbLogin = pcursor.getString(nameIndex).toString()
                    passwords.add(dbLogin)
                    dbPassword = pcursor.getString(passIndex).toString()
                } while (pcursor.moveToNext())
            }
        } catch (e: SQLException) {
        }

        passwordRecycler.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.VERTICAL, false)

        passwordRecycler.setHasFixedSize(true)

        passwordRecycler.adapter = PasswordAdapter(passwords, this, clickListener = {
            passClickListener(it)
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
            startActivity(intent)
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

        lengthToggle.text = getString(R.string.length)  + ": " +  length
        lengthToggle.setOnClickListener {
            if(seekBar2.visibility ==  View.GONE){
                seekBar2.visibility =  View.VISIBLE
            }
            else{
                seekBar2.visibility =  View.GONE
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
            intent.putExtra("useNums", useNums)
            intent.putExtra("useSyms", useSyms)
            intent.putExtra("length", length)
            startActivity(intent)
        }
    }

    private fun passClickListener(position: Int) {
        // You got the position of ArrayList
        val intent = Intent(this, PasswordViewActivity::class.java)
        intent.putExtra("login", login)
        intent.putExtra("passName", passwords[position])
        startActivity(intent)
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

}