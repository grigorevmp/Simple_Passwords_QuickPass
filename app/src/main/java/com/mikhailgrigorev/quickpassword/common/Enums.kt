package com.mikhailgrigorev.quickpassword.common

enum class PasswordGettingType {
    All, ByName, ByQuality
}

enum class PasswordCategory(val value: Int) {
    CORRECT(1),
    NEGATIVE(2),
    NOT_SAFE(3)
}
