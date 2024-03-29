package com.mikhailgrigorev.simple_password.common.utils.toogles

import com.mikhailgrigorev.simple_password.common.utils.Utils

object PinModeToggle : Toggle() {

    override fun set(value: Boolean) {
        Utils.editPreferences("prefPinMode", value)
    }

    override fun isEnabled(): Boolean = Utils.sharedPreferences!!.getBoolean("prefPinMode", false)

    fun remove(){
        Utils.sharedPreferences!!.edit().remove("prefPinMode").apply()
    }
}