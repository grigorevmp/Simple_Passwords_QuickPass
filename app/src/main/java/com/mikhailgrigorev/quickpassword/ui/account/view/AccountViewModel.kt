package com.mikhailgrigorev.quickpassword.ui.account.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.common.PasswordCategory
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository

class AccountViewModel : ViewModel() {
    private val passwordCardRepo: PasswordCardRepository = PasswordCardRepository()
    val passwords = passwordCardRepo.allData

    val userLogin = getUserLogin()
    @JvmName("getUserLogin1")
    private fun getUserLogin(): LiveData<String> = MutableLiveData(Utils.getLogin())

    fun getPasswordNumberWithQuality(): Triple<LiveData<Int>, LiveData<Int>, LiveData<Int>> {
        val correct = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.CORRECT.value)
        val notSafe = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.NOT_SAFE.value)
        val negative = passwordCardRepo.getItemsNumberWithQuality(PasswordCategory.NEGATIVE.value)
        return Triple(correct, notSafe, negative)
    }

    fun getItemsNumber() = passwordCardRepo.getItemsNumber()
    fun getItemsNumberWith2fa() = passwordCardRepo.getItemsNumberWith2fa()
    fun getItemsNumberWithEncrypted() = passwordCardRepo.getItemsNumberWithEncrypted()
    fun getItemsNumberWithTimeLimit() = passwordCardRepo.getItemsNumberWithTimeLimit()
    fun getPinItems() = passwordCardRepo.getPinItems()
}