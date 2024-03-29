package com.mikhailgrigorev.simple_password.ui.main_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.simple_password.common.utils.PasswordGettingType
import com.mikhailgrigorev.simple_password.common.utils.PasswordQuality
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.dbo.FolderCard
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard
import com.mikhailgrigorev.simple_password.data.repository.FolderRepository
import com.mikhailgrigorev.simple_password.data.repository.PasswordCardRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private var passwordCardRepo: PasswordCardRepository,
    private var folderRepo: FolderRepository
) : ViewModel() {

    val passwords = passwordCardRepo.allData

    val folders = folderRepo.allData



    fun insertCard(item: FolderCard) {
        folderRepo.insert(item)
    }

    fun insertPassword(item: PasswordCard) {
        passwordCardRepo.insert(item)
    }

    suspend fun updateCard(item: FolderCard) {
        folderRepo.update(item)
    }

    fun deleteCard(item: FolderCard) {
        folderRepo.delete(item)
    }

    val userLogin = getUserLogin()

    fun setLoginData(data: String) {
        userLogin.postValue(data)
    }

    fun getPasswordNumberWithQuality(): Triple<LiveData<Int>, LiveData<Int>, LiveData<Int>> {
        val correct = passwordCardRepo.getItemsNumberWithQuality(PasswordQuality.HIGH.value)
        val notSafe = passwordCardRepo.getItemsNumberWithQuality(PasswordQuality.LOW.value)
        val negative = passwordCardRepo.getItemsNumberWithQuality(PasswordQuality.MEDIUM.value)
        return Triple(correct, notSafe, negative)
    }

    fun getFavoriteItems() = passwordCardRepo.getFavoriteItems()

    fun getItemsNumber() = passwordCardRepo.getItemsNumber()

    fun getItemsNumberWith2fa() = passwordCardRepo.getItemsNumberWith2fa()

    fun getItemsNumberWithEncrypted() = passwordCardRepo.getItemsNumberWithEncrypted()



    @JvmName("getUserLogin1")
    private fun getUserLogin(): MutableLiveData<String> = MutableLiveData(Utils.accountSharedPrefs.getLogin())

    fun getPasswords(
        type: PasswordGettingType = PasswordGettingType.All,
        name: String = "",
        value: Int = 0,
        columnName: String = "name",
        isAsc: Boolean = false
    ) = when (type) {
        PasswordGettingType.All -> getAllPasswords(columnName, isAsc)
        PasswordGettingType.ByName -> getPasswordByName(name, columnName, isAsc)
        PasswordGettingType.ByQuality -> getPasswordByQuality(value, columnName, isAsc)
    }

    private fun getPasswordByName(
        name: String,
        columnName: String = "name",
        isAsc: Boolean = false
    ) = passwordCardRepo.getItemByName(name, columnName, isAsc)

    private fun getAllPasswords(
        columnName: String = "name",
        isAsc: Boolean = false
    ) = passwordCardRepo.getAll(columnName, isAsc)

    private fun getPasswordByQuality(
        value: Int,
        columnName: String = "name",
        isAsc: Boolean = false
    ) = passwordCardRepo.getItemByQuality(value, columnName, isAsc)



    suspend fun favPassword(currentPassword: PasswordCard) {
        currentPassword.favorite = !(currentPassword.favorite)
        passwordCardRepo.update(currentPassword)
    }

    suspend fun deleteItem(item: PasswordCard) = passwordCardRepo.delete(item)
}