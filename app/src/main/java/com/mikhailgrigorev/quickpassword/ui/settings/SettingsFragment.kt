package com.mikhailgrigorev.quickpassword.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.manager.BackupManager
import com.mikhailgrigorev.quickpassword.common.manager.BackupManager.goToFileIntent
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.FragmentSettingsBinding
import com.mikhailgrigorev.quickpassword.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.quickpassword.di.modules.RoomModule
import com.mikhailgrigorev.quickpassword.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.quickpassword.ui.main_activity.MainViewModel
import com.mikhailgrigorev.quickpassword.ui.pin_code.set.PinSetActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

class SettingsFragment: Fragment() {
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

        val hasBiometricFeature: Boolean =
                context!!.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        if (!hasBiometricFeature) {
            binding.sFingerprintUnlock.visibility = View.GONE
            binding.tvFingerprintUnlock.visibility = View.GONE
        }

        if (Utils.getBioMode()) {
            binding.sFingerprintUnlock.isChecked = true
        }

        if (!Utils.getAutoCopy()) {
            binding.sAutoCopy.isChecked = false
        }

        if (Utils.getPinMode()) {
            binding.sSetPin.isChecked = true
        }

        if (!Utils.useAnalyze()) {
            binding.sUseAnalyzer.isChecked = true
        }

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
                        val data = result.data?.data
                        data.let { commonResult ->
                            val dataResult = commonResult?.let { it ->
                                readCSV(it)
                            }
                            if (dataResult != null) {
                                var firstLine = true
                                for (line in dataResult) {
                                    if (firstLine) {
                                        firstLine = false
                                        continue
                                    }
                                    val split = line.split(",")
                                    if (split.size == 5) {
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
                                        Utils.makeToast(context!!, "IMPORT_DB FOLDER: Ok.")
                                    } else {
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
                                                            description = split[8],
                                                            tags = split[9],
                                                            folder = if (split[10] != "") split[10].toInt() else null,
                                                            login = split[11],
                                                            encrypted = split[12].toBoolean(),
                                                            quality = split[13].toInt(),
                                                            favorite = split[14].toBoolean(),
                                                    )
                                            )
                                        }
                                        Utils.makeToast(context!!, "IMPORT_DB PASS: Ok.")
                                    }
                                }
                            }
                        }
                    }
                }

        binding.ibImportDatabases.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"
            resultLauncher.launch(intent)
        }

        binding.checkAutoFillSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                testAutoFill(context!!)
            }
        }

        binding.sUseAnalyzer.setOnCheckedChangeListener { _, _ ->
            Utils.setAnalyze(!binding.sUseAnalyzer.isChecked)
        }

        binding.tvUseAnalyzer.setOnClickListener {
            binding.sUseAnalyzer.isChecked = !binding.sUseAnalyzer.isChecked
            Utils.setAnalyze(!binding.sUseAnalyzer.isChecked)
        }

        binding.sAutoCopy.setOnCheckedChangeListener { _, _ ->
            Utils.setAutoCopy(binding.sAutoCopy.isChecked)
        }

        binding.tvAutoCopy.setOnClickListener {
            binding.sAutoCopy.isChecked = !binding.sAutoCopy.isChecked
            Utils.setAutoCopy(binding.sAutoCopy.isChecked)
        }

        binding.sSetPin.setOnCheckedChangeListener { _, _ ->
            if (binding.sSetPin.isChecked) {
                val intent = Intent(context!!, PinSetActivity::class.java)
                startActivity(intent)
            } else {
                Utils.setPinMode(false)
            }
        }

        binding.tvSetPin.setOnClickListener {
            if (binding.sSetPin.isChecked) {
                binding.sSetPin.isChecked = false
                Utils.setPinMode(binding.sSetPin.isChecked)
            } else {
                val intent = Intent(context!!, PinSetActivity::class.java)
                startActivity(intent)
            }
        }

        binding.sFingerprintUnlock.setOnCheckedChangeListener { _, _ ->
            Utils.setBioMode(binding.sFingerprintUnlock.isChecked)
        }

        binding.tvFingerprintUnlock.setOnClickListener {
            binding.sFingerprintUnlock.isChecked = !binding.sFingerprintUnlock.isChecked
            Utils.setBioMode(binding.sFingerprintUnlock.isChecked)
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

    private fun exportFoldersToCSVFile(csvFile: File) {
        viewModel.folders.observe(viewLifecycleOwner) {
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
            } else {
                exportPasswordsToCSVFile(csvFile)
            }
            val intent = this.context?.let {
                goToFileIntent(it, csvFile)
            }
            startActivity(intent)
            Utils.makeToast(context!!, "EXPORT_DB: Ok.")
        } else {
            Utils.makeToast(context!!, "EXPORT_DB: Error.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testAutoFill(context: Context) {
        val autoFillManager: AutofillManager = context.getSystemService(AutofillManager::class.java)
        if (!autoFillManager.hasEnabledAutofillServices()) {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:com.mikhailgrigorev.quickpassword")
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