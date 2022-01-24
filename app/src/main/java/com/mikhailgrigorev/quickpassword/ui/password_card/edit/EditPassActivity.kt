package com.mikhailgrigorev.quickpassword.ui.password_card.edit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityEditPassBinding
import com.mikhailgrigorev.quickpassword.ui.account.view.AccountActivity
import com.mikhailgrigorev.quickpassword.ui.auth.login.LoginAfterSplashActivity
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModel
import com.mikhailgrigorev.quickpassword.ui.password_card.PasswordViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*


class EditPassActivity : AppCompatActivity() {

    private var isImage = false
    private var length = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private var imageName: String = ""
    private lateinit var viewModel: PasswordViewModel

    private lateinit var login: String
    private lateinit var binding: ActivityEditPassBinding

    private fun setQuitTimer() {
        var condition = true
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
                PasswordViewModelFactory(application)
        )[PasswordViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // App Quit Timer
        setQuitTimer()
        initViewModel()

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        val passwordId = args?.get("password_id").toString().toInt()

        binding.accountAvatar.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("activity", "menu")
            startActivityForResult(intent, 1)
        }

        loadPassword(passwordId)
        setListeners()
    }

    private fun loadPassword(passwordId: Int) {
        viewModel.getPasswordById(passwordId).observe(this) { passwordCard ->
            viewModel.currentPassword = passwordCard
            binding.helloTextId.text = passwordCard.name
            binding.newNameField.setText(passwordCard.name)
            var dbPassword = passwordCard.password
            if (passwordCard.encrypted) {
                binding.cryptToggle.isChecked = true
                dbPassword = Utils.password_manager.decrypt(dbPassword).toString()
            }
            binding.genPasswordIdField.setText(dbPassword)
            if (dbPassword != "") {
                length = dbPassword.length
                binding.seekBar.progress = length
                binding.lengthToggle.text = getString(R.string.length) + ": " + length
                val evaluation: String = Utils.password_manager.evaluatePasswordString(
                        binding.genPasswordIdField.text.toString()
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
                                    R.color.negative
                            )
                    )
                    "high" -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.positive
                            )
                    )
                    else -> binding.passQuality.setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.fixable
                            )
                    )
                }
                binding.lettersToggle.isChecked = Utils.password_manager.isLetters(
                        binding.genPasswordIdField.text.toString()
                )
                binding.upperCaseToggle.isChecked = Utils.password_manager.isUpperCase(
                        binding.genPasswordIdField.text.toString()
                )
                binding.numbersToggle.isChecked = Utils.password_manager.isNumbers(
                        binding.genPasswordIdField.text.toString()
                )
                binding.symToggles.isChecked = Utils.password_manager.isSymbols(
                        binding.genPasswordIdField.text.toString()
                )
            }

            binding.authToggle.isChecked = passwordCard.use_2fa

            binding.timeLimit.isChecked = passwordCard.use_time

            binding.noteField.setText(passwordCard.description)
            binding.keyWordsField.setText(passwordCard.tags)

            if (passwordCard.login != "") {
                binding.email.visibility = View.VISIBLE
                binding.emailSwitch.isChecked = true
                binding.emailField.setText(passwordCard.login)
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

            val file = File(mediaStorageDir, "${passwordCard.name}.jpg")
            if (file.exists()) {
                imageName = passwordCard.name
                val uri = Uri.fromFile(file)
                binding.attachedImage.setImageURI(uri)
                binding.clearImage.visibility = View.VISIBLE

                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val widthMax: Int = size.x
                val width = (widthMax / 1.3).toInt()
                val height =
                        binding.attachedImage.drawable.minimumHeight * width / binding.attachedImage.drawable.minimumWidth
                binding.attachedImage.layoutParams.height = height
                binding.attachedImage.layoutParams.width = width
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

            binding.clearImage.setOnClickListener {
                file.delete()
                binding.attachedImage.setImageURI(null)
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        val list = mutableListOf<String>()

        if (binding.lettersToggle.isChecked) {
            useLetters = true
            list.add(binding.lettersToggle.text.toString())
        }
        if (binding.upperCaseToggle.isChecked) {
            list.add(binding.upperCaseToggle.text.toString())
            useUC = true
        }
        if (binding.numbersToggle.isChecked) {
            list.add(binding.numbersToggle.text.toString())
            useNumbers = true
        }
        if (binding.symToggles.isChecked) {
            list.add(binding.symToggles.text.toString())
            useSymbols = true
        }

        binding.lengthToggle.setOnClickListener {
            if (binding.seekBar.visibility == View.GONE) {
                binding.seekBar.visibility = View.VISIBLE
            } else {
                binding.seekBar.visibility = View.GONE
            }
        }
        // Set a SeekBar change listener
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                length = i
                binding.lengthToggle.text = getString(R.string.length) + ": " + length
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        // Loop through the chips
        for (index in 0 until binding.passSettings.childCount) {
            val chip: Chip = binding.passSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                val deg = binding.generatePassword.rotation + 30f
                binding.generatePassword.animate().rotation(deg).interpolator =
                        AccelerateDecelerateInterpolator()
                if (isChecked) {
                    if (view.id == R.id.lettersToggle)
                        useLetters = true
                    if (view.id == R.id.symToggles)
                        useSymbols = true
                    if (view.id == R.id.numbersToggle)
                        useNumbers = true
                    if (view.id == R.id.upperCaseToggle)
                        useUC = true
                    list.add(view.text.toString())
                } else {
                    if (view.id == R.id.lettersToggle)
                        useLetters = false
                    if (view.id == R.id.symToggles)
                        useSymbols = false
                    if (view.id == R.id.numbersToggle)
                        useNumbers = false
                    if (view.id == R.id.upperCaseToggle)
                        useUC = false
                    list.remove(view.text.toString())
                }
            }
        }

        binding.genPasswordIdField.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                if (binding.genPasswordIdField.hasFocus()) {
                    length = s.toString().length
                    binding.lengthToggle.text = getString(R.string.length) + ": " + length
                    binding.seekBar.progress = length
                    val deg = binding.generatePassword.rotation + 10f
                    binding.generatePassword.animate().rotation(deg).interpolator =
                            AccelerateDecelerateInterpolator()
                    val myPasswordManager = PasswordManager()
                    binding.lettersToggle.isChecked =
                            myPasswordManager.isLetters(binding.genPasswordIdField.text.toString())
                    binding.upperCaseToggle.isChecked =
                            myPasswordManager.isUpperCase(binding.genPasswordIdField.text.toString())
                    binding.numbersToggle.isChecked =
                            myPasswordManager.isNumbers(binding.genPasswordIdField.text.toString())
                    binding.symToggles.isChecked =
                            myPasswordManager.isSymbols(binding.genPasswordIdField.text.toString())
                    val evaluation: String =
                            myPasswordManager.evaluatePasswordString(binding.genPasswordIdField.text.toString())
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
            binding.genPasswordIdField.clearFocus()
            val myPasswordManager = PasswordManager()
            //Create a password with letters, uppercase letters, numbers but not special chars with 17 chars
            if (list.size == 0 || (list.size == 1 && binding.lengthToggle.isChecked) || (list.size == 1 && list[0].contains(
                        getString(
                                R.string.length
                        )
                ))
            ) {
                binding.genPasswordId.error = getString(R.string.noRules)
            } else {
                binding.genPasswordId.error = null
                val newPassword: String =
                        myPasswordManager.generatePassword(
                                useLetters,
                                useUC,
                                useNumbers,
                                useSymbols,
                                length
                        )
                binding.genPasswordIdField.setText(newPassword)

                val evaluation: String =
                        myPasswordManager.evaluatePasswordString(binding.genPasswordIdField.text.toString())
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
            }
        }
        binding.generatePassword.setOnTouchListener { v, event ->
            v.performClick()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.cardPass.elevation = 50F
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
                    binding.cardPass.elevation = 10F
                    v.invalidate()
                }
            }
            false
        }
        binding.genPasswordId.setOnClickListener {
            if (binding.genPasswordIdField.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                        "Password",
                        binding.genPasswordIdField.text.toString()
                )
                clipboard.setPrimaryClip(clip)
                Utils.makeToast(applicationContext, getString(R.string.passCopied))
            }
        }
        binding.genPasswordIdField.setOnClickListener {
            if (binding.genPasswordIdField.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                        "Password",
                        binding.genPasswordIdField.text.toString()
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
                } else if (binding.genPasswordIdField.text.toString() == "" || binding.genPasswordIdField.text.toString().length < 4) {
                    binding.genPasswordId.error = getString(R.string.errPass)
                } else {
                    val password = if (binding.cryptToggle.isChecked)
                        Utils.password_manager.encrypt(binding.genPasswordIdField.text.toString())
                    else
                        binding.genPasswordIdField.text.toString()

                    if (viewModel.currentPassword != null) {
                        viewModel.currentPassword!!.name = binding.newNameField.text.toString()
                        viewModel.currentPassword!!.password = password!!
                        viewModel.currentPassword!!.use_2fa = binding.authToggle.isChecked
                        viewModel.currentPassword!!.use_time = binding.timeLimit.isChecked
                        viewModel.currentPassword!!.time = Date().toString()
                        viewModel.currentPassword!!.description = binding.noteField.text.toString()
                        viewModel.currentPassword!!.tags = binding.keyWordsField.text.toString()
                        viewModel.currentPassword!!.groups = ""
                        viewModel.currentPassword!!.login = binding.emailField.text.toString()
                        viewModel.currentPassword!!.encrypted = binding.cryptToggle.isChecked
                        viewModel.currentPassword!!.quality = Utils.evaluatePassword(
                                viewModel.currentPassword!!
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
                        if (imageName != "") {
                            val from = File(mediaStorageDir, "$imageName.jpg")
                            val to = File(mediaStorageDir, "${binding.newNameField.text}.jpg")
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
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("password_id", viewModel.currentPassword!!._id)
                setResult(1, intent)
                finish()
            }
        }
        binding.upload.setOnClickListener {
            checkPermissionForImage()
        }
    }


    private val PERMISSION_CODE_READ = 1001
    private val PERMISSION_CODE_WRITE = 1002
    private val IMAGE_PICK_CODE = 1000

    private fun checkPermissionForImage() {
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ) {
            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(permission, PERMISSION_CODE_READ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
            requestPermissions(permissionCoarse, PERMISSION_CODE_WRITE) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
        } else {
            pickImageFromGallery()
        }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
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

    @SuppressLint("SdCardPath")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 1) {
                recreate()
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // I'M GETTING THE URI OF THE IMAGE AS DATA AND SETTING IT TO THE IMAGEVIEW
            binding.attachedImage.setImageURI(data?.data)
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val widthMax: Int = size.x
            val width = (widthMax/1.3).toInt()
            val height = binding.attachedImage.drawable.minimumHeight * width /  binding.attachedImage.drawable.minimumWidth
            binding.attachedImage.layoutParams.height = height
            binding.attachedImage.layoutParams.width = width
            binding.attachedImage.layoutParams.height = height
            binding.attachedImage.layoutParams.width = width
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
                    Utils.makeToast(applicationContext, "Failed to create directory")
                }
            }

            if (viewModel.currentPassword != null) {
                imageName = viewModel.currentPassword!!.name

                val file = File(mediaStorageDir, "$imageName.jpg")

                val resultURI = getImagePath(this, selectedImageURI)
                if (resultURI != null) {
                    copyFile(File(resultURI), file)
                }
                isImage = true
            }
        }
    }
}