package com.mikhailgrigorev.quickpassword.ui.main_activity

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository

class MainViewModel(application: Application) : ViewModel() {
    private val passwordCardRepo: PasswordCardRepository = PasswordCardRepository()
    val passwords = passwordCardRepo.allData

    suspend fun favPassword(currentPassword: PasswordCard){
        currentPassword.favorite = !(currentPassword.favorite)
        passwordCardRepo.update(currentPassword)
    }

    fun getPasswordById(id: Int): LiveData<PasswordCard> {
        return passwordCardRepo.getItem(id)
    }
}