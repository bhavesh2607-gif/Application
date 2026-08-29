package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "images",
    indices = [
        Index(value = ["albumId"]),
        Index(value = ["isFavorite"]),
        Index(value = ["dateAdded"])
    ]
)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val title: String,
    val description: String = "",
    val albumId: Long? = null,
    val tags: String = "", // Comma-separated list of tags
    val dateAdded: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false
) {
    fun getTagList(): List<String> =
        if (tags.isBlank()) emptyList()
        else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
