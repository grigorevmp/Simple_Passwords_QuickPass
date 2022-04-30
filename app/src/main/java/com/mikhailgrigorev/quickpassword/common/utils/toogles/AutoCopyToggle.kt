package com.mikhailgrigorev.quickpassword.common.utils.toogles

import com.mikhailgrigorev.quickpassword.common.utils.Utils

object AutoCopyToggle : Toggle() {

    override fun set(value: Boolean) {
        Utils.editPreferences("prefAutoCopyKey", value)
    }

    override fun isEnabled(): Boolean = Utils.sharedPreferences!!.getBoolean("prefAutoCopyKey", true)
}