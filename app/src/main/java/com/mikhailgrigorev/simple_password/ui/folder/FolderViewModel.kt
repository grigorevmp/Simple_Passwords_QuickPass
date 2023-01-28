package com.mikhailgrigorev.simple_password.ui.folder

import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.simple_password.data.repository.PasswordCardRepository
import javax.inject.Inject

class FolderViewModel @Inject constructor(
    private var passwordCardRepo: PasswordCardRepository
) : ViewModel() {

    fun getPasswordsFromFolder(id: Int)= passwordCardRepo.getAllFromFolder(id)

    fun getItemsNumber() = passwordCardRepo.getItemsNumber()
}