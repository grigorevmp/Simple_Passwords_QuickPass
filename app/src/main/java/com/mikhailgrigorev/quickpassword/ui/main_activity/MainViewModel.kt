package com.mikhailgrigorev.quickpassword.ui.main_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.common.PasswordCategory
import com.mikhailgrigorev.quickpassword.common.PasswordGettingType
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository

class MainViewModel() : ViewModel() {
    private val passwordCardRepo: PasswordCardRepository = PasswordCardRepository()
    val passwords = passwordCardRepo.allData

    suspend fun favPassword(currentPassword: PasswordCard) {
        currentPassword.favorite = !(currentPassword.favorite)
        passwordCardRepo.update(currentPassword)
    }

    fun getPasswordById(id: Int): LiveData<PasswordCard> {
        return passwordCardRepo.getItem(id)
    }

    private fun getPasswordByName(name: String) = passwordCardRepo.getItemByName(name)

    private fun getPasswordByQuality(value: Int) = passwordCardRepo.getItemByQuality(value)

    fun getPasswordNumberWithQuality(): Triple<LiveData<Int>, LiveData<Int>, LiveData<Int>> {
        val correct = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.CORRECT.value)
        val notSafe = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.NOT_SAFE.value)
        val negative = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.NEGATIVE.value)
        return Triple(correct, notSafe, negative)
    }

    fun getFavoriteItems() = passwordCardRepo.getFavoriteItems()

    fun deleteItem(item: PasswordCard) = passwordCardRepo.delete(item)

    fun getPasswords(
        type: PasswordGettingType = PasswordGettingType.All,
        name: String = "", value: Int = 0
    ) =
            when (type) {
                PasswordGettingType.All -> passwords
                PasswordGettingType.ByName -> getPasswordByName(name)
                PasswordGettingType.ByQuality -> getPasswordByQuality(value)
            }
}