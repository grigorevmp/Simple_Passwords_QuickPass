package com.mikhailgrigorev.quickpassword.ui.password_card

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.data.repository.FolderRepository
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository
import javax.inject.Inject


class PasswordViewModel @Inject constructor(
    private var passwordCardRepo: PasswordCardRepository,
    private var folderRepo: FolderRepository
) : ViewModel() {
    val passwords = passwordCardRepo.allData
    val folders = folderRepo.allData
    var currentPassword: PasswordCard? = null

    fun addPassword(password: PasswordCard) {
        passwordCardRepo.insert(password)
    }

    fun insertCard(item: FolderCard) {
        folderRepo.insert(item)
    }

    fun getFolder(id: Int) = folderRepo.getItem(id)

    suspend fun updatePassword(password: PasswordCard) {
        passwordCardRepo.update(password)
    }

    fun deletePassword() {
        if (currentPassword != null) {
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