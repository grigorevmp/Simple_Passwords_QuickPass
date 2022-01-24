package com.mikhailgrigorev.quickpassword.ui.password_card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PasswordViewModelFactory :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            return PasswordViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}