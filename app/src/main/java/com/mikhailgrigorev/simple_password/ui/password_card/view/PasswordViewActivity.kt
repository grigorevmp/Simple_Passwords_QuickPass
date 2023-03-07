package com.mikhailgrigorev.simple_password.ui.password_card.view

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
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.base.MyBaseActivity
import com.mikhailgrigorev.simple_password.common.utils.PasswordQuality
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.dbo.CustomField
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard
import com.mikhailgrigorev.simple_password.databinding.ActivityPasswordViewBinding
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.simple_password.ui.auth.login.LoginActivity
import com.mikhailgrigorev.simple_password.ui.main_activity.MainActivity
import com.mikhailgrigorev.simple_password.ui.password_card.PasswordViewModel
import com.mikhailgrigorev.simple_password.ui.password_card.edit.PasswordEditActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class PasswordViewActivity : MyBaseActivity() {

    private lateinit var viewModel: PasswordViewModel
    private lateinit var from: String
    private lateinit var binding: ActivityPasswordViewBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        initViewModel()

        checkShortcut()

        setUpAutoCopy()
        setListeners()
    }

    override fun onResume() {
        super.onResume()

        val args: Bundle? = intent.extras
        val passwordId = args?.getInt("password_id")

        passwordId?.also { loadPassword(it) }
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                finish()
            }
        }
        return false
    }



    private fun setUpAutoCopy() {
        val args: Bundle? = intent.extras
        args?.also {
            from = it.getString("openedFrom", "def")
            if (from == "shortcut") {
                intent.putExtra("openedFrom", it.getString("openedFrom") as String)
                intent.putExtra("password_id", it.getString("password_id") as String)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkShortcut() {
        if (Utils.toggleManager.autoCopyToggle.isEnabled() && binding.etPassword.text.toString() != "") {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Password", binding.etPassword.text.toString())
            clipboard.setPrimaryClip(clip)
            Utils.makeToast(applicationContext, getString(R.string.passCopied))
        }
    }

    private fun loadPassword(passwordId: Int) {
        viewModel.getPasswordById(passwordId).observe(this) { passwordCard ->
            if (passwordCard == null) return@observe

            viewModel.currentPassword = passwordCard

            setUpScreenContent(passwordCard)
        }
    }

    private fun setUpScreenContent(passwordCard: PasswordCard) {
        passwordCard.also {
            setUpCustomTexts(it.custom_field)

            setUpSameParts(it.same_with)
            setUpFavorite(it.favorite)
            setUpLogin(it.login)
            setUpUsername(it.name)
            setUpTags(it.tags)
            setUpDescription(it.description)
            setUpPinCodeIcon(it.is_card_pin)
            setUpUseTwoFA(it.use_2fa)
            setUpCreationDate(it.time)
            setUpUseTime(it.use_time)
            setUpFolder(it.folder)

            setUpPasswordView(it.password, it.encrypted, it.quality)

            updateAllPhotos(it)
        }
    }

    private fun setUpPasswordView(databasePassword: String, isEncrypted: Boolean, quality: Int) {

        var password = databasePassword

        binding.cUseEncryption.isChecked = false
        binding.cUseEncryption.visibility = View.GONE
        binding.tvAdditionalSettings.visibility = View.GONE

        if (isEncrypted) {
            binding.cUseEncryption.isChecked = true
            binding.cUseEncryption.visibility = View.VISIBLE
            binding.tvAdditionalSettings.visibility = View.VISIBLE
            password = Utils.password_manager.decrypt(password).toString()
        }

        binding.etPassword.setText(password)

        when (quality) {
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

        if ((password.length == 4) and (quality == PasswordQuality.HIGH.value)) {
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
    }

    private fun setUpTags(tags: String) {
        binding.cgPasswordChipGroup.removeAllViews()

        binding.kwInfo.visibility = View.VISIBLE

        if (tags != "") {
            tags.trim()
                    .split("\\s".toRegex()).forEach { item ->
                    val chip = Chip(binding.cgPasswordChipGroup.context)
                    chip.text = item
                    chip.isClickable = false
                    binding.cgPasswordChipGroup.addView(chip)
            }
        } else {
            binding.kwInfo.visibility = View.GONE
        }
    }

    private fun setUpLogin(login: String) {
        binding.tilPasswordLogin.visibility = View.VISIBLE

        if (login != "")
            binding.etPasswordLogin.setText(login)
        else
            binding.tilPasswordLogin.visibility = View.GONE
    }

    private fun setUpSameParts(sameParts: String) {
        if (sameParts != "") {
            binding.imSamePartsImage.visibility = View.VISIBLE
            binding.sameParts.visibility = View.VISIBLE
            binding.sameParts.text = this.getString(R.string.same_parts, sameParts)
        }
    }

    private fun setUpFavorite(favorite: Boolean) {
        if (favorite) {
            binding.favButton.visibility = View.GONE
            binding.favButton2.visibility = View.VISIBLE
        }
    }

    private fun setUpFolder(folder: Int?) {
        binding.cFolderName.visibility = View.GONE
        binding.tvAdditionalSettings.visibility = View.GONE

        if (folder != null) {
            if (folder > 0) {
                viewModel.getFolder(folder).observe(this) {
                    if(it != null) {
                        binding.cFolderName.visibility = View.VISIBLE
                        binding.cFolderName.text = it.name
                        binding.tvAdditionalSettings.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setUpUsername(username: String) {
        binding.tvUsernameText.text = username
    }

    private fun setUpCreationDate(time: String) {
        binding.tvPasswordCreationDate.text = getString(
                R.string.time_lim,
                Utils.returnReadableDate(time)
        )
    }

    private fun setUpDescription(description: String) {
        binding.tilDescription.visibility = View.VISIBLE

        if (description != "")
            binding.etDescription.setText(description)
        else
            binding.tilDescription.visibility = View.GONE
    }

    private fun setUpPinCodeIcon(useTwoFA: Boolean) {
        binding.cIsPin.visibility = View.GONE

        if (useTwoFA) {
            binding.cIsPin.isChecked = true
            binding.cIsPin.visibility = View.VISIBLE
            binding.tvAdditionalSettings.visibility = View.VISIBLE
        }
    }

    private fun setUpUseTwoFA(useTwoFA: Boolean) {
        binding.cUse2fa.visibility = View.GONE

        if (useTwoFA) {
            binding.cUse2fa.isChecked = true
            binding.cUse2fa.visibility = View.VISIBLE
            binding.tvAdditionalSettings.visibility = View.VISIBLE
        }
    }

    private fun setUpUseTime(useTime: Boolean) {
        binding.cUseTimeLimit.visibility = View.GONE

        if (useTime) {
            binding.cUseTimeLimit.isChecked = true
            binding.cUseTimeLimit.visibility = View.VISIBLE
            binding.tvAdditionalSettings.visibility = View.VISIBLE
        }
    }

    private fun setUpCustomTexts(customFields: List<CustomField>) {
        binding.customFieldsText.visibility = View.VISIBLE
        binding.tvCustomFields.visibility = View.VISIBLE
        binding.customFieldsText.text = ""

        var customText = ""

        for (customField in customFields) {
            customText += "${customField.key} - ${customField.value}\n"
        }

        if (customText == "") {
            binding.customFieldsText.visibility = View.GONE
            binding.tvCustomFields.visibility = View.GONE
        } else {
            binding.customFieldsText.text = customText
        }
    }

    private fun updateAllPhotos(passwordCard: PasswordCard) {
        val mediaStorageDir = File(
                applicationContext.getExternalFilesDir("SimplePasswordsPhotos")!!.absolutePath
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

        viewModel.currentPassword?.also {
            val file = File(mediaStorageDir, "${it.name}_$imageNum.jpg")

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
                val intent = Intent(this, MainActivity::class.java)
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
                val intent = Intent(this, MainActivity::class.java)
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

        if (!Utils.toggleManager.analyzeToggle.isEnabled()) {
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

    private fun initViewModel() {
        viewModel = this.injectViewModel(viewModelFactory)
    }
}