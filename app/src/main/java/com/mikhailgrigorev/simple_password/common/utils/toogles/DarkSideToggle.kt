package com.mikhailgrigorev.simple_password.common.utils.toogles

import com.mikhailgrigorev.simple_password.common.utils.Utils

object DarkSideToggle : Toggle() {

    override fun set(value: Boolean) {
        Utils.editPreferences("darkSide", value)
    }

    override fun isEnabled(): Boolean = Utils.sharedPreferences!!.getBoolean("darkSide", true)
}