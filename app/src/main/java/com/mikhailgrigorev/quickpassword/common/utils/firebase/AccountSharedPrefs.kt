package com.mikhailgrigorev.simple_password.common.utils.firebase

import com.mikhailgrigorev.simple_password.common.utils.Utils

object AccountSharedPrefs {

    fun removeAll(){
        Utils.enSharedPrefsFile!!.edit().remove("prefLogin").apply()
        Utils.sharedPreferences!!.edit().remove("prefMail").apply()
        Utils.sharedPreferences!!.edit().remove("prefLocalAccount").apply()
    }

    fun getLogin() = Utils.enSharedPrefsFile!!.getString("prefLogin", "Stranger")
    fun getMail() = Utils.enSharedPrefsFile!!.getString("prefMail", "null")
    fun getIsLocal() = Utils.enSharedPrefsFile!!.getBoolean("prefLocalAccount", false)

    fun setLocal() {
        with(Utils.enSharedPrefsFile!!.edit()) {
            putBoolean("prefLocalAccount", true)
            apply()
        }
    }
    fun setLogin(login: String) {
        with(Utils.enSharedPrefsFile!!.edit()) {
            putString("prefLogin", login)
            apply()
        }
    }

    fun setMail(mail: String) {
        with(Utils.enSharedPrefsFile!!.edit()) {
            putString("prefMail", mail)
            apply()
        }
    }
}