package com.mikhailgrigorev.quickpassword.ui.pin_code.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityPinViewBinding
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginActivity
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainActivity
import java.util.concurrent.Executor

class PinViewActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityPinViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initHello()
        setListeners()
        initBio()
    }

    private fun initBio() {
        val hasBiometricFeature: Boolean =
                this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        if (hasBiometricFeature) {
            if (Utils.toggleManager.bioModeToggle.isEnabled()) {
                binding.finger.visibility = View.VISIBLE
                binding.finger.isClickable = true
                val intent = Intent(this, MainActivity::class.java)
                executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor,
                        object : BiometricPrompt.AuthenticationCallback() {

                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult
                            ) {
                                super.onAuthenticationSucceeded(result)
                                startActivity(intent)
                                finish()
                            }

                        })

                promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.biometricLogin))
                        .setSubtitle(getString(R.string.logWithBio))
                        .setNegativeButtonText(getString(R.string.usePass))
                        .build()

                biometricPrompt.authenticate(promptInfo)

            }

            binding.finger.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        }
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
        binding.exit.setOnClickListener {
            exit()
        }
        val intent = Intent(this, MainActivity::class.java)
        binding.inputPinIdField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.inputPinIdField.text.toString().length == 4) {
                    if (binding.inputPinIdField.text.toString().toInt() == Utils.getPin()) {
                        startActivity(intent)
                        finish()
                    } else {
                        binding.inputPinId.error = getString(R.string.incorrectPin)
                    }
                } else {
                    binding.inputPinId.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun erasePinNumber() {
        if (binding.inputPinIdField.text.toString().isNotEmpty())
            binding.inputPinIdField.setText(
                    binding.inputPinIdField.text
                            .toString()
                            .substring(0, binding.inputPinIdField.text.toString().length - 1)
            )
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

    private fun exit() {
        Utils.exitAccount()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initHello() {
        val login = Utils.accountSharedPrefs.getLogin()!!
        val name: String = getString(R.string.hi) + " " + login
        binding.tvUsernameText.text = name
    }
}
