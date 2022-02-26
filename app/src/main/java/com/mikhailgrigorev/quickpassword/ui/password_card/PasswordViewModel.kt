package com.mikhailgrigorev.quickpassword.ui.password_card

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository


class PasswordViewModel : ViewModel() {
    private val passwordCardRepo: PasswordCardRepository = PasswordCardRepository()
    val passwords = passwordCardRepo.allData
    var currentPassword: PasswordCard? = null

    fun addPassword(password: PasswordCard){
        passwordCardRepo.insert(password)
    }

    suspend fun updatePassword(password: PasswordCard){
        passwordCardRepo.update(password)
    }

    fun deletePassword(){
        if(currentPassword != null) {
            passwordCardRepo.delete(currentPassword!!)
        }
    }

    suspend fun favPassword(){
        if(currentPassword != null) {
            currentPassword!!.favorite = !(currentPassword!!.favorite)
            passwordCardRepo.update(currentPassword!!)
        }
    }

    fun getPasswordById(id: Int): LiveData<PasswordCard> {
        return passwordCardRepo.getItem(id)
    }
}