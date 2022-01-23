package com.mikhailgrigorev.quickpassword.common.utils

import android.content.Context
import android.widget.Toast
import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.common.PasswordManager

object Utils {
    private var application: Application? = null
    private const val preferences_file = "quickPassPreference"
    private val sharedPreferences = application?.getSharedPreferences(
            preferences_file,
            Context.MODE_PRIVATE
    )

    val lock_time = sharedPreferences!!.getString("appLockTime", "6")
    const val lock_default_interval: Long = 100000

    val password_manager = PasswordManager()
    val autoCopy = sharedPreferences!!.getString("prefAutoCopyKey", "none")
    val useAnalyze = sharedPreferences!!.getString("useAnalyze", "none")

    const val account_logo = "ic_account"

    fun init(application: Application) {
        Utils.application = application
    }

    fun makeToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}