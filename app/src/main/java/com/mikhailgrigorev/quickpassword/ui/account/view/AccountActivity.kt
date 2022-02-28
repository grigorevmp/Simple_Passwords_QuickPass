package com.mikhailgrigorev.quickpassword.ui.account.view

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityAccountBinding
import com.mikhailgrigorev.quickpassword.ui.about.AboutActivity
import com.mikhailgrigorev.quickpassword.ui.account.edit.EditAccountActivity
import com.mikhailgrigorev.quickpassword.ui.auth.auth.AuthActivity
import com.mikhailgrigorev.quickpassword.ui.donut.DonutActivity
import com.mikhailgrigorev.quickpassword.ui.settings.SettingsActivity

class AccountActivity : AppCompatActivity() {

    private lateinit var viewModel: AccountViewModel

    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0
    private var pass2FA = 0
    private var encryptedPass = 0
    private var timeLimit = 0
    private var pins = 0

    private var condition = true
    private lateinit var binding: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAnalyze()
        initViewModel()
        setHelloText()
        setObservers()
        setAnimation()
        setListeners()
    }

    private fun checkAnalyze() {
        if (!Utils.useAnalyze()) {
            binding.tvTotalPointsText.visibility = View.GONE
            binding.tvTotalPoints.visibility = View.GONE
            binding.cvQualityCard.visibility = View.GONE
            binding.cvTotalPoints.visibility = View.GONE
            binding.cvSpecialInfo.visibility = View.GONE
            binding.cvEncrypted.visibility = View.GONE
        }
    }

    private fun setHelloText() {
        val login = Utils.getLogin()!!
        val name: String = getString(R.string.hi) + " " + login
        binding.tvUsernameText.text = name
        binding.tvAvatarSymbol.text = login[0].toString()
    }

    private fun setObservers() {
        viewModel.getPasswordNumberWithQuality().first.observe(this) { safePass_ ->
            safePass = safePass_
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().second.observe(this) { notSafe_ ->
            fixPass = notSafe_
            setPasswordQualityText()
        }

        viewModel.getPasswordNumberWithQuality().third.observe(this) { negative_ ->
            unsafePass = negative_
            setPasswordQualityText()
        }
        viewModel.getItemsNumber().observe(this) { passwordNumber ->
            binding.tvAllPasswords.text = passwordNumber.toString()
        }

        viewModel.getItemsNumberWith2fa().observe(this) { pass2FA_ ->
            pass2FA = pass2FA_
            setPasswordQualityText()
        }

        viewModel.getItemsNumberWithEncrypted().observe(this) { encryptedPass_ ->
            encryptedPass = encryptedPass_
            setPasswordQualityText()
        }
        viewModel.getItemsNumberWithTimeLimit().observe(this) { timeLimit_ ->
            timeLimit = timeLimit_
            setPasswordQualityText()
        }
        viewModel.getPinItems().observe(this) { pins_ ->
            pins = pins_
            setPasswordQualityText()
        }

    }

    private fun setPasswordQualityText() {
        binding.tvCorrectPasswords.text = resources.getQuantityString(
                R.plurals.correct_passwords,
                safePass,
                safePass
        )
        binding.tvNegativePasswords.text = resources.getQuantityString(
                R.plurals.incorrect_password,
                fixPass,
                fixPass
        )
        binding.tvNotSafePasswords.text = resources.getQuantityString(
                R.plurals.need_fix,
                unsafePass,
                unsafePass
        )
        binding.tvNumberOfUse2faText.text = pass2FA.toString()
        binding.tvNumberOfEncrypted.text = encryptedPass.toString()

        binding.tvNumberOfTimeNotificationText.text = timeLimit.toString()
        binding.tvPinText.text = pins.toString()

        if (safePass + fixPass + unsafePass > 0) {
            if (binding.tvAllPasswords.text.toString() != "0") {
                binding.tvTotalPoints.text =
                        ((safePass.toFloat() + fixPass.toFloat() / 2 + unsafePass.toFloat() * 0 + encryptedPass.toFloat() + pass2FA.toFloat())
                                / (7 / 3 * (safePass.toFloat() + unsafePass.toFloat() + fixPass.toFloat())))
                                .toString()
            } else {
                binding.tvTotalPoints.text = "0"
            }
        }
    }

    private fun setAnimation() {
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotation.fillAfter = true
        binding.ivSettings.startAnimation(rotation)
    }

    private fun setListeners() {
        binding.ivAboutApp.setOnClickListener {
            condition = false
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
        binding.cvAdditionalInfoCard.setOnClickListener {
            condition = false
            val intent = Intent(this, DonutActivity::class.java)
            startActivity(intent)
        }
        binding.ivLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.exit_account))
            builder.setMessage(getString(R.string.accountExitConfirm))
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                exit()
            }
            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.cvEditAccount.setOnClickListener {
            condition = false
            val intent = Intent(this, EditAccountActivity::class.java)
            startActivity(intent)
        }

        binding.ivSettings.setOnClickListener {
            condition = false
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.ivDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.accountDelete))
            builder.setMessage(getString(R.string.accountDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                // TODO Account delete
                Utils.makeToast(this, getString(R.string.accountDeleted))
                Utils.auth.currentUser?.delete()
                exit()
            }

            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.ivBack.setOnClickListener {
            condition = false
            finish()
        }
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                condition = false
                finish()
            }
        }
        return false
    }

    private fun exit() {
        condition = false
        Utils.exitAccount()
        Utils.auth.signOut()
        removeShortcuts()
    }

    private fun removeShortcuts() {
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

    private fun initViewModel() {
        viewModel = ViewModelProvider(
                this,
                AccountViewModelFactory()
        )[AccountViewModel::class.java]
    }

}