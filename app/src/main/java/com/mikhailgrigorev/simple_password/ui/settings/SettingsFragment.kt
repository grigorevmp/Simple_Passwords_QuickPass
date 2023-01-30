package com.mikhailgrigorev.simple_password.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.manager.BackupManager
import com.mikhailgrigorev.simple_password.common.manager.BackupManager.goToFileIntent
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.dbo.FolderCard
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard
import com.mikhailgrigorev.simple_password.databinding.FragmentSettingsBinding
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.simple_password.ui.main_activity.MainViewModel
import com.mikhailgrigorev.simple_password.ui.pin_code.set.PinSetActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import javax.inject.Inject

class SettingsFragment: Fragment() {

    companion object {
        val IMPORT_FROM_PASSWORD_CODE = 5
        val IMPORT_FROM_OLD_QUICKPASS = 11
    }



    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        initViewModel()
        initUI()
        initListeners()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        initUI()
    }

    private fun setLockTimeText(lockTime: Int) {
        binding.sbAppLockTimer.progress = lockTime
        if (lockTime != 0) {
            binding.tvAppLockTime.text = getString(
                    R.string.minutesAppLock,
                    lockTime
            )
        } else {
            binding.tvAppLockTime.text = getString(R.string.doNotLock)
        }
    }

    private fun initUI() {
        setLockTimeText(Utils.getAppLockTime())

        val biometricManager = BiometricManager.from(requireActivity())
        when (biometricManager.canAuthenticate(BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("QP", "App can authenticate using biometrics.")
            }
            else -> {
                binding.sFingerprintUnlock.visibility = View.GONE
                binding.tvFingerprintUnlock.visibility = View.GONE
            }
        }

        binding.sDarkSide.isChecked = Utils.toggleManager.darkSideToggle.isEnabled()

        binding.sFingerprintUnlock.isChecked = Utils.toggleManager.bioModeToggle.isEnabled()

        binding.sAutoCopy.isChecked = !Utils.toggleManager.autoCopyToggle.isEnabled()

        binding.sSetPin.isChecked = Utils.toggleManager.pinModeToggle.isEnabled()

        binding.sUseAnalyzer.isChecked = !Utils.toggleManager.analyzeToggle.isEnabled()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.cvAutoFillSettings.visibility = View.GONE
        }
    }

    private fun initListeners() {
        binding.ibExportDatabases.setOnClickListener {
            exportDatabaseToCSVFile(false)
            exportDatabaseToCSVFile(true)
        }

        @Throws(IOException::class)
        fun readCSV(uri: Uri): List<String> {
            val csvFile = context?.contentResolver?.openInputStream(uri)
            val isr = InputStreamReader(csvFile)
            return BufferedReader(isr).readLines()
        }

        val resultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        try {
                            val data = result.data?.data
                            data.let { commonResult ->
                                val dataResult = commonResult?.let { readCSV(it) }

                                if (dataResult != null) {
                                    var firstLine = true
                                    var tmp = ""

                                    for (line in dataResult) {

                                        if (firstLine) {
                                            firstLine = false
                                            continue
                                        }

                                        if(line[line.length - 1] == '='){
                                            tmp = line
                                            continue
                                        }

                                        val split = (tmp + line).split(",".toRegex())

                                        tmp = ""

                                        when (split.size) {
                                            IMPORT_FROM_PASSWORD_CODE -> {
                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    viewModel.insertCard(
                                                        FolderCard(
                                                            name = split[1],
                                                            imageSrc = split[2],
                                                            colorTag = split[3],
                                                            description = split[4],
                                                        )
                                                    )
                                                }
                                            }
                                            IMPORT_FROM_OLD_QUICKPASS -> {
                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    viewModel.insertPassword(
                                                        PasswordCard(
                                                            name = split[1],
                                                            image_count = 0,
                                                            password = split[2],
                                                            use_2fa = split[3].toBoolean(),
                                                            is_card_pin = false,
                                                            use_time = split[4].toBoolean(),
                                                            time = Date().toString(),
                                                            description = split[9],
                                                            tags = split[6] + " Imported",
                                                            folder = -1,
                                                            login = split[8],
                                                            encrypted = split[10] == "crypted",
                                                            quality = 2,
                                                            same_with = "",
                                                            favorite = false,
                                                        )
                                                    )
                                                }
                                            }
                                            else -> {
                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    viewModel.insertPassword(
                                                        PasswordCard(
                                                            name = split[1],
                                                            image_count = split[2].toInt(),
                                                            password = split[3],
                                                            use_2fa = split[4].toBoolean(),
                                                            is_card_pin = split[5].toBoolean(),
                                                            use_time = split[6].toBoolean(),
                                                            time = split[7],
                                                            description = if (split[8] != "null") split[8] else "",
                                                            tags = if (split[9] != "null") split[9] + " Imported" else "",
                                                            folder = if (split[10] != "") split[10].toInt() else -1,
                                                            login = if (split[11] != "null") split[11] else "",
                                                            encrypted = split[12].toBoolean(),
                                                            quality = split[13].toInt(),
                                                            same_with = if (split[14] != "null") split[14] else "",
                                                            favorite = split[15].toBoolean(),
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Utils.makeToast(requireContext(), getString(R.string.import_db_folder_ok))
                                }
                            }
                        }
                        catch (e: Exception){
                            e.message?.let { Log.d("Backup", it) }
                            Utils.makeToast(requireContext(), "IMPORT_DB FOLDER: ${e.message}")
                        }
                    }
                }


        // Google format: Name, Url, Username, Password
        val googleResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        try {
                            val data = result.data?.data

                            data.let { commonResult ->
                                val dataResult = commonResult?.let { readCSV(it) }

                                if (dataResult != null) {
                                    var firstLine = true
                                    var tmp = ""

                                    for (line in dataResult) {
                                        if (firstLine) {
                                            firstLine = false
                                            continue
                                        }

                                        if(line[line.length - 1] == '='){
                                            tmp = line
                                            continue
                                        }

                                        val split = (tmp + line).split(",".toRegex())

                                        tmp = ""

                                        when (split.size) {
                                            4 -> {
                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    viewModel.insertPassword(
                                                        PasswordCard(
                                                            name = split[0],
                                                            image_count = 0,
                                                            password = split[3],
                                                            use_2fa = false,
                                                            is_card_pin = false,
                                                            use_time = false,
                                                            time = Date().toString(),
                                                            description = "Url: " + split[1],
                                                            tags = "Google_password Imported",
                                                            folder = -1,
                                                            login = split[2],
                                                            encrypted = false,
                                                            quality = 2,
                                                            same_with = "",
                                                            favorite = false,
                                                        )
                                                    )
                                                }
                                            }
                                            else -> Utils.makeToast(requireContext(), getString(R.string.import_db_failed))
                                        }
                                    }

                                    Utils.makeToast(requireContext(), getString(R.string.import_db_folder_ok))
                                }
                            }
                        }
                        catch (e: Exception){
                            e.message?.let { Log.d("Backup", it) }
                            Utils.makeToast(requireContext(), "IMPORT_DB FOLDER: ${e.message}")
                        }
                    }
                }

        binding.ibImportDatabases.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"
            resultLauncher.launch(intent)
        }

        binding.ibImportDatabasesFromGoogle.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"
            googleResultLauncher.launch(intent)
        }

        binding.checkAutoFillSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                testAutoFill(requireContext())
            }
        }

        binding.sUseAnalyzer.setOnCheckedChangeListener { _, _ ->
            Utils.toggleManager.analyzeToggle.set(!binding.sUseAnalyzer.isChecked)
        }

        binding.tvUseAnalyzer.setOnClickListener {
            binding.sUseAnalyzer.isChecked = !binding.sUseAnalyzer.isChecked
            Utils.toggleManager.analyzeToggle.set(!binding.sUseAnalyzer.isChecked)
        }

        binding.sAutoCopy.setOnCheckedChangeListener { _, _ ->
            Utils.toggleManager.autoCopyToggle.set(binding.sAutoCopy.isChecked)
        }

        binding.tvAutoCopy.setOnClickListener {
            binding.sAutoCopy.isChecked = !binding.sAutoCopy.isChecked
            Utils.toggleManager.autoCopyToggle.set(binding.sAutoCopy.isChecked)
        }

        binding.sSetPin.setOnCheckedChangeListener { _, _ ->
            if (binding.sSetPin.isChecked) {
                val intent = Intent(requireContext(), PinSetActivity::class.java)
                startActivity(intent)
            } else {
                Utils.toggleManager.pinModeToggle.set(false)
            }
        }

        binding.tvSetPin.setOnClickListener {
            if (binding.sSetPin.isChecked) {
                binding.sSetPin.isChecked = false
                Utils.toggleManager.pinModeToggle.set(binding.sSetPin.isChecked)
            } else {
                val intent = Intent(requireContext(), PinSetActivity::class.java)
                startActivity(intent)
            }
        }

        binding.tvDarkSide.setOnClickListener {
            if (binding.sDarkSide.isChecked) {
                binding.sDarkSide.isChecked = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                Utils.toggleManager.darkSideToggle.set(false)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        binding.sDarkSide.setOnCheckedChangeListener { _, _ ->
            if (binding.sDarkSide.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            Utils.toggleManager.darkSideToggle.set(binding.sDarkSide.isChecked)
        }

        binding.sFingerprintUnlock.setOnCheckedChangeListener { _, _ ->
            Utils.toggleManager.bioModeToggle.set(binding.sFingerprintUnlock.isChecked)
        }

        binding.tvFingerprintUnlock.setOnClickListener {
            binding.sFingerprintUnlock.isChecked = !binding.sFingerprintUnlock.isChecked
            Utils.toggleManager.bioModeToggle.set(binding.sFingerprintUnlock.isChecked)
        }

        binding.sbAppLockTimer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                setLockTimeText(i)
                Utils.setAppLockTime(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    private fun exportPasswordsToCSVFile(csvFile: File) {
        viewModel.passwords.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                this.context?.let { it1 ->
                    Utils.makeToast(
                        it1,
                        getString(R.string.no_passwords_found)
                    )
                }
            } else {
                csvWriter().open(csvFile, append = false) {
                    writeRow(
                        listOf(
                            "[id]",
                            "[name]",
                            "[image_count]",
                            "[password]",
                            "[use_2fa]",
                            "[is_card_pin]",
                            "[use_time]",
                            "[time]",
                            "[description]",
                            "[tags]",
                            "[folder]",
                            "[login]",
                            "[encrypted]",
                            "[quality]",
                            "[same_with]",
                            "[favorite]"
                        )
                    )
                    it.forEachIndexed { index, passwordCard ->
                        writeRow(
                            listOf(
                                index,
                                passwordCard.name,
                                passwordCard.image_count,
                                passwordCard.password,
                                passwordCard.use_2fa,
                                passwordCard.is_card_pin,
                                passwordCard.use_time,
                                passwordCard.time,
                                if (passwordCard.description != "") passwordCard.description else "null",
                                if (passwordCard.tags != "") passwordCard.tags else "null",
                                passwordCard.folder,
                                if (passwordCard.login != "") passwordCard.login else "null",
                                passwordCard.encrypted,
                                passwordCard.quality,
                                if (passwordCard.same_with != "") passwordCard.same_with else "null",
                                passwordCard.favorite
                            )
                        )
                    }
                }
            }
        }
    }

    private fun exportFoldersToCSVFile(csvFile: File) {
        viewModel.folders.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                this.context?.let { it1 ->
                    Utils.makeToast(
                            it1,
                            getString(R.string.no_folders_found)
                    )
                }
            } else {
                csvWriter().open(csvFile, append = false) {
                    writeRow(
                        listOf(
                            "[id]",
                            "[name]",
                            "[imageSrc]",
                            "[colorTag]",
                            "[description]",
                        )
                    )
                    it.forEachIndexed { index, folderCard ->
                        writeRow(
                            listOf(
                                index,
                                folderCard.name,
                                if (folderCard.imageSrc != "") folderCard.imageSrc else "null",
                                if (folderCard.colorTag != "") folderCard.colorTag else "0",
                                if (folderCard.description != "") folderCard.description else "null",
                            )
                        )
                    }
                }
            }
        }
    }

    private fun exportDatabaseToCSVFile(folder: Boolean) {
        val csvFile = this.context?.let {
            BackupManager.generateFile(
                    it,
                    BackupManager.getCSVFileName(folder)
            )
        }
        if (csvFile != null) {
            if (folder) {
                exportFoldersToCSVFile(csvFile)
                val intent = this.context?.let {
                    goToFileIntent(it, csvFile)
                }
                startActivity(intent)
            } else {
                exportPasswordsToCSVFile(csvFile)
                val intent = this.context?.let {
                    goToFileIntent(it, csvFile)
                }
                startActivity(intent)
            }
            // Utils.makeToast(requireContext(), "EXPORT_DB: Ok.")
        } else {
            Utils.makeToast(requireContext(), "EXPORT_DB: Error.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testAutoFill(context: Context) {
        val autoFillManager: AutofillManager = context.getSystemService(AutofillManager::class.java)
        if (!autoFillManager.hasEnabledAutofillServices()) {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:com.mikhailgrigorev.simple_password")
            startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:none")
            startActivity(intent)
        }

    }

    private fun initViewModel() {
        viewModel = this.injectViewModel(viewModelFactory)
    }
}