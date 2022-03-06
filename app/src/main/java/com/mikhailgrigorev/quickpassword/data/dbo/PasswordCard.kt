package com.mikhailgrigorev.quickpassword.data.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.mikhailgrigorev.quickpassword.common.PasswordQuality

@Entity(tableName = "password_card")
data class PasswordCard(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    @SerializedName("name")
    var name: String,
    @SerializedName("password")
    var image_count: Int = 0,
    @SerializedName("image_count")
    var password: String,
    @SerializedName("use_2fa")
    var use_2fa: Boolean,
    @SerializedName("is_card_pin")
    var is_card_pin: Boolean,
    @SerializedName("use_time")
    var use_time: Boolean,
    @SerializedName("time")
    var time: String,
    @SerializedName("description")
    var description: String,
    @SerializedName("tags")
    var tags: String,
    @SerializedName("folder")
    var folder: Int? = null,
    @SerializedName("login")
    var login: String,
    @SerializedName("encrypted")
    var encrypted: Boolean,
    @SerializedName("favorite")
    var favorite: Boolean = false,
    @SerializedName("quality")
    var quality: Int = PasswordQuality.LOW.value,
    @SerializedName("same_with")
    var same_with: String = ""
)