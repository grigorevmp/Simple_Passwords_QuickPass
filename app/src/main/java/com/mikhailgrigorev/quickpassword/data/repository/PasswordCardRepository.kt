package com.mikhailgrigorev.quickpassword.data.repository

import androidx.lifecycle.LiveData
import com.mikhailgrigorev.quickpassword.data.database.PasswordCardDatabase
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard


class PasswordCardRepository {
    private val pcDatabase = PasswordCardDatabase.getInstance()
    private val pcDao = pcDatabase.PasswordCardDao()
    val allData: LiveData<List<PasswordCard>> = pcDao.getAll()

    fun getItem(id: Int) = pcDao.getByID(id)

    suspend fun insert(pcItem: PasswordCard) {
        pcDao.insert(pcItem)
    }

    suspend fun update(pcItem: PasswordCard) {
        pcDao.update(pcItem)
    }

    suspend fun delete(pcItem: PasswordCard) {
        pcDao.delete(pcItem)
    }

    suspend fun deleteAll() {
        pcDao.deleteAll()
    }

}