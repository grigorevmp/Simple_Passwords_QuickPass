package com.mikhailgrigorev.quickpassword.data.repository

import androidx.lifecycle.LiveData
import com.mikhailgrigorev.quickpassword.data.database.FolderCardDatabase
import com.mikhailgrigorev.quickpassword.data.entity.FolderCard


class FolderRepository {
    private val fDatabase = FolderCardDatabase.getInstance()
    private val fDao = fDatabase.FolderDao()
    val allData: LiveData<List<FolderCard>> = fDao.getAll()

    fun getItem(id: Int) = fDao.getByID(id)

    fun insert(fItem: FolderCard) {
        fDao.insert(fItem)
    }

    suspend fun update(fItem: FolderCard) {
        fDao.update(fItem)
    }

    fun delete(fItem: FolderCard) {
        fDao.delete(fItem)
    }

    suspend fun deleteAll() {
        fDao.deleteAll()
    }

}