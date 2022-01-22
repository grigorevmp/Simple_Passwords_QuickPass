package com.mikhailgrigorev.quickpassword.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "password_card")
data class PasswordCard(
    /*
        Password card database class
     */
    @PrimaryKey(autoGenerate = true)
    var _id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("pass")
    val pass: String,
    @SerializedName("_2fa")
    val _2fa: Boolean,
    @SerializedName("utime")
    val utime: Boolean,
    @SerializedName("time")
    val time: Date,
    @SerializedName("description")
    val description: String,
    @SerializedName("tags")
    val tags: String,
    @SerializedName("groups")
    val groups: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("cipher")
    val cipher: String
)