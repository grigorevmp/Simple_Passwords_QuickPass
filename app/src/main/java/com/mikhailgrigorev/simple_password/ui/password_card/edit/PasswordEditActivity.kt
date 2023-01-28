package com.mikhailgrigorev.simple_password.ui.password_card.edit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.base.MyBaseActivity
import com.mikhailgrigorev.simple_password.common.manager.PasswordManager
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.dbo.CustomField
import com.mikhailgrigorev.simple_password.data.dbo.FolderCard
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard
import com.mikhailgrigorev.simple_password.databinding.ActivityPasswordEditBinding
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.simple_password.ui.password_card.PasswordViewModel
import com.thebluealliance.spectrum.SpectrumPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*
import javax.inject.Inject


class PasswordEditActivity : MyBaseActivity() {

    private var isImage = false
    private var length = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private var imageName: String = ""
    private var imageNum: Int = 0
    private lateinit var viewModel: PasswordViewModel
    private var folderId: Int = -1

    private var globalColor: String = ""
    private var passwordsCollection: List<PasswordCard>? = null
    private lateinit var launchSomeActivity: ActivityResultLauncher<Intent>

    private lateinit var login: String
    private lateinit var binding: ActivityPasswordEditBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private fun setObservers() {
        viewModel.passwords.observe(this) { passwords ->
            passwordsCollection = passwords
        }
        viewModel.folders.observe(this) { folders ->
            viewModel.currentPassword?.also {
                it.folder?.let { currentPasswordFolderId ->
                    viewModel.getFolder(currentPasswordFolderId)
                }!!.observe(this) { folderCard ->
                    val adapter =
                            ArrayAdapter(
                                    this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    folders.map { folder ->
                                        folder.name
                                    })
                    binding.actvFolder.setAdapter(adapter)
                    if (folderCard != null) {
                        binding.actvFolder.setText(folderCard.name, false)
                        folderId = folders.indexOf(folderCard) + 1
                    }
                }
                binding.actvFolder.onItemClickListener =
                        OnItemClickListener { _, _, position, _ ->
                            folderId = folders[position]._id
                        }
                binding.actvFolder.setDropDownBackgroundDrawable(
                        ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.filter_spinner_dropdown_bg,
                                null
                        )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        registerImagePickingIntent()
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        val args: Bundle? = intent.extras
        login = args?.getString("login")!!
        val passwordId = args.getInt("password_id")

        initViewModel()
        loadPassword(passwordId)
        setListeners()
    }

    private fun loadPassword(passwordId: Int) {
        viewModel.getPasswordById(passwordId).observe(this) { passwordCard ->
            viewModel.currentPassword = passwordCard
            setObservers()
            binding.tvUsernameText.text = passwordCard.name
            binding.newNameField.setText(passwordCard.name)
            var dbPassword = passwordCard.password
            if (passwordCard.encrypted) {
                binding.cryptToggle.isChecked = true
                dbPassword = Utils.password_manager.decrypt(dbPassword).toString()
            }
            binding.tePasswordToGenerate.setText(dbPassword)
            if (dbPassword != "") {
                length = dbPassword.length
                binding.sbPasswordLength.progress = length
                binding.cLengthToggle.text = getString(R.string.length, length)
                val evaluation: String = Utils.password_manager.evaluatePasswordString(
                        binding.tePasswordToGenerate.text.toString()
                )
                binding.passQuality.text = evaluation
                when (evaluation) {
                    "low" -> binding.passQuality.text = getString(R.string.low)
                    "high" -> binding.passQuality.text = getString(R.string.high)
                    else -> binding.passQuality.text = getString(R.string.medium)
                }
                when (evaluation) {
                    "low" -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.red_quality
                            )
                    )
                    "high" -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.green_quality
                            )
                    )
                    else -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.yellow_quality
                            )
                    )
                }
                binding.cLettersToggle.isChecked = Utils.password_manager.isLetters(
                        binding.tePasswordToGenerate.text.toString()
                )
                binding.cUpperCaseToggle.isChecked = Utils.password_manager.isUpperCase(
                        binding.tePasswordToGenerate.text.toString()
                )
                binding.cNumbersToggle.isChecked = Utils.password_manager.isNumbers(
                        binding.tePasswordToGenerate.text.toString()
                )
                binding.cSymToggles.isChecked = Utils.password_manager.isSymbols(
                        binding.tePasswordToGenerate.text.toString()
                )
            }

            binding.cUse2fa.isChecked = passwordCard.use_2fa

            binding.cIsPin.isChecked = passwordCard.is_card_pin

            binding.cNumberOfEncrypted.isChecked = passwordCard.use_time

            binding.noteField.setText(passwordCard.description)
            binding.keyWordsField.setText(passwordCard.tags)

            if (passwordCard.login != "") {
                binding.email.visibility = View.VISIBLE
                binding.emailSwitch.isChecked = true
                binding.emailField.setText(passwordCard.login)
            }

            if (passwordCard.custom_field.isNotEmpty()) {
                var customText = ""

                for (customField in passwordCard.custom_field) {
                    customText += "${customField.key},${customField.value} "
                }

                binding.customFieldsTextField.setText(customText)
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

            imageNum = passwordCard.image_count

            if (imageNum > 0){
                binding.clearImage.visibility = View.VISIBLE
            }

            binding.clearImage.setOnClickListener {
                val file =
                        File(mediaStorageDir, "${viewModel.currentPassword!!.name}_$imageNum.jpg")
                when (imageNum) {
                    1 -> {
                        binding.cvImageHolder1.visibility = View.GONE
                        binding.attachedImage1.setImageURI(null)
                    }
                    2 -> {
                        binding.cvImageHolder2.visibility = View.GONE
                        binding.attachedImage2.setImageURI(null)
                    }
                    3 -> {
                        binding.cvImageHolder3.visibility = View.GONE
                        binding.attachedImage3.setImageURI(null)
                    }
                }
                file.delete()
                imageNum -= 1
                if (imageNum == 0){
                    binding.clearImage.visibility = View.GONE
                }
            }

        }
    }

    private fun showImage(mediaStorageDir: File, imageNum: Int, currentImage: ImageView) {

        val file = File(mediaStorageDir, "${viewModel.currentPassword!!.name}_$imageNum.jpg")

        if (file.exists()) {
            val uri = Uri.fromFile(file)
            currentImage.setImageURI(uri)

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


    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        val list = mutableListOf<String>()

        binding.fabAddFolder.setOnClickListener {
            val customAlertDialogView = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_add_folder, null, false)
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            customAlertDialogView.findViewById<SpectrumPalette>(R.id.spPalette)
                    .setOnColorSelectedListener { color ->
                        globalColor = "#${Integer.toHexString(color).uppercase(Locale.getDefault())}"
                    }
            materialAlertDialogBuilder.setView(customAlertDialogView)
            materialAlertDialogBuilder
                    .setView(customAlertDialogView)
                    .setTitle("Folder creation")
                    .setMessage("Current configuration details")
                    .setPositiveButton("Ok") { dialog, _ ->
                        val name =
                                customAlertDialogView.findViewById<TextInputEditText>(
                                        R.id.etFolderName
                                ).text.toString()
                        val description =
                                customAlertDialogView.findViewById<TextInputEditText>(
                                        R.id.etFolderDesc
                                ).text.toString()
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.insertCard(
                                    FolderCard(
                                            name = name,
                                            description = description,
                                            colorTag = globalColor
                                    )
                            )
                        }

                        dialog.dismiss()

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()

        }

        if (binding.cLettersToggle.isChecked) {
            useLetters = true
            list.add(binding.cLettersToggle.text.toString())
        }
        if (binding.cUpperCaseToggle.isChecked) {
            list.add(binding.cUpperCaseToggle.text.toString())
            useUC = true
        }
        if (binding.cNumbersToggle.isChecked) {
            list.add(binding.cNumbersToggle.text.toString())
            useNumbers = true
        }
        if (binding.cSymToggles.isChecked) {
            list.add(binding.cSymToggles.text.toString())
            useSymbols = true
        }

        binding.cLengthToggle.setOnClickListener {
            if (binding.sbPasswordLength.visibility == View.GONE) {
                binding.sbPasswordLength.visibility = View.VISIBLE
            } else {
                binding.sbPasswordLength.visibility = View.GONE
            }
        }

        binding.sbPasswordLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                length = i
                binding.cLengthToggle.text = getString(R.string.length, length)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        // Loop through the chips
        for (index in 0 until binding.cgPasswordSettings.childCount) {
            val chip: Chip = binding.cgPasswordSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                val deg = binding.generatePassword.rotation + 30f
                binding.generatePassword.animate().rotation(deg).interpolator =
                        AccelerateDecelerateInterpolator()
                if (isChecked) {
                    if (view.id == R.id.cLettersToggle)
                        useLetters = true
                    if (view.id == R.id.cSymToggles)
                        useSymbols = true
                    if (view.id == R.id.cNumbersToggle)
                        useNumbers = true
                    if (view.id == R.id.cUpperCaseToggle)
                        useUC = true
                    list.add(view.text.toString())
                } else {
                    if (view.id == R.id.cLettersToggle)
                        useLetters = false
                    if (view.id == R.id.cSymToggles)
                        useSymbols = false
                    if (view.id == R.id.cNumbersToggle)
                        useNumbers = false
                    if (view.id == R.id.cUpperCaseToggle)
                        useUC = false
                    list.remove(view.text.toString())
                }
            }
        }

        binding.tePasswordToGenerate.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                if (binding.tePasswordToGenerate.hasFocus()) {
                    length = s.toString().length
                    binding.cLengthToggle.text = getString(R.string.length, length)
                    binding.sbPasswordLength.progress = length
                    val deg = binding.generatePassword.rotation + 10f
                    binding.generatePassword.animate().rotation(deg).interpolator =
                            AccelerateDecelerateInterpolator()
                    val myPasswordManager = PasswordManager()
                    binding.cLettersToggle.isChecked =
                            myPasswordManager.isLetters(binding.tePasswordToGenerate.text.toString())
                    binding.cUpperCaseToggle.isChecked =
                            myPasswordManager.isUpperCase(binding.tePasswordToGenerate.text.toString())
                    binding.cNumbersToggle.isChecked =
                            myPasswordManager.isNumbers(binding.tePasswordToGenerate.text.toString())
                    binding.cSymToggles.isChecked =
                            myPasswordManager.isSymbols(binding.tePasswordToGenerate.text.toString())
                    val evaluation: String =
                            myPasswordManager.evaluatePasswordString(binding.tePasswordToGenerate.text.toString())
                    binding.passQuality.text = evaluation
                    when (evaluation) {
                        "low" -> binding.passQuality.text = getString(R.string.low)
                        "high" -> binding.passQuality.text = getString(R.string.high)
                        else -> binding.passQuality.text = getString(R.string.medium)
                    }
                    when (evaluation) {
                        "low" -> binding.passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.red_quality
                                )
                        )
                        "high" -> binding.passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.green_quality
                                )
                        )
                        else -> binding.passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.yellow_quality
                                )
                        )
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        binding.generatePassword.setOnClickListener {
            val deg = 0f
            binding.generatePassword.animate().rotation(deg).interpolator =
                    AccelerateDecelerateInterpolator()
            binding.tePasswordToGenerate.clearFocus()
            val myPasswordManager = PasswordManager()
            //Create a password with letters, uppercase letters, numbers but not special chars with 17 chars
            if (list.size == 0 || (list.size == 1 && binding.cLengthToggle.isChecked) || (list.size == 1 && list[0].contains(
                        getString(
                                R.string.length
                        )
                ))
            ) {
                binding.tilPasswordToGenerate.error = getString(R.string.noRules)
            } else {
                binding.tilPasswordToGenerate.error = null
                val newPassword: String =
                        myPasswordManager.generatePassword(
                                useLetters,
                                useUC,
                                useNumbers,
                                useSymbols,
                                length
                        )
                binding.tePasswordToGenerate.setText(newPassword)

                val evaluation: String =
                        myPasswordManager.evaluatePasswordString(binding.tePasswordToGenerate.text.toString())
                when (evaluation) {
                    "low" -> binding.passQuality.text = getString(R.string.low)
                    "high" -> binding.passQuality.text = getString(R.string.high)
                    else -> binding.passQuality.text = getString(R.string.medium)
                }
                when (evaluation) {
                    "low" -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.red_quality
                            )
                    )
                    "high" -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.green_quality
                            )
                    )
                    else -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.yellow_quality
                            )
                    )
                }
            }
        }
        binding.generatePassword.setOnTouchListener { v, event ->
            v.performClick()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.cvPasswordGenerateButton.elevation = 50F
                    binding.generatePassword.background = ContextCompat.getDrawable(
                            this,
                            R.color.grey
                    )
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    binding.generatePassword.background = ContextCompat.getDrawable(
                            this,
                            R.color.white
                    )
                    binding.cvPasswordGenerateButton.elevation = 10F
                    v.invalidate()
                }
            }
            false
        }
        binding.tilPasswordToGenerate.setOnClickListener {
            if (binding.tePasswordToGenerate.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                        "Password",
                        binding.tePasswordToGenerate.text.toString()
                )
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.passCopied))
            }
        }
        binding.tePasswordToGenerate.setOnClickListener {
            if (binding.tePasswordToGenerate.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                        "Password",
                        binding.tePasswordToGenerate.text.toString()
                )
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.passCopied))
            }
        }
        binding.emailSwitch.setOnClickListener {
            if (binding.emailSwitch.isChecked)
                binding.email.visibility = View.VISIBLE
            else
                binding.email.visibility = View.GONE

        }
        binding.savePass.setOnClickListener {
            val login2 = binding.newNameField.text

            if (login2 != null) {
                if (login2.isEmpty() || login2.length < 2) {
                    binding.newName.error = getString(R.string.errNumOfText)
                } else if (binding.tePasswordToGenerate.text.toString() == "" || binding.tePasswordToGenerate.text.toString().length < 4) {
                    binding.tilPasswordToGenerate.error = getString(R.string.errPass)
                } else {
                    val password = if (binding.cryptToggle.isChecked)
                        Utils.password_manager.encrypt(binding.tePasswordToGenerate.text.toString())
                    else
                        binding.tePasswordToGenerate.text.toString()

                    viewModel.currentPassword?.also {
                        it.name = binding.newNameField.text.toString()
                        it.password = password!!
                        it.use_2fa = binding.cUse2fa.isChecked
                        it.use_time = binding.cNumberOfEncrypted.isChecked
                        it.is_card_pin = binding.cIsPin.isChecked
                        it.time = Date().toString()
                        it.folder = folderId
                        it.image_count = imageNum
                        it.description = binding.noteField.text.toString()
                        it.tags = binding.keyWordsField.text.toString()
                        it.login = binding.emailField.text.toString()
                        it.encrypted = binding.cryptToggle.isChecked

                        var customFields = emptyList<CustomField>()

                        try {
                            customFields = binding.customFieldsTextField.text
                                    ?.trim()
                                    ?.split(" ")
                                    ?.map { pair ->
                                        val (key, value) = pair.split(",")
                                        CustomField(
                                                key = key,
                                                value = value
                                        )
                                    } ?: emptyList()
                        } catch (e: Exception) {
                            Log.d("Crash", "Password creation failed in custom fields")
                        }

                        viewModel.currentPassword!!.custom_field = customFields

                        if (passwordsCollection != null) {
                            val analyzeResults =
                                    Utils.analyzeDataBase(
                                            viewModel.currentPassword!!,
                                            passwordsCollection!!
                                    )
                            if(analyzeResults.second.joinToString() != "")
                                viewModel.currentPassword!!.same_with = analyzeResults.second.joinToString()
                            else
                                viewModel.currentPassword!!.same_with = ""
                        }

                        viewModel.currentPassword!!.quality = Utils.evaluatePassword(
                                viewModel.currentPassword!!,
                                binding.tePasswordToGenerate.text.toString()
                        )

                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.updatePassword(viewModel.currentPassword!!)
                        }
                    }

                    val intent = Intent()
                    intent.putExtra("login", login)
                    setResult(1, intent)

                    val mediaStorageDir = File(
                            applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
                    )
                    if (!mediaStorageDir.exists()) {
                        mediaStorageDir.mkdirs()
                        Utils.makeToast(applicationContext, "Directory Created")
                    }

                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Utils.makeToast(applicationContext, "Failed to create directory")
                        }
                    }

                    if (mediaStorageDir.exists()) {
                        for (i in 0..imageNum) {
                            val from = File(mediaStorageDir, "${imageName}_$imageNum.jpg")
                            val to = File(mediaStorageDir, "${binding.newNameField.text}_$imageNum.jpg")
                            if (from.exists())
                                from.renameTo(to)
                        }
                    }

                    finish()
                }
            }
        }
        binding.back.setOnClickListener {
            if (viewModel.currentPassword != null) {
                finish()
            }
        }
        binding.bUploadImage.setOnClickListener {
            checkPermissionForImage()
        }
    }


    private val permissionCodeRead = 1001
    private val permissionCodeWrite = 1002

    private fun checkPermissionForImage() {
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ) {
            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(permission, permissionCodeRead)
            requestPermissions(permissionCoarse, permissionCodeWrite)
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startImagePick(intent) // GIVE AN INTEGER VALUE FOR imagePickCode LIKE 1000
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (viewModel.currentPassword != null) {
                    val intent = Intent()
                    intent.putExtra("login", login)
                    intent.putExtra("passName", viewModel.currentPassword!!.name)
                    setResult(1, intent)
                    finish()
                }
            }
        }
        return false
    }

    @Throws(IOException::class)
    private fun copyFile(sourceFile: File, destFile: File) {
        if (!sourceFile.exists()) {
            return
        }
        val source: FileChannel? = FileInputStream(sourceFile).channel
        val destination: FileChannel? = FileOutputStream(destFile).channel
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        source?.close()
        destination?.close()
    }

    private fun getImagePath(context: Context, uri: Uri): String? {
        var filePath = ""
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return context.getExternalFilesDir(null).toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf<String?>(
                            split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                        context,
                        uri,
                        null,
                        null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }

        } catch (e: java.lang.Exception) {
            filePath = ""
        }
        return filePath
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
                column
        )
        try {
            cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun registerImagePickingIntent() {
        launchSomeActivity =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        imageNum += 1
                        binding.clearImage.visibility = View.VISIBLE
                        val newImage: ImageView = when (imageNum) {
                            1 -> binding.attachedImage1
                            2 -> binding.attachedImage2
                            3 -> binding.attachedImage3
                            else -> {
                                binding.attachedImage1
                            }
                        }
                        newImage.setImageURI(data?.data)
                        when (imageNum) {
                            1 -> {
                                binding.cvImageHolder1.visibility = View.VISIBLE
                            }
                            2 -> {
                                binding.cvImageHolder2.visibility = View.VISIBLE
                            }
                            3 -> {
                                binding.cvImageHolder3.visibility = View.VISIBLE
                            }
                        }

                        val windowMetrics =
                                WindowMetricsCalculator.getOrCreate()
                                        .computeCurrentWindowMetrics(this)
                        val currentBounds = windowMetrics.bounds
                        val widthMax = currentBounds.width()

                        val width = (widthMax / 1.3).toInt()
                        val height =
                                newImage.drawable.minimumHeight * width / newImage.drawable.minimumWidth
                        newImage.layoutParams.height = height
                        newImage.layoutParams.width = width
                        newImage.layoutParams.height = height
                        newImage.layoutParams.width = width
                        if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    PackageManager.PERMISSION_GRANTED
                            )
                        }

                        val selectedImageURI: Uri = data?.data!!

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

                        imageName = if (binding.newNameField.text.toString() == "") {
                            "000000001tmp000000001"
                        } else
                            binding.newNameField.text.toString()
                        val file = File(mediaStorageDir, "${imageName}_$imageNum.jpg")

                        val resultURI = getImagePath(this, selectedImageURI)

                        resultURI?.also {
                            copyFile(File(it), file)
                        }

                        isImage = true
                    }
                }
    }

    private fun startImagePick(intent: Intent) {
        launchSomeActivity.launch(intent)
    }

    private fun initViewModel() {
        viewModel = this.injectViewModel(viewModelFactory)
    }
}