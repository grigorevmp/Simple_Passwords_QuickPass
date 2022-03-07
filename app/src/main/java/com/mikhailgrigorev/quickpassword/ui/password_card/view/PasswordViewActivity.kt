package com.mikhailgrigorev.quickpassword.ui.password_card.view

import android.animation.LayoutTransition
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.PasswordQuality
import com.mikhailgrigorev.quickpassword.common.base.MyBaseActivity
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ActivityPasswordViewBinding
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginActivity
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainNewActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModel
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModelFactory
import com.mikhailgrigorev.quickpassword.ui.password_card.edit.PasswordEditActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PasswordViewActivity : MyBaseActivity() {

    private lateinit var viewModel: PasswordViewModel
    private lateinit var from: String
    private lateinit var binding: ActivityPasswordViewBinding

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
        initViewModel()

        checkShortcut()
        val args: Bundle? = intent.extras
        val passwordId = args?.get("password_id").toString().toInt()

        setUpAutoCopy()
        loadPassword(passwordId)
        setListeners()
    }

    private fun setUpAutoCopy() {
        val args: Bundle? = intent.extras
        from = args?.get("openedFrom").toString()
        if (from == "shortcut") {
            intent.putExtra("openedFrom", args?.get("openedFrom").toString())
            intent.putExtra("password_id", args?.get("password_id").toString())
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkShortcut() {
        if (Utils.getAutoCopy() && binding.etPassword.text.toString() != "") {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", binding.etPassword.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(applicationContext, getString(R.string.passCopied))
        }
    }

    private fun loadPassword(passwordId: Int) {
        viewModel.getPasswordById(passwordId).observe(this) { passwordCard ->
            if (passwordCard.same_with != "") {
                binding.imSamePartsImage.visibility = View.VISIBLE
                binding.sameParts.visibility = View.VISIBLE
                binding.sameParts.text = this.getString(R.string.same_parts, passwordCard.same_with)
            }

            viewModel.currentPassword = passwordCard
            binding.cFolderName.visibility = View.GONE

            if(passwordCard.folder != null) {
                if(passwordCard.folder!! > 0) {
                    viewModel.getFolder(passwordCard.folder!!).observe(this) {
                        binding.cFolderName.visibility = View.VISIBLE
                        binding.cFolderName.text = it.name
                        binding.tvAdditionalSettings.visibility = View.VISIBLE
                    }
                }
            }

            if (viewModel.currentPassword!!.favorite) {
                binding.favButton.visibility = View.GONE
                binding.favButton2.visibility = View.VISIBLE
            }

            binding.tvUsernameText.text = passwordCard.name
            var originalPassword = passwordCard.password

            if (passwordCard.encrypted) {
                binding.cUseEncryption.isChecked = true
                binding.cUseEncryption.visibility = View.VISIBLE
                binding.tvAdditionalSettings.visibility = View.VISIBLE
                originalPassword = Utils.password_manager.decrypt(originalPassword).toString()
            }

            binding.etPassword.setText(originalPassword)

            val evaluation = passwordCard.quality

            binding.tvPasswordCreationDate.text = getString(
                    R.string.time_lim,
                    Utils.returnReadableDate(passwordCard.time)
            )

            when (evaluation) {
                PasswordQuality.LOW.value -> {
                    binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.red_quality
                            )
                    )
                    binding.passQuality.text = getString(R.string.low)
                    binding.ivMinorWarningImage.visibility = View.GONE
                }
                PasswordQuality.HIGH.value -> {
                    binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.green_quality
                            )
                    )
                    binding.passQuality.text = getString(R.string.high)
                    binding.ivMainWarningImage.visibility = View.GONE
                }
                PasswordQuality.MEDIUM.value -> {
                    binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.yellow_quality
                            )
                    )
                    binding.passQuality.text = getString(R.string.medium)
                    binding.ivMinorWarningImage.visibility = View.GONE
                }
            }

            if ((originalPassword.length == 4) and (evaluation == PasswordQuality.HIGH.value)) {
                binding.passQualityText.text = getString(R.string.showPin)
                binding.passQuality.visibility = View.GONE
                binding.ivMainWarningImage.visibility = View.GONE
                binding.ivMinorWarningImage.visibility = View.VISIBLE
                binding.ivMinorWarningImage.setImageDrawable(
                        AppCompatResources.getDrawable(
                                this,
                                R.drawable.credit_card
                        )
                )
            }

            if (passwordCard.is_card_pin) {
                binding.cIsPin.isChecked = true
                binding.cIsPin.visibility = View.VISIBLE
                binding.tvAdditionalSettings.visibility = View.VISIBLE
            }

            if (passwordCard.use_2fa) {
                binding.cUse2fa.isChecked = true
                binding.cUse2fa.visibility = View.VISIBLE
                binding.tvAdditionalSettings.visibility = View.VISIBLE
            }
            if (passwordCard.use_time) {
                binding.cUseTimeLimit.isChecked = true
                binding.cUseTimeLimit.visibility = View.VISIBLE
                binding.tvAdditionalSettings.visibility = View.VISIBLE
            }

            if (passwordCard.description != "")
                binding.etDescription.setText(passwordCard.description)
            else
                binding.tilDescription.visibility = View.GONE

            if (passwordCard.login != "")
                binding.etPasswordLogin.setText(passwordCard.login)
            else
                binding.tilPasswordLogin.visibility = View.GONE

            binding.cgPasswordChipGroup.removeAllViews()
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
            updateAllPhotos(passwordCard)
        }
    }

    private fun updateAllPhotos(passwordCard: PasswordCard) {
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

        for (i in 1..passwordCard.image_count) {
            when (i) {
                1 -> {
                    binding.cvImageHolder1.visibility = View.VISIBLE
                    showImage(mediaStorageDir, 1, binding.attachedImage1)
                }
                2 -> {
                    binding.cvImageHolder2.visibility = View.VISIBLE
                    showImage(mediaStorageDir, 2, binding.attachedImage2)
                }
                3 -> {
                    binding.cvImageHolder3.visibility = View.VISIBLE
                    showImage(mediaStorageDir, 3, binding.attachedImage3)
                }
            }
        }
    }

    private fun showImage(mediaStorageDir: File, imageNum: Int, currentImage: ImageView) {

        val file = File(mediaStorageDir, "${viewModel.currentPassword!!.name}_$imageNum.jpg")

        if (file.exists()) {
            currentImage.setImageURI(null)
            val uri = Uri.fromFile(file)
            currentImage.setImageURI(uri)
            binding.attachedImageText.visibility = View.VISIBLE

            val windowMetrics =
                    WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
            val currentBounds = windowMetrics.bounds
            val widthMax = currentBounds.width()

            val width = (widthMax / 2.4).toInt()
            val height =
                    currentImage.drawable.minimumHeight * width / currentImage.drawable.minimumWidth
            currentImage.layoutParams.height = height
            currentImage.layoutParams.width = width

            currentImage.setOnClickListener {
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

    private fun setListeners() {
        binding.deletePassword.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setTitle(getString(R.string.deletePassword))
            builder.setMessage(getString(R.string.passwordDeleteConfirm))

            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.deletePassword()
                    lifecycleScope.launch(Dispatchers.Main) {
                        Utils.makeToast(applicationContext, getString(R.string.passwordDeleted))
                    }
                }
                val intent = Intent(this, MainNewActivity::class.java)
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

        binding.tvUsernameText.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Name", binding.tvUsernameText.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(applicationContext, getString(R.string.nameCopied))
        }

        binding.tilPasswordLogin.setOnClickListener {
            if (binding.etPasswordLogin.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Login", binding.etPasswordLogin.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.loginCopied))
            }
        }

        binding.etPasswordLogin.setOnClickListener {
            if (binding.etPasswordLogin.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Login", binding.etPasswordLogin.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.loginCopied))
            }
        }

        binding.tilPassword.setOnClickListener {
            if (binding.etPassword.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                        ClipData.newPlainText("Password", binding.etPassword.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.passCopied))
            }
        }

        binding.etPassword.setOnClickListener {
            if (binding.etPassword.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                        ClipData.newPlainText("Password", binding.etPassword.text.toString())
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.passCopied))
            }
        }

        binding.ivBackButton.setOnClickListener {
            if (from != "short") {
                val intent = Intent()
                intent.putExtra("password_id", viewModel.currentPassword!!._id)
                setResult(1, intent)
                finish()
            } else {
                val intent = Intent(this, MainNewActivity::class.java)
                intent.putExtra("password_id", viewModel.currentPassword!!._id)
                startActivity(intent)
                finish()
            }
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, PasswordEditActivity::class.java)
            intent.putExtra("password_id", viewModel.currentPassword!!._id)
            startActivity(intent)
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

            if (!Utils.useAnalyze()) {
                binding.passQualityText.visibility = View.GONE
                binding.ivMainWarningImage.visibility = View.GONE
                binding.passQualityText.visibility = View.GONE
                binding.passQuality.visibility = View.GONE
                binding.ivMinorWarningImage.visibility = View.GONE
                binding.sameParts.visibility = View.GONE
                binding.imSamePartsImage.visibility = View.GONE
            }


        val layoutTransition = binding.mainLinearLayout.layoutTransition
        layoutTransition.setDuration(5000)
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

    }

    override fun onResume() {
        super.onResume()
        if (viewModel.currentPassword != null){
            updateAllPhotos(viewModel.currentPassword!!)
        }
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