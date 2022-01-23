package com.mikhailgrigorev.quickpassword.ui.password_card

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PasswordViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            return PasswordViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}