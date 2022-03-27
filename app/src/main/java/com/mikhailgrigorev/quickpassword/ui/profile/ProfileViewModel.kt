package com.mikhailgrigorev.quickpassword.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mikhailgrigorev.quickpassword.common.utils.PasswordQuality
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.repository.PasswordCardRepository
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private var passwordCardRepo: PasswordCardRepository
) : ViewModel() {
    val passwords = passwordCardRepo.allData

    val userLogin = getUserLogin()
    @JvmName("getUserLogin1")
    private fun getUserLogin(): LiveData<String> = MutableLiveData(Utils.getLogin())

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