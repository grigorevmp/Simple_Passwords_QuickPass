package com.mikhailgrigorev.quickpassword.common.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard

object Utils {

    fun init(application: Application) {
        Utils.application = application
    }

    private var application: Application? = null
    private const val preferences_file = "quickPassPreference"

    var sharedPreferences: SharedPreferences? = null

    fun setSharedPreferences() {
        sharedPreferences = application?.getSharedPreferences(
                preferences_file,
                Context.MODE_PRIVATE
        )
    }


    fun lockTime() = sharedPreferences!!.getString("appLockTime", "6")
    const val lock_default_interval: Long = 100000

    val password_manager = PasswordManager()
    fun autoCopy() = sharedPreferences!!.getString("prefAutoCopyKey", "none")
    fun userName() = sharedPreferences!!.getString("prefUserNameKey", "none")
    fun setUserName(name: String) {
        with(sharedPreferences!!.edit()) {
            putString("prefUserNameKey", name)
            apply()
        }
    }

    fun useAnalyze() = sharedPreferences!!.getString("useAnalyze", "none")
    fun sortingType() = sharedPreferences!!.getString("sort", "none")

    fun setSortingType(value: String) {
        with(sharedPreferences!!.edit()) {
            putString("sort", value)
            apply()
        }
    }

    fun bottomBarState() = sharedPreferences!!.getInt("__BS", BottomSheetBehavior.STATE_COLLAPSED)

    const val account_logo = "ic_account"

    fun makeToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun evaluatePassword(password: PasswordCard): Int {
        val evaluation: Float = password_manager.evaluatePassword(password.password)

        var qualityScore = when {
            evaluation < 0.33 -> 2
            evaluation < 0.66 -> 3
            else -> 1
        }

        if (password.encrypted)
            qualityScore = 6

        if (password_manager.evaluateDate(password.time))
            qualityScore = 2

        if (!password.encrypted && password.password.length == 4)
            qualityScore = 4

        if (password_manager.popularPasswords(password.password)
            or ((password.password.length == 4)
                    and password_manager.popularPin(password.password))
        ) {
            qualityScore = if (qualityScore == 4)
                5
            else
                2
        }

        return qualityScore
    }


}