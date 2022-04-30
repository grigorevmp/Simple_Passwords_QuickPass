package com.mikhailgrigorev.quickpassword.common.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.common.manager.PasswordManager
import com.mikhailgrigorev.quickpassword.common.utils.firebase.AccountSharedPrefs
import com.mikhailgrigorev.quickpassword.common.utils.toogles.ToggleManager
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    fun init(application: Application) {
        Utils.application = application
    }

    val toggleManager = ToggleManager()
    val accountSharedPrefs = AccountSharedPrefs

    val auth = FirebaseAuth.getInstance()

    private var application: Application? = null

    private const val preferences_file = "quickPassPreference"
    var sharedPreferences: SharedPreferences? = null
    var enSharedPrefsFile: SharedPreferences? = null

    fun setSharedPreferences() {
        sharedPreferences = application?.getSharedPreferences(
                preferences_file,
                Context.MODE_PRIVATE
        )

        enSharedPrefsFile =
                EncryptedSharedPreferences.create(
                        "secretFile",
                        "secretAlias",
                        application!!.applicationContext,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

    }

    fun returnReadableDate(date: String): String {
        try {
            val sdf3 = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val sameDate = sdf3.parse(date)

            val calendar = Calendar.getInstance()
            calendar.time = sameDate!!

            val year = calendar.get(Calendar.YEAR).toString()
            calendar.add(Calendar.MONTH, 1)
            val month = calendar.get(Calendar.MONTH).toString()
            val day = calendar.get(Calendar.DATE).toString()

            return "$day/$month/$year"
        } catch (e: Exception) {
            return "Date in old format"
        }
    }

    val password_manager = PasswordManager()

    fun exitAccount() {
        accountSharedPrefs.removeAll()
        toggleManager.pinModeToggle.remove()
        toggleManager.bioModeToggle.remove()
    }

    fun sortingAsc() = sharedPreferences!!.getBoolean("sortingAsc", false)
    fun getAppLockTime() = sharedPreferences!!.getInt("appLockTime", 6)
    fun getDisconnectTime() = getAppLockTime() * 10000L
    fun getPin() = sharedPreferences!!.getInt("prefPin", 0)
    fun sortingColumn() = sharedPreferences!!.getString("sortingColumn", "name")
    fun bottomBarState() = sharedPreferences!!.getInt(
            "bottomSheetDialogState",
            BottomSheetBehavior.STATE_HIDDEN
    )


    fun <ValueType> editPreferences(
        key: String,
        value: ValueType
    ) {
        with(sharedPreferences!!.edit()) {
            when (value) {
                is String -> {
                    putString(key, value)
                }
                is Boolean -> {
                    putBoolean(key, value)
                }
                is Int -> {
                    putInt(key, value)
                }
            }
            apply()
        }
    }

    fun setSortingType(value: String) {
        editPreferences("sortingColumn", value)
    }

    fun setAppLockTime(value: Int) {
        editPreferences("appLockTime", value)
    }

    fun setPin(value: Int) {
        editPreferences("prefPin", value)
    }

    fun setSortingAsc(value: Boolean) {
        editPreferences("sortingAsc", value)
    }

    fun setBottomBarState(state: Int) {
        editPreferences("bottomSheetDialogState", state)
    }

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

    fun validate(password: String): Boolean {
        return (password.isEmpty() || password.length < 4 || password.length > 20)
    }

    fun getApplication() = application

}