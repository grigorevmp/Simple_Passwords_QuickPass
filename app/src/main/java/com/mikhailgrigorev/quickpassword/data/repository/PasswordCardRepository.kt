package com.mikhailgrigorev.quickpassword.data.repository

import androidx.lifecycle.LiveData
import com.mikhailgrigorev.quickpassword.data.database.PasswordCardDatabase
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard


class PasswordCardRepository {
    private val pcDatabase = PasswordCardDatabase.getInstance()
    private val pcDao = pcDatabase.PasswordCardDao()
    val allData: LiveData<List<PasswordCard>> = pcDao.getAllSortName()

    fun getAll(
        columnName: String = "name",
        isAsc: Boolean = false
    ): LiveData<List<PasswordCard>> {
        return when (columnName) {
            "name" -> pcDao.getAllSortName(isAsc)
            "time" -> pcDao.getAllSortTime(isAsc)
            else -> pcDao.getAllSortName(isAsc)
        }
    }

    fun getAllFromFolder(
        folder: Int
    ): LiveData<List<PasswordCard>> {
        return pcDao.getAllFromFolder(folder)
    }

    fun getItem(id: Int) = pcDao.getByID(id)

    fun getItemByName(
        name: String,
        columnName: String = "name",
        isAsc: Boolean = false
    ): LiveData<List<PasswordCard>> {
        return when (columnName) {
            "name" -> pcDao.getByNameSortName(name, isAsc)
            "time" -> pcDao.getByNameSortTime(name, isAsc)
            else -> pcDao.getAllSortName(isAsc)
        }
    }

    fun getItemByQuality(value: Int,
                         columnName: String = "name",
                         isAsc: Boolean = false
    ): LiveData<List<PasswordCard>> {
        return when (columnName) {
            "name" -> pcDao.getByQualitySortName(value, isAsc)
            "time" -> pcDao.getByQualitySortTime(value, isAsc)
            else -> pcDao.getAllSortName(isAsc)
        }
    }

    fun getItemsNumberWithQuality(value: Int) = pcDao.getItemsNumberWithQuality(value)
    fun getItemsNumber() = pcDao.getItemsNumber()
    fun getItemsNumberWith2fa() = pcDao.getItemsNumberWith2fa()
    fun getItemsNumberWithEncrypted() = pcDao.getItemsNumberWithEncrypted()
    fun getItemsNumberWithTimeLimit() = pcDao.getItemsNumberWithTimeLimit()
    fun getPinItems() = pcDao.getPinItems()

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