package com.mikhailgrigorev.simple_password.data.repository

import androidx.lifecycle.LiveData
import com.mikhailgrigorev.simple_password.data.dao.FolderDao
import com.mikhailgrigorev.simple_password.data.dbo.FolderCard
import javax.inject.Inject


class FolderRepository @Inject constructor(
    private var folderDao: FolderDao
) {
    val allData: LiveData<List<FolderCard>> = folderDao.getAll()

    fun getItem(id: Int) = folderDao.getByID(id)

    fun insert(fItem: FolderCard) {
        folderDao.insert(fItem)
    }

    suspend fun update(fItem: FolderCard) {
        folderDao.update(fItem)
    }

    fun delete(fItem: FolderCard) {
        folderDao.delete(fItem)
    }

    suspend fun deleteAll() {
        folderDao.deleteAll()
    }

}