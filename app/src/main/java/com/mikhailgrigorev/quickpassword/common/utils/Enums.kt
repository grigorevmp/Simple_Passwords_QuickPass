package com.mikhailgrigorev.quickpassword.common.utils

enum class PasswordGettingType {
    All, ByName, ByQuality
}

enum class PasswordQuality(val value: Int) {
    HIGH(1),
    MEDIUM(2),
    LOW(3)
}
