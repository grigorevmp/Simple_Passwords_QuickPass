package com.mikhailgrigorev.quickpassword.data.repository

import androidx.lifecycle.LiveData
import com.mikhailgrigorev.quickpassword.data.database.PasswordCardDatabase
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard


class PasswordCardRepository {
    private val pcDatabase = PasswordCardDatabase.getInstance()
    private val pcDao = pcDatabase.PasswordCardDao()
    val allData: LiveData<List<PasswordCard>> = pcDao.getAll()

    fun getItem(id: Int) = pcDao.getByID(id)

    fun getItemByName(name: String) = pcDao.getByName(name)

    fun getItemByQuality(value: Int) = pcDao.getByQuality(value)

    fun getItemsNumberWithQuality(value: Int) = pcDao.getItemsNumberWithQuality(value)

    fun getFavoriteItems() = pcDao.getFavorite()

    fun insert(pcItem: PasswordCard) {
        pcDao.insert(pcItem)
    }

    suspend fun update(pcItem: PasswordCard) {
        pcDao.update(pcItem)
    }

    fun delete(pcItem: PasswordCard) {
        pcDao.delete(pcItem)
    }

    suspend fun deleteAll() {
        pcDao.deleteAll()
    }

}