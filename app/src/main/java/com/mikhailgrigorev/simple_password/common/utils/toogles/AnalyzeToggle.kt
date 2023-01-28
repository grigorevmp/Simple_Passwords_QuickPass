package com.mikhailgrigorev.simple_password.common.utils.toogles

import com.mikhailgrigorev.simple_password.common.utils.Utils

object AnalyzeToggle : Toggle() {

    override fun set(value: Boolean) {
        Utils.editPreferences("useAnalyze", value)
    }

    override fun isEnabled(): Boolean = Utils.sharedPreferences!!.getBoolean("useAnalyze", true)
}