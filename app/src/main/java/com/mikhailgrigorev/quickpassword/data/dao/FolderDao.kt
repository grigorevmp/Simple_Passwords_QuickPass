package com.mikhailgrigorev.quickpassword.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mikhailgrigorev.quickpassword.data.entity.FolderCard

@Dao
interface FolderDao {

    @Insert()
    fun insert(card: FolderCard)

    @Query(
            "select * from folder_card"
    )
    fun getAll(): LiveData<List<FolderCard>>

    @Query("select * from folder_card where _id = :id")
    fun getByID(id: Int): LiveData<FolderCard>

    // Getting additional data

    @Query("SELECT COUNT(*) FROM folder_card ")
    fun getItemsNumber(): LiveData<Int>


    // Operations with password

    @Update
    suspend fun update(card: FolderCard)

    @Delete
    fun delete(card: FolderCard)

    @Query("DELETE FROM folder_card")
    suspend fun deleteAll()
}