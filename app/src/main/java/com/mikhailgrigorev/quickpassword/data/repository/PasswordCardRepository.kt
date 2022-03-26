package com.mikhailgrigorev.quickpassword.data.repository

import androidx.lifecycle.LiveData
import com.mikhailgrigorev.quickpassword.data.dao.PasswordCardDao
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import javax.inject.Inject


class PasswordCardRepository @Inject constructor(
    var passwordCardDao: PasswordCardDao
     ) {

    val allData: LiveData<List<PasswordCard>> = passwordCardDao.getAllSortName()

    fun getAll(
        columnName: String = "name",
        isAsc: Boolean = false
    ): LiveData<List<PasswordCard>> {
        return when (columnName) {
            "name" -> passwordCardDao.getAllSortName(isAsc)
            "time" -> passwordCardDao.getAllSortTime(isAsc)
            else -> passwordCardDao.getAllSortName(isAsc)
        }
    }

    fun getAllFromFolder(
        folder: Int
    ): LiveData<List<PasswordCard>> {
        return passwordCardDao.getAllFromFolder(folder)
    }

    fun getItem(id: Int) = passwordCardDao.getByID(id)

    fun getItemByName(
        name: String,
        columnName: String = "name",
        isAsc: Boolean = false
    ): LiveData<List<PasswordCard>> {
        return when (columnName) {
            "name" -> passwordCardDao.getByNameSortName(name, isAsc)
            "time" -> passwordCardDao.getByNameSortTime(name, isAsc)
            else -> passwordCardDao.getAllSortName(isAsc)
        }
    }

    fun getItemByQuality(value: Int,
                         columnName: String = "name",
                         isAsc: Boolean = false
    ): LiveData<List<PasswordCard>> {
        return when (columnName) {
            "name" -> passwordCardDao.getByQualitySortName(value, isAsc)
            "time" -> passwordCardDao.getByQualitySortTime(value, isAsc)
            else -> passwordCardDao.getAllSortName(isAsc)
        }
    }

    fun getItemsNumberWithQuality(value: Int) = passwordCardDao.getItemsNumberWithQuality(value)
    fun getItemsNumber() = passwordCardDao.getItemsNumber()
    fun getItemsNumberWith2fa() = passwordCardDao.getItemsNumberWith2fa()
    fun getItemsNumberWithEncrypted() = passwordCardDao.getItemsNumberWithEncrypted()
    fun getItemsNumberWithTimeLimit() = passwordCardDao.getItemsNumberWithTimeLimit()
    fun getPinItems() = passwordCardDao.getPinItems()

    fun getFavoriteItems() = passwordCardDao.getFavorite()

    fun insert(pcItem: PasswordCard) {
        passwordCardDao.insert(pcItem)
    }

    suspend fun update(pcItem: PasswordCard) {
        passwordCardDao.update(pcItem)
    }

    fun delete(pcItem: PasswordCard) {
        passwordCardDao.delete(pcItem)
    }

    suspend fun deleteAll() {
        passwordCardDao.deleteAll()
    }

}