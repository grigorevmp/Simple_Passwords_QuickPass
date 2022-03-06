package com.mikhailgrigorev.quickpassword.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.base.MyBaseActivity
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivitySettingsBinding
import com.mikhailgrigorev.quickpassword.ui.pin_code.set.SetPinActivity


class SettingsActivity : MyBaseActivity() {

    private lateinit var login: String
    private var condition = true
    private lateinit var binding: ActivitySettingsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        login = Utils.getLogin().toString()

        val lockTime = Utils.getAppLockTime()

        if (lockTime != 0) {
            binding.sbAppLockTimer.progress = lockTime
            binding.tvAppLockTime.text = lockTime.toString() + "m"
        } else {
            binding.tvAppLockTime.text = getString(R.string.doNotLock)
        }

        val hasBiometricFeature: Boolean =
                this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
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

        binding.sUseAnalyzer.setOnCheckedChangeListener { _, _ ->
            Utils.setAnalyze(binding.sUseAnalyzer.isChecked)
        }

        binding.tvUseAnalyzer.setOnClickListener {
            binding.sUseAnalyzer.isChecked = !binding.sUseAnalyzer.isChecked
            Utils.setAnalyze(binding.sUseAnalyzer.isChecked)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.cvAutoFillSettings.visibility = View.GONE
        }

        binding.checkAutoFillSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                testAutoFill(this)
            }
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
                val intent = Intent(this, SetPinActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Utils.setPinMode(binding.sSetPin.isChecked)
            }
        }

        binding.tvSetPin.setOnClickListener {
            if (binding.sSetPin.isChecked) {
                binding.sSetPin.isChecked = false
                Utils.setPinMode(binding.sSetPin.isChecked)
            } else {
                condition = false
                Utils.setPinMode(binding.sSetPin.isChecked)
                val intent = Intent(this, SetPinActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.sbAppLockTimer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                binding.tvAppLockTime.text = i.toString() + "m"
                if (i == 0) {
                    binding.tvAppLockTime.text = getString(R.string.doNotLock)
                }
                Utils.setAppLockTime(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        binding.sFingerprintUnlock.setOnCheckedChangeListener { _, _ ->
            Utils.setBioMode(binding.sFingerprintUnlock.isChecked)
        }

        binding.tvFingerprintUnlock.setOnClickListener {
            binding.sFingerprintUnlock.isChecked = !binding.sFingerprintUnlock.isChecked
            Utils.setBioMode(binding.sFingerprintUnlock.isChecked)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testAutoFill(context: Context){
        val autoFillManager: AutofillManager = context.getSystemService(AutofillManager::class.java)
        if (!autoFillManager.hasEnabledAutofillServices()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:com.mikhailgrigorev.quickpassword")
            startActivityForResult(intent, 0)
        }
        else{
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:none")
            startActivityForResult(intent, 0)
        }

    }
}
