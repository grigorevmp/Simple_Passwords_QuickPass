package com.mikhailgrigorev.quickpass

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_gen)


        val args: Bundle? = intent.extras
        val login: String? = args?.get("login").toString()
        val name: String? = "Hi, $login"
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
                val ex_infoImgText = cursor.getString(imageIndex).toString()
                val infoImgText = "Avatar src: $ex_infoImgText"
                val id = getResources().getIdentifier(
                    ex_infoImgText,
                    "drawable",
                    packageName
                )
                userAvatar.setImageResource(id)
            } while (cursor.moveToNext())
        }

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putString(KEY_USERNAME, login)
            commit()
        }

        viewAccount.setOnClickListener {
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

                if (list.isNotEmpty()){
                    // SHow the selection
                    toast("Selected $list")
                }
            }
        }

        lengthToggle.setOnClickListener {
            toast("Length is $length")
        }

        checkPassword.setOnClickListener {
            val myPasswordManager = PasswordManager()
            //Evaluate password
            var evaluation: Float = myPasswordManager.evaluatePassword(genPasswordIdField.text.toString())
            toast(evaluation.toString())
        }

        generatePassword.setOnClickListener {
            val myPasswordManager = PasswordManager()
            //Create a password with letters, uppercase letters, numbers but not special chars with 17 chars
            if(list.size == 0){
                toast("You should choose at least 1 rule")
            }
            else {
                val newPassword: String =
                    myPasswordManager.generatePassword(useLetters, useUC, useNums, useSyms, length)
                genPasswordIdField.setText(newPassword)
            }
        }

    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

}