package com.mikhailgrigorev.quickpassword.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard

@Dao
interface PasswordCardDao {
    /*
        Password card dao class
     */
    @Insert()
    fun insert(card: PasswordCard)

    @Query("select * from password_card where _id = :id")
    fun getByID(id: Int): LiveData<PasswordCard>

    @Query("select * from password_card")
    fun getAll(): LiveData<List<PasswordCard>>

    @Update
    suspend fun update(card: PasswordCard)

    @Delete
    fun delete(card: PasswordCard)

    @Query("DELETE FROM password_card")
    suspend fun deleteAll()
}