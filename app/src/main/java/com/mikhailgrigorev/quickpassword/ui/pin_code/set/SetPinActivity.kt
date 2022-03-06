package com.mikhailgrigorev.quickpassword.ui.pin_code.set

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.base.MyBaseActivity
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivitySetPinBinding
import com.mikhailgrigorev.quickpassword.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpassword.ui.settings.SettingsActivity

class SetPinActivity : MyBaseActivity() {
    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsePin = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String
    private lateinit var binding: ActivitySetPinBinding

    @SuppressLint("SetTextI18n", "Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val cardRadius = sharedPref.getString("cardRadius", "none")
        if(cardRadius != null)
            if(cardRadius != "none") {
                binding.cardNums.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cardRadius.toFloat(), resources.displayMetrics)
            }

        login = Utils.getLogin()!!
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String = getString(R.string.hi) + " " + login
        binding.tvUsernameText.text = name


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
                    "ic_account" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                    "ic_account_Pink" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink
                    )
                    "ic_account_Red" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red
                    )
                    "ic_account_Purple" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple
                    )
                    "ic_account_Violet" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet
                    )
                    "ic_account_Dark_Violet" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet
                    )
                    "ic_account_Blue" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue
                    )
                    "ic_account_Cyan" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan
                    )
                    "ic_account_Teal" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal
                    )
                    "ic_account_Green" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green
                    )
                    "ic_account_lightGreen" -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen
                    )
                    else -> binding.cvAccountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                }
                binding.tvAvatarSymbol.text = login[0].toString()
            } while (cursor.moveToNext())
        }


        binding.num0.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "0")
        }
        binding.num1.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "1")
        }
        binding.num2.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "2")
        }
        binding.num3.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "3")
        }
        binding.num4.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "4")
        }
        binding.num5.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "5")
        }
        binding.num6.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "6")
        }
        binding.num7.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "7")
        }
        binding.num8.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "8")
        }
        binding.num9.setOnClickListener {
            if(binding.inputPinIdField.text.toString().length < 4)
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString() + "9")
        }
        binding.erase.setOnClickListener {
            if(binding.inputPinIdField.text.toString().isNotEmpty())
                binding.inputPinIdField.setText(binding.inputPinIdField.text.toString().substring(0, binding.inputPinIdField.text.toString().length - 1))
        }



        binding.inputPinIdField.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(binding.inputPinIdField.text.toString().length == 4){
                    binding.savePin.alpha = 1F
                }
                else{
                    binding.savePin.alpha = 0F
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.savePin.setOnClickListener {
            val sharedPref2 = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
            with (sharedPref2.edit()) {
                putString(_keyUsePin, binding.inputPinIdField.text.toString())
                commit()
            }
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                this.overridePendingTransition(
                        R.anim.right_in,
                        R.anim.right_out
                )
                finish()
            }
        }
        return false
    }

}