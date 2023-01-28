package com.mikhailgrigorev.simple_password.common.utils.toogles

import com.mikhailgrigorev.simple_password.common.utils.Utils

object AutoCopyToggle : Toggle() {

    override fun set(value: Boolean) {
        Utils.editPreferences("prefAutoCopyKey", value)
    }

    override fun isEnabled(): Boolean = Utils.sharedPreferences!!.getBoolean("prefAutoCopyKey", true)
}