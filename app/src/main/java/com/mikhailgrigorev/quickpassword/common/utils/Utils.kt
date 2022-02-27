package com.mikhailgrigorev.quickpassword.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.Application
import com.mikhailgrigorev.quickpassword.common.PasswordManager
import com.mikhailgrigorev.quickpassword.common.utils.senders.GMailSender
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    fun init(application: Application) {
        Utils.application = application
    }

    val auth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    val firestore = FirebaseFirestore.getInstance()

    private var application: Application? = null
    private const val preferences_file = "quickPassPreference"

    private var sharedPreferences: SharedPreferences? = null

    private var enSharedPrefsFile: SharedPreferences? = null

    private val gmailSender = GMailSender(
            hidden_email,
            hidden_password
    )

    fun sendMail(
        userMail: String,
        userPassword: String
    ) {
        if (userMail != "none") {
            Thread {
                try {
                    gmailSender.sendMail(
                            "QuickPass- Password restoring",
                            "Hello! Seems like you forgot your password and decided to restore it.\n\n" +
                                    "Your password: $userPassword \n\n" +
                                    "Have a good day, \n" +
                                    "QuickPass =)",
                            "quickpass@noreplay.com",
                            userMail
                    )

                } catch (e: Exception) {
                    Log.e("SendMail", e.message, e)
                }
            }.start()
            makeToast(
                    application!!.applicationContext,
                    application!!.resources.getString(R.string.emailWasSent) + "\n ($userMail)"
            )
        } else {
            makeToast(
                    application!!.applicationContext,
                    application!!.resources.getString(R.string.userDidNotSetMail)
            )
        }
    }

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



    fun setPassword(password: String) {
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12))
        with(enSharedPrefsFile!!.edit()) {
            putString("prefPassword", hashedPassword)
            apply()
        }
    }

    fun checkPassword(password: String): Boolean {
        return password == enSharedPrefsFile!!.getString("prefPassword", "none")
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

    fun exitAccount() {
        enSharedPrefsFile!!.edit().remove("prefLogin").apply()
        sharedPreferences!!.edit().remove("prefMail").apply()
        enSharedPrefsFile!!.edit().remove("prefPassword").apply()
        sharedPreferences!!.edit().remove("prefPinMode").apply()
        sharedPreferences!!.edit().remove("prefBioMode").apply()
    }

    fun autoCopy() = sharedPreferences!!.getString("prefAutoCopyKey", "none")
    fun getLogin() = enSharedPrefsFile!!.getString("prefLogin", "Stranger")
    fun getMail() = enSharedPrefsFile!!.getString("prefMail", "null")
    fun sortingAsc() = sharedPreferences!!.getBoolean("sortingAsc", false)
    fun getPinMode() = sharedPreferences!!.getBoolean("prefPinMode", false)
    fun getBioMode() = sharedPreferences!!.getBoolean("prefBioMode", false)
    fun useAnalyze() = sharedPreferences!!.getString("useAnalyze", "none")
    fun sortingColumn() = sharedPreferences!!.getString("sortingColumn", "name")
    fun bottomBarState() = sharedPreferences!!.getInt(
            "bottomSheetDialogState",
            BottomSheetBehavior.STATE_HIDDEN
    )

    fun setLogin(login: String) {
        with(enSharedPrefsFile!!.edit()) {
            putString("prefLogin", login)
            apply()
        }
    }

    fun setMail(mail: String) {
        with(enSharedPrefsFile!!.edit()) {
            putString("prefMail", mail)
            apply()
        }
    }

    private fun <ValueType> editPreferences(
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

    fun setBioMode(value: Boolean) {
        editPreferences("prefBioMode", value)
    }

    fun setPinMode(value: Boolean) {
        editPreferences("prefPinMode", value)
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

}