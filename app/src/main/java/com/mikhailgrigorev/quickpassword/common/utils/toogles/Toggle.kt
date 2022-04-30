package com.mikhailgrigorev.quickpassword.common.utils.toogles

abstract class Toggle {
    abstract fun isEnabled(): Boolean
    abstract fun set(value: Boolean)
}