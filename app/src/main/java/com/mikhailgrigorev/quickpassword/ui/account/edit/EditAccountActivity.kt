package com.mikhailgrigorev.quickpassword.ui.account.edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.UserProfileChangeRequest
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityEditAccountBinding


class EditAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAccountBinding

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setHelloText()
        setListeners()

    }

    private fun initViews() {
        binding.etUserEmail.setText(
                Utils.auth.currentUser?.email
        )
        binding.etUserLogin.setText(
                Utils.auth.currentUser?.displayName
        )
    }

    private fun setListeners() {
        binding.savePass.setOnClickListener {
            val login = binding.etUserLogin.toString()
            val mail = binding.etUserEmail.toString()
            val password = binding.etPassword.toString()
            val newPassword = binding.etNewPassword.toString()
            Utils.auth.signInWithEmailAndPassword(
                    Utils.getMail()!!,
                    password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (mail != "") {
                        Utils.auth.currentUser?.updateEmail(mail)
                        Utils.setMail(mail)
                    }
                    if (login != "") {
                        UserProfileChangeRequest.Builder().apply {
                            displayName = login
                        }.build()
                        Utils.setLogin(mail)
                    }
                    if (newPassword != "") {
                        Utils.auth.currentUser?.updatePassword(newPassword)
                    }
                    Utils.makeToast(this, "Saved")
                    finish()
                }
            }.addOnFailureListener { exception ->
                Utils.makeToast(this, exception.localizedMessage)
                exception.message?.let { Utils.makeToast(this, it) }
            }
        }

        binding.back.setOnClickListener {
            finish()
        }
        binding.generatePassword.setOnClickListener {
            binding.tilNewPassword.error = null
            val newPassword: String =
                    Utils.password_manager.generatePassword(
                            isWithLetters = true,
                            isWithUppercase = true,
                            isWithNumbers = true,
                            isWithSpecial = false,
                            length = 12
                    )
            binding.etNewPassword.setText(newPassword)
            binding.generatePassword.animate()
                    .rotation((binding.generatePassword.rotation + 45F) % 360F).interpolator =
                    AccelerateDecelerateInterpolator()
        }
    }

    private fun setHelloText() {
        val login = Utils.getLogin()!!
        val name: String = getString(R.string.hi) + " " + login
        binding.tvUsernameText.text = name
        binding.tvAvatarSymbol.text = login[0].toString()
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
