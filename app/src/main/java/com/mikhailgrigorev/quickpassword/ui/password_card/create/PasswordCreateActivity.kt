package com.mikhailgrigorev.quickpassword.ui.password_card.create

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.base.MyBaseActivity
import com.mikhailgrigorev.quickpassword.common.manager.PasswordManager
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ActivityPasswordCreateBinding
import com.mikhailgrigorev.quickpassword.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.quickpassword.di.modules.RoomModule
import com.mikhailgrigorev.quickpassword.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModel
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


class PasswordCreateActivity : MyBaseActivity() {

    private var isImage = false
    private var length = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private var imageName: String = ""
    private lateinit var viewModel: PasswordViewModel
    private var imageNum = 0
    private var folderId: Int = 0

    private lateinit var launchSomeActivity: ActivityResultLauncher<Intent>

    private var passwordsCollection: List<PasswordCard>? = null

    private var globalColor: String = ""
    private lateinit var login: String
    private lateinit var binding: ActivityPasswordCreateBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        registerImagePickingIntent()
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        val args: Bundle? = intent.extras
        login = Utils.accountSharedPrefs.getLogin()!!

        val list = mutableListOf<String>()
        val pass: String = args?.get("pass").toString()

        initViewModel()
        setObservers()
        loadFirstConfig(list, pass, args)
        setListeners(list)

    }

    private fun setObservers() {
        viewModel.passwords.observe(this) { passwords ->
            passwordsCollection = passwords
        }
        viewModel.folders.observe(this) { folders ->
            val adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, folders.map {
                        it.name
                    })
            binding.actvFolder.setAdapter(adapter)
            binding.actvFolder.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
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

    private fun loadFirstConfig(list: MutableList<String>, pass: String, args: Bundle?) {
        binding.tePasswordToGenerate.setText(pass)

        if (pass != "") {
            val passwordManager = Utils.password_manager
            var evaluation: String =
                    passwordManager.evaluatePasswordString(binding.tePasswordToGenerate.text.toString())
            if (passwordManager.popularPasswords(binding.tePasswordToGenerate.text.toString())) {
                evaluation = "low"
            }
            if (binding.tePasswordToGenerate.text.toString().length == 4)
                if (passwordManager.popularPin(binding.tePasswordToGenerate.text.toString()))
                    evaluation = "low"
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

        useLetters = args?.get("useLetters") as Boolean
        if(useLetters){
            binding.cLettersToggle.isChecked = true
            list.add(binding.cLettersToggle.text.toString())
        }
        useUC = args.get("useUC") as Boolean
        if(useUC){
            binding.cUpperCaseToggle.isChecked = true
            list.add(binding.cUpperCaseToggle.text.toString())
        }
        useNumbers = args.get("useNumbers") as Boolean
        if (useNumbers) {
            binding.cNumbersToggle.isChecked = true
            list.add(binding.cNumbersToggle.text.toString())
        }
        useSymbols = args.get("useSymbols") as Boolean
        if (useSymbols) {
            binding.cSymToggles.isChecked = true
            list.add(binding.cSymToggles.text.toString())
        }
        length = args.get("length") as Int
        binding.cLengthToggle.text = getString(R.string.length, length)
        binding.sbPasswordLength.progress = length
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(list: MutableList<String>) {

        binding.fabAddFolder.setOnClickListener {
            val customAlertDialogView = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_add_folder, null, false)
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            customAlertDialogView.findViewById<SpectrumPalette>(R.id.spPalette)
                    .setOnColorSelectedListener { it_ ->
                        globalColor = "#${Integer.toHexString(it_).uppercase(Locale.getDefault())}"
                    }
            materialAlertDialogBuilder.setView(customAlertDialogView)
            materialAlertDialogBuilder
                    .setView(customAlertDialogView)
                    .setTitle(getString(R.string.folder_creation))
                    .setMessage(getString(R.string.create_folder_config))
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

        binding.getInfo.setOnClickListener {
            if (binding.infoCard.visibility == View.GONE) {
                binding.infoCard.visibility = View.VISIBLE
            } else {
                binding.infoCard.visibility = View.GONE
            }
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
                binding.cLengthToggle.text = getString(R.string.length, i)
                length = i
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        for (index in 0 until binding.cgPasswordSettings.childCount) {
            val chip: Chip = binding.cgPasswordSettings.getChildAt(index) as Chip
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
                    var evaluation: String =
                            myPasswordManager.evaluatePasswordString(binding.tePasswordToGenerate.text.toString())
                    if (myPasswordManager.popularPasswords(binding.tePasswordToGenerate.text.toString())) {
                        evaluation = "low"
                    }
                    if (binding.tePasswordToGenerate.text.toString().length == 4)
                        if (myPasswordManager.popularPin(binding.tePasswordToGenerate.text.toString()))
                            evaluation = "low"

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
            binding.tePasswordToGenerate.clearFocus()
            val deg = 0f
            binding.generatePassword.animate().rotation(deg).interpolator =
                    AccelerateDecelerateInterpolator()
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

                var evaluation: String =
                        myPasswordManager.evaluatePasswordString(binding.tePasswordToGenerate.text.toString())
                if (myPasswordManager.popularPasswords(binding.tePasswordToGenerate.text.toString())) {
                    evaluation = "low"
                }
                if (binding.tePasswordToGenerate.text.toString().length == 4)
                    if (myPasswordManager.popularPin(binding.tePasswordToGenerate.text.toString()))
                        evaluation = "low"
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

        binding.generatePassword.setOnTouchListener { v, event ->
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

        binding.back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("login", login)
            setResult(1, intent)
            finish()
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

                    val newPassword = PasswordCard(
                            name = binding.newNameField.text.toString(),
                            password = password!!,
                            use_2fa = binding.cUse2fa.isChecked,
                            use_time = binding.cNumberOfEncrypted.isChecked,
                            is_card_pin = binding.cIsPin.isChecked,
                            time = Date().toString(),
                            folder = folderId,
                            image_count = imageNum,
                            description = binding.noteField.text.toString(),
                            tags = binding.etKeywords.text.toString(),
                            login = binding.emailField.text.toString(),
                            encrypted = binding.cryptToggle.isChecked,
                    )

                    viewModel.currentPassword = newPassword

                    if (passwordsCollection != null) {
                        val analyzeResults =
                                Utils.analyzeDataBase(
                                        newPassword,
                                        passwordsCollection!!
                                )
                        if (analyzeResults.second.joinToString() != "")
                            newPassword.same_with = analyzeResults.second.joinToString()
                        else
                            viewModel.currentPassword!!.same_with = ""
                    }

                    val quality = Utils.evaluatePassword(
                            newPassword,
                            binding.tePasswordToGenerate.text.toString()
                    )

                    newPassword.quality = quality

                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.addPassword(newPassword)
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
                        if (imageName != "") {
                            for (i in 0..imageNum) {
                                val from = File(mediaStorageDir, "${imageName}_$i.jpg")
                                val to = File(mediaStorageDir, "${binding.newNameField.text}_$i.jpg")
                                if (from.exists())
                                    from.renameTo(to)
                            }
                        }
                    }
                    finish()
                }
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
        startImagePick(intent)
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


    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent()
                intent.putExtra("login", login)
                setResult(1, intent)
                finish()
            }
        }
        return false
    }

    private fun registerImagePickingIntent() {
        launchSomeActivity =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        imageNum += 1
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
                        if (resultURI != null) {
                            copyFile(File(resultURI), file)
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