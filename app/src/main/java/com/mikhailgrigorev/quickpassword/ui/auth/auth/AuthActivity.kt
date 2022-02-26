package com.mikhailgrigorev.quickpassword.ui.auth.auth

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityAuthBinding
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginActivity
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainActivity
import com.mikhailgrigorev.quickpassword.ui.pin_code.view.PinActivity

class AuthActivity : AppCompatActivity() {

    private val defaultRotation = 45F
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginFab.show()

        checkIntents()
        initListeners()
    }

    private fun checkIntents() {
        val userLogin = Utils.getLogin()
        val usePin = Utils.getPinMode()
        var intent: Intent? = null
        var goToIntent = false

        if (usePin) {
            intent = Intent(this, PinActivity::class.java)
            goToIntent = true
        } else if (userLogin != "none") {
            intent = Intent(this, LoginActivity::class.java)
            goToIntent = true
        }
        if (goToIntent) {
            startActivity(intent)
            finish()
        }
    }

    private fun initListeners() {
        binding.loginFab.setOnClickListener {
            if (binding.signUpChip.isChecked) {
                if (validate(
                            binding.inputLoginIdField.text.toString(),
                            binding.inputPasswordIdField.text.toString()
                    )
                )
                    signUp(
                            binding.inputLoginIdField.text.toString(),
                            binding.inputPasswordIdField.text.toString()
                    )
            } else {
                if (validate(
                            binding.inputLoginIdField.text.toString(),
                            binding.inputPasswordIdField.text.toString()
                    )
                )
                    signIn(
                            binding.inputPasswordIdField.text.toString()
                    )
            }
        }

        binding.generatePassword.setOnClickListener {
            binding.inputPasswordId.error = null
            val newPassword: String =
                    Utils.password_manager.generatePassword(
                            isWithLetters = true,
                            isWithUppercase = true,
                            isWithNumbers = true,
                            isWithSpecial = false,
                            length = 12
                    )
            binding.inputPasswordIdField.setText(newPassword)

            binding.generatePassword.animate()
                    .rotation((binding.generatePassword.rotation + defaultRotation) % 360F).interpolator =
                    AccelerateDecelerateInterpolator()
        }

        binding.signUpChipGroup.setOnCheckedStateChangeListener { _, _ ->
            binding.signUpChip.let {
                if (binding.signUpChip.isChecked) {
                    binding.loginFab.hide()
                    binding.loginFab.text = getString(R.string.sign_up)
                    binding.loginFab.show()
                } else {
                    binding.loginFab.hide()
                    binding.loginFab.text = getString(R.string.sign_in)
                    binding.loginFab.show()
                }
            }
        }

        binding.sendMail.setOnClickListener {
            val login = binding.inputLoginIdField.text.toString()

            // TODO connect with Firebase
            val testMail = "test_mail"
            val testPassword = "test_password"

            Utils.sendMail(
                    testMail,
                    testPassword
            )

        }
    }

    private fun validate(login: String, password: String): Boolean {
        var valid = false
        if (login.isEmpty() || login.length < 3) {
            binding.inputLoginId.error = getString(R.string.errNumOfText)
        } else {
            binding.inputLoginId.error = null
            valid = true
        }
        if (password.isEmpty() || password.length < 4 || password.length > 20) {
            binding.inputPasswordId.error = getString(R.string.errPass)
            valid = false
        } else {
            binding.inputPasswordId.error = null
        }
        return valid
    }

    private fun signUp(login: String, password: String) {
        Utils.setLogin(login)
        signIn(password)
    }

    private fun signIn(password: String) {
        // TODO connect with Firebase
        val testPassword = "test_password"
        // val hashedPassword = BCrypt.hashpw(testPassword, BCrypt.gensalt(12))
        // val hashedUserPassword = BCrypt.hashpw(password, BCrypt.gensalt(12))


        if (password != "test") {
            binding.inputPasswordId.error = getString(R.string.wrong_pass)
            return
        } else {
            if (isAvailable(this)) {
                val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
                builder.setTitle(getString(R.string.bio_usage))
                builder.setMessage(getString(R.string.fingerUnlock))

                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    Utils.setBioMode(true)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                    Utils.setBioMode(false)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->
                    Utils.setBioMode(false)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                builder.setCancelable(false)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun isAvailable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }
}