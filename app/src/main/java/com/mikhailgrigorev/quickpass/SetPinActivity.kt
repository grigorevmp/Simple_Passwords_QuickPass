package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_set_pin.*

class SetPinActivity : AppCompatActivity() {
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USEPIN = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_pin)

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
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
                when(cursor.getString(imageIndex).toString()){
                    "ic_account" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                    "ic_account_Pink" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink)
                    "ic_account_Red" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red)
                    "ic_account_Purple" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple)
                    "ic_account_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet)
                    "ic_account_Dark_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet)
                    "ic_account_Blue" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue)
                    "ic_account_Cyan" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan)
                    "ic_account_Teal" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal)
                    "ic_account_Green" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green)
                    "ic_account_lightGreen" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen)
                    else -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                }
                accountAvatarText.text = login.get(0).toString()
            } while (cursor.moveToNext())
        }

        num0.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "0")
        }
        num1.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "1")
        }
        num2.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "2")
        }
        num3.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "3")
        }
        num4.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "4")
        }
        num5.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "5")
        }
        num6.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "6")
        }
        num7.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "7")
        }
        num8.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "8")
        }
        num9.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "9")
        }
        erase.setOnClickListener {
            if(inputPinIdField.text.toString().isNotEmpty())
                inputPinIdField.setText(inputPinIdField.text.toString().substring(0, inputPinIdField.text.toString().length - 1))
        }

        inputPinIdField.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(inputPinIdField.text.toString().length == 4){
                    savePin.alpha = 1F
                }
                else{
                    savePin.alpha = 0F
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        savePin.setOnClickListener {
            val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString(KEY_USEPIN, inputPinIdField.text.toString())
                commit()
            }
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            intent.putExtra("activity", account)
            startActivity(intent)
            finish()
        }


    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent(this, AccountActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                this.overridePendingTransition(R.anim.right_in,
                        R.anim.right_out)
                finish()
            }
        }
        return false
    }

}