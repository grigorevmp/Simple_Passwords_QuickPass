package com.mikhailgrigorev.simple_password.data.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "folder_card")
data class FolderCard(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    @SerializedName("name")
    var name: String,
    @SerializedName("description")
    var description: String,
    @SerializedName("image_src")
    var imageSrc: String = "",
    @SerializedName("color_tag")
    var colorTag: String = "",
)