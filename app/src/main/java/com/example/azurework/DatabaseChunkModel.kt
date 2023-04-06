package com.example.azurework

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ChunkDB")
data class DatabaseChunkModel(
    @PrimaryKey(autoGenerate = false)
    var chunkId: Long, //Primary key
    var filePath: String,
    var downloadStatus: String,
    var chunkName: String,
    var lowerRange: Long,
    var upperRange: Long,
    var size: Long
)
