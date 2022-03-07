package com.mikhailgrigorev.quickpassword.ui.settings

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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.manager.BackupManager
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.database.FOLDER_CARD_DB_NAME
import com.mikhailgrigorev.quickpassword.data.database.PASSWORD_CARD_DB_NAME
import com.mikhailgrigorev.quickpassword.databinding.FragmentSettingsBinding
import com.mikhailgrigorev.quickpassword.ui.pin_code.set.PinSetActivity

class SettingsFragment: Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        initUI()
        initListeners()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Utils.makeToast(context!!, "Success! Try again!")
        }
    }

    private fun initListeners() {
        binding.ibExportDatabases.setOnClickListener {
            when (BackupManager.exportEncryptedDB(context!!, PASSWORD_CARD_DB_NAME)) {
                2 -> {
                    Utils.makeToast(context!!, "EXPORT_DB PASSWORD: No storage permission.")
                }
                3 -> Utils.makeToast(context!!, "EXPORT_DB PASSWORD: Error exporting database!")
            }
            when (BackupManager.exportEncryptedDB(context!!, FOLDER_CARD_DB_NAME)) {
                2 -> {
                    Utils.makeToast(context!!, "EXPORT_DB FOLDERS: No storage permission.")
                }
                3 -> Utils.makeToast(context!!, "EXPORT_DB FOLDERS: Error exporting database!")
            }
        }

        binding.ibImportDatabases.setOnClickListener {
            when (BackupManager.importEncryptedDB(context!!, PASSWORD_CARD_DB_NAME)) {
                2 -> {
                    Utils.makeToast(context!!, "IMPORT_DB PASSWORD: No storage permission.")
                }
                3 -> Utils.makeToast(context!!, "IMPORT_DB PASSWORD: Error exporting database!")
            }
            when (BackupManager.importEncryptedDB(context!!, FOLDER_CARD_DB_NAME)) {
                2 -> {
                    Utils.makeToast(context!!, "IMPORT_DB FOLDERS: No storage permission.")
                }
                3 -> Utils.makeToast(context!!, "IMPORT_DB FOLDERS: Error exporting database!")
            }
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
                Utils.setPinMode(binding.sSetPin.isChecked)
            }
        }

        binding.tvSetPin.setOnClickListener {
            if (binding.sSetPin.isChecked) {
                binding.sSetPin.isChecked = false
                Utils.setPinMode(binding.sSetPin.isChecked)
            } else {
                Utils.setPinMode(binding.sSetPin.isChecked)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testAutoFill(context: Context){
        val autoFillManager: AutofillManager = context.getSystemService(AutofillManager::class.java)
        if (!autoFillManager.hasEnabledAutofillServices()) {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:com.mikhailgrigorev.quickpassword")
            startActivity(intent)
        }
        else{
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:none")
            startActivity(intent)
        }

    }
}