package com.mikhailgrigorev.quickpassword.ui.auth.login

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityLoginBinding
import com.mikhailgrigorev.quickpassword.ui.auth.auth.AuthActivity
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainActivity
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initName()
        initListeners()
        setBiometricFeature()
    }

    private fun initName() {
        val name: String = getString(R.string.hi) + " " + Utils.getLogin()
        binding.tvUsernameText.text = name
        binding.loginFab.show()
        this.let {
            if(Utils.auth.currentUser?.photoUrl != null){
                binding.tvAvatarSymbol.text = Utils.auth.currentUser?.photoUrl.toString()
                binding.tvAvatarSymbol.visibility = View.VISIBLE
                binding.ivPersonSample.visibility = View.GONE
            }
        }
    }

    private fun setBiometricFeature() {
        val bioMode = Utils.getBioMode()

        val hasBiometricFeature: Boolean =
                this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)

        val intent = Intent(this, MainActivity::class.java)

        if (hasBiometricFeature) {
            executor = ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            super.onAuthenticationSucceeded(result)
                            val args: Bundle? = intent.extras
                            val from = args?.getString("openedFrom", "none").toString()
                            if (args == null || from == "none") {
                                startActivity(intent)
                            } else {
                                intent.putExtra("openedFrom", args.get("none").toString())
                            }
                            finish()
                        }
                    })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.biometricLogin))
                    .setSubtitle(getString(R.string.logWithBio))
                    .setNegativeButtonText(getString(R.string.usePass))
                    .build()
        }

        if (hasBiometricFeature and bioMode) {
            binding.finger.visibility = View.VISIBLE
            binding.finger.isClickable = true
            biometricPrompt.authenticate(promptInfo)

            binding.finger.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        }


    }

    private fun initListeners() {
        val userLogin = Utils.getLogin()

        binding.loginFab.setOnClickListener {
            val password = binding.inputPasswordIdField.text.toString()
            if (
                validate(password)
                and (userLogin != null)
            ) {
                signIn(password)
            }
        }

        binding.fabLogOut.setOnClickListener {
            exit()
        }

    }

    private fun exit() {
        Utils.exitAccount()
        Utils.auth.signOut()
        deleteShortcuts()
    }

    private fun deleteShortcuts() {
        val shortcutList = mutableListOf<ShortcutInfo>()

        val shortcutManager: ShortcutManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager =
                    getSystemService(ShortcutManager::class.java)!!

            shortcutManager.dynamicShortcuts = shortcutList
        }

        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
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

    private fun signIn(password: String) {
        val userMail = Utils.getMail()!!
        Utils.auth.signInWithEmailAndPassword(
                userMail,
                password
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val args: Bundle? = intent.extras
                val from = args?.getString("openedFrom", "none").toString()
                if (args == null || from == "none") {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    intent.putExtra("openedFrom", args.get("none").toString())
                }
                finish()
            }
        }.addOnFailureListener {
            binding.inputPasswordId.error = getString(R.string.wrong_pass)
        }
    }

}