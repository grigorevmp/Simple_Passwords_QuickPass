package com.mikhailgrigorev.quickpassword.common.utils

import com.mikhailgrigorev.quickpassword.common.Application

object Utils {
    private var application: Application? = null

    fun init(application: Application) {
        Utils.application = application
    }
}