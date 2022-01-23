package com.mikhailgrigorev.quickpassword.common.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.common.PasswordManager

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

}