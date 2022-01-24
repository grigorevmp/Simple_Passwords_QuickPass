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

    @Query("SELECT * FROM password_card WHERE name LIKE :search order by favorite")
    fun getByName(search: String): LiveData<List<PasswordCard>>

    @Query("SELECT * FROM password_card WHERE quality = :value order by favorite")
    fun getByQuality(value: Int): LiveData<List<PasswordCard>>

    @Query("SELECT COUNT(*) FROM password_card WHERE quality = :value")
    fun getItemsNumberWithQuality(value: Int): LiveData<Int>

    @Query("SELECT * FROM password_card WHERE favorite = 1")
    fun getFavorite(): LiveData<List<PasswordCard>>

    @Query("select * from password_card order by favorite")
    fun getAll(): LiveData<List<PasswordCard>>

    @Update
    suspend fun update(card: PasswordCard)

    @Delete
    fun delete(card: PasswordCard)

    @Query("DELETE FROM password_card")
    suspend fun deleteAll()
}