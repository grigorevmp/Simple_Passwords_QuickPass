package com.mikhailgrigorev.simple_password.common.utils.toogles

abstract class Toggle {
    abstract fun isEnabled(): Boolean
    abstract fun set(value: Boolean)
}