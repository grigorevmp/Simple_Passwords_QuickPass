package com.mikhailgrigorev.quickpassword.common.utils.toogles

import com.mikhailgrigorev.quickpassword.common.utils.Utils

object BioModeToggle : Toggle() {

    override fun set(value: Boolean) {
        Utils.editPreferences("prefBioMode", value)
    }

    override fun isEnabled(): Boolean = Utils.sharedPreferences!!.getBoolean("prefBioMode", false)

    fun remove(){
        Utils.sharedPreferences!!.edit().remove("prefBioMode").apply()
    }
}