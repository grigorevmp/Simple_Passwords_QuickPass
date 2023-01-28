package com.mikhailgrigorev.simple_password.ui.pin_code.set

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.base.MyBaseActivity
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.databinding.ActivityPinSetBinding

class PinSetActivity : MyBaseActivity() {
    private lateinit var binding: ActivityPinSetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHello()
        setListeners()
    }

    private fun initHello() {
        val login = Utils.accountSharedPrefs.getLogin()!!
        val name: String = getString(R.string.hi) + " " + login
        binding.tvUsernameText.text = name
    }

    private fun setListeners() {
        binding.num0.setOnClickListener {
            addPinNumber(0)
        }
        binding.num1.setOnClickListener {
            addPinNumber(1)
        }
        binding.num2.setOnClickListener {
            addPinNumber(2)
        }
        binding.num3.setOnClickListener {
            addPinNumber(3)
        }
        binding.num4.setOnClickListener {
            addPinNumber(4)
        }
        binding.num5.setOnClickListener {
            addPinNumber(5)
        }
        binding.num6.setOnClickListener {
            addPinNumber(6)
        }
        binding.num7.setOnClickListener {
            addPinNumber(7)
        }
        binding.num8.setOnClickListener {
            addPinNumber(8)
        }
        binding.num9.setOnClickListener {
            addPinNumber(9)
        }
        binding.erase.setOnClickListener {
            erasePinNumber()
        }
        binding.savePin.setOnClickListener {
            Utils.toggleManager.pinModeToggle.set(true)
            Utils.setPin(binding.inputPinIdField.text.toString().toInt())
            finish()
        }
        binding.inputPinIdField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.inputPinIdField.text.toString().length == 4) {
                    binding.savePin.alpha = 1F
                } else {
                    binding.savePin.alpha = 0F
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun addPinNumber(number: Int) {
        if (binding.inputPinIdField.text.toString().length < 4)
            binding.inputPinIdField.setText(
                    getString(
                            R.string.stringConcat,
                            binding.inputPinIdField.text,
                            number
                    )
            )
    }

    private fun erasePinNumber() {
        if (binding.inputPinIdField.text.toString().isNotEmpty())
            binding.inputPinIdField.setText(
                    binding.inputPinIdField.text
                            .toString()
                            .substring(0, binding.inputPinIdField.text.toString().length - 1)
            )
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                finish()
            }
        }
        return false
    }

}