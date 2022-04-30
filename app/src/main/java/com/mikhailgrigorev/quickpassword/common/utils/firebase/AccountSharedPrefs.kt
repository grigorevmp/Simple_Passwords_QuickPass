package com.mikhailgrigorev.quickpassword.common.utils.firebase

import com.mikhailgrigorev.quickpassword.common.utils.Utils

object AccountSharedPrefs {

    fun removeAll(){
        Utils.enSharedPrefsFile!!.edit().remove("prefLogin").apply()
        Utils.sharedPreferences!!.edit().remove("prefMail").apply()
    }

    fun getLogin() = Utils.enSharedPrefsFile!!.getString("prefLogin", "Stranger")
    fun getMail() = Utils.enSharedPrefsFile!!.getString("prefMail", "null")

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