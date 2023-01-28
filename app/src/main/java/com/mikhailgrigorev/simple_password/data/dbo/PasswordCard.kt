package com.mikhailgrigorev.simple_password.data.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.mikhailgrigorev.simple_password.common.utils.PasswordQuality
import java.lang.reflect.Type


@Entity(tableName = "keyValue")
data class CustomField(
    @SerializedName("key")
    var key: String,
    @SerializedName("value")
    var value: String,
)

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
    var same_with: String = "",
    @SerializedName("custom_field")
    var custom_field: List<CustomField> = emptyList()
)

object CustomFieldConverters {
    @TypeConverter
    fun fromCustomFieldList(value: String): List<CustomField> {
        val listType: Type = object : TypeToken<List<CustomField>>() {}.type
        var subValue = value
        if (subValue == "") subValue = "[]"
        return Gson().fromJson(subValue, listType)
    }

    @TypeConverter
    fun fromCustomFieldList(list: List<CustomField>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}