package com.mikhailgrigorev.quickpassword.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.mikhailgrigorev.quickpassword.common.PasswordCategory

@Entity(tableName = "password_card")
data class PasswordCard(
    /*
        Password card database class
     */
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    @SerializedName("name")
    var name: String,
    @SerializedName("password")
    var password: String,
    @SerializedName("use_2fa")
    var use_2fa: Boolean,
    @SerializedName("use_time")
    var use_time: Boolean,
    @SerializedName("time")
    var time: String,
    @SerializedName("description")
    var description: String,
    @SerializedName("tags")
    var tags: String,
    @SerializedName("groups")
    var groups: String,
    @SerializedName("login")
    var login: String,
    @SerializedName("encrypted")
    var encrypted: Boolean,
    @SerializedName("favorite")
    var favorite: Boolean = false,
    @SerializedName("quality")
    var quality: Int = PasswordCategory.NOT_SAFE.value,
    @SerializedName("same_with")
    var same_with: String = ""
)