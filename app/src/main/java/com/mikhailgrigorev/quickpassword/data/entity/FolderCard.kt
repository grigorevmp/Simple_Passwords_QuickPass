package com.mikhailgrigorev.quickpassword.data.entity

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
    @SerializedName("color_tag")
    var colorTag: String,
)