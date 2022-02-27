package com.mikhailgrigorev.quickpassword.ui.main_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.common.PasswordCategory
import com.mikhailgrigorev.quickpassword.common.PasswordGettingType
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.data.repository.FolderRepository
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository

class MainViewModel() : ViewModel() {
    private val passwordCardRepo: PasswordCardRepository = PasswordCardRepository()
    private val folderRepo: FolderRepository = FolderRepository()
    val passwords = passwordCardRepo.allData
    val folders = folderRepo.allData

    fun insertCard(item: FolderCard) {
        folderRepo.insert(item)
    }

    suspend fun favPassword(currentPassword: PasswordCard) {
        currentPassword.favorite = !(currentPassword.favorite)
        passwordCardRepo.update(currentPassword)
    }

    fun getPasswordById(id: Int): LiveData<PasswordCard> {
        return passwordCardRepo.getItem(id)
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

    private fun getAllFolders() = folderRepo.allData

    private fun getPasswordByQuality(
        value: Int,
        columnName: String = "name",
        isAsc: Boolean = false
    ) = passwordCardRepo.getItemByQuality(value, columnName, isAsc)

    fun getPasswordNumberWithQuality(): Triple<LiveData<Int>, LiveData<Int>, LiveData<Int>> {
        val correct = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.CORRECT.value)
        val notSafe = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.NOT_SAFE.value)
        val negative = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.NEGATIVE.value)
        return Triple(correct, notSafe, negative)
    }

    fun getFavoriteItems() = passwordCardRepo.getFavoriteItems()

    fun getItemsNumber() = passwordCardRepo.getItemsNumber()
    fun getItemsNumberWith2fa() = passwordCardRepo.getItemsNumberWith2fa()
    fun getItemsNumberWithEncrypted() = passwordCardRepo.getItemsNumberWithEncrypted()

    fun deleteItem(item: PasswordCard) = passwordCardRepo.delete(item)

    fun getPasswords(
        type: PasswordGettingType = PasswordGettingType.All,
        name: String = "",
        value: Int = 0,
        columnName: String = "name",
        isAsc: Boolean = false
    ) =
            when (type) {
                PasswordGettingType.All -> getAllPasswords(columnName, isAsc)
                PasswordGettingType.ByName -> getPasswordByName(name, columnName, isAsc)
                PasswordGettingType.ByQuality -> getPasswordByQuality(value, columnName, isAsc)
            }
}