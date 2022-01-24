package com.mikhailgrigorev.quickpassword.ui.password_card.view

import android.animation.LayoutTransition
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityPasswordViewBinding
import com.mikhailgrigorev.quickpassword.ui.account.view.AccountActivity
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import com.mikhailgrigorev.quickpassword.ui.auth.re.login.ReLoginActivity
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModel
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModelFactory
import com.mikhailgrigorev.quickpassword.ui.password_card.edit.EditPassActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PasswordViewActivity : AppCompatActivity() {

    private lateinit var viewModel: PasswordViewModel
    private lateinit var from: String

    private lateinit var login: String
    private lateinit var binding: ActivityPasswordViewBinding
    private var condition = true

    private fun setQuitTimer() {
        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            if (condition) {
                condition = false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        val lockTime = Utils.lockTime()
        if (lockTime != "0") {
            handler.postDelayed(
                    r, Utils.lock_default_interval * lockTime!!.toLong()
            )
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
                this,
                PasswordViewModelFactory()
        )[PasswordViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // App Quit Timer
        setQuitTimer()
        initViewModel()

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()

        from = args?.get("openedFrom").toString()
        if (from == "shortcut") {
            intent.putExtra("login", login)
            intent.putExtra("password_id", args?.get("password_id").toString())
            condition = false
            val intent = Intent(this, ReLoginActivity::class.java)
            startActivityForResult(intent, 1)
        }

        val passwordId = args?.get("password_id").toString().toInt()

        loadPassword(passwordId)
        setListeners()

        if ((args?.get("sameWith") != null) and (args?.get("sameWith").toString() != "none")) {
            binding.warning0.visibility = View.VISIBLE
            binding.sameParts.visibility = View.VISIBLE
            binding.sameParts.text = args?.get("sameWith").toString()
            binding.passQuality.setTextColor(
                    ContextCompat.getColor(
                            applicationContext,
                            R.color.negative
                    )
            )
            binding.passQuality.text = getString(R.string.low)
        }

        if (Utils.autoCopy() == "none" && binding.passViewFieldView.text.toString() != "") {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", binding.passViewFieldView.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(applicationContext, getString(R.string.passCopied))
        }
    }

    private fun loadPassword(passwordId: Int) {
        viewModel.getPasswordById(passwordId).observe(this) { passwordCard ->
            if (passwordCard.same_with != "") {
                binding.warning0.visibility = View.VISIBLE
                binding.sameParts.visibility = View.VISIBLE
                binding.sameParts.text = passwordCard.same_with
                binding.passQuality.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.negative
                        )
                )
                binding.passQuality.text = getString(R.string.low)
            }

            viewModel.currentPassword = passwordCard

            if (viewModel.currentPassword!!.favorite) {
                binding.favButton.visibility = View.GONE
                binding.favButton2.visibility = View.VISIBLE
            }

            binding.helloTextId.text = passwordCard.name
            var dbPassword = passwordCard.password

            if (passwordCard.encrypted) {
                binding.crypt.isChecked = true
                binding.crypt.visibility = View.VISIBLE
                binding.addSettings.visibility = View.VISIBLE
                binding.cypheredWarn.visibility = View.VISIBLE
                binding.cypheredWarnImg.visibility = View.VISIBLE
                dbPassword = Utils.password_manager.decrypt(dbPassword).toString()
            }

            binding.passViewFieldView.setText(dbPassword)

            var evaluation: String = Utils.password_manager.evaluatePasswordString(dbPassword)

            binding.passwordTime.text = getString(R.string.time_lim) + " " + passwordCard.time

            if ((Utils.password_manager.evaluateDate(passwordCard.time)) && (dbPassword.length != 4)) {
                binding.warnCard.visibility = View.VISIBLE
                evaluation = "low"
            }

            if (
                Utils.password_manager.popularPasswords(dbPassword) or (
                        (dbPassword.length == 4) and
                                Utils.password_manager.popularPin(dbPassword)
                        )
            ) {
                binding.tooEasy.visibility = View.VISIBLE
                binding.tooEasyImg.visibility = View.VISIBLE
                evaluation = "low"
            }
            when (evaluation) {
                "low" -> binding.passQuality.text = getString(R.string.low)
                "high" -> binding.passQuality.text = getString(R.string.high)
                else -> binding.passQuality.text = getString(R.string.medium)
            }
            when (evaluation) {
                "low" -> binding.passQuality.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.negative
                        )
                )
                "high" -> binding.passQuality.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.positive
                        )
                )
                else -> binding.passQuality.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.fixable
                        )
                )
            }
            if (evaluation == "high")
                binding.warning.visibility = View.GONE
            else
                binding.warning2.visibility = View.GONE

            if ((dbPassword.length == 4) and (evaluation == "high")) {
                binding.passQualityText.text = getString(R.string.showPin)
                binding.passQuality.visibility = View.GONE
                binding.warning.visibility = View.GONE
                binding.warning2.visibility = View.VISIBLE
                binding.warning2.setImageDrawable(
                        AppCompatResources.getDrawable(
                                this,
                                R.drawable.credit_card
                        )
                )
            }

            binding.authToggle.visibility = View.GONE
            binding.timeLimit.visibility = View.GONE
            if (passwordCard.use_2fa) {
                binding.authToggle.isChecked = true
                binding.authToggle.visibility = View.VISIBLE
                binding.addSettings.visibility = View.VISIBLE
            }
            if (passwordCard.use_time) {
                binding.timeLimit.isChecked = true
                binding.timeLimit.visibility = View.VISIBLE
                binding.addSettings.visibility = View.VISIBLE
            }

            if (passwordCard.description != "")
                binding.noteViewField.setText(passwordCard.description)
            else
                binding.noteView.visibility = View.GONE

            if (passwordCard.login != "")
                binding.emailViewField.setText(passwordCard.login)
            else
                binding.emailView.visibility = View.GONE

            if (passwordCard.tags != "") {
                passwordCard.tags.split("\\s".toRegex()).forEach { item ->
                    val chip = Chip(binding.cgPasswordChipGroup.context)
                    chip.text = item
                    chip.isClickable = false
                    binding.cgPasswordChipGroup.addView(chip)
                }
            } else {
                binding.kwInfo.visibility = View.GONE
            }

            val mediaStorageDir = File(
                    applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
            )
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs()
                Utils.makeToast(applicationContext, "Directory Created")
            }

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("App", "failed to create directory")
                }
            }

            val file = File(mediaStorageDir, "${viewModel.currentPassword!!.name}.jpg")
            if (file.exists()) {
                val uri = Uri.fromFile(file)
                binding.attachedImage.setImageURI(uri)
                binding.attachedImageText.visibility = View.VISIBLE
                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val widthMax: Int = size.x
                val width = (widthMax / 2.4).toInt()
                val height =
                        binding.attachedImage.drawable.minimumHeight * width / binding.attachedImage.drawable.minimumWidth
                binding.attachedImage.layoutParams.height = height
                binding.attachedImage.layoutParams.width = width

                binding.attachedImage.setOnClickListener {
                    val uriForOpen = FileProvider.getUriForFile(
                            this,
                            this.applicationContext.packageName.toString() + ".provider",
                            file
                    )
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(uriForOpen, "image/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setListeners() {
        binding.deletePassword.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.deletePassword))
            builder.setMessage(getString(R.string.passwordDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.deletePassword()
                    Utils.makeToast(applicationContext, getString(R.string.passwordDeleted))
                }
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("login", login)
                condition = false
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            }

            builder.setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.helloTextId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Name", binding.helloTextId.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(applicationContext, getString(R.string.nameCopied))
        }

        binding.accountAvatar.setOnClickListener {
            condition = false
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("activity", "menu")
            startActivityForResult(intent, 1)
        }

        binding.emailView.setOnClickListener {
            if (binding.emailViewField.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Login", binding.emailViewField.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.loginCopied))
            }
        }

        binding.emailViewField.setOnClickListener {
            if (binding.emailViewField.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Login", binding.emailViewField.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.loginCopied))
            }
        }

        binding.passView.setOnClickListener {
            if (binding.passViewFieldView.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                        ClipData.newPlainText("Password", binding.passViewFieldView.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.passCopied))
            }
        }

        binding.passViewFieldView.setOnClickListener {
            if (binding.passViewFieldView.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                        ClipData.newPlainText("Password", binding.passViewFieldView.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.passCopied))
            }
        }

        binding.back.setOnClickListener {
            if (from != "short") {
                condition = false
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("password_id", viewModel.currentPassword!!._id)
                setResult(1, intent)
                finish()
            } else {
                condition = false
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("password_id", viewModel.currentPassword!!._id)
                startActivity(intent)
                finish()
            }
        }

        binding.editButton.setOnClickListener {
            condition = false
            val intent = Intent(this, EditPassActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("password_id", viewModel.currentPassword!!._id)
            startActivityForResult(intent, 1)
        }

        binding.favButton.setOnClickListener {
            binding.favButton.visibility = View.GONE
            binding.favButton2.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.favPassword()
            }
        }

        binding.favButton2.setOnClickListener {
            binding.favButton.visibility = View.VISIBLE
            binding.favButton2.visibility = View.GONE
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.favPassword()
            }
        }

        if (Utils.useAnalyze() != null)
            if (Utils.useAnalyze() != "none") {
                binding.passQualityText.visibility = View.GONE
                binding.warning.visibility = View.GONE
                binding.passQualityText.visibility = View.GONE
                binding.passQuality.visibility = View.GONE
                binding.warning2.visibility = View.GONE
                binding.sameParts.visibility = View.GONE
                binding.warning0.visibility = View.GONE
            }


        val layoutTransition = binding.mainLinearLayout.layoutTransition
        layoutTransition.setDuration(5000) // Change duration
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (from != "short") {
                    condition = false
                    val intent = Intent()
                    intent.putExtra("login", login)
                    intent.putExtra("password_id", viewModel.currentPassword!!._id)
                    setResult(1, intent)
                    finish()
                } else {
                    condition = false
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("login", login)
                    intent.putExtra("password_id", viewModel.currentPassword!!._id)
                    startActivity(intent)
                    finish()
                }
            }
        }
        return false
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 1) {
                condition=false
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }
}