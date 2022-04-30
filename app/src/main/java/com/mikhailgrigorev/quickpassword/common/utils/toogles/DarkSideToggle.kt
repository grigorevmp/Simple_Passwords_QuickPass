package com.mikhailgrigorev.quickpassword.common.utils.toogles

import com.mikhailgrigorev.quickpassword.common.utils.Utils

object DarkSideToggle : Toggle() {

    override fun set(value: Boolean) {
        Utils.editPreferences("darkSide", value)
    }

    override fun isEnabled(): Boolean = Utils.sharedPreferences!!.getBoolean("darkSide", true)
}