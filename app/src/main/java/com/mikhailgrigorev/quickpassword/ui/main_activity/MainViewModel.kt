package com.mikhailgrigorev.quickpassword.ui.main_activity

import android.app.Application
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository

class MainViewModel(application: Application) : ViewModel() {
    private val passwordCardRepo: PasswordCardRepository = PasswordCardRepository()
    private val passwords = passwordCardRepo.allData

}