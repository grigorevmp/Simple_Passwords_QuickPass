package com.mikhailgrigorev.simple_password.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.simple_password.common.utils.PasswordQuality
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.repository.PasswordCardRepository
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private var passwordCardRepo: PasswordCardRepository
) : ViewModel() {
    val passwords = passwordCardRepo.allData

    fun setLoginData(data: String) {
        userLogin.postValue(data)
    }

    val userLogin = getUserLogin()

    @JvmName("getUserLogin1")
    private fun getUserLogin(): MutableLiveData<String> = MutableLiveData(Utils.accountSharedPrefs.getLogin())

    fun getPasswordNumberWithQuality(): Triple<LiveData<Int>, LiveData<Int>, LiveData<Int>> {
        val correct = passwordCardRepo.getItemsNumberWithQuality(PasswordQuality.HIGH.value)
        val notSafe = passwordCardRepo.getItemsNumberWithQuality(PasswordQuality.LOW.value)
        val negative = passwordCardRepo.getItemsNumberWithQuality(PasswordQuality.MEDIUM.value)
        return Triple(correct, notSafe, negative)
    }

    fun getItemsNumber() = passwordCardRepo.getItemsNumber()
    fun getItemsNumberWith2fa() = passwordCardRepo.getItemsNumberWith2fa()
    fun getItemsNumberWithEncrypted() = passwordCardRepo.getItemsNumberWithEncrypted()
    fun getItemsNumberWithTimeLimit() = passwordCardRepo.getItemsNumberWithTimeLimit()
    fun getPinItems() = passwordCardRepo.getPinItems()
}