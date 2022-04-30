package com.mikhailgrigorev.quickpassword.ui.auth.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.UserProfileChangeRequest
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityAuthBinding
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginActivity
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainActivity
import com.mikhailgrigorev.quickpassword.ui.pin_code.view.PinViewActivity

class AuthActivity : AppCompatActivity() {

    private val defaultRotation = 45F
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (!Utils.toggleManager.darkSideToggle.isEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginFab.show()

        checkIntents()
        initListeners()
    }

    private fun checkIntents() {
        val userLogin = Utils.accountSharedPrefs.getLogin()
        val usePin = Utils.toggleManager.pinModeToggle.isEnabled()
        var intent: Intent? = null
        var goToIntent = false

        if (usePin) {
            intent = Intent(this, PinViewActivity::class.java)
            goToIntent = true
        } else if (userLogin != "Stranger") {
            intent = Intent(this, LoginActivity::class.java)
            goToIntent = true
        }
        if (goToIntent) {
            startActivity(intent)
            finish()
        }
    }

    private fun initListeners() {

        binding.tvAvatarSymbol.text = "🦊"

        binding.loginFab.setOnClickListener {
            binding.inputLoginId.error = null
            binding.inputPasswordId.error = null
            binding.inputPassword2Id.error = null
            binding.tilUserLogin.error = null
            if (binding.inputLoginIdField.text.toString() != "") {
                if (binding.signUpChip.isChecked) {
                    if (validate(binding.inputPasswordIdField.text.toString())) {
                        if (
                            binding.inputPasswordIdField.text.toString()
                            ==
                            binding.inputPasswordId2Field.text.toString()
                        ) {
                            signUp(
                                    binding.inputLoginIdField.text.toString(),
                                    binding.inputPasswordIdField.text.toString()
                            )
                        } else {
                            binding.inputPassword2Id.error = "Different passwords -_-"
                        }
                    }
                } else {
                    if (validate(binding.inputPasswordIdField.text.toString())
                    ) {
                        signIn(
                                binding.inputLoginIdField.text.toString(),
                                binding.inputPasswordIdField.text.toString()
                        )
                    }
                }
            }
            else{
                binding.inputLoginId.error = "We need your email -_-"
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
                    binding.tilUserLogin.visibility = View.VISIBLE
                    binding.inputPassword2Id.visibility = View.VISIBLE
                } else {
                    binding.loginFab.hide()
                    binding.loginFab.text = getString(R.string.sign_in)
                    binding.loginFab.show()
                    binding.tilUserLogin.visibility = View.GONE
                    binding.inputPassword2Id.visibility = View.GONE
                }
            }
        }

        binding.tvSendForgotPassMail.setOnClickListener {
            val email = binding.inputLoginIdField.text.toString()
            binding.inputLoginId.error = null
            if (email != "") {
                Utils.auth.sendPasswordResetEmail(email)
                Utils.makeToast(this, "Email sent")
            } else {
                binding.inputLoginId.error = "Please enter your email"
            }
        }
    }

    private fun validate(password: String): Boolean {
        var valid = true
        if (Utils.validate(password)) {
            binding.inputPasswordId.error = getString(R.string.errPass)
            valid = false
        } else {
            binding.inputPasswordId.error = null
        }
        return valid
    }

    private fun signUp(email: String, password: String) {
        Utils.auth.createUserWithEmailAndPassword(
                email,
                password
        ).addOnCompleteListener { task ->

            var login = binding.etUserLogin.text.toString()

            if(login == ""){
                login = "Stranger"
            }

            if (task.isSuccessful) {
                Utils.accountSharedPrefs.setLogin(login)
                Utils.accountSharedPrefs.setMail(email)
                Utils.auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder().apply {
                            displayName = login
                        }.build()
                )
                goHome()
            }

        }.addOnFailureListener { exception ->
            exception.message?.let { Utils.makeToast(this, it) }
        }

    }

    private fun signIn(email: String, password: String) {
        Utils.auth.signInWithEmailAndPassword(
                email,
                password
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val login = Utils.auth.currentUser?.displayName!!
                Utils.accountSharedPrefs.setLogin(login)
                Utils.accountSharedPrefs.setMail(email)
                goHome()
            }
        }.addOnFailureListener { exception ->
            exception.message?.let { Utils.makeToast(this, it) }
        }
    }

    private fun goHome() {
        if (isAvailable()) {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.bio_usage))
            builder.setMessage(getString(R.string.fingerUnlock))

            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                Utils.toggleManager.bioModeToggle.set(true)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                Utils.toggleManager.bioModeToggle.set(false)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->
                Utils.toggleManager.bioModeToggle.set(false)
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

    private fun isAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }
}