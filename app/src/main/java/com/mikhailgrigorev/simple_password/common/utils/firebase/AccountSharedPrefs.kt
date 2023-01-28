package com.mikhailgrigorev.simple_password.common.utils.firebase

import com.mikhailgrigorev.simple_password.common.utils.Utils

object AccountSharedPrefs {

    fun removeAll(){
        Utils.enSharedPrefsFile!!.edit().remove("prefLogin").apply()
        Utils.sharedPreferences!!.edit().remove("prefMail").apply()
        Utils.sharedPreferences!!.edit().remove("prefLocalAccount").apply()
        Utils.sharedPreferences!!.edit().remove("prefMasterKey").apply()
    }

    fun isCorrectLogin(login: String): Boolean {
        return login == getLogin()
    }

    fun isCorrectMasterPassword(password: String): Boolean {
        val masterKey = Utils.enSharedPrefsFile!!.getInt("prefMasterKey", 0)
        return masterKey == password.hashCode()
    }

    fun getLogin() = Utils.enSharedPrefsFile!!.getString("prefLogin", null)

    fun getAvatarEmoji() = Utils.enSharedPrefsFile!!.getString("prefAvatarEmoji", "\uD83E\uDD8A")

    fun setMasterPassword(password: String) {
        with(Utils.enSharedPrefsFile!!.edit()) {
            putInt("prefMasterKey", password.hashCode())
            apply()
        }
    }

    fun setLogin(login: String) {
        with(Utils.enSharedPrefsFile!!.edit()) {
            putString("prefLogin", login)
            apply()
        }
    }

    fun setAvatarEmoji(emoji: String) {
        with(Utils.enSharedPrefsFile!!.edit()) {
            putString("prefAvatarEmoji", emoji)
            apply()
        }
    }
}