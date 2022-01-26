package com.mikhailgrigorev.quickpassword.common.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard
import java.text.SimpleDateFormat
import java.util.*

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

    fun returnReadableDate(date: String): String {
        val sdf3 = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val sameDate = sdf3.parse(date)

        val calendar = Calendar.getInstance()
        calendar.time = sameDate!!

        val year = calendar.get(Calendar.YEAR).toString()
        calendar.add(Calendar.MONTH, 1);
        val month = calendar.get(Calendar.MONTH).toString()
        val day = calendar.get(Calendar.DATE).toString()

        return "$day/$month/$year"
    }

    fun lockTime() = sharedPreferences!!.getString("appLockTime", "6")
    const val lock_default_interval: Long = 100000

    val password_manager = PasswordManager()
    fun autoCopy() = sharedPreferences!!.getString("prefAutoCopyKey", "none")
    fun userName() = sharedPreferences!!.getString("prefUserNameKey", "Stranger")
    fun setUserName(name: String) {
        with(sharedPreferences!!.edit()) {
            putString("prefUserNameKey", name)
            apply()
        }
    }

    fun useAnalyze() = sharedPreferences!!.getString("useAnalyze", "none")
    fun sortingColumn() = sharedPreferences!!.getString("sortingColumn", "name")
    fun sortingAsc() = sharedPreferences!!.getBoolean("sortingAsc", false)

    fun setSortingType(value: String) {
        with(sharedPreferences!!.edit()) {
            putString("sortingColumn", value)
            apply()
        }
    }

    fun setSortingAsc(value: Boolean) {
        with(sharedPreferences!!.edit()) {
            putBoolean("sortingAsc", value)
            apply()
        }
    }

    fun bottomBarState() = sharedPreferences!!.getInt("bottomSheetDialogState", BottomSheetBehavior.STATE_COLLAPSED)
    fun setBottomBarState(state: Int) {
        with(sharedPreferences!!.edit()) {
            putInt("bottomSheetDialogState", state)
            apply()
        }
    }

    const val account_logo = "ic_account"

    fun makeToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun evaluatePassword(password: PasswordCard, originalPassword: String): Int {
        val evaluation: Float = password_manager.evaluatePassword(originalPassword)

        var qualityScore = when {
            evaluation < 0.33 -> 2
            evaluation < 0.66 -> 3
            else -> 1
        }

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

    fun analyzeDataBase(current_password: PasswordCard, passwords: List<PasswordCard>): Pair<Boolean, MutableList<String>> {
        var containOthers = false
        val otherPasswords: MutableList<String> = arrayListOf()
        for (password in passwords) {
            if (password._id != current_password._id) {
                var password1 = current_password.password
                var password2 = password.password

                if (current_password.encrypted)
                    password1 = password_manager.decrypt(password1).toString()

                if (password.encrypted)
                    password2 = password_manager.decrypt(password2).toString()

                if (
                    (password1.contains(password2))
                    or
                    (password2.contains(password1))
                ) {
                    containOthers = true
                    otherPasswords.add(password.name)
                }
            }
        }
        return Pair(containOthers, otherPasswords)
    }

}